/*******************************************************************************
 * Copyright (c) 2010 - 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.feed.db.entities;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.eclipse.skalli.services.feed.Link;

@Embeddable
public class LinkJPA implements Link {

    public static final int TITLE_LENGHT = EntryJPA.TITLE_LENGTH;
    public static final int HREF_LENGTH = EntryJPA.URI_LENGTH;

    @Column(length = LinkJPA.HREF_LENGTH)
    private String href;

    @Column(length = LinkJPA.TITLE_LENGHT)
    private String title;

    @Override
    public String getHref() {
        return href;
    }

    @Override
    public void setHref(String href) {
        this.href = href;
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((href == null) ? 0 : href.hashCode());
        result = prime * result + ((title == null) ? 0 : title.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LinkJPA other = (LinkJPA) obj;
        if (href == null) {
            if (other.href != null) {
                return false;
            }
        } else if (!href.equals(other.href)) {
            return false;
        }
        if (title == null) {
            if (other.title != null) {
                return false;
            }
        } else if (!title.equals(other.title)) {
            return false;
        }
        return true;
    }

}
