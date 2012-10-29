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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

@SuppressWarnings("nls")
public class ProtectorTest {

    private ArrayList<String> TEST_OBJECT = new ArrayList<String>();

    // test class with protected and unprotected fields
    public class A {
        @Protect
        private String secretString;
        private String publicString;
        @Protect
        private Object secretObject;
        private Object publicObject;
        @Protect
        private boolean secretBoolean;
        private boolean publicBoolean;
        @Protect
        private Boolean secretBooleanObject;
        private Boolean publicBooleanObject;
        @Protect
        private char secretChar;
        private char publicChar;
        @Protect
        private Character secretCharObject;
        private Character publicCharObject;
        @Protect
        private int secretInt;
        private int publicInt;
        @Protect
        private Integer secretIntObject;
        private Integer publicIntObject;
        @Protect
        private long secretLong;
        private long publicLong;
        @Protect
        private Long secretLongObject;
        private Long publicLongObject;

        public A(String str, Object o, char c, boolean b, int i) {
            this.secretString = str;
            this.publicString = str;
            this.secretObject = o;
            this.publicObject = o;
            this.secretBoolean = b;
            this.publicBoolean = b;
            this.secretBooleanObject = b;
            this.publicBooleanObject = b;
            this.secretChar = c;
            this.publicChar = c;
            this.secretCharObject = c;
            this.publicCharObject = c;
            this.secretInt = i;
            this.publicInt = i;
            this.secretIntObject = i;
            this.publicIntObject = i;
            this.secretLong = i;
            this.publicLong = i;
            this.secretLongObject = Long.valueOf(i);
            this.publicLongObject = Long.valueOf(i);
        }
    }

    // test class with a field of type A
    public class B {
        private A a;
    }

    // test class with a collection of A
    public class C {
        private List<A> myList;

        @Protect
        private String secretName;

        public C(String str, A... entries) {
            this.secretName = str;
            myList = Arrays.asList(entries);
        }
    }

    // test class with A as superclass
    private class D extends A {
        @Protect
        private String secretName;
        private String publicName;

        public D(String name, A a) {
            super(a.secretString, a.secretObject, a.secretChar, a.secretBoolean, a.secretInt);
            this.secretName = name;
            this.publicName = name;
        }
    }

    // inheritance over multiple classes
    private class E extends D {
        public E(A a) {
            super("name", a);
        }
    }

    @SuppressWarnings("unchecked")
    private static final List<Class<?>> withA =
        Arrays.asList(E.class, D.class, C.class, B.class, A.class);

    @SuppressWarnings("unchecked")
    private static final List<Class<?>> withoutA =
        Arrays.asList(E.class, D.class, C.class, B.class);

    @SuppressWarnings("unchecked")
    private static final List<Class<?>> withoutD =
        Arrays.asList(E.class, C.class, B.class, A.class);

    @SuppressWarnings("unchecked")
    private static final List<Class<?>> withoutAandD =
        Arrays.asList(E.class, C.class, B.class);


    @Test
    public void testProtect() throws IllegalAccessException {
        A a = new A("foobar", TEST_OBJECT, 'x', true, 4711);
        assertFields(a, false);
        Protector.protect(a, withA);
        assertFields(a, true);
    }

    @Test
    public void testProtectClass() throws IllegalAccessException {
        B b = new B();
        b.a = new A("foobar", TEST_OBJECT, 'x', true, 4711);
        assertFields(b.a, false);
        Protector.protect(b, withA);
        assertFields(b.a, true);
    }

    @Test
    public void testProtectIterator() throws IllegalAccessException {
        C c = new C("foobar",
                new A("foobar", TEST_OBJECT, 'x', true, 4711),
                new A("foobar", TEST_OBJECT, 'x', true, 4711));
        assertThat(c.secretName, is("foobar"));
        assertFields(c.myList.get(0), false);
        assertFields(c.myList.get(1), false);
        Protector.protect(c, withA);
        assertThat(c.secretName, is(Protector.PROTECTION_VALUE_STRING));
        assertFields(c.myList.get(0), true); // expect all entries of type A to be protected
        assertFields(c.myList.get(1), true);
    }

    @Test
    public void testProtectWithSupperClass() throws IllegalAccessException {
        D d = new D("name", new A("foobar", TEST_OBJECT, 'x', true, 4711));
        assertThat(d.secretName, is("name"));
        assertThat(d.publicName, is("name"));
        assertFields((A)d, false);
        Protector.protect(d, withA);
        // expect own fields to be protected
        assertThat(d.secretName, is(Protector.PROTECTION_VALUE_STRING));
        assertThat(d.publicName, is("name"));
        // expect fields inherited from A to be protected
        assertFields((A)d, true);
    }

    @Test
    public void testProtectClassHierarchy() throws IllegalAccessException {
        E e = new E(new A("foobar", TEST_OBJECT, 'x', true, 4711));
        // expect fields inherited from D to be unprotected
        assertThat(((D)e).secretName, is("name"));
        assertThat(((D)e).publicName, is("name"));
        // expect fields inherited from A to be unprotected
        assertFields((A)e, false);
        Protector.protect(e, withA);
        // expect fields inherited from D to be protected
        assertThat(((D)e).secretName, is(Protector.PROTECTION_VALUE_STRING));
        assertThat(((D)e).publicName, is("name"));
        // expect fields inherited from A to be protected
        assertFields((A)e, true);
    }

    @Test
    public void testRelevantClasses() throws IllegalAccessException {
        B b = new B();
        b.a = new A("foobar", TEST_OBJECT, 'x', true, 4711);
        // expect fields of A to be unprotected
        assertFields(b.a, false);
        Protector.protect(b, withoutA); // do not protect class A!
        // expect fields of A to be unprotected
        assertFields(b.a, false);
    }

    @Test
    public void testProtectClassHierarchyWithoutA() throws IllegalAccessException {
        E e = new E(new A("foobar", TEST_OBJECT, 'x', true, 4711));
        // expect fields inherited from D to be unprotected
        assertThat(((D)e).secretName, is("name"));
        assertThat(((D)e).publicName, is("name"));
        // expect fields inherited from A to be unprotected
        assertFields((A)e, false);
        // request protection of E and D but without A
        Protector.protect(e, withoutA); // protect D, but not A
        // expect fields inherited from D to be protected
        assertThat(((D)e).secretName, is(Protector.PROTECTION_VALUE_STRING));
        assertThat(((D)e).publicName, is("name"));
        // expect fields inherited from A to be unprotected
        assertFields((A)e, false);
    }

    @Test
    public void testProtectClassHierarchyWithoutD() throws IllegalAccessException {
        E e = new E(new A("foobar", TEST_OBJECT, 'x', true, 4711));
        // expect fields inherited from D to be unprotected
        assertThat(((D)e).secretName, is("name"));
        assertThat(((D)e).publicName, is("name"));
        // expect fields inherited from A to be unprotected
        assertFields((A)e, false);
        // request protection of E and A without D => silently include D
        Protector.protect(e, withoutD);
        // expect fields inherited from D to be protected
        assertThat(((D)e).secretName, is(Protector.PROTECTION_VALUE_STRING));
        assertThat(((D)e).publicName, is("name"));
        // expect fields inherited from A to be protected
        assertFields((A)e, true);
    }

    @Test
    public void testProtectClassHierarchyWithoutAandD() throws IllegalAccessException {
        E e = new E(new A("foobar", TEST_OBJECT, 'x', true, 4711));
        // expect fields inherited from D to be unprotected
        assertThat(((D)e).secretName, is("name"));
        assertThat(((D)e).publicName, is("name"));
        // expect fields inherited from A to be unprotected
        assertFields((A)e, false);
        // request protection of E without A and D
        Protector.protect(e, withoutAandD);
        // expect fields inherited from D to be unprotected
        assertThat(((D)e).publicName, is("name"));
        assertThat(((D)e).publicName, is("name"));
        // expect fields inherited from A to be unprotected
        assertFields((A)e, false);
    }

    private void assertFields(A a, boolean expectProtected) {
        assertThat(a.secretString, expectProtected? is(Protector.PROTECTION_VALUE_STRING) : is("foobar"));
        assertThat(a.publicString, is("foobar"));
        assertEquals(a.secretObject, expectProtected ? null : TEST_OBJECT);
        assertEquals(a.publicObject, TEST_OBJECT);
        assertThat(a.secretBoolean, expectProtected? is(false) : is(true));
        assertThat(a.publicBoolean, is(true));
        assertThat(a.secretBooleanObject, expectProtected? is((Boolean)null): is(Boolean.TRUE));
        assertThat(a.publicBooleanObject, is(Boolean.TRUE));
        assertThat(a.secretChar, expectProtected? is(Protector.PROTECTION_VALUE_CHAR) : is('x'));
        assertThat(a.publicChar, is('x'));
        assertThat(a.secretCharObject, expectProtected?
                is(Character.valueOf(Protector.PROTECTION_VALUE_CHAR)) : is(Character.valueOf('x')));
        assertThat(a.publicCharObject, is(Character.valueOf('x')));
        assertThat(a.secretInt, expectProtected? is(0) : is(4711));
        assertThat(a.publicInt, is(4711));
        assertThat(a.secretIntObject, expectProtected? is((Integer)null) : is(Integer.valueOf(4711)));
        assertThat(a.publicIntObject, is(Integer.valueOf(4711)));
        assertThat(a.secretLong, expectProtected? is(0L) : is(4711L));
        assertThat(a.publicLong, is(4711L));
        assertThat(a.secretLongObject, expectProtected? is((Long)null) : is(Long.valueOf(4711L)));
        assertThat(a.publicLongObject, is(Long.valueOf(4711L)));
    }
}
