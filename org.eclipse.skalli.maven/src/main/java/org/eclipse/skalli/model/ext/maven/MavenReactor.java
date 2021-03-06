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
package org.eclipse.skalli.model.ext.maven;

import java.util.Collection;
import java.util.TreeSet;

import org.eclipse.skalli.commons.ComparatorUtils;
import org.eclipse.skalli.model.Derived;

public class MavenReactor {

    @Derived
    public static final String PROPERTY_COORDINATE = "coordinate"; //$NON-NLS-1$

    @Derived
    public static final String PROPERTY_MODULES = "modules"; //$NON-NLS-1$

    private MavenModule coordinate;
    private TreeSet<MavenModule> modules = new TreeSet<MavenModule>();

    public MavenModule getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(MavenModule module) {
        this.coordinate = module;
    }

    public synchronized TreeSet<MavenModule> getModules() {
        if (modules == null) {
            modules = new TreeSet<MavenModule>();
        }
        return modules;
    }

    public void addModule(MavenModule module) {
        if (module != null) {
            if (!hasModule(module)) {
                getModules().add(module);
            }
        }
    }

    private boolean hasModule(MavenModule module) {
        for (MavenModule mavenModule : getModules()) {
            if (isSame(module, mavenModule)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSame(MavenModule mod1, MavenModule mod2) {
        if (mod1 == null && mod2 == null) {
            return true;
        }
        if (mod1 == null) {
            return false;
        }
        if (mod2 == null) {
            return false;
        }
        if (!ComparatorUtils.equals(mod1.getGroupId(), mod2.getGroupId())) {
            return false;
        }
        if (!ComparatorUtils.equals(mod1.getArtefactId(), mod2.getArtefactId())) {
            return false;
        }
        return false;
    }

    public void addModules(Collection<MavenModule> modules) {
        if (modules != null) {
            for (MavenModule mavenModule : modules) {
                addModule(mavenModule);
            }
        }
    }

    public void removeModule(MavenCoordinate module) {
        if (module != null) {
            getModules().remove(module);
        }
    }

    public boolean hasModule(MavenCoordinate module) {
        return getModules().contains(module);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((coordinate == null) ? 0 : coordinate.hashCode());
        result = prime * result + ((modules == null) ? 0 : modules.hashCode());
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
        MavenReactor other = (MavenReactor) obj;
        if (coordinate == null) {
            if (other.coordinate != null) {
                return false;
            }
        } else if (!coordinate.equals(other.coordinate)) {
            return false;
        }
        if (modules == null) {
            if (other.modules != null) {
                return false;
            }
        } else if (!modules.equals(other.modules)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format(
                "%s [coordinate=%s, modules=%s]", getClass().getSimpleName(), getCoordinate(), getModules()); //$NON-NLS-1$
    }

}
