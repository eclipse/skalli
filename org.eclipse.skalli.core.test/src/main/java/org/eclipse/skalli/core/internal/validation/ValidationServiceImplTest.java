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
package org.eclipse.skalli.core.internal.validation;

import static org.easymock.EasyMock.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.SortedSet;
import java.util.UUID;

import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.core.internal.validation.ValidationServiceImpl.QueueAllRunnable;
import org.eclipse.skalli.core.internal.validation.ValidationServiceImpl.QueueRunnable;
import org.eclipse.skalli.core.internal.validation.ValidationServiceImpl.ValidateAllRunnable;
import org.eclipse.skalli.core.internal.validation.ValidationServiceImpl.ValidateRunnable;
import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.Issuer;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.services.configuration.ConfigurationService;
import org.eclipse.skalli.services.entity.EntityService;
import org.eclipse.skalli.services.issues.Issues;
import org.eclipse.skalli.services.issues.IssuesService;
import org.eclipse.skalli.services.scheduler.RunnableSchedule;
import org.eclipse.skalli.services.scheduler.Schedule;
import org.eclipse.skalli.services.scheduler.SchedulerService;
import org.eclipse.skalli.services.scheduler.Task;
import org.eclipse.skalli.services.validation.Validation;
import org.eclipse.skalli.testutil.AssertUtils;
import org.eclipse.skalli.testutil.BundleManager;
import org.eclipse.skalli.testutil.PropertyHelperUtils;
import org.eclipse.skalli.testutil.TestEntityBase1;
import org.eclipse.skalli.testutil.TestEntityBase2;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("nls")
public class ValidationServiceImplTest {

    private static final String USERID = "hugo";
    private static final String USERID1 = "foobar";

    private static final SortedSet<Issue> ISSUES1 = CollectionUtils.asSortedSet(
            new Issue(Severity.ERROR, EntityService1.class, PropertyHelperUtils.TEST_UUIDS[0]),
            new Issue(Severity.WARNING, EntityService1.class, PropertyHelperUtils.TEST_UUIDS[0]));

    private IssuesService mockISS;
    private EntityService1 mockES1;
    private EntityService2 mockES2;
    private ConfigurationService mockCS;
    private SchedulerService mockSS;
    private Object[] mocks;

    private ArrayList<TestEntityBase1> entities1;
    private ArrayList<Issues> issues1;
    private ArrayList<TestEntityBase2> entities2;
    private ArrayList<Issues> issues2;

    private ValidationConfig[] configs;
    private ValidationsConfig config;

    private static interface EntityService1 extends EntityService<TestEntityBase1>, Issuer {
    }

    private static interface EntityService2 extends EntityService<TestEntityBase2>, Issuer {
    }

    private static class TestValidationService extends ValidationServiceImpl {
        @SuppressWarnings("rawtypes")
        public TestValidationService(IssuesService issuesService, ConfigurationService configService,
                SchedulerService schedulerService, EntityService... entityServices) {
            // do not change the binding order!
            // it simulates the binding order in ValidationComponent.xml!
            bindConfigurationService(configService);
            bindIssuesService(issuesService);
            for (EntityService entityService : entityServices) {
                bindEntityService(entityService);
            }
            bindSchedulerService(schedulerService);
        }
    }

    private static class TestRunnableSchedule extends RunnableSchedule {
        protected TestRunnableSchedule(String daysOfWeek, String hours, String minutes) {
            super(new Schedule(daysOfWeek, hours, minutes), "Test");
        }

        @Override
        public Runnable getRunnable() {
            return new Runnable() {
                @Override
                public void run() {
                }
            };
        }
    }

    @Before
    public void setup() throws Exception {
        BundleManager.startBundles();

        entities1 = new ArrayList<TestEntityBase1>();
        issues1 = new ArrayList<Issues>();
        for (int i = 0; i < PropertyHelperUtils.TEST_UUIDS.length; ++i) {
            entities1.add(new TestEntityBase1(PropertyHelperUtils.TEST_UUIDS[i]));
            issues1.add(new Issues(PropertyHelperUtils.TEST_UUIDS[i]));
        }
        issues2 = new ArrayList<Issues>();
        entities2 = new ArrayList<TestEntityBase2>();
        for (int i = 0; i < PropertyHelperUtils.TEST_UUIDS.length; ++i) {
            entities2.add(new TestEntityBase2(PropertyHelperUtils.TEST_UUIDS[i]));
            issues2.add(new Issues(PropertyHelperUtils.TEST_UUIDS[i]));
        }

        configs = new ValidationConfig[4];
        ValidationConfig c = new ValidationConfig();
        c.setSchedule(new TestRunnableSchedule("*", "*/2", "0,30"));
        c.setAction(ValidationAction.QUEUE);
        c.setEntityType(TestEntityBase1.class.getName());
        c.setMinSeverity(Severity.ERROR);
        c.setUserId("homer");
        configs[0] = c;
        c = new ValidationConfig();
        c.setSchedule(new TestRunnableSchedule("Monday", "0", "0"));
        c.setAction(ValidationAction.QUEUE_ALL);
        c.setMinSeverity(Severity.INFO);
        c.setUserId("homer");
        configs[1] = c;
        c = new ValidationConfig();
        c.setSchedule(new TestRunnableSchedule("*", "*", "*"));
        c.setAction(ValidationAction.VALIDATE);
        c.setEntityType(TestEntityBase2.class.getSimpleName());
        c.setMinSeverity(Severity.ERROR);
        c.setUserId("homer");
        configs[2] = c;
        c = new ValidationConfig();
        c.setSchedule(new TestRunnableSchedule("*/2", "*", "*"));
        c.setAction(ValidationAction.VALIDATE_ALL);
        c.setMinSeverity(Severity.ERROR);
        c.setUserId("homer");
        configs[3] = c;
        config = new ValidationsConfig(Arrays.asList(configs));

        mockISS = createNiceMock(IssuesService.class);
        mockES1 = createNiceMock(EntityService1.class);
        mockES2 = createNiceMock(EntityService2.class);
        mockSS = createNiceMock(SchedulerService.class);
        mockCS = createNiceMock(ConfigurationService.class);

        mocks = new Object[] { mockISS, mockES1, mockES2, mockCS, mockSS };
    }

    private void initEntityServiceMocks() {
        mockES1.getEntityClass();
        expectLastCall().andReturn(TestEntityBase1.class).anyTimes();
        mockES2.getEntityClass();
        expectLastCall().andReturn(TestEntityBase2.class).anyTimes();
        mockES1.getAll();
        expectLastCall().andReturn(entities1).anyTimes();
        mockES2.getAll();
        expectLastCall().andReturn(entities2).anyTimes();
    }

    private void initQueueMocks() throws Exception {
        mockISS.getByUUID(eq(PropertyHelperUtils.TEST_UUIDS[0]));
        expectLastCall().andReturn(issues1.get(0)).anyTimes();
        mockISS.getByUUID(eq(PropertyHelperUtils.TEST_UUIDS[1]));
        expectLastCall().andReturn(issues1.get(1)).anyTimes();
        mockISS.getByUUID(eq(PropertyHelperUtils.TEST_UUIDS[2]));
        expectLastCall().andReturn(issues2.get(2)).anyTimes();
        mockISS.persist(eq(issues1.get(0)), eq(USERID));
        expectLastCall().times(1);
        mockISS.persist(eq(issues1.get(1)), eq(USERID));
        expectLastCall().times(1);
        mockISS.persist(eq(issues2.get(2)), eq(USERID1));
        expectLastCall().times(1);
    }

    private void initQueueAllMocks() throws Exception {
        for (int i = 0; i < PropertyHelperUtils.TEST_UUIDS.length; ++i) {
            mockISS.getByUUID(eq(PropertyHelperUtils.TEST_UUIDS[i]));
            expectLastCall().andReturn(issues1.get(i)).anyTimes();
            mockISS.persist(eq(issues1.get(i)), eq(USERID));
            expectLastCall().times(1);
        }
    }

    private void initValidateMocks() throws Exception {
        mockISS.getByUUID(eq(PropertyHelperUtils.TEST_UUIDS[0]));
        expectLastCall().andReturn(issues1.get(0)).anyTimes();
        mockISS.persist(eq(PropertyHelperUtils.TEST_UUIDS[0]), eq(ISSUES1), eq(USERID));
        expectLastCall().times(1);
        mockES1.getByUUID(eq(PropertyHelperUtils.TEST_UUIDS[0]));
        expectLastCall().andReturn(entities1.get(0)).times(1);
        mockES1.validate(eq(entities1.get(0)), eq(Severity.WARNING));
        expectLastCall().andReturn(ISSUES1).times(1);
    }

    private void initValidateAllMocks() throws Exception {
        for (int i = 0; i < PropertyHelperUtils.TEST_UUIDS.length; ++i) {
            mockISS.getByUUID(eq(PropertyHelperUtils.TEST_UUIDS[i]));
            expectLastCall().andReturn(issues1.get(i)).anyTimes();
            mockISS.persist(eq(PropertyHelperUtils.TEST_UUIDS[i]), eq(ISSUES1), eq(USERID));
            expectLastCall().times(1);
            mockES1.validate(eq(entities1.get(i)), eq(Severity.WARNING));
            expectLastCall().andReturn(ISSUES1).times(1);
        }
    }

    private void initConfigurationServiceMock() {
        mockCS.readCustomization(eq(ValidationsResource.MAPPINGS_KEY), eq(ValidationsConfig.class));
        expectLastCall().andReturn(config).anyTimes();
    }

    private void initSchedulerServiceMock() {
        mockSS.registerSchedule(eq((TestRunnableSchedule) configs[0].getSchedule()));
        expectLastCall().andReturn(PropertyHelperUtils.TEST_UUIDS[0]).anyTimes();
        mockSS.registerSchedule(eq((TestRunnableSchedule) configs[1].getSchedule()));
        expectLastCall().andReturn(PropertyHelperUtils.TEST_UUIDS[1]).anyTimes();
        mockSS.registerSchedule(eq((TestRunnableSchedule) configs[2].getSchedule()));
        expectLastCall().andReturn(PropertyHelperUtils.TEST_UUIDS[2]).anyTimes();
        mockSS.registerSchedule(eq((TestRunnableSchedule) configs[3].getSchedule()));
        expectLastCall().andReturn(PropertyHelperUtils.TEST_UUIDS[3]).anyTimes();

        mockSS.unregisterSchedule(eq(PropertyHelperUtils.TEST_UUIDS[0]));
        expectLastCall().andReturn((TestRunnableSchedule) configs[0].getSchedule()).anyTimes();
        mockSS.unregisterSchedule(eq(PropertyHelperUtils.TEST_UUIDS[1]));
        expectLastCall().andReturn((TestRunnableSchedule) configs[1].getSchedule()).anyTimes();
        mockSS.unregisterSchedule(eq(PropertyHelperUtils.TEST_UUIDS[2]));
        expectLastCall().andReturn((TestRunnableSchedule) configs[2].getSchedule()).anyTimes();
        mockSS.unregisterSchedule(eq(PropertyHelperUtils.TEST_UUIDS[3]));
        expectLastCall().andReturn((TestRunnableSchedule) configs[3].getSchedule()).anyTimes();

        mockSS.registerTask(isA(Task.class));
        expectLastCall().andReturn(PropertyHelperUtils.TEST_UUIDS[3]).anyTimes();
    }

    @Test
    public void testQueue() throws Exception {
        reset(mocks);
        initEntityServiceMocks();
        initQueueMocks();
        replay(mocks);

        TestValidationService validationService = new TestValidationService(mockISS, mockCS, mockSS, mockES1, mockES2);
        Validation<TestEntityBase1> validation1 = new Validation<TestEntityBase1>(
                TestEntityBase1.class, PropertyHelperUtils.TEST_UUIDS[0], Severity.WARNING, USERID);
        Validation<TestEntityBase2> validation2 = new Validation<TestEntityBase2>(
                TestEntityBase2.class, PropertyHelperUtils.TEST_UUIDS[2], Severity.INFO, USERID1);
        Validation<TestEntityBase1> validation3 = new Validation<TestEntityBase1>(
                TestEntityBase1.class, PropertyHelperUtils.TEST_UUIDS[1], Severity.ERROR, USERID);
        Validation<TestEntityBase1> validation4 = new Validation<TestEntityBase1>(
                TestEntityBase1.class, PropertyHelperUtils.TEST_UUIDS[0], Severity.INFO, USERID1); // same as but with different severity/userId

        validationService.queue(validation1);
        validationService.queue(validation2);
        validationService.queue(validation3);
        validationService.queue(validation4);

        Assert.assertTrue(validationService.isQueued(entities1.get(0)));
        Assert.assertTrue(validationService.isQueued(entities1.get(1)));
        Assert.assertTrue(validationService.isQueued(entities2.get(2)));

        Assert.assertEquals(validation2, validationService.pollNextQueueEntry());
        Assert.assertEquals(validation3, validationService.pollNextQueueEntry());
        Assert.assertEquals(validation1, validationService.pollNextQueueEntry());
        Assert.assertNull(validationService.pollNextQueueEntry());
        verify(mocks);
    }

    @Test
    public void testQueueAll() throws Exception {
        reset(mocks);
        initEntityServiceMocks();
        initQueueAllMocks();
        replay(mocks);

        TestValidationService validationService = new TestValidationService(mockISS, mockCS, mockSS, mockES1, mockES2);
        validationService.queueAll(TestEntityBase1.class, Severity.ERROR, USERID);

        for (int i = 0; i < PropertyHelperUtils.TEST_UUIDS.length; ++i) {
            Assert.assertTrue(validationService.isQueued(entities1.get(i)));
        }

        ArrayList<Validation<? extends EntityBase>> queued = new ArrayList<Validation<? extends EntityBase>>();
        ArrayList<Validation<? extends EntityBase>> expected = new ArrayList<Validation<? extends EntityBase>>();
        for (int i = 0; i < PropertyHelperUtils.TEST_UUIDS.length; ++i) {
            queued.add(validationService.pollNextQueueEntry());
            expected.add(new Validation<TestEntityBase1>(
                    TestEntityBase1.class, PropertyHelperUtils.TEST_UUIDS[i], Severity.ERROR, USERID));
        }
        Assert.assertNull(validationService.pollNextQueueEntry());
        AssertUtils.assertEqualsAnyOrder("all", expected, queued);

        verify(mocks);
    }

    @Test
    public void testValidate() throws Exception {
        reset(mocks);
        initEntityServiceMocks();
        initValidateMocks();
        replay(mocks);

        TestValidationService validationService = new TestValidationService(mockISS, mockCS, mockSS, mockES1, mockES2);
        validationService.validate(TestEntityBase1.class, PropertyHelperUtils.TEST_UUIDS[0], Severity.WARNING, USERID);

        verify(mocks);
    }

    @Test
    public void testValidateAll() throws Exception {
        reset(mocks);
        initEntityServiceMocks();
        initValidateAllMocks();
        replay(mocks);

        TestValidationService validationService = new TestValidationService(mockISS, mockCS, mockSS, mockES1, mockES2);
        validationService.validateAll(TestEntityBase1.class, Severity.WARNING, USERID);

        verify(mocks);
    }

    @Test
    public void testStartValidationTasks() {
        reset(mocks);
        initEntityServiceMocks();
        initConfigurationServiceMock();
        initSchedulerServiceMock();
        replay(mocks);

        TestValidationService validationService = new TestValidationService(mockISS, mockCS, mockSS, mockES1, mockES2);
        Assert.assertEquals(configs.length, validationService.getRegisteredSchedules().size());
        Assert.assertEquals(PropertyHelperUtils.TEST_UUIDS[3], validationService.getTaskIdQueueValidator());
        validationService.stopAllTasks();
        Assert.assertEquals(0, validationService.getRegisteredSchedules().size());
        Assert.assertNull(validationService.getTaskIdQueueValidator());

        verify(mocks);
    }

    @Test
    public void testBindUnbindConfigService() {
        reset(mocks);
        initEntityServiceMocks();
        initConfigurationServiceMock();
        initSchedulerServiceMock();
        replay(mocks);

        TestValidationService validationService = new TestValidationService(mockISS, mockCS, mockSS, mockES1, mockES2);
        Assert.assertEquals(configs.length, validationService.getRegisteredSchedules().size());

        validationService.unbindConfigurationService(mockCS);
        Assert.assertEquals(0, validationService.getRegisteredSchedules().size()); // default nightly schedule!
        validationService.bindConfigurationService(mockCS);
        Assert.assertEquals(configs.length, validationService.getRegisteredSchedules().size());

        verify(mocks);
    }

    @Test
    public void testBindUnbindSchedulerService() {
        reset(mocks);
        initEntityServiceMocks();
        initConfigurationServiceMock();
        initSchedulerServiceMock();
        replay(mocks);

        TestValidationService validationService = new TestValidationService(mockISS, mockCS, mockSS, mockES1, mockES2);
        Assert.assertEquals(configs.length, validationService.getRegisteredSchedules().size());

        validationService.unbindSchedulerService(mockSS);
        Assert.assertEquals(0, validationService.getRegisteredSchedules().size());
        validationService.bindSchedulerService(mockSS);
        Assert.assertEquals(configs.length, validationService.getRegisteredSchedules().size());

        verify(mocks);
    }

    @Test
    public void testGetRunnableFromConfig() throws Exception {
        reset(mocks);
        initEntityServiceMocks();
        replay(mocks);

        TestValidationService validationService = new TestValidationService(mockISS, mockCS, mockSS, mockES1, mockES2);

        Runnable runnable = validationService.getRunnableFromConfig(configs[0]);
        Assert.assertEquals(QueueRunnable.class, runnable.getClass());
        runnable.run();
        assertQueueEntries(validationService, 0, TestEntityBase1.class, null);

        runnable = validationService.getRunnableFromConfig(configs[1]);
        Assert.assertEquals(QueueAllRunnable.class, runnable.getClass());
        runnable.run();
        assertQueueEntries(validationService, 1, TestEntityBase1.class, TestEntityBase2.class);

        runnable = validationService.getRunnableFromConfig(configs[2]);
        Assert.assertEquals(ValidateRunnable.class, runnable.getClass());

        runnable = validationService.getRunnableFromConfig(configs[3]);
        Assert.assertEquals(ValidateAllRunnable.class, runnable.getClass());

        verify(mocks);
    }

    private void assertQueueEntries(TestValidationService validationService, int config,
            Class<? extends EntityBase> entityClass1, Class<? extends EntityBase> entityClass2) {
        Set<UUID> uuids1 = CollectionUtils.asSet(PropertyHelperUtils.TEST_UUIDS);
        Set<UUID> uuids2 = CollectionUtils.asSet(PropertyHelperUtils.TEST_UUIDS);
        Validation<? extends EntityBase> next = validationService.pollNextQueueEntry();
        while (next != null) {
            Assert.assertNotNull(next.getEntityClass());
            if (next.getEntityClass().equals(entityClass1)) {
                Assert.assertTrue(uuids1.remove(next.getEntityId()));
            } else if (next.getEntityClass().equals(entityClass2)) {
                Assert.assertTrue(uuids2.remove(next.getEntityId()));
            }
            Assert.assertEquals(configs[config].getMinSeverity(), next.getMinSeverity());
            Assert.assertEquals(configs[config].getUserId(), next.getUserId());
            next = validationService.pollNextQueueEntry();
        }
        if (entityClass1 != null) {
            Assert.assertTrue(uuids1.isEmpty());
        } else {
            Assert.assertEquals(uuids1.size(), PropertyHelperUtils.TEST_UUIDS.length);
        }
        if (entityClass2 != null) {
            Assert.assertTrue(uuids2.isEmpty());
        } else {
            Assert.assertEquals(uuids2.size(), PropertyHelperUtils.TEST_UUIDS.length);
        }
    }
}
