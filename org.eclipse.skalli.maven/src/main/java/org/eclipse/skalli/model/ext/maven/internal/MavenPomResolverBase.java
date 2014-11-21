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
package org.eclipse.skalli.model.ext.maven.internal;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.ext.mapping.scm.ScmLocationMapper;
import org.eclipse.skalli.ext.mapping.scm.ScmLocationMapping;
import org.eclipse.skalli.model.ValidationException;
import org.eclipse.skalli.model.ext.maven.MavenPomResolver;
import org.eclipse.skalli.services.extension.PropertyMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MavenPomResolverBase implements MavenPomResolver {

    private static final Logger LOG = LoggerFactory.getLogger(MavenPomResolverBase.class);

    protected static final String DEFAULT_POM_FILENAME = "pom.xml"; //$NON-NLS-1$

    abstract protected String getProvider();

    @Override
    public boolean canResolve(String scmLocation) {
         ScmLocationMapping mapping = getScmLocationMapping(scmLocation);
         return (mapping != null)? getProvider().equals(mapping.getProvider()) : false;
    }


    protected MavenPom parse(InputStream pomInputStream) throws ValidationException, IOException {
        MavenPomParser parser = new MavenPomParserImpl();
        return parser.parse(pomInputStream);
    }

    protected ScmLocationMapping getScmLocationMapping(String scmLocation) {
        ScmLocationMapper mapper = new ScmLocationMapper(ScmLocationMapper.ALL_PROVIDERS,
                ScmLocationMapper.MAVEN_RESOLVER);
        List<ScmLocationMapping> mappings = mapper.getFilteredMappings();
        if (mappings.isEmpty()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "no suitable scm mapping found for purpose=''{0}''",
                        ScmLocationMapper.MAVEN_RESOLVER));
            }
            return null;
        }

        for (ScmLocationMapping mapping : mappings) {
            if (PropertyMapper.matches(scmLocation, mapping.getPattern())) {
                return mapping;
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "no suitable scm mapping found matching scmLocation=''{0}'' && purpose=''{1}''",
                    scmLocation, ScmLocationMapper.MAVEN_RESOLVER));
        }
        return null;
    }

    protected String getRepositoryRoot(String scmLocation) {
        ScmLocationMapping scmMappingConfig = getScmLocationMapping(scmLocation);
        if (scmMappingConfig == null) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "no scm location mapping available for location {0}", scmLocation));
        }

        String repsitoryRoot = PropertyMapper.convert(scmLocation, scmMappingConfig.getPattern(),
                scmMappingConfig.getTemplate(), "");
        return repsitoryRoot;
    }

    protected String getPomFileName(String relativePath) {
        StringBuilder fileName = new StringBuilder();
        if (StringUtils.isBlank(relativePath) || ".".equals(relativePath)) { //$NON-NLS-1$
            fileName.append(DEFAULT_POM_FILENAME);
        }
        else if (!relativePath.endsWith(DEFAULT_POM_FILENAME)) {
            appendPath(fileName, relativePath);
            if (!relativePath.endsWith("/")) { //$NON-NLS-1$
                fileName.append("/"); //$NON-NLS-1$
            }
            fileName.append(DEFAULT_POM_FILENAME);
        }
        else {
            appendPath(fileName, relativePath);
        }
        return fileName.toString();
    }

    protected void appendPath(StringBuilder rootPath, String relativePath) {
        if (relativePath.charAt(0) == '/') {
            rootPath.append(relativePath.substring(1));
        } else {
            rootPath.append(relativePath);
        }
    }

    @SuppressWarnings("nls")
    protected boolean isValidNormalizedPath(String path) {
        if (StringUtils.isNotBlank(path)) {
            if (path.indexOf('\\') >= 0) {
                return false;
            }
            if (path.indexOf("..") >= 0 ||
                    path.startsWith("./") ||
                    path.endsWith("/.") ||
                    path.indexOf("/./") >= 0) {
                return false;
            }
        }
        return true;
    }

}
