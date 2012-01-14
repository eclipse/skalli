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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.PropertyName;
import org.eclipse.skalli.services.template.ProjectTemplate;

import com.vaadin.data.Item;
import com.vaadin.ui.Form;
import com.vaadin.ui.FormFieldFactory;

/**
 * Base class for the implementation of {@link ExtensionFormService}.
 */
public abstract class AbstractExtensionFormService<T extends ExtensionEntityBase>
        implements ExtensionFormService<T> {

    protected static final String STYLE_PROJECT_EDIT_FORM = "prjedt-form-default"; //$NON-NLS-1$

    @Override
    public Form createForm(Project project, ProjectEditContext context) {
        ProjectTemplate projectTemplate = context.getProjectTemplate();

        Form form = new Form();
        form.setWriteThrough(false); // we want explicit 'apply'
        form.setInvalidCommitted(false); // no invalid values in datamodel
        form.addStyleName(getFormStyleName());
        form.setValidationVisible(false); // we use our own issue markers

        // FieldFactory for customizing the fields and adding validators
        FormFieldFactory fieldFactory = getFieldFactory(project, context);
        if (fieldFactory != null) {
            form.setFormFieldFactory(fieldFactory);
        }

        // Determine which properties are shown, and in which order:
        List<String> visibleItemProperties = new ArrayList<String>();
        for (String propertyId : getVisibleItemProperties()) {
            if (!projectTemplate.isHidden(getExtensionClass().getName(), propertyId, context.isAdministrator())) {
                visibleItemProperties.add(propertyId);
            }
        }

        // Bind to data source
        Item itemDataSource = getItemDataSource(project);
        if (itemDataSource != null) {
            form.setItemDataSource(itemDataSource, visibleItemProperties);
        } else {
            form.setVisibleItemProperties(visibleItemProperties);
        }

        return form;
    }

    @Override
    public boolean listenOnPropertyChanged(String propertyId, Object newValue) {
        return false;
    }

    /**
     * Determines which properties should be visible in the edit form.
     *
     * <p>
     * As a default, all {@link PropertyName} from the model are returned.
     * The order is determined by the {@link PropertyName#position()} attribute.
     * </p>
     * <p>
     * Override this method if this default does not fit.
     * </p>
     *
     * @return
     */
    protected String[] getVisibleItemProperties() {
        List<Field> fields = new LinkedList<Field>();
        final T instance = newExtensionInstance();
        for (Field field : getExtensionClass().getDeclaredFields()) {
            PropertyName propName = field.getAnnotation(PropertyName.class);
            if (propName != null && propName.position() >= 0) {
                fields.add(field);
            }
        }

        Collections.sort(fields, new Comparator<Field>() {
            @Override
            public int compare(Field o1, Field o2) {
                int ret = 0;
                int i1 = o1.getAnnotation(PropertyName.class).position();
                int i2 = o2.getAnnotation(PropertyName.class).position();
                ret = Integer.valueOf(i1).compareTo(i2);

                if (ret == 0) {
                    String c1 = getFieldContents(o1, instance);
                    String c2 = getFieldContents(o2, instance);
                    ret = c1.compareTo(c2);
                }

                return ret;
            }
        });

        List<String> ret = new LinkedList<String>();
        for (Field field : fields) {
            ret.add(getFieldContents(field, instance));
        }

        return ret.toArray(new String[ret.size()]);
    }

    private String getFieldContents(Field field, T instance) {
        try {
            return field.get(instance).toString();
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    abstract protected FormFieldFactory getFieldFactory(Project project, ProjectEditContext context);

    abstract protected Item getItemDataSource(Project project);

    protected String getFormStyleName() {
        return STYLE_PROJECT_EDIT_FORM;
    }

    protected T getExtension(Project project) {
        Class<T> extensionClass = getExtensionClass();
        T extension = project.getExtension(extensionClass);
        if (extension == null) {
            // in case ther is no suitable extension, i.e. extension is inherited
            // but there is no parent, just return an empty extension instance
            extension = newExtensionInstance();
        }
        return extension;
    }
}
