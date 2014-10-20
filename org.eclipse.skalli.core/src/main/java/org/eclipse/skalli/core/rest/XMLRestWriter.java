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
import java.util.Stack;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.eclipse.skalli.commons.CharacterStack;
import org.eclipse.skalli.commons.FormatUtils;
import org.eclipse.skalli.services.rest.RestWriter;
import org.eclipse.skalli.services.rest.RestWriterBase;
import org.restlet.data.MediaType;

public class XMLRestWriter extends RestWriterBase implements RestWriter {

    private static final MediaType MEDIA_TYPE = MediaType.TEXT_XML;

    private static final char STATE_INITIAL = '\u03B1';
    private static final char STATE_FINAL = '\u03C9';
    private static final char STATE_ARRAY = 'A';
    private static final char STATE_OBJECT = 'O';
    private static final char STATE_ITEM = 'I';

    private static final char EXPECT_OPENING_TAG = '\u03B1';
    private static final char OPENING_TAG = '<';
    private static final char EXPECT_TEXT_NODE = '#';

    private static final String MILLIS_KEY = "millis"; //$NON-NLS-1$
    private static final String ITEM = "item"; //$NON-NLS-1$
    private static final String LINK_KEY = "link"; //$NON-NLS-1$
    private static final String HREF_KEY = "href"; //$NON-NLS-1$
    private static final String REL_KEY = "rel"; //$NON-NLS-1$

    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"; //$NON-NLS-1$
    private static final String XMLNS_PREFIX = "xmlns:"; //$NON-NLS-1$

    // stack for state machine
    private CharacterStack states;

    // stack for names of nested tags
    private Stack<String> tags;

    // the current state
    private char state;

    // state that record where we are currently within a
    // tag, i.e. whether we are in the opening tag,
    // expect the text node or the begin of a new tag
    private char tagState;

    // the name of the tag we are currently in
    private String tag;

    // the name to assign to the next tag
    private String nextKey;

    // the name to assign to the next tag within an array
    private String nextItem;


    public XMLRestWriter(Writer writer, String webLocator) {
        this(writer, webLocator, 0);
    }

    public XMLRestWriter(Writer writer, String webLocator, int options) {
        super(writer, webLocator, options);
        states = new CharacterStack();
        tags = new Stack<String>();
        states.push(state = STATE_INITIAL);
        tagState = EXPECT_OPENING_TAG;
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
        return array(null, null);
    }

    @Override
    public RestWriter array(String itemKey) throws IOException {
        return array(null, itemKey);
    }

    @Override
    public RestWriter array(String key, String itemKey) throws IOException {
        if (state == STATE_FINAL) {
            throw new IllegalStateException("Unexpeced object: Final state already reached");
        }
        if (state == STATE_INITIAL) {
            writer.append(XML_HEADER);
        }
        closeOpeningTag();
        String tag = key;
        if (StringUtils.isBlank(tag)) {
            tag = nextKey;
            if (StringUtils.isBlank(tag)) {
                if (state == STATE_ITEM || state == STATE_ARRAY) {
                    tag = nextItem;
                }
            }
        }
        nextItem = itemKey;
        if (StringUtils.isBlank(nextItem)) {
            nextItem = ITEM;
        }
        if (StringUtils.isNotBlank(tag)) {
            writer.append('<').append(tag);
            tagState = OPENING_TAG;
        }
        states.push(state = STATE_ARRAY);
        tags.push(tag);
        nextKey = null;
        return this;
    }

    @Override
    public RestWriter item() throws IOException {
        if (state == STATE_INITIAL) {
            throw new IllegalStateException("Still in initial state");
        }
        closeOpeningTag();
        if (state != STATE_ITEM) {
            while (state != STATE_ARRAY) {
                end();
            }
            if (state == STATE_ARRAY) {
                states.push(state = STATE_ITEM);
                tags.push(nextItem);
            }
        }
        return this;
    }

    @Override
    public RestWriter item(String itemKey) throws IOException {
        nextItem = StringUtils.isNotBlank(itemKey)? itemKey : null;
        return item();
    }

    @Override
    public RestWriter object() throws IOException {
        return object(null);
    }

    @Override
    public RestWriter object(String key) throws IOException {
        if (state == STATE_FINAL) {
            throw new IllegalStateException("Unexpeced object: Final state already reached");
        }
        if (state == STATE_INITIAL) {
            writer.append(XML_HEADER);
        }
        closeOpeningTag();
        String tag = key;
        if (StringUtils.isBlank(tag)) {
            tag = nextKey;
            if (StringUtils.isBlank(tag)) {
                if (state == STATE_ITEM || state == STATE_ARRAY) {
                    tag = nextItem;
                }
            }
        }
        if (StringUtils.isNotBlank(tag)) {
            writer.append('<').append(tag);
            tagState = OPENING_TAG;
        }
        states.push(state = STATE_OBJECT);
        tags.push(tag);
        nextKey = null;
        return this;
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
        tag = tags.pop();
        if (state == STATE_INITIAL) {
            throw new IllegalStateException("Still in initial state");
        }
        if (state == STATE_ARRAY || state == STATE_OBJECT) {
            closeTag();
        }
        state = states.peek();
        if (state == STATE_INITIAL) {
            state = STATE_FINAL;
        }
        if (state == STATE_ARRAY || state == STATE_ITEM) {
            nextItem = tag;
        }
        return this;
    }

    @Override
    public RestWriter links() throws IOException {
        return array();
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
    public RestWriter value(String value) throws IOException {
        boolean isItem = (state == STATE_ITEM || state == STATE_ARRAY)
                && StringUtils.isNotBlank(nextItem);
        if (isItem || tagState == OPENING_TAG) {
            closeOpeningTag();
            if (isItem) {
                writer.append('<').append(nextItem).append('>');
                escaped(value);
                writer.append('<').append('/').append(nextItem).append('>');
            } else {
                escaped(value);
            }
        } else {
            throw new IllegalStateException("Unexpected value without tag");
        }
        nextKey = null;
        return this;
    }

    @Override
    public RestWriter value(long l) throws IOException {
        return value(Long.toString(l));
    }

    @Override
    public RestWriter value(double d) throws IOException {
        return value(Double.toString(d));
    }

    @Override
    public RestWriter value(Number n) throws IOException {
        return value(n.toString());
    }

    @Override
    public RestWriter value(boolean b) throws IOException {
        return value(Boolean.toString(b));
    }

    @Override
    public RestWriter value(UUID uuid) throws IOException {
        return value(uuid.toString());
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
    public RestWriter href(Object... pathSegments) throws IOException {
        return value(hrefOf(pathSegments));
    }

    @Override
    public RestWriter pair(String key, String value) throws IOException {
        if (StringUtils.isBlank(key)) {
            throw new IllegalStateException("Missing tag name");
        }
        if (state == STATE_OBJECT) {
            closeOpeningTag();
            if (StringUtils.isNotBlank(value) || isSet(ALL_MEMBERS)) {
                writer.append('<').append(key);
                if (value != null) {
                    writer.append('>');
                    escaped(value);
                    writer.append("</").append(key).append('>'); //$NON-NLS-1$
                } else {
                    writer.append("/>"); //$NON-NLS-1$
                }
            }
        } else {
            throw new IllegalStateException("Unexpected attribute without tag");
        }
        nextKey = null;
        return this;
    }

    @Override
    public RestWriter pair(String key, long l) throws IOException {
        return pair(key, Long.toString(l));
    }

    @Override
    public RestWriter pair(String key, double d) throws IOException {
        return pair(key, Double.toString(d));
    }

    @Override
    public RestWriter pair(String key, Number n) throws IOException {
        return pair(key, n.toString());
    }

    @Override
    public RestWriter pair(String key, boolean b) throws IOException {
        return pair(key, Boolean.toString(b));
    }

    @Override
    public RestWriter pair(String key, UUID uuid) throws IOException {
        return pair(key, uuid != null ? uuid.toString(): null);
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
    public RestWriter attribute(String key, String value) throws IOException {
        if (tagState == OPENING_TAG && value != null) {
            writer.append(' ').append(key).append('=');
            writer.append('"');
            if (value != null) {
                escaped(value);
            }
            writer.append('"');
        } else {
            throw new IllegalStateException("Unexpected attribute");
        }
        nextKey = null;
        return this;
    }

    @Override
    public RestWriter attribute(String key, long l) throws IOException {
        return attribute(key, Long.toString(l));
    }

    @Override
    public RestWriter attribute(String key, double d) throws IOException {
        return attribute(key, Double.toString(d));
    }

    @Override
    public RestWriter attribute(String key, Number n) throws IOException {
        return attribute(key, n.toString());
    }

    @Override
    public RestWriter attribute(String key, boolean b) throws IOException {
        return attribute(key, Boolean.toString(b));
    }

    @Override
    public RestWriter attribute(String key, UUID uuid) throws IOException {
        return attribute(key, uuid != null? uuid.toString() : null);
    }

    @Override
    public RestWriter namespace(String key, String value) throws IOException {
        attribute(StringUtils.isBlank(key)? XMLNS_PREFIX : key, value);
        return this;
    }

    private void closeOpeningTag() throws IOException {
        if (tagState == OPENING_TAG) {
            writer.append('>');
            tagState = EXPECT_TEXT_NODE;
        }
    }
    private void closeTag() throws IOException {
        if (state == STATE_FINAL) {
            throw new IllegalStateException("Final state already reached");
        }
        if (state == STATE_INITIAL) {
            throw new IllegalStateException("Initial state");
        }
        if (tagState == OPENING_TAG) {
            writer.append("/>"); //$NON-NLS-1$
            tagState = EXPECT_OPENING_TAG;
        } else if (tag != null) {
            writer.append("</").append(tag).append('>'); //$NON-NLS-1$
            tagState = EXPECT_OPENING_TAG;
        }
    }

    @SuppressWarnings("nls")
    private RestWriter escaped(String s) throws IOException {
        int len = s.length();
        for (int i = 0; i < len; ++i) {
            char c = s.charAt(i);
            switch (c) {
            case '&':
                writer.append("&amp;");
                break;
            case '<':
                writer.append("&lt;");
                break;
            case '>':
                writer.append("&gt;");
                break;
            case '"':
                writer.append("&quot;");
                break;
            case '\'':
                writer.append("&apos;");
                break;
            default:
                writer.append(c);
            }
        }
        return this;
    }
}
