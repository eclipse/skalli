<%--
    Copyright (c) 2010-2014 SAP AG and others.
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
<%@ page import="org.eclipse.skalli.view.Consts" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Creating Git Repository Failed - ${html:escapeHtml(pagetitle)}</title>
<style type="text/css">
@import "<%=Consts.JSP_STYLE%>";

.errormessage {
    font-style: italic;
    color: #9E0000;
}
</style>
</head>
<body>

    <%-- header area --%>
    <jsp:include page="<%=Consts.JSP_HEADER%>" flush="true" />
    <jsp:include page="<%=Consts.JSP_HEADER_SEARCH%>" flush="true" />

    <%-- navigation menu on left side --%>
    <jsp:include page="<%=Consts.JSP_NAVIGATIONBAR%>" flush="true" />

    <%-- git/gerrit error --%>
    <div class="rightarea">
        <h2>Creation of Git/Gerrit repository not possible!</h2>
        <c:choose>
            <c:when test="${errormessage == 'noConfiguration'}">
                <div class='errormessage'>Gerrit access is not configured.</div>
                <br />
                <c:if test="${feedbackConfig != null}">
                    <a title="${feedbackConfig.displayName}" href="${feedbackConfig.url}">Send Bug Report</a>
                </c:if>
            </c:when>
            <c:when test="${errormessage == 'noParent'}">
                <div id="errorMessageNoParent" class='errormessage'>Root projects are not allowed to create a Git
                    repository. Please set a parent project first.</div>
                <br />
                <a id="errorEditProjectLink"
                    href="<%=Consts.URL_PROJECTS + "/" + request.getAttribute(Consts.ATTRIBUTE_PROJECTID) + "?"
                            + Consts.PARAM_ACTION + "=" + Consts.PARAM_VALUE_EDIT%>"
                    target="_top"> Edit this project</a>
            </c:when>
            <c:otherwise>
                An unexpected error occurred.
                <c:if test="${feedbackConfig != null}">
                    <a title="${feedbackConfig.displayName}" href="${feedbackConfig.url}">Send Bug Report</a>
                </c:if>
            </c:otherwise>
        </c:choose>
    </div>
</body>
</html>
