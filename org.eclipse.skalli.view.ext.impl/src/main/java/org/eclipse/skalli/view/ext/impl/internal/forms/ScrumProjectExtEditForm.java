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
package org.eclipse.skalli.view.ext.impl.internal.forms;

import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.ext.scrum.ScrumProjectExt;
import org.eclipse.skalli.view.component.UsersPicker;
import org.eclipse.skalli.view.ext.AbstractExtensionFormService;
import org.eclipse.skalli.view.ext.DefaultProjectFieldFactory;
import org.eclipse.skalli.view.ext.ProjectEditContext;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormFieldFactory;

public class ScrumProjectExtEditForm extends AbstractExtensionFormService<ScrumProjectExt> {

    @Override
    public String getIconPath() {
        return "res/icons/scrum.png"; //$NON-NLS-1$
    }

    @Override
    public float getRank() {
        return 4.0f;
    }

    @Override
    protected FormFieldFactory getFieldFactory(Project project, ProjectEditContext context) {
        return new FieldFactory(project, context);
    }

    @Override
    protected Item getItemDataSource(Project project) {
        return new BeanItem<ScrumProjectExt>(getExtension(project));
    }

    private class FieldFactory extends DefaultProjectFieldFactory<ScrumProjectExt> {
        private static final long serialVersionUID = 994890554809770532L;
        private ScrumProjectExt extension;

        public FieldFactory(Project project, ProjectEditContext context) {
            super(project, ScrumProjectExt.class, context);
            this.extension = getExtension(project);
        }

        @Override
        protected Field createField(Object propertyId, String caption) {
            Field field = null;
            if (ScrumProjectExt.PROPERTY_SCRUM_MASTERS.equals(propertyId)) {
                field = new UsersPicker(caption, extension.getScrumMasters());
            } else if (ScrumProjectExt.PROPERTY_PRODUCT_OWNERS.equals(propertyId)) {
                field = new UsersPicker(caption, extension.getProductOwners());
            }
            return field;
        }
    }

    @Override
    public Class<ScrumProjectExt> getExtensionClass() {
        return ScrumProjectExt.class;
    }

    @Override
    public ScrumProjectExt newExtensionInstance() {
        return new ScrumProjectExt();
    }
}
