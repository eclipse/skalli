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
package org.eclipse.skalli.services.user;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Configuration specifying the preferred user store.
 */
@XStreamAlias("userStore")
public class UserStoreConfig {

    private String type;
    private boolean useLocalFallback;

    /**
     * Returns the value of the property <tt>userstore.type</tt> a user service
     * implementation must define to qualify as preferred user store.
     */
    public String getType() {
        return type;
    }
    /**
     * Sets the alue of the property <tt>userstore.type</tt> a user service
     * implementation must define to qualify as preferred user store.
     *
     * @param type the value of <tt>userstore.type</tt> to match.
     */
    public void setType(String type) {
        this.type = type;
    }
    /**
     * If <code>true</code> a fallback to the local user store implementation
     * is allowed.
     */
    public boolean isUseLocalFallback() {
        return useLocalFallback;
    }
    /**
     * Determines whether the instance may fallback to the local user
     * store implementation if the preferred user store is not available.
     *
     * @param useLocalFallback  if <code>true</code> the local user store
     * may be used instead of the preferred.
     */
    public void setUseLocalFallback(boolean useLocalFallback) {
        this.useLocalFallback = useLocalFallback;
    }
}
