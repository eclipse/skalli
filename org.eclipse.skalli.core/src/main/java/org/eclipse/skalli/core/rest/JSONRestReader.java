/*******************************************************************************
 * Copyright (c) 2010-2015 SAP AG and others.
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
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.CharacterStack;
import org.eclipse.skalli.commons.URLUtils;
import org.eclipse.skalli.services.rest.RestReader;
import org.restlet.data.MediaType;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;


public class JSONRestReader implements RestReader {

    /** Option to enable @-prefixed attributes. */
    public static final int PREFIXED_ATTRIBUTES = 0x10000;

    /** Option to enable strict parsing */
    public static final int STRICT = 0x20000;


    private static final MediaType MEDIA_TYPE = MediaType.APPLICATION_JSON;

    private static final char STATE_INITIAL = '\u03B1';
    private static final char STATE_FINAL = '\u03C9';
    private static final char STATE_ARRAY = 'A';
    private static final char STATE_OBJECT = 'O';

    private static final char EXPECT_KEY = 'k';
    private static final char EXPECT_VALUE = 'v';

    private JsonReader json;
    private long options;

    // stack for state machine
    private CharacterStack states;

    // the current state
    private char state;

    private char sequenceState;

    // look-ahead for attribute keys
    private String lookAhead;

    public JSONRestReader(Reader reader) {
        this(reader, 0);
    }

    public JSONRestReader(Reader reader, int options) {
        json = new JsonReader(reader);
        states = new CharacterStack();
        states.push(state = STATE_INITIAL);
        sequenceState = EXPECT_KEY;
        set(options);
    }

    @Override
    public MediaType getMediaType() {
        return MEDIA_TYPE;
    }

    @Override
    public boolean isMediaType(MediaType mediaType) {
        return mediaType != null && mediaType.equals(getMediaType());
    }

    @Override
    public boolean isSet(long optionsMask) {
        return (options & optionsMask) == optionsMask;
    }

    @Override
    public void set(long optionsMask) {
        options |= optionsMask;
        json.setLenient(!isSet(STRICT));
    }

    @Override
    public void reset(long optionsMask) {
        options &= ~optionsMask;
    }

    @Override
    public boolean hasMore() throws IOException {
        if (state == STATE_FINAL) {
            return false;
        }
        return json.hasNext();
    }

    @Override
    public boolean isKey() throws IOException {
        if (state == STATE_FINAL) {
            return false;
        }
        return lookAhead != null || json.peek() == JsonToken.NAME;
    }

    @Override
    public boolean isKey(String key) throws IOException {
        return isKeyAnyOf(key);
    }

    @Override
    public boolean isKeyAnyOf(String... keys) throws IOException {
        if (state == STATE_FINAL) {
            return false;
        }
        if (keys == null) {
            return false;
        }
        if (lookAhead == null) {
            if (json.peek() != JsonToken.NAME) {
                return false;
            }
            lookAhead = key();
        }
        for (String key: keys) {
            if (key.equals(lookAhead)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String key() throws IOException {
        assertNotFinal();
        if (lookAhead == null && sequenceState != EXPECT_KEY) {
            throw new IllegalStateException("Key expected");
        }
        String key = lookAhead;
        if (key != null) {
            lookAhead = null;
            return key;
        }
        key = json.nextName();
        sequenceState = EXPECT_VALUE;
        return isSet(PREFIXED_ATTRIBUTES)? StringUtils.removeStart(key, "@") : key; //$NON-NLS-1$
    }

    @Override
    public void skip() throws IOException {
        assertNotFinal();
        skipKey();
        skipValue();
        sequenceState = EXPECT_KEY;
    }

    @Override
    public boolean isValue() throws IOException {
        if (state == STATE_FINAL) {
            return false;
        }
        if (isKey("value")) { //$NON-NLS-1$
            return true;
        }
        JsonToken next = json.peek();
        return lookAhead == null && (next == JsonToken.STRING || next == JsonToken.NUMBER || next == JsonToken.BOOLEAN
                || next == JsonToken.BEGIN_ARRAY || next == JsonToken.BEGIN_OBJECT || next == JsonToken.NULL);
    }

    @Override
    public String valueString() throws IOException {
        assertNotFinal();
        skipKey();
        String value = null;
        switch (json.peek()) {
        case NULL:
            json.nextNull();
            break;
        case BOOLEAN:
            value = Boolean.toString(json.nextBoolean());
            break;
        default:
            value = json.nextString();
            break;
        }
        sequenceState = EXPECT_KEY;
        return value;
    }

    @Override
    public long valueLong() throws IOException {
        assertNotFinal();
        skipKey();
        long value = json.nextLong();
        sequenceState = EXPECT_KEY;
        return value;
    }

    @Override
    public double valueDouble() throws IOException {
        assertNotFinal();
        skipKey();
        double value = json.nextDouble();
        sequenceState = EXPECT_KEY;
        return value;
    }

    @Override
    public boolean valueBoolean() throws IOException {
        assertNotFinal();
        skipKey();
        boolean value = json.nextBoolean();
        sequenceState = EXPECT_KEY;
        return value;
    }

    @Override
    public UUID valueUUID() throws IOException {
        assertNotFinal();
        skipKey();
        UUID value = json.peek() != JsonToken.NULL ? UUID.fromString(valueString()) : null;
        sequenceState = EXPECT_KEY;
        return value;
    }

    @Override
    public URL valueURL() throws IOException {
        assertNotFinal();
        skipKey();
        URL value = json.peek() != JsonToken.NULL ? URLUtils.asURL(valueString()) : null;
        sequenceState = EXPECT_KEY;
        return value;
    }

    @Override
    public Calendar valueDatetime() throws IOException {
        assertNotFinal();
        skipKey();
        Calendar value = json.peek() != JsonToken.NULL ? DatatypeConverter.parseDateTime(valueString()) : null;
        sequenceState = EXPECT_KEY;
        return value;
    }

    @Override
    public Calendar valueDate() throws IOException {
        assertNotFinal();
        skipKey();
        Calendar value = json.peek() != JsonToken.NULL ? DatatypeConverter.parseDate(valueString()) : null;
        sequenceState = EXPECT_KEY;
        return value;
    }

    @Override
    public String attributeString() throws IOException {
        return valueString();
    }

    @Override
    public long attributeLong() throws IOException {
        return valueLong();
    }

    @Override
    public double attributeDouble() throws IOException {
        return valueDouble();
    }

    @Override
    public boolean attributeBoolean() throws IOException {
        return valueBoolean();
    }

    @Override
    public UUID attributeUUID() throws IOException {
        return valueUUID();
    }

    @Override
    public URL attributeURL() throws IOException {
        return valueURL();
    }

    @Override
    public Calendar attributeDatetime() throws IOException {
        return valueDatetime();
    }

    @Override
    public Calendar attributeDate() throws IOException {
        return valueDate();
    }

    @Override
    public boolean isArray() throws IOException {
        if (state == STATE_FINAL) {
            return false;
        }
        return json.peek() == JsonToken.BEGIN_ARRAY;
    }

    @Override
    public void array() throws IOException {
        array(null);
    }

    @Override
    public void array(String itemKey) throws IOException {
        assertNotFinal();
        skipKey();
        json.beginArray();
        states.push(state = STATE_ARRAY);
        sequenceState = EXPECT_VALUE;
    }

    @Override
    public boolean isObject() throws IOException {
        if (state == STATE_FINAL) {
            return false;
        }
        return json.peek() == JsonToken.BEGIN_OBJECT;
    }

    @Override
    public void object() throws IOException {
        assertNotFinal();
        skipKey();
        json.beginObject();
        states.push(state = STATE_OBJECT);
        sequenceState = EXPECT_KEY;
    }

    @Override
    public void end() throws IOException {
        assertNotFinal();
        if (state == STATE_INITIAL) {
            throw new IllegalStateException("Still in initial state");
        }
        state = states.pop();
        if (state == STATE_ARRAY) {
            skipMore();
            json.endArray();
        } else if (state == STATE_OBJECT) {
            skipMore();
            json.endObject();
        } else if (state == STATE_INITIAL) {
            throw new IllegalStateException("Still in initial state");
        }
        state = states.peek();
        if (state == STATE_INITIAL) {
            state = STATE_FINAL;
        }
        sequenceState = EXPECT_KEY;
    }

    @Override
    public List<String> collection(String itemKey) throws IOException {
        List<String> items = new ArrayList<String>();
        array(itemKey);
        while (hasMore()) {
            items.add(valueString());
        }
        end();
        return items;
    }

    private void assertNotFinal() {
        if (state == STATE_FINAL) {
            throw new IllegalStateException("Final state already reached");
        }
    }

    private void skipKey() throws IOException {
        if (isKey()) {
            key();
        }
    }

    private void skipValue() throws IOException {
        if (isValue()) {
            json.skipValue();
        }
    }

    private void skipMore() throws IOException {
        while (hasMore()) {
            skip();
        }
    }
}
