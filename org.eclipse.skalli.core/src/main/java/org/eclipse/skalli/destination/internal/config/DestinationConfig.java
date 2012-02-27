/*******************************************************************************
 * Copyright (c) 2010 - ${year} SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.destination.internal.config;

import org.eclipse.skalli.services.configuration.rest.Protect;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("destination")
public class DestinationConfig {
    private String id;
    private String authenticationMethod;
    private String pattern;
    private String user;
    @Protect
    private String password;
    private String service;

    public DestinationConfig() {
    }

    public DestinationConfig(String id) {
        this.id = id;
    }

    public DestinationConfig(String id, String pattern, String user, String password) {
        this.id = id;
        this.pattern = pattern;
        this.user = user;
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getAuthenticationMethod() {
        return authenticationMethod;
    }

    public void setAuthenticationMethod(String authenticationMethod) {
        this.authenticationMethod = authenticationMethod;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
