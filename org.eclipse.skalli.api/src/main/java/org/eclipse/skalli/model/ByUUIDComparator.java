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
package org.eclipse.skalli.model;

import java.util.Comparator;

/**
 * Comparator for comparing entities by their {@link EntityBase#getUuid() unique identifiers}.
 */
public class ByUUIDComparator implements Comparator<Project> {
    @Override
    public int compare(Project o1, Project o2) {
        return o1.getUuid().compareTo(o2.getUuid());
    }
}