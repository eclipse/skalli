/*******************************************************************************
 * Copyright (c) 2010-2014 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.core.storage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.NoSuchElementException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

public class Historian {

    private static final String HISTORY_FILE = ".history"; //$NON-NLS-1$
    private static final String CRLF = "\r\n"; //$NON-NLS-1$

    private final File historyFile;

    public Historian(File storageBase) {
        this.historyFile = new File(storageBase, HISTORY_FILE);
    }

    void historize(File file) throws IOException {
        if (file != null && file.exists()) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = new BufferedInputStream(new FileInputStream(file));
                out = new BufferedOutputStream(new FileOutputStream(historyFile, historyFile.exists()));
                String header = MessageFormat.format("{0}:{1}:{2}",  //$NON-NLS-1$
                        getNextEntryName(FilenameUtils.getBaseName(file.getAbsolutePath())),
                        Long.toString(file.length()),
                        Long.toString(System.currentTimeMillis()));
                out.write(header.getBytes("UTF-8")); //$NON-NLS-1$
                out.write(CRLF.getBytes("UTF-8")); //$NON-NLS-1$
                IOUtils.copy(in, out);
                out.write(CRLF.getBytes("UTF-8")); //$NON-NLS-1$
            } finally {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
            }
        }
    }

    String getNextEntryName(String fileName) throws IOException {
        int count = 0;
        InputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(historyFile));
            String header = readLine(in);
            while (header.length() > 0) {
                String[] parts = StringUtils.split(header, ':');
                if (parts[0].startsWith(fileName)) {
                    ++count;
                }
                skip(in, Integer.valueOf(parts[2]) + CRLF.length());
                header = readLine(in);
            }
        } finally {
            IOUtils.closeQuietly(in);
        }
        return fileName + ":" + count; //$NON-NLS-1$
    }

    HistoryIterator getHistory(String fileName) {
        return new HistoryIterator(fileName);
    }

    class HistoryEntry {
        private String id;
        private String content;
        private int version;
        private long timestamp;

        public HistoryEntry(String id, String content, int version, long timestamp) {
            this.id = id;
            this.content = content;
            this.version = version;
            this.timestamp = timestamp;
        }

        public String getId() {
            return id;
        }

        public String getContent() {
            return content;
        }

        public int getVersion() {
            return version;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    class HistoryIterator {
        private InputStream in;
        private String id;
        private byte[] content;
        private String[] parts;

        HistoryIterator(String id) {
            this.id = id;
        }

        public boolean hasNext() throws IOException {
            if (!historyFile.exists()) {
                return false;
            }
            if (in == null) {
                in = new BufferedInputStream(new FileInputStream(historyFile));
            }
            if (content == null) {
                String header = readLine(in);
                while (header.length() > 0) {
                    parts = StringUtils.split(header, ':');
                    if (id == null || id.equals(parts[0])) {
                        content = new byte[Integer.valueOf(parts[2])];
                        read(in, content);
                        skip(in, CRLF.length());
                        break;
                    }
                    header = readLine(in);
                }
            }
            return content != null;
        }

        public HistoryEntry next() throws IOException {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            HistoryEntry next = new HistoryEntry(parts[0], new String(content, "UTF-8"), //$NON-NLS-1$
                    Integer.valueOf(parts[1]), Long.valueOf(parts[3]));
            content = null;
            return next;
        }

        public void close() {
            IOUtils.closeQuietly(in);
        }
    }

    private static void skip(InputStream in, long n) throws IOException {
        while (n > 0) {
            n -= in.skip(n);
        }
    }

    private static void read(InputStream in, byte[] b) throws IOException {
        int off = 0;
        int len = b.length;
        while (len > 0) {
            int n = in.read(b, off, len);
            len -= n;
            off += n;
        }
    }

    private static String readLine(InputStream in) throws IOException {
        StringBuffer b = new StringBuffer();
        String result = null;
        boolean done = false;
        while (!done) {
            int next = in.read();
            switch (next) {
            case -1:
                result = b.toString();
                done = true;
                break;
            case '\n':
                if (b.length() == 0) {
                    break;
                }
                result = b.toString();
                done = true;
                break;
            case '\r':
                break;
            default:
                b.append((char) next);
            }
        }
        return result;
    }
}
