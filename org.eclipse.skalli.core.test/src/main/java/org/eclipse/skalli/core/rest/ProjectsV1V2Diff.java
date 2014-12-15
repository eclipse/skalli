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

    protected final Pattern ROOT_XPATH_PATTERN = getPattern("");
    protected final Pattern EXTENSION_XPATH_PATTERN = getPattern("/extensions\\[1\\]/.+\\[1\\]");
    protected final Pattern LINK_XPATH_PATTERN = getPattern("/link\\[\\d+\\]");
    protected final Pattern LINK_HREF_XPATH_PATTERN = getPattern("/link\\[\\d+\\]/@href");
    protected final Pattern LINK_REL_XPATH_PATTERN = getPattern("/link\\[\\d+\\]/@rel");
    protected final Pattern PHASE_XPATH_PATTERN = getPattern("/phase\\[1\\]");
    protected final Pattern REGISTERED_XPATH_PATTERN = getPattern("/registered\\[1\\]");
    protected final Pattern DESCRIPTION_XPATH_PATTERN = getPattern("/description\\[1\\]");
    protected final Pattern DESCRIPTION_TEXT_XPATH_PATTERN = getPattern("/description\\[1\\]/text\\(\\)\\[1\\]");
    protected final Pattern SUBPROJECTS_XPATH_PATTERN = getPattern("/subprojects\\[1\\]");
    protected final Pattern EXTENSIONS_XPATH_PATTERN = getPattern("/extensions\\[1\\]");
    protected final Pattern MEMBERS_XPATH_PATTERN = getPattern("/members\\[1\\]");

    protected final String webLocator;

    public ProjectsV1V2Diff(String webLocator) {
        this.webLocator = webLocator;
    }

    @Override
    public int differenceFound(Difference difference) {
        int result = RETURN_ACCEPT_DIFFERENCE;
        NodeDetail expected = difference.getControlNodeDetail();
        NodeDetail actual = difference.getTestNodeDetail();
        switch (difference.getId()) {
        case DifferenceConstants.ELEMENT_NUM_ATTRIBUTES_ID:
            // <project> tag has an additional "lastModifiedMillis" attribute in the new API
            if (equalsAndMatchesAnyXPath(expected, actual, ROOT_XPATH_PATTERN, EXTENSION_XPATH_PATTERN)
                    && (valueToInt(actual) == valueToInt(expected) + 1)) {
                result = RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
            }
            break;
        case DifferenceConstants.ATTR_NAME_NOT_FOUND_ID:
            // <project> tag has an additional "lastModifiedMillis" attribute in the new API
            if (equalsAndMatchesAnyXPath(expected, actual, ROOT_XPATH_PATTERN, EXTENSION_XPATH_PATTERN)
                    && equalsValueNull(expected)
                    && "lastModifiedMillis".equals(actual.getValue())) {
                result = RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
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
            if (equalsAndMatchesAnyXPath(expected, actual, ROOT_XPATH_PATTERN)
                    && (valueToInt(actual) >= valueToInt(expected) + 2)
                    && (valueToInt(actual) <= valueToInt(expected) + 4)) {
                result = RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
            }
            break;
        case DifferenceConstants.CHILD_NODELIST_SEQUENCE_ID:
            // some tags have changed their position in the new API compared to the old API, e.g.
            // all <link> tags are now grouped together, and the <phase>, <registered> and <description>
            // tags are now  rendered before the links.
            if (equalsAndMatchesAnyXPath(expected, actual, LINK_XPATH_PATTERN, PHASE_XPATH_PATTERN,
                    REGISTERED_XPATH_PATTERN, DESCRIPTION_XPATH_PATTERN, MEMBERS_XPATH_PATTERN,
                    SUBPROJECTS_XPATH_PATTERN, EXTENSIONS_XPATH_PATTERN)) {
                result = RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
            }
            break;
        case DifferenceConstants.ATTR_VALUE_ID:
            // The <link> tags have different ordering and position within the <project> tag
            if (equalsAndMatchesAnyXPath(expected, actual, LINK_HREF_XPATH_PATTERN,LINK_REL_XPATH_PATTERN)) {
                result = RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
            }
            break;
        case DifferenceConstants.CHILD_NODE_NOT_FOUND_ID:
            // in the new API we may have additional <link>, <subprojects> and <members> tags,
            // which may no be there in the old API
            if (equalsValueNull(expected) && (
                    matchesAnyXPath(actual, LINK_XPATH_PATTERN) && "link".equals(actual.getValue())
                 || matchesAnyXPath(actual, SUBPROJECTS_XPATH_PATTERN) && "subprojects".equals(actual.getValue())
                 || matchesAnyXPath(actual, MEMBERS_XPATH_PATTERN) && "members".equals(actual.getValue()))) {
                result = RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
            }
            break;
        case DifferenceConstants.TEXT_VALUE_ID:
            // old and new API render different file endings: the new API renders a single \n #xA),
            // while the old API preferred \r\n (#xD #xA)
            if (equalsAndMatchesAnyXPath(expected, actual, DESCRIPTION_TEXT_XPATH_PATTERN)
                    && equalsValueIgnoreLineEndings(expected, actual)) {
                result = RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
            };
            break;
        default:
            result = RETURN_ACCEPT_DIFFERENCE;
        }
        return result;
    }

    @Override
    public void skippedComparison(Node expected, Node actual) {
        fail(MessageFormat.format(
                "comparison skipped because the node types are not comparable: {0} (type: {1}) - {2} (type: {3})",
                expected.getNodeName(), expected.getNodeType(), actual.getNodeName(), actual.getNodeType()));
    }

    protected String getRootPath() {
        return "/projects\\[1\\]";
    }

    protected Pattern getPattern(String relPath) {
        return Pattern.compile(MessageFormat.format("^{0}/project\\[\\d+\\]{1}$", getRootPath(), relPath));
    }

    protected Pattern getExtPattern(String relPath) {
        return Pattern.compile(MessageFormat.format("^{0}/project\\[\\d+\\]/extensions\\[1\\]{1}$", getRootPath(), relPath));
    }

    protected boolean equalsXPath(NodeDetail expected, NodeDetail actual) {
        return ComparatorUtils.equals(expected.getXpathLocation(), actual.getXpathLocation());
    }

    protected boolean matchesAnyXPath(NodeDetail node, Pattern... xPathPatterns) {
        for (Pattern xPathPattern: xPathPatterns) {
            if (xPathPattern.matcher(node.getXpathLocation()).matches()) {
                return true;
            }
        }
        return false;
    }

    protected boolean equalsAndMatchesAnyXPath(NodeDetail expected, NodeDetail actual, Pattern... xPathPatterns) {
        for (Pattern xPathPattern : xPathPatterns) {
            if (equalsXPath(expected, actual)
                    && matchesAnyXPath(expected, xPathPattern)
                    && matchesAnyXPath(actual, xPathPattern)) {
                return true;
            }
        }
        return false;
    }

    protected boolean equalsValueInt(NodeDetail node, int value) {
        return isValueInt(node) && valueToInt(node) == value;
    }

    protected boolean isValueInt(NodeDetail node) {
        return NumberUtils.isNumber(node.getValue());
    }

    protected int valueToInt(NodeDetail node) {
         return NumberUtils.toInt(node.getValue());
    }

    protected boolean equalsValueNull(NodeDetail node) {
        return "null".equals(node.getValue());
    }

    protected boolean equalsValueIgnoreLineEndings(NodeDetail expected, NodeDetail actual) {
        return normalized(expected).equals(normalized(actual));
    }

    protected String normalized(NodeDetail node) {
        return StringUtils.replace(node.getValue(), "\r\n", "\n");
    }

    protected boolean hasEmptyChildNode(NodeDetail nodeDetails, String name) {
        Node node = nodeDetails.getNode().getFirstChild();
        while (node != null) {
            if (name.equals(node.getNodeName())) {
                return !node.hasChildNodes();
            }
            node = node.getNextSibling();
        }
        return false;
    }

    protected boolean hasBooleanChildNode(NodeDetail nodeDetails, String name) {
        Node node = nodeDetails.getNode().getFirstChild();
        while (node != null) {
            if (name.equals(node.getNodeName())) {
                return "true".equals(node.getTextContent()) || "false".equals(node.getTextContent());
            }
            node = node.getNextSibling();
        }
        return false;
    }

    protected boolean hasAnyChildNode(NodeDetail nodeDetails, Set<String> names) {
        Node node = nodeDetails.getNode().getFirstChild();
        while (node != null) {
            if (names.contains(node.getNodeName())) {
                return true;
            }
            node = node.getNextSibling();
        }
        return false;
    }
}