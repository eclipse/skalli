/*******************************************************************************
 * Copyright (c) 2010-2017 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.core.user.ldap;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamImplicit;

public class LDAPSubDNs {

    @XStreamImplicit(itemFieldName="subDN")
    private List<String> listSubDNs;

    // do not remove: required by xstream
    public LDAPSubDNs() {
    }

    public List<String> getList() {
        return listSubDNs;
    }

    public void setList(List<String> items) {
        this.listSubDNs = items;
    }

    public int size(){
        return this.listSubDNs.size();
    }

}
