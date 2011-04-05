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

import org.junit.Test;

import org.eclipse.skalli.model.ext.Severity;
import org.eclipse.skalli.testutil.ValidatorUtils;

@SuppressWarnings("nls")
public class RelativePomPathValidatorTest {

  @Test
  public void test() {
    RelativePomPathValidator validator = new RelativePomPathValidator(Severity.FATAL, "Reactor POM");
    ValidatorUtils.assertIsValid(validator, null);
    ValidatorUtils.assertIsValid(validator, "");

    ValidatorUtils.assertIsValid(validator, "dev");
    ValidatorUtils.assertIsValid(validator, "dev/to/something");
    ValidatorUtils.assertIsValid(validator, "some.parent.project");

    ValidatorUtils.assertNotValid(validator, "pom.xml", Severity.FATAL);
    ValidatorUtils.assertNotValid(validator, "something/", Severity.FATAL);
    ValidatorUtils.assertNotValid(validator, "/path/to", Severity.FATAL);
    ValidatorUtils.assertNotValid(validator, "*?*", Severity.FATAL);
    ValidatorUtils.assertNotValid(validator, "PRN", Severity.FATAL);
    ValidatorUtils.assertNotValid(validator, "..", Severity.FATAL);
    ValidatorUtils.assertNotValid(validator, "../somehwere/else", Severity.FATAL);
    ValidatorUtils.assertNotValid(validator, "up/../down", Severity.FATAL);
    ValidatorUtils.assertNotValid(validator, "some/go/up/and/..", Severity.FATAL);
    ValidatorUtils.assertNotValid(validator, "go\\to\\windows", Severity.FATAL);
  }

}

