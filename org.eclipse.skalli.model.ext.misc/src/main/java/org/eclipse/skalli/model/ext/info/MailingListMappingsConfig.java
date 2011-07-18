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
package org.eclipse.skalli.model.ext.info;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("mailingListMappings")
public class MailingListMappingsConfig {

    @XStreamImplicit
    private ArrayList<MailingListMappingConfig> mailingListMappings;

    public MailingListMappingsConfig(ArrayList<MailingListMappingConfig> mailingListMappings) {
        this.mailingListMappings = mailingListMappings;
    }

    public List<MailingListMappingConfig> getMailingListMappings() {
        return mailingListMappings;
    }

}
