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
package org.eclipse.skalli.model.core;

import java.util.Collection;
import java.util.Set;

import org.eclipse.skalli.model.ext.ExtensionValidator;
import org.eclipse.skalli.model.ext.Issuer;
import org.eclipse.skalli.model.ext.PropertyValidator;

/**
 * Interface representing a project template that controls
 * how projects that reference this project template are
 * displayed, edited, validated etc.<br>
 * Implementations of this interface should be derived
 * from {@link ProjectTemplateBase}.
 *
 * @see org.eclipse.skalli.model.core.Project#getProjectTemplateId()
 */
public interface ProjectTemplate extends Issuer {

  /**
   * Returns the symbolic identifier of the project template.
   * This identifier must be unique across all templates loaded
   * by the {@link org.eclipse.skalli.api.java.ProjectTemplateService}.
   */
  public String getId();

  /**
   * Returns the display name of the project template. This is
   * the name that appears for example on the template selection screen
   * when creating a new project.
   */
  public String getDisplayName();

  /**
   * Returns a description of the project template, e.g. for what kind
   * of projects it can or should be used, which extensions it does support etc.
   */
  public String getDescription();

  /**
   * Returns the rank of the project template. The rank allows to sort
   * project templates on the project template selection screen.
   * {@link  org.eclipse.skalli.model.core.DefaultProjectTemplate}
   * and {@link org.eclipse.skalli.model.core.DefaultComponentTemplate}
   * define ranks of <code>1000.0</code> and <code>1000.1</code>, respectively,
   * so that they will appear at the end of the selection list.
   */
  public float getRank();

  /**
   * Returns the nature of projects this template supports, e.g.
   * either {@link ProjectNature#PROJECT} or {@link ProjectNature#COMPONENT}.
   */
  public ProjectNature getProjectNature();

  /**
   * Checks if a project assigned to this template can have subprojects
   * assigned to the given template. This method should at least ensure
   * that the project natures of the templates are compatible, i.e. a
   * {@link ProjectNature#PROJECT} template can have both other projects
   * and components as subprojects, but a {@link ProjectNature#COMPONENT}
   * can only have other components as subprojects.
   *
   * @param projectTemplate  the template to check.
   *
   * @return  <code>true</code> if this template allows subprojects
   *          that are assigned to the given template.
   */
  public boolean isAllowedSubprojectTemplate(ProjectTemplate projectTemplate);

  /**
   * Returns a collection of model extensions that should be included in the edit dialog.
   * If this method returns <code>null</code>, all registered extensions are included
   * by default. The edit dialog first calculates the set of included extension with
   * <code>getIncludedExtensions()</code> and afterwards filters the result according
   * to <code>getExcludedExtensions()</code>.
   *
   * @return  a set of class names of included model extensions, or <code>null</code>
   *          if all known extensions should be included.
   */
  public Set<String> getIncludedExtensions();

  /**
   * Returns a collection of model extensions that should be excluded from the edit dialog.
   * If this method returns <code>null</code> (or an empty list), no extensions are to
   * be excluded. The edit dialog first calculates the set of included extension with
   * <code>getIncludedExtensions()</code> and afterwards filters the result according
   * to <code>getExcludedExtensions()</code>.
   *
   * @return  a set of class names of excluded model extensions, or <code>null</code>
   *          if no extensions should be excluded at all.
   */
  public Set<String> getExcludedExtensions();

  /**
   * Returns <code>true</code>, if the tray corresponding to
   * the given extension is expanded initially in the edit dialog.
   *
   * @param extensionClassName  the class name of a model extension.
   */
  public boolean isVisible(String extensionClassName);

  /**
   * Returns <code>true</code>, if the tray corresponding to
   * the given extension is switch on initially in the edit dialog.
   *
   * @param extensionClassName  the class name of a model extension.
   */
  public boolean isEnabled(String extensionClassName);

  /**
   * Returns a collection of validators for the given model extension.
   *
   * @param extensionClassName  the class name of a model extension.
   *
   * @return a set of validators, or <code>null</code>, if there are
   *         no validators for this extension.
   */
  public Set<ExtensionValidator<?>> getExtensionValidators(String extensionClassName);

  /**
   * Returns a collection of validators for the given property.
   *
   * @param extensionClassName  the class name of a model extension.
   * @param propertyId  a property of the given model extension.
   *
   * @return a set of validators, or an empty set, if there are
   *         no validators for the given property.
   */
  public Set<PropertyValidator> getPropertyValidators(String extensionClassName, Object propertyId);

  /**
   * Returns <code>true</code>, if the field corresponding to
   * the given property is visible.
   *
   * @param extensionClassName  the class name of a model extension.
   * @param propertyId  a property of the given model extension.
   * @param isAdmin  if <code>true</code>, the method checks if the given
   * property is visible for administrative users.
   */
  public boolean isVisible(String extensionClassName, Object propertyId, boolean isAdmin);

  /**
   * Returns <code>true</code>, if the field corresponding to
   * the given property is enabled. A disabled field allows no
   * interaction and is grayed out.
   *
   * @param extensionClassName  the class name of a model extension.
   * @param propertyId  a property of the given model extension.
   * @param isAdmin  if <code>true</code>, the method checks if the given
   *                 property is visible for administrative users.
   */
  public boolean isEnabled(String extensionClassName, Object propertyId, boolean isAdmin);

  /**
   * Returns <code>true</code>, if the field corresponding to
   * the given property is read only.
   *
   * @param extensionClassName  the class name of a model extension.
   * @param propertyId  a property of the given model extension.
   * @param isAdmin  if <code>true</code>, the method checks if the given
   *                 property is visible for administrative users.
   */
  public boolean isReadOnly(String extensionClassName, Object propertyId, boolean isAdmin);

  /**
   * Returns the maximum size of collection-like properties. This method
   * returns <code>Integer.MAX_VALUE</code> if the template imposes no restriction
   * on the size of the property.
   *
   * @param extensionClassName  the class name of a model extension.
   * @param propertyId  a property of the given model extension.
   */
  public int getMaxSize(String extensionClassName, Object propertyId);

  /**
   * Returns the caption of the field corresponding to
   * the given property. If not set, the form renders the default caption.
   *
   * @param extensionClassName  the class name of a model extension.
   * @param propertyId  a property of the given model extension.
   */
  public String getCaption(String extensionClassName, Object propertyId);

  /**
   * Returns the description (tooltip) of the field corresponding to
   * the given property. If not set, the form renders the default description.
   *
   * @param extensionClassName  the class name of a model extension.
   * @param propertyId  a property of the given model extension.
   */
  public String getDescription(String extensionClassName, Object propertyId);

  /**
   * Returns the input prompt of the field corresponding to
   * the given property. If not set, the form renders the default input prompt.
   *
   * @param extensionClassName  the class name of a model extension.
   * @param propertyId  a property of the given model extension.
   */
  public String getInputPrompt(String extensionClassName, Object propertyId);

  /**
   * Returns the default value of the field corresponding to
   * the given property. If not set, the default value is set by the form.
   *
   * @param extensionClassName  the class name of a model extension.
   * @param propertyId  a property of the given model extension.
   */
  public Object getDefaultValue(String extensionClassName, Object propertyId);

  /**
   * Returns the default values of a set-like field (e.g. a ComboBox) corresponding to
   * the given property. If not set, default values are set by the form.
   *
   * @param extensionClassName  the class name of a model extension.
   * @param propertyId  a property of the given model extension.
   */
  public Collection<?> getDefaultValues(String extensionClassName, Object propertyId);

  /**
   * Returns the allowed values of a set-like field (e.g. a ComboBox) corresponding to
   * the given property. If not set, the allowed values are determined by the form.
   *
   * @param extensionClassName  the class name of a model extension.
   * @param propertyId  a property of the given model extension.
   */
  public Collection<?> getAllowedValues(String extensionClassName, Object propertyId);

  /**
   * Returns <code>true</code>, if the field corresponding to
   * the given property (usually a ComboBox) allows entering of new values.
   *
   * @param extensionClassName  the class name of a model extension.
   * @param propertyId  a property of the given model extension.
   */
  public boolean isNewItemsAllowed(String extensionClassName, Object propertyId);

  /**
   * Returns the rank of an extension. The rank determines how information
   * is ordered, i.e. an extension with lower rank is displayed before
   * an extension with higher rank. A rank of zero means, the extension should
   * always be displayed first. A negative rank means, that the form determines
   * the rank of the extension.
   *
   * @param extensionClassName  the class name of a model extension.
   */
  public float getRank(String extensionClassName);
}

