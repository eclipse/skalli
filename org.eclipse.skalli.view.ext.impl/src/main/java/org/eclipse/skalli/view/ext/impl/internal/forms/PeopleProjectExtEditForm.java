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

import org.eclipse.skalli.model.core.Project;
import org.eclipse.skalli.model.ext.people.PeopleProjectExt;
import org.eclipse.skalli.view.component.UsersPicker;
import org.eclipse.skalli.view.ext.AbstractExtensionFormService;
import org.eclipse.skalli.view.ext.DefaultProjectFieldFactory;
import org.eclipse.skalli.view.ext.ProjectEditContext;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormFieldFactory;

public class PeopleProjectExtEditForm extends AbstractExtensionFormService<PeopleProjectExt> {

    @Override
    public String getIconPath() {
        return "res/icons/people.png"; //$NON-NLS-1$
    }

    @Override
    public float getRank() {
        return 1.0f;
    }

    @Override
    public Class<PeopleProjectExt> getExtensionClass() {
        return PeopleProjectExt.class;
    }

    @Override
    public PeopleProjectExt newExtensionInstance() {
        return new PeopleProjectExt();
    }

    @Override
    protected Item getItemDataSource(Project project) {
        return new BeanItem<PeopleProjectExt>(getExtension(project));
    }

    @Override
    protected FormFieldFactory getFieldFactory(Project project, ProjectEditContext context) {
        return new FieldFactory(project, context);
    }

    private class FieldFactory extends DefaultProjectFieldFactory<PeopleProjectExt> {
        private static final long serialVersionUID = 7304032586747249586L;
        private PeopleProjectExt extension;

        public FieldFactory(Project project, ProjectEditContext context) {
            super(project, PeopleProjectExt.class, context);
            this.extension = getExtension(project);
        }

        @Override
        protected Field createField(Object propertyId, String caption) {
            Field field = null;
            if (PeopleProjectExt.PROPERTY_MEMBERS.equals(propertyId)) {
                field = new UsersPicker(caption, extension.getMembers());
            } else if (PeopleProjectExt.PROPERTY_LEADS.equals(propertyId)) {
                field = new UsersPicker(caption, extension.getLeads());
            }
            return field;
        }
    }
}
