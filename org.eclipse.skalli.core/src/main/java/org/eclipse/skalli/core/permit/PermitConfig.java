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
package org.eclipse.skalli.core.permit;

import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.text.StrSubstitutor;
import org.eclipse.skalli.services.permit.Permit;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("permit")
public class PermitConfig {

    private UUID uuid;
    private String type;
    private String action;
    private String path;
    private int level;
    private boolean override;
    private String owner;

    private transient int pos;

    // do not remove: required by xstream
    public PermitConfig() {
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

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

    public boolean isOverride() {
        return override;
    }

    public void setOverride(boolean override) {
        this.override = override;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((action == null) ? 0 : action.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + level;
        result = prime * result + ((owner == null) ? 0 : owner.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PermitConfig other = (PermitConfig) obj;
        if (uuid != null && other.uuid != null) {
            return uuid.equals(other.uuid);
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        if (action == null) {
            if (other.action != null) {
                return false;
            }
        } else if (!action.equals(other.action)) {
            return false;
        }
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }
        if (level != other.level) {
            return false;
        }
        if (owner == null) {
            if (other.owner != null) {
                return false;
            }
        } else if (!owner.equals(other.owner)) {
            return false;
        }
        return true;
    }

    @SuppressWarnings("nls")
    @Override
    public String toString() {
        return "PermitConfig [uuid=" + uuid + ", type=" + type + ", action=" + action + ", path=" + path + ", level="
                + level + ", override=" + override + ", owner=" + owner + ", pos=" + pos + "]";
    }

}
