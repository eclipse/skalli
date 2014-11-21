/*******************************************************************************
 * Copyright (c) 2010-2014 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.core.feed.jpa;

import java.util.Date;
import java.util.UUID;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.eclipse.persistence.annotations.Index;
import org.eclipse.skalli.commons.UUIDUtils;
import org.eclipse.skalli.services.feed.Content;
import org.eclipse.skalli.services.feed.FeedEntry;
import org.eclipse.skalli.services.feed.Link;
import org.eclipse.skalli.services.feed.Person;

@Table(name = "Entry")
@NamedQueries({
        @NamedQuery(name = EntryJPA.FIND_BY_PROJECT_ID,
                query = "SELECT e FROM EntryJPA e where e.projectId = :projectId " + EntryJPA.DEFAULT_ORDER_BY),

        @NamedQuery(name = EntryJPA.FIND_BY_PROJECT_AND_SOURCES,
                query = "SELECT e FROM EntryJPA e where e.projectId = :projectId and e.source in :sources "
                        + EntryJPA.DEFAULT_ORDER_BY),

        @NamedQuery(name = EntryJPA.FIND_SOURCES_BY_PROJECT_ID,
                query = "SELECT DISTINCT e.source FROM EntryJPA e where e.projectId = :projectId "
                        +  "order by e.source")

})
@Entity
public class EntryJPA implements FeedEntry {

    public static final String FIND_BY_PROJECT_ID = "EntryJPA.findByProjectID"; //$NON-NLS-1$
    public static final String FIND_BY_PROJECT_AND_SOURCES = "EntryJPA.findByProjectAndSources"; //$NON-NLS-1$
    public static final String FIND_SOURCES_BY_PROJECT_ID = "EntryJPA.findSourcesByProjectID"; //$NON-NLS-1$

    public static final String PARAM_PROJECT_ID = "projectId"; //$NON-NLS-1$
    public static final String PARAM_SOURCES = "sources"; //$NON-NLS-1$

    static final String DEFAULT_ORDER_BY = "order by e.published DESC, e.id ASC"; //$NON-NLS-1$

    public static final int ID_LENGTH = 40;
    public static final int URI_LENGTH = 512;
    public static final int TITLE_LENGTH = 256;
    public static final int TITLE_EX_LENGTH = 512;
    public static final int TYPE_LENGTH = 32;
    public static final int SOURCE_LENGTH = 16;

    @Id
    @Column(length = ID_LENGTH)
    private String id;

    @Column(length = TITLE_LENGTH, nullable = false)
    private String title;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "href", column = @Column(name = "link_href", length = LinkJPA.HREF_LENGTH)),
            @AttributeOverride(name = "title", column = @Column(name = "link_title", length = LinkJPA.TITLE_LENGHT))
    })
    private LinkJPA link;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "type", column = @Column(name = "content_type", length = ContentJPA.TYPE_LENGTH)),
            @AttributeOverride(name = "value", column = @Column(name = "content_value", length = ContentJPA.VALUE_LENGTH))
    })
    private ContentJPA content;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date published;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "name", column = @Column(name = "author_name", length = PersonJPA.NAME_LENGTH)),
            @AttributeOverride(name = "email", column = @Column(name = "author_email", length = PersonJPA.EMAIL_LENGTH))
    })
    private PersonJPA author;

    @Column(nullable = false)
    @Index
    private String projectId;

    @Column(length = SOURCE_LENGTH, nullable = false)
    @Index
    private String source;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public Link getLink() {
        if (link == null) {
            link = new LinkJPA();
        }
        return link;
    }

    @Override
    public Content getContent() {
        if (content == null) {
            content = new ContentJPA();
        }
        return content;
    }

    @Override
    public Date getPublished() {
        return published;
    }

    @Override
    public void setPublished(Date published) {
        this.published = published;
    }

    @Override
    public Person getAuthor() {
        if (author == null) {
            author = new PersonJPA();
        }
        return author;
    }

    @Override
    public UUID getProjectId() {
        return UUIDUtils.asUUID(projectId);
    }

    @Override
    public void setProjectId(UUID projectId) {
        this.projectId = projectId.toString();
    }

    @Override
    public String getSource() {
        return source;
    }

    @Override
    public void setSource(String source) {
        this.source = source;
    }
}
