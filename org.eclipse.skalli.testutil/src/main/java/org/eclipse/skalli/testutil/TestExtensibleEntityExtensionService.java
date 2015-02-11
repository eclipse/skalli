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

public class TestExtensibleEntityExtensionService extends ExtensionServiceBase<TestExtensibleEntityBase> {

    @Override
    public Class<TestExtensibleEntityBase> getExtensionClass() {
        return TestExtensibleEntityBase.class;
    }

    @Override
    public TestExtensibleEntityBase newExtension() {
        return new TestExtensibleEntityBase();
    }

    @Override
    public String getShortName() {
        return "foobar"; //$NON-NLS-1$
    }

    @Override
    public String getCaption() {
        return "caption"; //$NON-NLS-1$
    }

    @Override
    public String getDescription() {
        return "description"; //$NON-NLS-1$
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
}
