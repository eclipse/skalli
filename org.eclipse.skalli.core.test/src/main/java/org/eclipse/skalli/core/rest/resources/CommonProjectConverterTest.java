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
package org.eclipse.skalli.core.rest.resources;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.UUID;

import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.commons.FormatUtils;
import org.eclipse.skalli.core.extension.info.ExtensionServiceInfo;
import org.eclipse.skalli.core.extension.info.InfoConverter;
import org.eclipse.skalli.core.extension.tags.ExtensionServiceTags;
import org.eclipse.skalli.core.extension.tags.TagsConverter;
import org.eclipse.skalli.core.rest.JSONRestWriter;
import org.eclipse.skalli.core.rest.XMLRestWriter;
import org.eclipse.skalli.model.Member;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.ext.commons.InfoExtension;
import org.eclipse.skalli.model.ext.commons.TagsExtension;
import org.eclipse.skalli.services.extension.ExtensionService;
import org.eclipse.skalli.services.project.ProjectService;
import org.eclipse.skalli.services.rest.RestWriter;
import org.eclipse.skalli.testutil.BundleManager;
import org.eclipse.skalli.testutil.RestWriterTestBase;
import org.eclipse.skalli.testutil.TestUUIDs;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("nls")
public class CommonProjectConverterTest extends RestWriterTestBase {

    public static final long NOW = System.currentTimeMillis();
    public static final String REGISTERED = FormatUtils.formatUTC(NOW);
    public static final String LAST_MODIFIED = FormatUtils.formatUTC(NOW-1000L);
    public static final String LAST_MODIFIER = "homer";

    public static final UUID[] SUBPROJECT_UUIDS = {
        TestUUIDs.TEST_UUIDS[3], TestUUIDs.TEST_UUIDS[4], TestUUIDs.TEST_UUIDS[5]
    };

    public static final Member[] MEMBERS = new Member[]{new Member("homer"), new Member("marge"),
        new Member("bart"), new Member("lisa")};
    public static final SortedSet<Member> ALL_MEMBERS = CollectionUtils.asSortedSet(MEMBERS);
    public static final Map<String,SortedSet<Member>> MEMBERS_BY_ROLE = new HashMap<String,SortedSet<Member>>();
    static {
        MEMBERS_BY_ROLE.put("leads", CollectionUtils.asSortedSet(MEMBERS[0], MEMBERS[1]));
        MEMBERS_BY_ROLE.put("members", CollectionUtils.asSortedSet(MEMBERS));
    }

    public static final List<ExtensionService<?>> EXTENSION_SERVICES = new ArrayList<ExtensionService<?>>();
    static {
        EXTENSION_SERVICES.add(new ExtensionServiceInfo());
        EXTENSION_SERVICES.add(new ExtensionServiceTags());
    }

    public static final String PROJECT_ATTRIBUTES(boolean omitNSAttributes) {
        return omitNSAttributes?
            MessageFormat.format(
                "apiVersion=\"{0}\""
                + " lastModified=\"{1}\""
                + " modifiedBy=\"{2}\"",
                CommonProjectConverter.API_VERSION,
                LAST_MODIFIED, LAST_MODIFIER)
          : MessageFormat.format(
            ATTRIBUTES_PATTERN
            + " lastModified=\"{3}\""
            + " modifiedBy=\"{4}\"",
            CommonProjectConverter.NAMESPACE, "project", CommonProjectConverter.API_VERSION,
            LAST_MODIFIED, LAST_MODIFIER);
    }

    public static final String ROOT_XML(boolean omitNSAttributes) {
        return MessageFormat.format(
            "<project {0}>", PROJECT_ATTRIBUTES(omitNSAttributes));
    }

    public static final String REGISTERED_XML = MessageFormat.format(
            "<registered millis=\"{0}\">{1}</registered>",
            Long.toString(NOW), REGISTERED);

    public static final String COMMON_SECTION_XML(UUID uuid, String id, String name, boolean omitNSAttributes) {
        return  MessageFormat.format(
            "{0}"
            + "<uuid>{1}</uuid>"
            + "<id>{2}</id>"
            + "<nature>PROJECT</nature>"
            + "<template>default</template>"
            + "<name>{3}</name>",
            ROOT_XML(omitNSAttributes), uuid, id, name);
    }

    public static final String LINKS_SECTION_XML(UUID uuid, String id) {
        return MessageFormat.format(
            "<link rel=\"project\" href=\"http://example.org/api/projects/{0}\"/>"
            + "<link rel=\"permalink\" href=\"http://example.org/projects/{0}\"/>"
            + "<link rel=\"browse\" href=\"http://example.org/projects/{1}\"/>"
            + "<link rel=\"issues\" href=\"http://example.org/api/projects/{0}/issues\"/>"
            + "<link rel=\"subprojects\" href=\"http://example.org/api/projects/{0}/subprojects\"/>",
            uuid, id);
    }

    public static final String SUBPROJECTLINKS_SECTION_XML(UUID...uuids) {
        return MessageFormat.format(
            "<subprojects>"
            + "<link rel=\"subproject\" href=\"http://example.org/api/projects/{0}\"/>"
            + "<link rel=\"subproject\" href=\"http://example.org/api/projects/{1}\"/>"
            + "<link rel=\"subproject\" href=\"http://example.org/api/projects/{2}\"/>"
            + "</subprojects>",
            (Object[])uuids);
    }

    public static final String MEMBERS_SECTION_XML() {
        return "<members>"
            + "<member><userId>bart</userId><link rel=\"user\" href=\"http://example.org/api/users/bart\"/>"
            + "<role>members</role></member>"
            + "<member><userId>homer</userId><link rel=\"user\" href=\"http://example.org/api/users/homer\"/>"
            + "<role>leads</role><role>members</role></member>"
            + "<member><userId>lisa</userId><link rel=\"user\" href=\"http://example.org/api/users/lisa\"/>"
            + "<role>members</role></member>"
            + "<member><userId>marge</userId><link rel=\"user\" href=\"http://example.org/api/users/marge\"/>"
            + "<role>leads</role><role>members</role></member>"
            + "</members>";
    }

    public static final String ATTRIBUTES_INFO_EXTENSION =
            MessageFormat.format(ATTRIBUTES_PATTERN + " lastModified=\"{3}\" inherited=\"true\" derived=\"false\"",
                    InfoConverter.NAMESPACE, "extension-info", InfoConverter.API_VERSION, LAST_MODIFIED);

    public static final String ATTRIBUTES_TAGS_EXTENSION =
            MessageFormat.format(ATTRIBUTES_PATTERN + " modifiedBy=\"{3}\" inherited=\"false\" derived=\"false\"",
                    TagsConverter.NAMESPACE, "extension-tags", TagsConverter.API_VERSION, LAST_MODIFIER);

    public static final String EXTENSIONS_SECTION_XML() {
        return MessageFormat.format("<extensions>"
            + "<info {0}><homepage>foobar</homepage><mailingLists/></info>"
            + "<tags {1}><tag>a</tag><tag>b</tag></tags>"
            + "</extensions>", ATTRIBUTES_INFO_EXTENSION, ATTRIBUTES_TAGS_EXTENSION);
    }

    public static final String MINIMAL_PROJECT_BEGIN_XML(UUID uuid, String id, String name, boolean omitNSAttributes) {
            return COMMON_SECTION_XML(uuid, id, name, omitNSAttributes)
            + "<phase>initial</phase>"
            + LINKS_SECTION_XML(uuid, id);
    }

    public static final String MINIMAL_PROJECT_XML(UUID uuid, String id, String name, boolean omitNSAttributes) {
        return  MINIMAL_PROJECT_BEGIN_XML(uuid, id, name, omitNSAttributes)
        + "<subprojects/><members/><extensions/></project>";
    }

    public static final String BASE_PROJECT_BEGIN_XML(UUID uuid, UUID parent, boolean omitNSAttributes) {
            return COMMON_SECTION_XML(uuid, "foo", "bar", omitNSAttributes)
            + "<shortName>sh1</shortName>"
            + "<phase>initial</phase>"
            + REGISTERED_XML
            + "<description>descr1</description>"
            + LINKS_SECTION_XML(uuid, "foo")
            + "<link rel=\"parent\" href=\"http://example.org/api/projects/" + parent + "\"/>";
    }

    public static final String BASE_PROJECT_XML(UUID uuid, UUID parent, boolean omitNSAttributes) {
        return BASE_PROJECT_BEGIN_XML(uuid, parent, omitNSAttributes)
            + "<subprojects/><members/><extensions/></project>";
    }

    public static final String PROJECT_WITH_SUBPROJECTS_XML(UUID uuid, boolean omitNSAttributes, UUID...subprojects) {
        return MINIMAL_PROJECT_BEGIN_XML(uuid, "foo", "bar", omitNSAttributes)
                + SUBPROJECTLINKS_SECTION_XML(subprojects)
                + "<members/><extensions/></project>";
    }

    public static final String REGISTERED_JSON = MessageFormat.format(
            "\"registered\":'{'\"millis\":{0},\"value\":\"{1}\"}",
            Long.toString(NOW), REGISTERED);

    public static final String COMMON_SECTION_JSON(UUID uuid, String id, String name) {
        return MessageFormat.format(
            "'{'"
            + "\"apiVersion\":\"{0}\","
            + "\"lastModified\":\"{1}\","
            + "\"modifiedBy\":\"{2}\","
            + "\"uuid\":\"{3}\","
            + "\"id\":\"{4}\","
            + "\"nature\":\"PROJECT\","
            + "\"template\":\"default\","
            + "\"name\":\"{5}\"",
            CommonProjectConverter.API_VERSION,
            LAST_MODIFIED, LAST_MODIFIER, uuid, id, name);
    }

    public static final String LINKS_SECTION_JSON(UUID uuid, String id) {
        return MessageFormat.format(
            "'{'\"rel\":\"project\",\"href\":\"http://example.org/api/projects/{0}\"},"
            + "'{'\"rel\":\"permalink\",\"href\":\"http://example.org/projects/{0}\"},"
            + "'{'\"rel\":\"browse\",\"href\":\"http://example.org/projects/{1}\"},"
            + "'{'\"rel\":\"issues\",\"href\":\"http://example.org/api/projects/{0}/issues\"},"
            + "'{'\"rel\":\"subprojects\",\"href\":\"http://example.org/api/projects/{0}/subprojects\"}",
            uuid, id);
    }

    public static final String SUBPROJECTLINKS_SECTION_JSON(UUID...uuids) {
        return MessageFormat.format(
            "\"subprojects\":["
            + "'{'\"rel\":\"subproject\",\"href\":\"http://example.org/api/projects/{0}\","
            + "\"uuid\":\"{0}\",\"id\":\"id1\",\"name\":\"name1\"},"
            + "'{'\"rel\":\"subproject\",\"href\":\"http://example.org/api/projects/{1}\","
            + "\"uuid\":\"{1}\",\"id\":\"id2\",\"name\":\"name2\"},"
            + "'{'\"rel\":\"subproject\",\"href\":\"http://example.org/api/projects/{2}\","
            + "\"uuid\":\"{2}\",\"id\":\"id3\",\"name\":\"name3\"}"
            + "]",
            (Object[])uuids);
    }

    public static final String MEMBERS_SECTION_JSON() {
        return "{\"members\":["
            + "{\"userId\":\"bart\",\"link\":{\"rel\":\"user\",\"href\":\"http://example.org/api/users/bart\"},"
            + "\"roles\":[\"members\"]},"
            + "{\"userId\":\"homer\",\"link\":{\"rel\":\"user\",\"href\":\"http://example.org/api/users/homer\"},"
            + "\"roles\":[\"leads\",\"members\"]},"
            + "{\"userId\":\"lisa\",\"link\":{\"rel\":\"user\",\"href\":\"http://example.org/api/users/lisa\"},"
            + "\"roles\":[\"members\"]},"
            + "{\"userId\":\"marge\",\"link\":{\"rel\":\"user\",\"href\":\"http://example.org/api/users/marge\"},"
            + "\"roles\":[\"leads\",\"members\"]}"
            + "]}";
    }

    public static final String EXTENSIONS_SECTION_JSON() {
        return MessageFormat.format("'{'\"extensions\":'{'"
            + "\"info\":'{'\"apiVersion\":\"{0}\",\"lastModified\":\"{1}\",\"inherited\":true,\"derived\":false,"
            + "\"homepage\":\"foobar\",\"mailingLists\":[]},"
            + "\"tags\":'{'\"apiVersion\":\"{2}\",\"modifiedBy\":\"{3}\",\"inherited\":false,\"derived\":false,"
            + "\"items\":[\"a\",\"b\"]}"
            + "}}", InfoConverter.API_VERSION, LAST_MODIFIED, TagsConverter.API_VERSION, LAST_MODIFIER);
    }

    public static final String MINIMAL_PROJECT_BEGIN_JSON(UUID uuid, String id, String name) {
        return COMMON_SECTION_JSON(uuid, id, name)
            + ",\"phase\":\"initial\","
            + "\"links\":[" + LINKS_SECTION_JSON(uuid, id) + "]";
    }

    public static final String MINIMAL_PROJECT_JSON(UUID uuid, String id, String name) {
        return MINIMAL_PROJECT_BEGIN_JSON(uuid, id, name)
            + ",\"subprojects\":[],\"members\":[],\"extensions\":{}}";
}

    public static final String BASE_PROJECT_BEGIN_JSON(UUID uuid, UUID parent) {
            return COMMON_SECTION_JSON(uuid, "foo", "bar")
            + ",\"shortName\":\"sh1\","
            + "\"phase\":\"initial\","
            + REGISTERED_JSON
            + ",\"description\":\"descr1\","
            + "\"links\":[" + LINKS_SECTION_JSON(uuid, "foo")
            + ",{\"rel\":\"parent\",\"href\":\"http://example.org/api/projects/" + parent + "\"}]";
    }

    public static final String BASE_PROJECT_JSON(UUID uuid, UUID parent) {
        return BASE_PROJECT_BEGIN_JSON(uuid, parent)
            + ",\"subprojects\":[],\"members\":[],\"extensions\":{}}";
    }

    public static final String PROJECT_WITH_SUBPROJECTS_JSON(UUID uuid, UUID...subprojects) {
        return MINIMAL_PROJECT_BEGIN_JSON(uuid, "foo", "bar")
            + "," + SUBPROJECTLINKS_SECTION_JSON(subprojects)
            +",\"members\":[],\"extensions\":{}}";
    }

    public static final String PROJECT_WITH_MEMBERS_JSON(UUID uuid) {
        return MINIMAL_PROJECT_BEGIN_JSON(uuid, "foo", "bar")
            + ",\"subprojects\":[]"
            + "," + MEMBERS_SECTION_JSON()
            + ",\"extensions\":{}}";
    }

    private List<Project> projects;
    private ProjectService projectService;

    @Override
    @Before
    public void setup() throws Exception {
        super.setup();
        projectService = BundleManager.getRequiredService(ProjectService.class);
        projects = projectService.getAll();
        Assert.assertTrue("projects.size() > 0", projects.size() > 0);
    }

    @Test
    public void testMarshalMinimalProjectXML() throws Exception {
        Project project = newMinimalProject();
        XMLRestWriter restWriter = new XMLRestWriter(writer, "http://example.org");
        marshalProject(project, restWriter);
        assertEqualsXML(MINIMAL_PROJECT_XML(TestUUIDs.TEST_UUIDS[0], "foo", "bar", false));
    }

    @Test
    public void testMarshalMinimalProjectJSON() throws Exception {
        Project project = newMinimalProject();
        JSONRestWriter restWriter = new JSONRestWriter(writer, "http://example.org");
        marshalProject(project, restWriter);
        assertEqualsJSON(MINIMAL_PROJECT_JSON(TestUUIDs.TEST_UUIDS[0], "foo", "bar"));
    }

    @Test
    public void testMarshalProjectNoExtensionsXML() throws Exception {
        Project project = newBaseProject();
        XMLRestWriter restWriter = new XMLRestWriter(writer, "http://example.org");
        marshalProject(project, restWriter);
        assertEqualsXML(BASE_PROJECT_XML(TestUUIDs.TEST_UUIDS[0], TestUUIDs.TEST_UUIDS[1], false));
    }

    @Test
    public void testMarshalProjectNoExtensionsJSON() throws Exception {
        Project project = newBaseProject();
        JSONRestWriter restWriter = new JSONRestWriter(writer, "http://example.org");
        marshalProject(project, restWriter);
        assertEqualsJSON(BASE_PROJECT_JSON(TestUUIDs.TEST_UUIDS[0], TestUUIDs.TEST_UUIDS[1]));
    }

    @Test
    public void testMarshalProjectWithSubprojectsXML() throws Exception {
        Project project = newMinimalProjectWithSubProjects();
        XMLRestWriter restWriter = new XMLRestWriter(writer, "http://example.org");
        marshalProject(project, restWriter);
        assertEqualsXML(PROJECT_WITH_SUBPROJECTS_XML(TestUUIDs.TEST_UUIDS[0], false, SUBPROJECT_UUIDS));
    }

    @Test
    public void testMarshalProjectWithSubprojectsJSON() throws Exception {
        Project project = newMinimalProjectWithSubProjects();
        JSONRestWriter restWriter = new JSONRestWriter(writer, "http://example.org");
        marshalProject(project, restWriter);
        assertEqualsJSON(PROJECT_WITH_SUBPROJECTS_JSON(TestUUIDs.TEST_UUIDS[0], SUBPROJECT_UUIDS));
    }

    @Test
    public void testMarshalMembersXML() throws Exception {
        XMLRestWriter restWriter = new XMLRestWriter(writer, "http://example.org");
        marshalMembers(ALL_MEMBERS, MEMBERS_BY_ROLE, restWriter);
        assertEqualsXML(MEMBERS_SECTION_XML());
    }

    @Test
    public void testMarshalMembersJSON() throws Exception {
        JSONRestWriter restWriter = new JSONRestWriter(writer, "http://example.org");
        restWriter.set(JSONRestWriter.NAMED_ROOT);
        marshalMembers(ALL_MEMBERS, MEMBERS_BY_ROLE, restWriter);
        assertEqualsJSON(MEMBERS_SECTION_JSON());
    }

    @Test
    public void testMarshalProjectWithExtensionsXML() throws Exception {
        Project project = newMinimalProjectWithExtensions();
        XMLRestWriter restWriter = new XMLRestWriter(writer, "http://example.org");
        marshalExtensions(project, EXTENSION_SERVICES, restWriter);
        assertEqualsXML(EXTENSIONS_SECTION_XML());
    }

    @Test
    public void testMarshalProjectWitExtensionsJSON() throws Exception {
        Project project = newMinimalProjectWithExtensions();
        JSONRestWriter restWriter = new JSONRestWriter(writer, "http://example.org");
        marshalExtensions(project, EXTENSION_SERVICES, restWriter);
        assertEqualsJSON(EXTENSIONS_SECTION_JSON());
    }

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
        project.setDescription("descr1");
        project.setShortName("sh1");
        project.setRegistered(NOW);
        project.setParentEntityId(TestUUIDs.TEST_UUIDS[1]);
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

    private void marshalProject(Project project, RestWriter restWriter) throws Exception {
        CommonProjectConverter converter = new CommonProjectConverter(false);
        restWriter.object("project");
        converter.marshal(project, restWriter);
        restWriter.end();
        restWriter.flush();
    }

    private void marshalMembers(SortedSet<Member> members,
            Map<String,SortedSet<Member>> membersByRole, RestWriter restWriter) throws Exception {
        CommonProjectConverter converter = new CommonProjectConverter(false);
        restWriter.object();
        converter.setRestWriter(restWriter);
        converter.marshalMembers(TestUUIDs.TEST_UUIDS[0], members, membersByRole);
        restWriter.end();
        restWriter.flush();
    }

    private void marshalExtensions(Project project, Collection<ExtensionService<?>> extensionServices,
            RestWriter restWriter) throws Exception {
        CommonProjectConverter converter = new CommonProjectConverter(false);
        restWriter.object();
        converter.setRestWriter(restWriter);
        converter.marshalExtensions(project, extensionServices);
        restWriter.end();
        restWriter.flush();
    }
}
