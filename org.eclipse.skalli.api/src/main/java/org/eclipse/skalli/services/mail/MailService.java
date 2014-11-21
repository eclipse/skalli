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
package org.eclipse.skalli.services.mail;

import java.util.List;

import javax.mail.Address;
import javax.management.relation.Role;

import org.eclipse.skalli.model.Project;

/**
 * Service that provides access to the configured mail system.
 */
public interface MailService {

    /**
     * Sends a mail to relevant persons of a certain project. Methods
     * {@link MailService#getToAddresses(Project)} and {@link MailService#getCCAddresses(Project)}
     * are used for calculation TO and CC of the email.
     *
     * @param project email will be sent to project leads and members of this project.
     * @param subject subject line of the mail.
     * @param body mail content.
     * @param from sender of the mail.
     */
    public void sendMailToProject(Project project, String subject, String body, Address from);

    /**
     * returns a list of email addresses for a certain project an email should be sent to
     * as TO. All project members with role {@link Role#PROJECT_LEAD} are added to the result
     * list.
     *
     * @param project project instance
     * @return list of email addresses
     */
    public List<Address> getToAddresses(Project project);

    /**
     * returns a list of email addresses for a certain project an email should be sent to
     * as CC. All project members with role {@link Role#PROJECT_MEMBER} are added to the result
     * list.
     *
     * @param project project instance
     * @return list of email addresses
     */
    public List<Address> getCCAddresses(Project project);
}
