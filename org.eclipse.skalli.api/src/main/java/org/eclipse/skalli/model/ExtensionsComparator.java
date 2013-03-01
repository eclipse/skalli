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
package org.eclipse.skalli.model;

import java.util.Comparator;

/**
 * Comparator for model extensions. This comparator compares the class names of the given extensions.
 * It handles <code>null</code> pointers in both arguments, i.e. <code>null</code> pointers are treated
 * as greater than non-<code>null</code> pointers.
 */
public class ExtensionsComparator implements Comparator<ExtensionEntityBase> {

    @Override
    public int compare(ExtensionEntityBase entity1, ExtensionEntityBase entity2) {
        if (entity1 == entity2) {
            return 0;
        }
        if (entity1 == null) {
            return 1;
        }
        if (entity2 == null) {
            return -1;
        }
        return entity1.getClass().getName().compareTo(entity2.getClass().getName());
    }

}
