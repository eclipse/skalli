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
package org.eclipse.skalli.core.destination;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("proxy")
public class ProxyConfig {

    private String host;
    private String port;
    private String hostSSL;
    private String portSSL;
    private String nonProxyHosts;

    // do not remove: required by xstream
    public ProxyConfig() {
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getHostSSL() {
        return hostSSL;
    }

    public void setHostSSL(String hostSSL) {
        this.hostSSL = hostSSL;
    }

    public String getPortSSL() {
        return portSSL;
    }

    public void setPortSSL(String portSSL) {
        this.portSSL = portSSL;
    }

    public void setNonProxyHosts(String nonProxyHosts) {
        this.nonProxyHosts = nonProxyHosts;
    }

    public String getNonProxyHosts() {
        return nonProxyHosts;
    }

}
