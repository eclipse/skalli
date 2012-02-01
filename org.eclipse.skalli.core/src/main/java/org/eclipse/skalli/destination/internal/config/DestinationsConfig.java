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
    private List<Destination> destinations;

    public List<Destination> getDestinations() {
        if (destinations == null) {
            destinations = new ArrayList<Destination>();
        }
        return destinations;
    }

    public void setDestinations(List<Destination> destinations) {
        this.destinations = destinations;
    }

    public void addDestination(Destination dest) {
        getDestinations().add(dest);
    }
}
