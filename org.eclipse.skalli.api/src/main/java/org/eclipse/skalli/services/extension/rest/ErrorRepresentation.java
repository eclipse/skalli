package org.eclipse.skalli.services.extension.rest;

import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.FormatUtils;
import org.eclipse.skalli.commons.XMLUtils;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.StringRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ErrorRepresentation extends StringRepresentation {

    private static final Logger LOG = LoggerFactory.getLogger(ErrorRepresentation.class);

    public ErrorRepresentation(String host, Status status, String errorId, String message) {
        super(getContent(host, status, errorId, message), MediaType.TEXT_XML);
    }

    private static String getContent(String host, Status status, String errorId, String message) {
        String timestamp = FormatUtils.formatUTCWithMillis(System.currentTimeMillis());
        try {
            Document doc = XMLUtils.newDocument();
            Element root = doc.createElement("error"); //$NON-NLS-1$
            root.setAttribute(RestUtils.XMLNS, RestUtils.API_NAMESPACE);
            root.setAttribute(RestUtils.XMLNS_XSI, RestUtils.XSI_INSTANCE_NS);
            root.setAttribute(RestUtils.XSI_SCHEMA_LOCATION,
                    MessageFormat.format("{0} {1}{2}error.xsd", RestUtils.API_NAMESPACE, host, RestUtils.URL_SCHEMAS)); //$NON-NLS-1$
            doc.appendChild(root);

            if (StringUtils.isBlank(errorId)) {
                errorId = Integer.toString(status.getCode());
            }
            Element idElement = doc.createElement("errorId"); //$NON-NLS-1$
            idElement.appendChild(doc.createTextNode(errorId));
            root.appendChild(idElement);

            Element timestampElement = doc.createElement("timestamp"); //$NON-NLS-1$
            timestampElement.appendChild(doc.createTextNode(timestamp));
            root.appendChild(timestampElement);

            Element messageElement = doc.createElement("message"); //$NON-NLS-1$
            messageElement.appendChild(doc.createTextNode(message));
            root.appendChild(messageElement);
            return XMLUtils.documentToString(doc);

        } catch (Exception e) {
            LOG.error("Failed to create error representation", e);
            return MessageFormat.format("error: ({0}) {1} {2}", errorId, timestamp, message);
        }
    }

}
