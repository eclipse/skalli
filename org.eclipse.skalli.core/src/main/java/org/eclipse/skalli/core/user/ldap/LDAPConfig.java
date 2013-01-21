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
package org.eclipse.skalli.core.user.ldap;

import org.eclipse.skalli.services.configuration.rest.Protect;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("ldap")
public class LDAPConfig {

    @Protect
    private String password;
    @Protect
    private String username;
    private String authentication;
    private String referral;
    private String providerUrl;
    private String ctxFactory;
    private boolean sslNoVerify;

    private String baseDN;
    private String searchScope;

    private String cacheSize;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProviderUrl() {
        return providerUrl;
    }

    public void setProviderUrl(String providerUrl) {
        this.providerUrl = providerUrl;
    }

    public String getCtxFactory() {
        return ctxFactory;
    }

    public void setCtxFactory(String ctxFactory) {
        this.ctxFactory = ctxFactory;
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
     * @return the authentication
     */
    public String getAuthentication() {
        return authentication;
    }

    /**
     * @param authentication the authentication to set
     */
    public void setAuthentication(String authentication) {
        this.authentication = authentication;
    }

    /**
     * @return the referral
     */
    public String getReferral() {
        return referral;
    }

    /**
     * @param referral the referral to set
     */
    public void setReferral(String referral) {
        this.referral = referral;
    }

    /**
     * @return the sslVerify
     */
    public boolean isSslNoVerify() {
        return sslNoVerify;
    }

    /**
     * @param sslVerify the sslVerify to set
     */
    public void setSslNoVerify(boolean sslNoVerify) {
        this.sslNoVerify = sslNoVerify;
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
