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
package org.eclipse.skalli.view.ext.impl.internal.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.skalli.view.ext.InfoBox;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("infoboxes")
public class InfoBoxes {

    private List<InfoBox> infoboxes;

    // do not remove: required by xstream
    public InfoBoxes() {
    }

    public InfoBoxes(Collection<InfoBox> infoboxes) {
        this.infoboxes = new ArrayList<InfoBox>(infoboxes);
    }

    public List<InfoBox> getInfoBoxes() {
        if (infoboxes == null) {
            infoboxes = new ArrayList<InfoBox>();
        }
        return infoboxes;
    }

}
