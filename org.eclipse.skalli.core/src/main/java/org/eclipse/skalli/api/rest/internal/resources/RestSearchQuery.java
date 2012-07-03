package org.eclipse.skalli.api.rest.internal.resources;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.BooleanUtils;
import org.eclipse.skalli.services.search.QueryParseException;
import org.eclipse.skalli.services.search.SearchQuery;
import org.restlet.data.Form;

public class RestSearchQuery extends SearchQuery {

    public static final String PARAM_PERSIST = "persist"; //$NON-NLS-1$

    private boolean doPersist;

    public RestSearchQuery(Form form) throws QueryParseException {
        super(asMap(form));
        doPersist = BooleanUtils.toBoolean(form.getFirstValue(PARAM_PERSIST));
    }

    public boolean doPersist() {
        return doPersist;
    }

    private static Map<String, String> asMap(Form form) {
        HashMap<String, String> params = new HashMap<String, String>();
        for (String key: PARAMS) {
            params.put(key, form.getFirstValue(key));
        }
        return params;
    }
}