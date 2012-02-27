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
package org.eclipse.skalli.destination.internal.config;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("destinations")
public class DestinationsConfig {

    @XStreamImplicit
    private List<DestinationConfig> destinations;

    public List<DestinationConfig> getDestinations() {
        if (destinations == null) {
            destinations = new ArrayList<DestinationConfig>();
        }
        return destinations;
    }

    public void setDestinations(List<DestinationConfig> destinations) {
        this.destinations = destinations;
    }

    public void addDestination(DestinationConfig dest) {
        getDestinations().add(dest);
    }
}
