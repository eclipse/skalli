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
package org.eclipse.skalli.services.project;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.UUID;

import org.eclipse.skalli.model.Issuer;
import org.eclipse.skalli.model.Member;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.ProjectNature;
import org.eclipse.skalli.services.entity.EntityService;

/**
 * Service that allows to retrieve projects, for example based on a project's
 * UUID, project identifier or by a part of its display name.
 */
public interface ProjectService extends EntityService<Project>, Issuer {

    /**
     * Returns a new project with all extensions defined in its template.
     *
     * @param templateId
     *          the template (ID) to use
     * @param userId
     *          the user that shall be added as project lead, might be null
     *
     * @return a project, never null
     */
    public Project createProject(String templateId, String userId);

    /**
     * Returns the nature of the given project, i.e whether it is
     * a component or a project.
     *
     * @param uuid  project UUID (see
     *          {@link org.eclipse.skalli.services.projects.Project#getUuid()})
     * @return the nature of the given project, or <code>null</code> if the
     *         project does not exist.
     */
    public ProjectNature getProjectNature(UUID uuid);

    /**
     * Returns a sorted list of all currently existing projects.
     *
     * @param c
     *          the comparator to use to sort the result.
     *
     * @return  a list of projects, or an empty list.
     */
    public List<Project> getProjects(Comparator<Project> c);

    /**
     * Returns projects specified by a list of unique identifiers.
     * Note that the method ignores unique identifiers for which
     * no project entity is available. The result may therefore contain
     * less entries than the <code>uuids</code> list.
     *
     * @param uuids
     *          a list of project UUIDs (see
     *          {@link org.eclipse.skalli.services.projects.Project#getUuid()}).
     *
     * @return  a list of projects matching the given unique identifiers.
     */
    public List<Project> getProjects(List<UUID> uuids);

    /**
     * returns a sorted list of root project nodes that can be used to
     * traverse the project hierarchy.
     *
     * @param c the comparator to use to sort the result list
     * @return sorted list of project nodes (see
     *          {@link org.eclipse.skalli.services.projects.ProjectNode})
     */
    public List<ProjectNode> getRootProjectNodes(Comparator<Project> c);

    /**
     * returns a project node for a certain project UUID that can be used to
     * traverse the project hierarchy starting from this project
     *
     * @param uuid project UUID (see
     *          {@link org.eclipse.skalli.services.projects.Project#getUuid()})
     * @param c the comparator to use to sort the child nodes of the returned
     *          <code>ProjectNode</code>
     * @return project node (see
     *          {@link org.eclipse.skalli.services.projects.ProjectNode})
     */
    public ProjectNode getProjectNode(UUID uuid, Comparator<Project> c);

    /**
     * Returns all subprojects mapped to their respective parent projects.
     * @return a map of subproject lists, or an empty map.
     */
    public Map<UUID, List<Project>> getSubProjects();

    /**
     * Returns the subprojects of the given project sorted by their
     * {@link Project#getProjectId() symbolic names}.
     * <p>
     * Note, if the {@link Project project instance} is known, the subprojects
     * can directly be retrieved with {@link Project#getSubProjects()}.
     *
     * @param uuid
     *          a project's UUID (see
     *          {@link org.eclipse.skalli.services.projects.Project#getUuid()}).
     *
     * @return  projects that reference the given project as parent project, or an empty set.
     */
    public SortedSet<Project> getSubProjects(UUID uuid);

    /**
     * Returns a sorted set of subprojects of the given project.
     * If no comparator is specified the result is sorted by {@link Project#getProjectId() symbolic names}.
     * This method is equivalent to calling {@link #getSubProjects(UUID, Comparator, int)} with
     * <code>depth</code> 1.
     * <p>
     * Note, if the {@link Project project instance} is known, the subprojects
     * can directly be retrieved with {@link Project#getSubProjects(Comparator)}.

     *
     * @param uuid
     *          a project's UUID (see
     *          {@link org.eclipse.skalli.services.projects.Project#getUuid()}).
     * @param c the comparator to use to sort the returned projects, or <code>null</code>
     *          if the default order should be used.
     *
     * @return  projects that reference the given project as parent project, or an empty set.
     */
    public SortedSet<Project> getSubProjects(UUID uuid, Comparator<Project> c);

    /**
     * Returns a sorted set of subprojects of the given project and depth of search.
     * If no comparator is specified the result is sorted by {@link Project#getProjectId() symbolic names}.
     *
     * @param uuid
     *          a project's UUID (see
     *          {@link org.eclipse.skalli.services.projects.Project#getUuid()}).
     * @param c the comparator to use to sort the returned projects, or <code>null</code>
     *          if the default order should be used.
     *  @param depth
     *          depth of subprojects tree. A <code>depth</code> of -1 is equivalent
     *          to {@link Integer#MAX_VALUE}.
     *
     * @return  projects that reference the given project as parent project (but not necessary direct parent),
     *          or an empty set.
     */
    public SortedSet<Project> getSubProjects(UUID uuid, Comparator<Project> c, int depth);

    /**
     * Returns the chain of parent projects of the given project.
     * <p>
     * Note, this method can also be used to retrieve the parent chain
     * of a deleted project.
     *
     * @param uuid
     *          a project's UUID (see
     *          {@link org.eclipse.skalli.services.projects.Project#getUuid()}).
     *
     * @return  the parent projects of the project including the project itself
     *          (as first list entry).
     */
    public List<Project> getParentChain(UUID uuid);

    /**
     * Returns the first ("nearest") parent project in the project's parent chain
     * that matches the given project nature. Since the project is member of its own
     * parent chain (see {@link #getParentChain(UUID)}), this method will return
     * the project itself, if it matches the given project nature.
     * <p>
     * Note, this method can also be applied to deleted projects.
     *
     * @param uuid
     *          a project's UUID (see
     *          {@link org.eclipse.skalli.services.projects.Project#getUuid()}).
     * @param nature
     *          the project nature to match.
     *
     * @return  the nearest parent project matching the given project nature,
     *          or the project itself, if it matches the given project nature.
     */
    public Project getNearestParent(UUID uuid, ProjectNature nature);

    /**
     * Returns the project with the given project identifier.
     *
     * @param projectId
     *          a project's project identifier
     *          (see {@link org.eclipse.skalli.services.projects.Project#getProjectId()}).
     *
     * @return  the project with the given project identifier, or
     *          <code>null</code> if no such project exists.
     */
    public Project getProjectByProjectId(String projectId);

    /**
     * Returns the unique identifiers of all deleted projects.
     * @return a set of unique identifiers, or an empty set.
     */
    public Set<UUID> deletedSet();

    /**
     * Returns all deleted projects.
     * The order of the result is not specified.
     *
     * @return  a list of deleted projects, or an empty list.
     */
    public List<Project> getDeletedProjects();

    /**
     * Returns a sorted list of all currently existing deleted projects.
     *
     * @param c
     *          the comparator to use to sort the result.
     *
     * @return  a list of deleted projects, or an empty list.
     */
    public List<Project> getDeletedProjects(Comparator<Project> c);

    /**
     * Returns the deleted project with the given unique identifier.
     *
     * @param uuid
     *          a project's UUID (see
     *          {@link org.eclipse.skalli.services.projects.Project#getUuid()}).
     *
     * @return  the project with the given unique identifier, or <code>null</code>
     *          if no project with the given unique identifier exists at all or
     *          the project matching the given unique identifier is not marked as
     *          deleted.
     */
    public Project getDeletedProject(UUID uuid);

    /**
     * Returns the members of a {@link Project} regardless of their roles.
     *
     * @param uuid
     *          a project's UUID (see
     *          {@link org.eclipse.skalli.services.projects.Project#getUuid()}).
     *
     * @return  a set of members, or an empty set.
     */
    public SortedSet<Member> getMembers(UUID uuid);

    /**
     * Returns the members of a {@link Project} that are assigned to certain roles.
     *
     * @param uuid
     *          a project's UUID (see
     *          {@link org.eclipse.skalli.services.projects.Project#getUuid()}).
     * @param roles  one or more role names to search for.
     *
     * @return a set of members matching the given roles, or an empty set.
     * If <code>roles</code> is <code>null</code> or an empty array, an empty set
     * is returned.
     */
    public SortedSet<Member> getMembers(UUID uuid, String... roles);

    /**
     * Returns all people involved in a {@link Project} together with their
     * respective roles.
     * <p>
     * The people returned are structured by string representations of their roles in the project.
     * Therefore, if a person has multiple roles in a project, there will be several entries
     * in the different sets accordingly.
     * </p>
     * @param uuid
     *          a project's UUID (see
     *          {@link org.eclipse.skalli.services.projects.Project#getUuid()}).
     * @return a map of project members with role names as keys, or an empty map.
     */
    public Map<String, SortedSet<Member>> getMembersByRole(UUID uuid);
}
