/*******************************************************************************
 * Copyright (c) 2010 - 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.feed.db.entities;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Lob;

import org.eclipse.skalli.api.java.feeds.Content;

@Embeddable
public class ContentJPA implements Content {

    public static final int VALUE_LENGTH = 1000000;
    public static final int TYPE_LENGTH = EntryJPA.TYPE_LENGTH;

    public ContentJPA() {
    }

    public ContentJPA(String type, String value) {
        this.type = type;
        this.value = value;

    }

    @Column(length = TYPE_LENGTH)
    private String type;

    @Lob
    @Column(length = VALUE_LENGTH)
    private String value;

    @Override
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
