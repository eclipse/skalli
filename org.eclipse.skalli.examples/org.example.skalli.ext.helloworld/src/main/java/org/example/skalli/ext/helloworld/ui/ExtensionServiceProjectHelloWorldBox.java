/*******************************************************************************
 * Copyright (c) 2010-2014 SAP AG and others.
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
import org.eclipse.skalli.view.ext.ExtensionUtil;
import org.eclipse.skalli.view.ext.InfoBox;
import org.eclipse.skalli.view.ext.InfoBoxBase;
import org.example.skalli.model.ext.helloworld.HelloWorldProjectExt;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;

public class ExtensionServiceProjectHelloWorldBox extends InfoBoxBase implements InfoBox {

    @Override
    public String getIconPath() {
        return null;
    }

    @Override
    public String getCaption() {
        return "Hello World";
    }

    @Override
    public float getPositionWeight() {
        return 1.01f;
    }

    @Override
    public int getPreferredColumn() {
        return COLUMN_WEST;
    }

    @Override
    public boolean isVisible(Project project, String userId) {
        return project.getExtension(HelloWorldProjectExt.class) != null;
    }

    @Override
    public Component getContent(Project project, ExtensionUtil util) {
        HelloWorldProjectExt ext = project.getExtension(HelloWorldProjectExt.class);

        Layout layout = new CssLayout();
        layout.setSizeFull();

        layout.addComponent(new Label(ext.getName() + " greets the following friends: "));

        for (String friend : ext.getFriends()) {
            layout.addComponent(new Label("Hello " + friend + "!"));
        }

        return layout;
    }
}
