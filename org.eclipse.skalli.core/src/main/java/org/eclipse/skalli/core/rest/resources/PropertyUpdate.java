package org.eclipse.skalli.core.rest.resources;

import org.eclipse.skalli.model.PropertyName;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("propertyUpdate")
public class PropertyUpdate {

    @PropertyName
    public static final String PROPERTY_TEMPLATE = "template"; //$NON-NLS-1$

    private String template = "";//$NON-NLS-1$

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }
}
