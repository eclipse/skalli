/*******************************************************************************
 * Copyright (c) 2010-2014 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.core.validation;

import java.io.IOException;
import java.util.Queue;

import org.eclipse.skalli.core.rest.monitor.MonitorConverterBase;
import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.services.validation.ValidationService;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

class QueueConverter extends MonitorConverterBase {
    public static final String API_VERSION = "1.0"; //$NON-NLS-1$

    public QueueConverter(String serviceComponentName, String resourceName) {
        super(serviceComponentName, resourceName);
    }

    @SuppressWarnings("nls")
    @Override
    protected void marshal(Object obj) throws IOException {
        writer.object(resourceName);
            namespaces();
            apiVersion();
            ValidationComponent service = getServiceInstance(ValidationService.class, ValidationComponent.class);
            if (service != null) {
                Queue<QueuedEntity<? extends EntityBase>> queuedEntities = service.getQueuedEntities();
                writer.pair("queueSize", queuedEntities.size());
                writer.array("queuedEntities", "queuedEntity");
                for (QueuedEntity<?> queuedEntity: queuedEntities) {
                    writer
                    .object()
                      .pair("entityClass", queuedEntity.getEntityClass().getName())
                      .pair("entityId", queuedEntity.getEntityId())
                      .pair("minSeverity", queuedEntity.getMinSeverity().toString())
                      .pair("userId", queuedEntity.getUserId())
                      .pair("priority", queuedEntity.priorityAsString())
                      .datetime("queuedAt", queuedEntity.getQueuedAt())
                      .datetime("startedAt", queuedEntity.getStartedAt())
                    .end();
                }
                writer.end();
        }
        writer.end();
    }

    @Deprecated
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
        ValidationComponent serviceInstance = getServiceInstance(ValidationService.class, ValidationComponent.class);
        if (serviceInstance != null) {
            marshalNSAttributes(writer);
            marshalApiVersion(writer);
            Queue<QueuedEntity<? extends EntityBase>> queuedEntities = serviceInstance.getQueuedEntities();
            writeNode(writer, "queueSize", queuedEntities.size());
            writer.startNode("queuedEntities");
            for (QueuedEntity<?> queuedEntity: queuedEntities) {
                writer.startNode("queuedEntity");
                writeNode(writer, "entityClass", queuedEntity.getEntityClass().getName());
                writeNode(writer, "entityId", queuedEntity.getEntityId().toString());
                writeNode(writer, "minSeverity", queuedEntity.getMinSeverity().toString());
                writeNode(writer, "userId", queuedEntity.getUserId());
                writeNode(writer, "priority", queuedEntity.priorityAsString());
                writeDateTime(writer, "queuedAt", queuedEntity.getQueuedAt());
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