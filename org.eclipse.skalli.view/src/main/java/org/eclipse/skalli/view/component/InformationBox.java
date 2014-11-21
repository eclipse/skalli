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

import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;

@SuppressWarnings("serial")
public class InformationBox extends Panel {

    public static final String STYLE = "infobox"; //$NON-NLS-1$

    public static final int CONTENT_TEXT = Label.CONTENT_TEXT;
    public static final int CONTENT_XHTML = Label.CONTENT_XHTML;

    private InformationBox(String caption) {
        super(caption);
        setWidth("100%"); //$NON-NLS-1$

        Layout layout = new CssLayout();
        layout.setSizeFull();
        setContent(layout);

        addStyleName(STYLE);
    }

    public static InformationBox getInformationBox(String caption, String content) {
        Label label = new Label(content, Label.CONTENT_TEXT);
        label.addStyleName(STYLE);
        InformationBox infobox = new InformationBox(caption);
        infobox.addComponent(label);
        return infobox;
    }

    public static InformationBox getInformationBox(String caption, String content, int contentMode) {
        Label label = new Label(content, contentMode);
        label.addStyleName(STYLE);
        InformationBox infobox = new InformationBox(caption);
        infobox.addComponent(label);
        return infobox;
    }

    public static InformationBox getInformationBox(String caption) {
        return new InformationBox(caption);
    }

}
