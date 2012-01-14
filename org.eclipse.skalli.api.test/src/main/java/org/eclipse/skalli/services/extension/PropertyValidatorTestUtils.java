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
