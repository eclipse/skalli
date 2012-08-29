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
<%@ page import="java.util.List" %>
<%@ page import="java.util.Comparator" %>
<%@ page import="org.eclipse.skalli.model.User" %>
<%@ page import="org.eclipse.skalli.model.Project" %>
<%@ page import="org.eclipse.skalli.view.Consts" %>
<%@ page import="org.eclipse.skalli.services.user.LoginUtils" %>
<%@ page import="org.eclipse.skalli.services.group.GroupUtils" %>
<%@ page import="org.eclipse.skalli.services.project.ProjectService" %>
<%@ page import="org.eclipse.skalli.services.Services" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Deleted Projects - ${html:escapeHtml(pagetitle)}</title>
<style type="text/css">
@import "<%=Consts.JSP_STYLE%>";
</style>
</head>
<body>

<%-- header area --%>
<jsp:include page="<%= Consts.JSP_HEADER %>" flush="true" />
<jsp:include page="<%= Consts.JSP_HEADER_SEARCH %>" flush="true" />

<%-- navigation menu on left side --%>
<jsp:include page="<%=Consts.JSP_NAVIGATIONBAR%>" flush="true" />

<%-- deleted projects --%>
<%
    LoginUtils util = new LoginUtils(request);
    User user = util.getLoggedInUser();
    if (GroupUtils.isAdministrator(user)) {
        ProjectService projectService = Services.getRequiredService(ProjectService.class);
        List<Project> deletedProjects = projectService.getDeletedProjects(new Comparator<Project>() {
            public int compare(Project p1, Project p2) {
                return p1.getProjectId().compareTo(p2.getProjectId());
                }
            });
%>
        <div class="projectarearight">
            <h3>Deleted Projects</h3>
            <div>
                <ul class="deletedlist">
                <% for (Project deletedProject: deletedProjects) { %>
                    <li><a href="<%=Consts.URL_PROJECTS%>/<%=deletedProject.getUuid()%>"><%=deletedProject.getProjectId()%> (<%=deletedProject.getName()%>)</a></li>
                <% } %>
                </ul>
            </div>
        </div>
<%  } %>
</body>
</html>
