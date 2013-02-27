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
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ page import="org.eclipse.skalli.view.Consts" %>

<div class="navigationbar">
    <div style="font-weight: bold; padding-bottom: 2px">Project Portal</div>
    <c:if test="${newsConfig != null }">
        <a id="linkNews" href="<%=Consts.URL_NEWS%>" target="_top">What's New</a><br />
    </c:if>
    <a id="linkAllProjects" href="<%=Consts.URL_ALLPROJECTS%>" target="_top">All Projects</a><br />
    <a id="linkTagCloud" href="<%=Consts.URL_TAGCLOUD%>" target="_top">Tag Cloud</a><br />
    <br />
    <c:if test="${userId != null}">
      <div style="font-weight: bold; padding-bottom: 2px">User Links</div>
      <a id="linkMyProjects" href="<%=Consts.URL_MYPROJECTS%>" target="_top">My Projects</a><br />
      <a id="linkMyFavorites" href="<%=Consts.URL_MYFAVORITES%>" target="_top">My Favorites</a><br />
      <a id="linkCreateProject" href="<%=Consts.URL_CREATEPROJECT%>" target="_top">Create Project</a><br />
      <br />
    </c:if>
    <c:if test="${isProjectAdmin == true && editmode == false}">
        <div style="font-weight: bold; padding-bottom: 2px">This Project</div>
        <a href="<%=request.getRequestURI() + "?action=edit"%>" target="_top">Edit</a><br />
        <a href="<%=request.getRequestURI() + "/infoboxes?action=refresh"%>" target="_top">Refresh</a><br />
        <c:forEach var="link" items="${projectContextLinks}" >
            <%-- render the project context links --%>
            <c:set var="linkCaption" value="${link.caption}" />
            <c:set var="linkUri" value="${link.uri}" />
            <c:set var="linkId" value="${link.id}" />
            <a id="${linkId}" href="${linkUri}" target="_top">${html:clean(linkCaption)}</a><br />
        </c:forEach>
    </c:if>
</div>

