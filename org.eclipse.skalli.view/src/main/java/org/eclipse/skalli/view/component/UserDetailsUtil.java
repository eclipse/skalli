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
package org.eclipse.skalli.view.component;

import java.util.IllegalFormatConversionException;

import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.configuration.ConfigurationService;
import org.eclipse.skalli.view.internal.config.UserDetailsConfig;
import org.eclipse.skalli.view.internal.config.UserDetailsResource;

public class UserDetailsUtil {

    /**
     * Returns a link to the user details page of a given user, or <code>null</code>
     * of there is no customization fopr user details defined, or the user is unknown.
     */
    public static String getUserDetailsLink(String userId) {
        ConfigurationService confService = Services.getService(ConfigurationService.class);
        if (confService != null) {
            UserDetailsConfig userDetailsConfig = confService.readCustomization(UserDetailsResource.KEY,
                    UserDetailsConfig.class);
            if (userDetailsConfig != null) {
                try {
                    // the configured base url can have a placeholder for
                    // the user ID (e.g. [http://show.user.com/userId=%s]),
                    // try to format with passed user ID and return
                    return String.format(userDetailsConfig.getUrl(), userId);
                } catch (IllegalFormatConversionException e) {
                    // user details base url seems to not contain any placeholder for
                    // the user ID, return the base url in this case.
                    return userDetailsConfig.getUrl();
                }
            }
        }
        return null;
    }
}
