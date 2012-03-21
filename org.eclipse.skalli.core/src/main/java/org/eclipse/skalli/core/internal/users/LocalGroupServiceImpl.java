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
package org.eclipse.skalli.core.internal.users;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.skalli.model.Group;
import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.model.ValidationException;
import org.eclipse.skalli.services.entity.EntityServiceBase;
import org.eclipse.skalli.services.extension.DataMigration;
import org.eclipse.skalli.services.group.GroupService;
import org.eclipse.skalli.services.persistence.EntityFilter;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link GroupService} using {@link Group} instances.
 * The service expects to find XML files named
 * <tt>&lt;groupId&gt.xml</tt> in the <tt>$workdir/storage/Group</tt>
 * directory.
 */
public class LocalGroupServiceImpl extends EntityServiceBase<Group> implements GroupService {

    private static final Logger LOG = LoggerFactory.getLogger(LocalGroupServiceImpl.class);

    private static final int CURRENT_MODEL_VERISON = 20;

    @Override
    protected void activate(ComponentContext context) {
        LOG.info(MessageFormat.format("[GroupService][type=local] {0} : activated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    @Override
    protected void deactivate(ComponentContext context) {
        LOG.info(MessageFormat.format("[GroupService][type=local] {0} : deactivated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    @Override
    public Class<Group> getEntityClass() {
        return Group.class;
    }

    @Override
    public int getModelVersion() {
        return CURRENT_MODEL_VERISON;
    }

    @Override
    public Map<String, Class<?>> getAliases() {
        Map<String, Class<?>> aliases = super.getAliases();
        aliases.put("entity-group", Group.class); //$NON-NLS-1$
        return aliases;
    }

    @Override
    public Set<DataMigration> getMigrations() {
        Set<DataMigration> migrations = super.getMigrations();
        migrations.add(new LocalGroupDataMigration19());
        return migrations;
    }

    @Override
    public boolean isAdministrator(String userId) {
        return isMemberOfGroup(userId, ADMIN_GROUP);
    }

    @Override
    public boolean isMemberOfGroup(final String userId, final String groupId) {
        Group group = getGroup(groupId);
        return group != null ? group.hasGroupMember(userId) : false;
    }

    @Override
    public List<Group> getGroups() {
        return getAll();
    }

    @Override
    protected void validateEntity(Group entity) throws ValidationException {
        SortedSet<Issue> issues = validate(entity, Severity.FATAL);
        if (issues.size() > 0) {
            throw new ValidationException("Group could not be saved due to the following reasons:", issues);
        }
    }

    @Override
    protected SortedSet<Issue> validateEntity(Group entity, Severity minSeverity) {
        return new TreeSet<Issue>();
    }

    @Override
    public Group getGroup(final String groupId) {
        Group group = getPersistenceService().getEntity(Group.class, new EntityFilter<Group>() {
            @Override
            public boolean accept(Class<Group> entityClass, Group entity) {
                return entity.getGroupId().equals(groupId);
            }
        });
        return group;
    }

    @Override
    public List<Group> getGroups(String userId) {
        List<Group> groups = new ArrayList<Group>();
        for (Group group: getAll()) {
            if (group.hasGroupMember(userId)) {
                groups.add(group);
            }
        }
        return groups;
    }
}
