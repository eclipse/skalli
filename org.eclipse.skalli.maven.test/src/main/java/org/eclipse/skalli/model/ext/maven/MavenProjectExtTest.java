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
package org.eclipse.skalli.model.ext.maven;

import java.util.Map;

import org.eclipse.skalli.testutil.PropertyHelper;
import org.eclipse.skalli.testutil.PropertyHelperUtils;
import org.junit.Test;

@SuppressWarnings("nls")
public class MavenProjectExtTest {

  @Test
  public void testPropertyDefinitions() throws Exception {
    Map<String,Object> values = PropertyHelperUtils.getValues();

    values.put(MavenProjectExt.PROPERTY_GROUPID, "org.eclipse.skalli");
    values.put(MavenProjectExt.PROPERTY_REACTOR_POM, "a/b/c");
    values.put(MavenProjectExt.PROPERTY_SITE_URL, "http://devinf.example.org/skalli/site");
    Map<Class<?>,String[]> requiredProperties = PropertyHelperUtils.getRequiredProperties();
    PropertyHelper.checkPropertyDefinitions(MavenProjectExt.class, requiredProperties, values);
  }
}

