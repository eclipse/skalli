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
package org.eclipse.skalli.model;

import org.eclipse.skalli.commons.ComparatorUtils;

/**
 * This class represents a generic user.
 */
public class User extends EntityBase {

    public static final String MODEL_VERSION = "1.0"; //$NON-NLS-1$
    public static final String NAMESPACE = "http://www.eclipse.org/skalli/2010/Model"; //$NON-NLS-1$
    private static final String UNKNOWN = "?"; //$NON-NLS-1$

    @PropertyName(position = 0)
    public static final String PROPERTY_USERID = "userId"; //$NON-NLS-1$

    @PropertyName(position = 1)
    public static final String PROPERTY_FIRSTNAME = "firstname"; //$NON-NLS-1$

    @PropertyName(position = 2)
    public static final String PROPERTY_LASTNAME = "lastname"; //$NON-NLS-1$

    @PropertyName(position = 3)
    public static final String PROPERTY_EMAIL = "email"; //$NON-NLS-1$

    @Derived
    @PropertyName
    public static final String PROPERTY_FULL_NAME = "fullName"; //$NON-NLS-1$

    @Derived
    @PropertyName
    public static final String PROPERTY_DISPLAY_NAME = "displayName"; //$NON-NLS-1$

    private String userId;
    private String firstname;
    private String lastname;
    private String email;
    private String telephone;
    private String mobile;
    private String room;
    private String location;
    private String department;
    private String company;
    private String sip;
    private boolean detailsMissing;

    public User() {
    }

    private User(String userId) {
        this(userId, UNKNOWN, UNKNOWN, UNKNOWN);
        this.detailsMissing = true;
    }

    public User(String userId, String firstname, String lastname, String email) {
        this.userId = userId;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.detailsMissing = false;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        if (detailsMissing) {
            return getUserId();
        } else {
            return getFirstname() + " " + getLastname(); //$NON-NLS-1$
        }
    }

    public String getDisplayName() {
        if (detailsMissing) {
            return getUserId();
        }
        return getFullName() + " (" + userId + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getSip() {
        return sip;
    }

    public void setSip(String sip) {
        this.sip = sip;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    public static User createUserWithoutDetails(String userId) {
        return new User(userId);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((userId == null) ? 0 : userId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        User other = (User) obj;
        if (userId == null) {
            if (other.userId != null) {
                return false;
            }
        } else if (!userId.equals(other.userId)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Object o) {
        int ret = 0;
        if (o instanceof User) {
            User u = (User) o;
            ret = ComparatorUtils.compare(lastname, u.lastname);
            if (ret == 0) {
                ret = ComparatorUtils.compare(firstname, u.firstname);
                if (ret == 0) {
                    ret =  ComparatorUtils.compare(userId, u.userId);
                }
            }
        }
        if (ret == 0) {
            ret = super.compareTo(o);
        }
        return ret;
    }
}
