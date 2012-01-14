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
package org.eclipse.skalli.services.project;

import java.util.List;

import org.eclipse.skalli.model.Project;

/**
 * Project Node that can be used to traverse the project hierarchy.
 */
public interface ProjectNode {

    /**
     * Returns a sorted list of child nodes. The hierarchy is sorted all
     * by the same comparator that is passed to project service
     * (see {@link org.eclipse.skalli.services.projects.ProjectService#getProjectNode(java.util.UUID, java.util.Comparator)}
     *  and {@link org.eclipse.skalli.services.projects.ProjectService#getRootProjectNodes(java.util.Comparator)}).
     *
     * @return sorted list of child nodes
     */
    public List<ProjectNode> getSubProjects();

    /**
     * Returns the project instance this node points to in the hierarchy.
     *
     * @return project instance
     */
    public Project getProject();

}
