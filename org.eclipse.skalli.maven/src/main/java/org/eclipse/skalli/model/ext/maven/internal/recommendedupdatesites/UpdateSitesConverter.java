package org.eclipse.skalli.model.ext.maven.internal.recommendedupdatesites;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.skalli.common.util.UUIDUtils;
import org.eclipse.skalli.model.ext.AbstractConverter;
import org.eclipse.skalli.model.ext.maven.recommendedupdatesites.RecommendedUpdateSites;
import org.eclipse.skalli.model.ext.maven.recommendedupdatesites.UpdateSite;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class UpdateSitesConverter extends AbstractConverter<RecommendedUpdateSites> {

    public static final String API_VERSION = "1.0"; //$NON-NLS-1$
    public static final String NAMESPACE = "http://www.eclipse.org/skalli/2010/API/Extension-Maven"; //$NON-NLS-1$

    public UpdateSitesConverter(String host) {
        super(RecommendedUpdateSites.class, "updateSites", host); //$NON-NLS-1$
    }

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        RecommendedUpdateSites recommendedUpdateSites = (RecommendedUpdateSites) source;
        marshalNSAttributes(writer);
        marshalApiVersion(writer);
        writeNode(writer, "name", recommendedUpdateSites.getName());//$NON-NLS-1$
        writeNode(writer, "description", recommendedUpdateSites.getDescription());//$NON-NLS-1$
        writeNode(writer, "shortName", recommendedUpdateSites.getShortName());//$NON-NLS-1$
        List<UpdateSite> updateSites = recommendedUpdateSites.getUpdateSites();
        for (UpdateSite updateSite : updateSites) {
            writer.startNode("updateSite"); //$NON-NLS-1$
            writeNode(writer, "projectUUID", updateSite.getProjectUUID().toString()); //$NON-NLS-1$
            writeNode(writer, "groupId", updateSite.getGroupId()); //$NON-NLS-1$
            writeNode(writer, "artifactId", updateSite.getArtifactId()); //$NON-NLS-1$
            writeNode(writer, "name", updateSite.getName()); //$NON-NLS-1$
            writeNode(writer, "description", updateSite.getDescription()); //$NON-NLS-1$
            writer.endNode();
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        RecommendedUpdateSites updateSites = iterateNodes(null, reader, context);
        return updateSites;
    }

    private RecommendedUpdateSites iterateNodes(RecommendedUpdateSites updateSites, HierarchicalStreamReader reader,
            UnmarshallingContext context) {
        if (updateSites == null) {
            updateSites = new RecommendedUpdateSites();
        }
        while (reader.hasMoreChildren()) {
            reader.moveDown();

            String field = reader.getNodeName();
            String value = reader.getValue();

            if ("updateSites".equals(field) && reader.hasMoreChildren()) { //$NON-NLS-1$
                iterateNodes(updateSites, reader, context);
            } else if ("updateSite".equals(field) && reader.hasMoreChildren()) { //$NON-NLS-1$
                List<UpdateSite> sites = iterateUpdateSiteNodes(reader, context);
                for (UpdateSite site : sites) {
                    updateSites.addUpdateSite(site);
                }
            } else if ("name".equals(field)) { //$NON-NLS-1$
                updateSites.setName(value);
            } else if ("shortName".equals(field)) { //$NON-NLS-1$
                updateSites.setShortName(value);
            } else if ("description".equals(field)) { //$NON-NLS-1$
                updateSites.setDescription(value);
            }
            reader.moveUp();
        }
        return updateSites;
    }

    private List<UpdateSite> iterateUpdateSiteNodes(HierarchicalStreamReader reader, UnmarshallingContext context) {
        List<UpdateSite> updateSites = new ArrayList<UpdateSite>();
        UpdateSite updateSite = new UpdateSite();
        while (reader.hasMoreChildren()) {
            reader.moveDown();

            String field = reader.getNodeName();
            String value = reader.getValue();

            if ("projectUUID".equals(field)) { //$NON-NLS-1$
                if(UUIDUtils.isUUID(value)){
                    UUID uuid = UUID.fromString(value);
                    updateSite.setProjectUUID(uuid);
                }
            } else if ("name".equals(field)) { //$NON-NLS-1$
                updateSite.setName(value);
            } else if ("description".equals(field)) { //$NON-NLS-1$
                updateSite.setDescription(value);
            } else if ("groupId".equals(field)) { //$NON-NLS-1$
                updateSite.setGroupId(value);
            } else if ("artifactId".equals(field)) { //$NON-NLS-1$
                updateSite.setArtifactId(value);
            }
            reader.moveUp();
        }
        updateSites.add(updateSite);
        return updateSites;
    }

    @Override
    public String getApiVersion() {
        return API_VERSION;
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public String getXsdFileName() {
        return "update-sites.xsd"; //$NON-NLS-1$
    }
}
