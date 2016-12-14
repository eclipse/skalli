/*******************************************************************************
 * Copyright (c) 2010-2016 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.testutil;

public class StorageKey {
    private String category;
    private String key;

    private StorageKey(String category, String key) {
        this.category = category;
        this.key = key;
    }

    public static StorageKey keyOf(String category, String key) {
        return new StorageKey(category, key);
    }

    public String getCategory() {
        return category;
    }

    public String getKey() {
        return key;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((category == null) ? 0 : category.hashCode());
        result = prime * result + ((key == null) ? 0 : key.hashCode());
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
        StorageKey other = (StorageKey) obj;
        if (category == null) {
            if (other.category != null) {
                return false;
            }
        } else if (!category.equalsIgnoreCase((other.category))) {
            return false;
        }
        if (key == null) {
            if (other.key != null) {
                return false;
            }
        } else if (!key.equals(other.key)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return category + "/" + key; //$NON-NLS-1$
    }
}
