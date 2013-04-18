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
<%@ page import="java.net.URL" %>
<%@ page import="org.eclipse.skalli.view.Consts" %>
<%@ page errorPage="/error" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Add Project to JIRA - ${html:escapeHtml(pagetitle)}</title>
<style type="text/css">
@import "<%=Consts.JSP_STYLE%>";
</style>
</head>
<body>

<%-- do some checks first --%>
<%
  String projectId = request.getParameter(Consts.PARAM_ID);
  if (projectId == null) {
    Exception e = new Exception("project id is not set, the prevent this error please use url parameter '"
        + Consts.PARAM_ID + "'!");
    request.setAttribute("exception", e);
    throw e;
  }
%>

<%-- do some other stuff for rendering --%>
<%
  request.setAttribute("projectUrl", request.getRequestURL().toString().replaceFirst(request.getServletPath(), "")
      + Consts.URL_PROJECTS + "/" + projectId);
%>

<%-- header area --%>
<jsp:include page="<%=Consts.JSP_HEADER%>" flush="true" />
<jsp:include page="<%=Consts.JSP_HEADER_SEARCH%>" flush="true" />

<%-- navigation menu on left side --%>
<jsp:include page="<%=Consts.JSP_NAVIGATIONBAR%>" flush="true" />

<%-- JIRA Form --%>
<div class="projectarearight" style="max-width:600px;">
<h3>
Functionality not available!
</h3>
<br/>
The functionality you requested is not available because Development Information of this
project is inherited from parent project. If you want to enable this functionality, disable
or edit this section in the edit dialog.
<br/><br/>
<a href="${projectUrl}" target="_self">
Back to project</a>
</div>
</body>
</html>
