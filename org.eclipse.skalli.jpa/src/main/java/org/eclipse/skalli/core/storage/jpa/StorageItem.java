/*******************************************************************************
 * Copyright (c) 2010-2014 SAP AG and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.core.storage.jpa;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@IdClass(StorageId.class)
@Table(name = "Storage")
@NamedQueries({
        @NamedQuery(name = "getByCategory", query = "SELECT r FROM StorageItem r WHERE r.category = :category"),
        @NamedQuery(name = "getIdsByCategory", query = "SELECT r.id FROM StorageItem r WHERE r.category = :category")
})
@Entity
public class StorageItem {
    @Id
    private String category;
    @Id
    private String id;
    @Lob
    private String content;
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateModified;

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getDateModified() {
        return dateModified;
    }

    public void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
    }
}