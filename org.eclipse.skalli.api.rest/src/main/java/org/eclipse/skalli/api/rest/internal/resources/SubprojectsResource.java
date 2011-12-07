package org.eclipse.skalli.api.rest.internal.resources;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.api.java.ProjectService;
import org.eclipse.skalli.api.rest.AbstractServerResource;
import org.eclipse.skalli.api.rest.ResourceRepresentation;
import org.eclipse.skalli.common.Consts;
import org.eclipse.skalli.common.Services;
import org.eclipse.skalli.common.util.Statistics;
import org.eclipse.skalli.common.util.UUIDUtils;
import org.eclipse.skalli.model.core.Project;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

public class SubprojectsResource extends AbstractServerResource {

    @Get
    public Representation retrieve() {
        Statistics.getDefault().trackUsage("api.rest.subprojects.get"); //$NON-NLS-1$
        Subprojects subprojects = new Subprojects();
        subprojects.setSubprojects(new LinkedHashSet<Project>());

        String id = (String) getRequestAttributes().get("id"); //$NON-NLS-1$
        String depthArg = getQuery().getValues("depth"); //$NON-NLS-1$
        int depth;
        try {
            depth = (StringUtils.isBlank(depthArg)) ? Integer.MAX_VALUE : new Integer(depthArg).intValue();
        } catch (NumberFormatException e) {
            return createError(Status.CLIENT_ERROR_BAD_REQUEST, "Depth value \"{0}\" should be a digit", depthArg);
        }

        ProjectService projectService = Services.getRequiredService(ProjectService.class);
        Project project = null;
        if (UUIDUtils.isUUID(id)) {
            UUID uuid = UUID.fromString(id);
            project = projectService.getByUUID(uuid);
        } else {
            project = projectService.getProjectByProjectId(id);
        }
        if (project == null) {
            return createError(Status.CLIENT_ERROR_NOT_FOUND, "Project \"{0}\" not found.", id);
        }
        Comparator<Project> comparator = new Comparator<Project>() {
            @Override
            public int compare(Project p1, Project p2) {
                // reverse ordering by project id!
                return p2.getProjectId().compareTo(p1.getProjectId());
            }
        };
        List<Project> subprojectList = projectService.getSubProjects(project.getUuid(), comparator, depth);
        subprojects.addAll(subprojectList);

        String extensionParam = getQuery().getValues(Consts.PARAM_EXTENSIONS);
        String[] extensions = new String[] {};
        if (extensionParam != null) {
            extensions = extensionParam.split(Consts.PARAM_LIST_SEPARATOR);
        }

        return new ResourceRepresentation<Subprojects>(subprojects,
               new SubprojectsConverter(getRequest().getResourceRef().getHostIdentifier(), extensions));
    }
}
