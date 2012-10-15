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
package org.eclipse.skalli.model.ext.maven.internal.recommendedupdatesites;

import java.io.IOException;
import java.text.MessageFormat;

import org.eclipse.skalli.model.ValidationException;
import org.eclipse.skalli.model.ext.maven.recommendedupdatesites.RecommendedUpdateSites;
import org.eclipse.skalli.model.ext.maven.recommendedupdatesites.RecommendedUpdateSitesService;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.extension.rest.ResourceBase;
import org.eclipse.skalli.services.extension.rest.ResourceRepresentation;
import org.eclipse.skalli.services.permit.Permits;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.mapper.CannotResolveClassException;

public class UpdateSitesServerResource extends ResourceBase {

    private static final String ID_PREFIX = "rest:api/updatesites/{0}/{1}:"; //$NON-NLS-1$
    private static final String ERROR_ID_IO_ERROR = ID_PREFIX + "10"; //$NON-NLS-1$
    private static final String ERROR_ID_PARSING_ENTITY_FAILED = ID_PREFIX + "30";  //$NON-NLS-1$
    private static final String ERROR_ID_VALIDATION_FAILED = ID_PREFIX + "40";  //$NON-NLS-1$
    private static final String ERROR_ID_SERVIVE_UNAVAILABLE = ID_PREFIX + "50";  //$NON-NLS-1$


    @Get
    public Representation retrieve() {
        if (!Permits.isAllowed(getAction(), getPath())) {
            return createUnauthorizedRepresentation();
        }

        String id = (String) getRequestAttributes().get("id");//$NON-NLS-1$
        String userId = (String) getRequestAttributes().get("userId");//$NON-NLS-1$

        RecommendedUpdateSitesService service = Services.getService(RecommendedUpdateSitesService.class);
        RecommendedUpdateSites updateSites = service != null? service.getRecommendedUpdateSites(userId, id) : null;
        if (updateSites == null) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND, MessageFormat.format(
                    "User {0} has no recommended update sites with id ''{1}''", userId, id));
            return null;
        }

        return new ResourceRepresentation<RecommendedUpdateSites>(updateSites,
                new UpdateSitesConverter(getRequest().getResourceRef().getHostIdentifier()));
    }

    @Put
    @Post
    private Representation createOrUpdate(Representation entity, boolean create) {
        if (!Permits.isAllowed(getAction(), getPath())) {
            return createUnauthorizedRepresentation();
        }

        String userId = (String) getRequestAttributes().get("userId");//$NON-NLS-1$
        String id = (String) getRequestAttributes().get("id");//$NON-NLS-1$
        String loggedInUser = Permits.getLoggedInUser();
        if (!loggedInUser.equals(userId)) {
            setStatus(Status.CLIENT_ERROR_FORBIDDEN, MessageFormat.format(
                    "You are not authorized to recommend update sites in the name of user {0}", userId));
            return null;
        }

        ResourceRepresentation<RecommendedUpdateSites> representation = new ResourceRepresentation<RecommendedUpdateSites>();
        representation.setConverters(new UpdateSitesConverter());
        representation.setAnnotatedClasses(RecommendedUpdateSites.class);
        representation.setClassLoader(RecommendedUpdateSites.class.getClassLoader());
        RecommendedUpdateSites updateSites = null;
        try {
            updateSites = representation.read(entity, RecommendedUpdateSites.class);
        } catch (IOException e) {
            String errorId = MessageFormat.format(ERROR_ID_IO_ERROR, userId, id);
            return createIOErrorRepresentation(errorId, e);
        } catch (CannotResolveClassException e) {
            String errorId = MessageFormat.format(ERROR_ID_PARSING_ENTITY_FAILED, userId, id);
            return createParseErrorRepresentation(errorId, e);
        } catch (ConversionException e) {
            String errorId = MessageFormat.format(ERROR_ID_PARSING_ENTITY_FAILED, userId, id);
            return createParseErrorRepresentation(errorId, e);
        }
        updateSites.setId(id);
        updateSites.setUserId(userId);

        RecommendedUpdateSitesService service = Services.getService(RecommendedUpdateSitesService.class);
        if (service == null) {
            String errorId = MessageFormat.format(ERROR_ID_SERVIVE_UNAVAILABLE, userId, id);
            return createServiceUnavailableRepresentation(errorId, "Recommended Update Sites Service");
        }

        Status status = Status.SUCCESS_NO_CONTENT;
        RecommendedUpdateSites oldUpdateSite = service.getRecommendedUpdateSites(userId, id);
        if (oldUpdateSite != null) {
            updateSites.setUuid(oldUpdateSite.getUuid());
            status = Status.SUCCESS_CREATED;
        }
        try {
            service.persist(updateSites, loggedInUser);
        } catch (ValidationException e) {
            String errorId = MessageFormat.format(ERROR_ID_VALIDATION_FAILED, updateSites.getId());
            return createValidationFailedRepresentation(errorId, updateSites.getId(), e);
        }

        setStatus(status);
        return null;
    }

    @Delete
    public Representation remove() {
        if (!Permits.isAllowed(getAction(), getPath())) {
            return createUnauthorizedRepresentation();
        }

        String userId = (String) getRequestAttributes().get("userId");//$NON-NLS-1$
        String id = (String) getRequestAttributes().get("id");//$NON-NLS-1$
        String loggedInUser = Permits.getLoggedInUser();
        if (!loggedInUser.equals(userId)) {
            setStatus(Status.CLIENT_ERROR_FORBIDDEN, MessageFormat.format(
                    "You are not authorized to delete recommended update sites of user {0}", userId)); //$NON-NLS-1$
            return null;
        }

        RecommendedUpdateSitesService service = Services.getService(RecommendedUpdateSitesService.class);
        if (service == null) {
            String errorId = MessageFormat.format(ERROR_ID_SERVIVE_UNAVAILABLE, userId, id);
            return createServiceUnavailableRepresentation(errorId, "Recommended Update Sites Service");
        }

        RecommendedUpdateSites updateSites = service.getRecommendedUpdateSites(userId, id);
        if (updateSites == null) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND, MessageFormat.format(
                    "User {0} has no recommended update sites with id ''{1}''", userId, id));
            return null;
        }
        updateSites.setDeleted(true);

        try {
            service.persist(updateSites, loggedInUser);
        } catch (ValidationException e) {
            String errorId = MessageFormat.format(ERROR_ID_VALIDATION_FAILED, updateSites.getId());
            return createValidationFailedRepresentation(errorId, updateSites.getId(), e);
        }

        setStatus(Status.SUCCESS_NO_CONTENT);
        return null;
    }
}
