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
package org.eclipse.skalli.services.entity;

import static org.easymock.EasyMock.*;

import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.Issuer;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.model.ValidationException;
import org.eclipse.skalli.services.persistence.PersistenceService;
import org.eclipse.skalli.services.validation.Validation;
import org.eclipse.skalli.services.validation.ValidationService;
import org.eclipse.skalli.testutil.AssertUtils;
import org.eclipse.skalli.testutil.BundleManager;
import org.eclipse.skalli.testutil.PropertyHelperUtils;
import org.eclipse.skalli.testutil.TestEntityBase1;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("nls")
public class EntityServiceBaseTest {

    private static final String USERID = "hugo";
    private static final Class<? extends Issuer> ISSUER = TestEntityServiceImpl.class;

    private TestEntityServiceImpl entityService;
    private Object[] mocks;
    private PersistenceService mockIPS;
    private ValidationService mockVS;

    private TestEntityBase1 validEntity;
    private TestEntityBase1 noUuidEntity;
    private TestEntityBase1 invalidEntity;
    private ArrayList<TestEntityBase1> entities;

    private class TestEntityServiceImpl extends EntityServiceBase<TestEntityBase1> implements Issuer {

        private ValidationService validationService;

        public TestEntityServiceImpl(PersistenceService persistenceService, ValidationService validationService) {
            bindPersistenceService(persistenceService);
            this.validationService = validationService;
        }

        @Override
        protected <S> S getService(Class<S> service, String name) {
            if ("ValidationService".equals(name)) {
                return (S)validationService;
            }
            return null;
        }

        @Override
        public Class<TestEntityBase1> getEntityClass() {
            return TestEntityBase1.class;
        }

        @Override
        public int getModelVersion() {
            return 0;
        }

        @Override
        protected void validateEntity(TestEntityBase1 entity) throws ValidationException {
            SortedSet<Issue> issues = validateEntity(entity, Severity.FATAL);
            if (issues.size() > 0) {
                throw new ValidationException("FAILED", issues);
            }
        }

        @Override
        protected SortedSet<Issue> validateEntity(TestEntityBase1 entity, Severity minSeverity) {
            TreeSet<Issue> issues = new TreeSet<Issue>();
            if (invalidEntity.equals(entity)) {
                issues.add(new Issue(Severity.FATAL, ISSUER, invalidEntity.getUuid()));
            }
            if (minSeverity.equals(Severity.WARNING)) {
                issues.add(new Issue(Severity.WARNING, ISSUER, entity.getUuid()));
            }
            return issues;
        }
    }

    @Before
    public void setup() throws Exception {
        BundleManager.startBundles();

        validEntity = new TestEntityBase1(PropertyHelperUtils.TEST_UUIDS[0]);
        noUuidEntity = new TestEntityBase1();
        invalidEntity = new TestEntityBase1(PropertyHelperUtils.TEST_UUIDS[1]);

        entities = new ArrayList<TestEntityBase1>();
        for (int i = 0; i < PropertyHelperUtils.TEST_UUIDS.length; ++i) {
            entities.add(new TestEntityBase1(PropertyHelperUtils.TEST_UUIDS[i]));
        }

        mockIPS = createNiceMock(PersistenceService.class);
        mockVS = createNiceMock(ValidationService.class);
        mocks = new Object[] { mockIPS, mockVS };

        entityService = new TestEntityServiceImpl(mockIPS, mockVS);
        Assert.assertNotNull(entityService.getPersistenceService());
        Assert.assertNotNull(entityService.getService(ValidationService.class, "ValidationService"));
    }

    @Test
    public void testGetters() throws Exception {
        reset(mocks);
        mockIPS.getEntity(eq(TestEntityBase1.class), eq(PropertyHelperUtils.TEST_UUIDS[0]));
        expectLastCall().andReturn(entities.get(0)).anyTimes();
        mockIPS.getEntities(eq(TestEntityBase1.class));
        expectLastCall().andReturn(entities).anyTimes();
        mockIPS.loadEntity(eq(TestEntityBase1.class), eq(PropertyHelperUtils.TEST_UUIDS[0]));
        expectLastCall().andReturn(entities.get(0)).anyTimes();
        replay(mocks);

        Assert.assertEquals(entities.get(0), entityService.getByUUID(PropertyHelperUtils.TEST_UUIDS[0]));
        AssertUtils.assertEquals("getAll()", entities, entityService.getAll());
        Assert.assertEquals(entities.get(0),
                entityService.loadEntity(TestEntityBase1.class, PropertyHelperUtils.TEST_UUIDS[0]));

        verify(mocks);
    }

    // persist

    @Test
    public void testPersist() throws Exception {
        reset(mocks);
        mockIPS.persist(eq(validEntity), eq(USERID));
        expectLastCall();
        mockIPS.persist(eq(noUuidEntity), eq(USERID));
        expectLastCall();
        mockVS.queue(eq(new Validation<TestEntityBase1>(TestEntityBase1.class, PropertyHelperUtils.TEST_UUIDS[0],
                Severity.INFO, USERID)));
        expectLastCall();
        mockVS.queue(isA(Validation.class)); // test for concrete Validation not possible,
                                             // because UUID for noUuidEntity is set in persist()!
        expectLastCall();
        replay(mocks);

        entityService.persist(validEntity, USERID);
        entityService.persist(noUuidEntity, USERID);
        Assert.assertNotNull(noUuidEntity.getUuid());

        verify(mocks);
    }

    @Test(expected = ValidationException.class)
    public void testPersist_invalidEntity() throws Exception {
        reset(mocks);
        mockIPS.persist(eq(validEntity), eq(USERID));
        expectLastCall();
        mockIPS.persist(eq(noUuidEntity), eq(USERID));
        expectLastCall();
        mockVS.queue(eq(new Validation<TestEntityBase1>(TestEntityBase1.class, PropertyHelperUtils.TEST_UUIDS[0],
                Severity.INFO, USERID)));
        expectLastCall();
        mockVS.queue(isA(Validation.class)); // test for concrete Validation not possible,
        // because UUID for noUuidEntity is set in persist()!
        expectLastCall();

        replay(mocks);

        entityService.persist(invalidEntity, USERID);
    }

    @Test
    public void testValidate() throws Exception {
        Assert.assertTrue(entityService.validate(validEntity, Severity.FATAL).isEmpty());

        TreeSet<Issue> expected = new TreeSet<Issue>();
        expected.add(new Issue(Severity.FATAL, ISSUER, invalidEntity.getUuid()));
        AssertUtils.assertEquals("validate(invalid)", expected, entityService.validate(invalidEntity, Severity.FATAL));

        expected.add(new Issue(Severity.WARNING, ISSUER, invalidEntity.getUuid()));
        AssertUtils.assertEquals("validate(invalid,WARNING)", expected,
                entityService.validate(invalidEntity, Severity.WARNING));
    }

    public void testValidateAll() throws Exception {
        reset(mocks);
        mockIPS.getEntities(eq(TestEntityBase1.class));
        expectLastCall().andReturn(entities).anyTimes();
        replay(mocks);

        TreeSet<Issue> expected = new TreeSet<Issue>();
        expected.add(new Issue(Severity.FATAL, ISSUER, invalidEntity.getUuid()));
        AssertUtils.assertEquals("validateAll()", expected, entityService.validateAll(Severity.FATAL));

        expected.add(new Issue(Severity.WARNING, ISSUER, invalidEntity.getUuid()));
        AssertUtils.assertEquals("validateAll(WARNING)", expected, entityService.validateAll(Severity.WARNING));
        verify(mocks);
    }

    private static Issue[] ISSUES = new Issue[] {
            new Issue(Severity.FATAL, ISSUER, PropertyHelperUtils.TEST_UUIDS[0]),
            new Issue(Severity.ERROR, ISSUER, PropertyHelperUtils.TEST_UUIDS[0]),
            new Issue(Severity.ERROR, ISSUER, PropertyHelperUtils.TEST_UUIDS[1]),
            new Issue(Severity.WARNING, ISSUER, PropertyHelperUtils.TEST_UUIDS[0]),
            new Issue(Severity.INFO, ISSUER, PropertyHelperUtils.TEST_UUIDS[0])
    };

    private static TreeSet<Issue> getIssues() {
        TreeSet<Issue> issues = new TreeSet<Issue>();
        issues.add(clone(ISSUES[0]));
        issues.add(clone(ISSUES[1]));
        issues.add(clone(ISSUES[2]));
        issues.add(clone(ISSUES[3]));
        issues.add(clone(ISSUES[4]));
        return issues;
    }

    private static Issue clone(Issue issue) {
        return new Issue(issue.getSeverity(), issue.getIssuer(), issue.getEntityId());
    }

    @Test
    public void testValidateIssues() {
        TestEntityServiceImpl service = new TestEntityServiceImpl(mockIPS, mockVS);

        TreeSet<Issue> issues = getIssues();
        assertTimestamps(issues, true);
        service.validateIssues(PropertyHelperUtils.TEST_UUIDS[0], Severity.FATAL, issues);
        Assert.assertTrue(issues.contains(ISSUES[0]));
        Assert.assertFalse(issues.contains(ISSUES[1]));
        Assert.assertFalse(issues.contains(ISSUES[2]));
        Assert.assertFalse(issues.contains(ISSUES[3]));
        Assert.assertFalse(issues.contains(ISSUES[4]));
        assertTimestamps(issues, false);

        issues = getIssues();
        assertTimestamps(issues, true);
        service.validateIssues(PropertyHelperUtils.TEST_UUIDS[0], Severity.ERROR, issues);
        Assert.assertTrue(issues.contains(ISSUES[0]));
        Assert.assertTrue(issues.contains(ISSUES[1]));
        Assert.assertFalse(issues.contains(ISSUES[2]));
        Assert.assertFalse(issues.contains(ISSUES[3]));
        Assert.assertFalse(issues.contains(ISSUES[4]));
        assertTimestamps(issues, false);

        issues = getIssues();
        assertTimestamps(issues, true);
        service.validateIssues(PropertyHelperUtils.TEST_UUIDS[0], Severity.WARNING, issues);
        Assert.assertTrue(issues.contains(ISSUES[0]));
        Assert.assertTrue(issues.contains(ISSUES[1]));
        Assert.assertFalse(issues.contains(ISSUES[2]));
        Assert.assertTrue(issues.contains(ISSUES[3]));
        Assert.assertFalse(issues.contains(ISSUES[4]));
        assertTimestamps(issues, false);
    }

    private void assertTimestamps(TreeSet<Issue> issues, boolean isInitial) {
        for (Issue issue : issues) {
            if (isInitial) {
                Assert.assertFalse(issue.getTimestamp() > 0);
            } else {
                Assert.assertTrue(issue.getTimestamp() > 0);
            }
        }
    }
}
