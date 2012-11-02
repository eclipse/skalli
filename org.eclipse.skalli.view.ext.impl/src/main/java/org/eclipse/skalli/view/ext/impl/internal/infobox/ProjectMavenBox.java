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

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.ext.mapping.scm.ScmLocationMapper;
import org.eclipse.skalli.ext.mapping.scm.ScmLocationMappingConfig;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.ext.devinf.DevInfProjectExt;
import org.eclipse.skalli.model.ext.maven.MavenCoordinate;
import org.eclipse.skalli.model.ext.maven.MavenModule;
import org.eclipse.skalli.model.ext.maven.MavenProjectExt;
import org.eclipse.skalli.model.ext.maven.MavenReactor;
import org.eclipse.skalli.model.ext.maven.MavenReactorProjectExt;
import org.eclipse.skalli.model.ext.maven.MavenResolverService;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.configuration.ConfigurationService;
import org.eclipse.skalli.services.extension.PropertyMapper;
import org.eclipse.skalli.view.ext.ExtensionUtil;
import org.eclipse.skalli.view.ext.InfoBox;
import org.eclipse.skalli.view.ext.InfoBoxBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.PopupView;

public class ProjectMavenBox extends InfoBoxBase implements InfoBox {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectMavenBox.class);

    private static final String STYLE_MAVEN_INFOBOX = "infobox-maven"; //$NON-NLS-1$
    private static final String STYLE_MODULE_POPUP = "module-popup"; //$NON-NLS-1$

    private static final String DEFAULT_POM_FILENAME = "pom.xml"; //$NON-NLS-1$

    private ConfigurationService configService;

    protected void bindConfigurationService(ConfigurationService configService) {
        this.configService = configService;
    }

    protected void unbindConfigurationService(ConfigurationService configService) {
        this.configService = configService;
    }

    @Override
    public String getIconPath() {
        return "res/icons/maven.png"; //$NON-NLS-1$
    }

    @Override
    public String getCaption() {
        return "Maven Project Information";
    }

    @Override
    public Component getContent(Project project, ExtensionUtil util) {
        Layout layout = new CssLayout();
        layout.addStyleName(STYLE_MAVEN_INFOBOX);
        layout.setSizeFull();

        boolean rendered = false;
        String groupId = null;
        MavenReactorProjectExt reactorExt = project.getExtension(MavenReactorProjectExt.class);
        if (reactorExt != null) {
            MavenReactor mavenReactor = reactorExt.getMavenReactor();
            if (mavenReactor != null) {
                MavenCoordinate coordinate = mavenReactor.getCoordinate();
                groupId = coordinate.getGroupId();
                createLabel(layout, "GroupId: <b>" + groupId + "</b>");//$NON-NLS-1$ //$NON-NLS-2$
                createLabel(layout, "ArtifactId: <b>" + coordinate.getArtefactId() + "</b>");//$NON-NLS-1$ //$NON-NLS-2$
                TreeSet<MavenModule> modules = mavenReactor.getModules();
                StringBuilder sb = new StringBuilder();

                if (modules.size() > 0) {
                    int lineLength = 0;
                    for (MavenModule module : modules) {
                        //create popup with xml snippet
                        sb.append("<dependency>\n");
                        sb.append("    <artifactId>" + module.getArtefactId() + "</artifactId>\n");
                        sb.append("    <groupId>" + module.getGroupId() + "</groupId>\n");
                        String latestVersion = module.getLatestVersion();
                        if (StringUtils.isNotBlank(latestVersion)) {
                            sb.append("    <version>" + latestVersion + "</version>\n");
                        } else {
                            sb.append("    <!--<version>0.0.0</version>-->\n");
                        }
                        String packaging = module.getPackaging();
                        if (StringUtils.isNotBlank(packaging)) {
                            sb.append("    <type>" + packaging + "</type>\n");
                        }
                        sb.append("</dependency>\n");
                        lineLength = calculateLineLength(module, lineLength);
                    }

                    final Label label = new Label(sb.toString(), Label.CONTENT_PREFORMATTED);
                    //add a buffer 10, as we didn't calculate the length of surrounding strings.
                    label.setWidth(lineLength + 10, Sizeable.UNITS_EM);

                    PopupView.Content content = new PopupView.Content() {
                        private static final long serialVersionUID = -8362267064485433525L;

                        @Override
                        public String getMinimizedValueAsHTML() {
                            return "Modules";
                        }

                        @Override
                        public Component getPopupComponent() {
                            return label;
                        }
                    };

                    PopupView popup = new PopupView(content);
                    popup.setHideOnMouseOut(false);
                    popup.addStyleName(STYLE_MODULE_POPUP);
                    layout.addComponent(popup);
                }
                rendered = true;
            }
        }
        MavenProjectExt mavenExt = project.getExtension(MavenProjectExt.class);
        if (mavenExt != null) {
            if (groupId == null) {
                groupId = mavenExt.getGroupID();
                if (StringUtils.isNotBlank(groupId)) {
                    createLabel(layout, "GroupId: <b>&nbsp;" + groupId + "</b>");//$NON-NLS-1$ //$NON-NLS-2$
                    rendered = true;
                }
            }
            DevInfProjectExt devInf = project.getExtension(DevInfProjectExt.class);
            if (devInf != null) {
                String reactorPomUrl = getReactorPomUrl(project, devInf, mavenExt);
                if (reactorPomUrl == null) {
                    String reactorPomPath = mavenExt.getReactorPOM();
                    String caption = MessageFormat.format(
                            "Reactor POM Path: {0} (relative to SCM root location)",
                            StringUtils.isNotBlank(reactorPomPath) ? reactorPomPath : "/");
                    createLabel(layout, caption);
                } else {
                    createLink(layout, "Reactor POM", reactorPomUrl);
                }
                rendered = true;
            }
            if (StringUtils.isNotBlank(mavenExt.getSiteUrl())) {
                createLink(layout, "Project Site", mavenExt.getSiteUrl());
                rendered = true;
            }
        }
        if (!rendered) {
            createLabel(layout, "Maven extension added but no data maintained.");
        }
        return layout;
    }

    private int calculateLineLength(MavenCoordinate module, int previousValue) {
        int newLength;
        int artefactLength = module.getArtefactId().length();
        int groupLength = module.getGroupId().length();
        newLength = Math.max(groupLength, artefactLength);
        newLength = Math.max(previousValue, newLength);

        return newLength;
    }

    private String getReactorPomUrl(Project project, DevInfProjectExt devInf, MavenProjectExt mavenExt) {
        if (configService == null) {
            return null;
        }
        String scmLocation = devInf.getScmLocation();
        if (StringUtils.isBlank(scmLocation)) {
            return null;
        }
        String relativePath = mavenExt.getReactorPOM();
        if (!isValidNormalizedPath(relativePath)) {
            return null;
        }
        ScmLocationMapper mapper = new ScmLocationMapper();
        List<ScmLocationMappingConfig> mappings = mapper.getMappings(configService,
                "git", ScmLocationMapper.PURPOSE_BROWSE); //$NON-NLS-1$
        if (mappings.isEmpty()) {
            return null;
        }
        String repositoryRoot = null;
        for (ScmLocationMappingConfig mapping : mappings) {
            repositoryRoot = PropertyMapper.convert(scmLocation, mapping.getPattern(),
                    mapping.getTemplate(), project.getProjectId());
            if (StringUtils.isNotBlank(repositoryRoot)) {
                break;
            }
        }
        if (StringUtils.isBlank(repositoryRoot)) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(repositoryRoot);
        sb.append(";f="); //$NON-NLS-1$
        if (StringUtils.isBlank(relativePath) || ".".equals(relativePath)) { //$NON-NLS-1$
            sb.append(DEFAULT_POM_FILENAME);
        }
        else if (!relativePath.endsWith(DEFAULT_POM_FILENAME)) {
            appendPath(sb, relativePath);
            if (!relativePath.endsWith("/")) { //$NON-NLS-1$
                sb.append("/"); //$NON-NLS-1$
            }
            sb.append(DEFAULT_POM_FILENAME);
        }
        else {
            appendPath(sb, relativePath);
        }
        sb.append(";hb=HEAD"); //$NON-NLS-1$
        return sb.toString();
    }

    private void appendPath(StringBuilder sb, String relativePath) {
        if (relativePath.charAt(0) == '/') {
            sb.append(relativePath.substring(1));
        } else {
            sb.append(relativePath);
        }
    }

    @SuppressWarnings("nls")
    private boolean isValidNormalizedPath(String path) {
        if (StringUtils.isNotBlank(path)) {
            if (path.indexOf('\\') >= 0) {
                return false;
            }
            if (path.indexOf("..") >= 0 ||
                    path.startsWith("./") ||
                    path.endsWith("/.") ||
                    path.indexOf("/./") >= 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public float getPositionWeight() {
        return 1.6f;
    }

    @Override
    public int getPreferredColumn() {
        return COLUMN_WEST;
    }

    @Override
    public boolean isVisible(Project project, String loggedInUserId) {
        return project.getExtension(MavenProjectExt.class) != null;
    }

    @Override
    public String getShortName() {
        return "maven"; //$NON-NLS-1$
    }

    @Override
    public void perform(String action, Project project, String userId) {
        if (REFRESH_ACTION.equalsIgnoreCase(action)) {
            try {
                MavenResolverService mavenService = Services.getService(MavenResolverService.class);
                if (mavenService != null) {
                    mavenService.queue(project, userId);
                }
            } catch (Exception e) {
                LOG.error(MessageFormat.format("Failed to perform \''{0}\'' action on project \''{1}\'' for user \''{2}\''",
                        action, project.getUuid(), userId));
            }
        }
    }

    @Override
    public List<String> getSupportedActions() {
        return Arrays.asList(REFRESH_ACTION);
    }
}
