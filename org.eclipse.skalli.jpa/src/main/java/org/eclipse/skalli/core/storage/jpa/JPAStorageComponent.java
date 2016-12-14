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
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.commons.io.IOUtils;
import org.eclipse.skalli.services.persistence.EntityManagerService;
import org.eclipse.skalli.services.persistence.EntityManagerServiceBase;
import org.eclipse.skalli.services.persistence.StorageService;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JPAStorageComponent extends EntityManagerServiceBase implements EntityManagerService, StorageService {

    private static final Logger LOG = LoggerFactory.getLogger(JPAStorageComponent.class);

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
                newItem.setContent(IOUtils.toString(blob, "UTF-8"));
                em.persist(newItem);
            } else { //update
                item.setDateModified(new Date());
                item.setContent(IOUtils.toString(blob, "UTF-8"));
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            throw new IOException("Failed to write data", e);
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
                returnStream = new ByteArrayInputStream(item.getContent().getBytes("UTF-8"));
            }
        } catch (UnsupportedEncodingException e) {
            throw new IOException("Failed to read data", e);
        } finally {
            em.close();
        }

        return returnStream;
    }

    @Override
    public void archive(String category, String id) throws IOException {
        EntityManager em = getEntityManager();

        try {
            em.getTransaction().begin();

            // find original StorageItem
            StorageItem item = findStorageItem(category, id, em);
            if (item == null) {
                // nothing to archive
                return;
            }

            // write to HistoryStorage
            HistoryStorageItem histItem = new HistoryStorageItem(item);
            em.persist(histItem);
            em.getTransaction().commit();
        } catch (Exception e) {
            throw new IOException("Failed to archive data", e);
        } finally {
            em.close();
        }
    }

    @Override
    public List<String> keys(String category) throws IOException {
        EntityManager em = getEntityManager();

        List<String> resultList = new ArrayList<String>();
        try {
            TypedQuery<String> query = em.createNamedQuery("getIdsByCategory", String.class);
            query.setParameter("category", category);
            resultList = query.getResultList();
        } catch (Exception e) {
            throw new IOException("Failed to retrieve IDs", e);
        } finally {
            em.close();
        }

        return resultList;
    }

    public List<HistoryStorageItem> getHistory(String category, String id) throws IOException {
        EntityManager em = getEntityManager();

        List<HistoryStorageItem> resultList;
        try {
            TypedQuery<HistoryStorageItem> query = em.createNamedQuery("getItemsByCompositeKey",
                    HistoryStorageItem.class);
            query.setParameter("category", category);
            query.setParameter("id", id);
            resultList = query.getResultList();
        } catch (Exception e) {
            throw new IOException("Failed to retrieve historical data", e);
        } finally {
            em.close();
        }

        return resultList;
    }

    private StorageItem findStorageItem(String category, String id, EntityManager em) throws IOException {
        StorageItem item;
        try {
            item = em.find(StorageItem.class, new StorageId(category, id));
        } catch (Exception e) {
            throw new IOException("Failed to find item", e);
        }
        return item;
    }
}
