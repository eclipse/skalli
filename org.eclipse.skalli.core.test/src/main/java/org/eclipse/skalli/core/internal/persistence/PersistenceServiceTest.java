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
package org.eclipse.skalli.core.internal.persistence;

import java.util.List;
import java.util.UUID;

import org.eclipse.skalli.api.java.EntityFilter;
import org.eclipse.skalli.model.ext.EntityBase;
import org.eclipse.skalli.model.ext.ExtensionEntityBase;
import org.eclipse.skalli.model.ext.ExtensionServiceBase;
import org.junit.Test;

// test for AbstractPersistenceService
public class PersistenceServiceTest {

    private static class TestAbstractPersistenceService extends AbstractPersistenceService {
        @Override
        public void persist(EntityBase objToPersist, String userId) {
        }

        @Override
        public <T extends EntityBase> T getEntity(Class<T> entityClass, UUID uuid) {
            return null;
        }

        @Override
        public <T extends EntityBase> List<T> getEntities(Class<T> entityClass) {
            return null;
        }

        @Override
        public <T extends EntityBase> T getEntity(Class<T> entityClass, EntityFilter<T> filter) {
            return null;
        }

        @Override
        public <T extends EntityBase> T getDeletedEntity(Class<T> entityClass, UUID uuid) {
            return null;
        }

        @Override
        public <T extends EntityBase> List<T> getDeletedEntities(Class<T> entityClass) {
            return null;
        }

        @Override
        public <T extends EntityBase> T loadEntity(Class<T> entityClass, UUID uuid) {
            return null;
        }

        @Override
        public <T extends EntityBase> void refresh(Class<T> entityClass) {
        }

        @Override
        public void refreshAll() {
        }

        @Override
        public <T extends EntityBase> int size(Class<T> entityClass) {
            return 0;
        }
    }

    private static class TestExtensionService extends ExtensionServiceBase<ExtensionEntityBase> {
        @Override
        public Class<ExtensionEntityBase> getExtensionClass() {
            return ExtensionEntityBase.class;
        }

        @Override
        public String getShortName() {
            return ":-)"; //$NON-NLS-1$
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

    private final TestExtensionService es1 = new TestExtensionService();
    private final TestExtensionService es2 = new TestExtensionService();

    @Test
    public void testBindExtensionService() {
        TestAbstractPersistenceService aps = new TestAbstractPersistenceService();
        aps.bindExtensionService(es1);
    }

    @Test(expected = RuntimeException.class)
    public void testBindExtensionService_twice() {
        TestAbstractPersistenceService aps = new TestAbstractPersistenceService();
        aps.bindExtensionService(es1);
        aps.bindExtensionService(es2);
    }

}
