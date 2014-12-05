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
 * Comparator for comparing projects by their {@link Project#getName() display names}.
 * If two projects happen ton have the same display name, they arem compared by
 * symbolic name.
 * <p>
 * Note, display names are compared case-insensitive.
 */
public class ByProjectNameComparator implements Comparator<Project> {
    @Override
    public int compare(Project o1, Project o2) {
        int result = o1.getName().compareToIgnoreCase(o2.getName());
        if (result == 0) {
            result = o1.getProjectId().compareTo(o2.getProjectId());
        }
        return result;
    }
}