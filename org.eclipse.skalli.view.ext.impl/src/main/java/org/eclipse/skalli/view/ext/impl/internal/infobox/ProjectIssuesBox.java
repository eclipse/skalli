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

import java.util.SortedSet;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.group.GroupUtils;
import org.eclipse.skalli.services.issues.Issues;
import org.eclipse.skalli.services.issues.IssuesService;
import org.eclipse.skalli.services.project.ProjectUtils;
import org.eclipse.skalli.view.Consts;
import org.eclipse.skalli.view.ext.ExtensionUtil;
import org.eclipse.skalli.view.ext.InfoBox;
import org.eclipse.skalli.view.ext.ProjectInfoBox;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Layout;

public class ProjectIssuesBox extends InfoBox implements ProjectInfoBox {

    private static final String STYLE_ISSUES_INFOBOX = "infobox-issues"; //$NON-NLS-1$
    private static final String STYLE_ISSUES = "prj-issues"; //$NON-NLS-1$

    @Override
    public String getIconPath() {
        return "res/icons/issues.png"; //$NON-NLS-1$
    }

    @Override
    public String getCaption() {
        return "Issues";
    }

    @Override
    @SuppressWarnings("nls")
    public Component getContent(Project project, ExtensionUtil util) {
        Layout layout = new CssLayout();
        layout.addStyleName(STYLE_ISSUES_INFOBOX);
        layout.setSizeFull();

        IssuesService issuesService = Services.getService(IssuesService.class);
        if (issuesService != null) {
            Issues issues = issuesService.loadEntity(Issues.class, project.getUuid());
            StringBuilder sb = new StringBuilder();
            if (issues != null) {
                if (issues.isStale()) {
                    sb.append("<ul><li class=\"STALE\">No information about issues available.</li></ul>");
                } else {
                    SortedSet<Issue> issueSet = issues.getIssues();
                    if (!issueSet.isEmpty()) {
                        sb.append(Issue.asHTMLList(null, issueSet));
                        if (util.isUserProjectAdmin(project)) {
                            sb.append("<p>Click <a href=\"").append(Consts.URL_PROJECTS).append("/")
                                    .append(project.getProjectId()).append("?").append(Consts.PARAM_ACTION).append("=")
                                    .append(Consts.PARAM_VALUE_EDIT).append("\">here</a> to correct ");
                            sb.append((issueSet.size() == 1) ? "this issue" : "these issues");
                            sb.append(".</p>");
                        }
                    }
                }
                if (util.isUserProjectAdmin(project)) {
                    sb.append("<p>Click <a href=\"").append(Consts.URL_PROJECTS).append("/");
                    sb.append(project.getProjectId()).append("?").append(Consts.PARAM_ACTION).append("=");
                    sb.append(Consts.PARAM_VALUE_VALIDATE).append("\">here</a> to validate the project now.</p>");
                }
                createLabel(layout, sb.toString(), STYLE_ISSUES);
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
        return COLUMN_EAST;
    }

    @Override
    public boolean isVisible(Project project, String loggedInUserId) {
        if (StringUtils.isBlank(loggedInUserId)) {
            return false;
        }

        boolean showIssues = GroupUtils.isAdministrator(loggedInUserId)
                || ProjectUtils.isProjectAdminInParentChain(loggedInUserId, project);
        if (!showIssues) {
            return false;
        }

        IssuesService issuesService = Services.getService(IssuesService.class);
        if (issuesService == null) {
            return false;
        }

        Issues issues = issuesService.loadEntity(Issues.class, project.getUuid());
        return issues != null && (issues.hasIssues() || issues.isStale());
    }

}
