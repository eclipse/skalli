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

import java.util.List;
import java.util.Set;

import org.eclipse.skalli.commons.HtmlBuilder;
import org.eclipse.skalli.commons.Link;
import org.eclipse.skalli.ext.mapping.mail.MailingListMapper;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.ext.commons.InfoExtension;
import org.eclipse.skalli.services.configuration.ConfigurationService;
import org.eclipse.skalli.services.extension.LinkMapper;
import org.eclipse.skalli.view.ext.ExtensionUtil;
import org.eclipse.skalli.view.ext.InfoBox;
import org.eclipse.skalli.view.ext.InfoBoxBase;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Layout;

public class ProjectMailingListBox extends InfoBoxBase implements InfoBox {

    private static final String STYLE_MAILING_INFOBOX = "infobox-mailingList"; //$NON-NLS-1$

    private ConfigurationService configService;

    protected void bindConfigurationService(ConfigurationService configService) {
        this.configService = configService;
    }

    protected void unbindConfigurationService(ConfigurationService configService) {
        this.configService = configService;
    }

    @Override
    public String getIconPath() {
        return "res/icons/mailinglist.png"; //$NON-NLS-1$
    }

    @Override
    public String getCaption() {
        return "Mailing Lists";
    }

    @Override
    public String getShortName() {
        return "mailingLists" ;
    }

    @SuppressWarnings("nls")
    @Override
    public Component getContent(Project project, ExtensionUtil util) {
        Layout layout = new CssLayout();
        layout.addStyleName(STYLE_MAILING_INFOBOX);
        layout.setSizeFull();

        HtmlBuilder html = new HtmlBuilder();
        InfoExtension ext = project.getExtension(InfoExtension.class);
        if (ext != null) {
            Set<String> mailingLists = ext.getMailingLists();
            if (mailingLists.size() > 0) {
                MailingListMapper mapper = new MailingListMapper(LinkMapper.ALL_PURPOSES);
                html.append("<ul>"); //$NON-NLS-1$
                for (String mailingList : ext.getMailingLists()) {
                    html.append("<li>");
                    html.appendMailToLink(mailingList);
                    List<Link> mappedLinks = mapper.getMappedLinks(mailingList,
                            util.getLoggedInUserId(), project, configService);
                    if (!mappedLinks.isEmpty()) {
                        html.appendLineBreak();
                        html.appendLinks(mappedLinks);
                    }
                    html.append("</li>");
                }
                html.append("</ul>");
            }
        }

        if (html.length() > 0) {
            createLabel(layout, html.toString());
        } else {
            createLabel(layout, "This project has no mailing lists."); //$NON-NLS-1$
        }
        return layout;
    }

    @Override
    public float getPositionWeight() {
        return 1.2f;
    }

    @Override
    public int getPreferredColumn() {
        return COLUMN_WEST;
    }

    @Override
    public boolean isVisible(Project project, String loggedInUserId) {
        InfoExtension ext = project.getExtension(InfoExtension.class);
        return ext != null && !ext.getMailingLists().isEmpty();
    }

}
