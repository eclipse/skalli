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
package org.eclipse.skalli.core.feed.jpa;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.services.feed.Entry;
import org.eclipse.skalli.services.feed.FeedService;
import org.eclipse.skalli.services.persistence.EntityManagerServiceBase;
import org.eclipse.skalli.services.persistence.StorageException;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JPAFeedComponent extends EntityManagerServiceBase implements FeedService {

    private static final Logger LOG = LoggerFactory.getLogger(JPAFeedComponent.class);

    @Override
    protected void activate(ComponentContext context) {
        super.activate(context);
        LOG.info(MessageFormat.format("[FeedService][jpa] {0} : activated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    @Override
    protected void deactivate(ComponentContext context) {
        LOG.info(MessageFormat.format("[FeedService][jpa] {0} : deactivated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
        super.deactivate(context);
    }

    @Override
    public List<Entry> findEntries(UUID projectId, int maxResults) throws StorageException {
        return findEntries(projectId, null, maxResults);
    }

    @Override
    public List<Entry> findEntries(UUID projectId, Collection<String> sources, int maxResults)
            throws StorageException {
        if (projectId == null) {
            throw new IllegalArgumentException("argument 'projectId' must not be null");
        }
        if (maxResults < 0) {
            maxResults =  FeedService.SELECT_ALL;
        }
        if (maxResults == 0 || CollectionUtils.isBlank(sources)) {
            return Collections.emptyList();
        }
        String jpaQuery = sources != null? EntryJPA.FIND_BY_PROJECT_AND_SOURCES : EntryJPA.FIND_BY_PROJECT_ID;

        List<Entry> results = new ArrayList<Entry>();
        EntityManager em = getEntityManager();
        EntityTransaction tx = null;
        try {
            tx = em.getTransaction();
            tx.begin();
            Query q = em.createNamedQuery(jpaQuery);
            if (maxResults > 0) {
                q.setMaxResults(maxResults);
            }
            q.setParameter(EntryJPA.PARAM_PROJECT_ID, projectId.toString());
            if (sources != null) {
                q.setParameter(EntryJPA.PARAM_SOURCES, sources);
            }
            results = (List<Entry>) q.getResultList();
            if (results == null) {
                results = new ArrayList<Entry>();
            }
            tx.rollback();
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new StorageException(MessageFormat.format(
                    "Can''t find feed entries for project {0}", projectId.toString(), e));
        } finally {
            em.close();
        }
        return results;
    }

    @Override
    public List<String> findSources(UUID projectId) throws StorageException {
        if (projectId == null) {
            throw new IllegalArgumentException("argument 'projectId' must not be null");
        }
        List<String> results;
        EntityManager em = getEntityManager();
        EntityTransaction tx = null;
        try {
            tx = em.getTransaction();
            tx.begin();
            Query q = em.createNamedQuery(EntryJPA.FIND_SOURCES_BY_PROJECT_ID);
            q.setParameter(EntryJPA.PARAM_PROJECT_ID, projectId.toString());

            results = (List<String>) q.getResultList();
            if (results == null) {
                results = new ArrayList<String>();
            }
            tx.rollback();
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new StorageException(MessageFormat.format(
                    "Can''t find feed sources for project {0}", projectId.toString()), e);
        } finally {
            em.close();
        }
        return results;
    }
}
