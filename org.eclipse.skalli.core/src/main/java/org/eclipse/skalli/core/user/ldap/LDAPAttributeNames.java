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
package org.eclipse.skalli.core.user.ldap;

@SuppressWarnings("nls")
public enum LDAPAttributeNames {

    BITS("userAccountControl"),
    EMAIL("mail"),
    USERID("sAMAccountName"),
    LASTNAME("sn"),
    FIRSTNAME("givenName"),
    TELEPHONE("telephoneNumber"),
    MOBILE("mobile"),
    ROOM("physicalDeliveryOfficeName"),
    LOCATION("l"),
    DEPARTMENT("department"),
    COMPANY("company"),
    SIP("msRTCSIP-PrimaryUserAddress");

    private final String ldapKey;

    private LDAPAttributeNames(String ldapKey) {
        this.ldapKey = ldapKey;
    }

    public String getLdapKey() {
        return ldapKey;
    }

    public static String[] getAll() {
        LDAPAttributeNames[] all = LDAPAttributeNames.values();
        String[] ret = new String[all.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = all[i].getLdapKey();
        }
        return ret;
    }

}
