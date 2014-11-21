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
package org.eclipse.skalli.model.ext.linkgroups;

import java.io.Serializable;
import java.util.Collection;
import java.util.Vector;

/*
 *  TODO refactoring / componentization
 *  This entity belongs to the 'LinkGroups' extension, but
 *  cannot reside in model.ext.misc as it is needed in model
 *  and view implementations.
 */
public class OrderableGroup<T> implements Serializable {

    private static final long serialVersionUID = 1616532142447649094L;

    private Vector<T> items;

    public OrderableGroup() {
        items = new Vector<T>();
    }

    public OrderableGroup(Collection<T> items) {
        this.items = new Vector<T>(items);
    }

    public Collection<T> getItems() {
        if (items == null) {
            items = new Vector<T>();
        }
        return items;
    }

    public boolean add(T item) {
        if (items.contains(item)) {
            return false;
        }

        return items.add(item);
    }

    public boolean remove(T item) {
        return items.remove(item);
    }

    public boolean hasItem(T item) {
        return items.contains(item);
    }

    public boolean moveUp(T item) {
        int oldIndex = items.indexOf(item);
        if (oldIndex <= 0) {
            return false;
        }

        items.remove(item);
        items.insertElementAt(item, oldIndex - 1);
        return true;
    }

    public boolean moveDown(T item) {
        int oldIndex = items.indexOf(item);
        if (oldIndex == -1 || oldIndex == items.size() - 1) {
            return false;
        }

        items.remove(item);
        items.insertElementAt(item, oldIndex + 1);
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((items == null) ? 0 : items.hashCode());
        return result;
    }

    @Override
    @SuppressWarnings("rawtypes")
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
        OrderableGroup other = (OrderableGroup) obj;
        if (items == null) {
            if (other.items != null) {
                return false;
            }
        } else if (!items.equals(other.items)) {
            return false;
        }
        return true;
    }
}
