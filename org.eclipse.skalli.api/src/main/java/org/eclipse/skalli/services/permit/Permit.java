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
package org.eclipse.skalli.services.permit;

import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrMatcher;
import org.apache.commons.lang.text.StrTokenizer;

public class Permit implements Comparable<Permit> {

    /**
     * Enumeration representing the default levels of a permit
     * equivalent to allowing (<code>level = 1</code>) or denying
     * (<code>level = 0</code>) a certain action.
     * <p>
     * Note: Custom levels can be set with the alternative constructors
     * {@link Permit#Permit(int, String, String)} and
     * {@link Permit#Permit(int, String, String...)}, respectively.
     *
     * @see Permit#Permit(Level, String, String)}
     */
    public static enum Level {
        FORBID(0), ALLOW(1);

        private final int level;

        private Level(int level) {
            this.level = level;
        }

        public int intValue() {
            return level;
        }
    }

    /**
     * Wildcard for permit actions. A permit with this action
     * applies to all actions, i.e. to the standard actions
     * like <tt>GET</tt>, <tt>PUT</tt>, <tt>POST</tt> and <tt>DELETE</tt>,
     * but also to all extension actions.
     */
    public static final String ALL_ACTIONS = "**"; //$NON-NLS-1$

    /**
     * Action that implies the retrieval of a resource (in the sense
     * of an REST <tt>GET</tt> request).
     */
    public static final String ACTION_GET = "GET"; //$NON-NLS-1$

    /**
     * Action that implies the change of a resource (in the sense
     * of an REST <tt>PUT</tt> request).
     */
    public static final String ACTION_PUT = "PUT"; //$NON-NLS-1$

    /**
     * Action that implies the creation of a member in a collection-like
     * resource (in the sense of an REST <tt>POST</tt> request).
     */
    public static final String ACTION_POST = "POST"; //$NON-NLS-1$

    /**
     * Action that implies the deletion of a resource (in the sense
     * of an REST <tt>DELETE</tt> request).
     */
    public static final String ACTION_DELETE = "DELETE"; //$NON-NLS-1$

    /**
     * The root of the permit tree. A permit with this path applies
     * to all resources of a Skalli instance.
     */
    public static final String ROOT = "/"; //$NON-NLS-1$

    /**
     * Wildcard for permit paths. By replacing a path segment that represents
     * members of a resource collection, e.g. projects in the collection
     * of all projects, a permit can be applied to all members of that collection.
     * <p>
     * Example: a permit with the path <tt>"/projects/**</tt> would apply to
     * all known projects.
     */
    public static final String WILDCARD = "**"; //$NON-NLS-1$

    /**
     * Wildcard representing the "current" project the user has requested.
     * When evaluating permits, this wildcard is replaced with the
     * {@link org.eclipse.skalli.modelProject#getProjectId() symbolic name}
     * of a project.
     * <p>
     * Example: a permit with the path <tt>"/projects/${project}</tt> would
     * map to <tt>"/projects/technology.skalli</tt> when a user would look
     * at the detail page of a project <tt>"technology.skalli"</tt>.
     */
    public static final String PROJECT_WILDCARD = "${project}"; //$NON-NLS-1$

    /**
     * Wildcard representing the currently logged in user.
     * When evaluating permits, this wildcard is replaced with the identifier
     * of the current user.
     * <p>
     * Example: a permit with the path <tt>"/users/${user}</tt> would
     * map to <tt>"/projects/hugo</tt> when a user with identifier <tt>"hugo"</tt>
     * would request that resource.
     */
    public static final String USER_WILDCARD = "${user}"; //$NON-NLS-1$

    /**
     * Permit set that forbids all actions on all resources.
     */
    public static final Permit FORBID_ALL = Permit.valueOf("FORBID ** /"); //$NON-NLS-1$

    /**
     * Permit set that allows all actions on all resources.
     */
    public static final Permit ALLOW_ALL = Permit.valueOf("ALLOW ** /"); //$NON-NLS-1$

    /** Shortcut for {@link Level#ALLOW} */
    public static final Level ALLOW = Level.ALLOW;

    /** Shortcut for {@link Level#FORBID} */
    public static final Level FORBID = Level.FORBID;


    private String action = ALL_ACTIONS;
    private String path = ROOT;
    private int level = 0;

    private transient String[] segments;

    /**
     * Creates a default permit equivalent to <tt>"FORBID ** /"</tt>.
     */
    public Permit() {
    }

    /**
     * Creates a permit.
     *
     * @param level  the permission level, i.e. {@link Level#FORBID} or {@link Level#ALLOW}.
     * @param action  the action, e.g. <tt>"GET"</tt> (case is ignored).
     * @param path the path, e.g. <tt>"/projects/*"</tt> (the leading '/' is optional).
     */
    public Permit(Level level, String action, String path) {
        setLevel(level.intValue());
        setAction(action);
        setPath(path);
    }

    /**
     * Creates a permit.
     *
     * @param level  the permission level, i.e. a positive number or 0. Negative
     * levels are treated as 0.
     * @param action  the action, e.g. <tt>"GET"</tt> (case is ignored).
     * @param path the path, e.g. <tt>"/projects/*"</tt> (the leading '/' is optional).
     */
    public Permit(int level, String action, String path) {
        setLevel(level);
        setAction(action);
        setPath(path);
    }

    /**
     * Creates a permit.
     *
     * @param level  the permission level, i.e. a positive number or 0. Negative
     * levels are treated as 0.
     * @param action  the action, e.g. <tt>"GET"</tt> (case is ignored).
     * @param segments  the segments of the permit's path
     */
    public Permit(int level, String action, String... segments) {
        setLevel(level);
        setAction(action);
        setSegments(segments);
    }

    /**
     * Creates a permit from a given string.
     *
     * @param permit  a permissions of the form <tt>"ALLOW/FORBID action path"</tt>,
     * for example <tt>"ALLOW GET /projects/**"</tt>, or <tt>"number action path"</tt>,
     * for example <tt>"1 GET /projects/**"</tt> or <tt>"+2 GET /projects/**"</tt>.
     */
    public static Permit valueOf(String s) {
        String[] parts = StringUtils.split(s);
        if (parts.length != 3) {
            throw new IllegalArgumentException("Expected format is 'ALLOW/FORBID action path'");
        }
        int level = parseLevel(parts[0]);
        return new Permit(level, parts[1], parts[2]);
    }

    /**
     * Parses the given string and returns the corresponding level.
     *
     * @param s  the string to parse, which must be either <code>"ALLOW"</code>,
     * or <code>"FORBID"</code> (case-insensitive), or an integer number.
     * If the given string is blank or <code>null</code>, <code>"FORBID"</code>
     * is assumed.
     *
     * @return  the level corresponding to the given string.
     *
     * @throws NumberFormatException  if the given string is neither <code>"ALLOW"</code>,
     * nor <code>"FORBID"</code>, nor an integer number.
     */
    public static int parseLevel(String s) {
        int level;
        if (StringUtils.isBlank(s)) {
            return Level.FORBID.intValue();
        }
        if (Level.ALLOW.name().equalsIgnoreCase(s)) {
            level = Level.ALLOW.intValue();
        } else if (Level.FORBID.name().equalsIgnoreCase(s)) {
            level = Level.FORBID.intValue();
        } else {
            if (s.startsWith("+")) { //$NON-NLS-1$
                s = s.substring(1);
            }
            level = Integer.parseInt(s);
        }
        return level;
    }

    /**
     * Returns the permit's action.
     */
    public String getAction() {
        return action;
    }

    /**
     * Sets the permit's action.
     *
     * @param action  the action to set, or <code>null</code>, which is treated equivalent to
     * <code>setAction(ALL_ACTIONS)</code>.
     */
    public void setAction(String action) {
        this.action = StringUtils.isNotBlank(action) ? action.toUpperCase(Locale.ENGLISH) : ALL_ACTIONS;
    }

    /**
     * Returns the permit's path.
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the permit's path.
     *
     * @param path  the path to set, or <code>null</code>, which is treated equivalent to
     * <code>setPath(ROOT)</code>.
     */
    public void setPath(String path) {
        if (StringUtils.isBlank(path)) {
            setRootPath();
        } else {
            this.segments = split(path);
            this.path = join(segments);
        }
    }

    /**
     * Returns the permit's level.
     */
    public int getLevel() {
        return level;
    }

    /**
     * Sets the permit's level.
     *
     * @param level  the level to set. Negative arguments are treated equivalent to
     * <code>setLevel(0)</code>.
     */
    public void setLevel(int level) {
        this.level = level < 0? 0 : level;
    }

    /**
     * Sets the permit's level.
     *
     * @param level  the level to set, or <code>null</code>, which is treated equivalent to
     * <code>setPath(Level.FORBID)</code>.
     */
    public void setLevel(Level level) {
       setLevel(level != null? level.intValue() : Level.FORBID.intValue());
    }

    /**
     * Returns the permit's path split up into its segments.
     */
    public String[] getSegments() {
        if (segments == null) {
            segments = split(path);
            path = join(segments);
        }
        return segments;
    }

    /**
     * Composes the permit's path from a given list of segments
     *
     * @param segments  the path segments. Empty lists and <code>null</code> are treated
     * equivalent to <code>setPath(ROOT)</code>.
     */
    public void setSegments(String...segments) {
        if (segments == null || segments.length == 0) {
            setRootPath();
        } else {
            this.segments = new String[segments.length];
            for (int i = 0; i< segments.length; ++i) {
                this.segments[i] = segments[i].trim();
            }
            this.path = join(segments);
        }
    }

    /**
     * Checks whether any of the given permits matches the requested permit.
     *
     * @param permits  the permits to search for a match.
     * @param requestedPermit  requested permit.
     *
     * @return <code>true</code>, if any of the given permits
     * matches the requested permit.
     */
    public static boolean match(PermitSet permits, Permit requestedPermit) {
        return match(permits, requestedPermit.getLevel(), requestedPermit.getAction(),
                requestedPermit.getSegments());
    }

    /**
     * Checks whether any of the given permits matches the requested permit.
     *
     * @param permits  the permits to search for a match.
     * @param requestedLevel  requested permit level.
     * @param requestedAction  requested action.
     * @param requestedSegments   the requested resource path. Either a  complete resource
     * path (with forward slashes (/) as separators), or a list of path
     * segments (without slashes). If the argument is <code>null</code> or an
     * empty array, always <code>false</code> is returned.
     *
     * @return <code>true</code>, if any of the given permits
     * matches the requested permit.
     */
    public static boolean match(PermitSet permits,
            int requestedLevel, String requestedAction, String... requestedSegments) {
        if (requestedSegments.length == 1) {
            requestedSegments = split(requestedSegments[0]);
        }
        for (Permit permit: permits) {
            // if permit's action is neither ** nor the same
            // as the requested action, then the permit is irrelevant
            if (!matchActions(permit.getAction(), requestedAction)) {
                continue;
            }
            // if requested path is shorter than permit's path,
            // then the permit applies to an inner resource and
            // is irrelevant
            String[] segments = permit.getSegments();
            if (segments.length > requestedSegments.length) {
                continue;
            }
            // if permit's path is not a subset of requested path
            // then the permit is irrelevant
            if (!matchSegments(segments, requestedSegments)) {
                continue;
            }
            // we have found a matching action and path,
            // so finally compare the levels: if permit's
            // level is above or equal requested level,
            // we have a match!
            return permit.getLevel() >= requestedLevel;
        }
        return false;
    }

    private static boolean matchActions(String action, String requestedAction) {
        return Permit.ALL_ACTIONS.equals(action) || action.equals(requestedAction);
    }

    private static boolean matchSegments(String[] segments, String[] requestedSegments) {
        for (int i = 0; i < segments.length; ++i) {
            // if permit's segment is ** then it matches any requested segment,
            // otherwise test corresponding segments for equality
            if (!Permit.WILDCARD.equals(segments[i]) && !segments[i].equals(requestedSegments[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compares permits according to the following rules:
     * The most "concrete" permits should be first in a sorted collection, i.e.
     * <ul>
     * <li>paths with more segments are lower than paths with less segments (e.g. /projects/foobar &lt; /projects)</li>
     * <li>path with less wildcards are lower than paths with more wildcards (e.g. /projects/foobar &lt; /projects/**)</li>
     * <li>permits with same path should be sorted alphnanumerically according to their actions (GET &lt; PUT)</li>
     * <li>concrete actions are lower than wildcard actions (GET, PUT... &lt; **)
     * <li>permits with same paths and actions are equal, regardless of their levels
     * </ul>
     */
    @Override
    public int compareTo(Permit o) {
        int result = compareSegments(getSegments(), o.getSegments());
        if (result == 0) {
            result = compareActions(action, o.action);
            if (result == 0) {
                compareWildcardActions(action, o.action);
            }
        }
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((action == null) ? 0 : action.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return compareTo((Permit)obj) == 0;
    }

    @Override
    public String toString() {
        return toString(action, path, level);
    }

    public static String toString(String action, String path, Level level) {
        return toString(action, path, level.intValue());
    }

    public static String toString(String action, String path, int level) {
        StringBuilder sb = new StringBuilder();
        sb.append(action).append(' ').append(path).append(' ');
        switch (level) {
        case 0:
            sb.append(Level.FORBID);
            break;
        case 1:
            sb.append(Level.ALLOW);
            break;
        default:
            sb.append(level);
        }
        return sb.toString();
    }

    private static String[] split(String path) {
        StrTokenizer tokenizer = new StrTokenizer();
        tokenizer.setDelimiterChar('/');
        tokenizer.setTrimmerMatcher(StrMatcher.trimMatcher());
        tokenizer.reset(path);
        return tokenizer.getTokenArray();
    }

    private static String join(String...segments) {
        return "/" + StringUtils.join(segments, '/'); //$NON-NLS-1$
    }

    private int compareSegments(String[] left, String[] right) {
        int result = - Integer.signum(left.length - right.length);
        if (result == 0) {
            int i = 0;
            while (result == 0 && i < left.length && i < right.length) {
                result = compareSegments(left[i], right[i]);
                i++;
            }
            if (result == 0) {
                result = i < left.length? -1 : (i < right.length? 1 : 0);
            }
        }
        return result;
    }

    private int compareSegments(String s1, String s2) {
        int result = 0;
        boolean leftWildcard = WILDCARD.equals(s1);
        boolean rightWildcard = WILDCARD.equals(s2);
        if (leftWildcard) {
            result = rightWildcard ? 0 : 1;
        } else {
            result = rightWildcard ? -1 : s1.compareTo(s2);
        }
        return result;
    }

    private int compareActions(String action1, String action2) {
        int result = 0;
        boolean leftWildcard = WILDCARD.equals(action1);
        boolean rightWildcard = WILDCARD.equals(action2);
        if (leftWildcard) {
            result = rightWildcard ? 0 : 1;
        } else {
            result = rightWildcard ? -1 : action1.compareTo(action2);
        }
        return result;
    }

    private int compareWildcardActions(String action1, String action2) {
        int result = 0;
        boolean leftWildcard = WILDCARD.equals(action1);
        boolean rightWildcard = WILDCARD.equals(action2);
        if (leftWildcard) {
            result = rightWildcard ? 0 : 1;
        } else {
            result = rightWildcard ? -1 : 0;
        }
        return result;
    }

    private void setRootPath() {
        this.segments = null;
        this.path = ROOT;
    }
}
