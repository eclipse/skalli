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
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceConstants;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.NodeDetail;
import org.eclipse.skalli.commons.ComparatorUtils;
import org.w3c.dom.Node;

@SuppressWarnings("nls")
public class ProjectsV1V2Diff implements DifferenceListener {

    private static final Pattern PROJECT_XPATH_PATTERN = getPattern("");
    private static final Pattern LINK_XPATH_PATTERN = getPattern("/link\\[\\d+\\]");
    private static final Pattern LINK_HREF_XPATH_PATTERN = getPattern("/link\\[\\d+\\]/@href");
    private static final Pattern LINK_REL_XPATH_PATTERN = getPattern("/link\\[\\d+\\]/@rel");
    private static final Pattern PHASE_XPATH_PATTERN = getPattern("/phase\\[1\\]");
    private static final Pattern REGISTERED_XPATH_PATTERN = getPattern("/registered\\[1\\]");
    private static final Pattern DESCRIPTION_XPATH_PATTERN = getPattern("/description\\[1\\]");
    private static final Pattern DESCRIPTION_TEXT_XPATH_PATTERN = getPattern("/description\\[1\\]/text\\(\\)\\[1\\]");
    private static final Pattern SUBPROJECTS_XPATH_PATTERN = getPattern("/subprojects\\[1\\]");
    private static final Pattern EXTENSIONS_XPATH_PATTERN = getPattern("/extensions\\[1\\]");
    private static final Pattern MEMBERS_XPATH_PATTERN = getPattern("/members\\[1\\]");

    @Override
    public int differenceFound(Difference difference) {
        boolean identical = false;
        NodeDetail expected = difference.getControlNodeDetail();
        NodeDetail actual = difference.getTestNodeDetail();
        switch (difference.getId()) {
        case DifferenceConstants.ELEMENT_NUM_ATTRIBUTES_ID:
            // <project> tag has an additional "lastModifiedMillis" attribute in the new API
            if (equalsAndMatchesXPath(expected, actual, PROJECT_XPATH_PATTERN)
                    && equalsValueInt(expected, 3)
                    && equalsValueInt(actual, 4)) {
                identical = true;
            }
            break;
        case DifferenceConstants.ATTR_NAME_NOT_FOUND_ID:
            // <project> tag has an additional "lastModifiedMillis" attribute in the new API
            if (equalsAndMatchesXPath(expected, actual, PROJECT_XPATH_PATTERN)
                    && equalsValueNull(expected)
                    && "lastModifiedMillis".equals(actual.getValue())) {
                identical = true;
            };
            break;
        case DifferenceConstants.CHILD_NODELIST_LENGTH_ID:
            // <project> tag has always an additional <link rel=permalink> in the new API, but never in the old API;
            // <project> tag has always a <link rel=subprojects> in the new API,
            // but only if it has also a <subprojects> tag in the old API;
            // <project> tag has always a <members> tag in the new API (even if empty),
            // but only if it was non-empty in the old API;
            // <project> tag has always a <subprojects> tag in the new API (even if empty),
            // but only if it was non-empty in the old API;
            // therefore, we have at least 2 additional tags, but never more than 4
            if (equalsAndMatchesXPath(expected, actual, PROJECT_XPATH_PATTERN)
                    && (valueToInt(actual) >= valueToInt(expected) + 2)
                    && (valueToInt(actual) <= valueToInt(expected) + 4)) {
                identical = true;
            }
            break;
        case DifferenceConstants.CHILD_NODELIST_SEQUENCE_ID:
            // some tags have changed their position in the new API compared to the old API, e.g.
            // all <link> tags are now grouped together, and the <phase>, <registered> and <description>
            // tags are now  rendered before the links.
            if (equalsAndMatchesXPath(expected, actual, LINK_XPATH_PATTERN)
                    || equalsAndMatchesXPath(expected, actual, PHASE_XPATH_PATTERN)
                    || equalsAndMatchesXPath(expected, actual, REGISTERED_XPATH_PATTERN)
                    || equalsAndMatchesXPath(expected, actual, DESCRIPTION_XPATH_PATTERN)
                    || equalsAndMatchesXPath(expected, actual, MEMBERS_XPATH_PATTERN)
                    || equalsAndMatchesXPath(expected, actual, SUBPROJECTS_XPATH_PATTERN)
                    || equalsAndMatchesXPath(expected, actual, EXTENSIONS_XPATH_PATTERN)) {
                identical = true;
            }
            break;
        case DifferenceConstants.ATTR_VALUE_ID:
            // The <link> tags have different ordering and position within the <project> tag
            if (equalsAndMatchesXPath(expected, actual, LINK_HREF_XPATH_PATTERN)
                    || equalsAndMatchesXPath(expected, actual, LINK_REL_XPATH_PATTERN)) {
                identical = true;
            }
            break;
        case DifferenceConstants.CHILD_NODE_NOT_FOUND_ID:
            // in the new API we may have additional <link>, <subprojects> and <members> tags,
            // which may no be there in the old API
            if (equalsValueNull(expected) && (
                    matchesXPath(actual, LINK_XPATH_PATTERN) && "link".equals(actual.getValue())
                 || matchesXPath(actual, SUBPROJECTS_XPATH_PATTERN) && "subprojects".equals(actual.getValue())
                 || matchesXPath(actual, MEMBERS_XPATH_PATTERN) && "members".equals(actual.getValue()))) {
                identical = true;
            }
            break;
        case DifferenceConstants.TEXT_VALUE_ID:
            // old and new API render different file endings: the new API renders a single \n #xA),
            // while the old API preferred \r\n (#xD #xA)
            if (equalsAndMatchesXPath(expected, actual, DESCRIPTION_TEXT_XPATH_PATTERN)
                    && equalsValueIgnoreLineEndings(expected, actual)) {
                identical = true;
            };
            break;
        default:
            identical = false;
        }
        return identical? RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL : RETURN_ACCEPT_DIFFERENCE;
    }

    @Override
    public void skippedComparison(Node expected, Node actual) {
        fail(MessageFormat.format(
                "comparison skipped because the node types are not comparable: {0} (type: {1}) - {2} (type: {3})",
                expected.getNodeName(), expected.getNodeType(), actual.getNodeName(), actual.getNodeType()));
    }

    private static Pattern getPattern(String relPath) {
        return Pattern.compile(MessageFormat.format("^/projects\\[1\\]/project\\[\\d+\\]{0}$", relPath));
    }

    private boolean equalsXPath(NodeDetail expected, NodeDetail actual) {
        return ComparatorUtils.equals(expected.getXpathLocation(), actual.getXpathLocation());
    }

    private boolean matchesXPath(NodeDetail node, Pattern xPathPattern) {
        return xPathPattern.matcher(node.getXpathLocation()).matches();
    }

    private boolean equalsAndMatchesXPath(NodeDetail expected, NodeDetail actual, Pattern xPathPattern) {
        return equalsXPath(expected, actual)
                && matchesXPath(expected, xPathPattern)
                && matchesXPath(actual, xPathPattern);
    }

    private boolean equalsValueInt(NodeDetail node, int value) {
        return isValueInt(node) && valueToInt(node) == value;
    }

    private boolean isValueInt(NodeDetail node) {
        return NumberUtils.isNumber(node.getValue());
    }

    private int valueToInt(NodeDetail node) {
         return NumberUtils.toInt(node.getValue());
    }

    private boolean equalsValueNull(NodeDetail node) {
        return "null".equals(node.getValue());
    }

    private boolean equalsValueIgnoreLineEndings(NodeDetail expected, NodeDetail actual) {
        return normalized(expected).equals(normalized(actual));
    }

    private String normalized(NodeDetail node) {
        return StringUtils.replace(node.getValue(), "\r\n", "\n");
    }
}