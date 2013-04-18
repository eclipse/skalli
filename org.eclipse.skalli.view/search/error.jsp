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
<%@ page import="org.eclipse.skalli.commons.FormatUtils" %>
<%@ page import="org.eclipse.skalli.view.Consts" %>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<%@ page import="org.slf4j.LoggerFactory" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Error</title>
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

<%-- Error --%>
<div class="projectarearight" >
<div class="error">

<c:if test="${exception != null}">
    <p style=" color: red;"> OOPS, an unexpected error occurred.</p>
    Please send us a bug report and include the following information in your feedback:<br>
    <ul>
    <li>The project that causes the problem.</li>
    <li>Step-by-step description how to reproduce the problem.</li>
    <li>The following error identifier:
    <%
        Exception exception = (Exception)request.getAttribute("exception");
        String timestamp = FormatUtils.formatUTCWithMillis(System.currentTimeMillis());
        String msg = request.getRequestURI() + ":" + timestamp + ":" + exception.getMessage();
        LoggerFactory.getLogger(Exception.class).error(msg, exception);
        out.print("<pre>");
        out.print(StringEscapeUtils.escapeHtml(msg));
        out.println("</pre>");
    %>
    </li></ul>
    <c:if test="${feedbackConfig != null }">
       <a title="${feedbackConfig.displayName}" href="${feedbackConfig.url}">Send Bug Report</a>
    </c:if>
</c:if>
</div>
</div>

</body>
</html>
