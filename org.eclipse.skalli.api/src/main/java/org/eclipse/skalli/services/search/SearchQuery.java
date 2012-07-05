package org.eclipse.skalli.services.search;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

public class SearchQuery {

    public static final String PARAM_QUERY = "query"; //$NON-NLS-1$
    public static final String PARAM_TAG = "tag"; //$NON-NLS-1$
    public static final String PARAM_USER = "user"; //$NON-NLS-1$
    public static final String PARAM_PROPERTY = "property"; //$NON-NLS-1$
    public static final String PARAM_PATTERN = "pattern"; //$NON-NLS-1$
    public static final String PARAM_EXTENSIONS = "extensions"; //$NON-NLS-1$
    public static final String PARAM_START = "start"; //$NON-NLS-1$
    public static final String PARAM_COUNT = "count"; //$NON-NLS-1$

    public static final String[] PARAMS = new String[] {
        PARAM_QUERY, PARAM_TAG, PARAM_USER, PARAM_PROPERTY, PARAM_PATTERN, PARAM_EXTENSIONS, PARAM_START, PARAM_COUNT
    };

    public static final String PARAM_LIST_SEPARATOR = ","; //$NON-NLS-1$

    private String query;
    private String tag;
    private String user;

    private String[] extensions;

    private String shortName;
    private String propertyName;
    private boolean negate;
    private Pattern pattern;

    private PagingInfo pagingInfo;

    public SearchQuery() {
    }

    public SearchQuery(Map<String, String> params) throws QueryParseException {
        setQuery(params.get(PARAM_QUERY));
        setTag(params.get(PARAM_TAG));
        setUser(params.get(PARAM_USER));

        String property = params.get(PARAM_PROPERTY);
        if (StringUtils.isNotBlank(property)) {
            if (property.startsWith("!")) { //$NON-NLS-1$
                setNegate(true);
                property = property.substring(1);
            }
            String[] split = property.split("\\."); //$NON-NLS-1$
            if (split.length < 1 || split.length > 2) {
                throw new QueryParseException("Property should conform to the pattern <extension.propertyName>");
            }
            setShortName(split.length == 1 ? "project" : split[0]); //$NON-NLS-1$
            setPropertyName(split.length == 1 ? split[0] : split[1]);

            String patternArg = params.get(PARAM_PATTERN);
            if (StringUtils.isBlank(patternArg)) {
                //return all projects that have the given property, as no pattern was provided
                patternArg = ".+"; //$NON-NLS-1$
            }
            try {
                setPattern(Pattern.compile(patternArg));
            } catch (PatternSyntaxException e) {
                throw new QueryParseException("Pattern has a syntax error", e);
            }
        }

        int start = NumberUtils.toInt(params.get(PARAM_START), 0);
        int count = NumberUtils.toInt(params.get(PARAM_COUNT), Integer.MAX_VALUE);
        setPagingInfo(start, count);

        String extensionParam = params.get(PARAM_EXTENSIONS);
        if (extensionParam != null) {
            setExtensions(extensionParam.split(PARAM_LIST_SEPARATOR));
        }
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

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
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
}
