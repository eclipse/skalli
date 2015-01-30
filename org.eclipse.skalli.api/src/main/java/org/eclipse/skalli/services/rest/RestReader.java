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
package org.eclipse.skalli.services.rest;

import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import org.restlet.data.MediaType;

/**
 * Interface for a reader of structured REST formats like XML and JSON.
 * The interface is inspired by com.google.gson.stream.
 */
public interface RestReader {

    /**
     * Returns the {@link MediaType} this REST reader is able to read and parse.
     */
    public MediaType getMediaType();

    /**
     * Checks whether this reader is able to read and parse input of the
     * given {@link MediaType}.
     *
     * @param mediaType  the media type to check.
     * @return <code>true</<code>,if this reader supports the given media type.
     */
    public boolean isMediaType(MediaType mediaType);

    /**
     * Checks if a given set of options is active.
     *
     * @param optionsMask an OR-combination of option flags.
     * @return <code>true</code> if all given options are active.
     */
    public boolean isSet(long optionsMask);

    /**
     * Sets the given options.
     *
     * @param optionsMask  an OR-combination of option flags to set.
     */
    public void set(long optionsMask);

    /**
     * Resets the given options.
     *
     * @param optionsMask  an OR-combination of option flags to reset.
     */
    public void reset(long optionsMask);

    /**
     * Returns <code>true</code> if the current object has more
     * attributes, or the current array has more elements.
     *
     * @throws IOException if an i/o error occured.
     */
    public boolean hasMore() throws IOException;

    /**
     * Skips the current object attribute or array element.
     *
     * @throws IOException if an i/o error occured.
     */
    public void skip() throws IOException;


    /**
     * Returns <code>true</code> if the current token in the input stream
     * is the key of an object attribute.
     *
     * @throws IOException if an i/o error occured.
     */
    public boolean isKey() throws IOException;

    /**
     * Returns <code>true</code> if the current token in the input stream
     * is the key of an object attribute, and the value of that key equals any
     * of the given keys.
     *
     * @param keys the keys to compare the current token with.
     *
     * @throws IOException if an i/o error occured.
     */
    public boolean isKeyAnyOf(String... keys) throws IOException;

    /**
     * Returns the key of the current object attribute.
     *
     * @throws IOException if an i/o error occured.
     * @throws IllegalStateException  if the current token in the input stream
     * is not a key of an object attribute.
     */
    public String key() throws IOException;

    /**
     * Returns the value of the current object attribute or array element as string.
     * If the value is a numerical literal, the string representation of the
     * numerical value is returned. If the value is a boolean literal, either
     * <tt>"true"</tt> or <tt>"false"</tt> is returned.
     * <p>
     * Note, <code>null</code> may be returned if the value is missing or undefined.
     * This depends on the media type. For example, an XML tag of the form &lt;tag/&gt;
     * has a value equivalent to <code>null</code>, but note that &lt;tag&gt;&lt;/tag&gt;
     * will return an empty string! JSON supports the literal <tt>null</tt> directly,
     * i.e. one can define an object attribute like the following: <tt>{"attr":null}</tt>.
     *
     * @throws IOException if an i/o error occured.
     * @throws IllegalStateException  if the value is neither a string literal,
     * nor any other literal that can be interpreted as string.
     */
    public String valueString() throws IOException;

    /**
     * Returns the value of the current object attribute or array element as {@link Long long}.
     * If the value is a string literal that can be interpreted as numerical literal,
     * the numerical value of the string is returned.
     * <p>
     * @throws IOException if an i/o error occured.
     * @throws NumberFormatException  if the value could not be interpreted as number, or
     * could not be represented by a long.
     * @throws IllegalStateException  if the value is neither a numerical nor a string literal.
     */
    public long valueLong() throws IOException;

    /**
     * Returns the value of the current object attribute or array element as {@link Double double}.
     * If the value is a string literal that can be interpreted as numerical literal,
     * the numerical value of the string is returned.
     *
     * @throws IOException if an i/o error occured.
     * @throws NumberFormatException  if the value could not be interpreted as number, or
     * could not be represented by a double.
     * @throws IllegalStateException  if the value is neither a numerical nor a string literal.
     */
    public double valueDouble() throws IOException;

    /**
     * Returns the value of the current object attribute or array element as {@link Boolean boolean}.
     * If the value is a string literal that can be interpreted as boolean literal,
     * the boolean value of the string is returned.
     *
     * @throws IOException if an i/o error occured.
     * @throws IllegalStateException  if the value is not an expected literal.
     */
    public boolean valueBoolean() throws IOException;

    /**
     * Returns the value of the current object attribute or array element as {@link UUID}.
     * <p>
     * Note, <code>null</code> may be returned if the value is missing or undefined.
     *
     * @throws IOException if an i/o error occured.
     * @throws IllegalArgumentException  if the value could not be interpreted as UUID.
     * @throws IllegalStateException  if the value is not a string literal.
     */
    public UUID valueUUID() throws IOException;

    /**
     * Returns the value of the current object attribute or array element as {@link URL}.
     *
     * Note, <code>null</code> may be returned if the value is missing or undefined.
     *
     * @throws IOException if an i/o error occured.
     * @throws IllegalArgumentException  if the value could not be interpreted as URL.
     * @throws IllegalStateException  if the value is not a string literal.
     */
    public URL valueURL() throws IOException;

    /**
     * Returns the value of the current object attribute or array element as {@link Calendar}.
     * The value must be the string representation of a date including a time and a timezone
     * representation as defined by ISO 8601, e.g. something of the form <tt>"yyyy-MM-dd'T'HH:mm:ss'Z'"</tt>.
     * This is compatible with the XML Schema type <tt>xsd:dateTime</tt>.
     * <p>
     * Note, <code>null</code> may be returned if the value is missing or undefined.
     *
     * @throws IOException if an i/o error occured.
     * @throws IllegalArgumentException  if the value could not be interpreted as ISO 8601
     * date/time representation.
     * @throws IllegalStateException  if the value is not a string literal.
     */
    public Calendar valueDatetime() throws IOException;

    /**
     * Returns the value of the current object attribute or array element as {@link Calendar}.
     * The value must be the string representation of a date as defined by ISO 8601, e.g. something
     * of the form <tt>"yyyy-MM-dd"</tt>. This is compatible with the XML Schema type <tt>xsd:date</tt>.
     * <p>
     * Note, <code>null</code> may be returned if the value is missing or undefined.
     *
     * @throws IOException if an i/o error occured.
     * @throws IllegalArgumentException  if the value could not be interpreted as ISO 8601
     * date representation.
     * @throws IllegalStateException  if the value is not a string literal.
     */
    public Calendar valueDate() throws IOException;

    /**
     * Returns the value of the current object attribute as string.
     * If the value is a numerical literal, the string representation of the
     * numerical value is returned. If the value is a boolean literal, either
     * <tt>"true"</tt> or <tt>"false"</tt> is returned.
     * <p>
     * Note, <code>null</code> may be returned if the value is missing or undefined.
     *
     * @throws IOException if an i/o error occured.
     * @throws IllegalStateException  if the value is neither a string literal,
     * nor any other literal that can be interpreted as string.
     */
    public String attributeString() throws IOException;

    /**
     * Returns the value of the current object attribute as {@link Long long}.
     * If the value is a string literal that can be interpreted as numerical literal,
     * the numerical value of the string is returned.
     * <p>
     * @throws IOException if an i/o error occured.
     * @throws NumberFormatException  if the value could not be interpreted as number, or
     * could not be represented by a long.
     * @throws IllegalStateException  if the value is neither a numerical nor a string literal.
     */
    public long attributeLong() throws IOException;

    /**
     * Returns the value of the current object attribute as {@link Double double}.
     * If the value is a string literal that can be interpreted as numerical literal,
     * the numerical value of the string is returned.
     *
     * @throws IOException if an i/o error occured.
     * @throws NumberFormatException  if the value could not be interpreted as number, or
     * could not be represented by a double.
     * @throws IllegalStateException  if the value is neither a numerical nor a string literal.
     */
    public double attributeDouble() throws IOException;

    /**
     * Returns the value of the current object attribute as {@link Boolean boolean}.
     * If the value is a string literal that can be interpreted as boolean literal,
     * the boolean value of the string is returned.
     *
     * @throws IOException if an i/o error occured.
     * @throws IllegalStateException  if the value is not an expected literal.
     */
    public boolean attributeBoolean() throws IOException;

    /**
     * Returns the value of the current object attribute as {@link UUID}.
     * <p>
     * Note, <code>null</code> may be returned if the value is missing or undefined.
     *
     * @throws IOException if an i/o error occured.
     * @throws IllegalArgumentException  if the value could not be interpreted as UUID.
     * @throws IllegalStateException  if the value is not a string literal.
     */
    public UUID attributeUUID() throws IOException;

    /**
     * Returns the value of the current object attribute as {@link URL}.
     * <p>
     * Note, <code>null</code> may be returned if the value is missing or undefined.
     *
     * @throws IOException if an i/o error occured.
     * @throws IllegalArgumentException  if the value could not be interpreted as URL.
     * @throws IllegalStateException  if the value is not a string literal.
     */
    public URL attributeURL() throws IOException;

    /**
     * Returns the value of the current object attribute as {@link Calendar}.
     * The value must be the string representation of a date including a time and a timezone
     * representation as defined by ISO 8601, e.g. something of the form <tt>"yyyy-MM-dd'T'HH:mm:ss'Z'"</tt>.
     * This is compatible with the XML Schema type <tt>xsd:dateTime</tt>.
     * <p>
     * Note, <code>null</code> may be returned if the value is missing or undefined.
     *
     * @throws IOException if an i/o error occured.
     * @throws IllegalArgumentException  if the value could not be interpreted as ISO 8601
     * date/time representation.
     * @throws IllegalStateException  if the value is not a string literal.
     */
    public Calendar attributeDatetime() throws IOException;

    /**
     * Returns the value of the current object attribute as {@link Calendar}.
     * The value must be the string representation of a date as defined by ISO 8601, e.g. something
     * of the form <tt>"yyyy-MM-dd"</tt>. This is compatible with the XML Schema type <tt>xsd:date</tt>.
     * <p>
     * Note, <code>null</code> may be returned if the value is missing or undefined.
     *
     * @throws IOException if an i/o error occured.
     * @throws IllegalArgumentException  if the value could not be interpreted as ISO 8601
     * date representation.
     * @throws IllegalStateException  if the value is not a string literal.
     */
    public Calendar attributeDate() throws IOException;

    /**
     * Returns <code>true</code> if the current token in the input stream marks the
     * begin of an array.
     * <p>
     * Note that it depends on the input format whether arrays and objects
     * can be distinguished upfront, if at all. For example, XML has no notion
     * of arrays and objects, but only sequences of tags. However, an implementation
     * must at least guarantee that a subsequent call of {@link #array()} or
     * {@link #array(String)} will not fail with an {@link IllegalStateException}
     * if this method returned <code>true</code>.
     *
     * @throws IOException if an i/o error occured.
     */
    public boolean isArray() throws IOException;

    /**
     * Expects that the current token in the input stream marks the begin of an array.
     * <p>
     * Note that it's at discretion of an implementation how to handle
     * arrays containing elements with differing names (in case the media
     * type supports arrays with named elements at all). The prefered way
     * is to throw an {@link IllegalStateException} in that case.
     *
     * @throws IOException if an i/o error occured.
     * @throws IllegalStateException  if the current token does not mark
     * the begin of an array.
     */
    public void array() throws IOException;

    /**
     * Expects that the current token in the input stream  marks the begin of an array
     * with elements of the given name.
     * <p>
     * Note that it's at discretion of an implementation how to handle array
     * elements with wrong names. It may ignore element names entirely (for example,
     * JSON does not support arrays with named elements), filter out such
     * elements, or throw an {@link IllegalStateException}.
     *
     * @param itemKey the expected name of array elements.
     *
     * @throws IOException if an i/o error occured.
     * @throws IllegalStateException  if the current token does not mark
     * the begin of an array.
     */
    public void array(String itemKey) throws IOException;

    /**
     * Returns <code>true</code> if the current token in the input stream marks the
     * begin of an object.
     * <p>
     * Note that it depends on the input format whether arrays and objects
     * can be distinguished upfront, if at all. For example, XML has no notion
     * of arrays and objects, but only sequences of tags. However, an implementation
     * must at least guarantee that a subsequent call of {@link #object()} will not
     * fail with an {@link IllegalStateException} if this method returned <code>true</code>.
     *
     * @throws IOException if an i/o error occured.
     */
    public boolean isObject() throws IOException;

    /**
     * Expects that the current token in the input stream marks the begin of an object.
     *
     * @throws IOException if an i/o error occured.
     * @throws IllegalStateException if the current token in the input stream does not
     * mark the begin of an object.
     */
    public void object() throws IOException;

    /**
     * Expects that the current token in the input stream marks the end
     * of an object or array.
     *
     * @throws IOException if an i/o error occured.
     * @throws IllegalStateException  if the current token does not indicate
     * the end of an object or array.
     */
    public void end() throws IOException;

    /**
     * Expects that the current token in the input stream is an array of
     * string values with the given element names. The whole array is read
     * and parsed till the end of the array and the array elements are
     * returned as strings.
     * <p>
     * This method is a convenience method equivalent to the following code snippet:
     * <pre>
     * List<String> items = new ArrayList<String>();
     * reader.array(itemKey);
     * while (reader.hasMore()) {
     *     items.add(reader.valueString());
     * }
     * reader.end();
     * </pre>
     *
     * @param itemKey the expected name of array elements.
     * @return a list of strings, or an empty list.
     *
     * @throws IOException if an i/o error occured.
     * @throws IllegalStateException  if the next element is not an array, or
     * any of the array elements could not be interpreted as string literal.
     */
    public List<String> collection(String itemKey) throws IOException;
}
