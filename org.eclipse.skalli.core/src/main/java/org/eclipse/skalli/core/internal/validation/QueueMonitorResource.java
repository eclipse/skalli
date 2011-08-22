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
package org.eclipse.skalli.core.internal.validation;

import org.eclipse.skalli.api.rest.monitor.MonitorResource;
import org.eclipse.skalli.model.ext.AliasedConverter;

public class QueueMonitorResource extends MonitorResource {
    public static final String RESOURCE_NAME = "queue"; //$NON-NLS-1$

    @Override
    protected AliasedConverter getConverter(String host) {
        return new QueueConverter(ValidationServiceImpl.SERVICE_COMPONENT_NAME, RESOURCE_NAME, host);
    }
}