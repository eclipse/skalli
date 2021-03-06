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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.eclipse.persistence.annotations.Index;

@Table(name = "HistoryStorage")
@NamedQueries({
    @NamedQuery(name = "getItemsByCompositeKey", query = "SELECT r FROM HistoryStorageItem r WHERE r.category = :category AND r.id = :id"),
    @NamedQuery(name = "getItemByTimestamp", query = "SELECT r FROM HistoryStorageItem r WHERE r.category = :category AND r.id = :id AND r.dateCreated = :dateCreated")
})
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
    private String content;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
