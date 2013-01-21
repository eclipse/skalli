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
package org.eclipse.skalli.core.persistence;

import org.easymock.EasyMock;
import org.eclipse.skalli.core.persistence.NoopConverter;
import org.junit.Assert;
import org.junit.Test;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class NoopConverterTest {

    @Test
    public void testCanConvert() {
        NoopConverter nc = new NoopConverter();
        Assert.assertTrue(nc.canConvert(Noop.class));
        Assert.assertFalse(nc.canConvert(Object.class));
    }

    @Test
    public void testMarshal() {
        NoopConverter nc = new NoopConverter();
        HierarchicalStreamWriter mockWriter = EasyMock.createMock(HierarchicalStreamWriter.class);
        MarshallingContext mockContext = EasyMock.createMock(MarshallingContext.class);
        Object[] mocks = new Object[] { mockWriter, mockContext };
        EasyMock.reset(mocks);
        // expect no calls
        EasyMock.replay(mocks);
        nc.marshal(new Object(), mockWriter, mockContext);
        EasyMock.verify(mocks);
    }

    @Test
    public void testUnmarshal() {
        NoopConverter nc = new NoopConverter();
        HierarchicalStreamReader mockWriter = EasyMock.createMock(HierarchicalStreamReader.class);
        UnmarshallingContext mockContext = EasyMock.createMock(UnmarshallingContext.class);
        Object[] mocks = new Object[] { mockWriter, mockContext };
        EasyMock.reset(mocks);
        // expect no calls
        EasyMock.replay(mocks);
        Object res = nc.unmarshal(mockWriter, mockContext);
        Assert.assertNull(res);
        EasyMock.verify(mocks);
    }
}
