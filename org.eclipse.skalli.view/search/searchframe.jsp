<%--
    Copyright (c) 2010, 2011 SAP AG and others.
    All rights reserved. This program and the accompanying materials
    are made available under the terms of the Eclipse Public License v1.0
    which accompanies this distribution, and is available at
    http://www.eclipse.org/legal/epl-v10.html

    Contributors:
        SAP AG - initial API and implementation
 --%>

<%@ taglib prefix="html" uri="http://www.eclipse.org/skalli/taglib" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ page import="org.eclipse.skalli.view.Consts" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>${html:escapeHtml(pagetitle)} - Embedded Search</title>
<style type="text/css">
body {
    color: #b8bbbe;
    font-family: arial, helvetica, verdana, sans-serif;
    font-size: 12px;
    color: #000000;
    margin: 0px;
    padding: 0px;
}

a img {
    border:0px none;
}
.searchframe {
    width: 655px;
    height: 38px;
}

.searchframe-left {
    float: left;
    width: 150px
}

.searchframe-right {
    float: left;
    clear: none;
    width: 500px;
    padding-left: 5px;
}

.searchframe-field {
    width: 30em;
    margin-top: 3px;
    border-width: 1px;
    border-style: solid;
    border-color: #999999;
}

.searchframe-field:hover {
    width: 30em;
    margin-top: 3px;
    border-width: 1px;
    border-style: solid;
    border-color: #1B699F;
}

.searchframe-field:focus {
    width: 30em;
    margin-top: 3px;
    border-width: 1px;
    border-style: solid;
    border-color: #1B699F;
}

.searchframe-submit {
    margin-top: 3px;
    margin-left: 3px;
    border-width: 1px;
    border-style: solid;
    border-color: #999999;
}

.searchframe-submit:hover {
    margin-top: 3px;
    margin-left: 3px;
    border-width: 1px;
    border-style: solid;
    border-color: #1B699F;
}
</style>
<META HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE">
</head>
<body>
    <div class="searchframe">
        <div class="searchframe-left">
            <a href="/" target="_top">
                <img src="/VAADIN/themes/simple/images/logo_medium.png" alt="Logo" />
            </a>
        </div>
        <div class="searchframe-right">
            <form method="get" id="searchform" action="<%=Consts.URL_PROJECTS%>" target="_top">
                <input type="text" value="" name="query" class="searchframe-field"/>
                <input type="submit" value="Search" class="searchframe-submit"/>
            </form>
        </div>
    </div>
</body>
</html>
