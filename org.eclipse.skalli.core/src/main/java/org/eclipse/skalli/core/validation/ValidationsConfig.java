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
package org.eclipse.skalli.core.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("validations")
public class ValidationsConfig {

    @XStreamImplicit
    private ArrayList<ValidationConfig> validationConfigs;

    // do not remove: required by xstream
    public ValidationsConfig() {
    }

    public ValidationsConfig(Collection<ValidationConfig> validationConfigs) {
        this.validationConfigs = new ArrayList<ValidationConfig>(validationConfigs);
    }

    public List<ValidationConfig> getValidationConfigs() {
        return validationConfigs;
    }
}
