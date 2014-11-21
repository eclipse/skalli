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
package org.eclipse.skalli.view;

import java.security.Principal;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.User;
import org.eclipse.skalli.services.user.UserServices;

/**
 * Utility to retrieve the currenly logged in user.
 */
public class LoginUtils {

    private String userName;

    /**
     * Returns a <code>LoginUtil</code> instance that has been initialized
     * from the given servlet request. This constructor retrieves the unique identifier
     * of the authenticated user with {@link HttpServletRequest#getUserPrincipal()}.
     */
    public LoginUtils(HttpServletRequest request) {
        Principal userPrincipal = request.getUserPrincipal();
        if (userPrincipal != null) {
            userName = userPrincipal.getName();
            if (StringUtils.isNotBlank(userName)) {
                userName = userName.toLowerCase(Locale.ENGLISH);
            }
        }
    }

    /**
     * Returns the unique identifier of the user that is currently logged in.
     *
     * @return the unique identifier of a user, or <code>null</code>, if no user
     *         is authenticated.
     */
    public String getLoggedInUserId() {
        return userName;
    }

    /**
     * Returns the user that is currently authenticated.
     *
     * <p>
     * Note: Calling this method may invoke a remote call to the user store (i.e. an LDAP user store).
     * Hence, if knowing the <code>userId</code> of the currently logged in user is sufficient,
     * you should use {@link #getLoggedInUserId()} instead of this method.
     *
     * @return the currently logged in user, or <code>null</code>, if no user is logged in
     * (anonymous user) or the logged in user is not known to the user service.
     *
     * @throws IllegalStateException if no user service is available.
     */
    public User getLoggedInUser() {
        return UserServices.getUser(getLoggedInUserId());
    }
}
