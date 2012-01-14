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
import org.eclipse.skalli.model.ext.maven.MavenProjectExt;
import org.eclipse.skalli.view.ext.AbstractExtensionFormService;
import org.eclipse.skalli.view.ext.DefaultProjectFieldFactory;
import org.eclipse.skalli.view.ext.ProjectEditContext;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.FormFieldFactory;

public class MavenProjectExtEditForm extends AbstractExtensionFormService<MavenProjectExt> {

    @Override
    public String getIconPath() {
        return "res/icons/maven.png"; //$NON-NLS-1$
    }

    @Override
    public float getRank() {
        return 3.0f;
    }

    @Override
    protected FormFieldFactory getFieldFactory(Project project, ProjectEditContext context) {
        return new FieldFactory(project, context);
    }

    @Override
    protected Item getItemDataSource(Project project) {
        return new BeanItem<MavenProjectExt>(getExtension(project));
    }

    private class FieldFactory extends DefaultProjectFieldFactory<MavenProjectExt> {
        private static final long serialVersionUID = -6885011127921195256L;

        public FieldFactory(Project project, ProjectEditContext context) {
            super(project, MavenProjectExt.class, context);
        }
    }

    @Override
    public Class<MavenProjectExt> getExtensionClass() {
        return MavenProjectExt.class;
    }

    @Override
    public MavenProjectExt newExtensionInstance() {
        return new MavenProjectExt();
    }
}
