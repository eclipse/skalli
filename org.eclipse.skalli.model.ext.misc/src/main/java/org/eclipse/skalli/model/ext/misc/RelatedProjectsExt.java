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
package org.eclipse.skalli.model.ext.misc;

import org.eclipse.skalli.commons.UUIDList;
import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.PropertyName;

public class RelatedProjectsExt extends ExtensionEntityBase {
    public static final String MODEL_VERSION = "1.0"; //$NON-NLS-1$
    public static final String NAMESPACE = "http://www.eclipse.org/skalli/2010/Model/Extension-Related"; //$NON-NLS-1$

    @PropertyName(position = 0)
    public static final String PROPERTY_CALCULATED = "calculated";

    @PropertyName(position = 1)
    public static final String PROPERTY_RELATED_PROJECT = "relatedProjects"; //$NON-NLS-1$

    private UUIDList relatedProjects;
    private boolean calculated;

    public boolean getCalculated() {
        return calculated;
    }

    public void setCalculated(boolean calculated) {
        this.calculated = calculated;
    }

    public UUIDList getRelatedProjects() {
        if (relatedProjects == null) {
            relatedProjects = new UUIDList();
        }
        return relatedProjects;
    }

    public void setRelatedProjects(UUIDList relatedProjects) {
        this.relatedProjects = new UUIDList(relatedProjects);
    }

}
