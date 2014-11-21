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

public enum ProjectRating {
    UP, DOWN, NONE,
    FACE_SMILE_BIG, FACE_SMILE, FACE_PLAIN, FACE_SAD, FACE_CRYING;

    private static final ProjectRating[] TWO_STATE_RATINGS =
            new ProjectRating[] { UP, DOWN };

    private static final ProjectRating[] FIVE_STATE_RATINGS =
            new ProjectRating[] { FACE_SMILE_BIG, FACE_SMILE, FACE_PLAIN, FACE_SAD, FACE_CRYING };

    public static ProjectRating[] getRatings(ProjectRatingStyle style) {
        switch (style) {
        case TWO_STATES:
            return TWO_STATE_RATINGS;
        case FIVE_STATES:
            return FIVE_STATE_RATINGS;
        }
        return null;
    }
}
