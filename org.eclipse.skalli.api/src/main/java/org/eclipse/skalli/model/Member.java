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

    public Member(String userId) {
        this.userID = userId;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
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
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((userID == null) ? 0 : userID.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!super.equals(o)) {
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
        } else if (!StringUtils.lowerCase(userID).equals(StringUtils.lowerCase(other.userID))) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Member o) {
        if (o instanceof Member && userID != null && ((Member) o).getUserID() != null) {
            return StringUtils.lowerCase(userID).compareTo(StringUtils.lowerCase(((Member) o).getUserID()));
        } else {
            return 0;
        }
    }
}
