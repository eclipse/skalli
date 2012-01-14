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
package org.eclipse.skalli.core.internal.templates;

import org.eclipse.skalli.model.ProjectNature;

public class DefaultComponentTemplate extends DefaultProjectTemplate {

    /** Identifier of this template, see {@link #getId()} */
    public static final String ID = "component";

    private static final String TEMPLATE_DISPLAYNAME = "Free-Style Component";
    private static final String TEMPLATE_DESCRIPTION =
            "Compose a component freely from all available project natures and enter exactly the information you need.<br/>"
                    +
                    "This kind of project represents the technical aspects of a project "
                    +
                    "like source code, repositories, artifacts, build jobs, technical documentation and so on. "
                    +
                    "You can assign a Free-Style Component for example to a Free-Style Project, and you can assign other "
                    +
                    "Free-Style Components to it.";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return TEMPLATE_DISPLAYNAME;
    }

    @Override
    public String getDescription() {
        return TEMPLATE_DESCRIPTION;
    }

    @Override
    public ProjectNature getProjectNature() {
        return ProjectNature.COMPONENT;
    }

    @Override
    public float getRank() {
        return 1000.1f;
    }

}
