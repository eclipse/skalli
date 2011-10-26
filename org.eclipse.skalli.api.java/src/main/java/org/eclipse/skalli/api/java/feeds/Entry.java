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
package org.eclipse.skalli.api.java.feeds;

import java.util.Date;
import java.util.UUID;

/**
 * Interface representing an entry of a project timeline.
 */
public interface Entry {

    /**
     * Returns the unique identifier of the timeline entry.
     * @return a 40 characters long SHA-1 key in hexadecimal format.
     */
    public String getId();

    /**
     * Returns the unique identifier of the project to which this timeline entry belongs.
     * @return the identifier of a project, or <code>null</code> if the entry is not
     * assigned to a certain project.
     */
    public UUID getProjectId();

    /**
     * Returns an identifier that indicates from which source this timeline entry
     * has been retrieved. For example, a feed provider retrieving events from a GitWeb
     * server could return <code>"gitweb"</code>.
     */
    public String getSource();

    /**
     * Returns the title of this timeline entry.
     */
    public String getTitle();

    /**
     * Returns the date and time of publication of this timeline entry.
     */
    public Date getPublished();

    /**
     * Returns a link to the details of this timeline entry.
     * Note that the returned {@link Link} may neither have {@link Link#getTitle() title}
     * nor {@link Link#getHref() target address}.
     */
    public Link getLink();

    /**
     * Returns the content (usually a short summary or synopsis) of the timeline entry.
     * Note that the returned {@link Content} may neither have a {@link Content#getType() MIME-type}
     * (in which case <code>text/plain</code> should be assumed) nor a {@link Content#getValue() value}.
     * When rendering content of a timeline entry one should not assume that the returned MIME-type
     * matches the actual content string.
     */
    public Content getContent();

    /**
     * Returns the author of the timeline entry.
     * Note that the returned {@link Person} may neither have a {@link Person#getUserId() user id}
     * nor a {@link Person#getName() clear text name} nor an {@link Person#getEmail() e-mail address}.
     * In the UI the user id should be used as fallback for an unknown name. If the e-mail address
     * of an author is known, the name (or user id) should be rendered as <code>mailto:</code> link.
     */
    public Person getAuthor();

}
