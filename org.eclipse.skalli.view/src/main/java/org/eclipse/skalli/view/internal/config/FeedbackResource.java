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
package org.eclipse.skalli.view.internal.config;

import org.eclipse.skalli.services.configuration.rest.CustomizingResource;

public class FeedbackResource extends CustomizingResource<FeedbackConfig> {

    public static final String FEEDBACK_KEY = "view.feedback"; //$NON-NLS-1$

    @Override
    protected String getKey() {
        return FEEDBACK_KEY;
    }

    @Override
    protected Class<FeedbackConfig> getConfigClass() {
        return FeedbackConfig.class;
    }

}
