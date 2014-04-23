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
package org.eclipse.skalli.model.ext.maven.internal;

import java.io.IOException;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.ext.maven.MavenModule;
import org.eclipse.skalli.model.ext.maven.MavenReactor;
import org.eclipse.skalli.model.ext.maven.MavenReactorProjectExt;
import org.eclipse.skalli.services.extension.rest.RestConverterBase;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

class MavenReactorConverter extends RestConverterBase<MavenReactorProjectExt> {

    public static final String API_VERSION = "1.0"; //$NON-NLS-1$
    public static final String NAMESPACE = "http://www.eclipse.org/skalli/2010/API/Extension-MavenReactor"; //$NON-NLS-1$

    private static final String TAG_MODULE = "module"; //$NON-NLS-1$
    private static final String TAG_MODULES = "modules"; //$NON-NLS-1$
    private static final String TAG_PACKAGING = "packaging"; //$NON-NLS-1$
    private static final String TAG_ARTIFACTID = "artifactId"; //$NON-NLS-1$
    private static final String TAG_GROUPID = "groupId"; //$NON-NLS-1$
    private static final String TAG_VERSIONS = "versions"; //$NON-NLS-1$
    private static final String TAG_VERSION = "version"; //$NON-NLS-1$
    private static final String TAG_COORDINATE = "coordinate"; //$NON-NLS-1$

    public MavenReactorConverter() {
        super(MavenReactorProjectExt.class);
    }

    @Override
    protected void marshal(MavenReactorProjectExt extension) throws IOException {
        MavenReactor reactor = extension.getMavenReactor();
        if (reactor != null) {
            MavenModule reactorCoordinate = reactor.getCoordinate();
            if (reactorCoordinate != null) {
                writer.object(TAG_COORDINATE);
                writeCoordinate(reactorCoordinate);
                writer.end();
            }
            TreeSet<MavenModule> modules = reactor.getModules();
            if (modules.size() > 0) {
                writer.array(TAG_MODULES, TAG_MODULE);
                for (MavenModule moduleCoordinate : modules) {
                    writer.object();
                    writeCoordinate(moduleCoordinate);
                    writer.end();
                }
                writer.end();
            }
        }
    }

    private void writeCoordinate(MavenModule reactorCoordinate) throws IOException {
        writer.pair(TAG_GROUPID, reactorCoordinate.getGroupId());
        writer.pair(TAG_ARTIFACTID, reactorCoordinate.getArtefactId());
        writer.collection(TAG_VERSIONS, TAG_VERSION, reactorCoordinate.getSortedVersions());
        if (StringUtils.isNotBlank(reactorCoordinate.getPackaging())) {
            writer.pair(TAG_PACKAGING, reactorCoordinate.getPackaging());
        }
    }

    @Deprecated
    public MavenReactorConverter(String host) {
        super(MavenReactorProjectExt.class, "mavenReactor", host); //$NON-NLS-1$
    }

    @Deprecated
    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        MavenReactorProjectExt ext = (MavenReactorProjectExt) source;
        MavenReactor reactor = ext.getMavenReactor();
        if (reactor != null) {
            MavenModule reactorCoordinate = reactor.getCoordinate();
            if (reactorCoordinate != null) {
                writer.startNode(TAG_COORDINATE); // <mavenReactor>
                writeContent(writer, reactorCoordinate);
                writer.endNode(); // </coordinate>
            }

            TreeSet<MavenModule> modules = reactor.getModules();
            if (modules.size() > 0) {
                writer.startNode(TAG_MODULES); // <modules>
                for (MavenModule moduleCoordinate : modules) {
                    writer.startNode(TAG_MODULE); // <module>
                    writeContent(writer, moduleCoordinate);
                    writer.endNode(); // </module>
                }
                writer.endNode(); // </modules>
            }
        }
    }

    @Deprecated
    private void writeContent(HierarchicalStreamWriter writer, MavenModule reactorCoordinate) {
        writeNode(writer, TAG_GROUPID, reactorCoordinate.getGroupId());
        writeNode(writer, TAG_ARTIFACTID, reactorCoordinate.getArtefactId());
        writeNode(writer, TAG_VERSIONS, TAG_VERSION, reactorCoordinate.getSortedVersions());
        if (StringUtils.isNotBlank(reactorCoordinate.getPackaging())) {
            writeNode(writer, TAG_PACKAGING, reactorCoordinate.getPackaging());
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        // don't support that yet
        return null;
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
        return "extension-maven-reactor.xsd"; //$NON-NLS-1$
    }
}
