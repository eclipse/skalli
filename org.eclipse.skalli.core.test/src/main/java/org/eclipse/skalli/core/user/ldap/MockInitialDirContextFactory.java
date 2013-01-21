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

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.spi.InitialContextFactory;

import org.easymock.EasyMock;

public class MockInitialDirContextFactory implements InitialContextFactory {

    private static DirContext mockContext = null;

    public static DirContext getLatestMockContext() {
        return getOrCreateMockContext();
    }

    public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
        return getOrCreateMockContext();
    }

    private static DirContext getOrCreateMockContext() {
        synchronized (MockInitialDirContextFactory.class) {
            if (mockContext == null) {
                mockContext = (DirContext) EasyMock.createMock(DirContext.class);
            }
        }
        return mockContext;
    }

}
