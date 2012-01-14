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
package org.eclipse.skalli.model.ext.misc;

import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.model.PropertyName;

public class ReviewEntry extends EntityBase {

    @PropertyName(position = 0)
    public static final String PROPERTY_VOTER = "voter"; //$NON-NLS-1$

    @PropertyName(position = 1)
    public static final String PROPERTY_COMMENT = "comment"; //$NON-NLS-1$

    @PropertyName(position = 2)
    public static final String PROPERTY_TIMESTAMP = "timestamp"; //$NON-NLS-1$

    @PropertyName(position = 3)
    public static final String PROPERTY_RATING = "rating"; //$NON-NLS-1$

    private String voter = ""; //$NON-NLS-1$
    private String comment = ""; //$NON-NLS-1$
    private long timestamp = System.currentTimeMillis();
    private ProjectRating rating = ProjectRating.NONE;

    public ReviewEntry() {
    }

    public ReviewEntry(ProjectRating rating, String comment, String voter, long timestamp) {
        this.rating = rating;
        this.comment = comment;
        this.voter = voter;
        this.timestamp = timestamp;
    }

    public ProjectRating getRating() {
        return rating;
    }

    public void setRating(ProjectRating rating) {
        if (rating == null) {
            rating = ProjectRating.NONE;
        }
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getVoter() {
        return voter;
    }

    public void setVoter(String voter) {
        this.voter = voter;
    }

    public long getTimestamp() {
        if (timestamp == 0) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

}
