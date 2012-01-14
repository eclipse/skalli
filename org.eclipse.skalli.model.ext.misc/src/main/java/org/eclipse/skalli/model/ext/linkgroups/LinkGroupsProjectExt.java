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
package org.eclipse.skalli.model.ext.linkgroups;

import java.util.Collection;

import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.PropertyName;

public class LinkGroupsProjectExt extends ExtensionEntityBase {

    public static final String MODEL_VERSION = "1.0"; //$NON-NLS-1$
    public static final String NAMESPACE = "http://www.eclipse.org/skalli/2010/Model/Extension-LinkGroups"; //$NON-NLS-1$

    @PropertyName(position = 0)
    public static final String PROPERTY_LINKGROUPS = "linkGroups"; //$NON-NLS-1$

    private OrderableGroup<LinkGroup> linkGroups = new OrderableGroup<LinkGroup>();

    public synchronized Collection<LinkGroup> getLinkGroups() {
        if (linkGroups == null) {
            linkGroups = new OrderableGroup<LinkGroup>();
        }
        return linkGroups.getItems();
    }

    public void setLinkGroups(Collection<LinkGroup> linkGroups) {
        this.linkGroups = new OrderableGroup<LinkGroup>(linkGroups);
    }

    public void addLinkGroup(LinkGroup linkGroup) {
        if (linkGroup != null) {
            getLinkGroups().add(linkGroup);
        }
    }

    public void removeLinkGroup(LinkGroup linkGroup) {
        if (linkGroup != null) {
            getLinkGroups().remove(linkGroup);
        }
    }

    public boolean hasLinkGroup(LinkGroup linkGroup) {
        return getLinkGroups().contains(linkGroup);
    }
}
