package org.eclipse.skalli.services.persistence;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.services.configuration.ConfigurationProperties;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.jpa.EntityManagerFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base implementation of {@link EntityManagerService} that uses {@link EntityManagerFactoryBuilder}
 * from the OSGI JPA service to create {@link EntityManager entity managers}.
 * <p>
 * Note, entity manager service implementations derived from this class must be registered as
 * declarative service and must define a reference to <tt>org.osgi.service.jpa.EntityManagerFactoryBuilder</tt>
 * of the following form:
 * <pre>
 * &lt;reference
 *     target="(osgi.unit.name=&lt;your persistence unit name&gt;)"
 *     interface="org.osgi.service.jpa.EntityManagerFactoryBuilder"
 *     name="EntityManagerFactoryBuilder"
 *     policy="dynamic"
 *     cardinality="1..1"
 *     bind="bindEntityManagerFactoryBuilder"
 *     unbind="unbindEntityManagerFactoryBuilder"/&gt;
 * </pre>
 * <p>
 * Furthermore, derived implementations must call {@link #activate(ComponentContext)} and
 * {@link #deactivate(ComponentContext)}.
 */
public class EntityManagerServiceBase implements EntityManagerService {

    private static final Logger LOG = LoggerFactory.getLogger(EntityManagerServiceBase.class);

    private static final Set<String> ALL_PERSISTENCE_PROPERTIES =
            getPublicStaticFinalFieldValues(PersistenceUnitProperties.class);

    static final String[] REQUIRED_PROPERTIES = {
        PersistenceUnitProperties.JDBC_DRIVER,
        PersistenceUnitProperties.JDBC_URL,
        PersistenceUnitProperties.JDBC_USER,
        PersistenceUnitProperties.JDBC_PASSWORD,
        PersistenceUnitProperties.TARGET_DATABASE,
    };

    static final String SKALLI_PERSISTENCE = "skalli.persistence."; //$NON-NLS-1$
    static final String SKALLI_EMF_TIMEOUT = SKALLI_PERSISTENCE + "timeout"; //$NON-NLS-1$
    static final long SKALLI_EMF_DEFAULT_TIMEOUT = -1L; // no timeout

    private ComponentContext context;
    private EntityManagerFactoryBuilder emfb;
    private String persistenceUnitName;

    // Available JPA properties
    // key: persistenceUnitName value: the property map for the persistenceUnitName
    private Map<String, Map<String, Object>> propertiesCache = new HashMap<String, Map<String, Object>>();

    protected void activate(ComponentContext context) {
        this.context = context;
    }

    protected void deactivate(ComponentContext context) {
        this.context = null;
    }

    protected EntityManagerFactory locateEntityManagerFactory() {
        return (EntityManagerFactory)context.locateService("EntityManagerFactory"); //$NON-NLS-1$
    }

    protected void bindEntityManagerFactoryBuilder(EntityManagerFactoryBuilder emfb, Map<Object, Object> properties) {
        LOG.info(MessageFormat.format("bindEntityManagerFactoryBuilder({0})", emfb.getClass().getName())); //$NON-NLS-1$
        this.emfb = emfb;
        Object value = properties.get("osgi.unit.name"); //$NON-NLS-1$
        if (value != null) {
            persistenceUnitName = value.toString();
        }
    }

    protected void unbindEntityManagerFactoryBuilder(EntityManagerFactoryBuilder emfb) {
        LOG.info(MessageFormat.format("unbindEntityManagerFactoryBuilder({0})", emfb.getClass().getName())); //$NON-NLS-1$
        this.emfb = null;
    }

    /**
     * Retrieves an {@link EntityManager}.
     * <p>
     * If JPA configuration parameters are available (see {@link PersistenceUnitProperties}),
     * the entity manager is created based on these configuration parameters.
     * First checks whether a property matching the pattern
     * <tt>"skalli.persistence.&lt;jpaUnitName&gt;.&lt;propertyKey&gt;"</tt>
     * can be found. Alternatively, searches for a property matching the pattern
     * <tt>"skalli.persistence.&lt;propertyKey&gt;"</tt>.
     * <p>
     * If no explicit configuration is provided, the OSGi service registry is searched for an
     * implementation of the service interface {@link EntityManagerFactory}.
     * A timeout can be specified with the property <tt>skalli.persistence.timeout</tt>
     * to allow a platform persistence service coming up and be injected into the service registry.
     * By default, the timeout is set to <tt>-1</tt> meaning that no timeout is applied.
     * A timeout of zero causes this method to wait indefinitely. All other values define a
     * timeout in milliseconds.
     *
     * @return an entity manager instance, never <code>null</code>.
     *
     * @throws StorageException  if neither the platform could provide a suitable entity manager
     * factory, nor suitable configuration properties have been provided that would allow to
     * construct one, or the creation of the entity manager failed.
     **/
    @Override
    public EntityManager getEntityManager() throws StorageException {
        return getEntityManager(getConfiguredProperties(persistenceUnitName));
    }

    // package protected for tests
    EntityManager getEntityManager(Map<String, Object> properties) throws StorageException {
        if (StringUtils.isBlank(persistenceUnitName)) {
            throw new StorageException("Failed to create an entity manager: no persistence unit name available");
        }

        // if explicit JPA properties are provided, create the entity manager based
        // on these properties - even if the runtime has injected an entity manager factory!
        // note: properties always contains EntityManagerFactoryBuilder.JPA_UNIT_NAME, therefore
        // we check for additional properties
        if (properties != null && properties.size() > 1) {
            return createEntityManager(properties);
        }

        // otherwise: use the entity manager factory provided by the runtime - if any!
        EntityManagerFactory entityManagerFactory = getEntityManagerFactory();
        if (entityManagerFactory == null) {
            throw new StorageException(MessageFormat.format("Failed to create an entity manager: no entity manager" +
                "factory available for persistence unit {0}", persistenceUnitName));
        }
        return createEntityManager(entityManagerFactory);
    }

    private EntityManager createEntityManager(Map<String, Object> properties) throws StorageException {
        if (emfb == null) {
            throw new StorageException("Failed to create entity manager: no entity manager factory builder available");
        }
        List<String> missingProperties = getMissingRequiredProperties(properties);
        if (missingProperties.size() > 0) {
            throw new StorageException(MessageFormat.format(
                    "Failed to create an entity manager: required persistence properties missing ({0})",
                    CollectionUtils.toString(missingProperties, ',')));
        }
        return createEntityManager(emfb.createEntityManagerFactory(properties));
    }

    private EntityManagerFactory getEntityManagerFactory() {
        EntityManagerFactory entityManagerFactory = null;
        long timeout = NumberUtils.toLong(ConfigurationProperties.getProperty(SKALLI_EMF_TIMEOUT),
                SKALLI_EMF_DEFAULT_TIMEOUT);
        try {
            entityManagerFactory = waitEntityManagerFactory(timeout);
        } catch (InterruptedException e) {
            LOG.warn("Thread has been interrupted while waiting for an entity manager factory to appear", e);
        }
        return entityManagerFactory;
    }

    private EntityManagerFactory waitEntityManagerFactory(long timeout) throws InterruptedException {
        long sleep = 0;
        long waited = 0;
        EntityManagerFactory instance = locateEntityManagerFactory();
        if (timeout > 0) {
            while (instance == null && waited < timeout) {
                if (sleep == 0) {
                    sleep = timeout;
                    while (sleep > 10) {
                        sleep >>= 1;
                    }
                }
                if (sleep > 0) {
                    Thread.sleep(sleep);
                    waited += sleep;
                    sleep <<= 1;
                }
                instance = locateEntityManagerFactory();
            }
        }
        return instance;
    }

    private EntityManager createEntityManager(EntityManagerFactory entityManagerFactory) throws StorageException {
        try {
            return entityManagerFactory.createEntityManager();
        } catch (IllegalStateException e) {
            throw new StorageException(MessageFormat.format("Failed to create an entity manager using factory {0}",
                    entityManagerFactory.getClass()), e);
        }
    }

    private List<String> getMissingRequiredProperties(Map<String, Object> properties) {
        List<String> missing = new ArrayList<String>();
        for (String key: REQUIRED_PROPERTIES) {
            if (!properties.containsKey(key)) {
                missing.add(key);
            }
        }
        return missing;
    }

    private Map<String, Object> getConfiguredProperties(String jpaUnitName) throws StorageException {
        Map<String, Object> cachedProps = propertiesCache.get(jpaUnitName);
        if (cachedProps != null) {
            return cachedProps;
        }

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.putAll(getConfiguredPropertyMap(jpaUnitName));
        properties.put(EntityManagerFactoryBuilder.JPA_UNIT_NAME, jpaUnitName);

        propertiesCache.put(jpaUnitName, properties);

        logConfiguredProperties(jpaUnitName, properties);
        return properties;
    }

    @SuppressWarnings("nls")
    private void logConfiguredProperties(String jpaUnitName, Map<String, Object> properties) {
        if (LOG.isInfoEnabled()) {
            StringBuilder msg = new StringBuilder("persistence.unit = '" + jpaUnitName + "': ");
            msg.append(PersistenceUnitProperties.TARGET_DATABASE).append(" = ")
                    .append(properties.get(PersistenceUnitProperties.TARGET_DATABASE)).append("; ");
            msg.append(PersistenceUnitProperties.JDBC_DRIVER).append(" = ")
                    .append(properties.get(PersistenceUnitProperties.JDBC_DRIVER)).append("; ");
            msg.append(PersistenceUnitProperties.JDBC_URL).append(" = ")
                    .append(properties.get(PersistenceUnitProperties.JDBC_URL)).append("; ");
            msg.append(PersistenceUnitProperties.JDBC_USER).append(" = ")
                    .append(properties.get(PersistenceUnitProperties.JDBC_USER));
            LOG.info(msg.toString());
        }
    }

    private Map<String, String> getConfiguredPropertyMap(String jpaUnitName) {
        HashMap<String, String> properties = new HashMap<String, String>();
        for (String propertyKey : ALL_PERSISTENCE_PROPERTIES) {
            putNonEmptyProperty(properties, jpaUnitName, propertyKey);
        }
        return properties;
    }

    private void putNonEmptyProperty(HashMap<String, String> properties, String jpaUnitName, String propertyKey) {
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

    static Set<String> getPublicStaticFinalFieldValues(Class<?> clazz) {
        Set<String> properties = new HashSet<String>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.getType().isAssignableFrom(String.class)) {
                int mod = field.getModifiers();
                if (Modifier.isPublic(mod) && Modifier.isStatic(mod) && Modifier.isFinal(mod)) {
                    try {
                        String propertyValue = field.get(null).toString();
                        if (StringUtils.isNotBlank(propertyValue)) {
                            properties.add(propertyValue);
                        }
                    } catch (Exception e) {
                        LOG.error("Failed to read public static final fields of class " + clazz, e);
                    }
                }
            }
        }
        return properties;
    }
}
