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
package org.eclipse.skalli.model.ext.commons;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.PropertyName;

public class InfoExtension extends ExtensionEntityBase {

    public static final String MODEL_VERSION = "1.0"; //$NON-NLS-1$
    public static final String NAMESPACE = "http://www.eclipse.org/skalli/2010/Model/Extension-Info"; //$NON-NLS-1$

    @PropertyName(position = 0)
    public static final String PROPERTY_PAGE_URL = "pageUrl"; //$NON-NLS-1$

    @PropertyName(position = 1)
    public static final String PROPERTY_MAILING_LIST = "mailingLists"; //$NON-NLS-1$

    private String pageUrl = ""; //$NON-NLS-1$
    private LinkedHashSet<String> mailingLists = new LinkedHashSet<String>();

    public String getPageUrl() {
        return pageUrl;
    }

    public void setPageUrl(String pageUrl) {
        this.pageUrl = pageUrl;
    }

    public Set<String> getMailingLists() {
        if (mailingLists == null) {
            mailingLists = new LinkedHashSet<String>();
        }
        return mailingLists;
    }

    public void setMailingLists(Collection<String> mailingLists) {
        this.mailingLists = new LinkedHashSet<String>(mailingLists);
    }

    public void addMailingList(String mailingList) {
        if (mailingList != null) {
            Set<String> list = getMailingLists();
            if (!list.contains(mailingList)) {
                list.add(mailingList);
            }
        }
    }

    public void removeMailingList(String mailingList) {
        if (mailingList != null) {
            Set<String> list = getMailingLists();
            list.remove(mailingList);
        }
    }

    public boolean hasMailingList(String mailingList) {
        if (mailingList == null) {
            return false;
        }
        Set<String> list = getMailingLists();
        return list.contains(mailingList);
    }

}
