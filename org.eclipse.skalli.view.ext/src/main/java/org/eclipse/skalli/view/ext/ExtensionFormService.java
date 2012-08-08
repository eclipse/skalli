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
package org.eclipse.skalli.view.ext;

import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.Project;

import com.vaadin.ui.Form;

/**
 * Service for the creation of Vaadin forms for model extension instances.
 * Implementation should derive from {@link AbstractExtensionFormService}.
 */

public interface ExtensionFormService<T extends ExtensionEntityBase> extends IconProvider {

    public static final String DEFAULT_ICON = "res/icons/default.png"; //$NON-NLS-1$

    /**
     * Returns the default rank of the form. The rank
     * determines in which order the forms are displayed.
     */
    public float getRank();

    /**
     * Returns the extension class for which this form
     * factory is able to create forms.
     */
    public Class<T> getExtensionClass();

    /**
     * Creates a new instance of the extension class.
     */
    public T newExtensionInstance();

    /**
     * Returns a form for an extension instance matching {@link #getExtensionClass()}
     * of a given project. Note, this method always creates a new form instance.
     * @param project  the project to which the extension instance is attached.
     * @param mode  determines if a new project is to be created or an existing project to be edited.
     * @param context  additional information that influence the form creation.
     * @return  a new form instance for the extension.
     */
    public Form createForm(Project project, ProjectEditContext context);

    /**
     * Returns <code>true</code> if the form should be re-created when the property
     * with the given name has changed.
     * @param propertyId  the name of the property.
     * @param newValue  the new value of the property.
     */
    public boolean listenOnPropertyChanged(String extensionClassName, String propertyId, Object newValue);
}
