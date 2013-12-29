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
package org.eclipse.skalli.core.rest;

import java.io.IOException;
import java.io.Writer;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.eclipse.skalli.commons.CharacterStack;
import org.eclipse.skalli.commons.FormatUtils;
import org.eclipse.skalli.services.rest.RestWriter;
import org.eclipse.skalli.services.rest.RestWriterBase;
import org.restlet.data.MediaType;

public class JSONRestWriter extends RestWriterBase implements RestWriter {

    /** Option to enable @-prefixed attributes. */
    public static final int PREFIXED_ATTRIBUTES = 0x10000;

    /** Option to enable the rendering of namespace atributes. */
    public static final int NAMESPACE_ATTRIBUTES = 0x20000;

    /** Option to allow a key for the root object/array. */
    public static final int NAMED_ROOT = 0x40000;

    private static final MediaType MEDIA_TYPE = MediaType.APPLICATION_JSON;

    private static final char STATE_INITIAL = '\u03B1';
    private static final char STATE_FINAL = '\u03C9';
    private static final char STATE_ARRAY = 'A';
    private static final char STATE_OBJECT = 'O';
    private static final char STATE_ITEM = 'I';

    private static final char EXPECT_SEQUENCE = '\u03B1';
    private static final char IN_SEQUENCE = ',';
    private static final char EXPECT_END_SEQUENCE = '\u03C9';

    private static final String VALUE_KEY = "value"; //$NON-NLS-1$
    private static final String VALUES_KEY = "values"; //$NON-NLS-1$

    private static final String MILLIS_KEY = "millis"; //$NON-NLS-1$
    private static final String LINKS_KEY = "links"; //$NON-NLS-1$
    private static final String LINK_KEY = "link"; //$NON-NLS-1$
    private static final String HREF_KEY = "href"; //$NON-NLS-1$
    private static final String REL_KEY = "rel"; //$NON-NLS-1$

    private static final String XMLNS_PREFIX = "xmlns:"; //$NON-NLS-1$

    // stack for state machine
    private CharacterStack states;

    // the current state
    private char state;

    // state that record whether we are in a sequence of elements,
    // or expect the begin/end of a sequence;
    // a sequence is a comma-separated list of elements
    private char sequenceState;

    // true, if the next element is the first in a sequence, i.e.
    // we must not render a comma before the element
    private boolean firstInSequence;

    // the key to assign to the next element
    private String nextKey;


    public JSONRestWriter(Writer writer, String webLocator) {
        this(writer, webLocator, 0);
    }

    /**
     *
     * @param writer
     * @param options
     */
    public JSONRestWriter(Writer writer, String webLocator, int options) {
        super(writer, webLocator, options);
        this.options = options;
        states = new CharacterStack();
        states.push(state = STATE_INITIAL);
        sequenceState = EXPECT_SEQUENCE;
        firstInSequence = true;
    }

    @Override
    public MediaType getMediaType() {
        return MEDIA_TYPE;
    }

    @Override
    public void flush() throws IOException {
        if (state != STATE_FINAL) {
            throw new IllegalStateException("Final state not yet reached");
        }
        writer.flush();
    }

    @Override
    public RestWriter key(String key) {
        nextKey = StringUtils.isNotBlank(key)? key : null;
        return this;
    }

    @Override
    public RestWriter array() throws IOException {
        if (state == STATE_FINAL) {
            throw new IllegalStateException("Unexpeced array: Final state already reached");
        }
        appendComma();
        if (nextKey != null && (state == STATE_OBJECT || (state == STATE_INITIAL && isSet(NAMED_ROOT)))) {
            appendKey(nextKey);
            writer.append(':');
        }
        writer.append('[');
        states.push(state = STATE_ARRAY);
        sequenceState = IN_SEQUENCE;
        firstInSequence = true;
        nextKey = null;
        return this;
    }

    @Override
    public RestWriter array(String itemKey) throws IOException {
        return array();
    }

    @Override
    public RestWriter array(String key, String itemKey) throws IOException {
        return key(key).array();
    }

    @Override

    public RestWriter item() throws IOException {
        appendComma();
        if (state != STATE_ITEM) {
            while (state != STATE_ARRAY) {
                end();
            }
            if (state == STATE_ARRAY) {
                states.push(state = STATE_ITEM);
            }
        }
        firstInSequence = true;
        return this;
    }

    @Override
    public RestWriter item(String itemKey) throws IOException {
        return item();
    }

    @Override
    public RestWriter object() throws IOException {
        if (state == STATE_FINAL) {
            throw new IllegalStateException("Unexpected object: Final state already reached");
        }
        appendComma();
        if (nextKey != null && (state == STATE_OBJECT || (state == STATE_INITIAL && isSet(NAMED_ROOT)))) {
            appendKey(nextKey);
            writer.append(':');
        }
        writer.append('{');
        states.push(state = STATE_OBJECT);
        sequenceState = IN_SEQUENCE;
        nextKey = null;
        firstInSequence = true;
        return this;
    }

    @Override
    public RestWriter object(String key) throws IOException {
        return key(key).object();
    }

    @Override
    public RestWriter end() throws IOException {
        if (state == STATE_FINAL) {
            throw new IllegalStateException("Final state already reached");
        }
        if (state == STATE_INITIAL) {
            throw new IllegalStateException("Still in initial state");
        }
        state = states.pop();
        if (state == STATE_ARRAY) {
            writer.append(']');
        } else if (state == STATE_OBJECT) {
            writer.append('}');
        } else if (state == STATE_INITIAL) {
            throw new IllegalStateException("Still in initial state");
        }
        state = states.peek();
        if (state == STATE_INITIAL) {
            state = STATE_FINAL;
        }
        firstInSequence = false;
        return this;
    }

    @Override
    public RestWriter links() throws IOException {
        return links(LINKS_KEY);
    }

    @Override
    public RestWriter links(String key) throws IOException {
        return key(key).array();
    }

    @Override
    public RestWriter link(String rel, String href) throws IOException {
        return object(LINK_KEY)
                .attribute(REL_KEY, rel)
                .attribute(HREF_KEY, href).end();
    }

    @Override
    public RestWriter value(String s) throws IOException {
        return value(s, true);
    }

    @Override
    public RestWriter value(long l) throws IOException {
        return value(l, false);
    }

    @Override
    public RestWriter value(double d) throws IOException {
        return value(d, false);
    }

    @Override
    public RestWriter value(boolean b) throws IOException {
        return value(b, false);
    }

    @Override
    public RestWriter value(UUID uuid) throws IOException {
        return value(uuid, true);
    }

    @Override
    public RestWriter href(Object... pathSegments) throws IOException {
        return value(hrefOf(pathSegments));
    }

    @Override
    public RestWriter date(long millis) throws IOException {
        return value(DateFormatUtils.formatUTC(millis, "yyyy-MM-dd")); //$NON-NLS-1$
    }

    @Override
    public RestWriter datetime(long millis) throws IOException {
        return value(FormatUtils.formatUTC(millis));
    }

    @Override
    public RestWriter duration(long millis) throws IOException {
        return value(DurationFormatUtils.formatDurationISO(millis));
    }

    @Override
    public RestWriter pair(String key, String s) throws IOException {
        return value(key, s, true);
    }

    @Override
    public RestWriter pair(String key, long l) throws IOException {
        return value(key, l, false);
    }

    @Override
    public RestWriter pair(String key, double d) throws IOException {
        return value(key, d, false);
    }

    @Override
    public RestWriter pair(String key, boolean b) throws IOException {
        return value(key, b, false);
    }

    @Override
    public RestWriter pair(String key, UUID uuid) throws IOException {
        return value(key, uuid, true);
    }

   @Override
    public RestWriter date(String key, long millis) throws IOException {
        return object(key).pair(MILLIS_KEY, millis).value(DateFormatUtils.formatUTC(millis, "yyyy-MM-dd")); //$NON-NLS-1$
    }

    @Override
    public RestWriter datetime(String key, long millis) throws IOException {
        return object(key).pair(MILLIS_KEY, millis).value(FormatUtils.formatUTC(millis));
    }

    @Override
    public RestWriter duration(String key, long millis) throws IOException {
        return object(key).pair(MILLIS_KEY, millis).value(DurationFormatUtils.formatDurationISO(millis));
    }

    @Override
    public RestWriter href(String key, Object... pathSegments) throws IOException {
        return pair(key, hrefOf(pathSegments));
    }

    @Override
    public RestWriter attribute(String name, String value) throws IOException {
        return attribute(name, value, true);
    }

    @Override
    public RestWriter attribute(String name, long l) throws IOException {
        return attribute(name, l, false);
    }

    @Override
    public RestWriter attribute(String name, double d) throws IOException {
        return attribute(name, d, false);
    }

    @Override
    public RestWriter attribute(String name, boolean b) throws IOException {
        return attribute(name, b, false);
    }

    @Override
    public RestWriter attribute(String name, UUID uuid) throws IOException {
        return attribute(name, uuid, true);
    }

    @Override
    public RestWriter namespace(String name, String value) throws IOException {
        if (isSet(NAMESPACE_ATTRIBUTES)) {
            attribute(StringUtils.isBlank(name)? XMLNS_PREFIX : name, value);
        }
        return this;
    }

    private void append(String s, boolean quoted) throws IOException {
        if (quoted) {
            writer.append('"');
            escaped(s);
            writer.append('"');
        } else {
            escaped(s);
        }
    }

    private void appendComma() throws IOException {
        if (!firstInSequence) {
            writer.append(',');
        }
    }

    private void appendKey(String key) throws IOException {
        append(StringUtils.isBlank(key)? VALUE_KEY : key, true);
    }

    private RestWriter value(Object value, boolean quoted) throws IOException {
        appendComma();
        if (state == STATE_ARRAY || state == STATE_ITEM) {
            append(value.toString(), quoted);
        } else if (state == STATE_OBJECT && sequenceState == IN_SEQUENCE) {
            append(VALUE_KEY, true);
            writer.append(':');
            append(value.toString(), quoted);
            sequenceState = EXPECT_END_SEQUENCE;
        } else {
            throw new IllegalStateException("Unexpected value");
        }
        nextKey = null;
        firstInSequence = false;
        return this;
    }

    private RestWriter value(String key, Object value, boolean quoted) throws IOException {
        if (StringUtils.isBlank(key)) {
            throw new IllegalStateException("Missing key");
        }
        if (state == STATE_OBJECT) {
            String str = value != null? value.toString() : null;
            if (StringUtils.isNotBlank(str) || isSet(ALL_MEMBERS)) {
                appendComma();
                appendKey(key);
                writer.append(':');
                if (str == null) {
                    append("null", false); //$NON-NLS-1$
                } else {
                    append(str, quoted);
                }
                firstInSequence = false;
            }
        } else {
            throw new IllegalStateException("Unexpected named attribute");
        }
        nextKey = null;
        return this;
    }

    private RestWriter attribute(String name, Object value, boolean quoted) throws IOException {
        String key = isSet(PREFIXED_ATTRIBUTES)? "@" + name : name; //$NON-NLS-1$
        if (sequenceState == IN_SEQUENCE) {
            if (state == STATE_ARRAY) {
                object();
                value(key, value, quoted);
                end();
            } else {
                value(key, value, quoted);
            }
        } else {
            throw new IllegalStateException("Unexpected attribute");
        }
        nextKey = null;
        return this;
    }

    @SuppressWarnings("nls")
    private RestWriter escaped(String s) throws IOException {
        int len = s.length();
        for (int i = 0; i < len; ++i) {
            char c = s.charAt(i);
            switch (c) {
            case '"':
                writer.append("\\\"");
                break;
            case '\\':
                writer.append("\\\\");
                break;
            case '<':
                writer.append('\u0047');
                break;
            case '\n':
                writer.append("\\n");
                break;
            case '\r':
                writer.append("\\r");
                break;
            case '\t':
                writer.append("\\t");
                break;
            case '\b':
                writer.append("\\b");
                break;
            case '\f':
                writer.append("\\f");
                break;
            default:
                if (c < 0x20 || c >= 0x80 && c <= 0xa0 || c >= '\u2000' && c < '\u2100') {
                    writer.append("\\u");
                    writer.append(StringUtils.leftPad(Integer.toHexString(c), 4, '0'));
                } else {
                    writer.append(c);
                }
            }
        }
        return this;
    }
}
