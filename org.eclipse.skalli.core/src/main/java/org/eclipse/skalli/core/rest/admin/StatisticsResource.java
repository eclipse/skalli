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
package org.eclipse.skalli.core.rest.admin;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.Statistics;
import org.eclipse.skalli.services.extension.rest.ResourceBase;
import org.eclipse.skalli.services.extension.rest.ResourceRepresentation;
import org.eclipse.skalli.services.permit.Permits;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;

public class StatisticsResource extends ResourceBase {

    private static final String ID_PREFIX = "rest:api/admin/statistics:"; //$NON-NLS-1$
    private static final String ERROR_ID_INVALID_QUERY = ID_PREFIX + "20"; //$NON-NLS-1$

    public static final String PARAM_SECTION = "section"; //$NON-NLS-1$
    public static final String PARAM_FILTER = "filter"; //$NON-NLS-1$

    @Get
    public Representation retrieve() {
        if (!Permits.isAllowed(getAction(), getPath())) {
            return createUnauthorizedRepresentation();
        }

        StatisticsQuery query = new StatisticsQuery(getQueryAttributes());
        String section = (String) getRequestAttributes().get(PARAM_SECTION);
        if (StringUtils.isNotBlank(section)) {
            query.setSection(section);
        }
        String filter = (String) getRequestAttributes().get(PARAM_FILTER);
        if (StringUtils.isNotBlank(filter)) {
            query.setFilter(filter);
        }
        Statistics statistics = Statistics.getDefault();
        return new ResourceRepresentation<Statistics>(statistics,
                new StatisticsConverter(getRequest().getResourceRef().getHostIdentifier(), query));
    }

    @Delete
    public Representation remove() {
        if (!Permits.isAllowed(getAction(), getPath())) {
            return createUnauthorizedRepresentation();
        }
        if (!hasQueryAttribute(StatisticsQuery.PARAM_TO)) {
            return createErrorRepresentation(Status.CLIENT_ERROR_BAD_REQUEST, ERROR_ID_INVALID_QUERY,
                    "Query attribute \"to\" is mandatory");
        }
        StatisticsQuery query = new StatisticsQuery(getQueryAttributes());
        Statistics statistics = Statistics.getDefault();
        statistics.clear(query.getFrom(), query.getTo());
        setStatus(Status.SUCCESS_NO_CONTENT);
        return null;
    }

}
