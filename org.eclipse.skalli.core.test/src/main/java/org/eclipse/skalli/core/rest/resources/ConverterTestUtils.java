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
package org.eclipse.skalli.core.rest.resources;

import java.util.UUID;

import org.eclipse.skalli.commons.FormatUtils;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.ext.commons.InfoExtension;
import org.eclipse.skalli.model.ext.commons.TagsExtension;
import org.eclipse.skalli.testutil.TestUUIDs;

@SuppressWarnings("nls")
public class ConverterTestUtils {

    public static final long NOW = System.currentTimeMillis();
    public static final long EARLIER = NOW - 1000L;

    public static final String REGISTERED_MILLIS = Long.toString(NOW);
    public static final String REGISTERED = FormatUtils.formatUTC(NOW);

    public static final String LAST_MODIFIED_MILLIS = Long.toString(EARLIER);
    public static final String LAST_MODIFIED = FormatUtils.formatUTCWithMillis(EARLIER);
    public static final String LAST_MODIFIER = "homer";

    public static Project newMinimalProject() {
        return newMinimalProject(TestUUIDs.TEST_UUIDS[0], "foo","bar");
    }

    public static Project newMinimalProject(UUID uuid, String id, String name) {
        Project project = new Project(id, null, name);
        project.setUuid(uuid);
        project.setLastModified(LAST_MODIFIED);
        project.setLastModifiedBy(LAST_MODIFIER);
        return project;
    }

    public static Project newBaseProject() {
        Project project = newMinimalProject();
        Project parent = newMinimalProject(TestUUIDs.TEST_UUIDS[1], "parent", "Parent");
        project.setDescription("descr1");
        project.setShortName("sh1");
        project.setRegistered(NOW);
        project.setParentEntity(parent);
        return project;
    }

    public static Project newMinimalProjectWithSubProjects() {
        Project project = newMinimalProject();
        Project child1 = newMinimalProject(TestUUIDs.TEST_UUIDS[3], "id1", "name1");
        Project child2 = newMinimalProject(TestUUIDs.TEST_UUIDS[4], "id2", "name2");
        Project child3 = newMinimalProject(TestUUIDs.TEST_UUIDS[5], "id3", "name3");
        project.setFirstChild(child2);
        child2.setNextSibling(child3);
        child3.setNextSibling(child1);
        return project;
    }

    public static Project newMinimalProjectWithExtensions() {
        Project project = newMinimalProject();
        Project parent = newMinimalProject(TestUUIDs.TEST_UUIDS[1], "parent", "parent");
        InfoExtension info = new InfoExtension();
        info.setPageUrl("foobar");
        info.setLastModified(LAST_MODIFIED);
        parent.addExtension(info);
        project.setParentEntity(parent);
        project.setInherited(InfoExtension.class, true);
        TagsExtension tags = new TagsExtension("a", "b");
        tags.setLastModifiedBy(LAST_MODIFIER);
        project.addExtension(tags);
        return project;
    }

}
