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
package org.eclipse.skalli.services.search;

public class IndexEntry {

    public enum Indexed {
        TOKENIZED, UN_TOKENIZED, NO;
    }

    public enum Stored {
        YES, NO;
    }

    private final String fieldName;
    private final String value;
    private final Indexed indexed;
    private final Stored stored;

    public IndexEntry(String fieldName, String value, Stored stored, Indexed indexed) {
        super();
        this.fieldName = fieldName;
        this.value = value;
        this.indexed = indexed;
        this.stored = stored;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getValue() {
        return value;
    }

    public Indexed getIndexed() {
        return indexed;
    }

    public Stored getStored() {
        return stored;
    }

}
