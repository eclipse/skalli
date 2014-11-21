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
package org.eclipse.skalli.model.ext.commons;

import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.Member;
import org.eclipse.skalli.model.PropertyName;

public class PeopleExtension extends ExtensionEntityBase {

    public static final String MODEL_VERSION = "1.0"; //$NON-NLS-1$
    public static final String NAMESPACE = "http://www.eclipse.org/skalli/2010/Model/Extension-People"; //$NON-NLS-1$

    @PropertyName(position = 0)
    public static final String PROPERTY_LEADS = "leads"; //$NON-NLS-1$

    @PropertyName(position = 1)
    public static final String PROPERTY_MEMBERS = "members"; //$NON-NLS-1$

    private TreeSet<Member> leads = new TreeSet<Member>();
    private TreeSet<Member> members = new TreeSet<Member>();

    public synchronized SortedSet<Member> getMembers() {
        if (members == null) {
            members = new TreeSet<Member>();
        }
        return members;
    }

    public void addMember(Member member) {
        if (member != null) {
            getMembers().add(member);
        }
    }

    public void removeMember(Member member) {
        if (member != null) {
            getMembers().remove(member);
        }
    }

    public boolean hasMember(Member member) {
        return getMembers().contains(member);
    }

    public synchronized SortedSet<Member> getLeads() {
        if (leads == null) {
            leads = new TreeSet<Member>();
        }
        return leads;
    }

    public void addLead(Member lead) {
        if (lead != null) {
            getLeads().add(lead);
        }
    }

    public void removeLead(Member lead) {
        if (lead != null) {
            getLeads().remove(lead);
        }
    }

    public boolean hasLead(Member lead) {
        return getLeads().contains(lead);
    }
}
