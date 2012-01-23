package org.eclipse.skalli.services.persistence;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
import org.osgi.service.jpa.EntityManagerFactoryBuilder;

public class EntityManagerServiceImplTest {

    private static final String TEST_JPA_UNIT_NAME = "EntityManagerServiceImplTestJpaUnitName";

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

        @SuppressWarnings("unchecked")
        @Override
        public EntityManagerFactory createEntityManagerFactory(Map props) {
            this.emfMock = new EntityManagerFactoryMock(props);
            return emfMock;
        }
    }

    private Map<String, Object> getPropertiesOfGetEntityManagerCall() throws StorageException {
        EntityManagerServiceBase ems = new EntityManagerServiceBase();
        EntityManagerFactoryBuilderMock emfbMock = new EntityManagerFactoryBuilderMock();
        EntityManagerMock emMock = (EntityManagerMock) ems.createEntityManagerFromConfiguration(
                emfbMock, TEST_JPA_UNIT_NAME);
        Map<String, Object> aktualProps = emMock.getProperties();
        return aktualProps;
    }

    @Test
    public void testGetEntityManagerReturnsDefaultDerby() throws StorageException {
        Map<String, Object> aktualProps = getPropertiesOfGetEntityManagerCall();

        assertThat(aktualProps.get(PersistenceUnitProperties.JDBC_DRIVER).toString(),
                is("org.apache.derby.jdbc.EmbeddedDriver"));
        assertThat(aktualProps.get(PersistenceUnitProperties.JDBC_URL).toString(),
                is("jdbc:derby:memory:SkalliDB;create=true"));
        assertThat(aktualProps.get(PersistenceUnitProperties.JDBC_USER).toString(), is("skalli"));
        assertThat(aktualProps.get(PersistenceUnitProperties.JDBC_PASSWORD).toString(), is("skalli"));
        assertThat(aktualProps.get(PersistenceUnitProperties.TARGET_DATABASE).toString(), is("Derby"));
        assertThat(aktualProps.get(EntityManagerFactoryBuilder.JPA_UNIT_NAME).toString(), is(TEST_JPA_UNIT_NAME));

        assertThat(aktualProps.size(), is(6));
    }

    @Test
    public void testGetEntityManagerReturnsConfiguredProps() throws StorageException {

        System.setProperty(EntityManagerServiceBase.SKALLI_PERSISTENCE + PersistenceUnitProperties.JDBC_DRIVER,
                TEST_DRIVER);

        for (Entry<String, String> entry : getExplicitProperties().entrySet()) {
            System.setProperty(entry.getKey(), entry.getValue());
        }

        Map<String, Object> aktualProps = getPropertiesOfGetEntityManagerCall();

        assertThat(aktualProps.get(PersistenceUnitProperties.JDBC_DRIVER).toString(), is(TEST_DRIVER));
        assertThat(aktualProps.get(PersistenceUnitProperties.JDBC_URL).toString(), is(TEST_URL));
        assertThat(aktualProps.get(PersistenceUnitProperties.JDBC_USER).toString(), is(TEST_USER));
        assertThat(aktualProps.get(PersistenceUnitProperties.JDBC_PASSWORD).toString(), is(TEST_PWD));
        assertThat(aktualProps.get(PersistenceUnitProperties.TARGET_DATABASE).toString(), is(TEST_TARGET_DB));
        assertThat(aktualProps.get(EntityManagerFactoryBuilder.JPA_UNIT_NAME).toString(), is(TEST_JPA_UNIT_NAME));

        assertThat(aktualProps.size(), is(6));

        //clean the system properties
        for (Entry<String, String> entry : getExplicitProperties().entrySet()) {
            System.setProperty(entry.getKey(), "");
        }

    }

    private HashMap<String, String> getExplicitProperties() {
        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put(EntityManagerServiceBase.SKALLI_PERSISTENCE + TEST_JPA_UNIT_NAME + "."
                + PersistenceUnitProperties.JDBC_URL, TEST_URL);
        properties.put(EntityManagerServiceBase.SKALLI_PERSISTENCE + PersistenceUnitProperties.JDBC_URL, "ERROR");

        properties.put(EntityManagerServiceBase.SKALLI_PERSISTENCE + PersistenceUnitProperties.JDBC_USER, TEST_USER);
        properties.put(EntityManagerServiceBase.SKALLI_PERSISTENCE + PersistenceUnitProperties.JDBC_PASSWORD, TEST_PWD);
        properties.put(EntityManagerServiceBase.SKALLI_PERSISTENCE + PersistenceUnitProperties.TARGET_DATABASE,
                TEST_TARGET_DB);
        return properties;
    }
}
