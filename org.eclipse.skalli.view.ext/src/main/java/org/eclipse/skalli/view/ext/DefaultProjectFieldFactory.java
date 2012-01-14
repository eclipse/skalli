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

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.services.extension.ExtensionService;
import org.eclipse.skalli.services.extension.ExtensionServices;
import org.eclipse.skalli.services.extension.PropertyValidator;
import org.eclipse.skalli.services.extension.validators.MinMaxOccurrencesPropertyValidator;
import org.eclipse.skalli.services.template.ProjectTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Validator;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Select;

public abstract class DefaultProjectFieldFactory<T extends ExtensionEntityBase> extends DefaultFieldFactory {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultProjectFieldFactory.class);
    private static final long serialVersionUID = 162355838708348565L;
    protected static final int COMMON_TEXT_FIELD_COLUMNS = 30;

    protected final Project project;
    protected final ProjectEditContext context;
    protected final ProjectTemplate projectTemplate;
    protected final boolean isAdmin;
    protected final ProjectEditMode mode;
    protected final String extensionClassName;
    protected final ExtensionService<T> extensionService;

    public DefaultProjectFieldFactory(Project project, Class<T> extensionClass, ProjectEditContext context) {
        this.project = project;
        this.extensionClassName = extensionClass.getName();
        this.extensionService = ExtensionServices.getExtensionService(extensionClass);
        this.projectTemplate = context.getProjectTemplate();
        this.isAdmin = context.isAdministrator();
        this.mode = context.getProjectEditMode();
        this.context = context;
    }

    @Override
    public Field createField(Item item, Object propertyId, final Component uiContext) {
        String propertyName = propertyId.toString();
        String caption = getCaption(propertyName);
        Field field = createField(propertyId, caption);
        if (field == null) {
            field = super.createField(item, propertyId, uiContext);
            if (field == null) {
                throw new IllegalStateException("Cannot create field for property " + propertyId);
            }
            if (StringUtils.isNotBlank(caption)) {
                field.setCaption(caption);
            }
        }

        setImmediate(field);
        field.addStyleName(extensionService.getShortName() + "_" + propertyName);
        initializeField(propertyId, field);

        removeLegacyValidators(field);

        setColumns(field, COMMON_TEXT_FIELD_COLUMNS);

        String description = getDescription(propertyName);
        if (StringUtils.isNotBlank(description)) {
            field.setDescription(description);
        }

        String inputPrompt = getInputPrompt(propertyName);
        if (StringUtils.isNotBlank(inputPrompt)) {
            setInputPrompt(field, inputPrompt);
        }

        int maxSize = projectTemplate.getMaxSize(extensionClassName, propertyId);
        if (maxSize > 0) {
            setMaxSize(field, maxSize);
        }

        if (Select.class.isAssignableFrom(field.getClass())) {
            Select selection = (Select) field;
            Collection<?> defaultValues = projectTemplate.getDefaultValues(extensionClassName, propertyId);
            if (defaultValues != null) {
                field.setValue(defaultValues);
            }
            Object defaultValue = projectTemplate.getDefaultValue(extensionClassName, propertyId);
            if (defaultValue != null) {
                selection.select(defaultValue);
            }
            if (projectTemplate.isNewItemsAllowed(extensionClassName, propertyId)) {
                selection.setNewItemsAllowed(true);
            }
        }
        else {
            Object defaultValue = projectTemplate.getDefaultValue(extensionClassName, propertyId);
            if (defaultValue != null) {
                field.setValue(defaultValue);
            }
        }

        if (projectTemplate.isReadOnly(extensionClassName, propertyId, isAdmin)) {
            field.setReadOnly(true);
        }

        if (mode.equals(ProjectEditMode.VIEW_PROJECT)) {
            field.setReadOnly(true);
        }

        if (projectTemplate.isDisabled(extensionClassName, propertyId, isAdmin)) {
            field.setEnabled(false);
        }

        return field;
    }

    private void setColumns(Field field, int columns) {
        try {
            Method method = field.getClass().getMethod("setColumns", int.class); //$NON-NLS-1$
            method.invoke(field, columns);
        } catch (Exception e) {
            // not all Vaadin Field implementations allow to set the columns, so this is ok
            LOG.debug(MessageFormat.format("Field {0} does not allow to set columns", field.getCaption()));
        }
    }

    private void setImmediate(Field field) {
        try {
            Method method = field.getClass().getMethod("setImmediate", boolean.class); //$NON-NLS-1$
            method.invoke(field, true);
        } catch (Exception e) {
            // not all Vaadin Field implementations allow to set the immediate flag, so this is ok
            LOG.debug(MessageFormat.format("Field {0} does not allow to set immediate flag", field.getCaption()));
        }
    }

    private void setInputPrompt(Field field, String inputPrompt) {
        try {
            Method method = field.getClass().getMethod("setInputPrompt", String.class); //$NON-NLS-1$
            method.invoke(field, inputPrompt);
        } catch (Exception e) {
            // not all Vaadin Field implementations have an input prompt, so this is ok
            LOG.debug(MessageFormat.format("Field {0} does not allow to set columns", field.getCaption()));
        }
    }

    private void setMaxSize(Field field, int maxSize) {
        try {
            Method method = field.getClass().getMethod("setMaxSize", int.class); //$NON-NLS-1$
            method.invoke(field, maxSize);
        } catch (Exception e) {
            // not all Vaadin Field implementations have an max size, so this is ok
            LOG.debug(MessageFormat.format("Field {0} does not allow to set max size", field.getCaption()));
        }
    }

    private void removeLegacyValidators(Field field) {
        if (field.isRequired()) {
            field.setRequired(false);
            field.setRequiredError(null);
            LOG.warn("Required flag removed from field " + field.getCaption() + " of project " + project.getName()
                    + ". Attach a " + MinMaxOccurrencesPropertyValidator.class.getName() + " to the field.");
        }
        Collection<Validator> vaadinValidators = field.getValidators();
        if (vaadinValidators != null) {
            for (Validator vaadinValidator : vaadinValidators) {
                LOG.warn("Validator " + vaadinValidator.getClass().getName() + " removed from field "
                        + field.getCaption()
                        + " of project " + project.getName() + ". Attach a suitable "
                        + PropertyValidator.class.getName()
                        + " to the field.");
                field.removeValidator(vaadinValidator);
            }
        }
    }

    protected String getCaption(String propertyName) {
        String caption = projectTemplate.getCaption(extensionClassName, propertyName);
        if (StringUtils.isBlank(caption)) {
            caption = extensionService.getCaption(propertyName);
        }
        return caption;
    }

    protected String getDescription(String propertyName) {
        String description = projectTemplate.getDescription(extensionClassName, propertyName);
        if (StringUtils.isBlank(description)) {
            description = extensionService.getDescription(propertyName);
        }
        return description;
    }

    protected String getInputPrompt(String propertyName) {
        String inputPrompt = projectTemplate.getInputPrompt(extensionClassName, propertyName);
        if (StringUtils.isBlank(inputPrompt)) {
            inputPrompt = extensionService.getInputPrompt(propertyName);
        }
        return inputPrompt;
    }

    protected Field createField(Object propertyId, String caption) {
        return null;
    }

    protected void initializeField(Object propertyId, Field field) {
        return;
    }
}
