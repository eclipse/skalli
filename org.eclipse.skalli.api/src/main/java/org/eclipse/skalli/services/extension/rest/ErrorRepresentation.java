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

/**
 * REST representation for error responses as defined by <tt>/schemas/error.xsd</tt>.
 * <p>
 * {@link RestExtension Extensions for the REST API} should return error representations
 * at least for all kinds of HTTP 5xx server errors, and may return them for HTTP 4xx
 * client errors as well.
 */
public class ErrorRepresentation extends StringRepresentation {

    private static final Logger LOG = LoggerFactory.getLogger(ErrorRepresentation.class);

    /**
     * Constructs an error representation with the given status, error identifier and
     * detail message.
     * <p>
     * The error identifier helps tracing the error in the log and distinguishing it from
     * other errors produced by the same REST extension.<br>
     * The usual format is <tt>rest:&lt;path&gt;:&lt;number%&gt;</tt>, e.g.
     * <tt>rest:/api/projects/technology.skalli:20</tt>. The <tt>&lt;number&gt;</tt> distinguishes
     * errors for a REST extensions, where "00" is reserved for unexpected errors and
     * "10" for i/o errors. All other error numbers are specific for the REST extension.<br>
     * The <tt>&lt;path&gt;</tt> should be the path of the original resource that caused
     * the error, but REST extensions may deviate from that convention.
     *
     * @param host  the host, from which the error is sent.
     * @param status  the status of the response, including the status code.
     * @param errorId  a unique identifier for the error that has happened.
     * @param message  the error message.
     */
    public ErrorRepresentation(String host, Status status, String errorId, String message) {
        super(getContent(host, status, errorId, message), MediaType.TEXT_XML);
    }

    private static String getContent(String host, Status status, String errorId, String message) {
        String timestamp = FormatUtils.formatUTCWithMillis(System.currentTimeMillis());
        try {
            Document doc = XMLUtils.newDocument();
            Element root = doc.createElement("error"); //$NON-NLS-1$
            root.setAttribute(XMLUtils.XMLNS, RestUtils.API_NAMESPACE);
            root.setAttribute(XMLUtils.XMLNS_XSI, XMLUtils.XSI_INSTANCE_NS);
            root.setAttribute(XMLUtils.XSI_SCHEMA_LOCATION,
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
