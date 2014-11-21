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
package org.eclipse.skalli.testutil;

import org.eclipse.skalli.services.extension.ExtensionServiceBase;

@SuppressWarnings("nls")
public class TestExtensionService extends ExtensionServiceBase<TestExtension> {

    @Override
    public Class<TestExtension> getExtensionClass() {
        return TestExtension.class;
    }

    @Override
    public String getModelVersion() {
        return "0.8.15";
    }

    @Override
    public String getNamespace() {
        return "namespace";
    }

    @Override
    public String getXsdFileName() {
        return "schema.xsd";
    }

    @Override
    public String getShortName() {
        return "testext";
    }

    @Override
    public String getCaption() {
        return "TestExtension";
    }

    @Override
    public String getDescription() {
        return "TestExtension Description";
    }
}
