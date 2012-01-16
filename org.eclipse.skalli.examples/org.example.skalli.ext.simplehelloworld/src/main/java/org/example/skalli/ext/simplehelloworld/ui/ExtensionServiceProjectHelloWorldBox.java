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
package org.example.skalli.ext.simplehelloworld.ui;

import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.view.ext.ExtensionUtil;
import org.eclipse.skalli.view.ext.ProjectInfoBox;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;

public class ExtensionServiceProjectHelloWorldBox implements ProjectInfoBox {

    @Override
    public String getCaption() {
        return "Hello World";
    }

    @Override
    public float getPositionWeight() {
        // some high value to have it displayed as one of the last extensions
        return 100;
    }

    @Override
    public int getPreferredColumn() {
        return COLUMN_EAST;
    }

    @Override
    public boolean isVisible(Project project, String userId) {
        // the Hello World info box should be visible in all projects
        return true;
    }

    @Override
    public String getIconPath() {
        return null;
    }

    @Override
    public Component getContent(Project project, ExtensionUtil util) {
        Layout layout = new CssLayout();
        layout.setSizeFull();
        layout.addComponent(new Label("The project you are viewing is called \"" + project.getName() + "\"."));

        return layout;
    }
}
