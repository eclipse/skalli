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
package org.eclipse.skalli.common;

/**
 * Collection of constants for URLs and SOLR searches.
 */
@SuppressWarnings("nls")
public class Consts {

    private Consts() {
    };

    public static final String PARAM_QUERY = "query";
    public static final String PARAM_START = "start";
    public static final String PARAM_COUNT = "count";
    public static final String PARAM_USER = "user";
    public static final String PARAM_TAG = "tag";
    public static final String PARAM_TYPE = "type";
    public static final String PARAM_ID = "id";
    public static final String PARAM_UUID = "uuid";
    public static final String PARAM_ACTION = "action";
    public static final String PARAM_VIEW = "view";
    public static final String PARAM_VALUE_EDIT = "edit";
    public static final String PARAM_VALUE_VIEW_HIERARCHY = "hierarchy";
    public static final String PARAM_VALUE_VALIDATE = "validate";
    public static final String PARAM_EXTENSIONS = "extensions";
    public static final String PARAM_LIST_SEPARATOR = ",";

    public static final String ATTRIBUTE_USERID = "userId";
    public static final String ATTRIBUTE_USER = "user";
    public static final String ATTRIBUTE_PROJECT = "project";
    public static final String ATTRIBUTE_PROJECTID = "projectId";
    public static final String ATTRIBUTE_PROJECTUUID = "projectUUID";
    public static final String ATTRIBUTE_PROJECTADMIN = "isProjectAdmin";
    public static final String ATTRIBUTE_SHOW_ISSUES = "showIssues";
    public static final String ATTRIBUTE_EDITMODE = "editmode";
    public static final String ATTRIBUTE_WINDOWNAME = "windowName";
    public static final String ATTRIBUTE_PROJECTTEMPLATE = "projectTemplate";
    public static final String ATTRIBUTE_FAVORITES = "favorites";
    public static final String ATTRIBUTE_PROJECTCONTEXTLINKS = "projectContextLinks";
    public static final String ATTRIBUTE_ISSUES = "issues";
    public static final String ATTRIBUTE_MAX_SEVERITY = "maxSeverity";
    public static final String ATTRIBUTE_FEEDBACKCONFIG = "feedbackConfig";
    public static final String ATTRIBUTE_TOPLINKSCONFIG = "toplinksConfig";
    public static final String ATTRIBUTE_NEWSCONFIG = "newsConfig";
    public static final String ATTRIBUTE_BRANDINGCONFIG = "brandingConfig";
    public static final String ATTRIBUTE_USERDETAILSCONFIG = "userDetailsConfig";
    public static final String ATTRIBUTE_PAGETITLE = "pagetitle";
    public static final String ATTRIBUTE_EXCEPTION = "exception";
    public static final String ATTRIBUTE_QUERY = "query";
    public static final String ATTRIBUTE_TAGQUERY = "tagquery";
    public static final String ATTRIBUTE_USERQUERY = "userquery";

    public static final String URL_WELCOME = "/";
    public static final String URL_FAVICON = "/favicon.ico";
    public static final String URL_SEARCH_PLUGIN = "/search-plugin.xml";
    public static final String URL_REST_API = "/api";
    public static final String URL_NEWS = "/news";
    public static final String URL_TAGCLOUD = "/tags";
    public static final String URL_PROJECTS = "/projects";
    public static final String URL_HIERARCHY = "/hierarchy";
    public static final String URL_ALLPROJECTS = URL_PROJECTS + "?" + Consts.PARAM_VIEW + "="
            + PARAM_VALUE_VIEW_HIERARCHY;
    public static final String URL_SUBPROJECTS = "/subprojects";
    public static final String URL_RELATEDPROJECTS = "/relatedprojects";
    public static final String URL_MYPROJECTS = "/myprojects";
    public static final String URL_MYFAVORITES = "/myfavorites";
    public static final String URL_REINDEX = "/reindex";
    public static final String URL_CREATEPROJECT = "/create";
    public static final String URL_VAADIN_PROJECTS = "/vprojects/";
    public static final String URL_ERROR = "/error";

    public static final String URL_PROJECTS_QUERY = URL_PROJECTS + "?" + PARAM_QUERY + "=";
    public static final String URL_PROJECTS_TAG = URL_PROJECTS + "?" + PARAM_TAG + "=";
    public static final String URL_PROJECTS_USER = URL_PROJECTS + "?" + PARAM_USER + "=";

    public static final String JSP_WELCOME = "/search/welcome.jsp";
    protected static final String JSP_TAGCLOUD = "/search/tagcloud.jsp";
    protected static final String JSP_MYPROJECTS = "/search/myprojects.jsp";
    public static final String JSP_HEADER = "/search/includes/header.jsp";
    public static final String JSP_SEARCHRESULT = "/search/searchresult.jsp";
    public static final String JSP_HEADER_SEARCH = "/search/includes/searchheader.jsp";
    public static final String JSP_NAVIGATIONBAR = "/search/includes/navigationbar.jsp";
    public static final String JSP_ISSUES = "/search/includes/issues.jsp";
    public static final String JSP_STYLE = "/search/style.css";
    public static final String TOGGLE_JS = "/js/toggle.js";

    public static final String FILE_FAVICON = "/search/favicon.ico";
    public static final String FILE_SEARCH_PLUGIN = "/search/search-plugin.xml";

    public static final String PROPERTIES_RESOURCE = "/skalli.properties";//$NON-NLS-1$
    public static final String PROPERTY_WORKDIR = "workdir"; //$NON-NLS-1$
    public static final String PROPERTY_STORAGE_SERVICE = "skalli.storageService";

    public static final String DEFAULT_PAGETITLE = "Skalli";
}
