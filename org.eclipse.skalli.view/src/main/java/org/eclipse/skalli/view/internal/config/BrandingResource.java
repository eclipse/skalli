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

public class BrandingResource extends CustomizingResource<BrandingConfig> {

  public static final String KEY = "view.branding"; //$NON-NLS-1$

  @Override
  protected String getKey() {
    return KEY;
  }

  @Override
  protected Class<BrandingConfig> getConfigClass() {
    return BrandingConfig.class;
  }
}

