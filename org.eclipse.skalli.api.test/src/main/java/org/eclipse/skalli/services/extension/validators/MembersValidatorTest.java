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
package org.eclipse.skalli.services.extension.validators;

import static org.junit.Assert.*;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.eclipse.skalli.model.ExtensibleEntityBase;
import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.Member;
import org.eclipse.skalli.model.PropertyName;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.model.User;
import org.eclipse.skalli.services.user.UserService;
import org.eclipse.skalli.testutil.HashMapUserService;
import org.eclipse.skalli.testutil.TestUUIDs;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("nls")
public class MembersValidatorTest {

    private static class TestExtension extends ExtensibleEntityBase {
        Set<Member> members = new HashSet<Member>();

        @PropertyName
        public static final String PROPERTY_MEMBERS = "members";

        public Set<Member> getMembers() {
            return members;
        }
    }

    private static class LocalMembersValidator extends MembersValidator {

        private UserService userService;

        public LocalMembersValidator(Severity severity, UserService userService, String caption) {
            super(severity, TestExtension.class, TestExtension.PROPERTY_MEMBERS, caption);
            this.userService = userService;
        }

        @Override
        protected UserService getUserService() {
            return userService;
        }
    }

    private static final String MSG_PREFIX_WITH_CAPTION = "Members list references user ''{0}''";
    private static final String MSG_PREFIX_NO_CAPTION =
            "Property ''" + TestExtension.PROPERTY_MEMBERS + "'' references user ''{0}''";

    private List<Member> members;
    private User[] users = new User[3];
    private HashMapUserService userService;

    @Before
    public void before() {
        users[0] = new User("jdoe", "John", "Doe", "jdoe@example.org");
        users[1] = new User("homer", "Homer", "Simpson", "homer@example.org");
        users[2] = new User("marge", "Marge", "Simpson", "marge@example.org");
        userService = new HashMapUserService();
        for (User user: users) {
            userService.put(user.getUserId(), user);
        }
        members = new ArrayList<Member>();
        members.add(new Member(users[0].getUserId()));
        members.add(new Member(users[2].getUserId()));

    }

    @Test
    public void testAllMembersKnown() throws Exception {
        LocalMembersValidator validator = new LocalMembersValidator(Severity.WARNING, userService, "Members");
        SortedSet<Issue> issues = validator.validate(TestUUIDs.TEST_UUIDS[0], members, Severity.INFO);
        assertTrue(issues.isEmpty());
    }

    @Test
    public void testUnknownMember() throws Exception {
        members.add(new Member("unknown"));
        LocalMembersValidator validator = new LocalMembersValidator(Severity.WARNING, userService, "Members");
        SortedSet<Issue> issues = validator.validate(TestUUIDs.TEST_UUIDS[0], members, Severity.INFO);
        assertIssues(issues, 1, MSG_PREFIX_WITH_CAPTION, "unknown");
    }

    @Test
    public void testMultipleUnknownMembers() throws Exception {
        members.add(new Member("unknown1"));
        members.add(new Member("unknown2"));
        members.add(new Member("unknown3"));
        LocalMembersValidator validator = new LocalMembersValidator(Severity.WARNING, userService, "Members");
        SortedSet<Issue> issues = validator.validate(TestUUIDs.TEST_UUIDS[0], members, Severity.INFO);
        assertIssues(issues, 3, MSG_PREFIX_WITH_CAPTION, "unknown1", "unknown2", "unknown3");
    }

    @Test
    public void testNoPropertyCaption() throws Exception {
        members.add(new Member("unknown"));
        LocalMembersValidator validator = new LocalMembersValidator(Severity.WARNING, userService, null);
        SortedSet<Issue> issues = validator.validate(TestUUIDs.TEST_UUIDS[0], members, Severity.INFO);
        assertIssues(issues, 1, MSG_PREFIX_NO_CAPTION, "unknown");
    }

    @Test
    public void testSeverity() throws Exception {
        members.add(new Member("unknown"));
        LocalMembersValidator validator = new LocalMembersValidator(Severity.WARNING, userService, null);
        SortedSet<Issue> issues = validator.validate(TestUUIDs.TEST_UUIDS[0], members, Severity.INFO);
        assertFalse(issues.isEmpty());
        issues = validator.validate(TestUUIDs.TEST_UUIDS[0], members, Severity.WARNING);
        assertFalse(issues.isEmpty());
        issues = validator.validate(TestUUIDs.TEST_UUIDS[0], members, Severity.ERROR);
        assertTrue(issues.isEmpty());
        issues = validator.validate(TestUUIDs.TEST_UUIDS[0], members, Severity.FATAL);
        assertTrue(issues.isEmpty());
    }

    @Test
    public void testNoUserService() throws Exception {
        members.add(new Member("unknown"));
        LocalMembersValidator validator = new LocalMembersValidator(Severity.WARNING, null, null);
        SortedSet<Issue> issues = validator.validate(TestUUIDs.TEST_UUIDS[0], members, Severity.INFO);
        assertTrue(issues.isEmpty());
    }

    @Test
    public void testNoCollection() throws Exception {
        LocalMembersValidator validator = new LocalMembersValidator(Severity.WARNING, userService, null);
        SortedSet<Issue> issues = validator.validate(TestUUIDs.TEST_UUIDS[0], new Member("unknown"), Severity.INFO);
        assertEquals(1, issues.size());
        assertIssue(issues.first(), 0, MSG_PREFIX_NO_CAPTION, "unknown");
    }

    private void assertIssues(SortedSet<Issue> issues, int expectedNumberIssues,
            String msgPrefix, String ... unknownMembers) {
        assertEquals(expectedNumberIssues, issues.size());
        int item = 0;
        for (Issue issue: issues) {
            assertIssue(issue,  2 + item, msgPrefix, unknownMembers[item]);
            ++item;
        }
    }

    private void assertIssue(Issue issue, int item, String msgPrefix, String unknownMember) {
        assertEquals(TestUUIDs.TEST_UUIDS[0], issue.getEntityId());
        assertEquals(TestExtension.class, issue.getExtension());
        assertEquals(TestExtension.PROPERTY_MEMBERS, issue.getPropertyId());
        assertEquals(LocalMembersValidator.class, issue.getIssuer());
        assertEquals(item, issue.getItem());
        assertTrue(issue.getMessage().startsWith(MessageFormat.format(msgPrefix, unknownMember)));
    }
}
