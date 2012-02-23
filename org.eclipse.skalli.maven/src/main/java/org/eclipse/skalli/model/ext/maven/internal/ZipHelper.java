package org.eclipse.skalli.model.ext.maven.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipHelper {

    public static InputStream getEntry(ZipInputStream zin, String fileName) throws IOException {
        ZipEntry ze = null;
        ByteArrayOutputStream outStream = null;

        while ((ze = zin.getNextEntry()) != null && outStream == null) {
            try {
                if (ze.getName() != null && fileName.equals(ze.getName().replace("\\", "/"))) {
                    outStream = readEntry(zin);
                }
            } finally {
                zin.closeEntry();
            }
        }

        if (outStream == null) {
            throw new IOException("the zip did not contain the requested file =\"" + fileName + "\"");
        }
        return new ByteArrayInputStream(outStream.toByteArray());
    }

    private static ByteArrayOutputStream readEntry(ZipInputStream zin) throws IOException {
        ByteArrayOutputStream outStream;
        outStream = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        for (int read = zin.read(buf); read >= 0; read = zin.read(buf)) {
            outStream.write(buf, 0, read);
        }
        return outStream;
    }
}
