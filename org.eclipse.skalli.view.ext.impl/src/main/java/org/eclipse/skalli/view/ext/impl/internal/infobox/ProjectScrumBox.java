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
package org.eclipse.skalli.view.ext.impl.internal.infobox;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.ext.scrum.ScrumProjectExt;
import org.eclipse.skalli.view.component.PeopleComponent;
import org.eclipse.skalli.view.ext.ExtensionUtil;
import org.eclipse.skalli.view.ext.InfoBox;
import org.eclipse.skalli.view.ext.ProjectInfoBox;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Layout;

public class ProjectScrumBox extends InfoBox implements ProjectInfoBox {

    private static final String STYLE_SCRUM_INFOBOX = "infobox-scrum"; //$NON-NLS-1$
    private static final String STYLE_TEAMLABEL = "teamlabel"; //$NON-NLS-1$
    private static final String STYLE_SCRUM_LOG = "scrumlog"; //$NON-NLS-1$

    @Override
    public String getIconPath() {
        return "res/icons/scrum.png"; //$NON-NLS-1$
    }

    @Override
    public String getCaption() {
        return "Scrum";
    }

    @Override
    public Component getContent(Project project, ExtensionUtil util) {
        Layout layout = new CssLayout();
        layout.addStyleName(STYLE_SCRUM_INFOBOX);
        layout.setSizeFull();

        boolean rendered = false;
        ScrumProjectExt scrumExt = project.getExtension(ScrumProjectExt.class);
        if (scrumExt != null) {
            if (CollectionUtils.isNotBlank(scrumExt.getProductOwners())) {
                createLabel(layout, "Product Owner", STYLE_TEAMLABEL);
                Component peopleComponent = PeopleComponent.getPeopleListComponentForMember(scrumExt.getProductOwners());
                layout.addComponent(peopleComponent);
                rendered = true;
            }

            if (CollectionUtils.isNotBlank(scrumExt.getScrumMasters())) {
                createLabel(layout, "Scrum Master", STYLE_TEAMLABEL);
                Component peopleComponent = PeopleComponent.getPeopleListComponentForMember(scrumExt.getScrumMasters());
                layout.addComponent(peopleComponent);
                rendered = true;
            }

            if (StringUtils.isNotBlank(scrumExt.getBacklogUrl())) {
                createLink(layout, "Scrum Backlog", scrumExt.getBacklogUrl(), DEFAULT_TARGET, STYLE_SCRUM_LOG);
                rendered = true;
            }
        }
        if (!rendered) {
            createLabel(layout, "SCRUM extension added but no data maintained.", STYLE_LABEL);
        }
        return layout;
    }

    @Override
    public float getPositionWeight() {
        return 1.6f;
    }

    @Override
    public int getPreferredColumn() {
        return COLUMN_EAST;
    }

    @Override
    public boolean isVisible(Project project, String loggedInUserId) {
        if (project.getExtension(ScrumProjectExt.class) != null) {
            return true;
        } else {
            return false;
        }
    }

}
