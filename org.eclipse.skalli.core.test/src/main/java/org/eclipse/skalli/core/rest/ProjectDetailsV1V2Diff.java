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
package org.eclipse.skalli.core.rest;

import static org.junit.Assert.fail;

import java.text.MessageFormat;
import java.util.Set;
import java.util.regex.Pattern;

import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceConstants;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.NodeDetail;
import org.eclipse.skalli.commons.CollectionUtils;
import org.w3c.dom.Node;

@SuppressWarnings("nls")
public class ProjectDetailsV1V2Diff extends ProjectsV1V2Diff implements DifferenceListener {

    private final Pattern MEMBER_XPATH_PATTERN = getPattern("/members\\[1\\]/member\\[\\d+\\]/link\\[1\\]/@href");
    private final Pattern PEOPLE_EXT_LEADS_XPATH_PATTERN = getExtPattern("/people\\[1\\]/leads\\[1\\]/lead\\[\\d+\\]/link\\[1\\]/@href");
    private final Pattern PEOPLE_EXT_MEMBERS_XPATH_PATTERN = getExtPattern("/people\\[1\\]/members\\[1\\]/member\\[\\d+\\]/link\\[1\\]/@href");
    private final Pattern INFO_EXT_XPATH_PATTERN = getExtPattern("/info\\[1\\]");
    private final Pattern INFO_EXT_MAILINGLISTS_XPATH_PATTERN = getExtPattern("/info\\[1\\]/mailingLists\\[1\\]");
    private final Pattern DEVINF_EXT_XPATH_PATTERN = getExtPattern("/devInf\\[1\\]");
    private final Pattern DEVINF_EXT_JAVADOCS_XPATH_PATTERN = getExtPattern("/devInf\\[1\\]/javadocs\\[1\\]");
    private final Pattern DEVINF_EXT_SCMLOCATIONS_XPATH_PATTERN = getExtPattern("/devInf\\[1\\]/scmLocations\\[1\\]");
    private final Pattern MAVEN_EXT_COORDINATE_XPATH_PATTERN = getExtPattern("/mavenReactor\\[1\\]/coordinate\\[1\\]");
    private final Pattern MAVEN_EXT_VERSIONS_XPATH_PATTERN = getExtPattern("/mavenReactor\\[1\\]/coordinate\\[1\\]/versions\\[1\\]");
    private final Pattern MAVEN_EXT_PACKAGING_XPATH_PATTERN = getExtPattern("/mavenReactor\\[1\\]/coordinate\\[1\\]/packaging\\[1\\]");
    private final Pattern MAVEN_EXT_MODULE_XPATH_PATTERN = getExtPattern("/mavenReactor\\[1\\]/modules\\[1\\]/module\\[\\d+\\]");
    private final Pattern MAVEN_EXT_MODULE_VERSIONS_PATTERN = getExtPattern("/mavenReactor\\[1\\]/modules\\[1\\]/module\\[\\d+\\]/versions\\[1\\]");
    private final Pattern MAVEN_EXT_MODULE_PACKAGING_XPATH_PATTERN = getExtPattern("/mavenReactor\\[1\\]/modules\\[1\\]/module\\[\\d+\\]/packaging\\[1\\]");
    private final Pattern RELATED_EXT_XPATH_PATTERN = getPattern("/extensions\\[1\\]/relatedProjects\\[1\\]");
    private final Pattern RELATED_EXT_CALCULATED_XPATH_PATTERN = getExtPattern("/relatedProjects\\[1\\]/calculated\\[1\\]");
    private final Pattern RELATED_EXT_LINK_XPATH_PATTERN = getExtPattern("/relatedProjects\\[1\\]/link\\[\\d+\\]");
    private final Pattern REVIEWS_EXT_XPATH_PATTERN = getExtPattern("/reviews\\[1\\]");
    private final Pattern REVIEWS_EXT_REVIEW_XPATH_PATTERN = getExtPattern("/reviews\\[1\\]/review\\[\\d+\\]");
    private final Pattern REVIEWS_EXT_STYLE_XPATH_PATTERN = getExtPattern("/reviews\\[1\\]/ratingStyle\\[1\\]");
    private final Pattern REVIEWS_EXT_ANONYMOUS_XPATH_PATTERN = getExtPattern("/reviews\\[1\\]/allowAnonymous\\[1\\]");
    private final Pattern REVIEWS_EXT_NUMBERVOTES_XPATH_PATTERN = getExtPattern("/reviews\\[1\\]/numberVotes\\[1\\]");
    private final Pattern REVIEWS_EXT_NUMBERUPS_XPATH_PATTERN = getExtPattern("/reviews\\[1\\]/numberThumbsUp\\[1\\]");
    private final Pattern REVIEWS_EXT_NUMBERDNS_XPATH_PATTERN = getExtPattern("/reviews\\[1\\]/numberThumbsDown\\[1\\]");
    private final Pattern REVIEWS_EXT_AVGRATING_XPATH_PATTERN = getExtPattern("/reviews\\[1\\]/averageRating\\[1\\]");
    private final Pattern REVIEWS_EXT_COMMENT_TEXT_XPATH_PATTERN = getExtPattern("/reviews\\[1\\]/review\\[\\d+\\]/comment\\[1\\]/text\\(\\)\\[1\\]");
    private final Pattern SCRUM_EXT_XPATH_PATTERN = getExtPattern("/scrum\\[1\\]");
    private final Pattern SCRUM_EXT_SCRUMMASTERAXPATH_PATTERN = getExtPattern("/scrum\\[1\\]/scrumMasters\\[1\\]");
    private final Pattern SCRUM_EXT_PRODUCTOWNERS_XPATH_PATTERN = getExtPattern("/scrum\\[1\\]/productOwners\\[1\\]");

    private static final Set<String> ADDITIONAL_REVIEW_TAGS = CollectionUtils.asSet("ratingStyle", "allowAnonymous",
            "numberVotes", "numberThumbsUp", "numberThumbsDown", "averageRating");

    private final Pattern USER_PATTERN;
    private final Pattern USERS_PATTERN;

    public ProjectDetailsV1V2Diff(String webLocator) {
        super(webLocator);
        USER_PATTERN = Pattern.compile("^" + webLocator + "/api/user/.+$");
        USERS_PATTERN = Pattern.compile("^" + webLocator + "/api/users/.+$");
    }

    @Override
    public int differenceFound(Difference difference) {
        int result = super.differenceFound(difference);
        if (result == RETURN_ACCEPT_DIFFERENCE) {
            NodeDetail expected = difference.getControlNodeDetail();
            NodeDetail actual = difference.getTestNodeDetail();
            switch (difference.getId()) {
            case DifferenceConstants.ATTR_VALUE_ID:
                // path prefix of the user API has been changed from /api/user to /api/users:
                // all <link> tags to the user API have changed from v1 to v2; this affects
                // the <members> section and the <people> extension
                if (equalsAndMatchesAnyXPath(expected, actual, MEMBER_XPATH_PATTERN,
                        PEOPLE_EXT_LEADS_XPATH_PATTERN, PEOPLE_EXT_MEMBERS_XPATH_PATTERN)
                        && USER_PATTERN.matcher(expected.getValue()).matches()
                        && USERS_PATTERN.matcher(actual.getValue()).matches()) {
                    result = RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                }
                break;
            case DifferenceConstants.HAS_CHILD_NODES_ID:
                if (equalsAndMatchesAnyXPath(expected, actual, INFO_EXT_XPATH_PATTERN)
                        && hasEmptyChildNode(actual, "mailingLists")) {
                    // collection-like tags are always rendered in the v2 API even if empty;
                    // in the v1 API they were not rendered at all
                    result = RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                } else if (equalsAndMatchesAnyXPath(expected, actual, DEVINF_EXT_XPATH_PATTERN)
                        && hasEmptyChildNode(actual, "scmLocations")) {
                    // collection-like tags are always rendered in the v2 API even if empty;
                    // in the v1 API they were not rendered at all
                    result = RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                } else if (equalsAndMatchesAnyXPath(expected, actual, SCRUM_EXT_XPATH_PATTERN)
                        && hasAnyChildNode(actual, CollectionUtils.asSet("scrumMasters", "productOwners"))) {
                    // the v1 API did not rendered the scrum masters and product owners at all;
                    // the v2 API renders these lists similiar to the <people> extension (userId + link)
                    result = RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                } else if (equalsAndMatchesAnyXPath(expected, actual, RELATED_EXT_XPATH_PATTERN)
                        && hasBooleanChildNode(actual, "calculated")) {
                    // the v1 API rendered the <calculated> tag only in the case the value is true;
                    // the v2 API renders this tag always with the corresponding value
                    result = RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                } else if (equalsAndMatchesAnyXPath(expected, actual, REVIEWS_EXT_XPATH_PATTERN)
                        && hasAnyChildNode(actual, ADDITIONAL_REVIEW_TAGS)) {
                    // the v2 API renders several additional tags (see ADDITIONAL_REVIEW_TAGS)
                    // that are not present in the v1 API
                    result = RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                }
                break;
            case DifferenceConstants.CHILD_NODELIST_LENGTH_ID:
                if (equalsAndMatchesAnyXPath(expected, actual, INFO_EXT_XPATH_PATTERN)
                        && hasEmptyChildNode(actual, "mailingLists")) {
                    // collection-like tags are always rendered in the v2 API even if empty;
                    // in the v1 API they were not rendered at all
                    result = RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                } else if (equalsAndMatchesAnyXPath(expected, actual, DEVINF_EXT_XPATH_PATTERN)
                        && hasEmptyChildNode(actual, "javadocs")) {
                    // collection-like tags are always rendered in the v2 API even if empty;
                    // in the v1 API they were not rendered at all
                    result = RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                } else if (equalsAndMatchesAnyXPath(expected, actual, MAVEN_EXT_COORDINATE_XPATH_PATTERN, MAVEN_EXT_MODULE_XPATH_PATTERN)
                        && hasEmptyChildNode(actual, "versions")) {
                    // collection-like tags are always rendered in the v2 API even if empty;
                    // in the v1 API they were not rendered at all
                    result = RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                } else if (equalsAndMatchesAnyXPath(expected, actual, SCRUM_EXT_XPATH_PATTERN)
                        && hasAnyChildNode(actual, CollectionUtils.asSet("scrumMasters", "productOwners"))) {
                    // the v1 API did not rendered the scrum masters and product owners at all;
                    // the v2 API renders these lists similiar to the <people> extension (userId + link)
                    result = RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                } else if (equalsAndMatchesAnyXPath(expected, actual, RELATED_EXT_XPATH_PATTERN)
                        && hasBooleanChildNode(actual, "calculated")) {
                    // the v1 API rendered the <calculated> tag only in the case the value is true;
                    // the v2 API renders this tag always with the corresponding value
                    result = RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                } else if (equalsAndMatchesAnyXPath(expected, actual, REVIEWS_EXT_XPATH_PATTERN)
                        && hasAnyChildNode(actual, ADDITIONAL_REVIEW_TAGS)) {
                    // the v2 API renders several additional tags (see ADDITIONAL_REVIEW_TAGS)
                    // that are not present in the v1 API
                    result = RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                }
                break;
            case DifferenceConstants.CHILD_NODE_NOT_FOUND_ID:
                if (matchesAnyXPath(actual, INFO_EXT_MAILINGLISTS_XPATH_PATTERN)
                        && "mailingLists".equals(actual.getValue())) {
                    // collection-like tags are always rendered in the v2 API even if empty;
                    // in the v1 API they were not rendered at all
                    result = RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                } else if (matchesAnyXPath(actual, DEVINF_EXT_JAVADOCS_XPATH_PATTERN)
                        && "javadocs".equals(actual.getValue())) {
                    // collection-like tags are always rendered in the v2 API even if empty;
                    // in the v1 API they were not rendered at all
                    result = RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                } else if (matchesAnyXPath(actual, DEVINF_EXT_SCMLOCATIONS_XPATH_PATTERN)
                        && "scmLocations".equals(actual.getValue())) {
                    // collection-like tags are always rendered in the v2 API even if empty;
                    // in the v1 API they were not rendered at all
                    result = RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                } else if (matchesAnyXPath(actual, MAVEN_EXT_VERSIONS_XPATH_PATTERN, MAVEN_EXT_MODULE_VERSIONS_PATTERN)
                        && "versions".equals(actual.getValue())) {
                    // collection-like tags are always rendered in the v2 API even if empty;
                    // in the v1 API they were not rendered at all
                    result = RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                } else if (matchesAnyXPath(actual, SCRUM_EXT_SCRUMMASTERAXPATH_PATTERN)
                        && "scrumMasters".equals(actual.getValue())) {
                    // the v1 API did not rendered the scrum masters and product owners at all;
                    // the v2 API renders these lists similiar to the <people> extension (userId + link)
                    result = RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                }  else if (matchesAnyXPath(actual, SCRUM_EXT_PRODUCTOWNERS_XPATH_PATTERN)
                        && "productOwners".equals(actual.getValue())) {
                    // the v1 API did not rendered the scrum masters and product owners at all;
                    // the v2 API renders these lists similiar to the <people> extension (userId + link)
                    result = RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                } else if (matchesAnyXPath(actual, RELATED_EXT_CALCULATED_XPATH_PATTERN)
                        && "calculated".equals(actual.getValue())) {
                    // the v1 API rendered the <calculated> tag only in the case the value is true;
                    // the v2 API renders this tag always with the corresponding value
                    result = RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                }  else if (matchesAnyXPath(actual, REVIEWS_EXT_STYLE_XPATH_PATTERN, REVIEWS_EXT_ANONYMOUS_XPATH_PATTERN,
                        REVIEWS_EXT_NUMBERVOTES_XPATH_PATTERN, REVIEWS_EXT_NUMBERUPS_XPATH_PATTERN,
                        REVIEWS_EXT_NUMBERDNS_XPATH_PATTERN, REVIEWS_EXT_AVGRATING_XPATH_PATTERN)
                        && ADDITIONAL_REVIEW_TAGS.contains(actual.getValue())) {
                    // the v2 API renders several additional tags (see ADDITIONAL_REVIEW_TAGS)
                    // that are not present in the v1 API
                    result = RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                }
                break;
            case DifferenceConstants.CHILD_NODELIST_SEQUENCE_ID:
                if (equalsAndMatchesAnyXPath(expected, actual, MAVEN_EXT_PACKAGING_XPATH_PATTERN,
                        MAVEN_EXT_MODULE_PACKAGING_XPATH_PATTERN, RELATED_EXT_LINK_XPATH_PATTERN)) {
                    result = RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                } else if (equalsAndMatchesAnyXPath(expected, actual, REVIEWS_EXT_REVIEW_XPATH_PATTERN)) {
                    result = RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                }
                break;
            case DifferenceConstants.TEXT_VALUE_ID:
                // old and new API render different file endings: the new API renders a single \n #xA),
                // while the old API preferred \r\n (#xD #xA)
                if (equalsAndMatchesAnyXPath(expected, actual, REVIEWS_EXT_COMMENT_TEXT_XPATH_PATTERN)
                        && equalsValueIgnoreLineEndings(expected, actual)) {
                    result = RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                };
                break;
            default:
                result = RETURN_ACCEPT_DIFFERENCE;
            }
        }
        return result;
    }

    @Override
    public void skippedComparison(Node expected, Node actual) {
        fail(MessageFormat.format(
                "comparison skipped because the node types are not comparable: {0} (type: {1}) - {2} (type: {3})",
                expected.getNodeName(), expected.getNodeType(), actual.getNodeName(), actual.getNodeType()));

    }

    @Override
    protected String getRootPath() {
        return "";
    }
}
