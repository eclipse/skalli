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

import org.eclipse.skalli.services.template.ProjectTemplate;

public interface ProjectEditContext {

    public ProjectEditMode getProjectEditMode();

    public ProjectTemplate getProjectTemplate();

    public boolean isAdministrator();

    /**
     * Returns <code>true</code> if the panel corresponding to the given extension exists.
     * @param extensionClassName  the extension to check.
     */
    public boolean hasPanel(String extensionClassName);

    /**
     * Raises a property change event, i.e. notifies all forms
     * that the property with name <code>propertyName</code> of
     * a given extension has changed and delivers the new value.
     *
     * @param extensionClassName  the extension that holds the given property.
     * @param propertyName  the name of the property.
     * @param propertyValue  new value of the property.
     */
    public void onPropertyChanged(String extensionClassName, String propertyName, Object propertyValue);

    /**
     * Retrieves the current value of the property with name <code>propertyName</code> of
     * a given extension.
     * <p>
     * First, a commit of the form that correspond to the given extension is enforced,
     * then the value of the property is retrieved from the extension.
     * <p>
     * If the panel corresponding to the extension was in state
     * <ul>
     * <li><tt>disabled</tt>, the result is always <code>null</code>.</li>
     * <li><tt>inherited</tt>, the property from the parent extension is returned, if any.</li>
     * </ul>
     *
     * @param extensionClassName  the extension that holds the given property.
     * @param propertyName  the name of the property.
     *
     * @return  the value of the property as read from the form, or <code>null</code>.
     * If there is no panel for the given extension, the result is <code>null</code>, too.
     */
    public Object getProperty(String extensionClassName, String propertyName);

    /**
     * Sets a new value for the property with name <code>propertyName</code> of
     * a given extension.
     * <p>
     * First, a commit of the form that correspond to the given extension is enforced,
     * then the value of the property is set on the extension and finally the form
     * is recreated with the updated value.
     * <p>
     * If the panel corresponding to the extension was in state
     * <ul>
     * <li><tt>disabled</tt>, it is enabled first.</li>
     * <li><tt>inherited</tt>, the request to change the property is ignored.</li>
     * <li><tt>collapsed</tt>, it is expanded.</li>
     * </ul>
     *
     * If there is no panel for the given extension, the method does nothing.
     *
     * @param extensionClassName  the extension that holds the given property.
     * @param propertyName  the name of the property.
     * @param propertyValue  new value of the property.
     */
    public void setProperty(String extensionClassName, String propertyName, Object propertyValue);

    /**
     * Returns <code>true</code> if the panel corresponding to the given extension
     * is in state <tt>editable</tt>.
     *
     * @throws IllegalArgumentException if there is no panel for the given extension.
     */
    public boolean isEditable(String extensionClassName);

    /**
     * Returns <code>true</code> if the panel corresponding to the given extension
     * is in state <tt>inherited</tt>.
     *
     * @param extensionClassName  the extension to check.
     *
     * @throws IllegalArgumentException if there is no panel for the given extension.
     */
    public boolean isInherited(String extensionClassName);

    /**
     * Returns <code>true</code> if the panel corresponding to the given extension
     * is in state <tt>disabled</tt>.
     *
     * @param extensionClassName  the extension to check.
     *
     * @throws IllegalArgumentException if there is no panel for the given extension.
     */
    public boolean isDisabled(String extensionClassName);

    /**
     * Returns <code>true</code> if the panel corresponding to the given extension is expanded,
     * i.e. its content is visible.
     * @param extensionClassName  the extension to check.
     *
     * @throws IllegalArgumentException if there is no panel for the given extension.
     */
    public boolean isExpanded(String extensionClassName);

}
