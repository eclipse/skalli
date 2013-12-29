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
package org.eclipse.skalli.testutil;

import java.io.StringWriter;

import org.eclipse.skalli.services.extension.rest.RestConverter;
import org.junit.Assert;
import org.junit.Before;

/**
 * Base class for {@link RestConverter} tests.
 */
public class RestWriterTestBase {

    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"; //$NON-NLS-1$

    protected StringWriter writer;

    @Before
    public void setup() throws Exception {
        writer = new StringWriter();
    }

    protected void assertEqualsXML(String expected) throws Exception {
        Assert.assertEquals(XML_HEADER + expected, writer.toString());
    }

    protected void assertEqualsJSON(String expected) throws Exception {
        Assert.assertEquals(expected, writer.toString());
    }
}
