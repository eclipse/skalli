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
package org.eclipse.skalli.model.ext.maven.internal;

import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.model.ext.maven.MavenProjectExt;
import org.eclipse.skalli.testutil.ValidatorUtils;
import org.junit.Test;

@SuppressWarnings("nls")
public class MavenIdValidatorTest {

    @Test
    public void test() {
        MavenIdValidator validator = new MavenIdValidator(Severity.FATAL, MavenProjectExt.class,
                MavenProjectExt.PROPERTY_GROUPID, "Maven ID");
        ValidatorUtils.assertIsValid(validator, null);
        ValidatorUtils.assertIsValid(validator, "");

        ValidatorUtils.assertIsValid(validator, "com.sap.ldi");
        ValidatorUtils.assertIsValid(validator, "com.sap.ldi_something");
        ValidatorUtils.assertIsValid(validator, "com");

        ValidatorUtils.assertNotValid(validator, ".", Severity.FATAL);
        ValidatorUtils.assertNotValid(validator, "com.sap.", Severity.FATAL);
        ValidatorUtils.assertNotValid(validator, ".com.sap.", Severity.FATAL);
    }

}
