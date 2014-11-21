/*******************************************************************************
 * Copyright (c) 2010-2014 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.core.permit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("permits")
public class PermitsConfig {

    /** The "{@value}" permit type. */
    public static final String GLOBAL_PERMIT = "global"; //$NON-NLS-1$
    /** The "{@value}" permit type. */
    public static final String ROLE_PERMIT = "role"; //$NON-NLS-1$
    /** The "{@value}" permit type. */
    public static final String GROUP_PERMIT = "group"; //$NON-NLS-1$
    /** The "{@value}" permit type. */
    public static final String USER_PERMIT = "user"; //$NON-NLS-1$
    /** The "{@value}" permit type. */
    public static final String TEMPLATE_PERMIT = "template"; //$NON-NLS-1$

    @XStreamImplicit(itemFieldName = "permit")
    private List<PermitConfig> permits;

    // do not remove: required by xstream
    public PermitsConfig() {
    }

    /**
     * Returns the (unmodifiable) set of permits of
     * this configuration, or an empty set.
     */
    public synchronized List<PermitConfig> getPermits() {
        if (permits == null) {
            permits = new ArrayList<PermitConfig>();
        }
        return Collections.unmodifiableList(permits);
    }

    /**
     * Sets the permits of this configuration.
     * Orders the given permits by their {@link PermitConfig#getPos() positions}.
     *
     * @param c  the collection of permits to set. If the collection is
     * <code>null</code> or empty all permits of this configuration are removed.
     */
    public synchronized void setPermits(Collection<? extends PermitConfig> c) {
        if (c == null || c.isEmpty()) {
            permits = new ArrayList<PermitConfig>();
            return;
        }
        permits = new ArrayList<PermitConfig>(c);
        int i = 0;
        for (PermitConfig next: c) {
            int pos = next.getPos();
            if (pos >= 0) {
                PermitConfig permit = permits.remove(i);
                if (pos < permits.size()) {
                    permits.add(pos, permit);
                } else {
                    permits.add(permit);
                }
            }
            ++i;
        }
    }

    /**
     * Adds the given permit to this configuration.
     *
     * If there is already a permit with the same {@link PermitConfig#getUuid() UUID}
     * that permit is replaced by the given permit. If the given permit
     * specifies a {@link PermitConfig#getPos() positions}, it is inserted
     * at the corresponding index. Otherwise (or if the position is outside
     * the index range) it is added to the end of the permit list.
     *
     * @param permit  the permit to add or replace.
     */
    public synchronized void add(PermitConfig permit) {
        if (permits == null || permits.isEmpty()) {
            permits = new ArrayList<PermitConfig>();
            permits.add(permit);
            return;
        }
        // first try to find a permit with the same uuid
        // in the permit list (given that the argument has an uuid);
        // if there is such a permit, overwrite it and move
        // it to the desired position (given the argument
        // specifies as position)
        int pos = permit.getPos();
        UUID uuid = permit.getUuid();
        if (uuid != null) {
            for (int i = 0; i < permits.size(); ++i) {
                if (uuid.equals(permits.get(i).getUuid())) {
                    if (pos >= 0 && pos < permits.size()) {
                        permits.remove(i);
                        permits.add(pos, permit);
                    } else {
                        permits.set(i, permit);
                    }
                    return;
                }
            }
        }
        // if the permit is unknown, add it at the desired position
        // or at the end of the permit list
        if (pos >= 0 && pos < permits.size()) {
            permits.add(pos, permit);
        } else {
            permits.add(permit);
        }
    }

    /**
     * Removes the permit with the given {@link PermitConfig#getUuid() UUID}
     * from this configuration. If the given permit does not exist in
     * this configuration, the method does nothing.
     *
     * @param uuid  the UUID of the permit to remove.
     * @return  the permit with the given UUID from this configuration,
     * or <code>null</code> if such a permit does not exist.
     */
    public synchronized PermitConfig remove(UUID uuid) {
        if (uuid == null || permits == null || permits.isEmpty()) {
            return null;
        }
        for (int i = 0; i < permits.size(); ++i) {
            if (uuid.equals(permits.get(i).getUuid())) {
                return permits.remove(i);
            }
        }
        return null;
    }

    /**
     * Returns the permit with the given {@link PermitConfig#getUuid() UUID}
     * from this configuration.
     *
     * @param uuid  the UUID of the permit to return.
     * @return  the permit, or <code>null</code> if no matching permit exists.
     */
    public PermitConfig get(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        for (PermitConfig permit: getPermits()) {
            if (uuid.equals(permit.getUuid())) {
                return permit;
            }
        }
        return null;
    }

    /**
     * Returns the permits of this configuration ordered by
     * their {@link PermitConfig#getType() types}. Global
     * permits are mapped to type {@link #GLOBAL_PERMIT}.
     *
     * @return a map of permits mapped to their types, or an empty map.
     */
    public Map<String, List<PermitConfig>> getByType() {
        Map<String, List<PermitConfig>> result = new HashMap<String, List<PermitConfig>>();
        for (PermitConfig permit: getPermits()) {
            String type = permit.getType();
            if (type == null) {
                type = GLOBAL_PERMIT;
            }
            List<PermitConfig> list = result.get(type);
            if (list == null) {
                list = new ArrayList<PermitConfig>();
                result.put(type, list);
            }
            list.add(permit);
        }
        return result;
    }

    /**
     * Returns the permits of this configuration ordered by
     * their {@link PermitConfig#getOwner() owners}. Global
     * permits are mapped to owner {@link #GLOBAL_PERMIT}.
     *
     * @return a map of permits mapped to their owners, or an empty map.
     */
    public Map<String,List<PermitConfig>> getByOwner() {
        Map<String, List<PermitConfig>> result = new HashMap<String, List<PermitConfig>>();
        for (PermitConfig permit: getPermits()) {
            String owner = permit.getOwner();
            if (owner == null) {
                owner = GLOBAL_PERMIT;
            }
            List<PermitConfig> list = result.get(owner);
            if (list == null) {
                list = new ArrayList<PermitConfig>();
                result.put(owner, list);
            }
            list.add(permit);
        }
        return result;
    }
}

