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
package org.eclipse.skalli.view.internal.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.eclipse.skalli.services.FilterMode;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.extension.rest.RestUtils;
import org.eclipse.skalli.view.Consts;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class StaticContentServlet extends HttpServlet {

    private static final long serialVersionUID = -1552291510430177634L;

    private static final String BASE_DIR = "content";
    private static final String TIP_OF_THE_DAY = "/tipoftheday";
    private static final String TIP_PREFIX = "tip_";
    private static final String TIP_SUFFIX = ".html";

    public StaticContentServlet() {
        super();
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/content/")) {
            String path = request.getPathInfo();
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            InputStream in = getContent(path);
            if (in != null) {
                response.setContentType(getContentType(path));
                try {
                    OutputStream out = response.getOutputStream();
                    IOUtils.copy(in, out);
                } finally {
                    IOUtils.closeQuietly(in);
                }
                return;
            }
        } else if (requestURI.startsWith("/schemas/")) {
            // otherwise check the bundles for a matching schema resource
            URL schema = RestUtils.findSchemaResource(requestURI);
            if (schema != null) {
                response.setContentType(getContentType(requestURI));
                InputStream in = schema.openStream();
                OutputStream out = response.getOutputStream();
                try {
                    resolveIncludes(in, out);
                } catch (Exception ex) {
                    throw new IOException("Failed to resolve schema resource", ex);
                } finally {
                    IOUtils.closeQuietly(in);
                }
                return;
            }
        } else if (requestURI.equals(Consts.URL_SEARCH_PLUGIN)) {
            URL searchPluginXML = getSearchPluginXML();
            if (searchPluginXML != null) {
                response.setContentType("application/opensearchdescription+xml"); //$NON-NLS-1$
                Properties properties = new Properties();
                properties.put(Consts.ATTRIBUTE_SEARCH_PLUGIN_TITLE,
                        request.getAttribute(Consts.ATTRIBUTE_SEARCH_PLUGIN_TITLE));
                properties.put(Consts.ATTRIBUTE_SEARCH_PLUGIN_DESCRIPTION,
                        request.getAttribute(Consts.ATTRIBUTE_SEARCH_PLUGIN_DESCRIPTION));
                properties.put(Consts.ATTRIBUTE_WEBLOCATOR, StringUtils.removeEnd(request.getRequestURL().toString(), requestURI));
                InputStream in = searchPluginXML.openStream();
                try {
                    String content = IOUtils.toString(in, "UTF-8"); //$NON-NLS-1$
                    StrSubstitutor subst = new StrSubstitutor(properties);
                    content = subst.replace(content);
                    OutputStream out = response.getOutputStream();
                    IOUtils.write(content, out, "UTF-8"); //$NON-NLS-1$
                } finally {
                    IOUtils.closeQuietly(in);
                }
                return;
            }
        }
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private URL getSearchPluginXML() {
        List<URL> urls = Services.findResources("/search", "search-plugin.xml", false, FilterMode.FIRST_MATCHING);
        return urls.size() == 1 ? urls.get(0) : null;
    }

    /**
     * Resolves all &lt;include&gt; in a given schema and writes the
     * result to the given output stream. Includes that can't be resolved
     * are removed from the schema.
     * @param in  the input stream providing the schema to resolve.
     * @param out  the output stream to write the result to.
     * @throws IOException  if an i/o error occured.
     * @throws SAXException  if parsing of the schema failed.
     * @throws ParserConfigurationException  indicates a serious parser configuration error.
     * @throws TransformerException  if transforming the schema DOM to a character stream failed.
     * @throws TransformerConfigurationException  indicates a serious transformer configuration error.
     */
    private void resolveIncludes(InputStream in, OutputStream out)
            throws IOException, SAXException, ParserConfigurationException,
            TransformerConfigurationException, TransformerException
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document schemaDOM = dbf.newDocumentBuilder().parse(new InputSource(in));
        Element schemaRoot = schemaDOM.getDocumentElement();

        // iterate all <include> tags and resolve them if possible
        NodeList includes = schemaDOM.getElementsByTagName("xsd:include");
        while (includes.getLength() > 0) {
            for (int i = 0; i < includes.getLength(); ++i) {
                Node includeNode = includes.item(i);
                Node includeParent = includeNode.getParentNode();
                Node schemaLocation = includeNode.getAttributes().getNamedItem("schemaLocation");
                if (schemaLocation != null)
                {
                    // extract the pure file name from the schemaLocation and
                    // try to find an XSD resource in all model extensions matching
                    // the given schemaLocation attribute -> if found, replace the include tag
                    // with the DOM of the include (without the root tag, of course!)
                    URL includeFile = RestUtils.findSchemaResource(schemaLocation.getTextContent());
                    if (includeFile != null) {
                        Document includeDOM = dbf.newDocumentBuilder().parse(new InputSource(includeFile.openStream()));
                        NodeList includeNodes = includeDOM.getDocumentElement().getChildNodes();
                        for (int j = 0; j < includeNodes.getLength(); ++j)
                        {
                            // import and insert the tag before <include>
                            schemaRoot.insertBefore(schemaDOM.importNode(includeNodes.item(j), true), includeNode);
                        }
                    }

                    // in any case: remove the <include> tag
                    includeParent.removeChild(includeNode);
                }
            }
            // resolve includes of includes (if any)
            includes = schemaDOM.getElementsByTagName("xsd:include");
        }

        // serialize the schema DOM to the given output stream
        Transformer xform = TransformerFactory.newInstance().newTransformer();
        xform.setOutputProperty(OutputKeys.INDENT, "yes");
        xform.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        xform.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        xform.transform(new DOMSource(schemaDOM), new StreamResult(out));
    }

    private InputStream getContent(String path) throws IOException {
        if (TIP_OF_THE_DAY.equalsIgnoreCase(path)) {
            return getRandomFile(BASE_DIR + path, TIP_PREFIX, TIP_SUFFIX);
        }
        File f = new File(BASE_DIR + path);
        return f.exists() && f.isFile() ? new FileInputStream(f) : null;
    }

    private InputStream getRandomFile(String path, final String prefix, final String suffix) throws IOException {
        File dir = new File(path);
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith(prefix) && name.endsWith(suffix);
            }
        });
        if (files == null || files.length == 0) {
            return null;
        }
        int next = RandomUtils.nextInt(files.length);
        return new FileInputStream(files[next]);
    }

    private String getContentType(String path) {
        if (TIP_OF_THE_DAY.equalsIgnoreCase(path)) {
            return "text/html";
        } else if (path.endsWith(".png")) {
            return "image/png";
        } else if (path.endsWith(".gif")) {
            return "image/gif";
        } else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (path.endsWith(".html") || path.endsWith(".html") || path.endsWith(".htmls")) {
            return "text/html";
        } else if (path.endsWith(".css")) {
            return "text/css";
        } else if (path.endsWith(".js")) {
            return "text/javascript";
        } else if (path.endsWith(".xml") || path.endsWith(".xsd")) {
            return "text/xml";
        }
        return "application/octet-stream";
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
    }

}
