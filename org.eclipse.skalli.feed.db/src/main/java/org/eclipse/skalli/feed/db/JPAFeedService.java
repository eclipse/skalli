/*******************************************************************************
 * Copyright (c) 2010 - 2011 SAP AG and others.
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.feed.db.entities.EntryJPA;
import org.eclipse.skalli.services.feed.Entry;
import org.eclipse.skalli.services.feed.FeedService;
import org.eclipse.skalli.services.feed.FeedServiceException;
import org.eclipse.skalli.services.persistence.StorageException;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JPAFeedService implements FeedService {

    private static final Logger LOG = LoggerFactory.getLogger(JPAFeedService.class);

    private FeedEntityManagerService entityManagerService;

    protected void activate(ComponentContext context) {
        LOG.info(MessageFormat.format("[FeedService] {0} : activated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    protected void deactivate(ComponentContext context) {
        LOG.info(MessageFormat.format("[FeedService] {0} : deactivated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    public void bindEntityManagerService(FeedEntityManagerService ems) {
        this.entityManagerService = ems;
        LOG.info(MessageFormat.format("bindEntityManagerService({0})", ems.getClass().getName())); //$NON-NLS-1$

    }

    public void unbindEntityManagerService(FeedEntityManagerService ems) {
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
    public List<Entry> findEntries(UUID projectId, int maxResults) throws FeedServiceException {
        if (projectId == null) {
            throw new IllegalArgumentException("projectId expected to be not null");
        }
        if (maxResults < 0 && maxResults != FeedService.SELECT_ALL) {
            throw new IllegalArgumentException("maxResults expected to be >= 0 or " + FeedService.SELECT_ALL);
        }
        if (maxResults == 0) {
            return Collections.emptyList();
        }

        List<Entry> results = new ArrayList<Entry>();
        EntityManager em = getEntityManager();
        EntityTransaction tx = null;
        try {
            tx = em.getTransaction();
            tx.begin();
            Query q = em.createNamedQuery(EntryJPA.FIND_BY_PROJECT_ID);
            if (maxResults > 0) {
                q.setMaxResults(maxResults);
            }
            q.setParameter("projectId", projectId.toString());

            results = (List<Entry>) q.getResultList();
            if (results == null) {
                results = new ArrayList<Entry>();
            }
            tx.rollback();
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new FeedServiceException("Can't find feed entries for project " + projectId.toString(), e);
        } finally {
            em.close();
        }
        return results;
    }

    @Override
    public List<Entry> findEntries(UUID projectId, Collection<String> sources, int maxResults)
            throws FeedServiceException {
        if (projectId == null) {
            throw new IllegalArgumentException("projectId expected to be not null");
        }
        if (maxResults < 0 && maxResults != FeedService.SELECT_ALL) {
            throw new IllegalArgumentException("maxResults expected to be >= 0 or " + FeedService.SELECT_ALL);
        }
        if (maxResults == 0 || CollectionUtils.isEmpty(sources)) {
            return Collections.emptyList();
        }

        List<Entry> results = new ArrayList<Entry>();
        EntityManager em = getEntityManager();
        EntityTransaction tx = null;
        try {
            tx = em.getTransaction();
            tx.begin();
            Query q = em.createNamedQuery(EntryJPA.FIND_BY_PROJECT_AND_SOURCES);
            if (maxResults > 0) {
                q.setMaxResults(maxResults);
            }
            q.setParameter("projectId", projectId.toString());
            q.setParameter("sources", sources);

            results = (List<Entry>) q.getResultList();
            if (results == null) {
                results = new ArrayList<Entry>();
            }
            tx.rollback();
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new FeedServiceException("Can't find feed entries for project " + projectId.toString()
                    + " matching any of " + StringUtils.join(sources, ","), e);
        } finally {
            em.close();
        }
        return results;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> findSources(UUID projectId) throws FeedServiceException {
        if (projectId == null) {
            throw new IllegalArgumentException("projectId expected to be not null");
        }
        List<String> results;
        EntityManager em = getEntityManager();
        EntityTransaction tx = null;
        try {
            tx = em.getTransaction();
            tx.begin();
            Query q = em.createNamedQuery(EntryJPA.FIND_SOURCES_BY_PROJECT_ID);
            q.setParameter("projectId", projectId.toString());

            results = (List<String>) q.getResultList();
            if (results == null) {
                results = new ArrayList<String>();
            }
            tx.rollback();
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new FeedServiceException("Can't find feed sources for project " + projectId.toString(), e);
        } finally {
            em.close();
        }
        return results;
    }
}
