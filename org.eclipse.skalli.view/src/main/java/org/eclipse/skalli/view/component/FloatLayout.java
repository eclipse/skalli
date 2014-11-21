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
package org.eclipse.skalli.view.component;

import java.util.LinkedHashMap;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;

public class FloatLayout extends CssLayout {

    private static final long serialVersionUID = 6837635603865705941L;

    private LinkedHashMap<Component, String> styles;

    public FloatLayout() {
        super();
        setSizeFull();
    }

    public void addComponent(Component c, String style) {
        super.addComponent(c);
        if (styles == null) {
            styles = new LinkedHashMap<Component, String>();
        }
        styles.put(c, style);
    }

    @Override
    protected String getCss(Component c) {
        String css = "float:left;";
        if (styles != null) {
            if (styles.containsKey(c)) {
                css += styles.get(c);
            }
        }
        else if (c instanceof Label) {
            css += "line-height:18px;padding-left:3px;padding-top:3px";
        }
        return css;
    }
}
