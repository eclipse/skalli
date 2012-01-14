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
package org.eclipse.skalli.core.internal.mail;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.Member;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.User;
import org.eclipse.skalli.model.ext.commons.PeopleExtension;
import org.eclipse.skalli.services.mail.MailService;
import org.eclipse.skalli.services.user.UserUtils;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MailServiceImpl implements MailService {

    private static final Logger LOG = LoggerFactory.getLogger(MailServiceImpl.class);

    protected void activate(ComponentContext context) {
        LOG.info(MessageFormat.format("[MailService] {0} : activated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    protected void deactivate(ComponentContext context) {
        LOG.info(MessageFormat.format("[MailService] {0} : deactivated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    @Override
    public void sendMailToProject(Project project, String subject, String body, Address from) {
        List<Address> toAddresses = getToAddresses(project);
        List<Address> ccAddresses = getCCAddresses(project);

        if (toAddresses.size() > 0) {
            sendMailInternal(toAddresses.toArray(new Address[toAddresses.size()]),
                    ccAddresses.toArray(new Address[ccAddresses.size()]), null, from, subject, body);
        } else if (ccAddresses.size() > 0) {
            sendMailInternal(ccAddresses.toArray(new Address[ccAddresses.size()]),
                    null, null, from, subject, body);
        }
    }

    @Override
    public List<Address> getToAddresses(Project project) {
        PeopleExtension ext = project.getExtension(PeopleExtension.class);
        if (ext == null) {
            return Collections.emptyList();
        }
        return getAdresses(ext.getLeads());
    }

    @Override
    public List<Address> getCCAddresses(Project project) {
        PeopleExtension ext = project.getExtension(PeopleExtension.class);
        if (ext == null) {
            return Collections.emptyList();
        }
        return getAdresses(ext.getMembers());
    }

    /******************
     * internal methods
     ******************/

    private List<Address> getAdresses(Set<Member> members) {
        List<Address> addressList = new ArrayList<Address>();
        for (Member member : members) {
            User user = UserUtils.getUser(member.getUserID());
            if (StringUtils.isNotBlank(user.getEmail())) {
                try {
                    Address address = new InternetAddress(user.getEmail());
                    addressList.add(address);
                } catch (AddressException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return addressList;
    }

    private void sendMailInternal(Address[] rcptTo, Address[] rcptCC, Address[] rcptBCC, Address from, String subject,
            String body) {
        try {
            String mailHost = "mail.sap.corp"; //$NON-NLS-1$
            Properties props = System.getProperties();
            props.put("mail.smtp.host", mailHost); //$NON-NLS-1$
            Session session = Session.getInstance(props, null);
            Message message = new MimeMessage(session);
            message.setFrom(from);
            if (rcptTo != null && rcptTo.length > 0) {
                message.setRecipients(Message.RecipientType.TO, rcptTo);
            }
            if (rcptCC != null && rcptCC.length > 0) {
                message.setRecipients(Message.RecipientType.CC, rcptCC);
            }
            if (rcptBCC != null && rcptBCC.length > 0) {
                message.setRecipients(Message.RecipientType.BCC, rcptBCC);
            }

            message.setSubject(subject);
            message.setContent(body, "text/plain"); //$NON-NLS-1$
            Transport.send(message);
        } catch (AddressException e) {
            throw new RuntimeException(e);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

}
