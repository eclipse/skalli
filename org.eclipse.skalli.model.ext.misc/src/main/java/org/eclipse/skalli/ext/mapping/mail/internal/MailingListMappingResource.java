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
package org.eclipse.skalli.ext.mapping.mail.internal;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.skalli.ext.mapping.mail.MailingListMapping;
import org.eclipse.skalli.ext.mapping.mail.MailingListMappings;
import org.eclipse.skalli.services.configuration.ConfigResourceBase;

public class MailingListMappingResource extends ConfigResourceBase<MailingListMappings> {

    @Override
    protected Class<MailingListMappings> getConfigClass() {
        return MailingListMappings.class;
    };

    @Override
    protected List<Class<?>> getAdditionalConfigClasses() {
        List<Class<?>> ret = new LinkedList<Class<?>>();
        ret.add(MailingListMapping.class);
        return ret;
    }

}
