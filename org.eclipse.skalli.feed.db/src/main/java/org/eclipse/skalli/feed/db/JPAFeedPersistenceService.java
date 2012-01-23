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
package org.eclipse.skalli.feed.db;

import java.text.MessageFormat;
import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.eclipse.skalli.feed.db.entities.EntryJPA;
import org.eclipse.skalli.services.feed.FeedEntry;
import org.eclipse.skalli.services.feed.FeedPersistenceService;
import org.eclipse.skalli.services.feed.FeedServiceException;
import org.eclipse.skalli.services.persistence.StorageException;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JPAFeedPersistenceService implements FeedPersistenceService {

    private static final Logger LOG = LoggerFactory.getLogger(JPAFeedPersistenceService.class);

    private FeedEntityManagerService entityManagerService;

    protected void activate(ComponentContext context) {
        LOG.info(MessageFormat.format("[FeedPersistenceService] {0} : activated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    protected void deactivate(ComponentContext context) {
        LOG.info(MessageFormat.format("[FeedPersistenceService] {0} : deactivated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    protected void bindEntityManagerService(FeedEntityManagerService ems) {
        this.entityManagerService = ems;
        LOG.info(MessageFormat.format("bindEntityManagerService({0})", ems.getClass().getName())); //$NON-NLS-1$

    }

    protected void unbindEntityManagerService(FeedEntityManagerService ems) {
        this.entityManagerService = null;
        LOG.info(MessageFormat.format("unbindEntityManagerService({0})", ems.getClass().getName())); //$NON-NLS-1$
    }

    private EntityManager getEntityManager() throws FeedServiceException {
        if (entityManagerService == null) {
            throw new FeedServiceException("Can't create an entity manager as no entity manager service is available");
        }
        try {
            return entityManagerService.getEntityManager();
        } catch (StorageException e) {
            throw new FeedServiceException(e);
        }
    }

    @Override
    public void merge(Collection<FeedEntry> entries) throws FeedServiceException {
        for (FeedEntry entry : entries) {
            EntityManager em = getEntityManager();
            EntityTransaction tx = null;
            try {
                tx = em.getTransaction();
                tx.begin();
                em.merge(entry);
                tx.commit();
            } catch (RuntimeException e) {
                if (tx != null && tx.isActive()) {
                    tx.rollback();
                }
                throw new FeedServiceException("Failed to persist " + EntryJPA.class.getSimpleName() + " ("
                        + entry.getProjectId().toString() + ")", e);
            } finally {
                em.close();
            }
        }
    }



    @Override
    public EntryJPA createEntry() {
        return new EntryJPA();
    }

}
