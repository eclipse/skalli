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
import org.eclipse.skalli.model.ext.devinf.DevInfProjectExt;
import org.eclipse.skalli.view.component.MultiTextField;
import org.eclipse.skalli.view.ext.AbstractExtensionFormService;
import org.eclipse.skalli.view.ext.DefaultProjectFieldFactory;
import org.eclipse.skalli.view.ext.ProjectEditContext;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormFieldFactory;

public class DevInfProjectExtEditForm extends AbstractExtensionFormService<DevInfProjectExt> {

    @Override
    public String getIconPath() {
        return "res/icons/devinf.png"; //$NON-NLS-1$
    }

    @Override
    public float getRank() {
        return 2.0f;
    }

    @Override
    protected FormFieldFactory getFieldFactory(Project project, ProjectEditContext context) {
        return new FieldFactory(project, context);
    }

    @Override
    protected Item getItemDataSource(Project project) {
        return new BeanItem<DevInfProjectExt>(getExtension(project));
    }

    private class FieldFactory extends DefaultProjectFieldFactory<DevInfProjectExt> {
        private static final long serialVersionUID = -1233616280454351799L;
        private DevInfProjectExt extension;

        public FieldFactory(Project project, ProjectEditContext context) {
            super(project, DevInfProjectExt.class, context);
            this.extension = getExtension(project);
        }

        @Override
        protected Field createField(Object propertyId, String caption) {
            Field field = null;
            if (DevInfProjectExt.PROPERTY_SCM_LOCATIONS.equals(propertyId)) {
                field = new MultiTextField(caption, extension.getScmLocations());
            }
            else if (DevInfProjectExt.PROPERTY_JAVADOCS_URL.equals(propertyId)) {
                field = new MultiTextField(caption, extension.getJavadocs());
            }
            return field;
        }
    }

    @Override
    public Class<DevInfProjectExt> getExtensionClass() {
        return DevInfProjectExt.class;
    }

    @Override
    public DevInfProjectExt newExtensionInstance() {
        return new DevInfProjectExt();
    }
}
