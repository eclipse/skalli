package org.eclipse.skalli.services.configuration.rest;

public abstract class ConfigSectionBase implements ConfigSection {

    @Override
    public String[] getResourcePaths() {
        return new String[] { getName() };
    }

}
