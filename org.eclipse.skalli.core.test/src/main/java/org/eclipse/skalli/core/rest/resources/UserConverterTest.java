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

import org.eclipse.skalli.core.rest.JSONRestWriter;
import org.eclipse.skalli.core.rest.XMLRestWriter;
import org.eclipse.skalli.model.User;
import org.eclipse.skalli.services.rest.RestWriter;
import org.eclipse.skalli.testutil.RestWriterTestBase;
import org.junit.Test;

@SuppressWarnings("nls")
public class UserConverterTest extends RestWriterTestBase {

    private static final String NAMESPACE_ATTRIBUTES = MessageFormat.format(ATTRIBUTES_PATTERN,
            UserConverter.NAMESPACE, "user", UserConverter.API_VERSION);

    @Test
    public void testMarshalAnonymousUserXML() throws Exception {
        User user = new User("me");
        XMLRestWriter restWriter = new XMLRestWriter(writer, "http://example.org");
        marshalUser(user, restWriter);
        assertEqualsXML("<user " + NAMESPACE_ATTRIBUTES + ">"
                + "<link rel=\"self\" href=\"http://example.org/api/users/me\"/>"
                + "<userId>me</userId></user>");
    }

    @Test
    public void testMarshalUserXML() throws Exception {
        User user = newUser();
        XMLRestWriter restWriter = new XMLRestWriter(writer, "http://example.org");
        marshalUser(user, restWriter);
        assertEqualsXML("<user " + NAMESPACE_ATTRIBUTES + ">"
                + "<link rel=\"self\" href=\"http://example.org/api/users/me\"/>"
                + "<userId>me</userId>"
                + "<firstname>john</firstname>"
                + "<lastname>doe</lastname>"
                + "<email>mail@example.org</email>"
                + "<phone>4711</phone>"
                + "<mobile>0815</mobile>"
                + "<sip>sip</sip>"
                + "<company>Acme Inc.</company>"
                + "<department>whatever</department>"
                + "<location>nowhere</location>"
                + "<room>basement</room>"
                + "</user>");
    }

    @Test
    public void testMarshalAnonymousUserJSON() throws Exception {
        User user = new User("me");
        JSONRestWriter restWriter = new JSONRestWriter(writer, "http://example.org");
        marshalUser(user, restWriter);
        assertEqualsJSON("{\"apiVersion\":\"1.0\","
                + "\"link\":{\"rel\":\"self\",\"href\":\"http://example.org/api/users/me\"},"
                + "\"userId\":\"me\"}");
    }

    @Test
    public void testMarshalUserJSON() throws Exception {
        User user = newUser();
        JSONRestWriter restWriter = new JSONRestWriter(writer, "http://example.org");
        marshalUser(user, restWriter);
        assertEqualsJSON("{\"apiVersion\":\"1.0\","
                + "\"link\":{\"rel\":\"self\",\"href\":\"http://example.org/api/users/me\"},"
                + "\"userId\":\"me\","
                + "\"firstname\":\"john\","
                + "\"lastname\":\"doe\","
                + "\"email\":\"mail@example.org\","
                + "\"phone\":\"4711\","
                + "\"mobile\":\"0815\","
                + "\"sip\":\"sip\","
                + "\"company\":\"Acme Inc.\","
                + "\"department\":\"whatever\","
                + "\"location\":\"nowhere\","
                + "\"room\":\"basement\""
                + "}");
    }

    private User newUser() {
        User user = new User("me");
        user.setFirstname("john");
        user.setLastname("doe");
        user.setEmail("mail@example.org");
        user.setTelephone("4711");
        user.setMobile("0815");
        user.setSip("sip");
        user.setCompany("Acme Inc.");
        user.setDepartment("whatever");
        user.setLocation("nowhere");
        user.setRoom("basement");
        return user;
    }

    private void marshalUser(User user, RestWriter restWriter) throws Exception {
        UserConverter converter = new UserConverter();
        converter.marshal(user, restWriter);
        restWriter.flush();
    }
}
