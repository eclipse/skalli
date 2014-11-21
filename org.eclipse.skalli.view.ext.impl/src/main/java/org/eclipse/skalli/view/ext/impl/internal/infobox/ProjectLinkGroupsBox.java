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
package org.eclipse.skalli.view.ext.impl.internal.infobox;

import org.eclipse.skalli.commons.Link;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.ext.linkgroups.LinkGroup;
import org.eclipse.skalli.model.ext.linkgroups.LinkGroupsProjectExt;
import org.eclipse.skalli.view.ext.ExtensionUtil;
import org.eclipse.skalli.view.ext.InfoBox;
import org.eclipse.skalli.view.ext.InfoBoxBase;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;

public class ProjectLinkGroupsBox extends InfoBoxBase implements InfoBox {

    protected static final String SYTLE_LINKGROUPS_INFOBOX = "infobox-linkgroups"; //$NON-NLS-1$

    protected static final String STYLE_LABEL_GROUP = "grouplabel"; //$NON-NLS-1$
    protected static final String STYLE_LABEL_LINK = "linklabel"; //$NON-NLS-1$

    @Override
    public String getIconPath() {
        return "res/icons/link.png"; //$NON-NLS-1$
    }

    @Override
    public String getCaption() {
        return "Additional Links";
    }

    @Override
    public String getShortName() {
        return "linkgroups";
    }

    @Override
    public Component getContent(Project project, ExtensionUtil util) {
        Layout layout = new CssLayout();
        layout.addStyleName(SYTLE_LINKGROUPS_INFOBOX);
        layout.setSizeFull();
        LinkGroupsProjectExt ext = project.getExtension(LinkGroupsProjectExt.class);
        if (ext != null && !ext.getLinkGroups().isEmpty()) {
            for (LinkGroup linkGroup : ext.getLinkGroups()) {
                Label linkGroupHeaderLabel = new Label(linkGroup.getCaption());
                linkGroupHeaderLabel.addStyleName(STYLE_LABEL_GROUP);
                layout.addComponent(linkGroupHeaderLabel);

                for (Link link : linkGroup.getItems()) {
                    com.vaadin.ui.Link uiLink = new com.vaadin.ui.Link(link.getLabel(), new ExternalResource(
                            link.getUrl()));
                    uiLink.setDescription(link.getUrl());
                    uiLink.addStyleName(STYLE_LABEL_LINK);
                    uiLink.setTargetName("_blank"); //$NON-NLS-1$
                    layout.addComponent(uiLink);
                }
            }
        }
        return layout;
    }

    @Override
    public float getPositionWeight() {
        return 2.0f;
    }

    @Override
    public int getPreferredColumn() {
        return COLUMN_WEST;
    }

    @Override
    public boolean isVisible(Project project, String loggedInUserId) {
        LinkGroupsProjectExt ext = project.getExtension(LinkGroupsProjectExt.class);
        if (ext == null || ext.getLinkGroups().isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

}
