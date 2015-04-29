/*******************************************************************************
 * Copyright (c) 2010-2015 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.gerrit.client;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.ComparatorUtils;

/**
 * Helper class representing plugin installed in a Gerrit server.
 */
public class GerritPlugin {

    private String name;
    private String version;
    private String url;
    private boolean enabled;

    /**
     * Creates an instance with the given name.
     *
     * @param name the name of the plugin.
     *
     * @throws IllegalArgumentException  if the given name is <code>null</code>
     * or blank.
     */
    public GerritPlugin(String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("'name' must not be null or blank");
        }
        this.name = name;
    }

    /**
     * Creates an instance with the given name, version and state.
     *
     * @param name  the name of the plugin.
     * @param version  the bersion of the plugin.
     * @param enabled  the state of the plugin.
     */
    public GerritPlugin(String name, String version, boolean enabled) {
        this(name);
        setVersion(version);
        setEnabled(enabled);
    }

    /**
     *  Creates an instance from a given string. The string must provide
     *  the attributes of the plugin separated by whitespace. At least the
     *  name of the plugin must be provided. If available the second entry
     *  is interpreted as version of the plugin, and the third entry as
     *  state (enabled, disabled).
     *
     * @param s  the string to parse.
     *
     * @return a <code>GerritPlugin</code>, or <code>null</code> if the
     * given string was <code>null</code> or blank.
     */
    public static GerritPlugin valueOf(String s) {
        if (StringUtils.isBlank(s)) {
            return null;
        }
        String[] items = StringUtils.split(s);
        int len = items.length;
        GerritPlugin plugin = new GerritPlugin(items[0]);
        plugin.setUrl("/plugins/" + items[0] + "/"); //$NON-NLS-1$ //$NON-NLS-2$
        if (len > 1) {
            plugin.setVersion(items[1]);
            if (len > 2) {
                plugin.setEnabled(items[2].equalsIgnoreCase("ENABLED")); //$NON-NLS-1$
            }
        }
        return plugin;
    }

    /**
     * Returns the name of the plugin, never <code>null</code>.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the URL path of the plugin.
     *
     * @return the plugin URL, e.g. <tt>"/plugins/menuextender/</tt>.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the URL path of the plugin.
     *
     * @param url  the URL path that can be used to access the API
     * of the plugin, e.g. "/plugins/menuextender".
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Returns the version of the plugin.
     *
     * @return the plugin version, or <code>null</code> if the version is unknown.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the version of the plugin.
     *
     * @param version  the plugin version.
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Checks whether the plugin is enabled on the Gerrit server.
     *
     * @return <code>true</code> if the plugin is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Determines whether the plugin is enabled on the Gerrit server.
     *
     * @param enabled  the state of the plugin.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (enabled ? 1231 : 1237);
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((url == null) ? 0 : url.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        GerritPlugin other = (GerritPlugin) obj;
        if (enabled != other.enabled) {
            return false;
        }
        if (!ComparatorUtils.equals(name, other.name)) {
            return false;
        }
        if (!ComparatorUtils.equals(version, other.version)) {
            return false;
        }
        if (!ComparatorUtils.equals(url, other.url)) {
            return false;
        }
        return true;
    }

    @SuppressWarnings("nls")
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(name);
        if (StringUtils.isNotBlank(version)) {
            sb.append(' ').append(version);
        }
        if (StringUtils.isNotBlank(url)) {
            sb.append(' ').append(url);
        }
        sb.append(' ').append(enabled? "ENABLED" : "DISABLED");
        return sb.toString();
    }
}
