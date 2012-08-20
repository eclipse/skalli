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
package org.eclipse.skalli.api.rest.internal.admin;

import java.util.concurrent.TimeUnit;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.eclipse.skalli.commons.Statistics;
import org.eclipse.skalli.services.extension.rest.ResourceBase;
import org.eclipse.skalli.services.extension.rest.ResourceRepresentation;
import org.eclipse.skalli.services.permit.Permits;
import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

public class StatisticsResource extends ResourceBase {

    @Get
    public Representation retrieve() {
        if (!Permits.isAllowed(getAction(), getPath())) {
            return createUnauthorizedRepresentation();
        }

        long delta = 0;
        Form form = getRequest().getResourceRef().getQueryAsForm();
        String periodAttribute = form.getFirstValue("period"); //$NON-NLS-1$
        if (StringUtils.isNotBlank(periodAttribute)) {
            int period = 1;
            TimeUnit unit = TimeUnit.DAYS;
            period = NumberUtils.toInt(periodAttribute.substring(0, periodAttribute.length() - 1), 1);
            if (period <= 0) {
                period = 1;
            }
            if (periodAttribute.endsWith("h")) { //$NON-NLS-1$
                unit = TimeUnit.HOURS;
            } else if (periodAttribute.endsWith("m")) { //$NON-NLS-1$
                unit = TimeUnit.MINUTES;
            }
            delta = TimeUnit.MILLISECONDS.convert(period, unit);
        }

        long from = parseDateTime(form.getFirstValue("from"), 0); //$NON-NLS-1$
        long to = parseDateTime(form.getFirstValue("to"), 0); //$NON-NLS-1$
        if (from <= 0 && to <= 0) {
            to = System.currentTimeMillis();
            from = delta > 0 ? to - delta : to - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS);
        } else if (from > 0 && to <= 0) {
            to = delta > 0 ? from + delta : System.currentTimeMillis();
        } else if (from <= 0 && to > 0) {
            from = delta > 0 ? to - delta : 0;
        } else {
            // both from/to given: ignore period
        }

        return new ResourceRepresentation<Statistics>(Statistics.getDefault(),
                new StatisticsConverter(getRequest().getResourceRef().getHostIdentifier(), from, to));
    }

    private long parseDateTime(String s, long defaultValue) {
        if (StringUtils.isBlank(s)) {
            return defaultValue;
        }
        try {
            return DatatypeConverter.parseDateTime(s).getTimeInMillis();
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }
}
