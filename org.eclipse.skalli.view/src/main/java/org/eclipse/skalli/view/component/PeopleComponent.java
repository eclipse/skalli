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
package org.eclipse.skalli.view.component;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.Member;
import org.eclipse.skalli.model.User;
import org.eclipse.skalli.view.Consts;
import org.eclipse.skalli.view.internal.container.UserContainer;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;

@SuppressWarnings("serial")
public class PeopleComponent extends CustomComponent {

    private static final String STYLE = "peoplecomponent"; //$NON-NLS-1$

    protected PeopleComponent(final User user) {
        addStyleName(STYLE);

        Layout layout = new CssLayout();
        layout.setSizeFull();
        layout.setMargin(false);

        StringBuilder sb = new StringBuilder();
        sb.append("<span class=\"v-img-peoplecomponent\">"); //$NON-NLS-1$
        sb.append("<img src=\"/VAADIN/themes/simple/icons/people/team.png\" /> "); //$NON-NLS-1$
        sb.append("</span>"); //$NON-NLS-1$

        String userDetailsLink = UserDetailsUtil.getUserDetailsLink(user.getUserId());
        if (userDetailsLink != null) {
            // user details link configured, render a link to user details dialog
            sb.append("<a href=\""); //$NON-NLS-1$
            sb.append(userDetailsLink);
            sb.append("\" target=\"_blank\">"); //$NON-NLS-1$
            sb.append(user.getDisplayName());
            sb.append("</a> "); //$NON-NLS-1$
        } else {
            // not configured, just display the user name
            sb.append(user.getDisplayName());
            sb.append(" "); //$NON-NLS-1$
        }

        sb.append("<span class=\"v-link-peoplecomponent\">"); //$NON-NLS-1$

        if (!StringUtils.isBlank(user.getEmail()))
        {
            sb.append("<a class=\"link\" href=\"mailto:"); //$NON-NLS-1$
            sb.append(user.getEmail());
            sb.append("\">"); //$NON-NLS-1$
            sb.append("mail");
            sb.append("</a> "); //$NON-NLS-1$
        }

        sb.append("<a class=\"link\" href=\""); //$NON-NLS-1$
        sb.append(Consts.URL_PROJECTS_USER);
        sb.append(user.getUserId());
        sb.append("\">"); //$NON-NLS-1$
        sb.append("projects");
        sb.append("</a> "); //$NON-NLS-1$

        sb.append("</span>"); //$NON-NLS-1$

        Label lbl = new Label();
        lbl.setContentMode(Label.CONTENT_XHTML);
        lbl.setValue(sb.toString());
        layout.addComponent(lbl);

        setCompositionRoot(layout);
    }

    public static Component getPeopleListComponent(Set<User> users) {
        return new PeopleListComponent(users);
    }

    public static Component getPeopleListComponentForMember(Set<Member> member) {
        return new PeopleListComponent(UserContainer.getUsers(member));
    }

    static class PeopleListComponent extends CustomComponent {
        public PeopleListComponent(Set<User> users) {

            TreeSet<User> sortedUsers = new TreeSet<User>(new Comparator<User>() {
                @Override
                public int compare(User u1, User u2) {
                    return u1.getDisplayName().compareTo(u2.getDisplayName());
                }
            });
            sortedUsers.addAll(users);

            Layout layout = new CssLayout();
            layout.setSizeFull();
            for (User user : sortedUsers) {
                layout.addComponent(new PeopleComponent(user));
            }
            setCompositionRoot(layout);
        }
    }
}
