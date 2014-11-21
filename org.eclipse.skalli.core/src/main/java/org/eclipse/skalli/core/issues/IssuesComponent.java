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
package org.eclipse.skalli.core.issues;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.model.ValidationException;
import org.eclipse.skalli.services.entity.EntityServiceBase;
import org.eclipse.skalli.services.extension.DataMigration;
import org.eclipse.skalli.services.issues.Issues;
import org.eclipse.skalli.services.issues.IssuesService;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IssuesComponent extends EntityServiceBase<Issues> implements IssuesService {

    private static final Logger LOG = LoggerFactory.getLogger(IssuesComponent.class);

    private static final int CURRENT_MODEL_VERISON = 20;

    protected void activate(ComponentContext context) {
        LOG.info(MessageFormat.format("[IssuesService] {0} : activated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    protected void deactivate(ComponentContext context) {
        LOG.info(MessageFormat.format("[IssuesService] {0} : deactivated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    @Override
    public Class<Issues> getEntityClass() {
        return Issues.class;
    }

    @Override
    public int getModelVersion() {
        return CURRENT_MODEL_VERISON;
    }

    @Override
    public Map<String, Class<?>> getAliases() {
        Map<String, Class<?>> aliases = super.getAliases();
        aliases.put("entity-issues", Issues.class); //$NON-NLS-1$
        aliases.put("issue", Issue.class); //$NON-NLS-1$
        return aliases;
    }

    @Override
    public Set<DataMigration> getMigrations() {
        Set<DataMigration> migrations = super.getMigrations();
        migrations.add(new IssuesDataMigration19());
        return migrations;
    }

    @Override
    protected void validateEntity(Issues entity) throws ValidationException {
        SortedSet<Issue> issues = validate(entity, Severity.FATAL);
        if (issues.size() > 0) {
            throw new ValidationException("Issues could not be saved due to the following reasons:", issues);
        }
    }

    @Override
    protected SortedSet<Issue> validateEntity(Issues entity, Severity minSeverity) {
        TreeSet<Issue> issues = new TreeSet<Issue>();
        UUID entityId = entity.getUuid();
        if (entityId == null) {
            issues.add(new Issue(Severity.FATAL, IssuesService.class, entity.getUuid(),
                    "Issues instance is not associated with an entity"));
        }
        for (Issue issue : entity.getIssues()) {
            // don't accept issues for other entities
            if (!issue.getEntityId().equals(entityId)) {
                issues.add(new Issue(Severity.FATAL, IssuesService.class, entityId,
                        MessageFormat.format("Invalid issue detected (requested entity={0} but found entity={1})",
                                issue.getEntityId(), entity.getUuid())));
            }
            // if the issue has no timestamp, set the current system time
            long timestamp = issue.getTimestamp();
            if (timestamp <= 0) {
                issue.setTimestamp(System.currentTimeMillis());
            }
        }
        return issues;
    }

    @Override
    public void persist(UUID entityId, Collection<Issue> issues, String userId) throws ValidationException {
        Issues entity = new Issues(entityId, issues);
        persist(entity, userId);
    }
}
