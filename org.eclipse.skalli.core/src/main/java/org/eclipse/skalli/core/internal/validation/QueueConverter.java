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

import java.util.Queue;

import org.eclipse.skalli.api.java.ValidationService;
import org.eclipse.skalli.api.rest.monitor.AbstractMonitorConverter;
import org.eclipse.skalli.model.ext.EntityBase;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

class QueueConverter extends AbstractMonitorConverter {
    public static final String API_VERSION = "1.0"; //$NON-NLS-1$

    public QueueConverter(String serviceComponentName, String resourceName, String host) {
        super(serviceComponentName, resourceName, host);
    }

    @Override
    public String getApiVersion() {
        return API_VERSION;
    }

    @SuppressWarnings("nls")
    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        ValidationServiceImpl serviceInstance = getServiceInstance(ValidationService.class, ValidationServiceImpl.class);
        if (serviceInstance != null) {
            marshalNSAttributes(writer);
            marshalApiVersion(writer);
            Queue<QueuedEntity<? extends EntityBase>> queuedEntities = serviceInstance.getQueuedEntities();
            writeNode(writer, "queueSize", queuedEntities.size());
            writer.startNode("queuedEntities");
            for (QueuedEntity<?> queuedEntity: queuedEntities) {
                writer.startNode("queuedEntity");
                writeNode(writer, "entityClass", queuedEntity.getEntityClass().toString());
                writeNode(writer, "entityId", queuedEntity.getEntityId().toString());
                writeNode(writer, "minSeverity", queuedEntity.getMinSeverity().toString());
                writeNode(writer, "userId", queuedEntity.getUserId());
                writeDateTime(writer, "QueuedAt", queuedEntity.getQueuedAt());
                writeDateTime(writer, "startedAt", queuedEntity.getStartedAt());
                writer.endNode();
            }
            writer.endNode();
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader arg0, UnmarshallingContext arg1) {
        // not supported yet
        return null;
    }
}