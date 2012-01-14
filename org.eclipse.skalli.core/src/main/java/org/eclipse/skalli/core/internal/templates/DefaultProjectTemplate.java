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
import org.eclipse.skalli.services.template.ProjectTemplateBase;

/**
 * Default implementation of a project template suitable for
 * generic projects. This template for example allows all model
 * extensions and defines a variety of project phases.
 */
public class DefaultProjectTemplate extends ProjectTemplateBase {

    private static final String TEMPLATE_DISPLAYNAME = "Free-Style Project";
    private static final String TEMPLATE_DESCRIPTION =
            "Compose a project freely from all available project natures and enter exactly the information you need.<br/>"
          + "This kind of project represents a group of people working on a topic, for example a TGiF innovation. "
          + "It can have subprojects allowing to break down the topic into more manageable pieces or represent "
          + "different parallel work streams. Furthermore, you can assign component-like subprojects, for example"
          + "Free-Style Components, to represent the more technical aspects of the topic.";

    @Override
    public String getId() {
        return DEFAULT_ID;
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
    public float getRank() {
        return 1000.0f;
    }

    /**
     * Returns the style of projects this template supports,
     * i.e. always {@link ProjectNature#PROJECT}.
     */
    @Override
    public ProjectNature getProjectNature() {
        return ProjectNature.PROJECT;
    }
}
