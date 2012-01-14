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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.commons.HtmlBuilder;
import org.eclipse.skalli.commons.Link;
import org.eclipse.skalli.ext.mapping.scm.ScmLocationMapper;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.ext.devinf.DevInfProjectExt;
import org.eclipse.skalli.services.configuration.ConfigurationService;
import org.eclipse.skalli.view.ext.ExtensionUtil;
import org.eclipse.skalli.view.ext.InfoBox;
import org.eclipse.skalli.view.ext.ProjectInfoBox;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Layout;

public class ProjectDevInfBox extends InfoBox implements ProjectInfoBox {

    private static final String STYLE_DEFINF_INFOBOX = "infobox-devInf"; //$NON-NLS-1$

    // TODO: solve the icon madness
    private static final String ICON_SOURCES = "/VAADIN/themes/simple/icons/devinf/code.png"; //$NON-NLS-1$
    private static final String ICON_BUGTRACKER = "/VAADIN/themes/simple/icons/devinf/bug.png"; //$NON-NLS-1$
    private static final String ICON_METRICS = "/VAADIN/themes/simple/icons/devinf/metrics.png"; //$NON-NLS-1$
    private static final String ICON_CI_SERVER = "/VAADIN/themes/simple/icons/devinf/ci_server.png"; //$NON-NLS-1$
    private static final String ICON_REVIEW = "/VAADIN/themes/simple/icons/devinf/review.png"; //$NON-NLS-1$
    private static final String ICON_JAVADOC = "/VAADIN/themes/simple/icons/devinf/javadoc.png"; //$NON-NLS-1$

    private ConfigurationService configService;

    protected void bindConfigurationService(ConfigurationService configService) {
        this.configService = configService;
    }

    protected void unbindConfigurationService(ConfigurationService configService) {
        this.configService = null;
    }

    @Override
    public String getIconPath() {
        return "res/icons/devInfInfo.png"; //$NON-NLS-1$
    }

    @Override
    public String getCaption() {
        return "Development Information";
    }

    @Override
    public Component getContent(Project project, ExtensionUtil util) {
        Layout layout = new CssLayout();
        layout.addStyleName(STYLE_DEFINF_INFOBOX);
        layout.setSizeFull();

        DevInfProjectExt devInf = project.getExtension(DevInfProjectExt.class);

        HtmlBuilder html = new HtmlBuilder();
        if (devInf != null) {
            // Project Sources
            if (StringUtils.isNotBlank(devInf.getScmUrl())) {
                html.appendIconizedLink(ICON_SOURCES, "Project Sources", devInf.getScmUrl()).appendLineBreak();
            }
            // Bug Tracker
            if (StringUtils.isNotBlank(devInf.getBugtrackerUrl())) {
                Set<String> linkList = new HashSet<String>();
                linkList.add(devInf.getBugtrackerUrl());
                addCreateBugLinks(linkList, project, devInf);
                html.appendIconizedLinks(ICON_BUGTRACKER, "Bug Tracker", "(Create Issue)", linkList).appendLineBreak();
            }
            // Code Metrics
            if (StringUtils.isNotBlank(devInf.getMetricsUrl())) {
                html.appendIconizedLink(ICON_METRICS, "Code Metrics", devInf.getMetricsUrl()).appendLineBreak();
            }
            // CI / Build Server
            if (StringUtils.isNotBlank(devInf.getCiUrl())) {
                html.appendIconizedLink(ICON_CI_SERVER, "Continuous Integration / Build Server", devInf.getCiUrl())
                        .appendLineBreak();
            }
            // Code Review
            if (StringUtils.isNotBlank(devInf.getReviewUrl())) {
                html.appendIconizedLink(ICON_REVIEW, "Code Review", devInf.getReviewUrl()).appendLineBreak();
            }
            // Javadoc
            if (CollectionUtils.isNotBlank(devInf.getJavadocs())) {
                html.appendIconizedLinks(ICON_JAVADOC, "Javadoc", "(more Javadoc)", devInf.getJavadocs()).appendLineBreak();
            }

            // SCM Locations
            if (CollectionUtils.isNotBlank(devInf.getScmLocations())) {
                html.appendHeader("Source Locations", 4).append('\n');
                html.append("<ul>\n"); //$NON-NLS-1$
                ScmLocationMapper mapper = new ScmLocationMapper();
                for (String scmUrl : devInf.getScmLocations()) {
                    html.append("<li>"); //$NON-NLS-1$
                    html.append(copyToClipboardLink(scmUrl, scmUrl.replaceFirst("^scm:.+?:", ""))); //$NON-NLS-1$ //$NON-NLS-2$
                    List<Link> mappedScmLinks = mapper.getMappedLinks(configService, project.getProjectId(), scmUrl,
                            ScmLocationMapper.PURPOSE_BROWSE, ScmLocationMapper.PURPOSE_REVIEW);
                    html.appendLinks(mappedScmLinks);
                    html.append("</li>\n"); //$NON-NLS-1$
                }
                html.append("</ul>\n"); //$NON-NLS-1$
            }
        }

        if (html.length() > 0) {
            createLabel(layout, html.toString());
        } else {
            createLabel(layout, "This project has no development information.");
        }
        return layout;
    }

    private void addCreateBugLinks(Set<String> linkList, Project project, DevInfProjectExt devInf) {
        List<Link> createBugLinks = getCreateBugUrl(devInf.getBugtrackerUrl(), project);
        for (Link createBugLink : createBugLinks) {
            linkList.add(createBugLink.getUrl());
        }
    }

    private List<Link> getCreateBugUrl(String bugtrackerUrl, Project project) {
        return (new ScmLocationMapper()).getMappedLinks(configService, project.getProjectId(), bugtrackerUrl,
                ScmLocationMapper.PURPOSE_CREATE_BUG);
    }

    @Override
    public float getPositionWeight() {
        return 1.5f;
    }

    @Override
    public int getPreferredColumn() {
        return COLUMN_WEST;
    }

    @Override
    public boolean isVisible(Project project, String loggedInUserId) {
        if (project.getExtension(DevInfProjectExt.class) != null) {
            return true;
        } else {
            return false;
        }
    }
}
