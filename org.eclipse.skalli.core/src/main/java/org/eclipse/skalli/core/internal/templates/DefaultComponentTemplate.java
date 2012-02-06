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
            "Compose a component freely from all available plugins and enter exactly the information you need.<br/>"
          + "This template is suitable to represent the more technical aspects of a project. A typical use case "
          + "for a component would be to represent an orbit repository. Or you might want to split the source code"
          + "of your project into multiple independent parts, each with its own source repository.<br/>"
          + "Components must always be assigned to a parent project or other component.";

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
