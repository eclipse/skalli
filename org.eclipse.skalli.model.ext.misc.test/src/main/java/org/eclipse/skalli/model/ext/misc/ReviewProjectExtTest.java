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

import java.util.List;
import java.util.Map;

import org.eclipse.skalli.testutil.PropertyTestUtil;
import org.junit.Assert;
import org.junit.Test;

public class ReviewProjectExtTest {

    @Test
    public void testPropertyDefinitions() throws Exception {
        Map<String, Object> values = PropertyTestUtil.getValues();

        values.put(ReviewProjectExt.PROPERTY_ALLOW_ANONYMOUS, true);
        values.put(ReviewProjectExt.PROPERTY_RATING_STYLE, ProjectRatingStyle.FIVE_STATES);

        Map<Class<?>, String[]> requiredProperties = PropertyTestUtil.getRequiredProperties();

        PropertyTestUtil.checkPropertyDefinitions(ReviewProjectExt.class, requiredProperties, values);
    }

    @Test
    public void testAddReview() {
        long timestamp = System.currentTimeMillis();
        long timestamp1 = System.currentTimeMillis() + 7364L;
        long timestamp2 = System.currentTimeMillis() + 28454545L;
        ReviewProjectExt ext = new ReviewProjectExt();
        ext.setRatingStyle(ProjectRatingStyle.FIVE_STATES);
        ReviewEntry entry = new ReviewEntry(ProjectRating.FACE_CRYING, "crying", "homer", timestamp);
        ReviewEntry entry1 = new ReviewEntry(ProjectRating.FACE_PLAIN, "plain", "marge", timestamp1);
        ReviewEntry entry2 = new ReviewEntry(ProjectRating.FACE_SMILE, "smile", "lise", timestamp2);
        ext.addReview(entry);
        ext.addReview(entry1);
        ext.addReview(entry2);
        List<ReviewEntry> reviews = ext.getReviews();
        Assert.assertEquals(entry, reviews.get(0));
        Assert.assertEquals(entry1, reviews.get(1));
        Assert.assertEquals(entry2, reviews.get(1));
        Assert.assertEquals(ProjectRating.FACE_PLAIN, ext.getAverageRating());
        Assert.assertEquals(3, ext.getNumberVotes());
        Assert.assertEquals(1, ext.getNumberThumbsUp());
        Assert.assertEquals(1, ext.getNumberThumbsDown());
        Assert.assertEquals("33%", ext.getRecommendedRatio());
    }
}
