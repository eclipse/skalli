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
package org.eclipse.skalli.services.rest;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import org.restlet.data.MediaType;

/**
 * Interface for a writer for hierarchically structured REST formats like XML and JSON.
 * The interface is inspired by the <a href="http://json.org">JSON syntax</a> since
 * it distinguishes between arrays and objects as basic structures. However, it
 * supports also typical XML features like attributes and arrays with named elements.
 * <p>
 * Implementations of this interface should be derived from {@link RestWriterBase}
 * and must be registered as OSGI service.
 */
public interface RestWriter {

    /**
     * Enables the rendering of links with relative URLs (without web locator part)
     * instead of absolute URLs, which is the default.
     */
    public static long RELATIVE_LINKS = 0x0001L;

    /**
     * Enables the rendering of blank object members, i.e. object members
     * with value equal to <code>null</code> or the empty string.
     * By default, such members are surpressed.
     */
    public static long ALL_MEMBERS = 0x0002L;

    /**
     * Returns the {@link MediaType} this REST writer is able to produce.
     */
    public MediaType getMediaType();

    /**
     * Checks whether this writer is able to produce output for a given {@link MediaType}.
     * @param mediaType  the media type to check.
     * @return <code>true</<code>, if this writer supports the given media type.
     */
    public boolean isMediaType(MediaType mediaType);

    /**
     * Returns the web locator used to generated absolute URIs.
     * @return a web locator including protocol, host and port, or <code>null</code>
     * if no web locator has been specified.
     */
    public String getHost();

    /**
     * Concatenates the given path segments to a resource path.
     * Unless the option {@link #RELATIVE_LINKS} is set, the path is converted
     * to an absolute URL.
     *
     * @param pathSegments  a list of path segments.
     *
     * @return either a relative path or the absolute URL of a resource.
     */
    public String hrefOf(Object... pathSegments);

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
    public RestWriter set(long optionsMask);

    /**
     * Resets the given options.
     *
     * @param optionsMask  an OR-combination of option flags to reset.
     */
    public RestWriter reset(long optionsMask);

    /**
     * Checks that all structures (arrays/objects) have been
     * closed properly and flushes the underlying writer.
     * All subsequent attempts to create new structures will fail.
     *
     * @throws IOException  if an i/o error occured when writing to the underlying writer.
     * @throws IllegalStateException  if the writer is not yet in the final state,
     * because there are structures that have not been closed properly.
     */
    public void flush() throws IOException;

    /**
     * Defines the name for a subsequent structure, i.e. for an array or object.
     *
     * @param key  the name to define, or <code>null</code>.
     */
    public RestWriter key(String key) throws IOException;

    /**
     * Begins an array.
     * <p>
     * If a name has previously been defined with {@link #key(String)} the array is
     * rendered as named array. Otherwise depending on the requirements of the media type
     * a suitable default name is choosen. If the media type requires names for array elements,
     * a suitable default name is choosen for the elements.
     * <p>
     * Note, element names can be overriden individually with {@link #item(String)}.
     *
     * @throws IOException  if an i/o error occured when writing to the underlying writer.
     * @throws IllegalStateException  if the final state of this writer was already reached.
     */
    public RestWriter array() throws IOException;

    /**
     * Begins an array and defines the name for elements of that array
     * given the media type supports named array elements.
     * <p>
     * If a name has previously been defined with {@link #key(String)} the array is
     * rendered as named array. Otherwise depending on the requirements of the media type
     * a suitable default name is choosen.
     * <p>
     * Note, element names can be overriden individually with {@link #item(String)}.
     *
     * @param itemKey  the default name to assign to the array elements, or <code>null</code>.
     *
     * @throws IOException  if an i/o error occured when writing to the underlying writer.
     * @throws IllegalStateException  if the final state of this writer was already reached.
     */
    public RestWriter array(String itemKey) throws IOException;

    /**
     * Begins a named array and defines the name for elements of that array
     * given the media type supports named array elements.
     * <p>
     * Note, element names can be overriden individually with {@link #item(String)}.
     *
     * @param key  the name of the array, or <code>null</code>. Overrides any name that
     * has previously been defined with {@link #key(String)}.
     * @param itemKey  the default name to assign to the array elements, or <code>null</code>.
     *
     * @throws IOException  if an i/o error occured when writing to the underlying writer.
     * @throws IllegalStateException  if the final state of this writer was already reached.
     */
    public RestWriter array(String key, String itemKey) throws IOException;

    /**
     * Begins the next element of an array ("item"). If the media type requires named
     * array elements a suitable default name is choosen.
     *
     * @throws IOException  if an i/o error occured when writing to the underlying writer.
     * @throws IllegalStateException  if the final state of this writer was already reached,
     * or the method was called outside of an array.
     */
    public RestWriter item() throws IOException;

    /**
     * Begins the next element of an array (item) and defines the name for that and
     * all subsequent array elements given the media type supports named array elements.
     *
     * @param itemKey  the name to assign to the next and all subsequent array elements,
     * or <code>null</code>.
     *
     * @throws IOException  if an i/o error occured when writing to the underlying writer.
     * @throws IllegalStateException  if the final state of this writer was already reached,
     * or the method was called outside of an array.
     */
    public RestWriter item(String itemKey) throws IOException;

    /**
     * Begins an object.
     * <p>
     * If a name has previously been defined with {@link #key(String)} the object is
     * rendered as named object. Otherwise depending on the requirements of the media type
     * a suitable default name is choosen.
     *
     * @throws IOException  if an i/o error occured when writing to the underlying writer.
     * @throws IllegalStateException  if the final state of this writer was already reached.
     */
    public RestWriter object() throws IOException;

    /**
     * Begins a named object.
     *
     * @param key  the name of the object, or <code>null</code>. Overrides any name that
     * has previously been defined with {@link #key(String)}.
     *
     * @throws IOException  if an i/o error occured when writing to the underlying writer.
     * @throws IllegalStateException  if the final state of this writer was already reached.
     */
    public RestWriter object(String key) throws IOException;

    /**
     * Ends an object, array or array element (item).
     *
     * @throws IOException  if an i/o error occured when writing to the underlying writer.
     * @throws IllegalStateException  if the final state of this writer was already reached,
     * or the method is used outside of an object, array or array element.
     */
    public RestWriter end() throws IOException;

    /**
     * Begins a list of {@link #link(String, String) links}.
     * If the media type requires a name for the list, <tt>"links"</tt> is assumed.
     *
     * @throws IOException  if an i/o error occured when writing to the underlying writer.
     * @throws IllegalStateException  if the final state of this writer was already reached.
     */
    public RestWriter links() throws IOException;

    /**
     * Begins a named list of {@link #link(String, String) links}.
     *
     * @param key  the name to assign to the link list.
     *
     * @throws IOException  if an i/o error occured when writing to the underlying writer.
     * @throws IllegalStateException  if the final state of this writer was already reached.
     */
    public RestWriter links(String key) throws IOException;

    /**
     * Appends a link with given relation type and target URL.
     *
     * @param rel  the type of link relation.
     * @param href  the absolute URL of the linked resource.
     *
     * @throws IOException  if an i/o error occured when writing to the underlying writer.
     * @throws IllegalStateException  if the final state of this writer was already reached,
     * or the method was used outside of an object, array or {@link #links() link list}.
     */
    public RestWriter link(String rel, String href) throws IOException;

    /**
     * Appends a link with given relation type and resource path.
     *
     * @param rel  the type of link relation.
     * @param pathSegments  a list of path segments that are concatenated
     * by this method to form a relative resource path. Unless the option
     * {@link #RELATIVE_LINKS} is set, the path is converted to an absolute URL
     * before rendering.
     *
     * @throws IOException  if an i/o error occured when writing to the underlying writer.
     * @throws IllegalStateException  if the final state of this writer was already reached,
     * or the method was used outside of an object, array or {@link #links() link list}.
     */
    public RestWriter link(String rel, Object... pathSegments) throws IOException;

    /**
     * Appends a string as next element to an array or as text value to an object.
     * If the media type requires a name for the text value of an object, <tt>"value"</tt>
     * is assumed.
     * <p>
     * Note, once a text value has been appended to an object, no further members or attributes
     * can be appended to that object afterwards.
     *
     * @param s  the string to append.
     *
     * @throws IOException  if an i/o error occured when writing to the underlying writer.
     * @throws IllegalStateException  if the method was used outside of an object or array,
     * or the surrounding object already has a text value.
     */
    public RestWriter value(String s) throws IOException;

    /**
     * Appends the string representation of a number as next element to an array or
     * as text value to an object. If the media type requires a name for the text value
     * of an object, <tt>"value"</tt> is assumed.
     * <p>
     * Note, once a text value has been appended to an object, no further members or attributes
     * can be appended to that object afterwards.
     *
     * @param l  the number to append.
     *
     * @throws IOException  if an i/o error occured when writing to the underlying writer.
     * @throws IllegalStateException  if the method was used outside of an object or array,
     * or the surrounding object already has a text value.
     */
    public RestWriter value(long l) throws IOException;

    /**
     * Appends the string representation of a floating point number as next element to an
     * array or as text value to an object. If the media type requires a name for the text
     * value of an object, <tt>"value"</tt> is assumed.
     * <p>
     * Note, once a text value has been appended to an object, no further members or attributes
     * can be appended to that object afterwards.
     *
     * @param d  the floating point number to append.
     *
     * @throws IOException  if an i/o error occured when writing to the underlying writer.
     * @throws IllegalStateException  if the method was used outside of an object or array,
     * or the surrounding object already has a text value.
     */
    public RestWriter value(double d) throws IOException;

    /**
     * Appends the string representation of a boolean as next element to an array or as text
     * value to an object. If the media type requires a name for the text value of an object,
     * <tt>"value"</tt> is assumed.
     * <p>
     * Note, once a text value has been appended to an object, no further members or attributes
     * can be appended to that object afterwards.
     *
     * @param b  the boolean to append.
     *
     * @throws IOException  if an i/o error occured when writing to the underlying writer.
     * @throws IllegalStateException  if the method was used outside of an object or array,
     * or the surrounding object already has a text value.
     */
    public RestWriter value(boolean b) throws IOException;

    /**
     * Appends the string representation of a universally unique identifier (UUID) as next
     * element to an array or as text value to an object. If the media type requires a name
     * for the text value of an object, <tt>"value"</tt> is assumed.
     * <p>
     * Note, once a text value has been appended to an object, no further members or attributes
     * can be appended to that object afterwards.
     *
     * @param uuid  the universally unique identifier (UUID) to append
     *
     * @throws IOException  if an i/o error occured when writing to the underlying writer.
     * @throws IllegalStateException  if the method was used outside of an object or array,
     * or the surrounding object already has a text value.
     */
    public RestWriter value(UUID uuid) throws IOException;

    /**
     * Appends the string representation of the date given by the time in milliseconds
     * since January 1, 1970 as next element to an array or as text value to an object.
     * The string representation contains the date and time as specified by ISO 8601
     * following the pattern <tt>"yyyy-MM-dd'T'HH:mm:ss'Z'"</tt>. Timezone is always UTC.
     * This is compatible with the XML Schema type <tt>xsd:dateTime</tt>.
     * If the media type requires a name for the text value of an object, <tt>"value"</tt>
     * is assumed.
     * <p>
     * Note, once a text value has been appended to an object, no further members or attributes
     * can be appended to that object afterwards.
     *
     * @param millis  the date in milliseconds since January 1, 1970.
     *
     * @throws IOException  if an i/o error occured when writing to the underlying writer.
     * @throws IllegalStateException  if the method was used outside of an object or array,
     * or the surrounding object already has a text value.
     */
    public RestWriter datetime(long millis) throws IOException;

    /**
     * Appends the string representation of the date given by the time in milliseconds
     * since January 1, 1970 as next element to an array or as text value to an object.
     * The string representation contains the date without the time as specified by ISO 8601
     * following the pattern <tt>""yyyy-MM-dd""</tt>. Timezone is always UTC.
     * This is compatible with the XML Schema type <tt>xsd:date</tt>.
     * If the media type requires a name for the text value of an object, <tt>"value"</tt>
     * is assumed.
     * <p>
     * Note, once a text value has been appended to an object, no further members or attributes
     * can be appended to that object afterwards.
     *
     * @param millis  the date in milliseconds since January 1, 1970.
     *
     * @throws IOException  if an i/o error occured when writing to the underlying writer.
     * @throws IllegalStateException  if the method was used outside of an object or array,
     * or the surrounding object already has a text value.
     */
    public RestWriter date(long millis) throws IOException;

    /**
     * Appends the string representation of the time intervall given in milliseconds
     * as next element to an array or as text value to an object.
     * The string representation follows ISO8601 period format.
     * If the media type requires a name for the text value of an object, <tt>"value"</tt>
     * is assumed.
     * <p>
     * Note, once a text value has been appended to an object, no further members or attributes
     * can be appended to that object afterwards.
     *
     * @param millis  the duration in milliseconds.
     *
     * @throws IOException  if an i/o error occured when writing to the underlying writer.
     * @throws IllegalStateException  if the method was used outside of an object or array,
     * or the surrounding object already has a text value.
     */
    public RestWriter duration(long millis) throws IOException;

    /**
     * Append an URL as next element to an array or as text value to an object.
     * If the media type requires a name for the text value of an object, <tt>"value"</tt>
     * is assumed.
     * <p>
     * Note, once a text value has been appended to an object, no further members or attributes
     * can be appended to that object afterwards.
     *
     * @param pathSegments  a list of path segments that are concatenated
     * by this method to form a relative resource path. Unless the option
     * {@link #RELATIVE_LINKS} is set, the path is converted to an absolute URL
     * before rendering.
     *
     * @throws IOException  if an i/o error occured when writing to the underlying writer.
     * @throws IllegalStateException  if the method was used outside of an object or array,
     * or the surrounding object already has a text value.
     */
    public RestWriter href(Object... pathSegments) throws IOException;

    /**
     * Appends an object member with a string value.
     *
     * @param key  the name the object member, never <code>null</code>.
     * @param value  the value of the object member, or <code>null</code>.
     *
     * @throws IOException  if an i/o error occured when writing to the underlying writer.
     * @throws IllegalStateException  if the method was used outside of an object,
     * or the key was <code>null</code>.
     */
    public RestWriter pair(String key, String value) throws IOException;

    /**
     * Appends an object member with a numerical value.
     *
     * @param key  the name the object member, never <code>null</code>.
     * @param l  the numerical value of the object member.
     *
     * @throws IOException  if an i/o error occured when writing to the underlying writer.
     * @throws IllegalStateException  if the method was used outside of an object,
     * or the key was <code>null</code>.
     */
    public RestWriter pair(String key, long l) throws IOException;

    /**
     * Appends an object member with a floating point value.
     *
     * @param key  the name the object member, never <code>null</code>.
     * @param d  the floating point value of the object member.
     *
     * @throws IOException  if an i/o error occured when writing to the underlying writer.
     * @throws IllegalStateException  if the method was used outside of an object,
     * or the key was <code>null</code>.
     */
    public RestWriter pair(String key, double d) throws IOException;

    /**
     * Appends an object member with a boolean value.
     *
     * @param key  the name the object member, never <code>null</code>.
     * @param b  the boolean value of the object member.
     *
     * @throws IOException  if an i/o error occured when writing to the underlying writer.
     * @throws IllegalStateException  if the method was used outside of an object,
     * or the key was <code>null</code>.
     */
    public RestWriter pair(String key, boolean b) throws IOException;

    /**
     * Appends an object member with a universally unique identifier (UUID) as value.
     *
     * @param key  the name the object member, never <code>null</code>.
     * @param uuid  a universally unique identifier (UUID).
     *
     * @throws IOException  if an i/o error occured when writing to the underlying writer.
     * @throws IllegalStateException  if the method was used outside of an object,
     * or the key was <code>null</code>.
     */
    public RestWriter pair(String key, UUID uuid) throws IOException;

    /**
     * Appends an object member with a date value.
     *
     * @param key  the name the object member, never <code>null</code>.
     * @param millis  a timestamp in milliseconds. The timestamp is converted
     * to ISO 8601 date representation (see {@link #date(long)}).
     *
     * @throws IOException  if an i/o error occured when writing to the underlying writer.
     * @throws IllegalStateException  if the method was used outside of an object,
     * or the key was <code>null</code>.
     */
    public RestWriter date(String key, long millis) throws IOException;

    /**
     * Appends an object member with a date and time value.
     *
     * @param key  the name the object member, never <code>null</code>.
     * @param millis  a timestamp in milliseconds. The timestamp is converted
     * to ISO 8601 date/time representation (see {@link #datetime(long)}).
     *
     * @throws IOException  if an i/o error occured when writing to the underlying writer.
     * @throws IllegalStateException  if the method was used outside of an object,
     * or the key was <code>null</code>.
     */
    public RestWriter datetime(String key, long millis) throws IOException;

    /**
     * Appends an object member representing a time intervall.
     *
     * @param key  the name the object member, never <code>null</code>.
     * @param millis  the time intervall in milliseconds. The  time intervall is converted
     * to ISO 8601 time intervall representation (see {@link #duration(long)}).
     *
     * @throws IOException  if an i/o error occured when writing to the underlying writer.
     * @throws IllegalStateException  if the method was used outside of an object,
     * or the key was <code>null</code>.
     */
    public RestWriter duration(String key, long millis) throws IOException;

    /**
     * Appends an object member with a <tt>"millis"</tt> attribute
     * followed by the ISO 8601 representation of the timestamp as text value.
     *
     * @param key  the name the object member, never <code>null</code>.
     * @param millis  a timestamp in milliseconds. The timestamp is converted
     * to ISO 8601 date/time representation (see {@link #datetime(long)}).
     *
     * @throws IOException  if an i/o error occured when writing to the underlying writer.
     * @throws IllegalStateException  if the method was used outside of an object,
     * or the key was <code>null</code>.
     */
    public RestWriter timestamp(String key, long millis) throws IOException;

    /**
     * Appends an object member with an URL as value.
     *
     * @param key  the name the object member, never <code>null</code>.
     * @param pathSegments  a list of path segments that are concatenated
     * by this method to form a relative resource path. Unless the option
     * {@link #RELATIVE_LINKS} is set, the path is converted to an absolute URL
     * before rendering.
     *
     * @throws IOException  if an i/o error occured when writing to the underlying writer.
     * @throws IllegalStateException  if the method was used outside of an object,
     * or the key was <code>null</code>.
     */
    public RestWriter href(String key, Object... pathSegments) throws IOException;

    /**
     * Appends an object attribute with a string value.
     * <p>
     * Note, attributes must be appended to an object before any other members
     * or text values.
     *
     * @param key  the name of the attribute, never <code>null</code>.
     * @param value  the value of the attribute, or <code>null</code>.
     *
     * @throws IOException  if an i/o error occured when writing to the underlying writer.
     * @throws IllegalStateException  if the method was used outside of an object,
     * or the object has already members, or the key was <code>null</code>.
     */
    public RestWriter attribute(String key, String value) throws IOException;

    /**
     * Appends an object attribute with a numerical value.
     * <p>
     * Note, attributes must be appended to an object before any other members
     * or text values.
     *
     * @param key  the name of the attribute, never <code>null</code>.
     * @param l  the numerical value of the attribute.
     *
     * @throws IOException  if an i/o error occured when writing to the underlying writer.
     * @throws IllegalStateException  if the method was used outside of an object,
     * or the object has already members, or the key was <code>null</code>.
     */
    public RestWriter attribute(String key, long l) throws IOException;

    /**
     * Appends an object attribute with a floating point value.
     * <p>
     * Note, attributes must be appended to an object before any other members
     * or text values.
     *
     * @param key  the name of the attribute, never <code>null</code>.
     * @param d  the floating point value of the attribute.
     *
     * @throws IOException  if an i/o error occured when writing to the underlying writer.
     * @throws IllegalStateException  if the method was used outside of an object,
     * or the object has already members, or the key was <code>null</code>.
     */
    public RestWriter attribute(String key, double d) throws IOException;

    /**
     * Appends an object attribute with a boolean value.
     * <p>
     * Note, attributes must be appended to an object before any other members
     * or text values.
     *
     * @param key  the name of the attribute, never <code>null</code>.
     * @param d  the boolean value of the attribute.
     *
     * @throws IOException  if an i/o error occured when writing to the underlying writer.
     * @throws IllegalStateException  if the method was used outside of an object,
     * or the object has already members, or the key was <code>null</code>.
     */
    public RestWriter attribute(String key, boolean b) throws IOException;

    /**
     * Appends an object attribute with a boolean value.
     * <p>
     * Note, attributes must be appended to an object before any other members
     * or text values.
     *
     * @param key  the name of the attribute, never <code>null</code>.
     * @param uuid  a universally unique identifier (UUID).
     *
     * @throws IOException  if an i/o error occured when writing to the underlying writer.
     * @throws IllegalStateException  if the method was used outside of an object,
     * or the object has already members, or the key was <code>null</code>.
     */
    public RestWriter attribute(String key, UUID uuid) throws IOException;

    /**
     * Appends a namespace attribute.
     * <p>
     * Note, namespace attributes must be appended to an object before any other members
     * or text values.
     *
     * @param key  the namespace identifier including the namespace prefix, e.g. <tt>"xmlns:xsi"</tt>.
     * @param value  the value of the namespace attribute, usually an URI.
     *
     * @throws IOException  if an i/o error occured when writing to the underlying writer.
     * @throws IllegalStateException  if the method was used outside of an object,
     * or the object has already members, or the key was <code>null</code>.
     */
    public RestWriter namespace(String name, String value) throws IOException;

    /**
     * Appends the given collection as array of strings with given name and element name.
     *
     * @param key  the name of the array, or <code>null</code>. Overrides any name that
     * has previously been defined with {@link #key(String)}.
     * @param itemKey  the default name to assign to the array elements, or <code>null</code>.
     * @param values  the collection to append.
     *
     * @throws IOException  if an i/o error occured when writing to the underlying writer.
     * @throws IllegalStateException  if the final state of this writer was already reached.
     */
    public RestWriter collection(String key, String itemKey, Collection<String> values) throws IOException;
}
