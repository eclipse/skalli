<%--
    Copyright (c) 2010, 2011 SAP AG and others.
    All rights reserved. This program and the accompanying materials
    are made available under the terms of the Eclipse Public License v1.0
    which accompanies this distribution, and is available at
    http://www.eclipse.org/legal/epl-v10.html

    Contributors:
        SAP AG - initial API and implementation
 --%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://www.eclipse.org/skalli/taglib" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="org.eclipse.skalli.view.component.TagCloud" %>
<%@ page import="org.eclipse.skalli.view.Consts" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Welcome to ${html:escapeHtml(pagetitle)}</title>
<style type="text/css">
@import "<%=Consts.JSP_STYLE%>";
</style>
<script type="text/javascript">
function focusSearch()
{
    document.getElementById("query").focus();
}
</script>
<link rel="search" type="application/opensearchdescription+xml" title="${html:escapeHtml(searchPluginTitle)}" href="/search-plugin.xml">
</head>
<body class="welcomepage" onload="focusSearch()">

<jsp:include page="<%=Consts.JSP_HEADER%>" flush="true" />

<%
    int viewMax = 50;
    TagCloud tagCloud = new TagCloud(viewMax);
%>

<div class="searcharea">
    <center>
    <img src="/VAADIN/themes/simple/images/logo_large.png" alt="Logo" >
    <div class="search-section">
        <form method="get" id="searchform" action="<%=Consts.URL_PROJECTS%>">
            <input type="text" value="" name="query" id="query" class="searchfield"/>
            <input type="submit" value="Search" id="searchsubmit" class="searchsubmit"/>
        </form>
    </div>
    <div class="searchsyntax-link"><a href="http://lucene.apache.org/java/3_0_2/queryparsersyntax.html" target="_blank">Extended Query Syntax</a></div>
    <div class="link-section">
        <c:if test="${newsConfig != null}">
          <a id="linkNews" href="${newsConfig.url}">What's new?</a>
        </c:if>
        <a id="linkAllProjects" href="<%=Consts.URL_ALLPROJECTS%>">All Projects</a>
        <c:if test="${user != null}">
            <a id="linkMyProjects" href="<%=Consts.URL_MYPROJECTS%>">My Projects</a>
            <a id="linkMyFavorites" href="<%=Consts.URL_MYFAVORITES%>">My Favorites</a>
            <a id="linkCreateProject" href="<%=Consts.URL_CREATEPROJECT%>">Create Project</a>
        </c:if>
    </div>
    </center>
</div>

<center>
<% if (tagCloud != null) { %>
    <div class="tagcloud">
        <div class="tagcloudheader">Most Popular Tags</div>
        <div class="tagcloudcontent">
        <%= tagCloud.doLayout() %>
        </div>
        <div class="tagcloudfooter"><a href='<%= Consts.URL_TAGCLOUD %>'>Show All Tags</a></div>
    </div>
<% } %>
<c:if test="${newsConfig != null && newsConfig.messageDefined}">
    <div class="messages-section">
      <c:if test="${newsConfig.alert != null}">
          <div class="alert-section">
              <span class="alert-msg">${html:clean(newsConfig.alert)}</span>
          </div>
      </c:if>
      <c:if test="${newsConfig.info != null}">
          <div class="info-section">
              <span class="info-msg">${html:clean(newsConfig.info)}</span>
          </div>
      </c:if>
    </div>
</c:if>
</center>
</body>
</html>
