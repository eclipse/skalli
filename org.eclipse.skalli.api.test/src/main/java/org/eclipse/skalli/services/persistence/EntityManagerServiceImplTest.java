package org.eclipse.skalli.services.persistence;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.metamodel.Metamodel;

import org.apache.commons.lang.NotImplementedException;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.jpa.EntityManagerFactoryBuilder;

@SuppressWarnings("nls")
public class EntityManagerServiceImplTest {

    private static final String TEST_JPA_UNIT_NAME = "EntityManagerServiceImplTestJpaUnitName";
    private static final Map<Object,Object> TEST_JPA_SERVICE_PROPS =
            Collections.singletonMap((Object)"osgi.unit.name", (Object)TEST_JPA_UNIT_NAME);

    private static final String TEST_DRIVER = "MyDriver";
    private static final String TEST_URL = "MyUrl";
    private static final String TEST_USER = "MyUser";
    private static final String TEST_PWD = "MyPWD";
    private static final String TEST_TARGET_DB = "MyTargetDB";

    private class EntityManagerMock implements EntityManager {
        private Map<String, Object> properties;

        public EntityManagerMock(Map<String, Object> properties) {
            this.properties = properties;
        }

        @Override
        public void clear() {
        }

        @Override
        public void close() {
        }

        @Override
        public boolean contains(Object arg0) {
            return false;
        }

        @Override
        public Query createNamedQuery(String arg0) {
            throw new NotImplementedException();
        }

        @Override
        public <T> TypedQuery<T> createNamedQuery(String arg0, Class<T> arg1) {
            throw new NotImplementedException();
        }

        @Override
        public Query createNativeQuery(String arg0) {
            throw new NotImplementedException();
        }

        @Override
        public Query createNativeQuery(String arg0, Class arg1) {
            throw new NotImplementedException();
        }

        @Override
        public Query createNativeQuery(String arg0, String arg1) {
            throw new NotImplementedException();
        }

        @Override
        public Query createQuery(String arg0) {
            throw new NotImplementedException();
        }

        @Override
        public <T> TypedQuery<T> createQuery(CriteriaQuery<T> arg0) {
            throw new NotImplementedException();
        }

        @Override
        public <T> TypedQuery<T> createQuery(String arg0, Class<T> arg1) {
            throw new NotImplementedException();
        }

        @Override
        public void detach(Object arg0) {
        }

        @Override
        public <T> T find(Class<T> arg0, Object arg1) {
            throw new NotImplementedException();
        }

        @Override
        public <T> T find(Class<T> arg0, Object arg1, Map<String, Object> arg2) {
            throw new NotImplementedException();
        }

        @Override
        public <T> T find(Class<T> arg0, Object arg1, LockModeType arg2) {
            throw new NotImplementedException();
        }

        @Override
        public <T> T find(Class<T> arg0, Object arg1, LockModeType arg2, Map<String, Object> arg3) {
            throw new NotImplementedException();
        }

        @Override
        public void flush() {
        }

        @Override
        public CriteriaBuilder getCriteriaBuilder() {
            throw new NotImplementedException();
        }

        @Override
        public Object getDelegate() {
            throw new NotImplementedException();
        }

        @Override
        public EntityManagerFactory getEntityManagerFactory() {
            throw new NotImplementedException();
        }

        @Override
        public FlushModeType getFlushMode() {
            throw new NotImplementedException();
        }

        @Override
        public LockModeType getLockMode(Object arg0) {
            throw new NotImplementedException();
        }

        @Override
        public Metamodel getMetamodel() {
            throw new NotImplementedException();
        }

        @Override
        public Map<String, Object> getProperties() {
            return this.properties;
        }

        @Override
        public <T> T getReference(Class<T> arg0, Object arg1) {
            throw new NotImplementedException();
        }

        @Override
        public EntityTransaction getTransaction() {
            throw new NotImplementedException();
        }

        @Override
        public boolean isOpen() {
            return false;
        }

        @Override
        public void joinTransaction() {
        }

        @Override
        public void lock(Object arg0, LockModeType arg1) {
        }

        @Override
        public void lock(Object arg0, LockModeType arg1, Map<String, Object> arg2) {
        }

        @Override
        public <T> T merge(T arg0) {
            throw new NotImplementedException();
        }

        @Override
        public void persist(Object arg0) {
        }

        @Override
        public void refresh(Object arg0) {
        }

        @Override
        public void refresh(Object arg0, Map<String, Object> arg1) {
        }

        @Override
        public void refresh(Object arg0, LockModeType arg1) {
        }

        @Override
        public void refresh(Object arg0, LockModeType arg1, Map<String, Object> arg2) {
        }

        @Override
        public void remove(Object arg0) {
        }

        @Override
        public void setFlushMode(FlushModeType arg0) {
        }

        @Override
        public void setProperty(String arg0, Object arg1) {
        }

        @Override
        public <T> T unwrap(Class<T> arg0) {
            return null;
        }

    }

    private class EntityManagerFactoryMock implements EntityManagerFactory {

        private Map<String, Object> props;

        public EntityManagerFactoryMock(Map props) {
            this.props = props;
        }

        @Override
        public boolean isOpen() {
            return false;
        }

        @Override
        public Map<String, Object> getProperties() {
            return props;
        }

        @Override
        public PersistenceUnitUtil getPersistenceUnitUtil() {
            return null;
        }

        @Override
        public Metamodel getMetamodel() {
            return null;
        }

        @Override
        public CriteriaBuilder getCriteriaBuilder() {
            return null;
        }

        @Override
        public Cache getCache() {
            return null;
        }

        @SuppressWarnings("unchecked")
        @Override
        public EntityManager createEntityManager(Map props) {
            this.props = props;
            return new EntityManagerMock(props);
        }

        @Override
        public EntityManager createEntityManager() {
            return new EntityManagerMock(props);
        }

        @Override
        public void close() {

        }
    }

    private class EntityManagerFactoryBuilderMock implements EntityManagerFactoryBuilder {
        private EntityManagerFactory emfMock;

        @Override
        public EntityManagerFactory createEntityManagerFactory(Map props) {
            this.emfMock = new EntityManagerFactoryMock(props);
            return emfMock;
        }
    }

    @Test
    public void testGetEntityManagerReturnsConfiguredProps() throws StorageException {
        Map<String, Object> expectedProps = getDefaultPersistenceProperties();
        Map<String, Object> aktualProps = getPropertiesOfGetEntityManagerCall(expectedProps);
        assertPersistenceProperties(aktualProps);
    }

    @Test
    public void testGetEntityManagerMissingConfigProperties() throws StorageException {
        for (String property: EntityManagerServiceBase.REQUIRED_PROPERTIES) {
            Map<String, Object> expectedProps = getDefaultPersistenceProperties();
            expectedProps.remove(property);
            try {
                getPropertiesOfGetEntityManagerCall(expectedProps);
                fail("StorageException expected due to missing property " + property);
            } catch (StorageException e) {
                // expected
            }
        }
    }

    @Test
    public void testGetEntityManagerFromInjectedFactory() throws StorageException {
        Hashtable<String, String> serviceProps = new Hashtable<String, String>();
        serviceProps.put("osgi.unit.name", TEST_JPA_UNIT_NAME);

        BundleContext context = FrameworkUtil.getBundle(EntityManagerServiceImplTest.class).getBundleContext();
        ServiceRegistration<EntityManagerFactory> registration = null;
        try {
            registration = context.registerService(EntityManagerFactory.class, new EntityManagerFactoryMock(
                    getDefaultPersistenceProperties()), serviceProps);

            Map<String, Object> aktualProps = getPropertiesOfGetEntityManagerCall(null);
            assertPersistenceProperties(aktualProps);
        } finally {
            registration.unregister();
        }
    }

    @Test
    public void testGetEntityManagerLazyFactoryAppearsWithinTimeout() throws Exception {
        // factory appears with delay of 250 milliseconds, client is willing to wait for 1 second => success
        assertLazyFactory(1000, 250, 1000);
    }

    @Test(expected=StorageException.class)
    public void testGetEntityManagerLazyFactoryAppearsTooLate() throws Exception {
        // factory appears with delay of 250 millisecond, client is willing to wait for 100 millisecond only => failure
        assertLazyFactory(100, 250, 1000);
    }

    @Test(expected=StorageException.class)
    public void testGetEntityManagerNoWait() throws Exception {
        // factory appears with delay of 10 milliseconds only, client is not willing to wait at all => failure
        assertLazyFactory(-1, 10, 1000);
    }

    private void assertLazyFactory(long timeout, long initialDelay, long awaitDone) throws Exception {
        CountDownLatch latchStart = new CountDownLatch(1);
        CountDownLatch latchDone = new CountDownLatch(1);

        // thread that registers the entity manager factory: waits on latchStart, then registers the service with
        // a delay of initialDelay millisecond, then waits on latchDone for max. awaitDone milliseconds;
        // asserts that latchDone has been triggered before the timeout elapsed; then it terminates
        RegisterRunnable registerRunnable = new RegisterRunnable(latchStart, latchDone, initialDelay, awaitDone);
        Thread registerThread = new Thread(registerRunnable);

        // thread that waits on a entity manager factory to appear within the timeout milliseconds;
        // if a factory appears, asserts that the entity manager has the expected
        // properties; then it triggers latchDone before terminating
        ClientRunnable clientRunnable = new ClientRunnable(latchStart, latchDone, timeout);
        Thread clientThread = new Thread(clientRunnable);

        registerThread.start();
        clientThread.start();

        registerThread.join();
        clientThread.join();

        if (registerRunnable.failure != null) {
            throw registerRunnable.failure;
        }
        if (clientRunnable.failure != null) {
            throw clientRunnable.failure;
        }
        assertTrue(registerRunnable.done);
        assertTrue(clientRunnable.done);
    }

    @Test
    public void testGetAllPersistenceProperties() {
        Set<String> allFieldValues = EntityManagerServiceBase.getPublicStaticFinalFieldValues(PersistenceUnitProperties.class);

        //check that the most relevant properties of PersistenceUnitProperties have not disappeared
        assertThat(allFieldValues, hasItems(PersistenceUnitProperties.JDBC_DRIVER,
                PersistenceUnitProperties.JDBC_URL,
                PersistenceUnitProperties.JDBC_USER,
                PersistenceUnitProperties.JDBC_PASSWORD,
                PersistenceUnitProperties.TARGET_DATABASE));
    }

    private static Map<String, Object> getDefaultPersistenceProperties() {
        Map<String, Object> properties = new HashMap<String, Object>(5);
        properties.put(PersistenceUnitProperties.JDBC_DRIVER, TEST_DRIVER);
        properties.put(PersistenceUnitProperties.JDBC_URL, TEST_URL);
        properties.put(PersistenceUnitProperties.JDBC_USER, TEST_USER);
        properties.put(PersistenceUnitProperties.JDBC_PASSWORD, TEST_PWD);
        properties.put(PersistenceUnitProperties.TARGET_DATABASE, TEST_TARGET_DB);
        return properties;
    }

    private void assertPersistenceProperties(Map<String, Object> aktualProps) {
        assertThat(aktualProps.get(PersistenceUnitProperties.JDBC_DRIVER).toString(), is(TEST_DRIVER));
        assertThat(aktualProps.get(PersistenceUnitProperties.JDBC_URL).toString(), is(TEST_URL));
        assertThat(aktualProps.get(PersistenceUnitProperties.JDBC_USER).toString(), is(TEST_USER));
        assertThat(aktualProps.get(PersistenceUnitProperties.JDBC_PASSWORD).toString(), is(TEST_PWD));
        assertThat(aktualProps.get(PersistenceUnitProperties.TARGET_DATABASE).toString(), is(TEST_TARGET_DB));
        assertThat(aktualProps.size(), is(5));
    }

    private Map<String, Object> getPropertiesOfGetEntityManagerCall(Map<String, Object> expectedProps) throws StorageException {
        EntityManagerServiceBase ems = new EntityManagerServiceBase();
        ems.bindEntityManagerFactoryBuilder(new EntityManagerFactoryBuilderMock(), TEST_JPA_SERVICE_PROPS);
        EntityManagerMock emMock = (EntityManagerMock) ems.getEntityManager(expectedProps);
        Map<String, Object> aktualProps = emMock.getProperties();
        return aktualProps;
    }

    private class RegisterRunnable implements Runnable {
        public boolean done;
        public Exception failure;

        private CountDownLatch latchStart;
        private CountDownLatch latchDone;
        private long initialDelay;
        private long awaitDone;

        public RegisterRunnable(CountDownLatch latchStart, CountDownLatch latchDone, long initialDelay, long awaitDone) {
            this.latchStart = latchStart;
            this.latchDone = latchDone;
            this.initialDelay = initialDelay;
            this.awaitDone = awaitDone;
        }

        @Override
        public void run() {
            try {
                latchStart.await();
                Thread.sleep(initialDelay);
            } catch (InterruptedException e) {
                failure = e;
                return;
            }

            Hashtable<String, String> serviceProps = new Hashtable<String, String>();
            serviceProps.put("osgi.unit.name", TEST_JPA_UNIT_NAME);
            BundleContext context = FrameworkUtil.getBundle(EntityManagerServiceImplTest.class).getBundleContext();
            ServiceRegistration<EntityManagerFactory> registration = null;
            try {
                registration = context.registerService(EntityManagerFactory.class, new EntityManagerFactoryMock(
                        getDefaultPersistenceProperties()), serviceProps);
                done = latchDone.await(awaitDone, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                failure = e;
            } finally {
                registration.unregister();
            }
        }
    }


    private class ClientRunnable implements Runnable {
        public boolean done;
        public Exception failure;

        private CountDownLatch latchStart;
        private CountDownLatch latchDone;
        private long timeout;

        public ClientRunnable(CountDownLatch latchStart, CountDownLatch latchDone, long timeout) {
            this.latchStart = latchStart;
            this.latchDone = latchDone;
            this.timeout = timeout;
        }
        @Override
        public void run() {
            try {
                System.setProperty(EntityManagerServiceBase.SKALLI_EMF_TIMEOUT, Long.toString(timeout));
                latchStart.countDown();
                Map<String, Object> aktualProps = getPropertiesOfGetEntityManagerCall(null);
                assertPersistenceProperties(aktualProps);
                latchDone.countDown();
                done = true;
            } catch (StorageException e) {
                failure = e;
            } finally {
                System.clearProperty(EntityManagerServiceBase.SKALLI_EMF_TIMEOUT);
            }
        }
    }
}
