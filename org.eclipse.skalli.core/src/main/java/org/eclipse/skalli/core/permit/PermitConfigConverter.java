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
package org.eclipse.skalli.core.permit;

import java.text.MessageFormat;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.UUIDUtils;
import org.eclipse.skalli.services.permit.Permit;

import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class PermitConfigConverter implements Converter {

    private String host;

    public PermitConfigConverter(String host) {
        this.host = host;
    }

    @Override
    public boolean canConvert(@SuppressWarnings("rawtypes") Class type) {
        return type.equals(PermitConfig.class);
    }

    @SuppressWarnings("nls")
    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        PermitConfig config = (PermitConfig)source;
        writeNode(writer, "pos", Integer.toString(config.getPos()));
        UUID uuid = config.getUuid();
        if (uuid != null) {
            String uuidStr = uuid.toString();
            writeNode(writer, "uuid", uuidStr);
            if (StringUtils.isNotBlank(host)) {
                writer.startNode("link");
                writer.addAttribute("rel", "self");
                writer.addAttribute("href", host + "/api/config/permits/" + uuidStr);
                writer.endNode();
            }
        }
        writeNode(writer, "type", config.getType());
        writeNode(writer, "action", config.getAction());
        writeNode(writer, "path", config.getPath());
        writeNode(writer, "level", Integer.toString(config.getLevel()));
        if (config.isOverride()) {
            writer.startNode("override");
            writer.endNode();
        }
        writeNode(writer, "owner", config.getOwner());
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        PermitConfig config = iterateNodes(reader, context);
        if (StringUtils.isBlank(config.getType())) {
            throw new XStreamException("Permit elements must have a 'type'");
        }
        if (StringUtils.isBlank(config.getAction())) {
            throw new XStreamException("Permit elements must have an 'action'");
        }
        if (StringUtils.isBlank(config.getPath())) {
           throw new XStreamException("Permit elements must have a 'path'");
        }
        if (StringUtils.isBlank(config.getOwner()) && !"global".equals(config.getType())) { //$NON-NLS-1$
            throw new XStreamException("Permit elements must have an 'owner' or be of type 'global");
        }
        if (config.getUuid() == null) {
            config.setUuid(UUID.randomUUID());
        }
        return config;
    }

    @SuppressWarnings("nls")
    private PermitConfig iterateNodes(HierarchicalStreamReader reader, UnmarshallingContext context) {
        PermitConfig config = new PermitConfig();
        config.setPos(-1);
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            String field = reader.getNodeName();
            String value = reader.getValue();
            if ("type".equals(field)) {
                config.setType(value);
            } else if ("pos".equals(field)) {
                int pos = -1;
                if (StringUtils.isNotBlank(value)) {
                    try {
                        pos = Integer.parseInt(value);
                    } catch (NumberFormatException e) {
                        throw new XStreamException(MessageFormat.format("''{0} is not a valid position", value));
                    }
                }
                config.setPos(pos);
            } else if ("action".equals(field)) {
                config.setAction(value);
            } else if ("path".equals(field)) {
                config.setPath(value);
            } else if ("level".equals(field) && value != null) {
                try {
                    config.setLevel(Permit.parseLevel(value));
                } catch (NumberFormatException e) {
                    throw new XStreamException(MessageFormat.format("''{0} is not a valid permit level", value));
                }
            } else if ("owner".equals(field)) {
                config.setOwner(value);
            } else if ("override".equals(field)) {
                config.setOverride(true);
            } else if ("uuid".equals(field)) {
                if (UUIDUtils.isUUID(value)) {
                    config.setUuid(UUIDUtils.asUUID(value));
                } else {
                    throw new XStreamException(MessageFormat.format("''{0} is not a valid UUID", value));
                }
            }
            reader.moveUp();
        }
        return config;
    }

    private void writeNode(HierarchicalStreamWriter writer, String nodeName, String value) {
        if (StringUtils.isNotBlank(value)) {
            writer.startNode(nodeName);
            writer.setValue(value);
            writer.endNode();
        }
    }
}
