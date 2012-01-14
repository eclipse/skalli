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

import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.ext.commons.PeopleExtension;
import org.eclipse.skalli.view.component.InformationBox;
import org.eclipse.skalli.view.component.PeopleComponent;
import org.eclipse.skalli.view.ext.ExtensionUtil;
import org.eclipse.skalli.view.ext.InfoBox;
import org.eclipse.skalli.view.ext.ProjectInfoBox;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;

public class ProjectTeamBox extends InfoBox implements ProjectInfoBox {

    private static final String STYLE_TEAM_INFOBOX = "infobox-team"; //$NON-NLS-1$
    private static final String STYLE_TEAMLABEL = "teamlabel"; //$NON-NLS-1$

    @Override
    public String getIconPath() {
        return "res/icons/people.png"; //$NON-NLS-1$
    }

    @Override
    public String getCaption() {
        return "Team";
    }

    @Override
    public Component getContent(Project project, ExtensionUtil util) {
        Layout layout = new CssLayout();
        layout.addStyleName(STYLE_TEAM_INFOBOX);
        layout.setSizeFull();
        PeopleExtension ext = project.getExtension(PeopleExtension.class);
        if (ext != null) {
            if (ext.getLeads().size() > 0) {
                Label projectLeadHeaderLabel = new Label("Project Lead");
                projectLeadHeaderLabel.addStyleName(STYLE_TEAMLABEL);
                layout.addComponent(projectLeadHeaderLabel);
                Component peopleComponent = PeopleComponent.getPeopleListComponentForMember(ext.getLeads());
                peopleComponent.addStyleName(InformationBox.STYLE);
                layout.addComponent(peopleComponent);
            }

            if (ext.getMembers().size() > 0) {
                Label projectTeamHeaderLabel = new Label("Project Team/Committers");
                projectTeamHeaderLabel.addStyleName(STYLE_TEAMLABEL);
                layout.addComponent(projectTeamHeaderLabel);

                Component peopleComponent = PeopleComponent.getPeopleListComponentForMember(ext.getMembers());
                peopleComponent.addStyleName(InformationBox.STYLE);
                layout.addComponent(peopleComponent);
            }
        }
        return layout;
    }

    @Override
    public float getPositionWeight() {
        return 1.4f;
    }

    @Override
    public int getPreferredColumn() {
        return COLUMN_EAST;
    }

    @Override
    public boolean isVisible(Project project, String loggedInUserId) {
        PeopleExtension ext = project.getExtension(PeopleExtension.class);
        if (ext == null || ext.getMembers().isEmpty() && ext.getLeads().isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

}
