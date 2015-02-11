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

import java.util.UUID;

import org.eclipse.skalli.commons.UUIDList;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.ext.misc.RelatedProjectsExt;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.entity.EntityServices;
import org.eclipse.skalli.services.project.ProjectService;
import org.eclipse.skalli.services.search.SearchHit;
import org.eclipse.skalli.services.search.SearchResult;
import org.eclipse.skalli.services.search.SearchService;
import org.eclipse.skalli.view.ext.ExtensionUtil;
import org.eclipse.skalli.view.ext.InfoBox;
import org.eclipse.skalli.view.ext.InfoBoxBase;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;

public class RelatedProjectsInfoBox extends InfoBoxBase implements InfoBox {

    private static final String STYLE_RELATEDPROJECTS_INFOBOX = "infobox-related-projects"; //$NON-NLS-1$

    @Override
    public String getIconPath() {
        return "res/icons/relProjects.png"; //$NON-NLS-1$
    }

    @Override
    public String getCaption() {
        return "Related Projects";
    }

    @Override
    public String getShortName() {
        return "relatedProjects";
    }

    @Override
    public Component getContent(Project project, ExtensionUtil util) {
        Layout layout = new CssLayout();
        layout.addStyleName(STYLE_RELATEDPROJECTS_INFOBOX);
        layout.setSizeFull();

        RelatedProjectsExt ext = project.getExtension(RelatedProjectsExt.class);
        if (ext != null) {
            createLabel(layout, "The following projects might also be of interest to you:");
            boolean calculated = ext.getCalculated();
            if (calculated) {
                addCalculatedContent(project, layout);
            } else {
                UUIDList ids = ext.getRelatedProjects();
                ProjectService projectService = ((ProjectService)EntityServices.getByEntityClass(Project.class));
                for (UUID uuid : ids) {
                    Project relatedProject = projectService.getByUUID(uuid);
                    if (relatedProject != null) {
                        //project might have deleted meanwhile
                        ExternalResource externalResource = new ExternalResource("/projects/"
                                + relatedProject.getProjectId());
                        String content = HSPACE + "<a href=\"" + externalResource.getURL() + "\">"
                                + relatedProject.getName()
                                + "</a>";
                        createLabel(layout, content);
                    }
                }
            }
        }
        return layout;
    }

    protected void addCalculatedContent(Project project, Layout layout) {
        SearchService searchService = Services.getService(SearchService.class);
        if (searchService == null) {
            return;
        }
        SearchResult<Project> relatedProjects = searchService.getRelatedProjects(project, 3);
        if (relatedProjects.getResultCount() == 0) {
            createLabel(layout, HSPACE + "No matches found");
            return;
        }
        for (SearchHit<Project> hit : relatedProjects.getResult()) {
            ExternalResource externalResource = new ExternalResource("/projects/" + hit.getEntity().getProjectId());
            String content = HSPACE + "<a href=" + externalResource.getURL() + ">" + hit.getEntity().getName()
                    + "*</a>";
            createLabel(layout, content);
        }
        Label label = new Label(HSPACE + "*calculated based on similarities between the projects",
                Label.CONTENT_XHTML);
        label.setStyleName("light");//$NON-NLS-1$
        layout.addComponent(label);

    }

    @Override
    public float getPositionWeight() {
        return 1.8f;
    }

    @Override
    public int getPreferredColumn() {
        return COLUMN_EAST;
    }

    @Override
    public boolean isVisible(Project project, String loggedInUserId) {
        RelatedProjectsExt ext = project.getExtension(RelatedProjectsExt.class);
        if (ext == null || (ext.getRelatedProjects().isEmpty() && !ext.getCalculated())) {
            return false;
        } else {
            return true;
        }
    }

}
