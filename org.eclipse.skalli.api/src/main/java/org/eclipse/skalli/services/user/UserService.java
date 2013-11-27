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

import java.util.List;
import java.util.Set;

import org.eclipse.skalli.model.User;

/**
 * Service that allows to retrieve {@link User users} from an underlying user store,
 * for example from an LDAP server, based on their unique identifiers or a given
 * search query.
 */
public interface UserService {

    /**
     * Returns the type of the user service, e.g. <tt>"local"</tt> for the built-in
     * local user store, or <tt>"ldap"</tt> for an LDAP user store.
     *
     * @return  the type of the user service, never <code>null</code>. Note, the type
     * <tt>"local"</tt> is reserved for the built-in user store.
     */
    public String getType();

    /**
     * Returns all currently cached users. Note, this method <b>does not</b> load
     * all users from the user store (e.g. an LDAP directory), but only returns
     * those users that already have been cached previously.
     *
     * @return a list of cached users, or an empty list.
     */
    public List<User> getUsers();

    /**
     * Returns the user with the given unique identifier. Note, this method
     * retrieves the user from the user store (e.g. an LDAP directory), if it is
     * not already cached.
     *
     * @param userId
     *          a user's unique identifier.
     * @return the user with the given unique identifier, or an anonymous user (<code>User</code> instance
     *         without details, see {@link User#createUserWithoutDetails(String)}) if the user service
     *         cannot provide the requested user (because it does not exist, or a connection to the user
     *         store cannot be established).
     */
    public User getUserById(String userId);

    /**
     * Searches in the user store (i.e. LDAP) for users matching a given search string.
     * @param search
     *          a string to search for in the user data.
     * @return a list of users matching the given search string, or an empty list.
     */
    public List<User> findUser(String search);

    /**
     * Converts a given set of user identifiers to a set of <code>User</code>s.
     * This method ignores user identifiers that have no matching
     * <coce>User</code> entry in the user store. Note that the ordering of users
     * in the result set may be different from that in the <code>userIds</code>
     * set.
     *
     * @param userIds
     *          a set of user identifiers.
     * @return a set of <code>User</code> corresponding to the given set of unique identifiers.
     *         The result set contains anonymous users (<code>User</code> instances
     *         without details, see {@link User#createUserWithoutDetails(String)}) for those user
     *         identifiers for which the user service cannot provide a valid <code>User</code>
     *         instance (because no such user exist, or a connection to the user store cannot
     *         be established).
     */
    public Set<User> getUsersById(Set<String> userIds);

}
