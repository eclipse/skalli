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
package org.eclipse.skalli.services.search;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.lang.model.SourceVersion;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.text.StrMatcher;
import org.apache.commons.lang.text.StrTokenizer;
import org.eclipse.skalli.model.Expression;

public class SearchQuery {

    public static final String PARAM_QUERY = "query"; //$NON-NLS-1$
    public static final String PARAM_TAG = "tag"; //$NON-NLS-1$
    public static final String PARAM_USER = "user"; //$NON-NLS-1$
    public static final String PARAM_PROPERTY = "property"; //$NON-NLS-1$
    public static final String PARAM_PATTERN = "pattern"; //$NON-NLS-1$
    public static final String PARAM_IGNORE_CASE = "ignoreCase"; //$NON-NLS-1$
    public static final String PARAM_EXTENSIONS = "extensions"; //$NON-NLS-1$
    public static final String PARAM_START = "start"; //$NON-NLS-1$
    public static final String PARAM_COUNT = "count"; //$NON-NLS-1$
    public static final String PARAM_ORDER_BY = "orderBy"; //$NON-NLS-1$

    public static final String DEFAULT_SHORTNAME = "project"; //$NON-NLS-1$
    public static final String PROJECT_PREFIX = DEFAULT_SHORTNAME + "."; //$NON-NLS-1$

    public static final String[] PARAMS = new String[] {
        PARAM_QUERY, PARAM_TAG, PARAM_USER, PARAM_PROPERTY, PARAM_PATTERN, PARAM_IGNORE_CASE,
        PARAM_EXTENSIONS, PARAM_START, PARAM_COUNT
    };

    public static final String PARAM_LIST_SEPARATOR = ","; //$NON-NLS-1$

    private String query;
    private String tag;
    private String user;

    private String[] extensions;

    private String property;
    private String shortName;
    private boolean negate;
    private Pattern pattern;
    private boolean isExtension;

    private StrTokenizer tokenizer = getTokenizer();
    private Expression[] expressions;

    private PagingInfo pagingInfo;

    private SortOrder orderBy;

    public SearchQuery() {
    }

    public SearchQuery(Map<String, String> params) throws QueryParseException {
        setQuery(params.get(PARAM_QUERY));
        setTag(params.get(PARAM_TAG));
        setUser(params.get(PARAM_USER));
        setProperty(params.get(PARAM_PROPERTY));
        setOrderBy(params.get(PARAM_ORDER_BY));

        String patternArg = params.get(PARAM_PATTERN);
        if (StringUtils.isBlank(patternArg)) {
            //return all projects that have the given property, as no pattern was provided
            patternArg = ".+"; //$NON-NLS-1$
        }
        boolean ignoreCase = params.containsKey(PARAM_IGNORE_CASE);
        setPattern(patternArg, ignoreCase);

        int start = NumberUtils.toInt(params.get(PARAM_START), 0);
        int count = NumberUtils.toInt(params.get(PARAM_COUNT), Integer.MAX_VALUE);
        setPagingInfo(start, count);

        String extensionParam = params.get(PARAM_EXTENSIONS);
        if (extensionParam != null) {
            setExtensions(StringUtils.split(extensionParam, PARAM_LIST_SEPARATOR));
        }
    }

    public boolean isQueryAll() {
        return "*".equals(query) || //$NON-NLS-1$
                StringUtils.isBlank(query) && StringUtils.isBlank(user) && StringUtils.isBlank(tag);
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) throws QueryParseException {
        if (StringUtils.isNotBlank(property)) {
            if (property.startsWith("!")) { //$NON-NLS-1$
                setNegate(true);
                property = property.substring(1);
            }
            String[] parts = split(property);
            if (parts.length == 1 || StringUtils.isBlank(parts[0])) {
                shortName = DEFAULT_SHORTNAME;
            } else {
                shortName = parts[0].trim();
                isExtension = true;
            }
            int first = parts.length == 1? 0 : 1;
            expressions = new Expression[parts.length - first];
            for (int i = first, j = 0; i < parts.length; ++i, ++j) {
                expressions[j] = asExpression(parts[i]);
            }
        }
        this.property = property;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String[] getExtensions() {
        return extensions;
    }

    public void setExtensions(String[] extensions) {
        this.extensions = extensions;
    }

    public String[] addExtension(String shortName) {
        extensions = (String[]) ArrayUtils.add(extensions, shortName);
        return extensions;
    }

    public boolean hasExtension(String shortName) {
        return ArrayUtils.contains(extensions, shortName);
    }

    public boolean isExtension() {
        return isExtension;
    }

    public String getShortName() {
        return shortName;
    }

    public String getPropertyName() {
        if (expressions == null || expressions.length != 1) {
            return null;
        }
        Expression first = expressions[0];
        if (first.getArguments().length > 0) {
            return null;
        }
        return first.getName();
    }

    public Expression[] getExpressions() {
        return expressions;
    }

    public boolean isNegate() {
        return negate;
    }

    public void setNegate(boolean negate) {
        this.negate = negate;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

   public  void setPattern(String pattern, boolean ignoreCase) throws QueryParseException {
        if (StringUtils.isNotBlank(pattern)) {
            try {
                int flags = Pattern.DOTALL;
                if (ignoreCase) {
                    flags = flags | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
                }
                setPattern(Pattern.compile(pattern, flags));
            } catch (PatternSyntaxException e) {
                throw new QueryParseException("Pattern has a syntax error", e);
            }
        }
    }

    public PagingInfo getPagingInfo() {
        return pagingInfo;
    }

    public void setPagingInfo(PagingInfo pagingInfo) {
        this.pagingInfo = pagingInfo;
    }

    public void setPagingInfo(int start, int count) {
        this.pagingInfo = new PagingInfo(start, count);
    }

    public int getStart() {
        return pagingInfo != null? pagingInfo.getStart() : 0;
    }

    public int getCount() {
        return pagingInfo != null? pagingInfo.getCount() : Integer.MAX_VALUE;
    }

    public SortOrder getOrderBy() {
        return orderBy != null? orderBy : SortOrder.NONE;
    }

    public void setOrderBy(SortOrder orderBy) {
        this.orderBy = orderBy;
    }

    public void setOrderBy(String orderBy) {
        if ("projectId".equalsIgnoreCase(orderBy)) { //$NON-NLS-1$
            setOrderBy(SortOrder.PROJECT_ID);
        } else if ("name".equalsIgnoreCase(orderBy)) { //$NON-NLS-1$
            setOrderBy(SortOrder.PROJECT_NAME);
        } else if ("uuid".equalsIgnoreCase(orderBy)) { //$NON-NLS-1$
            setOrderBy(SortOrder.UUID);
        } else {
            setOrderBy(SortOrder.NONE);
        }
    }

    StrTokenizer getTokenizer() {
        StrTokenizer tokenizer = new StrTokenizer("", StrMatcher.commaMatcher(), StrMatcher.quoteMatcher());
        tokenizer.setTrimmerMatcher(StrMatcher.trimMatcher());
        return tokenizer;
    }

    private String[] split(String property) throws QueryParseException {
        ArrayList<String> tokens = new ArrayList<String>();
        char[] chars = property.toCharArray();
        int last = chars.length - 1;
        char quoteChar = 0;
        boolean quoted = false;
        boolean bracketed = false;
        int nextTokenStart = 0;
        int nextTokenLength = 0;
        for (int i = 0; i <= last; ++i) {
            char c = chars[i];
            switch (c) {
            case '.':
                if (i == last) {
                    throw new QueryParseException("Property must not end with trailing dot:" + property);
                }
                if (!quoted && !bracketed) {
                    tokens.add(trimmed(chars, nextTokenStart, nextTokenLength));
                    nextTokenStart = i + 1;
                    nextTokenLength = 0;
                } else {
                    ++nextTokenLength;
                }
                break;
            case '"':
            case '\'':
                if (quoted && c == quoteChar) {
                    if (i == last || (i < last && chars[i+1] != quoteChar)) {
                        quoteChar = 0;
                        quoted = false;
                    }
                } else {
                    quoteChar = c;
                    quoted = true;
                }
                ++nextTokenLength;
                break;
            case '(':
                if (!bracketed && !quoted) {
                    bracketed = true;
                }
                ++nextTokenLength;
                break;
            case ')':
                if (bracketed && !quoted) {
                    bracketed = false;
                }
                ++nextTokenLength;
                break;
            default:
                ++nextTokenLength;
            }
        }
        if (nextTokenLength > 0) {
            tokens.add(trimmed(chars, nextTokenStart, nextTokenLength));
        }
        return tokens.toArray(new String[tokens.size()]);
    }

    private String trimmed(char[] chars, int off, int len) {
        if (len == 0) {
            return ""; //$NON-NLS-1$
        }
        int first = off;
        int last = off + len - 1;
        int count = len;
        while (first <= last && chars[first] <= ' ') { ++first; --count; }
        while (last >= first && chars[last] <= ' ') { --last; --count; }
        return new String(chars, first, count);
    }

    Expression asExpression(String s) throws QueryParseException {
        int n = s.indexOf('(');
        if (n < 0) {
            if (!SourceVersion.isIdentifier(s)) {
                throw new QueryParseException("Invalid property name :" + s);
            }
            return new Expression(s);
        }
        if (!s.endsWith(")")) { //$NON-NLS-1$
            throw new QueryParseException("Invalid property expression: " + s);
        }
        String name = s.substring(0, n);
        if (name.length() == 0) {
            throw new QueryParseException("Property expression specifies no property name :" + s);
        }
        if (!SourceVersion.isIdentifier(name)) {
            throw new QueryParseException("Invalid property name :" + s);
        }
        String argsList = s.substring(n+1, s.length()-1);
        if (StringUtils.isBlank(argsList)) {
            return new Expression(name);
        }
        if ("''".equals(argsList) || "\"\"".equals(argsList)) { //$NON-NLS-1$ //$NON-NLS-2$
            return new Expression(name, ""); //$NON-NLS-1$
        }
        tokenizer.reset(argsList);
        return new Expression(name, tokenizer.getTokenArray());
    }
}
