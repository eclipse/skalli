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
package org.eclipse.skalli.ext.mapping.mail;

import org.eclipse.skalli.commons.LinkMapping;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("mailingListMapping")
public class MailingListMapping extends LinkMapping {

    // do not remove: required by xstream
    public MailingListMapping() {
        super();
    }

    public MailingListMapping(String id, String purpose, String pattern, String template, String name) {
        super(id, purpose, pattern, template, name);
    }
}
