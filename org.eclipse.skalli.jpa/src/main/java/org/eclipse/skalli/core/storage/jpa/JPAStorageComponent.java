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
package org.eclipse.skalli.core.storage.jpa;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.io.IOUtils;
import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.queries.CursoredStream;
import org.eclipse.skalli.services.persistence.EntityManagerService;
import org.eclipse.skalli.services.persistence.EntityManagerServiceBase;
import org.eclipse.skalli.services.persistence.StorageConsumer;
import org.eclipse.skalli.services.persistence.StorageService;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JPAStorageComponent extends EntityManagerServiceBase implements EntityManagerService, StorageService {

    private static final Logger LOG = LoggerFactory.getLogger(JPAStorageComponent.class);

    // page size for mass operations
    private static final int PAGE_SIZE = 100;

    @Override
    protected void activate(ComponentContext context) {
        super.activate(context);
        LOG.info(MessageFormat.format("[StorageService][JPA] {0} : activated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    @Override
    protected void deactivate(ComponentContext context) {
        LOG.info(MessageFormat.format("[StorageService][JPA] {0} : deactivated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
        super.deactivate(context);
    }

    @Override
    public void write(String category, String id, InputStream blob) throws IOException {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        try {
            StorageItem item = findStorageItem(category, id, em);
            if (item == null) {
                StorageItem newItem = new StorageItem();
                newItem.setId(id);
                newItem.setCategory(category);
                newItem.setDateModified(new Date());
                newItem.setContent(IOUtils.toString(blob, "UTF-8")); //$NON-NLS-1$
                em.persist(newItem);
            } else { //update
                item.setDateModified(new Date());
                item.setContent(IOUtils.toString(blob, "UTF-8")); //$NON-NLS-1$
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    @Override
    public InputStream read(String category, String id) throws IOException {
        EntityManager em = getEntityManager();
        ByteArrayInputStream returnStream = null;
        try {
            StorageItem item = findStorageItem(category, id, em);
            if (item != null) {
                returnStream = new ByteArrayInputStream(item.getContent().getBytes("UTF-8")); //$NON-NLS-1$
            }
        } finally {
            em.close();
        }
        return returnStream;
    }

    @Override
    public void readAll(String category, StorageConsumer consumer) throws IOException {
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("getByCategory", StorageItem.class); //$NON-NLS-1$
            query.setHint(QueryHints.CURSOR, HintValues.TRUE);
            query.setHint(QueryHints.CURSOR_INITIAL_SIZE, PAGE_SIZE);
            query.setHint(QueryHints.CURSOR_PAGE_SIZE, PAGE_SIZE);
            query.setParameter("category", category); //$NON-NLS-1$
            CursoredStream cursor = null;
            try {
                cursor = (CursoredStream) query.getSingleResult();
                while (cursor.hasNext()) {
                    StorageItem next = (StorageItem)cursor.next();
                    consumer.consume(next.getCategory(), next.getId(), next.getDateModified().getTime(),
                            asStream(next.getContent()));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } finally {
            em.close();
        }
    }

    @Override
    public void archive(String category, String id) throws IOException {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            StorageItem item = findStorageItem(category, id, em);
            if (item == null) {
                return;
            }
            HistoryStorageItem histItem = new HistoryStorageItem();
            histItem.setCategory(category);
            histItem.setId(id);
            histItem.setContent(item.getContent());
            histItem.setDateCreated(new Date());
            em.persist(histItem);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    @Override
    public void writeToArchive(String category, String id, long timestamp, InputStream blob) throws IOException {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            HistoryStorageItem item = findHistoryItem(category, id, timestamp, em);
            if (item == null) {
                HistoryStorageItem newItem = new HistoryStorageItem();
                newItem.setCategory(category);
                newItem.setId(id);
                newItem.setContent(IOUtils.toString(blob, "UTF-8")); //$NON-NLS-1$
                newItem.setDateCreated(new Date(timestamp));
                em.persist(newItem);
            } else {
                item.setContent(IOUtils.toString(blob, "UTF-8")); //$NON-NLS-1$
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    @Override
    public void readFromArchive(String category, String id, StorageConsumer consumer) throws IOException {
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("getItemsByCompositeKey", HistoryStorageItem.class); //$NON-NLS-1$
            query.setHint(QueryHints.CURSOR, HintValues.TRUE);
            query.setHint(QueryHints.CURSOR_INITIAL_SIZE, PAGE_SIZE);
            query.setHint(QueryHints.CURSOR_PAGE_SIZE, PAGE_SIZE);
            query.setParameter("category", category); //$NON-NLS-1$
            query.setParameter("id", id); //$NON-NLS-1$
            CursoredStream cursor = null;
            try {
                cursor = (CursoredStream) query.getSingleResult();
                while (cursor.hasNext()) {
                    HistoryStorageItem next = (HistoryStorageItem)cursor.next();
                    consumer.consume(next.getCategory(), next.getId(), next.getDateCreated().getTime(),
                            asStream(next.getContent()));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } finally {
            em.close();
        }
    }

    @Override
    public List<String> keys(String category) throws IOException {
        EntityManager em = getEntityManager();
        List<String> resultList = new ArrayList<String>();
        try {
            TypedQuery<String> query = em.createNamedQuery("getIdsByCategory", String.class); //$NON-NLS-1$
            query.setParameter("category", category); //$NON-NLS-1$
            resultList = query.getResultList();
        } finally {
            em.close();
        }
        return resultList;
    }

    private static StorageItem findStorageItem(String category, String id, EntityManager em) {
        return em.find(StorageItem.class, new StorageId(category, id));
    }

    private static HistoryStorageItem findHistoryItem(String category, String id, long timestamp, EntityManager em) {
        TypedQuery<HistoryStorageItem> query = em.createNamedQuery("getItemByTimestamp", HistoryStorageItem.class); //$NON-NLS-1$
        query.setParameter("category", category); //$NON-NLS-1$
        query.setParameter("id", id); //$NON-NLS-1$
        query.setParameter("dateCreated", new Date(timestamp)); //$NON-NLS-1$
        List<HistoryStorageItem> resultList = query.getResultList();
        if (resultList.size() > 1) {
            throw new IllegalStateException();
        }
        return resultList.isEmpty() ? null : resultList.get(0);
    }

    private static InputStream asStream(String content) throws IOException {
        return new ByteArrayInputStream(content.getBytes("UTF-8")); //$NON-NLS-1$
    }
}
