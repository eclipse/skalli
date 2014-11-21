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
package org.eclipse.skalli.model.ext.misc.internal;

import org.eclipse.skalli.model.ext.misc.ProjectRating;
import org.eclipse.skalli.model.ext.misc.ProjectRatingStyle;
import org.eclipse.skalli.model.ext.misc.ReviewEntry;
import org.eclipse.skalli.model.ext.misc.ReviewProjectExt;
import org.eclipse.skalli.services.rest.RestWriter;
import org.eclipse.skalli.testutil.RestWriterTestBase;
import org.junit.Test;

@SuppressWarnings("nls")
public class ReviewConverterTest extends RestWriterTestBase {

    private static final long NOW = System.currentTimeMillis();

    @Test
    public void testMarshalBlankExtensionXML() throws Exception {
        ReviewProjectExt reviews = new ReviewProjectExt();
        RestWriter restWriter = getRestWriterXML();
        marshalRelatedProjects(reviews, restWriter);
        assertEqualsXML("<reviews>"
                +"<ratingStyle>TWO_STATES</ratingStyle>"
                +"<allowAnonymous>false</allowAnonymous>"
                +"<numberVotes>0</numberVotes>"
                +"<numberThumbsUp>0</numberThumbsUp>"
                +"<numberThumbsDown>0</numberThumbsDown>"
                +"<averageRating>NONE</averageRating>"
                + "</reviews>");
    }

    @Test
    public void testMarshalExtensionXML() throws Exception {
        ReviewProjectExt reviews = newReviews();
        RestWriter restWriter = getRestWriterXML();
        marshalRelatedProjects(reviews, restWriter);
        assertEqualsXML("<reviews>"
                +"<ratingStyle>FIVE_STATES</ratingStyle>"
                +"<allowAnonymous>true</allowAnonymous>"
                +"<numberVotes>3</numberVotes>"
                +"<numberThumbsUp>1</numberThumbsUp>"
                +"<numberThumbsDown>1</numberThumbsDown>"
                +"<averageRating>FACE_PLAIN</averageRating>"
                + "<review><voter>homer</voter><comment>foo</comment>"
                + "<timestamp>" + NOW + "</timestamp><rating>FACE_CRYING</rating></review>"
                + "<review><voter>marge</voter><comment>bar</comment>"
                + "<timestamp>" + (NOW+10) + "</timestamp><rating>FACE_PLAIN</rating></review>"
                + "<review><voter>bart</voter><comment>foobar</comment>"
                + "<timestamp>" + (NOW+20) + "</timestamp><rating>FACE_SMILE</rating></review>"
                + "</reviews>");
    }

    @Test
    public void testMarshalBlankExtensionJSON() throws Exception {
        ReviewProjectExt reviews = new ReviewProjectExt();
        RestWriter restWriter = getRestWriterJSON();
        marshalRelatedProjects(reviews, restWriter);
        assertEqualsJSON("{"
                + "\"ratingStyle\":\"TWO_STATES\""
                + ",\"allowAnonymous\":false"
                + ",\"numberVotes\":0"
                + ",\"numberThumbsUp\":0"
                + ",\"numberThumbsDown\":0"
                + ",\"averageRating\":\"NONE\""
                + ",\"items\":[]}");
    }

    @Test
    public void testMarshalExtensionJSON() throws Exception {
        ReviewProjectExt reviews = newReviews();
        RestWriter restWriter = getRestWriterJSON();
        marshalRelatedProjects(reviews, restWriter);
        assertEqualsJSON("{"
                + "\"ratingStyle\":\"FIVE_STATES\""
                + ",\"allowAnonymous\":true"
                + ",\"numberVotes\":3"
                + ",\"numberThumbsUp\":1"
                + ",\"numberThumbsDown\":1"
                + ",\"averageRating\":\"FACE_PLAIN\""
                + ",\"items\":["
                + "{\"voter\":\"homer\",\"comment\":\"foo\","
                + "\"timestamp\":" + NOW + ",\"rating\":\"FACE_CRYING\"},"
                + "{\"voter\":\"marge\",\"comment\":\"bar\","
                + "\"timestamp\":" + (NOW+10) + ",\"rating\":\"FACE_PLAIN\"},"
                + "{\"voter\":\"bart\",\"comment\":\"foobar\","
                + "\"timestamp\":" + (NOW+20) + ",\"rating\":\"FACE_SMILE\"}"
                + "]}");
    }

    private ReviewProjectExt newReviews() {
        ReviewProjectExt reviews = new ReviewProjectExt();
        reviews.setAllowAnonymous(true);
        reviews.setRatingStyle(ProjectRatingStyle.FIVE_STATES);
        reviews.addReview(new ReviewEntry(ProjectRating.FACE_CRYING, "foo", "homer", NOW));
        reviews.addReview(new ReviewEntry(ProjectRating.FACE_PLAIN, "bar", "marge", NOW+10));
        reviews.addReview(new ReviewEntry(ProjectRating.FACE_SMILE, "foobar", "bart", NOW+20));
        return reviews;
    }

    private void marshalRelatedProjects(ReviewProjectExt reviews, RestWriter restWriter) throws Exception {
        ReviewConverter converter = new ReviewConverter();
        restWriter.object("reviews");
        converter.marshal(reviews, restWriter);
        restWriter.end();
        restWriter.flush();
    }
}
