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
package org.eclipse.skalli.ext.mapping.scm;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("scmMappings")
public class ScmLocationMappings {

    @XStreamImplicit
    private ArrayList<ScmLocationMapping> scmMapping;

    // do not remove: required by xstream
    public ScmLocationMappings() {
    }

    public ScmLocationMappings(ArrayList<ScmLocationMapping> scmMappings) {
        this.scmMapping = scmMappings;
    }

    public List<ScmLocationMapping> getScmMappings() {
        return scmMapping;
    }

}
