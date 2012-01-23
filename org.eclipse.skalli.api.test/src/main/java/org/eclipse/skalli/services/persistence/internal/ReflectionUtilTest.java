package org.eclipse.skalli.services.persistence.internal;

import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;

import java.util.Set;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.skalli.services.persistence.internal.ReflectionUtil;
import org.junit.Test;

public class ReflectionUtilTest {

    @Test
    public void testGetAllPersistencePropertyies() {
        Set<String> allFieldValues = ReflectionUtil.getPublicStaticFinalFieldValues(PersistenceUnitProperties.class);

        //check that the most relevant properties for PersistenceUnitProperties have not disappeard
        assertThat(allFieldValues, hasItems(PersistenceUnitProperties.JDBC_DRIVER,
                PersistenceUnitProperties.JDBC_URL,
                PersistenceUnitProperties.JDBC_USER,
                PersistenceUnitProperties.JDBC_PASSWORD,
                PersistenceUnitProperties.TARGET_DATABASE));
    }
}
