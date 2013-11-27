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
package org.eclipse.skalli.core.user;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.Issuer;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.model.User;
import org.eclipse.skalli.model.ValidationException;
import org.eclipse.skalli.services.entity.EntityServiceBase;
import org.eclipse.skalli.services.extension.DataMigration;
import org.eclipse.skalli.services.user.UserService;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link UserService} based on XStream-serialized
 * {@link User} instances. The service expects to find XML files named
 * <tt>&lt;userId&gt.xml</tt> in the <tt>$workdir/storage/User</tt>
 * directory.<br>
 * Note, by default this service implementation is disabled (see component
 * descriptor <tt>LocalUserComponent</tt>). It must be enabled explicitly
 * in the OSGi shell (<tt>enable</tt> command, LDAP user service should
 * be disabled before that).
 */
public class LocalUserComponent extends EntityServiceBase<User> implements UserService, Issuer {

    private static final Logger LOG = LoggerFactory.getLogger(LocalUserComponent.class);

    private static final int CURRENT_MODEL_VERISON = 20;

    private Map<String, User> cache;

    protected void activate(ComponentContext context) {
        LOG.info(MessageFormat.format("[UserService][local] {0} : activated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    protected void deactivate(ComponentContext context) {
        LOG.info(MessageFormat.format("[UserService][local] {0} : deactivated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    @Override
    public Class<User> getEntityClass() {
        return User.class;
    }

    @Override
    public int getModelVersion() {
        return CURRENT_MODEL_VERISON;
    }

    @Override
    public Map<String, Class<?>> getAliases() {
        Map<String, Class<?>> aliases = super.getAliases();
        aliases.put("entity-user", User.class); //$NON-NLS-1$
        return aliases;
    }

    @Override
    public Set<DataMigration> getMigrations() {
        Set<DataMigration> migrations = super.getMigrations();
        migrations.add(new LocalUserDataMigration19());
        return migrations;
    }

    private synchronized Map<String, User> getCache() {
        if (cache == null) {
            cache = new HashMap<String, User>();
            for (User user: getAll()) {
                cache.put(user.getUserId(), user);
            }
        }
        return cache;
    }

    @Override
    public String getType() {
        return "local"; //$NON-NLS-1$
    }

    @Override
    public synchronized List<User> getUsers() {
        return getAll();
    }

    @Override
    public User getUserById(String userId) {
        User user = getCache().get(userId);
        return user != null ? user : new User(userId);
    }

    @Override
    public List<User> findUser(String search) {
        List<User> result = new ArrayList<User>();
        if (StringUtils.isNotBlank(search)) {
            String[] parts = StringUtils.split(NormalizeUtil.normalize(search), " ,"); //$NON-NLS-1$
            Pattern[] patterns = new Pattern[parts.length];
            for (int i = 0; i < parts.length; ++i) {
                patterns[i] = Pattern.compile(parts[i] + ".*", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE) ;//$NON-NLS-1$
            }

            for (User user : getAll()) {
                if (parts.length == 1) {
                    match(user, patterns[0], result);
                }
                else if (parts.length == 2) {
                    // givenname surname ('Michael Ochmann')
                    if (matches(patterns, user.getFirstname(), user.getLastname())) {
                        result.add(user);
                    }
                    // surname givenname('Ochmann, Michael')
                    else if (matches(patterns, user.getLastname(), user.getFirstname())) {
                        result.add(user);
                    }
                }
                else if (parts.length == 3) {
                    // givenname initial surname, e.g. 'Michael R. Ochmann'
                    // or title givenname surname or given name surname title
                    if (matches(patterns, user.getFirstname(), null, user.getLastname())) {
                        result.add(user);
                    }
                    else if (matches(patterns, user.getLastname(), null, user.getFirstname())) {
                        result.add(user);
                    }
                    else if (matches(patterns, null, user.getFirstname(), user.getLastname())) {
                        result.add(user);
                    }
                    else if (matches(patterns, user.getFirstname(), user.getLastname(), null)) {
                        result.add(user);
                    }
                }
            }
            if (result.isEmpty()) {
                for (User user : getAll()) {
                    // try to match each part individually
                    for (int i = 0; i < parts.length; ++i) {
                        match(user, patterns[i], result);
                    }
                }
            }
        }
        return result;
    }

    private boolean matches(Pattern[] pattern, String... strings) {
        for (int i = 0; i < strings.length; ++i) {
            if (StringUtils.isNotBlank(strings[i])
                && pattern[i].matcher(strings[i]).matches()) {
                    return true;
            }
        }
        return false;
    }

    private void match(User user, Pattern pattern, List<User> result) {
        Pattern[] patterns = new Pattern[] { pattern };
        // try a match with surname*
        if (matches(patterns, user.getLastname())) {
            result.add(user);
        }
        // try a match with firstname*
        else if (matches(patterns, user.getFirstname())) {
            result.add(user);
        }
        //try a match with the account name
        if (result.isEmpty() && matches(patterns, user.getUserId())) {
            result.add(user);
        }
        //try a match with the mail address
        if (result.isEmpty() && matches(patterns, user.getEmail())) {
            result.add(user);
        }
        //try a match with the department
        if (result.isEmpty() && matches(patterns, user.getDepartment())) {
            result.add(user);
        }
    }

    @Override
    public Set<User> getUsersById(Set<String> userIds) {
        Set<User> result = new HashSet<User>();
        if (userIds != null) {
            for (String userId : userIds) {
                User user = getCache().get(userId);
                if (user != null) {
                    result.add(user);
                } else {
                    result.add(new User(userId));
                }
            }
        }
        return result;
    }

    @Override
    protected void validateEntity(User entity) throws ValidationException {
        SortedSet<Issue> issues = validate(entity, Severity.FATAL);
        if (issues.size() > 0) {
            throw new ValidationException("User could not be saved due to the following reasons:", issues);
        }
    }

    @Override
    protected SortedSet<Issue> validateEntity(User entity, Severity minSeverity) {
        TreeSet<Issue> issues = new TreeSet<Issue>();
        String userId = entity.getUserId();
        if (StringUtils.isBlank(userId)) {
            issues.add(new Issue(Severity.FATAL, LocalUserComponent.class, entity.getUuid(),
                    "Users must have a unique user identifier which is not blank"));
        }
        return issues;
    }

}
