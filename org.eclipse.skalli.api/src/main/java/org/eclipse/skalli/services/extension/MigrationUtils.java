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
package org.eclipse.skalli.services.extension;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MigrationUtils {

    static final String EXTENSIONS_TAGNAME = "extensions"; //$NON-NLS-1$
    static final String NO_COMPARATOR_TAGNAME = "no-comparator"; //$NON-NLS-1$
    static final String DELETED_TAGNAME = "deleted"; //$NON-NLS-1$
    static final String STRINGSET_ENTRY_TAGNAME = "string"; //$NON-NLS-1$

    /**
     * Returns the value of the &lt;uuid&gt; element.
     *
     * @param doc  the XML document.
     *
     * @throws MigrationException  if there is no &lt;uuid&gt; element
     * in the given document, or there are multiple such elements.
     */
    public static String getUuid(Document doc) throws MigrationException {
        Element element = getElementOfEntity(doc, "uuid"); //$NON-NLS-1$
        if (element == null) {
            throw new MigrationException("Document contains no <uuid> element"); //$NON-NLS-1$
        }
        return element.getTextContent();
    }

    /**
     * Returns the root element of a given extension.
     *
     * @param doc  the XML document.
     * @param extensionAlias  the alias of the extension to retrieve.
     * @return the extension matching the given alias, or <code>null</code>
     * if no such extension exists.
     *
     * @throws MigrationException  if there is more than one element matching
     * the given extension alias.
     */
    public static Element getExtension(Document doc, String extensionAlias)
            throws MigrationException {
        Element extensionsElement = getElementOfEntity(doc, EXTENSIONS_TAGNAME);
        return extensionsElement != null ? getChild(extensionsElement, extensionAlias) : null;
    }

    /**
     * Returns the element of the entity with the given <code>elementName</code>.
     *
     * @param doc  the XML document.
     * @param elementName  the name of the element to retrieve.
     * @return the element matching the given name, or <code>null</code>
     * if no such element exists.
     *
     * @throws MigrationException  if the entity has more than one
     * element matching the given element name.
     */
    public static Element getElementOfEntity(Document doc, String elementName)
            throws MigrationException {
        return getChild(doc.getDocumentElement(), elementName);
    }

    /**
     * Returns the element of an extension with the given <code>elementName</code>.
     *
     * @param doc  the XML document.
     * @param extensionAlias  the alias of an extension.
     * @param elementName  the name of the element to retrieve.
     *
     * @return the element matching the given name, or <code>null</code>
     * if no such element exists.
     *
     * @throws MigrationException  if the extension has more than one
     * element matching the given element name.
     */
    public static Element getElementOfExtension(Document doc, String extensionAlias, String elementName)
            throws MigrationException {
        Element extensionElement = getExtension(doc, extensionAlias);
        return extensionElement != null ? getChild(extensionElement, elementName) : null;
    }

    /**
     * Returns the child element with the given <code>elementName</code> of the
     * specified parent element.
     *
     * @param parentElement  the parent element to search for a certain child element.
     * @param elementName  the name of the element to retrieve.
     *
     * @return  the element matching the given name, or <code>null</code> if no
     * such element exists.
     *
     * @throws MigrationException  if the parent element has more than one
     * child element matching the given name.
     */
    public static Element getChild(Element parentElement, String elementName) throws MigrationException {
        if (parentElement== null) {
            throw new IllegalArgumentException("argument 'parentElement' must not be null"); //$NON-NLS-1$
        }
        int count = 0;
        Element result = null;
        NodeList nodes = parentElement.getChildNodes();
        for (int i = 0; i < nodes.getLength(); ++i) {
            Node child = nodes.item(i);
            if (matches(child, elementName)) {
                result = (Element) child;
                ++count;
            }
        }
        if (count > 1) {
            throwInvalidNumberOfElementsFound(parentElement.getNodeName(), elementName, 1, count);
        }
        return result;
    }

    /**
     * Returns the <tt>extensions</tt> element of the entity, or <code>null</code>
     * if there is no such element.
     *
     * @param doc  the XML document.
     * @throws MigrationException  if the entity has more than one
     * <tt>extensions</tt> element.
     */
    public static Element getExtensionsNode(Document doc) throws MigrationException {
        return getElementOfEntity(doc, EXTENSIONS_TAGNAME);
    }

    /**
     * Returns a list of extension elements matching the given aliases.
     *
     * @param doc  the XML document.
     * @param aliases  the aliases to search for.
     * @return  a list of extension elements, or an empty list.
     *
     * @throws MigrationException  if the entity has more than one
     * <tt>extensions</tt> element.
     */
    public static List<Element> getExtensions(Document doc, Set<String> aliases) throws MigrationException {
        List<Element> result = new ArrayList<Element>();
        Element extensions = getElementOfEntity(doc, EXTENSIONS_TAGNAME);
        if (extensions == null) {
            return result;
        }
        NodeList nodes = extensions.getChildNodes();
        for (int i = 0; i < nodes.getLength(); ++i) {
            Node node = nodes.item(i);
            String name = node.getNodeName();
            if (aliases.contains(name)) {
                result.add((Element) node);
            }
        }
        return result;
    }

    /**
     * Returns the <tt>extensions</tt> element of the entity. If the element does
     * not exist, it is created and appended to the entity root element.
     *
     * @param doc  the XML document.
     * @return the <tt>extensions</tt> element of the entity.
     *
     * @throws MigrationException  if the entity has more than one
     * <tt>extensions</tt> element.
     */
    public static Element getOrCreateExtensionsNode(Document doc) throws MigrationException {
        Element extensionsElement = getElementOfEntity(doc, EXTENSIONS_TAGNAME);
        if (extensionsElement == null) {
            extensionsElement = doc.createElement(EXTENSIONS_TAGNAME);
            Element nocompElement = doc.createElement(NO_COMPARATOR_TAGNAME);
            extensionsElement.appendChild(nocompElement);
            Element entity = doc.getDocumentElement();
            entity.appendChild(extensionsElement);
        }
        return extensionsElement;
    }

    /**
     * Returns the root element of the given extension. If there is not yet an element
     * for the extension, a new element is created and attached to the <tt>&lt;extensions&gt;</tt>
     * element of the entity.
     *
     * @param doc  the XML document.
     * @param extensionAlias  the alias of the extension to retrieve or create.
     *
     * @throws MigrationException  if the entity has more than one
     * extension matching the given alias.
     */
    public static Element getOrCreateExtensionNode(Document doc, String extensionAlias) throws MigrationException {
        Element extensionElement = getExtension(doc, extensionAlias);
        if (extensionElement == null) {
            Element extensionsElement = getOrCreateExtensionsNode(doc);
            extensionElement = doc.createElement(extensionAlias);
            Element deletedElement = doc.createElement(DELETED_TAGNAME);
            deletedElement.setTextContent(Boolean.FALSE.toString());
            extensionElement.appendChild(deletedElement);
            extensionsElement.appendChild(extensionElement);
        }
        return extensionElement;
    }

    /**
     * Migrates a string-like node of an enity to a set-like node.
     * Note that the set-like node is only created, if the string-like node exists.
     * The value of the string-like node is added as first entry to the set-like node.
     *
     * @param doc   the XML document.
     * @param elementName  the name of the string-like node.
     * @param setElementName  the name of the set-like node after migration.
     * @param noComparator
     *          if <code>true</code>, a &lt;no-comparator/&gt; element is added to
     *          the set-like node.
     *
     * @throws MigrationException  if the entity has more than one
     * element matching the given <code>elementName</code>.
     *
     */
    public static void migrateStringToStringSet(Document doc, String elementName, String setElementName,
            boolean noComparator)
            throws MigrationException {
        Element element = getElementOfEntity(doc, elementName);
        migrateStringToStringSet(doc, element, setElementName, noComparator);
    }

    /**
     * Migrates a string-like node of an extension to a set-like node.
     * Note that the set-like node is only created, if the string-like node exists.
     * The value of the string-like node is added as first entry to the set-like node.
     *
     * @param doc  the XML document.
     * @param extensionAlias   the alias of an extension.
     * @param elementName   the name of the string-like node.
     * @param setElementName  the name of the set-like node after migration.
     * @param noComparator
     *          if <code>true</code>, a &lt;no-comparator/&gt; node is added to
     *          the set-like node.
     *
     * @throws MigrationException  if the extension has more than one
     * element matching the given <code>elementName</code>.
     */
    public static void migrateStringToStringSet(Document doc, String extensionAlias, String elementName,
            String setElementName, boolean noComparator)
            throws MigrationException {
        Element stringElement = getElementOfExtension(doc, extensionAlias, elementName);
        migrateStringToStringSet(doc, stringElement, setElementName, noComparator);
    }

    private static void migrateStringToStringSet(Document doc, Element stringElement, String setElementName,
            boolean noComparator) {
        if (stringElement != null) {
            String value = stringElement.getTextContent();
            Node parent = stringElement.getParentNode();
            parent.removeChild(stringElement);
            Element setElement = doc.createElement(setElementName);
            if (noComparator) {
                Element setEntry = doc.createElement(NO_COMPARATOR_TAGNAME);
                setElement.appendChild(setEntry);
            }
            Element setEntry = doc.createElement(STRINGSET_ENTRY_TAGNAME);
            setEntry.setTextContent(value);
            setElement.appendChild(setEntry);
            parent.appendChild(setElement);
        }
    }

    /**
     * Renames an element of an entity.
     *
     * @param doc  the XML document.
     * @param elementName  the name of the element to rename.
     * @param newElementName  the new name of the element.
     *
     * @throws MigrationException  if the entity has more than one
     * element matching the given <code>elementName</code>.
     */
    public static void renameTag(Document doc, String elementName, String newElementName)
            throws MigrationException {
        Element element = getElementOfEntity(doc, elementName);
        renameTag(doc, element, newElementName);
    }

    /**
     * Renames an element of an extension.
     *
     * @param doc   the XML document.
     * @param extensionAlias   the alias of an extension.
     * @param elementName  the name of the element to rename.
     * @param newElementName  the new name of the element.
     *
     * @throws MigrationException  if the extension has more than one
     * element matching the given <code>elementName</code>.
     */
    public static void renameTag(Document doc, String extensionAlias, String elementName, String newElementName)
            throws MigrationException {
        Element element = getElementOfExtension(doc, extensionAlias, elementName);
        renameTag(doc, element, newElementName);
    }

    /**
     * Renames an element.
     *
     * @param doc   the XML document.
     * @param element  the lement to rename.
     * @param newTagName  the new name of the element.
     */
    public static void renameTag(Document doc, Element element, String newTagName) {
        if (element != null) {
            Node parent = element.getParentNode();
            parent.removeChild(element);
            Element newElement = doc.createElement(newTagName);
            parent.appendChild(newElement);
            NodeList children = element.getChildNodes();
            cloneNodes(newElement, children);
            cloneAttributes(element, newElement);
        }
    }

    /**
     * Renames all tags with the given old tag name.
     * @param doc   the XML document.
     * @param tagName  the tag name to search for.
     * @param newTagName  the new name of the tags.
     */
    public static void renameAllTags(Document doc, String tagName, String newTagName) {
        NodeList nodes = doc.getElementsByTagName(tagName);
        while (nodes.getLength() > 0) {
            Element elem = (Element) nodes.item(0);
            renameTag(doc, elem, newTagName);
            nodes = doc.getElementsByTagName(tagName);
        }
    }

    /**
     * Removes all tags with the given tag name.
     * @param doc  the XML document.
     * @param tagName the tag name to search for.
     */
    public static void removeAllTags(Document doc, String tagName) {
        NodeList nodes = doc.getElementsByTagName(tagName);
        while (nodes.getLength() > 0) {
            Element elem = (Element) nodes.item(0);
            elem.getParentNode().removeChild(elem);
        }
    }

    /**
     * Moves an element of the entity to an extension.
     *
     * @param doc  the XML document.
     * @param targetExtensionAlias  the alias of the target extension.
     * @param elementName  the name of the element to move.
     *
     * @throws MigrationException  if the entity has more than one
     * extension matching the given alias or more than one
     * element matches the given <code>elementName</code>.
     */
    public static void moveTagToExtension(Document doc, String targetExtensionAlias, String elementName)
            throws MigrationException {
        cloneNodes(doc, null, targetExtensionAlias, elementName, elementName, true);
    }

    /**
     * Moves an element of the entity to an extension and renames it.
     *
     * @param doc  the XML document.
     * @param targetExtensionAlias  the alias of the target extension.
     * @param sourceElementName  the name of the element to move.
     * @param targetElementName  the new name of the element.
     *
     * @throws MigrationException  if the entity has more than one
     * extension matching the given alias or more than one
     * element matches the given <code>sourceElementName</code>.
     */
    public static void moveTagToExtension(Document doc, String targetExtensionAlias, String sourceElementName,
            String targetElementName)
            throws MigrationException {
        cloneNodes(doc, null, targetExtensionAlias, sourceElementName, targetElementName, true);
    }

    /**
     * Moves an element from one extension to another and renames it.
     *
     * @param doc  the XML document.
     * @param sourceExtensionAlias  the alias of the extension with the element to be moved.
     * @param targetExtensionAlias  the alias of the extension to which the element should be moved.
     * @param sourceElementName   the name of the element to move.
     * @param targetElementName  the new name of the element.
     *
     * @throws MigrationException  if the entity has more than one
     * extension matching the given alias or the source extension has more
     * than one element matches the given <code>sourceElementName</code>.
     */
    public static void moveTagToExtension(Document doc,
            String sourceExtensionAlias, String targetExtensionAlias,
            String sourceElementName, String targetElementName)
            throws MigrationException {
        cloneNodes(doc, sourceExtensionAlias, targetExtensionAlias, sourceElementName, targetElementName, true);
    }

    /**
     * Copies an element of the entity to an extension.
     *
     * @param doc  the XML document.
     * @param targetExtensionAlias  the alias of the target extension.
     * @param elementName  the name of the element to copy.
     *
     * @throws MigrationException  if the entity has more than one
     * extension matching the given alias or more than one
     * element matches the given <code>elementName</code>.
     */
    public static void copyTagToExtension(Document doc, String targetExtensionAlias, String elementName)
            throws MigrationException {
        cloneNodes(doc, null, targetExtensionAlias, elementName, elementName, false);
    }

    /**
     * Copies an element of the entity to an extension and renames it.
     *
     * @param doc  the XML document.
     * @param targetExtensionAlias   the alias of the target extension.
     * @param sourceElementName  the name of the element to copy.
     * @param targetElementName  the new name of the element.
     *
     * @throws MigrationException  if the entity has more than one
     * extension matching the given alias or more than one
     * element matches the given <code>sourceElementName</code>.
     */
    public static void copyTagToExtension(Document doc, String targetExtensionAlias, String sourceElementName,
            String targetElementName)
            throws MigrationException {
        cloneNodes(doc, null, targetExtensionAlias, sourceElementName, targetElementName, false);
    }

    private static void cloneNodes(Document doc,
            String sourceExtensionAlias, String targetExtensionAlias,
            String sourceElementName, String targetElementName, boolean moveElement)
            throws MigrationException {
        Element sourceElement = sourceExtensionAlias != null ?
                getElementOfExtension(doc, sourceExtensionAlias, sourceElementName) :
                getElementOfEntity(doc, sourceElementName);
        if (sourceElement != null) {
            Element targetExtensionElement = getOrCreateExtensionNode(doc, targetExtensionAlias);
            Element targetElement = doc.createElement(targetElementName);
            targetExtensionElement.appendChild(targetElement);
            Node clonedSourceNode = sourceElement.cloneNode(true);
            cloneNodes(targetElement, clonedSourceNode.getChildNodes());
            if (moveElement) {
                Node parent = sourceElement.getParentNode();
                parent.removeChild(sourceElement);
            }
        }
    }

    private static void cloneNodes(Element targetElement, NodeList nodes) {
        if (nodes != null) {
            for (int i = 0; i < nodes.getLength(); i++) {
                targetElement.appendChild(nodes.item(i).cloneNode(true));
            }
        }
    }

    private static void cloneAttributes(Element element, Element targetElement) {
        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); ++i) {
            Node attribute = attributes.item(i);
            targetElement.setAttribute(attribute.getNodeName(), attribute.getTextContent());
        }
    }

    private static boolean matches(Node node, String name) {
        return (node != null) && (node instanceof Element) && (name.equals(node.getNodeName()));
    }

    private static void throwInvalidNumberOfElementsFound(String parentName, String elementName, int expected, int found)
            throws MigrationException {
        throw new MigrationException(MessageFormat.format(
                "Unexpected number of <{0}> elements found inside <{1}> (expected: {2}, found: {3})",
                elementName, parentName, expected, found));
    }
}
