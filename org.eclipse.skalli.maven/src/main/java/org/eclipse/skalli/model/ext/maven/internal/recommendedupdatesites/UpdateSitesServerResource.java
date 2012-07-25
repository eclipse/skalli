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

import org.eclipse.skalli.model.ValidationException;
import org.eclipse.skalli.model.ext.maven.recommendedupdatesites.RecommendedUpdateSites;
import org.eclipse.skalli.model.ext.maven.recommendedupdatesites.RecommendedUpdateSitesService;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.extension.rest.ResourceBase;
import org.eclipse.skalli.services.extension.rest.ResourceRepresentation;
import org.eclipse.skalli.services.group.GroupUtils;
import org.eclipse.skalli.services.permit.Permit;
import org.eclipse.skalli.services.user.LoginUtils;
import org.restlet.data.Status;
import org.restlet.ext.servlet.ServletUtils;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Put;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.mapper.CannotResolveClassException;

public class UpdateSitesServerResource extends ResourceBase {

    @Get
    public Representation retrieve() {
        String path = getReference().getPath();
        Representation result = checkAuthorization(Permit.ACTION_GET, path);
        if (result != null) {
            return result;
        }
        String id = (String) getRequestAttributes().get("id");//$NON-NLS-1$
        String userId = (String) getRequestAttributes().get("userId");//$NON-NLS-1$
        RecommendedUpdateSitesService service = Services.getRequiredService(RecommendedUpdateSitesService.class);
        RecommendedUpdateSites updateSites = service.getRecommendedUpdateSites(userId, id);

        if (updateSites == null) {
            return createStatusMessage(Status.CLIENT_ERROR_NOT_FOUND, "Recommended update sites \"{0}\" for user \"{1}\" not found.", id, userId); //$NON-NLS-1$
        }

        ResourceRepresentation<RecommendedUpdateSites> representation = new ResourceRepresentation<RecommendedUpdateSites>(
                updateSites, new UpdateSitesConverter(getRequest().getResourceRef().getHostIdentifier()));
        return representation;
    }

    @Put
    public Representation store(Representation entity) {
        String path = getReference().getPath();
        Representation result = checkAuthorization(Permit.ACTION_PUT, path);
        if (result != null) {
            return result;
        }
        ResourceRepresentation<RecommendedUpdateSites> representation = new ResourceRepresentation<RecommendedUpdateSites>();
        String host = getRequest().getResourceRef().getHostIdentifier();
        representation.setConverters(new UpdateSitesConverter(host));
        representation.setAnnotatedClasses(RecommendedUpdateSites.class);
        representation.setClassLoader(RecommendedUpdateSites.class.getClassLoader());
        RecommendedUpdateSites updateSites = null;
        try {
            updateSites = representation.read(entity, RecommendedUpdateSites.class);
            String id = (String) getRequestAttributes().get("id");//$NON-NLS-1$
            updateSites.setId(id);
            String userId = (String) getRequestAttributes().get("userId");//$NON-NLS-1$
            updateSites.setUserId(userId);

            LoginUtils loginUtil = new LoginUtils(ServletUtils.getRequest(getRequest()));
            String loggedInUser = loginUtil.getLoggedInUserId();

            if (GroupUtils.isAdministrator(loggedInUser)) {
                if (!loggedInUser.equals(userId)) {
                    return createStatusMessage(Status.CLIENT_ERROR_BAD_REQUEST,
                            "You are not allowed to create recommended update sites for another user");
                }
                RecommendedUpdateSitesService service = Services
                        .getRequiredService(RecommendedUpdateSitesService.class);
                RecommendedUpdateSites oldUpdateSite = service.getRecommendedUpdateSites(userId,
                        updateSites.getId());
                if (oldUpdateSite != null) {
                    updateSites.setUuid(oldUpdateSite.getUuid());
                }
                service.persist(updateSites, loggedInUser);
            } else {
                return createStatusMessage(Status.CLIENT_ERROR_FORBIDDEN, "Access denied.", new Object[] {});
            }
        } catch (IOException e) {
            createStatusMessage(Status.SERVER_ERROR_INTERNAL,
                    "Failed to read recommended update sites entity: " + e.getMessage());
        } catch (ValidationException e) {
            createStatusMessage(Status.CLIENT_ERROR_BAD_REQUEST,
                    "Validating resource with id \"{0}\" failed: " + e.getMessage(), updateSites.getId());
        } catch (CannotResolveClassException e) {
            createStatusMessage(Status.CLIENT_ERROR_BAD_REQUEST,
                    "Failed to parse the request body: " + e.getMessage());
        } catch (ConversionException e) {
            createStatusMessage(Status.CLIENT_ERROR_BAD_REQUEST,
                    "Failed to parse the request body: " + e.getMessage());
        }
        return null;
    }

}
