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
import org.eclipse.skalli.commons.ComparatorUtils;

/**
 * This class represents a generic user.
 */
public class User extends EntityBase implements Comparable<User> {

    /** The version of this model entity ({@value}) */
    public static final String MODEL_VERSION = "1.0"; //$NON-NLS-1$

    /** The namespace of this model entity ({@value}) */
    public static final String NAMESPACE = "http://www.eclipse.org/skalli/2010/Model"; //$NON-NLS-1$

    /**
     * Constant representing first name, last name and email address of
     * an {@link #isUnknown() unknown} user.
     */
    public static final String UNKNOWN = "?"; //$NON-NLS-1$

    @PropertyName(position = 0)
    public static final String PROPERTY_USERID = "userId"; //$NON-NLS-1$

    @PropertyName(position = 1)
    public static final String PROPERTY_FIRSTNAME = "firstname"; //$NON-NLS-1$

    @PropertyName(position = 2)
    public static final String PROPERTY_LASTNAME = "lastname"; //$NON-NLS-1$

    @PropertyName(position = 3)
    public static final String PROPERTY_EMAIL = "email"; //$NON-NLS-1$

    @PropertyName
    public static final String PROPERTY_TELEPHONE = "telephone"; //$NON-NLS-1$

    @PropertyName
    public static final String PROPERTY_MOBILE = "mobile"; //$NON-NLS-1$

    @PropertyName
    public static final String PROPERTY_ROOM = "room"; //$NON-NLS-1$

    @PropertyName
    public static final String PROPERTY_LOCATION = "location"; //$NON-NLS-1$

    @PropertyName
    public static final String PROPERTY_DEPARTMENT = "department"; //$NON-NLS-1$

    @PropertyName
    public static final String PROPERTY_COMPANY = "company"; //$NON-NLS-1$

    @PropertyName
    public static final String PROPERTY_SIP = "sip"; //$NON-NLS-1$

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

    /**
     * Default constructor for unmarshaling users.
     * This constructor should not be called in application code.
     * <p>
     * Note, there is a difference between an {@link #isUnknown() unknown} user
     * and a user created with this constructor: the former has a
     * {@link #getUserId() user identifier}, the latter may have
     * a {@link #getUuid() UUID} (or nothing at all).
     */
    public User() {
    }

    /**
     * Creates an "unknown" user with only a unique identifier but no further
     * details like first name, last name or email. Calling {@link #isUnknown()}
     * on such a user will always return <code>true</code>.
     *
     * @param userId  the unique identifier of the user.
     */
    public User(String userId) {
        this(userId, UNKNOWN, UNKNOWN, UNKNOWN);
    }

    /**
     * Creates a user with given unique identifier, first name, last name
     * and email address.
     *
     * @param userId  the unique identifier of the user.
     * @param firstname  the first name of the user.
     * @param lastname  the last name of the user.
     * @param email  the email address of the user.
     */
    public User(String userId, String firstname, String lastname, String email) {
        this.userId = userId;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
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

    public boolean hasFirstname() {
        return StringUtils.isNotBlank(firstname) && !"?".equals(firstname); //$NON-NLS-1$
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public boolean hasLastname() {
        return StringUtils.isNotBlank(lastname) && !"?".equals(lastname); //$NON-NLS-1$
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean hasEmail() {
        return StringUtils.isNotBlank(email) && !"?".equals(email); //$NON-NLS-1$
    }

    /**
     * Returns the display name of the user.
     *
     * @return either the concatenation of {@link #getFirstname() first name} and {@link #getLastname() last name},
     * or the {@link {@link #getEmail()} email address}, or the {@link #getUserId() userId} of the user in that
     * sequence. For an {@link #isUnknown() unknown user} always the userId is returned. If not even a userId
     * is known, <tt>"Anonymous"</tt> is returned.
     */
    public String getDisplayName() {
        StringBuilder sb = new StringBuilder();
        if (hasFirstname()) {
            sb.append(firstname);
        }
        if (hasLastname()) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(lastname);
        }
        if (sb.length() == 0 && hasEmail()) {
            sb.append(email);
        }
        if (sb.length() == 0 && userId != null) {
            sb.append(userId);
        }
        if (sb.length() == 0) {
            sb.append("Anonymous");
        }
        return sb.toString();
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

    /**
     * Checks if this user is an "unknown" user.
     *
     * @return <code>true</code> if <em>all</em> first name, last name and email of
     * this user equal {@link #UNKNOWN}. If any of these parameters is specified,
     * this method will return <code>false</code>.
     */
    public boolean isUnknown() {
        return UNKNOWN.equals(firstname) && UNKNOWN.equals(lastname) && UNKNOWN.equals(email);
    }

    /**
     * Returns the {@link #getDisplayName() display name} of this user.
     */
    @Override
    public String toString() {
        return getDisplayName();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((userId == null) ? 0 : userId.hashCode());
        return result;
    }

    /**
     * Checks if this user equals a given user. Two users are treated
     * equal, if their {@link #getUserId() unique identifiers} are equal.
     */
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

    /**
     * Compares this user to a given user by last name, first name and
     * unique identifier (in this order).
     */
    @Override
    public int compareTo(User user) {
        int ret = ComparatorUtils.compare(lastname, user.lastname);
        if (ret == 0) {
            ret = ComparatorUtils.compare(firstname, user.firstname);
            if (ret == 0) {
                ret =  ComparatorUtils.compare(userId, user.userId);
            }
        }
        return ret;
    }
}
