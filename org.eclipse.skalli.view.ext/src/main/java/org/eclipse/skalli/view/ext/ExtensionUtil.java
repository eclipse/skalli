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

import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.User;
import org.eclipse.skalli.services.template.ProjectTemplate;

import com.vaadin.terminal.Resource;

public interface ExtensionUtil {

    public void persist(Project project);

    public boolean isUserAdmin();

    public boolean isUserProjectAdmin(Project project);

    public User getLoggedInUser();

    public String getLoggedInUserId();

    public Navigator getNavigator();

    public ProjectTemplate getProjectTemplate();

    /**
     * Returns a resource from the bundle providing the info box.
     * @param path  the path relative to the bundle root.
     * @return  a Vaadin resource.
     */
    public Resource getBundleResource(String path);
}