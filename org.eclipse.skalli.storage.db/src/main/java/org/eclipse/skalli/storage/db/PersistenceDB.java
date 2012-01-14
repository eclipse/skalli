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
package org.eclipse.skalli.storage.db;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import org.apache.commons.io.IOUtils;
import org.eclipse.skalli.services.persistence.StorageException;
import org.eclipse.skalli.services.persistence.StorageService;
import org.eclipse.skalli.storage.db.entities.HistoryStorageItem;
import org.eclipse.skalli.storage.db.entities.StorageId;
import org.eclipse.skalli.storage.db.entities.StorageItem;

public class PersistenceDB implements StorageService {

    public PersistenceDB() {
    }

    private EntityManagerFactory emf;

    @Override
    public void write(String category, String id, InputStream blob) throws StorageException {
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
            throw new StorageException("Failed to write data", e);
        } finally {
            em.close();
        }
    }

    @Override
    public InputStream read(String category, String id) throws StorageException {
        EntityManager em = getEntityManager();

        ByteArrayInputStream returnStream = null;
        try {
            StorageItem item = findStorageItem(category, id, em);
            if (item != null) {
                returnStream = new ByteArrayInputStream(item.getContent().getBytes("UTF-8"));
            }
        } catch (UnsupportedEncodingException e) {
            throw new StorageException("Failed to read data", e);
        } finally {
            em.close();
        }

        return returnStream;
    }

    @Override
    public void archive(String category, String id) throws StorageException {
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
            throw new StorageException("Failed to archive data", e);
        } finally {
            em.close();
        }
    }

    @Override
    public List<String> keys(String category) throws StorageException {
        EntityManager em = getEntityManager();

        List<String> resultList = new ArrayList<String>();
        try {
            TypedQuery<String> query = em.createNamedQuery("getIdsByCategory", String.class);
            query.setParameter("category", category);
            resultList = query.getResultList();
        } catch (Exception e) {
            throw new StorageException("Failed to retrieve IDs", e);
        } finally {
            em.close();
        }

        return resultList;
    }

    public List<HistoryStorageItem> getHistory(String category, String id) throws StorageException {
        EntityManager em = getEntityManager();

        List<HistoryStorageItem> resultList;
        try {
            TypedQuery<HistoryStorageItem> query = em.createNamedQuery("getItemsByCompositeKey",
                    HistoryStorageItem.class);
            query.setParameter("category", category);
            query.setParameter("id", id);
            resultList = query.getResultList();
        } catch (Exception e) {
            throw new StorageException("Failed to retrieve historical data", e);
        } finally {
            em.close();
        }

        return resultList;
    }

    private StorageItem findStorageItem(String category, String id, EntityManager em) throws StorageException {
        StorageItem item;
        try {
            item = em.find(StorageItem.class, new StorageId(category, id));
        } catch (Exception e) {
            throw new StorageException("Failed to find item", e);
        }
        return item;
    }

    private EntityManager getEntityManager() throws StorageException {
        try {
            return emf.createEntityManager();
        } catch (Exception e) {
            throw new StorageException("EntityManager could not be created", e);
        }
    }

    public synchronized void setService(EntityManagerFactory emFactory) {
        emf = emFactory;
    }

    public synchronized void unsetService(EntityManagerFactory emFactory) {
        emf = null;
    }

}
