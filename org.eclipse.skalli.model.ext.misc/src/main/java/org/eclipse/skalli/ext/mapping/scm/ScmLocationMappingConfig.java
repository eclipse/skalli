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
package org.eclipse.skalli.ext.mapping.scm;

import org.eclipse.skalli.ext.mapping.LinkMappingConfig;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("scmMapping")
public class ScmLocationMappingConfig extends LinkMappingConfig {

    private String provider;

    public ScmLocationMappingConfig(String id, String provider, String purpose, String pattern, String template,
            String name) {
        super(id, purpose, pattern, template, name);
        this.provider = provider;
    }

    /**
     * Returns the provider name of the SCM system, e.g. <code>"git"</code> or <code>"svn"</code>.
     * @see http://maven.apache.org/scm/scm-url-format.html
     */
    public String getProvider() {
        return provider;
    }

    /**
     * Sets the provider name of the SCM system, e.g. <code>"git"</code> or <code>"svn"</code>.
     * @see http://maven.apache.org/scm/scm-url-format.html
     */
    public void setProvider(String provider) {
        this.provider = provider;
    }

    @SuppressWarnings("nls")
    @Override
    public String toString() {
        return super.toString() + "[provider=" + provider + "]";
    }
}
