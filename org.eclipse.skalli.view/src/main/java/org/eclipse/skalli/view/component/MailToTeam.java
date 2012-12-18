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

import java.text.MessageFormat;
import java.util.Set;
import java.util.SortedSet;

import org.eclipse.skalli.model.Member;
import org.eclipse.skalli.model.User;
import org.eclipse.skalli.model.ext.commons.PeopleExtension;
import org.eclipse.skalli.view.internal.container.UserContainer;

public class MailToTeam {

    private PeopleExtension ext;

    public MailToTeam(PeopleExtension ext) {
        this.ext = ext;
    }

    public String composeMailToTeamLabel() {
        SortedSet<Member> leads = ext.getLeads();
        StringBuilder sendTo = new StringBuilder();
        Set<User> teamLeads = UserContainer.getUsers(leads);

        for (User user : teamLeads) {
            appendEmail(sendTo, user);
        }

        SortedSet<Member> members = ext.getMembers();
        Set<User> teamMembers = UserContainer.getUsers(members);
        StringBuilder sendCc = new StringBuilder();
        for (User user : teamMembers) {
            if (teamLeads.contains(user)) {
                continue;
            }
            appendEmail(sendCc, user);
        }
        StringBuilder sb = new StringBuilder();
        String mailTo = sendTo.toString();
        if (mailTo.length() == 0 && sendCc.length() > 0) {
            mailTo = sendCc.toString();
        } else if (sendCc.length() > 0) {
            mailTo = MessageFormat.format("{0}?cc={1}", mailTo, sendCc.toString());//$NON-NLS-1$
        }
        sb.append("<span class=\"v-img-mailImage\">"); //$NON-NLS-1$
        sb.append("<img src=\"/VAADIN/themes/simple/icons/mail/email.png\" /> "); //$NON-NLS-1$
        sb.append("</span>"); //$NON-NLS-1$
        sb.append("<span class=\"v-link\">"); //$NON-NLS-1$
        sb.append("<a href=\"mailto:"); //$NON-NLS-1$
        sb.append(mailTo);
        sb.append("\">"); //$NON-NLS-1$
        sb.append("Contact Team");
        sb.append("</a> "); //$NON-NLS-1$
        sb.append("</span>"); //$NON-NLS-1$



        return sb.toString();
    }

    private void appendEmail(StringBuilder sendTo, User user) {
        if (sendTo.length() > 0) {
            sendTo.append(";");//$NON-NLS-1$
        }
        if (user.hasEmail()) {
            sendTo.append(user.getEmail());
        }
    }



}
