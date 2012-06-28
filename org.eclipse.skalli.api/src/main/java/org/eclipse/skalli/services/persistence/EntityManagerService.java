package org.eclipse.skalli.services.persistence;

import javax.persistence.EntityManager;

public interface EntityManagerService {

    /**
     * Retrieves an {@link EntityManager}.
     *
     * @return an entity manager instance, never <code>null</code>.
     *
     * @throws StorageException
     *             if no entity manager could be created.
     */
    public EntityManager getEntityManager() throws StorageException;
}
