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
package org.eclipse.skalli.testutil;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import javax.xml.bind.DatatypeConverter;

import org.eclipse.skalli.model.ext.EntityBase;

public class PropertyHelperUtils {

  public static final UUID[] TEST_UUIDS = new UUID[] {
    UUID.fromString("e4d78581-08da-4f04-8a90-a7dac41f6247"),
    UUID.fromString("db028fc0-06b0-4cb2-b3eb-57804ce24877"),
    UUID.fromString("1eb78108-1685-41fd-8149-1501c5b81fcf"),
    UUID.fromString("84b2dbab-1038-44a3-a925-06aa58039a16"),
    UUID.fromString("24144e5a-6cb5-4925-9ea9-c6e13fb660e0"),
    UUID.fromString("2bf62f55-780b-4df8-b0b1-c97e773cce11"),
    UUID.fromString("7488bfa2-d795-460e-997f-11ba39fd2a07")
  };

  public static Map<String,Object> getValues() {
    HashMap<String,Object> values = new HashMap<String,Object>();
    values.put(EntityBase.PROPERTY_UUID, TEST_UUIDS[0]);
    values.put(EntityBase.PROPERTY_DELETED, Boolean.FALSE);
    TestExtensibleEntityBase parent = new TestExtensibleEntityBase(TEST_UUIDS[1]);
    values.put(EntityBase.PROPERTY_PARENT_ENTITY, parent);
    values.put(EntityBase.PROPERTY_PARENT_ENTITY_ID, TEST_UUIDS[1]);
    Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.ENGLISH); //$NON-NLS-1$
    String lastModified = DatatypeConverter.printDateTime(now);
    values.put(EntityBase.PROPERTY_LAST_MODIFIED, lastModified);
    values.put(EntityBase.PROPERTY_LAST_MODIFIED_BY, "homer"); //$NON-NLS-1$
    return values;
  }

  public static Map<Class<?>,String[]> getRequiredProperties() {
    return new HashMap<Class<?>,String[]>();
  }
}

