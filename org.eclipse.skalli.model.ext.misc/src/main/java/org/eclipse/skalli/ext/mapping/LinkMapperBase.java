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
package org.eclipse.skalli.ext.mapping;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.skalli.commons.ComparatorUtils;
import org.eclipse.skalli.commons.Link;
import org.eclipse.skalli.services.configuration.ConfigurationService;

public abstract class LinkMapperBase implements LinkMapper {

    @Override
    public List<Link> getMappedLinks(ConfigurationService configService, String projectId, String source,
            String... purposes) {
        List<Link> ret = new ArrayList<Link>();
        if (configService != null) {
            List<? extends LinkMappingConfig> allMappings = getAllMappings(configService);
            if (allMappings != null) {
                for (LinkMappingConfig mapping : allMappings) {
                    if (matches(mapping, purposes)) {
                        String url = MapperUtil.convert(projectId, source, mapping);
                        if (url != null) {
                            Link location = new Link(url, mapping.getName());
                            ret.add(location);
                        }
                    }
                }
            }
        }
        return ret;
    }

    protected boolean matches(LinkMappingConfig mapping, String... purposes) {
        if (purposes == null) {
            purposes = ALL_PURPOSES;
        }
        for (String purpose : purposes) {
            if (ALL_PURPOSES[0].equals(purpose) || ComparatorUtils.equals(purpose, mapping.getPurpose())) {
                return true;
            }
        }
        return false;
    }

    protected abstract List<? extends LinkMappingConfig> getAllMappings(ConfigurationService configService);
}
