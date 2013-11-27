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
package org.eclipse.skalli.testutil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.User;
import org.eclipse.skalli.services.user.UserService;

/**
 * Simple {@link UserService} implementation derived from {@link HashMap}.
 */
@SuppressWarnings("nls")
public class HashMapUserService extends HashMap<String, User> implements UserService {

    private static final long serialVersionUID = -2289396624054177002L;

    @Override
    public String getType() {
        return "HashMapUserService";
    }

    @Override
    public List<User> getUsers() {
        return new ArrayList<User>(values());
    }

    @Override
    public User getUserById(String userId) {
        User user = get(userId);
        return user != null ? user : new User(userId);
    }

    /**
     * Simplified find alogorthmus comparing the given search term
     * with userId, firstname and lastname (in this order).
     */
    @Override
    public List<User> findUser(String search) {
        List<User> result = new ArrayList<User>(values());
        if (StringUtils.isNotBlank(search)) {
            for (User user : values()) {
                if (search.equals(user.getUserId())
                        || search.equals(user.getFirstname())
                        || search.equals(user.getLastname())) {
                    result.add(user);
                }
            }
        }
        return result;
    }

    @Override
    public Set<User> getUsersById(Set<String> userIds) {
        Set<User> result = new HashSet<User>();
        if (userIds != null) {
            for (String userId : userIds) {
                User user = get(userId);
                if (user != null) {
                    result.add(user);
                } else {
                    result.add(new User(userId));
                }
            }
        }
        return result;
    }

}
