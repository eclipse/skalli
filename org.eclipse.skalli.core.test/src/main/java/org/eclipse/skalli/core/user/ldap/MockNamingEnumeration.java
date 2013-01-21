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

import java.util.Iterator;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

public class MockNamingEnumeration<SearchResult> implements NamingEnumeration<Object> {

    private Iterator<SearchResult> iterator = null;

    public MockNamingEnumeration() {
        this(null);
    }

    public MockNamingEnumeration(List<SearchResult> results) {
        if (results != null) {
            iterator = results.iterator();
        }
    }

    public void close() throws NamingException {
    }

    public boolean hasMore() throws NamingException {
        return hasMoreElements();
    }

    public SearchResult next() throws NamingException {
        return nextElement();
    }

    public boolean hasMoreElements() {
        if (iterator == null)
            return false;
        return iterator.hasNext();
    }

    public SearchResult nextElement() {
        if (iterator == null)
            return null;
        return iterator.next();
    }
}
