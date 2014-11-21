/*******************************************************************************
 * Copyright (c) 2010-2014 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.core.feed.jpa;

import java.text.MessageFormat;
import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.eclipse.skalli.services.feed.FeedEntry;
import org.eclipse.skalli.services.feed.FeedPersistenceService;
import org.eclipse.skalli.services.persistence.EntityManagerServiceBase;
import org.eclipse.skalli.services.persistence.StorageException;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JPAFeedPersistenceComponent extends EntityManagerServiceBase implements FeedPersistenceService {

    private static final Logger LOG = LoggerFactory.getLogger(JPAFeedPersistenceComponent.class);

    @Override
    protected void activate(ComponentContext context) {
        super.activate(context);
        LOG.info(MessageFormat.format("[FeedPersistenceService][jpa] {0} : activated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    @Override
    protected void deactivate(ComponentContext context) {
        LOG.info(MessageFormat.format("[FeedPersistenceService][jpa] {0} : deactivated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
        super.deactivate(context);
    }

    @Override
    public void merge(Collection<FeedEntry> entries) throws StorageException {
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
                throw new StorageException(MessageFormat.format("Failed to persist {0} ({1})",
                        EntryJPA.class.getSimpleName(), entry.getProjectId().toString()), e);
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
