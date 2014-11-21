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
package org.eclipse.skalli.services.template;

import java.util.Set;

import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.Project;

/**
 * Service that allows to retrieve project templates.
 */
public interface ProjectTemplateService {

    /**
     * Returns a set of all known templates.
     * @return a set of project templates, or an empty set.
     */
    public Set<ProjectTemplate> getAllTemplates();

    /**
     * Returns the project template with the given class name.
     *
     * @param name
     *          the class name of a project template
     * @return the project template with the given identifier, or
     *         the default template if no such project template exists.
     */
    public ProjectTemplate getProjectTemplate(String className);

    /**
     * Returns the project template with the given symbolic name.
     *
     * @param name
     *          the id of a project template
     * @return the project template with the given identifier, or
     *         the default template if no such project template exists.
     */
    public ProjectTemplate getProjectTemplateById(String templateId);

    /**
     * Returns all extensions that are allowed for a given template.
     *
     * If a project is passed in, all extensions known to the project are added
     * as well to remain backward compatibility.
     *
     * @param template the template to check.
     * @param project might be null
     * @return
     */
    public Set<Class<? extends ExtensionEntityBase>> getSelectableExtensions(ProjectTemplate template,
            Project project);
}
