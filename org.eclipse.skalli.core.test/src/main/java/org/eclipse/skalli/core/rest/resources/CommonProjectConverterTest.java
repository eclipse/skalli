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

import static org.eclipse.skalli.core.rest.resources.ConverterTestUtils.*;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.UUID;

import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.core.extension.info.ExtensionServiceInfo;
import org.eclipse.skalli.core.extension.info.InfoConverter;
import org.eclipse.skalli.core.extension.tags.ExtensionServiceTags;
import org.eclipse.skalli.core.extension.tags.TagsConverter;
import org.eclipse.skalli.core.rest.JSONRestWriter;
import org.eclipse.skalli.core.rest.XMLRestWriter;
import org.eclipse.skalli.model.Member;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.services.extension.ExtensionService;
import org.eclipse.skalli.services.rest.RestWriter;
import org.eclipse.skalli.testutil.RestWriterTestBase;
import org.eclipse.skalli.testutil.TestUUIDs;
import org.junit.Test;

@SuppressWarnings("nls")
public class CommonProjectConverterTest extends RestWriterTestBase {

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

    public static final String PROJECT_ATTRIBUTES_XML(boolean omitAttributes) {
        return omitAttributes? "" :
            MessageFormat.format(
                " apiVersion=\"{0}\""
                + " lastModifiedMillis=\"{1}\""
                + " lastModified=\"{2}\""
                + " modifiedBy=\"{3}\"",
                CommonProjectConverter.API_VERSION,
                LAST_MODIFIED_MILLIS, LAST_MODIFIED, LAST_MODIFIER);
    }

    public static final String ROOT_XML(boolean omitAttributes) {
        return MessageFormat.format(
            "<project{0}>", PROJECT_ATTRIBUTES_XML(omitAttributes));
    }

    public static final String REGISTERED_XML = MessageFormat.format(
            "<registered millis=\"{0}\">{1}</registered>",
            REGISTERED_MILLIS, REGISTERED);

    public static final String COMMON_SECTION_XML(UUID uuid, String id, String name, boolean omitAttributes) {
        return  MessageFormat.format(
            "{0}"
            + "<uuid>{1}</uuid>"
            + "<id>{2}</id>"
            + "<nature>PROJECT</nature>"
            + "<template>default</template>"
            + "<name>{3}</name>",
            ROOT_XML(omitAttributes), uuid, id, name);
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
            MessageFormat.format(ATTRIBUTES_PATTERN + " lastModifiedMillis=\"{3}\" lastModified=\"{4}\" inherited=\"true\" derived=\"false\"",
                    InfoConverter.NAMESPACE, "extension-info", InfoConverter.API_VERSION, LAST_MODIFIED_MILLIS, LAST_MODIFIED);

    public static final String ATTRIBUTES_TAGS_EXTENSION =
            MessageFormat.format(ATTRIBUTES_PATTERN + " modifiedBy=\"{3}\" inherited=\"false\" derived=\"false\"",
                    TagsConverter.NAMESPACE, "extension-tags", TagsConverter.API_VERSION, LAST_MODIFIER);

    public static final String EXTENSIONS_SECTION_XML() {
        return MessageFormat.format("<extensions>"
            + "<info {0}><homepage>foobar</homepage><mailingLists/></info>"
            + "<tags {1}><tag>a</tag><tag>b</tag></tags>"
            + "</extensions>", ATTRIBUTES_INFO_EXTENSION, ATTRIBUTES_TAGS_EXTENSION);
    }

    public static final String MINIMAL_PROJECT_BEGIN_XML(UUID uuid, String id, String name, boolean omitAttributes) {
            return COMMON_SECTION_XML(uuid, id, name, omitAttributes)
            + "<phase>initial</phase>"
            + LINKS_SECTION_XML(uuid, id);
    }

    public static final String MINIMAL_PROJECT_XML(UUID uuid, String id, String name, boolean omitAttributes) {
        return  MINIMAL_PROJECT_BEGIN_XML(uuid, id, name, omitAttributes)
        + "<subprojects/><members/><extensions/></project>";
    }

    public static final String BASE_PROJECT_BEGIN_XML(UUID uuid, UUID parent, boolean omitAttributes) {
            return COMMON_SECTION_XML(uuid, "foo", "bar", omitAttributes)
            + "<shortName>sh1</shortName>"
            + "<phase>initial</phase>"
            + REGISTERED_XML
            + "<description>descr1</description>"
            + LINKS_SECTION_XML(uuid, "foo")
            + "<link rel=\"parent\" href=\"http://example.org/api/projects/" + parent + "\"/>";
    }

    public static final String BASE_PROJECT_XML(UUID uuid, UUID parent, boolean omitAttributes) {
        return BASE_PROJECT_BEGIN_XML(uuid, parent, omitAttributes)
            + "<subprojects/><members/><extensions/></project>";
    }

    public static final String PROJECT_WITH_SUBPROJECTS_XML(UUID uuid, boolean omitAttributes, UUID...subprojects) {
        return MINIMAL_PROJECT_BEGIN_XML(uuid, "foo", "bar", omitAttributes)
                + SUBPROJECTLINKS_SECTION_XML(subprojects)
                + "<members/><extensions/></project>";
    }

    public static final String REGISTERED_JSON = MessageFormat.format(
            "\"registered\":'{'\"millis\":{0},\"value\":\"{1}\"}",
            REGISTERED_MILLIS, REGISTERED);

    public static final String PROJECT_ATTRIBUTES_JSON(boolean omitAttributes) {
        return omitAttributes? "" :
                MessageFormat.format(
                "\"apiVersion\":\"{0}\","
                + "\"lastModifiedMillis\":{1},"
                + "\"lastModified\":\"{2}\","
                + "\"modifiedBy\":\"{3}\",",
                CommonProjectConverter.API_VERSION,
                LAST_MODIFIED_MILLIS, LAST_MODIFIED, LAST_MODIFIER);
    }

    public static final String ROOT_JSON(boolean omitAttributes) {
        return MessageFormat.format(
            "'{'{0}", PROJECT_ATTRIBUTES_JSON(omitAttributes));
    }

    public static final String COMMON_SECTION_JSON(UUID uuid, String id, String name, boolean omitAttributes) {
        return MessageFormat.format(
            "{0}"
            +"\"uuid\":\"{1}\","
            + "\"id\":\"{2}\","
            + "\"nature\":\"PROJECT\","
            + "\"template\":\"default\","
            + "\"name\":\"{3}\"",
            ROOT_JSON(omitAttributes), uuid, id, name);
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
            + "\"info\":'{'\"apiVersion\":\"{0}\",\"lastModifiedMillis\":{1},\"lastModified\":\"{2}\","
            + "\"inherited\":true,\"derived\":false,"
            + "\"homepage\":\"foobar\",\"mailingLists\":[]},"
            + "\"tags\":'{'\"apiVersion\":\"{3}\",\"modifiedBy\":\"{4}\",\"inherited\":false,\"derived\":false,"
            + "\"items\":[\"a\",\"b\"]}"
            + "}}",
            InfoConverter.API_VERSION, LAST_MODIFIED_MILLIS, LAST_MODIFIED, TagsConverter.API_VERSION, LAST_MODIFIER);
    }

    public static final String MINIMAL_PROJECT_BEGIN_JSON(UUID uuid, String id, String name, boolean omitAttributes) {
        return COMMON_SECTION_JSON(uuid, id, name, omitAttributes)
            + ",\"phase\":\"initial\","
            + "\"links\":[" + LINKS_SECTION_JSON(uuid, id) + "]";
    }

    public static final String MINIMAL_PROJECT_JSON(UUID uuid, String id, String name, boolean omitAttributes) {
        return MINIMAL_PROJECT_BEGIN_JSON(uuid, id, name, omitAttributes)
            + ",\"subprojects\":[],\"members\":[],\"extensions\":{}}";
}

    public static final String BASE_PROJECT_BEGIN_JSON(UUID uuid, UUID parent, boolean omitAttributes) {
            return COMMON_SECTION_JSON(uuid, "foo", "bar", omitAttributes)
            + ",\"shortName\":\"sh1\","
            + "\"phase\":\"initial\","
            + REGISTERED_JSON
            + ",\"description\":\"descr1\","
            + "\"links\":[" + LINKS_SECTION_JSON(uuid, "foo")
            + ",{\"rel\":\"parent\",\"href\":\"http://example.org/api/projects/" + parent + "\"}]";
    }

    public static final String BASE_PROJECT_JSON(UUID uuid, UUID parent, boolean omitAttributes) {
        return BASE_PROJECT_BEGIN_JSON(uuid, parent, omitAttributes)
            + ",\"subprojects\":[],\"members\":[],\"extensions\":{}}";
    }

    public static final String PROJECT_WITH_SUBPROJECTS_JSON(UUID uuid, boolean omitAttributes, UUID...subprojects) {
        return MINIMAL_PROJECT_BEGIN_JSON(uuid, "foo", "bar", omitAttributes)
            + "," + SUBPROJECTLINKS_SECTION_JSON(subprojects)
            +",\"members\":[],\"extensions\":{}}";
    }

    public static final String PROJECT_WITH_MEMBERS_JSON(UUID uuid, boolean omitAttributes) {
        return MINIMAL_PROJECT_BEGIN_JSON(uuid, "foo", "bar", omitAttributes)
            + ",\"subprojects\":[]"
            + "," + MEMBERS_SECTION_JSON()
            + ",\"extensions\":{}}";
    }

    @Test
    public void testMarshalMinimalProjectXML() throws Exception {
        Project project = newMinimalProject();
        XMLRestWriter restWriter = new XMLRestWriter(writer, "http://example.org");
        marshalProject(project, restWriter);
        assertEqualsXML(MINIMAL_PROJECT_XML(TestUUIDs.TEST_UUIDS[0], "foo", "bar", true));
    }

    @Test
    public void testMarshalMinimalProjectJSON() throws Exception {
        Project project = newMinimalProject();
        JSONRestWriter restWriter = new JSONRestWriter(writer, "http://example.org");
        marshalProject(project, restWriter);
        assertEqualsJSON(MINIMAL_PROJECT_JSON(TestUUIDs.TEST_UUIDS[0], "foo", "bar", true));
    }

    @Test
    public void testMarshalProjectNoExtensionsXML() throws Exception {
        Project project = newBaseProject();
        XMLRestWriter restWriter = new XMLRestWriter(writer, "http://example.org");
        marshalProject(project, restWriter);
        assertEqualsXML(BASE_PROJECT_XML(TestUUIDs.TEST_UUIDS[0], TestUUIDs.TEST_UUIDS[1], true));
    }

    @Test
    public void testMarshalProjectNoExtensionsJSON() throws Exception {
        Project project = newBaseProject();
        JSONRestWriter restWriter = new JSONRestWriter(writer, "http://example.org");
        marshalProject(project, restWriter);
        assertEqualsJSON(BASE_PROJECT_JSON(TestUUIDs.TEST_UUIDS[0], TestUUIDs.TEST_UUIDS[1], true));
    }

    @Test
    public void testMarshalProjectWithSubprojectsXML() throws Exception {
        Project project = newMinimalProjectWithSubProjects();
        XMLRestWriter restWriter = new XMLRestWriter(writer, "http://example.org");
        marshalProject(project, restWriter);
        assertEqualsXML(PROJECT_WITH_SUBPROJECTS_XML(TestUUIDs.TEST_UUIDS[0], true, SUBPROJECT_UUIDS));
    }

    @Test
    public void testMarshalProjectWithSubprojectsJSON() throws Exception {
        Project project = newMinimalProjectWithSubProjects();
        JSONRestWriter restWriter = new JSONRestWriter(writer, "http://example.org");
        marshalProject(project, restWriter);
        assertEqualsJSON(PROJECT_WITH_SUBPROJECTS_JSON(TestUUIDs.TEST_UUIDS[0], true, SUBPROJECT_UUIDS));
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

    private void marshalProject(Project project, RestWriter restWriter) throws Exception {
        CommonProjectConverter converter = new CommonProjectConverter(CommonProjectConverter.ALL_EXTENSIONS);
        restWriter.object("project");
        converter.marshal(project, restWriter);
        restWriter.end();
        restWriter.flush();
    }

    private void marshalMembers(SortedSet<Member> members,
            Map<String,SortedSet<Member>> membersByRole, RestWriter restWriter) throws Exception {
        CommonProjectConverter converter = new CommonProjectConverter(CommonProjectConverter.ALL_EXTENSIONS);
        restWriter.object();
        converter.setRestWriter(restWriter);
        converter.marshalMembers(TestUUIDs.TEST_UUIDS[0], members, membersByRole);
        restWriter.end();
        restWriter.flush();
    }

    private void marshalExtensions(Project project, Collection<ExtensionService<?>> extensionServices,
            RestWriter restWriter) throws Exception {
        CommonProjectConverter converter = new CommonProjectConverter(CommonProjectConverter.ALL_EXTENSIONS);
        restWriter.object();
        converter.setRestWriter(restWriter);
        converter.marshalExtensions(project, extensionServices);
        restWriter.end();
        restWriter.flush();
    }
}
