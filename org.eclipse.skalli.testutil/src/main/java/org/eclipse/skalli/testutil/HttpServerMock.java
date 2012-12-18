/*******************************************************************************
 * Copyright (c) 2010, 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.testutil;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

@SuppressWarnings("nls")
public class HttpServerMock implements Runnable {

    private String host = "localhost";
    private int port = 12421; // just an arbitrary port
    private Thread t;
    private Map<String, String> bodies = new HashMap<String, String>();
    private ServerSocket server;

    public HttpServerMock() {
        String systemPropertyHost = System.getProperty("httpservermock.host");
        if (StringUtils.isNotBlank(systemPropertyHost)) {
            this.host = systemPropertyHost;
        }
        String sytemPropertyPort = System.getProperty("httpservermock.port");
        if (StringUtils.isNotBlank(sytemPropertyPort) && StringUtils.isNumeric(sytemPropertyPort)) {
            this.port = Integer.valueOf(sytemPropertyPort);
        }
    }

    public HttpServerMock(Map<String, String> map) {
        this();
        for (String contentId : map.keySet()) {
            addContent(contentId, map.get(contentId));
        }
    }

    public void addContent(String contentId, String content) {
        synchronized (bodies) {
            bodies.put(contentId, content);
        }
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public void start() {
        if (t != null) {
            return;
        }
        t = new Thread(this);
        t.start();
    }

    public void stop() {
        if (t == null) {
            return;
        }
        try {
            synchronized (server) {
                server.close();
            }
            t.interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        BufferedReader request = null;
        DataOutputStream response = null;
        try {
            server = new ServerSocket(port);
            while (true) {
                Socket connection = server.accept();

                request = new BufferedReader(new InputStreamReader(connection.getInputStream(), "ISO8859_1"));
                response = new DataOutputStream(connection.getOutputStream());

                String httpCode;
                String contentId = "";
                String requestLine = request.readLine();

                if (!StringUtils.startsWithIgnoreCase(requestLine, "GET")) {
                    httpCode = "405";
                } else {
                    String path = StringUtils.split(requestLine, " ")[1];
                    int n = StringUtils.lastIndexOf(path, "/");
                    contentId = StringUtils.substring(path, 1, n);
                    httpCode = StringUtils.substring(path, n + 1);
                }

                String content = bodies.get(contentId);
                StringBuffer sb = new StringBuffer();
                sb.append("HTTP/1.1 ").append(httpCode).append(" CustomStatus\r\n");
                sb.append("Server: MiniMockUnitServer\r\n");
                sb.append("Content-Type: text/plain\r\n");
                if (content != null) {
                    sb.append("Content-Length: ").append(content.length()).append("\r\n");
                }
                sb.append("Connection: close\r\n");
                sb.append("\r\n");
                if (content != null) {
                    sb.append(content);
                }

                response.writeBytes(sb.toString());
                IOUtils.closeQuietly(response);
            }
        } catch (IOException e) {
            IOUtils.closeQuietly(request);
            IOUtils.closeQuietly(response);
        } finally {
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
