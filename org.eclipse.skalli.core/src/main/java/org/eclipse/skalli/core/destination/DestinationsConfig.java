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
package org.eclipse.skalli.core.destination;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("destinations")
public class DestinationsConfig {

    @XStreamImplicit
    private List<DestinationConfig> destinations;

    // do not remove: required by xstream
    public DestinationsConfig() {
    }

    public List<DestinationConfig> getDestinations() {
        if (destinations == null) {
            destinations = new ArrayList<DestinationConfig>();
        }
        return destinations;
    }

    public void addDestination(DestinationConfig dest) {
        getDestinations().add(dest);
    }

    /**
     * Returns the destination for a given unique identifier.
     *
     * @param id  the identifier to search for.
     * @return a destination configuration, or <code>null</code> if no matching
     * configuration could be found.
     */
    public DestinationConfig getDestination(String id) {
        for (DestinationConfig dest: getDestinations()) {
            if (dest.getId().equals(id)) {
                return dest;
            }
        }
        return null;
    }

}
