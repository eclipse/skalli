/*******************************************************************************
 * Copyright (c) 2010, 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/edl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.example.skalli.ext.helloworld.ui;

import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.view.component.MultiTextField;
import org.eclipse.skalli.view.ext.AbstractExtensionFormService;
import org.eclipse.skalli.view.ext.DefaultProjectFieldFactory;
import org.eclipse.skalli.view.ext.ProjectEditContext;
import org.example.skalli.model.ext.helloworld.HelloWorldProjectExt;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormFieldFactory;

public class ExtensionServiceHelloWorldProjectExtEditForm extends AbstractExtensionFormService<HelloWorldProjectExt> {

    private class MyFieldFactory extends DefaultProjectFieldFactory<HelloWorldProjectExt> {

        private static final long serialVersionUID = -6604268736563790136L;
        private HelloWorldProjectExt extension;

        public MyFieldFactory(Project project, ProjectEditContext context) {
            super(project, HelloWorldProjectExt.class, context);
            extension = getExtension(project);
        }

        @Override
        protected Field createField(Object propertyId, String caption) {
            Field field = null;
            if (HelloWorldProjectExt.PROPERTY_FRIENDS.equals(propertyId)) {
                field = (Field) new MultiTextField(caption, extension.getFriends());
            }
            return field;
        }

        @Override
        protected void initializeField(Object propertyId, Field field) {
        }

    }

    @Override
    public float getRank() {
        return 100.0f; // higher numbers move it further down the extensions list, hello world is not an important one
    }

    @Override
    public Class<HelloWorldProjectExt> getExtensionClass() {
        return HelloWorldProjectExt.class;
    }

    @Override
    public HelloWorldProjectExt newExtensionInstance() {
        return new HelloWorldProjectExt();
    }

    @Override
    public String getIconPath() {
        return null;
    }

    @Override
    protected FormFieldFactory getFieldFactory(Project project, ProjectEditContext context) {
        return new MyFieldFactory(project, context);
    }

    @Override
    protected Item getItemDataSource(Project project) {
        return new BeanItem<HelloWorldProjectExt>(getExtension(project));
    }

}
