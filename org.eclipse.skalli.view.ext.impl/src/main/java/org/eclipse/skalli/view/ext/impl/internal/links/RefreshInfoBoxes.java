package org.eclipse.skalli.view.ext.impl.internal.links;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.view.Consts;
import org.eclipse.skalli.view.ext.ProjectContextLink;

public class RefreshInfoBoxes implements ProjectContextLink {

    @Override
    public String getCaption(Project project) {
        return "Refresh";
    }

    @Override
    public URI getUri(Project project) {
        StringBuilder sb = new StringBuilder();
        sb.append(Consts.URL_PROJECTS).append("/").append(project.getProjectId()).append("/").append("infoboxes")
                .append("?").append(Consts.PARAM_ACTION).append("=").append(Consts.ACTION_REFRESH);
        try {
            return new URI(sb.toString());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public float getPositionWeight() {
        return 10.0f;

    }

    @Override
    public boolean isVisible(Project project, String userId) {
        return true;
    }

}
