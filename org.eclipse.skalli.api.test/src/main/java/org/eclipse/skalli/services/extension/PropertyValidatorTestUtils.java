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
package org.eclipse.skalli.services.extension;

import org.junit.Assert;

public class PropertyValidatorTestUtils {

    public static void assertIsDefaultMessage(PropertyValidatorBase validator, String msg) {
        Assert.assertEquals(validator.getDefaultUndefinedMessage(), msg);
    }

    public static void assertIsUndefinedMessageFromCaption(PropertyValidatorBase validator, String msg) {
        Assert.assertEquals(validator.getUndefinedMessageFromCaption(), msg);
    }

}
