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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.eclipse.persistence.annotations.Index;

@Table(name = "HistoryStorage")
@NamedQuery(name = "getItemsByCompositeKey", query = "SELECT r FROM HistoryStorageItem r WHERE r.category = :category AND r.id = :id")
@Entity
public class HistoryStorageItem {
    @Id
    @TableGenerator(name = "histKeyGen", table = "SEQUENCE", pkColumnName = "NAME", valueColumnName = "NEXTID")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "histKeyGen")
    private int autoId;
    @Index
    @Column(name = "category")
    private String category;
    @Index
    @Column(name = "id")
    private String id;
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;
    @Lob
    @Column(length = 100000)
    private String content;

    public HistoryStorageItem() {
        // just needed for JPA
    }

    /**
     * Creates a HistoryStorageItem using the current date.
     *
     */
    public HistoryStorageItem(StorageItem item) {
        this.category = item.getCategory();
        this.id = item.getId();
        this.content = item.getContent();

        this.dateCreated = new Date();
    }

    /**
     * Creates a HistoryStorageItem using the supplied date.
     *
     */
    public HistoryStorageItem(StorageItem item, Date dateCreated) {
        this(item);

        this.dateCreated = dateCreated;
    }

    public int getAutoId() {
        return autoId;
    }

    public String getCategory() {
        return category;
    }

    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public Date getDateCreated() {
        return dateCreated;
    }
}
