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
package org.eclipse.skalli.view.ext.impl.internal.forms;

import org.eclipse.skalli.commons.UUIDList;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.ext.misc.RelatedProjectsExt;
import org.eclipse.skalli.view.component.MultiComboBox;
import org.eclipse.skalli.view.ext.AbstractExtensionFormService;
import org.eclipse.skalli.view.ext.DefaultProjectFieldFactory;
import org.eclipse.skalli.view.ext.ProjectEditContext;

import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormFieldFactory;

public class RelatedProjectsExtEditForm extends AbstractExtensionFormService<RelatedProjectsExt> {

    @Override
    public String getIconPath() {
        return "res/icons/relProjects.png"; //$NON-NLS-1$
    }

    @Override
    public float getRank() {
        return 1.5f;
    }

    @Override
    protected FormFieldFactory getFieldFactory(Project project, ProjectEditContext context) {
        return new FieldFactory(project, context);
    }

    @Override
    protected Item getItemDataSource(Project project) {
        return new BeanItem<RelatedProjectsExt>(getExtension(project));
    }

    private class FieldFactory extends DefaultProjectFieldFactory<RelatedProjectsExt> {

        private static final long serialVersionUID = 939519704283769367L;
        private RelatedProjectsExt extension;
        private Field comboBox;

        public FieldFactory(Project project, ProjectEditContext context) {
            super(project, RelatedProjectsExt.class, context);
            this.extension = getExtension(project);
        }

        @Override
        protected Field createField(Object propertyId, String caption) {

            if (RelatedProjectsExt.PROPERTY_CALCULATED.equals(propertyId)) {
                final CheckBox field = new CheckBox(caption);
                field.addListener(new ValueChangeListener() {

                    private static final long serialVersionUID = 3996507266934851419L;

                    @Override
                    public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
                        if (comboBox != null) {
                            comboBox.setEnabled(!field.booleanValue());
                        }
                    }
                });
                return field;
            } else if (RelatedProjectsExt.PROPERTY_RELATED_PROJECT.equals(propertyId)) {
                UUIDList ids = extension.getRelatedProjects();
                comboBox = new MultiComboBox(caption, ids);
                comboBox.setEnabled(!extension.getCalculated());
                return comboBox;
            }
            return null;
        }
    }

    @Override
    public Class<RelatedProjectsExt> getExtensionClass() {
        return RelatedProjectsExt.class;
    }

    @Override
    public RelatedProjectsExt newExtensionInstance() {
        return new RelatedProjectsExt();
    }
}
