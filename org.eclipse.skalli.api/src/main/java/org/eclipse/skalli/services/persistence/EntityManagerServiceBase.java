package org.eclipse.skalli.services.persistence;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.lang.StringUtils;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.configuration.ConfigurationProperties;
import org.eclipse.skalli.services.persistence.internal.ReflectionUtil;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.jpa.EntityManagerFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityManagerServiceBase implements EntityManagerService {

    private class PropertiesCache {
        //key: jpaUnitName value: the propsMap for the jpaUnitName
        private Map<String, Map<String, Object>> propertiesMapCache = new HashMap<String, Map<String, Object>>();

        Map<String, Object> get(String jpaUnitName) {
            return propertiesMapCache.get(jpaUnitName);
        }

        public void update(String jpaUnitName, Map<String, Object> properties) {
            propertiesMapCache.put(jpaUnitName, properties);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(EntityManagerServiceBase.class);

    private static final Set<String> ALL_PERSISTENCE_PROPERTIES = ReflectionUtil
            .getPublicStaticFinalFieldValues(PersistenceUnitProperties.class);

    static final String SKALLI_PERSISTENCE = "skalli.persistence."; //$NON-NLS-1$

    static final Map<String, String> DERBY_MEMORY_PERSISTENCE_PROPERTIES = new HashMap<String, String>(5);
    static {
        DERBY_MEMORY_PERSISTENCE_PROPERTIES.put(PersistenceUnitProperties.JDBC_DRIVER,
                "org.apache.derby.jdbc.EmbeddedDriver"); //$NON-NLS-1$
        DERBY_MEMORY_PERSISTENCE_PROPERTIES.put(PersistenceUnitProperties.JDBC_URL,
                "jdbc:derby:memory:SkalliDB;create=true"); //$NON-NLS-1$
        DERBY_MEMORY_PERSISTENCE_PROPERTIES.put(PersistenceUnitProperties.JDBC_USER, "skalli"); //$NON-NLS-1$
        DERBY_MEMORY_PERSISTENCE_PROPERTIES.put(PersistenceUnitProperties.JDBC_PASSWORD, "skalli"); //$NON-NLS-1$
        DERBY_MEMORY_PERSISTENCE_PROPERTIES.put(PersistenceUnitProperties.TARGET_DATABASE, "Derby"); //$NON-NLS-1$
    }

    private EntityManagerFactoryBuilder emfb;
    private String jpaUnitName;

    private PropertiesCache propertiesCache = new PropertiesCache();

    protected void activate(ComponentContext context) {
        LOG.info(MessageFormat.format("[EntityManagerService] {0} : activated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    protected void deactivate(ComponentContext context) {
        LOG.info(MessageFormat.format("[EntityManagerService] {0} : deactivated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    @Override
    public EntityManager getEntityManager() throws StorageException {
        if (StringUtils.isBlank(jpaUnitName)) {
            throw new StorageException("Can't create EntityManager. No jpaUnitName set!");
        }
        return getEntityManager(jpaUnitName);
    }

    /**
     * Creates an <code>EntityManager</code> for the given persistence unit name.
     * If <code>EntityManagerFactory</code> already exists for the given unit name,
     * it is used to create an <code>EntityManager</code>.
     * <p>
     * Otherwise, try to get configuration properties for creating a new factory
     * with {@link ConfigurationProperties#getProperty(String)}. Searches for properties with names corresponding
     * to the constants defined in {@link PersistenceUnitProperties}. First checks whether
     * a property matching the pattern <tt>"skalli.persistence.&lt;jpaUnitName&gt;.&lt;propertyKey&gt;"</tt>
     * can be found. Alternatively, searches for a property matching the pattern
     * <tt>"skalli.persistence.&lt;propertyKey&gt;"</tt>.
     * <p>
     * If no such configuration properties could be found, an entity manager for an in-memory Derby
     * database is returned (see {@link #DERBY_MEMORY_PERSISTENCE_PROPERTIES}.
     *
     * @see EntityManagerFactoryBuilder#createEntityManagerFactory(Map)
     * @see EntityManagerService#getEntityManager(EntityManagerFactory, String)
     *
     **/
    protected EntityManager getEntityManager(String jpaUnitName) throws StorageException {
        EntityManagerFactory defaultEmf = Services.getService(EntityManagerFactory.class, getFilter(jpaUnitName));
        return getEntityManager(defaultEmf, jpaUnitName);
    }

    private EntityManager getEntityManager(EntityManagerFactory defaultEntityManagerFactory, String jpaUnitName)
            throws StorageException {
        if (defaultEntityManagerFactory != null) {
            LOG.debug("Using default entity manager factory");
            return createEntityManager(defaultEntityManagerFactory);
        }
        else {
            LOG.debug("Using configuration parameters to create entity manager");
            return createEntityManagerFromConfiguration(jpaUnitName);
        }
    }

    EntityManager createEntityManager(EntityManagerFactory entityManagerFactory) throws StorageException {
        try {
            return entityManagerFactory.createEntityManager();
        } catch (IllegalStateException e) {
            throw new StorageException(MessageFormat.format("Can't create entity manager using {0}: ",
                    entityManagerFactory.toString()), e);
        }
    }

    private EntityManager createEntityManagerFromConfiguration(String jpaUnitName) throws StorageException {
        EntityManagerFactoryBuilder emfbService = getEnityManagerFactoryBuilder();
        if (emfbService == null) {
            throw new StorageException("Can't create an entity manager: No entity manager factory builder available.");
        }
        return createEntityManagerFromConfiguration(emfbService, jpaUnitName);
    }

    /**
    * severity default so that unitTest have accepted
    */
    EntityManager createEntityManagerFromConfiguration(EntityManagerFactoryBuilder emfbService,
            String jpaUnitName) throws StorageException {
        Map<String, Object> configuredProperties = getConfiguredProperties(jpaUnitName);
        return createEntityManager(emfbService.createEntityManagerFactory(configuredProperties));
    }

    Map<String, Object> getConfiguredProperties(String jpaUnitName) throws StorageException {
        Map<String, Object> cachedProps = propertiesCache.get(jpaUnitName); //performance reasons a cache
        if (cachedProps != null) {
            return cachedProps;
        }

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.putAll(getConfiguredPropertyMap(jpaUnitName));
        if (properties.size() == 0) {
            properties.putAll(DERBY_MEMORY_PERSISTENCE_PROPERTIES);
            LOG.info(MessageFormat.format("No persistence parameters configured. Using a Derby in-memory database " +
                    "as fallback with following parameters: {0}", properties.toString()));
        }
        properties.put(EntityManagerFactoryBuilder.JPA_UNIT_NAME, jpaUnitName);

        propertiesCache.update(jpaUnitName, properties);

        logConfiguredProperties(jpaUnitName, properties);
        return properties;
    }

    /**
     * Writes the properties to the log.
     * As the properties might contain passwords, which should not be displayed, we only log some of them.
    */
    private void logConfiguredProperties(String jpaUnitName, Map<String, Object> properties) {
        if (!LOG.isInfoEnabled()) {
            return;
        }

        StringBuilder msg = new StringBuilder("calculated properties for jpaPersistenceUnit = '" + jpaUnitName + "': ");
        msg.append(PersistenceUnitProperties.TARGET_DATABASE).append(" = ")
                .append(properties.get(PersistenceUnitProperties.TARGET_DATABASE)).append("; ");
        msg.append(PersistenceUnitProperties.JDBC_DRIVER).append(" = ")
                .append(properties.get(PersistenceUnitProperties.JDBC_DRIVER)).append("; ");
        msg.append(PersistenceUnitProperties.JDBC_URL).append(" = ")
                .append(properties.get(PersistenceUnitProperties.JDBC_URL)).append("; ");
        msg.append(PersistenceUnitProperties.JDBC_USER).append(" = ")
                .append(properties.get(PersistenceUnitProperties.JDBC_USER)).append("...");
        LOG.info(msg.toString());
    }

    private Map<String, String> getConfiguredPropertyMap(String jpaUnitName) {
        HashMap<String, String> properties = new HashMap<String, String>();
        for (String propertyKey : ALL_PERSISTENCE_PROPERTIES) {
            putNotEmptyProperty(properties, jpaUnitName, propertyKey);
        }
        return properties;
    }

    private void putNotEmptyProperty(HashMap<String, String> properties, String jpaUnitName, String propertyKey) {
        String propertyValue = getPropertyValue(jpaUnitName, propertyKey);
        if (StringUtils.isNotBlank(propertyValue)) {
            properties.put(propertyKey, propertyValue);
        }
    }

    private String getPropertyValue(String jpaUnitName, String propertyKey) {
        String result = ConfigurationProperties.getProperty(SKALLI_PERSISTENCE + jpaUnitName + "." + propertyKey); //$NON-NLS-1$
        if (result == null) {
            result = ConfigurationProperties.getProperty(SKALLI_PERSISTENCE + propertyKey);
        }
        return result;
    }

    @SuppressWarnings("nls")
    private String getFilter(String jpaUnitName) {
        return "(osgi.unit.name=" + jpaUnitName + ")";
    }

    private EntityManagerFactoryBuilder getEnityManagerFactoryBuilder() {
        return emfb;
    }

    public void bindEntityManagerFactoryBuilder(EntityManagerFactoryBuilder emfb, Map<Object, Object> properties) {
        this.emfb = emfb;
        Object value = properties.get("osgi.unit.name");
        this.jpaUnitName = (value == null ? null : value.toString());
    }

    public void unbindEntityManagerFactoryBuilder(EntityManagerFactoryBuilder emfb) {
        this.emfb = null;
    }

}
