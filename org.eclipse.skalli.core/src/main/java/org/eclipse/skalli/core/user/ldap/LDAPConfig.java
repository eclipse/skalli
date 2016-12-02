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
package org.eclipse.skalli.core.user.ldap;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("ldap")
public class LDAPConfig {

    private String providerId;
    private String destination;
    private String baseDN;
    private String searchScope;
    private String cacheSize;

    // do not remove: required by xstream
    public LDAPConfig() {
    }

    /**
     * @return the providerId
     */
    public String getProviderId() {
        return providerId;
    }

    /**
     * @param providerId the providerId to set
     */
    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    /**
     * @return the destination
     */
    public String getDestination() {
        return destination;
    }

    /**
     * @param destination the destination to set
     */
    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getBaseDN() {
        return baseDN;
    }

    public void setBaseDN(String baseDN) {
        this.baseDN = baseDN;
    }

    public String getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(String cacheSize) {
        this.cacheSize = cacheSize;
    }

    /**
     * Returns the desired search scope.
     *
     * @return either "base" or "onelevel", or "subtree".
     */
    public String getSearchScope() {
        return searchScope;
    }

    /**
     * Specifies the desired search scope, i.e. either
     * "base", if only the given {@link #getBaseDN() base path) should be searched,
     * "onelevel", if one level below the base path should be searched, or
     * "subtree", if the whole subtree rooted by the base path should be searched.
     *
     * @param searchScope the desired search scope. Default is "onelevel".
     */
    public void setSearchScope(String searchScope) {
        this.searchScope = searchScope;
    }
}
