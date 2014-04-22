package org.eclipse.skalli.services.extension.rest;

import java.io.IOException;
import java.io.Writer;
import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.FormatUtils;
import org.eclipse.skalli.commons.XMLUtils;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.rest.RequestContext;
import org.eclipse.skalli.services.rest.RestService;
import org.eclipse.skalli.services.rest.RestWriter;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.WriterRepresentation;

/**
 * REST representation for error responses as defined by <tt>/schemas/error.xsd</tt>.
 * <p>
 * {@link RestExtension Extensions for the REST API} should return error representations
 * at least for all kinds of HTTP 5xx server errors, and may return them for HTTP 4xx
 * client errors as well.
 */
public class ErrorRepresentation extends WriterRepresentation {

    private RequestContext context;
    private Status status;
    private String errorId;
    private String message;
    private String timestamp;

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
     * @param context  the parameters of the request like media type an host.
     * @param status  the status of the response, including the status code.
     * @param errorId  a unique identifier for the error that has happened, or <code>null</code>.
     * If no id is specified, one is generated from the status.
     * @param message  the error message, or <code>null</code>.
     * If no message is specified, one is generated from the status.
     */
    public ErrorRepresentation(RequestContext context, Status status, String errorId, String message) {
        super(context.getMediaType());
        this.context = context;
        this.status = status;
        this.errorId = errorId;
        this.message = message;
        this.timestamp = FormatUtils.formatUTCWithMillis(System.currentTimeMillis());
    }

    // for testing purposes
    String getTimestamp() {
        return timestamp;
    }

    @Override
    public void write(Writer writer) throws IOException {
        RestService restService = Services.getRequiredService(RestService.class);
        MediaType mediaType = context.getMediaType();
        if (!restService.isSupportedMediaType(mediaType)) {
            throw new IOException(MessageFormat.format("Unsupported media type ''{0}''", mediaType));
        }
        RestWriter restWriter = restService.getRestWriter(writer, context);
        restWriter.object("error"); //$NON-NLS-1$
        restWriter.namespace(XMLUtils.XMLNS, RestUtils.API_NAMESPACE);
        restWriter.namespace(XMLUtils.XMLNS_XSI, XMLUtils.XSI_INSTANCE_NS);
        restWriter.namespace(XMLUtils.XSI_SCHEMA_LOCATION, MessageFormat.format(
                "{0} {1}/schemas/error.xsd", RestUtils.API_NAMESPACE, context.getHost())); //$NON-NLS-1$
        if (StringUtils.isBlank(errorId)) {
            errorId = MessageFormat.format("rest:{0}:{1}", context.getPath(), Integer.toString(status.getCode())); //$NON-NLS-1$
        }
        restWriter.pair("errorId", errorId); //$NON-NLS-1$
        restWriter.pair("timestamp", timestamp); //$NON-NLS-1$
        if (StringUtils.isBlank(message)) {
            message = MessageFormat.format("{0} ({1})", status.getDescription(), status.getName()); //$NON-NLS-1$
        }
        restWriter.pair("message", message); //$NON-NLS-1$
        restWriter.end();
        restWriter.flush();
    }
}
