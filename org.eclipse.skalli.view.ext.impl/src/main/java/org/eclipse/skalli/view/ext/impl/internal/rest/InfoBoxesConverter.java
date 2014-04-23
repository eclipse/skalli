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
package org.eclipse.skalli.view.ext.impl.internal.rest;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.services.extension.rest.RestConverterBase;
import org.eclipse.skalli.view.ext.InfoBox;
import org.restlet.data.MediaType;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class InfoBoxesConverter extends RestConverterBase<InfoBoxes> {

    public static final String API_VERSION = "1.0"; //$NON-NLS-1$
    public static final String NAMESPACE = "http://www.eclipse.org/skalli/2010/API"; //$NON-NLS-1$

    public InfoBoxesConverter() {
        super(InfoBoxes.class);
    }

    @SuppressWarnings("nls")
    @Override
    protected void marshal(InfoBoxes infoboxes) throws IOException {
        writer.object("infoboxes");
            namespaces();
            apiVersion();
            if (writer.isMediaType(MediaType.APPLICATION_JSON)) {
                writer.key("items");
            }
            writer.array("infobox");
            for (InfoBox infoBox : infoboxes.getInfoBoxes()) {
                List<String> actions = infoBox.getSupportedActions();
                String shortName = infoBox.getShortName();
                if (StringUtils.isNotBlank(shortName)) {
                    writer.pair("shortName", shortName);
                    if (CollectionUtils.isNotBlank(actions)) {
                        writer.collection("actions", "action", actions);
                    }
                }
            }
            writer.end();
        writer.end();
    }

    @Deprecated
    public InfoBoxesConverter(String host) {
        super(InfoBoxes.class, "infoboxes", host); //$NON-NLS-1$
    }

    @Override
    public String getApiVersion() {
        return API_VERSION;
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public String getXsdFileName() {
        return "infoboxes.xsd"; //$NON-NLS-1$
    }

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        marshalNSAttributes(writer);
        marshalApiVersion(writer);
        for (InfoBox infoBox : ((InfoBoxes)source).getInfoBoxes()) {
            List<String> actions = infoBox.getSupportedActions();
            String shortName = infoBox.getShortName();
            if (StringUtils.isNotBlank(shortName)) {
                writer.startNode("infobox"); //$NON-NLS-1$
                writeNode(writer, "shortName", shortName); //$NON-NLS-1$
                if (CollectionUtils.isNotBlank(actions)) {
                    writeActions(writer, actions);
                }
                writer.endNode();
            }
        }
    }

    private void writeActions(HierarchicalStreamWriter writer, List<String> actions) {
        writer.startNode("actions"); //$NON-NLS-1$
        for (String action : actions) {
            writeNode(writer, "action", action); //$NON-NLS-1$
        }
        writer.endNode();
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        // don't support that
        return null;
    }
}
