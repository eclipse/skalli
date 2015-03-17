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
package org.eclipse.skalli.model;

import org.apache.commons.lang.StringUtils;

/**
 * Model entity representing the unique identifier of a user.
 */
public class Member implements Comparable<Member> {

    @PropertyName(position = 0)
    public static final String PROPERTY_USERID = "userID"; //$NON-NLS-1$

    private String userID = ""; //$NON-NLS-1$

    public Member() {
    }

    public Member(String userID) {
        this.userID = StringUtils.lowerCase(userID);
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = StringUtils.lowerCase(userID);
    }

    @Override
    public String toString() {
        if (StringUtils.isNotBlank(userID)) {
            return userID;
        }
        return super.toString();
    }

    @Override
    public int hashCode() {
        return 31 + ((userID == null) ? 0 : userID.hashCode());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (getClass() != o.getClass()) {
            return false;
        }
        Member other = (Member) o;
        if (userID == null) {
            if (other.userID != null) {
                return false;
            }
        } else if (!userID.equals(other.userID)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Member o) {
        int result = 0;
        boolean thisDefined = userID != null;
        boolean otherDefined = o.userID != null;
        if (thisDefined) {
            result = otherDefined ? userID.compareTo(o.userID) : 1;
        } else {
            result = otherDefined ? -1 : 0;
        }
        return result;
    }
}
