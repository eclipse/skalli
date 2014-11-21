/*******************************************************************************
 * Copyright (c) 2010-2014 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.core.feed.jpa;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.eclipse.skalli.services.feed.Person;

@Embeddable
public class PersonJPA implements Person {

    public static final int NAME_LENGTH = 256;
    public static final int EMAIL_LENGTH = 256;
    public static final int USERID_LENGTH = 32;

    public PersonJPA() {
    }

    public PersonJPA(String name, String email) {
        super();
        this.name = name;
        this.email = email;
    }

    @Column(length = USERID_LENGTH)
    private String userId;

    @Column(length = NAME_LENGTH)
    private String name;

    @Column(length = EMAIL_LENGTH)
    private String email;


    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

}
