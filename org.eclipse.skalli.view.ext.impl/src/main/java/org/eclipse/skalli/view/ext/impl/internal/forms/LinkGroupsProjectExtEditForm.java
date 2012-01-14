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
import org.eclipse.skalli.model.ext.linkgroups.LinkGroupsProjectExt;
import org.eclipse.skalli.view.component.MultiLinkField;
import org.eclipse.skalli.view.ext.AbstractExtensionFormService;
import org.eclipse.skalli.view.ext.DefaultProjectFieldFactory;
import org.eclipse.skalli.view.ext.ProjectEditContext;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormFieldFactory;

public class LinkGroupsProjectExtEditForm extends AbstractExtensionFormService<LinkGroupsProjectExt> {

    @Override
    public String getIconPath() {
        return "res/icons/link.png"; //$NON-NLS-1$
    }

    @Override
    public float getRank() {
        return 1.2f;
    }

    @Override
    public Class<LinkGroupsProjectExt> getExtensionClass() {
        return LinkGroupsProjectExt.class;
    }

    @Override
    public LinkGroupsProjectExt newExtensionInstance() {
        return new LinkGroupsProjectExt();
    }

    @Override
    protected Item getItemDataSource(Project project) {
        return new BeanItem<LinkGroupsProjectExt>(getExtension(project));
    }

    @Override
    protected FormFieldFactory getFieldFactory(Project project, ProjectEditContext context) {
        return new FieldFactory(project, context);
    }

    private class FieldFactory extends DefaultProjectFieldFactory<LinkGroupsProjectExt> {
        private static final long serialVersionUID = 1997123634123666226L;
        private LinkGroupsProjectExt extension;

        public FieldFactory(Project project, ProjectEditContext context) {
            super(project, LinkGroupsProjectExt.class, context);
            this.extension = getExtension(project);
        }

        @Override
        protected Field createField(Object propertyId, String caption) {
            Field field = null;
            if (LinkGroupsProjectExt.PROPERTY_LINKGROUPS.equals(propertyId)) {
                field = new MultiLinkField(caption, extension.getLinkGroups());
            }
            return field;
        }

    }
}
