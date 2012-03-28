package org.eclipse.skalli.model.ext.maven.internal;

import static org.apache.http.HttpStatus.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.params.HttpParams;
import org.eclipse.skalli.services.destination.Destinations;
import org.eclipse.skalli.services.destination.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class HttpMavenPomResolverBase extends MavenPomResolverBase {

    private static final Logger LOG = LoggerFactory.getLogger(HttpMavenPomResolverBase.class);

    /**
     * Calculates the <code>URL</code> corresponding to the parameters <code>scmLocation</code> and <code>relativePath</code>.
     * @throws MalformedURLException in case the <code>URL</code> could not be created
     */
    protected abstract URL resolvePath(String scmLocation, String relativePath) throws MalformedURLException;

    /**
     * Returns an input stream, from which one can directly read the content of the POM.
     * @param entity  the HTTP entity to convert to a stream.
     * @param relativePath  the path of the POM relative to the root of the project's source code.
     * @return an input stream for the content of the POM.
     * @throws IOException  if an i/o problem occured.
     */
    protected abstract InputStream asPomInputStream(HttpEntity entity, String relativePath) throws IOException;

    @Override
    public MavenPom getMavenPom(UUID project, String scmLocation, String relativePath) throws IOException {
        URL url = resolvePath(scmLocation, relativePath);
        if (url == null) {
            throw new IOException(MessageFormat.format(
                    "url to read the pom for scm {0}, relativePath {1} is null.", scmLocation, relativePath));
        }
        try {
            return parse(url, relativePath, false);
        } catch (Exception e) {
            try {
                parse(url, relativePath, true);
            } catch (Exception e1) {
                LOG.error("some unexpected error occured while trying to writing content of " + url.toExternalForm()+ " to log", e);
            }

            throw new IOException(MessageFormat.format(
                    "Can not resolve pom for scm=\"{0}\", relativePath =\"{1}\", url=\"{2}\" ",
                    scmLocation, relativePath, url.toExternalForm()), e);

        }
    }

    /**
     * @param writeContentDetailToLog = true will return an default empty MavenPom  and log the content read from the url with level Error to LOG; if set to false the method
     * parse is called.
     */
    private MavenPom parse(URL url, String relativePath, boolean writeContentDetailToLog) throws IOException,
            HttpException, MavenValidationException {
        HttpClient client = Destinations.getClient(url);
        if (client == null) {
            //we can't read the Url
            return null;
        }
        HttpParams params = client.getParams();
        HttpClientParams.setRedirecting(params, false); // we want to find 301 MOVED PERMANTENTLY
        HttpResponse response = null;
        try {
            LOG.info("GET " + url); //$NON-NLS-1$
            HttpGet method = new HttpGet(url.toExternalForm());
            response = client.execute(method);
            int status = response.getStatusLine().getStatusCode();
            LOG.info(status + " " + response.getStatusLine().getReasonPhrase()); //$NON-NLS-1$
            if (status == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                if (entity == null) {
                    return null;
                }
                if (!writeContentDetailToLog) {
                    return parse(asPomInputStream(entity, relativePath));
                } else {
                    writeContentDetailToLog(url, entity);
                    return new MavenPom();
                }
            }
            else {
                String statusText = response.getStatusLine().getReasonPhrase();
                switch (status) {
                case SC_NOT_FOUND:
                    throw new HttpException(MessageFormat.format("{0} not found", url));
                case SC_UNAUTHORIZED:
                    throw new HttpException(MessageFormat.format("{0} found but authentication required: {1} {2}", url,
                            status, statusText));
                case SC_INTERNAL_SERVER_ERROR:
                case SC_SERVICE_UNAVAILABLE:
                case SC_GATEWAY_TIMEOUT:
                case SC_INSUFFICIENT_STORAGE:
                    throw new HttpException(MessageFormat.format(
                            "{0} not found. Host reports a temporary problem: {1} {2}", url, status, statusText));
                case SC_MOVED_PERMANENTLY:
                    throw new HttpException(MessageFormat.format(
                            "{0} not found. Resource has been moved permanently to {1}",
                            url, response.getFirstHeader("Location")));
                default:
                    throw new HttpException(MessageFormat.format("{0} not found. Host responded with {1} {2}", url,
                            status, statusText));
                }
            }
        } finally {
            HttpUtils.consumeQuietly(response);
        }
    }

    private void writeContentDetailToLog(URL url, HttpEntity entity) {
        try {
            StringWriter writer = new StringWriter();

            String encoding = null;
            if (entity.getContentEncoding() != null) {
                encoding = entity.getContentEncoding().getValue();
            }
            IOUtils.copy(entity.getContent(), writer, encoding);
            String theString = writer.toString();

            StringBuilder sb = new StringBuilder();
            sb.append("Details of reading from ").append(url.toExternalForm()).append(": ");

            Header contentType = entity.getContentType();
            sb.append("ContentType = ").append(
                    (contentType == null ? "null" : (entity.getContentType().getValue()))).append("; ");
            sb.append("encoding =  ").append(encoding).append("; ");
            sb.append("String contet = ").append(theString);
            LOG.error(sb.toString());
        } catch (Exception e) {
            LOG.error("unexpected exception while trying to read the content: ", e);
        }

    }
}
