/*******************************************************************************
 * Copyright (c) 2010 - 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.model.ext.maven.internal.recommendedupdatesites;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.model.ValidationException;
import org.eclipse.skalli.model.ext.maven.recommendedupdatesites.RecommendedUpdateSites;
import org.eclipse.skalli.model.ext.maven.recommendedupdatesites.RecommendedUpdateSitesService;
import org.eclipse.skalli.model.ext.maven.recommendedupdatesites.UpdateSite;
import org.eclipse.skalli.services.entity.EntityServiceBase;
import org.eclipse.skalli.services.persistence.EntityFilter;
import org.eclipse.skalli.services.project.ProjectService;
import org.eclipse.skalli.services.user.UserUtils;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecommendedUpdateSitesServiceImpl extends EntityServiceBase<RecommendedUpdateSites> implements
        RecommendedUpdateSitesService {

    private static final Logger LOG = LoggerFactory.getLogger(RecommendedUpdateSitesServiceImpl.class);

    private final Pattern pattern = Pattern.compile("[A-Za-z0-9_\\-]([A-Za-z0-9_\\-.]*[A-Za-z0-9_\\-])?");//$NON-NLS-1$

    private ProjectService projectService;

    protected void activate(ComponentContext context) {
        LOG.info(MessageFormat.format("[RecommendedUpdateSitesService] {0} : activated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    protected void deactivate(ComponentContext context) {
        LOG.info(MessageFormat.format("[RecommendedUpdateSitesService] {0} : deactivated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    protected void bindProjectService(ProjectService projectService) {
        LOG.info("Project service injected into recommended update sites service"); //$NON-NLS-1$
        this.projectService = projectService;
    }

    protected void unbindProjectService(ProjectService projectService) {
        LOG.info("Project service removed from recommended update sites service"); //$NON-NLS-1$
        this.projectService = null;
    }

    @Override
    public Class<RecommendedUpdateSites> getEntityClass() {
        return RecommendedUpdateSites.class;
    }


    @Override
    public int getModelVersion() {
        return 0;
    }

    @Override
    public Map<String, Class<?>> getAliases() {
        Map<String, Class<?>> aliases = super.getAliases();
        aliases.put("entity-recommendedupdatesites", RecommendedUpdateSites.class); //$NON-NLS-1$
        aliases.put("updatesite", UpdateSite.class); //$NON-NLS-1$
        return aliases;
    }

    /* (non-Javadoc)
     * @see org.eclipse.skalli.api.java.EntityServiceImpl#validateEntity(org.eclipse.skalli.model.ext.EntityBase)
     */
    @Override
    protected void validateEntity(RecommendedUpdateSites entity) throws ValidationException {
        SortedSet<Issue> issues = validate(entity, Severity.FATAL);
        if (issues.size() > 0) {
            throw new ValidationException(
                    "Recommended update sites could not be saved because of the following reasons:", issues);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.skalli.api.java.EntityServiceImpl#validateEntity(org.eclipse.skalli.model.ext.EntityBase, org.eclipse.skalli.model.ext.Severity)
     */
    @Override
    protected SortedSet<Issue> validateEntity(RecommendedUpdateSites entity, Severity minSeverity) {
        SortedSet<Issue> issues = new TreeSet<Issue>();

        issues.addAll(validateValueIsNotBlank(entity));

        issues.addAll(validateUserId(entity));
        List<UpdateSite> updateSites = entity.getUpdateSites();
        for (UpdateSite updateSite : updateSites) {
            issues.addAll(validateMavenArtifactProperty(entity, updateSite.getGroupId(), "groupId"));
            issues.addAll(validateMavenArtifactProperty(entity, updateSite.getArtifactId(), "artifactId"));
            issues.addAll(validateProjectUUID(entity, updateSite));
            issues.addAll(validateShortName(entity));
        }
        return issues;
    }

    /* (non-Javadoc)
     * @see org.eclipse.skalli.api.java.RecommendedUpdateSitesService#getRecommendedUpdateSitesService(java.lang.String)
     */
    @Override
    public RecommendedUpdateSites getRecommendedUpdateSites(String userId, String updateSiteId) {
        RecommendedUpdateSites sites = getPersistenceService().getEntity(RecommendedUpdateSites.class,
                new RecommendedUpdateSitesFilter(userId, updateSiteId));
        return sites;
    }

    @Override
    public RecommendedUpdateSites getRecommendedUpdateSites(String shortName) {
        RecommendedUpdateSites sites = getPersistenceService().getEntity(RecommendedUpdateSites.class,
                new RecommendedUpdateSitesShortNameFilter(shortName));
        return sites;
    }

    protected static class RecommendedUpdateSitesFilter implements EntityFilter<RecommendedUpdateSites> {
        private String updateSiteId;
        private String userId;

        public RecommendedUpdateSitesFilter(String userId, String updateSiteId) {
            this.userId = userId;
            this.updateSiteId = updateSiteId;
        }

        @Override
        public boolean accept(Class<RecommendedUpdateSites> entityClass, RecommendedUpdateSites entity) {
            return entity.getId().equals(updateSiteId) && entity.getUserId().equals(userId);
        }
    }

    protected static class RecommendedUpdateSitesShortNameFilter implements EntityFilter<RecommendedUpdateSites> {
        private String shortName;

        public RecommendedUpdateSitesShortNameFilter(String shortName) {
            this.shortName = shortName;
        }

        @Override
        public boolean accept(Class<RecommendedUpdateSites> entityClass, RecommendedUpdateSites entity) {
            return shortName.equals(entity.getShortName());
        }
    }

    private SortedSet<Issue> validateValueIsNotBlank(RecommendedUpdateSites entity) {
        SortedSet<Issue> issues = new TreeSet<Issue>();
        if (StringUtils.isBlank(entity.getName())) {
            issues.add(new Issue(Severity.FATAL, RecommendedUpdateSitesService.class, entity.getUuid(),
                    "Recommended update sites must have a name"));
        }
        return issues;
    }

    private SortedSet<Issue> validateMavenArtifactProperty(RecommendedUpdateSites entity, String value,
            String propertyName) {
        SortedSet<Issue> issues = new TreeSet<Issue>();
        Matcher matcher = pattern.matcher(value);
        if (!matcher.matches()) {
            issues.add(new Issue(Severity.FATAL, RecommendedUpdateSitesService.class, entity.getUuid(),
                    MessageFormat.format("Value of property ''{0}'' does not match the pattern {1}", propertyName,
                            pattern.toString())));
        }
        return issues;
    }

    private SortedSet<Issue> validateProjectUUID(RecommendedUpdateSites entity, UpdateSite updateSite) {
        SortedSet<Issue> issues = new TreeSet<Issue>();
        UUID uuid = updateSite.getProjectUUID();
        if (uuid == null || StringUtils.isBlank(uuid.toString())) {
            issues.add(new Issue(Severity.FATAL, RecommendedUpdateSitesService.class, entity.getUuid(),
                    "Update site must have a project UUID"));
        } else {
            if (projectService != null && projectService.getByUUID(uuid) == null) {
                issues.add(new Issue(Severity.FATAL, RecommendedUpdateSitesService.class, entity.getUuid(),
                        MessageFormat.format("Update site \"{0}\" has invalid project UUID \"{1}\"",
                                updateSite.getName(), updateSite.getProjectUUID())));
            }
        }
        return issues;
    }

    private SortedSet<Issue> validateShortName(RecommendedUpdateSites entity) {
        SortedSet<Issue> issues = new TreeSet<Issue>();
        String shortName = entity.getShortName();
        if (StringUtils.isNotBlank(shortName)) {
            RecommendedUpdateSites existing = getRecommendedUpdateSites(shortName);
            if (existing != null && !existing.getUuid().equals(entity.getUuid())) {
                issues.add(new Issue(Severity.FATAL, RecommendedUpdateSitesService.class, entity.getUuid(),
                        MessageFormat.format(
                                "Provided short name ''{0}'' is already used by recommendation ''{1}'' of user {2}",
                                shortName, existing.getId(), existing.getUserId())));
            }
        }
        return issues;
    }

    private SortedSet<Issue> validateUserId(RecommendedUpdateSites entity) {
        SortedSet<Issue> issues = new TreeSet<Issue>();
        String userId = entity.getUserId();
        if (StringUtils.isBlank(userId)) {
            issues.add(new Issue(Severity.FATAL, RecommendedUpdateSitesService.class, entity.getUuid(),
                    "Recommended update sites must have a user ID"));
        } else if (UserUtils.getUser(userId) == null) {
            issues.add(new Issue(Severity.FATAL, RecommendedUpdateSitesService.class, entity.getUuid(),
                    MessageFormat.format("Provided userId: {0} is invalid", userId)));
        }
        return issues;
    }
}
