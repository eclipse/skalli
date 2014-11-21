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
package org.eclipse.skalli.view.ext;

import java.net.URI;

import org.eclipse.skalli.model.Project;

/**
 * Extension point for project related links and actions on the project details page.
 * Project related links are rendered in the navigation pane to the left of the
 * project details.
 *
 * Implementations must be registered as OSGi service component.
 */
public interface ProjectContextLink {

    /**
     * Returns the caption of the link.
     *
     * @param project instance that can be used for calculating the caption in project context.
     */
    public String getCaption(Project project);

    /**
     * Returns the target URI of this link.
     *
     * @param project  the project for which the link is to be rendered.
     */
    public URI getUri(Project project);

    /**
     * Returns the position weight of this link. This value is used to render all context links
     * sorted by their relevance.
     */
    public float getPositionWeight();

    /**
     * Returns <code>true</code>, if the link should be rendered and the user
     * requesting the info box is allowed to use it.
     *
     * @param project  the project for which the link is to be rendered.
     * @param userId  the unique identifier of the user viewing the project details page.
     *
     * @return <code>true</code>, if the link should be rendered.
     */
    public boolean isVisible(Project project, String userId);

}
