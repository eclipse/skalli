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
package org.eclipse.skalli.view.ext.impl.internal.links;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.view.ext.ProjectContextLink;

public class CreateJiraProjectLink implements ProjectContextLink {

    @Override
    public String getCaption(Project project) {
        return "Create Jira Project";
    }

    @Override
    public URI getUri(Project project) {
        try {
            return new URI("/create/jira?id=" + project.getProjectId()); //$NON-NLS-1$
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public float getPositionWeight() {
        return 2.0f;
    }

    @Override
    public boolean isVisible(Project project, String userId) {
        return true;
    }

}
