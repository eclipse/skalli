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
package org.eclipse.skalli.core.validation;

import org.eclipse.skalli.core.rest.monitor.MonitorResource;
import org.eclipse.skalli.services.extension.rest.RestConverter;

public class QueueMonitorResource extends MonitorResource {
    public static final String RESOURCE_NAME = "queue"; //$NON-NLS-1$

    @Override
    protected RestConverter<?> getConverter(String host) {
        return new QueueConverter(ValidationComponent.SERVICE_COMPONENT_NAME, RESOURCE_NAME, host);
    }
}