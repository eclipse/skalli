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
package org.eclipse.skalli.model.ext.misc;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.PropertyName;

public class ReviewProjectExt extends ExtensionEntityBase {

    public static final String MODEL_VERSION = "1.0"; //$NON-NLS-1$
    public static final String NAMESPACE = "http://www.eclipse.org/skalli/2010/Model/Extension-Review"; //$NON-NLS-1$

    @PropertyName(position = 0)
    public static final String PROPERTY_RATING_STYLE = "ratingStyle"; //$NON-NLS-1$

    @PropertyName(position = 1)
    public static final String PROPERTY_ALLOW_ANONYMOUS = "allowAnonymous"; //$NON-NLS-1$

    private ArrayList<ReviewEntry> reviews = new ArrayList<ReviewEntry>();
    private int numberVotes = 0;
    private int numberThumbsUp = 0;
    private int numberThumbsDown = 0;
    private float averageRating = 0;
    private boolean allowAnonymous = false;
    private ProjectRatingStyle ratingStyle = ProjectRatingStyle.TWO_STATES;

    public boolean getAllowAnonymous() {
        return allowAnonymous;
    }

    public void setAllowAnonymous(boolean allowAnonymous) {
        this.allowAnonymous = allowAnonymous;
    }

    public ProjectRatingStyle getRatingStyle() {
        if (ratingStyle == null) {
            ratingStyle = ProjectRatingStyle.TWO_STATES;
        }
        return ratingStyle;
    }

    public void setRatingStyle(ProjectRatingStyle ratingStyle) {
        this.ratingStyle = ratingStyle;
    }

    public synchronized List<ReviewEntry> getReviews() {
        if (reviews == null) {
            reviews = new ArrayList<ReviewEntry>();
        }
        return reviews;
    }

    public synchronized void addReview(ReviewEntry review) {
        if (reviews == null) {
            reviews = new ArrayList<ReviewEntry>();
        }
        updateRating(review);
        reviews.add(review);
    }

    private void updateRating(ReviewEntry review) {
        float delta = 0;
        switch (review.getRating()) {
        case UP:
            ++numberThumbsUp;
            delta = 4.0f;
            break;
        case DOWN:
            ++numberThumbsDown;
            delta = 2.0f;
            break;
        case FACE_CRYING:
            ++numberThumbsDown;
            delta = 1.0f;
            break;
        case FACE_SAD:
            ++numberThumbsDown;
            delta = 2.0f;
            break;
        case FACE_PLAIN:
            delta = 3.0f;
            break;
        case FACE_SMILE:
            ++numberThumbsUp;
            delta = 4.0f;
            break;
        case FACE_SMILE_BIG:
            ++numberThumbsUp;
            delta = 5.0f;
            break;
        }
        averageRating = (averageRating * numberVotes + delta) / (numberVotes + 1);
        ++numberVotes;
    }

    public int getNumberVotes() {
        return numberVotes;
    }

    public int getNumberThumbsUp() {
        return numberThumbsUp;
    }

    public int getNumberThumbsDown() {
        return numberThumbsDown;
    }

    public String getRecommendedRatio() {
        if (numberVotes == 0) {
            return "no votes yet";
        }
        return Integer.toString(numberThumbsUp * 100 / numberVotes) + "%";
    }

    public ProjectRating getAverageRating() {
        switch (Math.round(averageRating)) {
        case 0:
            return ProjectRating.NONE;
        case 1:
            return ProjectRating.FACE_CRYING;
        case 2:
            return ProjectRating.FACE_SAD;
        case 3:
            return ProjectRating.FACE_PLAIN;
        case 4:
            return ProjectRating.FACE_SMILE;
        case 5:
            return ProjectRating.FACE_SMILE_BIG;
        }
        // should never happen
        throw new IllegalStateException("Invalid average rating");
    }
}
