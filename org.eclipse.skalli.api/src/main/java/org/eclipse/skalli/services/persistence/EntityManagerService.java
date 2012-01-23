package org.eclipse.skalli.services.persistence;

import javax.persistence.EntityManager;

public interface EntityManagerService {

    /**
     * Creates an <code>EntityManager</code>
     *
     * @return an entity manager instance.
     *
     * @throws StorageException
     *             if no entity manager could be created.
     */
    public EntityManager getEntityManager() throws StorageException;

}
