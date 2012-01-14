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
package org.eclipse.skalli.model.ext.devinf.internal;

import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.testutil.ValidatorUtils;
import org.junit.Test;

@SuppressWarnings("nls")
public class SCMValidatorTest {

    @Test
    public void test() {
        SCMValidator validator = new SCMValidator("SCM Location");
        ValidatorUtils.assertIsValid(validator, null);
        ValidatorUtils.assertIsValid(validator, "");

        // git
        ValidatorUtils.assertIsValid(validator, "scm:git:git://git.wdf.sap.corp/helloWorld.git");
        ValidatorUtils.assertIsValid(validator, "scm:git:git://git.wdf.sap.corp/NGP/LDI/ldi.hudson.jobgenerator.git");

        ValidatorUtils.assertNotValid(validator, "scm:git:git://git.wdf.sap.corp/helloWorld", Severity.ERROR);
        ValidatorUtils.assertNotValid(validator, "scm:git://git.wdf.sap.corp/helloWorld", Severity.ERROR);

        // perforce
        ValidatorUtils.assertIsValid(validator, "scm:perforce:perforce3227.wdf.sap.corp:3227://LDI/ldi.helloworld/");
        ValidatorUtils.assertIsValid(validator, "scm:perforce:perforce3227:3227://LDI/ldi.helloworld/");

        ValidatorUtils.assertNotValid(validator, "scm:perforce:perforce3227.wdf.sap.corp:3227://LDI/ldi.helloworld",
                Severity.ERROR);

        ValidatorUtils.assertNotValid(validator, "scm:perforce:perforce3227.wdf.sap.corp:3227:/LDI/ldi.helloworld/",
                Severity.ERROR);
        ValidatorUtils
                .assertNotValid(validator, "scm:perforce:perforce3227:3217://LDI/ldi.helloworld/", Severity.ERROR);
        ValidatorUtils.assertNotValid(validator, "scm:perforce:perforce3227.wdfxsap.corp:3227://LDI/ldi.helloworld/",
                Severity.ERROR);

        // others
        ValidatorUtils.assertNotValid(validator, "scn:git:git://git.wdf.sap.corp/helloWorld", Severity.FATAL);
        ValidatorUtils.assertNotValid(validator, "scm:hg:http://host/v3", Severity.FATAL);
    }
}
