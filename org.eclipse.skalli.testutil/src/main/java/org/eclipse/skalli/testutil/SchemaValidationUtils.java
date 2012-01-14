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
package org.eclipse.skalli.testutil;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.List;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.eclipse.skalli.model.ExtensibleEntityBase;
import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.extension.ExtensionService;
import org.eclipse.skalli.services.extension.rest.RestConverter;
import org.eclipse.skalli.services.extension.rest.RestUtils;
import org.junit.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

@SuppressWarnings("nls")
public class SchemaValidationUtils {

    public static void validate(Object o, RestConverter converter, String xsdFile)
            throws Exception {
        DocumentBuilderFactory dbf = getDocumentBuilderFactory(xsdFile);
        StringBufferHierarchicalStreamWriter writer = new StringBufferHierarchicalStreamWriter();
        SchemaValidationUtils.marshal(o, converter, writer);
        validate(o, writer, dbf);
    }

    public static void validate(List<?> list, RestConverter converter, String xsdFile)
            throws Exception {
        DocumentBuilderFactory dbf = getDocumentBuilderFactory(xsdFile);
        for (Object o : list) {
            StringBufferHierarchicalStreamWriter writer = new StringBufferHierarchicalStreamWriter();
            SchemaValidationUtils.marshal(o, converter, writer);
            validate(o, writer, dbf);
        }
    }

    public static <T extends ExtensionEntityBase>
            void validate(List<? extends ExtensibleEntityBase> extensibles, Class<T> extensionClass, String xsdFile)
                    throws Exception
    {
        DocumentBuilderFactory dbf = getDocumentBuilderFactory(xsdFile);

        for (ExtensibleEntityBase extensible : extensibles) {
            StringBufferHierarchicalStreamWriter writer = new StringBufferHierarchicalStreamWriter();
            T extension = extensible.getExtension(extensionClass);
            if (extension != null) {
                boolean done = SchemaValidationUtils.marshalExtension(extensible, extension, writer);
                if (!done) {
                    Assert.fail("Failed to marshal extension " + extensionClass + " for entity " + extensible);
                }
                validate(extensible, writer, dbf);
            }
        }
    }

    private static DocumentBuilderFactory getDocumentBuilderFactory(String xsdFile) throws Exception {
        URL schemaFile = RestUtils.findSchemaResource(xsdFile);
        String mergedSchema = SchemaValidationUtils.resolveIncludes(schemaFile);

        SchemaFactory xsFact = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = xsFact.newSchema(new StreamSource(new StringReader(mergedSchema)));
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setValidating(false);
        dbf.setSchema(schema);
        return dbf;
    }

    private static void validate(final Object o, HierarchicalStreamWriter writer, DocumentBuilderFactory dbf)
            throws Exception {
        final String xml = writer.toString();
        Reader reader = new StringReader(xml);
        DocumentBuilder db = dbf.newDocumentBuilder();
        db.setErrorHandler(new ErrorHandler() {
            @Override
            public void warning(SAXParseException exception) throws SAXException {
                Assert.fail(o.toString() + ": " + exception.getMessage() + "\n" + xml);
            }

            @Override
            public void error(SAXParseException exception) throws SAXException {
                Assert.fail(o.toString() + ": " + exception.getMessage() + "\n" + xml);
            }

            @Override
            public void fatalError(SAXParseException exception) throws SAXException {
                Assert.fail(o.toString() + ": " + exception.getMessage() + "\n" + xml);
            }
        });
        db.parse(new InputSource(reader));
    }

    private static String resolveIncludes(URL schema) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document schemaDOM = dbf.newDocumentBuilder().parse(new InputSource(schema.openStream()));
        NodeList includes = schemaDOM.getElementsByTagName("xsd:include");
        while (includes.getLength() > 0) {
            for (int i = 0; i < includes.getLength(); ++i) {
                Node includeNode = includes.item(i);
                Node includeParent = includeNode.getParentNode();
                NamedNodeMap attributes = includeNode.getAttributes();
                String schemaLocation = attributes.getNamedItem("schemaLocation").getTextContent();
                URL includeFile = RestUtils.findSchemaResource(schemaLocation);
                Document includeDOM = dbf.newDocumentBuilder().parse(new InputSource(includeFile.openStream()));
                Element schemaRoot = schemaDOM.getDocumentElement();
                Element includeRoot = includeDOM.getDocumentElement();
                NodeList children = includeRoot.getChildNodes();
                for (int j = 0; j < children.getLength(); ++j) {
                    schemaRoot.insertBefore(schemaDOM.importNode(children.item(j), true), includeNode);
                }
                includeParent.removeChild(includeNode);
            }
            includes = schemaDOM.getElementsByTagName("xsd:include");
        }

        StringWriter out = new StringWriter();
        Transformer xform = TransformerFactory.newInstance().newTransformer();
        xform.setOutputProperty(OutputKeys.INDENT, "yes");
        xform.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        xform.transform(new DOMSource(schemaDOM), new StreamResult(out));
        return out.toString();
    }

    @SuppressWarnings("rawtypes")
    private static <T extends ExtensionEntityBase>
            boolean marshalExtension(ExtensibleEntityBase extensible, T extension, HierarchicalStreamWriter writer) {
        boolean done = false;
        Set<ExtensionService> extensionServices = Services.getServices(ExtensionService.class);
        for (ExtensionService extensionService : extensionServices) {
            RestConverter converter = extensionService.getRestConverter("https://localhost");
            Class<T> extensionClass = (Class<T>) extension.getClass();
            if (converter != null && extensionClass.equals(converter.getConversionClass())) {
                marshal(extension,
                        new ConverterWrapper(converter, extensionService.getShortName(), extensible
                                .isInherited(extensionClass)), writer);
                done = true;
                break;
            }
        }
        return done;
    }

    private static void marshal(Object o, RestConverter converter, HierarchicalStreamWriter writer) {
        MarshallingContext context = new MarshallingContextMock(writer);
        converter.marshal(o, writer, context);
    }
}
