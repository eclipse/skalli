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
package org.eclipse.skalli.core.rest.resources;

import java.io.IOException;

import org.eclipse.skalli.services.extension.ExtensionServiceBase;
import org.eclipse.skalli.services.extension.rest.RestConverter;
import org.eclipse.skalli.services.rest.RestWriter;
import org.eclipse.skalli.testutil.TestExtension;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

@SuppressWarnings("nls")
class TestExtensionService extends ExtensionServiceBase<TestExtension> {

    private static class TestRestConverter implements RestConverter<TestExtension> {

        @Override
        public void marshal(Object obj, RestWriter writer) throws IOException {
        }

        @Override
        public void marshal(Object obj, HierarchicalStreamWriter hierarchicalstreamwriter,
                MarshallingContext marshallingcontext) {
        }

        @Override
        public Object unmarshal(HierarchicalStreamReader hierarchicalstreamreader,
                UnmarshallingContext unmarshallingcontext) {
            return null;
        }

        @Override
        public boolean canConvert(@SuppressWarnings("rawtypes") Class class1) {
            return class1.equals(TestExtension.class);
        }

        @Override
        public String getAlias() {
            return null;
        }

        @Override
        public Class<TestExtension> getConversionClass() {
            return TestExtension.class;
        }

        @Override
        public String getApiVersion() {
            return "1.0";
        }

        @Override
        public String getNamespace() {
            return "tiffy";
        }

        @Override
        public String getXsdFileName() {
            return "tiffy.xsd";
        }
    }

    @Override
    public Class<TestExtension> getExtensionClass() {
        return TestExtension.class;
    }

    @Override
    public TestExtension newExtension() {
        return new TestExtension();
    }

    @Override
    public String getShortName() {
        return "testExtension";
    }

    @Deprecated
    @Override
    public RestConverter<TestExtension> getRestConverter(String host) {
        return new TestRestConverter();
    }

    @Override
    public RestConverter<TestExtension> getRestConverter() {
        return new TestRestConverter();
    }

    @Override
    public String getModelVersion() {
        return null;
    }

    @Override
    public String getNamespace() {
        return null;
    }

    @Override
    public String getXsdFileName() {
        return null;
    }

    @Override
    public String getCaption() {
        return "caption"; //$NON-NLS-1$
    }

    @Override
    public String getDescription() {
        return "description"; //$NON-NLS-1$
    }
};
