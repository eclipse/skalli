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
package org.eclipse.skalli.core.internal.users;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("ldap")
public class LDAPConfig {

    private String password;
    private String username;
    private String hostname;
    private String ctxFactory;
    private String usersGroup;
    private String cacheSize;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getCtxFactory() {
        return ctxFactory;
    }

    public void setCtxFactory(String ctxFactory) {
        this.ctxFactory = ctxFactory;
    }

    public String getUsersGroup() {
        return usersGroup;
    }

    public void setUsersGroup(String usersGroup) {
        this.usersGroup = usersGroup;
    }

    public String getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(String cacheSize) {
        this.cacheSize = cacheSize;
    }
}
