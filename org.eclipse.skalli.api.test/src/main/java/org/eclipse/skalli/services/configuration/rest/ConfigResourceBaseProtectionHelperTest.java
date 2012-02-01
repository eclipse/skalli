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
package org.eclipse.skalli.services.configuration.rest;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class ConfigResourceBaseProtectionHelperTest {

    private static final String EXPECTED_PROETECTION_VALUE_STRING = null;
    private static final Character EXPECTED_PROTECTION_VALUE_CHARACTER = null;
    private static final Boolean EXPECTED_PROTECTION_VALUE_BOOLEAN = null;

    public class A {

        @Protect
        private String pwd;
        private String user;
        @Protect
        private boolean isImportant;
        private boolean isValid;
        @Protect
        private char myChar;
        @Protect
        private Character myCharacter;
        @Protect
        private Boolean isMyBoolean;

        public A(String pwd, String user, boolean isImportant, boolean isValid) {
            super();
            this.pwd = pwd;
            this.user = user;
            this.isImportant = isImportant;
            this.isValid = isValid;
            this.myChar = pwd.charAt(0);
        }

    }

    public class B {
        private A a;
    }

    public class C {
        List<A> myList = new ArrayList<A>();

        @Protect
        private String verrySecret;

        public C(String verrySecret, A firstListElement) {
            super();
            this.verrySecret = verrySecret;
            myList.add(firstListElement);
        }
    }

    private class D extends A {
        @Protect
        String name;

        public String getPwd() {
            return super.pwd;
        }

        public D(String name, A a) {
            super(a.pwd, a.user, a.isImportant, a.isValid);
            this.name = name;
        }
    }

    @SuppressWarnings("unchecked")
    private static final List<Class<?>> allRelevantClasses = Arrays.asList(A.class, B.class, C.class, D.class);

    @Test
    public void testProtectFields() throws ConfigResourceBase.ProtectionException {
        A a = new A("ejhndskf", "Jon", true, true);
        a.myChar = 'A';
        a.myCharacter = 'A';
        a.isMyBoolean = true;

        ConfigResourceBase.ProtectionHelper.protect(a, allRelevantClasses);
        assertThat(a.pwd, is(EXPECTED_PROETECTION_VALUE_STRING));
        assertThat(a.user, is("Jon"));
        assertThat(a.isImportant, is(false));
        assertThat(a.isValid, is(true));
        assertThat(a.myCharacter, is(EXPECTED_PROTECTION_VALUE_CHARACTER));
        assertThat(a.myChar, is(' '));
        assertThat(a.isMyBoolean, is(EXPECTED_PROTECTION_VALUE_BOOLEAN));

    }

    @Test
    public void testProtectClass() throws ConfigResourceBase.ProtectionException {
        B b = new B();
        b.a = new A("ejhndskf", "Jon", true, true);
        ConfigResourceBase.ProtectionHelper.protect(b, allRelevantClasses);
        assertThat(b.a.pwd, is(EXPECTED_PROETECTION_VALUE_STRING));
        assertThat(b.a.user, is("Jon"));
        assertThat(b.a.isImportant, is(false));
        assertThat(b.a.isValid, is(true));
    }

    @Test
    public void testProtectIterator() throws ConfigResourceBase.ProtectionException {
        C c = new C("The earth is a sphere.", new A("ejhndskf", "Jim", true, true));
        ConfigResourceBase.ProtectionHelper.protect(c, allRelevantClasses);
        assertThat(c.verrySecret, is(EXPECTED_PROETECTION_VALUE_STRING));

        assertThat(c.myList.get(0).pwd, is(EXPECTED_PROETECTION_VALUE_STRING));
        assertThat(c.myList.get(0).user, is("Jim"));
        assertThat(c.myList.get(0).isImportant, is(false));
        assertThat(c.myList.get(0).isValid, is(true));
    }

    @Test
    public void testProtectWithSupperClass() throws ConfigResourceBase.ProtectionException {
        D d = new D("new", new A("ejhndskf", "Jim", true, true));
        ConfigResourceBase.ProtectionHelper.protect(d, allRelevantClasses);
        assertThat(d.getPwd(), is(EXPECTED_PROETECTION_VALUE_STRING));
        assertNull(d.name); // name is protected
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRelevantClasses() throws ConfigResourceBase.ProtectionException {
        B b = new B();
        b.a = new A("ejhndskf", "Jon", true, true);
        List<Class<?>> relevantCasses = Arrays.asList(B.class, C.class, D.class);// do not protect class A!
        ConfigResourceBase.ProtectionHelper.protect(b, relevantCasses);

        //a of class A is not relevant, so we expect that his values are unchanged
        assertThat(b.a.pwd, is("ejhndskf"));
        assertThat(b.a.user, is("Jon"));
        assertThat(b.a.isImportant, is(true));
        assertThat(b.a.isValid, is(true));
    }

    @Test
    public void testProtectWithSupperClassButSupperclassIsNotRelevant() throws ConfigResourceBase.ProtectionException {
        D d = new D("new", new A("ejhndskf", "Jim", true, true));
        ConfigResourceBase.ProtectionHelper.protect(d, null);
        assertThat(d.getPwd(), is("ejhndskf")); // the pwd is in a field of supperclass A and not protected
        assertNull(d.name); // name is protected
    }

}
