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
package org.eclipse.skalli.core.services.permit;

import java.util.Map;

import org.apache.commons.lang.text.StrSubstitutor;
import org.eclipse.skalli.services.permit.Permit;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("permit")
public class PermitConfig {

    private String action;
    private String path;
    private int level;
    private String owner;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Permit asPermit() {
        return asPermit(null);
    }

    public Permit asPermit(Map<String,String> properties) {
        if (properties == null || properties.isEmpty()) {
            return new Permit(level, action, path);
        }
        StrSubstitutor subst = new StrSubstitutor(properties);
        return new Permit(level, action, subst.replace(path));
    }
}
