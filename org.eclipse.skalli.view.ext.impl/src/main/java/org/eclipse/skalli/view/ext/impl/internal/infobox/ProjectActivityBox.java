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

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.Link;
import org.eclipse.skalli.ext.mapping.scm.ScmLocationMapper;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.ext.devinf.DevInfProjectExt;
import org.eclipse.skalli.view.ext.ExtensionUtil;
import org.eclipse.skalli.view.ext.InfoBox;
import org.eclipse.skalli.view.ext.InfoBoxBase;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;

public class ProjectActivityBox extends InfoBoxBase implements InfoBox {

    private static final String STYLE_ACTIVITY_INFOBOX = "infobox-activity"; //$NON-NLS-1$

    @Override
    public String getIconPath() {
        return "res/icons/activity.png"; //$NON-NLS-1$
    }

    @Override
    public String getCaption() {
        return "Project Activity";
    }

    @Override
    public String getShortName() {
        return "activity";
    }

    @Override
    public float getPositionWeight() {
        return 1.55f;
    }

    @Override
    public int getPreferredColumn() {
        return COLUMN_WEST;
    }

    @Override
    public boolean isVisible(Project project, String loggedInUserId) {
        return getActivityGraphicUrl(project, loggedInUserId) != null
                || getActivityDetailsUrl(project, loggedInUserId) != null;
    }

    private Link getActivityGraphicUrl(Project project, String userId) {
        return getMappedLink(ScmLocationMapper.PURPOSE_ACTIVITY, project, userId);
    }

    private Link getActivityDetailsUrl(Project project, String userId) {
        return getMappedLink(ScmLocationMapper.PURPOSE_ACTIVITY_DETAILS, project, userId);
    }

    private Link getMappedLink(String purpose, Project project, String userId) {
        DevInfProjectExt devInf = project.getExtension(DevInfProjectExt.class);
        if (devInf == null) {
            return null;
        }
        String scmLocation = devInf.getScmLocation();
        if (StringUtils.isBlank(scmLocation)) {
            return null;
        }
        ScmLocationMapper mapper = new ScmLocationMapper(ScmLocationMapper.ALL_PROVIDERS, purpose);
        List<Link> links = mapper.getMappedLinks(scmLocation, userId, project);
        return CollectionUtils.isNotEmpty(links)? links.get(0) : null;
    }

    @Override
    public Component getContent(Project project, ExtensionUtil util) {
        Layout layout = new CssLayout();
        layout.addStyleName(STYLE_ACTIVITY_INFOBOX);
        layout.setSizeFull();

        Label label;
        label = new Label("Latest commit activity (click for details):");
        layout.addComponent(label);
        layout.addComponent(new Label("&nbsp;", Label.CONTENT_XHTML));

        Link imgLink = getActivityGraphicUrl(project, util.getLoggedInUserId());
        Link detailsLink = getActivityDetailsUrl(project, util.getLoggedInUserId());

        Label activityArea = new Label();
        activityArea.setContentMode(Label.CONTENT_XHTML);
        if (imgLink != null) {
            String imgTag =
                    "<img src=\"" + imgLink.getUrl() + "\" " +
                            "title=\"" + imgLink.getLabel() + "\" " +
                            "onerror=\"document.getElementById('activityBoxError').style.visibility='visible';" +
                            "document.getElementById('activityBoxImage').style.visibility='hidden';\" " +
                            ">";

            String content;
            if (detailsLink != null) {
                content = "<a href=\"" + detailsLink.getUrl() + "\" target=\"_new\">" + imgTag + "</a>";
            } else {
                content = imgTag;
            }
            activityArea.setValue("<span id=\"activityBoxImage\">" + content + "</span>");
            layout.addComponent(activityArea);

            Label errorArea = new Label(
                    "<span id=\"activityBoxError\" " +
                            "style=\"visibility:hidden\">" +
                            "<i>Currently there is no activity information available.</i>" +
                            "</span>");
            errorArea.setContentMode(Label.CONTENT_XHTML);
            layout.addComponent(errorArea);
        } else if (detailsLink != null) {
            activityArea.setValue("<a href=\"" + detailsLink.getUrl() + "\" target=\"_new\">Show details</a>");
            layout.addComponent(activityArea);
        }

        return layout;
    }
}
