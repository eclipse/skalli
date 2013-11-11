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
package org.eclipse.skalli.core.project;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.model.ExtensibleEntityBase;
import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.Member;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.ProjectNature;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.model.ValidationException;
import org.eclipse.skalli.model.ext.commons.PeopleExtension;
import org.eclipse.skalli.services.entity.EntityServiceBase;
import org.eclipse.skalli.services.extension.ExtensionService;
import org.eclipse.skalli.services.extension.ExtensionServices;
import org.eclipse.skalli.services.extension.ExtensionValidator;
import org.eclipse.skalli.services.extension.PropertyValidator;
import org.eclipse.skalli.services.persistence.PersistenceService;
import org.eclipse.skalli.services.project.InvalidParentChainException;
import org.eclipse.skalli.services.project.ProjectNode;
import org.eclipse.skalli.services.project.ProjectService;
import org.eclipse.skalli.services.role.RoleProvider;
import org.eclipse.skalli.services.template.NoSuchTemplateException;
import org.eclipse.skalli.services.template.ProjectTemplate;
import org.eclipse.skalli.services.template.ProjectTemplateService;
import org.eclipse.skalli.services.user.UserUtils;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectComponent extends EntityServiceBase<Project> implements ProjectService {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectComponent.class);

    // TODO "central" versioning together with extensibility sucks!
    /*
     * Alternative idea: Instead of a version number just use unique IDs. A
     * migration then defines a set of other migration's ids which have to be
     * applied as a prerequisite. The Migrator then first collects all registered
     * migrations an then sorts them by their prerequisites. Hypothetis: A
     * migration logically cannot depend on another migration if the unique id of
     * that migration is not known. Open issues: - What should be used as a
     * "version" information in the persisted xml? => Maybe something like a
     * checksum (must be stable with respect to the order of calculation)
     */
    private static final int CURRENT_MODEL_VERISON = 23;


    private ProjectTemplateService projectTemplateService;
    private Set<RoleProvider> roleProviders = new HashSet<RoleProvider>();

    protected void activate(ComponentContext context) {
        LOG.info(MessageFormat.format("[ProjectService] {0} : activated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    protected void deactivate(ComponentContext context) {
        LOG.info(MessageFormat.format("[ProjectService] {0} : deactivated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    protected void bindProjectTemplateService(ProjectTemplateService projectTemplateService) {
        this.projectTemplateService = projectTemplateService;
        LOG.info(MessageFormat.format("bindProjectTemplateService({0})", projectTemplateService)); //$NON-NLS-1$
    }

    protected void unbindProjectTemplateService(ProjectTemplateService projectTemplateService) {
        LOG.info(MessageFormat.format("unbindProjectTemplateService({0})", projectTemplateService)); //$NON-NLS-1$
        this.projectTemplateService = null;
    }

    protected void bindRoleProvider(RoleProvider roleProvider) {
        roleProviders.add(roleProvider);
    }

    protected void unbindRoleProvider(RoleProvider roleProvider) {
        roleProviders.remove(roleProvider);
    }

    @Override
    public Class<Project> getEntityClass() {
        return Project.class;
    }

    @Override
    public int getModelVersion() {
        return CURRENT_MODEL_VERISON;
    }

    @Override
    public ProjectNature getProjectNature(UUID uuid) {
        ProjectTemplate projectTemplate = getProjectTemplate(uuid);
        return projectTemplate != null ? projectTemplate.getProjectNature() : null;
    }

    private ProjectTemplate getProjectTemplate(UUID uuid) {
        Project project = getByUUID(uuid);
        if (project == null || projectTemplateService == null) {
            return null;
        }
        return projectTemplateService.getProjectTemplateById(project.getProjectTemplateId());
    }

    @Override
    public Project getProjectByProjectId(String projectId) {
        for (Project p : getAll()) {
            if (p.getProjectId().equalsIgnoreCase(projectId)) {
                return p;
            }
        }
        return null;
    }

    @Override
    public List<Project> getProjects(Comparator<Project> c) {
        List<Project> projects = getAll();
        Collections.sort(projects, c);
        return projects;
    }

    @Override
    public List<Project> getProjects(List<UUID> uuids) {
        List<Project> result = new ArrayList<Project>();
        PersistenceService persistence = getPersistenceService();
        for (UUID uuid : uuids) {
            Project project = persistence.getEntity(Project.class, uuid);
            if (project != null) {
                result.add(project);
            }
        }
        return result;
    }

    @Override
    public Map<UUID, List<Project>> getSubProjects() {
        Map<UUID, List<Project>> result = new HashMap<UUID, List<Project>>();
        List<UUID> uuids = new ArrayList<UUID>(keySet());
        for (UUID uuid : uuids) {
            Project project = getByUUID(uuid);
            if (project == null) {
                continue;
            }
            Project parent = (Project) project.getParentEntity();
            if (parent == null) {
               continue;
            }
            List<Project> subprojects = result.get(parent.getUuid());
            if (subprojects == null) {
                subprojects = new ArrayList<Project>();
                result.put(parent.getUuid(), subprojects);
            }
            subprojects.add(project);
        }
        return result;
    }

    @Override
    public List<Project> getSubProjects(UUID uuid) {
        return getSubProjects(uuid, null, 1);
    }

    @Override
    public List<Project> getSubProjects(UUID uuid, Comparator<Project> c) {
        return getSubProjects(uuid, c, 1);
    }

    @Override
    public List<Project> getSubProjects(UUID uuid, Comparator<Project> c, int depth) {
        depth = depth < 0 ? Integer.MAX_VALUE : depth;
        List<Project> result = new ArrayList<Project>();
        if (depth == 0) {
            return result;
        }
        for (Project project : getAll()) {
            if (isSubproject(project, uuid, depth)) {
                result.add(project);
            }
        }
        if (c != null) {
            Collections.sort(result, c);
        }
        return result;
    }

    private boolean isSubproject(Project project, UUID uuid, int depth) {
        for (int i = 0; i < depth; i++) {
            project = (Project) project.getParentEntity();
            if (project == null) {
                return false;
            }
            UUID parentUUID = project.getUuid();
            if (uuid.equals(parentUUID)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<Project> getParentChain(UUID uuid) {
        return getParentChain(getByUUID(uuid));
    }

    private List<Project> getParentChain(Project project) {
        List<Project> result = new LinkedList<Project>();
        if (project != null) {
            result.add(project);
            UUID parentUUID = project.getParentProject();
            while (parentUUID != null) {
                Project parent = getByUUID(parentUUID);
                if (parent == null) {
                    throw new InvalidParentChainException(project.getUuid(), parentUUID);
                }
                result.add(parent);
                parentUUID = parent.getParentProject();
            }
        }
        return result;
    }

    @Override
    public Project getNearestParent(UUID uuid, ProjectNature nature) {
        UUID parentUUID = uuid;
        while (parentUUID != null) {
            Project parent = getByUUID(parentUUID);
            if (parent == null) {
                throw new InvalidParentChainException(uuid, parentUUID);
            }
            String templateId = parent.getProjectTemplateId();
            ProjectTemplate template = projectTemplateService.getProjectTemplateById(templateId);
            if (template == null) {
                throw new NoSuchTemplateException(parentUUID, templateId);
            }
            if (nature.equals(template.getProjectNature())) {
                return parent;
            }
            parentUUID = getByUUID(parentUUID).getParentProject();
        }
        return null;
    }

    @Override
    public Set<UUID> deletedSet() {
        return getPersistenceService().deletedSet(Project.class);
    }

    @Override
    public List<Project> getDeletedProjects() {
        return getPersistenceService().getDeletedEntities(Project.class);
    }

    @Override
    public List<Project> getDeletedProjects(Comparator<Project> c) {
        List<Project> projects = getDeletedProjects();
        Collections.sort(projects, c);
        return projects;
    }

    @Override
    public Project getDeletedProject(UUID uuid) {
        return getPersistenceService().getDeletedEntity(Project.class, uuid);
    }

    @Override
    public List<ProjectNode> getRootProjectNodes(Comparator<Project> c) {
        List<ProjectNode> rootNodes = new LinkedList<ProjectNode>();
        List<Project> projects = getProjects(c);
        for (Project project : projects) {
            if (project.getParentProject() == null) {
                ProjectNode projectNode = new ProjectNodeImpl(this, project, c);
                rootNodes.add(projectNode);
            }
        }
        return rootNodes;
    }

    @Override
    public ProjectNode getProjectNode(UUID uuid, Comparator<Project> c) {
        return new ProjectNodeImpl(this, uuid, c);
    }

    @Override
    protected void validateEntity(Project entity) throws ValidationException {
        SortedSet<Issue> issues = validate(entity, Severity.FATAL);
        if (issues.size() > 0) {
            throw new ValidationException("Project could not be saved due to the following reasons:", issues);
        }
    }

    /**
    * Validates the given project.
    * Checks basic data like project ID, template, project lead etc.
    * Furthermore, validates the project with the default property/extension validators provided by
    * <code>ExtensionServiceCore</code> and the extension services of the assigned extensions and
    * with the custom validators provided by the {@link ProjectTemplate project template}.
     */
    @Override
    protected SortedSet<Issue> validateEntity(Project project, Severity minSeverity) {
        SortedSet<Issue> issues = new TreeSet<Issue>();

        issues.addAll(validateProjectId(project));
        issues.addAll(validateProjectName(project));
        issues.addAll(validatePeopleExtension(project));

        // ensure that the entity service exists
        UUID projectUUID = project.getUuid();
        ExtensionService<?> extensionService = validateExtensionService(projectUUID, project, issues);

        // ensure that the project template exists
        ProjectTemplate projectTemplate = validateProjectTemplate(project, issues);

        if (extensionService != null && projectTemplate != null) {
            // use the validators provided by the project template/extension services to validate the project
            validateExtension(projectUUID, project, extensionService, projectTemplate, issues, minSeverity);

            // check that all extensions are compatible with the template and vice versa
            if (minSeverity.compareTo(Severity.ERROR) >= 0) {
                validateCompatibility(project, projectTemplate, extensionService, issues);
            }
        }
        return issues;
    }

    private SortedSet<Issue> validatePeopleExtension(Project project) {
        // ensure that the project has a PeopleProjectExt and a project lead
        SortedSet<Issue> issues = new TreeSet<Issue>();
        PeopleExtension peopleExtension = project.getExtension(PeopleExtension.class);
        if (peopleExtension == null) {
            issues.add(new Issue(Severity.FATAL, ProjectService.class, project.getUuid(),
                    PeopleExtension.class, null,
                    "Project must have a Project Members extension or inherit it from a parent"));
        } else if (peopleExtension.getLeads().isEmpty()) {
            issues.add(new Issue(Severity.FATAL, ProjectService.class, project.getUuid(),
                    PeopleExtension.class, PeopleExtension.PROPERTY_LEADS,
                    "Project must have a least one Project Lead"));
        }
        return issues;
    }

    private SortedSet<Issue> validateProjectId(Project project) {
        SortedSet<Issue> issues = new TreeSet<Issue>();
        String projectId = project.getProjectId();
        if (StringUtils.isBlank(projectId)) {
            issues.add(new Issue(Severity.FATAL, ProjectService.class, project.getUuid(),
                    Project.class, Project.PROPERTY_PROJECTID, 1, "Project must have a Project ID"));
        }
        else {
            if (projectId.trim().length() != projectId.length()) {
                issues.add(new Issue(Severity.FATAL, ProjectService.class, project.getUuid(),
                        Project.class, Project.PROPERTY_PROJECTID, 2,
                        "Project ID must not have leading or trailing whitespaces"));
            }
            else {
                for (Project anotherProject : getAll()) {
                    String anotherProjectId = anotherProject.getProjectId();
                    if (projectId.equals(anotherProjectId) && !anotherProject.getUuid().equals(project.getUuid())) {
                        issues.add(new Issue(Severity.FATAL, ProjectService.class, project.getUuid(),
                                Project.class, Project.PROPERTY_PROJECTID, 3,
                                MessageFormat.format("Project with Project ID ''{0}'' already exists", projectId)));
                        break;
                    }
                }
            }
        }
        return issues;
    }

    private SortedSet<Issue> validateProjectName(Project project) {
        SortedSet<Issue> issues = new TreeSet<Issue>();
        String name = project.getName();
        if (StringUtils.isBlank(name)) {
            issues.add(new Issue(Severity.FATAL, ProjectService.class, project.getUuid(),
                    Project.class, Project.PROPERTY_NAME, "Projects must have a Display Name"));
        }
        return issues;
    }

    private ProjectTemplate validateProjectTemplate(Project project, Set<Issue> issues) {
        ProjectTemplate projectTemplate = projectTemplateService.getProjectTemplateById(project.getProjectTemplateId());
        if (projectTemplate == null) {
            issues.add(new Issue(Severity.FATAL, ProjectService.class, project.getUuid(),
                    Project.class, Project.PROPERTY_TEMPLATEID,
                    MessageFormat.format(
                            "Project references project template ''{0}'' but such a template is not registered",
                            project.getProjectTemplateId())));
        }
        validateDirectParent(projectTemplate, project, issues);
        validateAllowedParents(projectTemplate, project, issues);
        return projectTemplate;
    }

    private void validateDirectParent(ProjectTemplate projectTemplate, Project project, Set<Issue> issues) {
        UUID directParent = projectTemplate.getDirectParent();
        if (directParent != null && !directParent.equals(project.getParentEntityId())) {
            issues.add(new Issue(Severity.FATAL, ProjectService.class, project.getUuid(),
                    Project.class, Project.PROPERTY_PARENT_ENTITY_ID,
                    MessageFormat.format(
                            "Project assigned to project template ''{0}'' must be direct subproject of project ''{1}''",
                            project.getProjectTemplateId(), directParent)));
        }
    }

    private void validateAllowedParents(ProjectTemplate projectTemplate, Project project, Set<Issue> issues) {
        Set<UUID> allowedParents = projectTemplate.getAllowedParents();
        if (CollectionUtils.isNotBlank(allowedParents)) {
            boolean hasAllowedParent = false;
            for (Project parent: getParentChain(project.getUuid())) {
                if (allowedParents.contains(parent.getUuid())) {
                    hasAllowedParent = true;
                    break;
                }
            }
            if (!hasAllowedParent) {
                issues.add(new Issue(Severity.FATAL, ProjectService.class, project.getUuid(),
                        Project.class, Project.PROPERTY_PARENT_ENTITY_ID,
                        MessageFormat.format(
                                "Project assigned to project template ''{0}'' must be subproject of any of ''{1}''",
                                projectTemplate.getId(), CollectionUtils.toString(allowedParents, ','))));
            }
        }
    }

    private ExtensionService<?> validateExtensionService(UUID projectUUID, ExtensionEntityBase ext, Set<Issue> issues) {
        Class<? extends ExtensionEntityBase> extensionClass = ext.getClass();
        ExtensionService<?> extensionService = ExtensionServices.getByExtensionClass(extensionClass);
        if (extensionService == null) {
            issues.add(new Issue(Severity.FATAL, ProjectService.class, projectUUID,
                    MessageFormat.format("Project references model extension ''{0}'' but there is no " +
                            "corresponding extension service registered", extensionClass.getName())));
        }
        return extensionService;
    }

    private void validateCompatibility(Project project, ProjectTemplate projectTemplate,
            ExtensionService<?> extensionService, Set<Issue> issues) {
        Set<String> allowed = extensionService.getProjectTemplateIds();
        Set<String> included = projectTemplate.getIncludedExtensions();
        Set<String> excluded = projectTemplate.getExcludedExtensions();
        if (allowed != null || included != null || excluded != null) {
            UUID projectUUID = project.getUuid();
            for (ExtensionEntityBase extension : project.getAllExtensions()) {
                String extensionClassName = extension.getClass().getName();
                if (allowed != null && !allowed.contains(projectTemplate.getId())) {
                    issues.add(new Issue(
                            Severity.ERROR, ProjectTemplate.class, projectUUID, extension.getClass(), null,
                            MessageFormat.format("{0} projects are not compatible with ''{1}'' extensions. " +
                                    "Disable the extension or select another project template.",
                                    projectTemplate.getDisplayName(), extensionClassName)));
                }
                if (excluded != null && excluded.contains(extensionClassName) ||
                        included != null && !included.contains(extensionClassName)) {
                    issues.add(new Issue(
                            Severity.ERROR, ProjectTemplate.class, projectUUID, extension.getClass(), null,
                            MessageFormat.format("''{0}'' extensions are not appropriate for {1} projects. " +
                                    "Disable the extension or select another project template.",
                                    extensionClassName, projectTemplate.getDisplayName())));
                }
            }
        }
    }

    private void validateExtension(UUID projectUUID, ExtensionEntityBase ext, ProjectTemplate projectTemplate,
            Set<Issue> issues, Severity minSeverity) {
        ExtensionService<?> extensionService = validateExtensionService(projectUUID, ext, issues);
        if (extensionService != null) {
            validateExtension(projectUUID, ext, extensionService, projectTemplate, issues, minSeverity);
        }
    }

    private void validateExtension(UUID projectUUID, ExtensionEntityBase ext, ExtensionService<?> extensionService,
            ProjectTemplate projectTemplate, Set<Issue> issues, Severity minSeverity) {
        String extensionClassName = extensionService.getExtensionClass().getName();

        Map<String, String> captions = new HashMap<String, String>();

        Set<String> propertyNames = ext.getPropertyNames();
        for (String propertyName : propertyNames) {

            // determine a suitable caption for the property, either from the template or the
            // extension service; if neither is availablle, pass null to the validators
            String caption = projectTemplate.getCaption(extensionClassName, propertyName);
            if (StringUtils.isBlank(caption)) {
                caption = extensionService.getCaption(propertyName);
            }
            captions.put(propertyName, caption);

            List<PropertyValidator> propertyValidators = new ArrayList<PropertyValidator>();
            List<PropertyValidator> defaultValidators = extensionService.getPropertyValidators(propertyName, caption);
            if (defaultValidators == null) {
                LOG.warn(MessageFormat.format(
                        "{0}#getPropertyValidators({1}) returned null, but is expected to return an empty set",
                        extensionService.getClass().getName(), propertyName));
            } else {
                propertyValidators.addAll(defaultValidators);
            }
            List<PropertyValidator> customValidators = projectTemplate.getPropertyValidators(extensionClassName,
                    propertyName);
            if (customValidators == null) {
                LOG.warn(MessageFormat.format(
                        "{0}#getPropertyValidators({1}, {2}) returned null, but is expected to return an empty set",
                        projectTemplate.getClass().getName(), extensionClassName, propertyName));
            } else {
                propertyValidators.addAll(customValidators);
            }
            for (PropertyValidator propertyValidator : propertyValidators) {
                try {
                    issues.addAll(propertyValidator.validate(projectUUID, ext.getProperty(propertyName), minSeverity));
                } catch (RuntimeException e) {
                    LOG.error(MessageFormat.format("{0}#validate on project {1} threw an exception", propertyValidator
                            .getClass().getName(), projectUUID), e);
                }
            }
        }

        issues.addAll(validateExtensionServiceExtensionValidators(projectUUID, ext, extensionService, projectTemplate,
                minSeverity, captions));
        issues.addAll(validateProjectTemplateExtensionValidators(projectUUID, ext, projectTemplate, minSeverity));

        // if this extension is extensible, recursively validate all extensions
        if (ext instanceof ExtensibleEntityBase) {
            for (ExtensionEntityBase extension : ((ExtensibleEntityBase) ext).getAllExtensions()) {
                validateExtension(projectUUID, extension, projectTemplate, issues, minSeverity);
            }
        }
    }

    private Set<Issue> validateExtensionServiceExtensionValidators(UUID projectUUID, ExtensionEntityBase ext,
            ExtensionService<?> extensionService, ProjectTemplate projectTemplate, Severity minSeverity,
            Map<String, String> captions) {
        Set<Issue> issues = new TreeSet<Issue>();

        List<? extends ExtensionValidator<?>> extensionValidators = extensionService.getExtensionValidators(captions);
        if (extensionValidators == null) {
            LOG.warn(MessageFormat.format(
                    "{0}#getExtensionValidators() returned null, but is expected to return an empty set",
                    projectTemplate.getClass().getName()));
        } else {
            for (ExtensionValidator<?> extensionValidator : extensionValidators) {
                try {
                    issues.addAll(extensionValidator.validate(projectUUID, ext, minSeverity));
                } catch (RuntimeException e) {
                    LOG.error(MessageFormat.format("{0}#validate on project {1} threw an exception", extensionValidator
                            .getClass().getName(), projectUUID), e);
                }
            }
        }
        return issues;
    }

    private Set<Issue> validateProjectTemplateExtensionValidators(UUID uuid, ExtensionEntityBase ext,
            ProjectTemplate projectTemplate, Severity minSeverity) {
        Set<Issue> issues = new TreeSet<Issue>();
        List<ExtensionValidator<?>> extentionValidators = projectTemplate.getExtensionValidators(ext.getClass()
                .getName());
        if (extentionValidators == null) {
            LOG.warn(MessageFormat.format(
                    "{0}#getExtensionValidators({1}) returned null, but is expected to return an empty set",
                    projectTemplate.getClass().getName(), ext.getClass().getName()));
        } else {
            for (ExtensionValidator<?> extensionValidator : extentionValidators) {
                try {
                    issues.addAll(extensionValidator.validate(uuid, ext, minSeverity));
                } catch (RuntimeException e) {
                    LOG.error(MessageFormat.format("{0}#validate on project {1} threw an exception", extensionValidator
                            .getClass().getName(), uuid), e);
                }
            }
        }
        return issues;
    }

    @Override
    public SortedSet<Member> getMembers(UUID uuid) {
        TreeSet<Member> ret = new TreeSet<Member>();
        Project project = getByUUID(uuid);
        if (project != null) {
            for (RoleProvider roleProvider : roleProviders) {
                ret.addAll(roleProvider.getMembers(project));
            }
        }
        return ret;
    }

    @Override
    public SortedSet<Member> getMembers(UUID uuid, String... roles) {
        TreeSet<Member> ret = new TreeSet<Member>();
        Project project = getByUUID(uuid);
        if (project != null) {
            for (RoleProvider roleProvider : roleProviders) {
                ret.addAll(roleProvider.getMembers(project, roles));
            }
        }
        return ret;
    }

    @Override
    public Map<String, SortedSet<Member>> getMembersByRole(UUID uuid) {
        Map<String, SortedSet<Member>> ret = new HashMap<String, SortedSet<Member>>();
        Project project = getByUUID(uuid);
        if (project != null) {
            for (RoleProvider roleProvider : roleProviders) {
                ret.putAll(roleProvider.getMembersByRole(project));
            }
        }
        return ret;
    }

    @Override
    public Project createProject(final String templateId, final String userId) {
        final Project project = (StringUtils.isNotBlank(templateId)) ? new Project(templateId) : new Project();
        project.setUuid(UUID.randomUUID());

        if (StringUtils.isNotBlank(userId) && UserUtils.getUser(userId) != null) {
            PeopleExtension peopleExt = new PeopleExtension();
            peopleExt.getLeads().add(new Member(userId));
            project.addExtension(peopleExt);
        }

        final ProjectTemplate template = projectTemplateService.getProjectTemplateById(templateId);

        if (template != null) {
            for (Class<? extends ExtensionEntityBase> extensionClass : projectTemplateService.getSelectableExtensions(
                    template, null)) {
                String extensionClassName = extensionClass.getName();
                if (template.isEnabled(extensionClassName)) {
                    try {
                        if (project.getExtension(extensionClass) == null) {
                            project.addExtension(extensionClass.cast(extensionClass.newInstance()));
                        }
                    } catch (InstantiationException e) {
                        LOG.warn(MessageFormat.format("Extension ''{0}'' could not be instantiated: {1}",
                                extensionClassName, e.getMessage()));
                    } catch (IllegalAccessException e) {
                        LOG.warn(MessageFormat.format("Extension ''{0}'' could not be instantiated: {1}",
                                extensionClassName, e.getMessage()));
                    }
                }
            }
        }

        return project;
    }
}