package org.eclipse.skalli.core.internal.groups;

import java.util.Map;

import org.eclipse.skalli.services.configuration.ConfigurationService;
import org.eclipse.skalli.services.configuration.rest.ConfigResourceBase;

public class GroupResource extends ConfigResourceBase<GroupConfig> {

    private static final String PARAM_GROUP_NAME = "groupName"; //$NON-NLS-1$

    @Override
    protected Class<GroupConfig> getConfigClass() {
        return GroupConfig.class;
    }

    @Override
    protected GroupConfig readConfig(ConfigurationService configService, Map<String, Object> requestAttributes) {
        String groupName = (String)requestAttributes.get(PARAM_GROUP_NAME);
        GroupsConfig groupsConfig = readGroupsConfig(configService);
        if (groupsConfig == null) {
            return null;
        }
        for (GroupConfig groupConfig: groupsConfig.getGroups()) {
            if (groupName.equals(groupConfig.getGroupId())) {
                return groupConfig;
            }
        }
        return null;
    }

    @Override
    protected void storeConfig(ConfigurationService configService, GroupConfig configObject,
            Map<String, Object> requestAttributes) {
        String groupName = (String)requestAttributes.get(PARAM_GROUP_NAME);
        GroupsConfig groupsConfig = readGroupsConfig(configService);
        if (groupsConfig == null) {
            groupsConfig = new GroupsConfig();
        }
        boolean updated = false;
        for (GroupConfig groupConfig: groupsConfig.getGroups()) {
            if (groupName.equals(groupConfig.getGroupId())) {
                groupConfig.setGroupMembers(configObject.getGroupMembers());
                updated = true;
            }
        }
        if (!updated) {
            groupsConfig.getGroups().add(configObject);
        }
        storeGroupsConfig(configService, groupsConfig);
    }

    private GroupsConfig readGroupsConfig(ConfigurationService configService) {
        return configService.readCustomization(GroupsResource.MAPPINGS_KEY, GroupsConfig.class);
    }

    private void storeGroupsConfig(ConfigurationService configService, GroupsConfig configObject) {
        configService.writeCustomization(GroupsResource.MAPPINGS_KEY, configObject);
    }
}
