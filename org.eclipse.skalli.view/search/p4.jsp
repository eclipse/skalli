<%--
    Copyright (c) 2010, 2011 SAP AG and others.
    All rights reserved. This program and the accompanying materials
    are made available under the terms of the Eclipse Public License v1.0
    which accompanies this distribution, and is available at
    http://www.eclipse.org/legal/epl-v10.html

    Contributors:
        SAP AG - initial API and implementation
 --%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="html" uri="http://www.eclipse.org/skalli/taglib" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page import="org.eclipse.skalli.view.internal.filter.ext.JiraFilter"%>
<%@ page import="org.eclipse.skalli.view.Consts"%>
<%@ page import="org.eclipse.skalli.view.PatternUtil"%>
<%@ page import="org.eclipse.skalli.model.Project"%>
<%@ page errorPage="/error"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<title>Request Perforce Project - ${html:escapeHtml(pagetitle)}</title>
<style type="text/css">
@import "<%=Consts.JSP_STYLE%>";

.leftMargin {
    margin-left: 40px;
}

.important {
    color: red;
}
</style>
</head>
<body>

    <%-- header area --%>
    <jsp:include page="<%= Consts.JSP_HEADER %>" flush="true" />
    <jsp:include page="<%= Consts.JSP_HEADER_SEARCH %>" flush="true" />

    <%-- navigation menu on left side --%>
    <jsp:include page="<%=Consts.JSP_NAVIGATIONBAR%>" flush="true" />

    <%-- P4 Project --%>
    <div class="projectarearight" style="max-width: 600px;">
        <h3>
            <img src="/img/p4_logo.png" alt="Perforce Logo"
                style="width: 32px; height: 32px; margin-right: 5px; vertical-align: middle;" />
            Request Perforce Project
        </h3>
        <p>A Perforce project can be requested via an IT/IBC ticket.
            Follow the link below to create a new ticket in a separate
            browser window:</p>
        <%
        Project project = (Project) request.getAttribute(Consts.ATTRIBUTE_PROJECT);
        String projectName = PatternUtil.adjustProjectName(project.getProjectId());
        %>

        <p class="leftMargin">
            <a
                href="https://ifp.wdf.sap.corp/itform/zitform.htm?formname=PROD_REQ_PROJECT_MAVEN&PROJECTNAME=<%=projectName%>"
                target="_blank">Request initial P4 project</a>
        </p>

        <ul>
            <li>You should verify that a proposed project name is free, using <a
                href="https://pie.wdf.sap.corp/webdynpro/dispatcher/sap.com/test~p4ms~wd_app/PerforceMain"
                target="_blank">P4MS</a>. Use P4 user management self-service after P4 repo creation to maintain project committers.
            </li>
            <li>You should use the default p4 server but you may
                change it if appropriate. Do not use Perforce1666 as
                this sandbox server is not supported by the LeanDI
                infrastructure.</li>
            <li>You should use the default depot but you may change
                it if appropriate.</li>
            <li>You can request dev and cons or only dev codeline.</li>
        </ul>
        <p class="important">
            Note: The new Perforce project will <strong>not</strong>
            automatically be added to your SCM locations, but needs to
            be added manually once it has been created by the Production
            team.
        </p>
        <p>
            <a href="/projects/${project.projectId}">Back to project</a>
        </p>
    </div>
</body>
</html>
