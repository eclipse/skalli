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
<title>Login to ${html:escapeHtml(pagetitle)}</title>
<style type="text/css">
@import "<%=Consts.JSP_STYLE%>";
</style>
</head>
<body class="welcomepage">
<%
    // if user is already authenticated, go to welcome page
    if (request.getUserPrincipal()!=null) {
        response.sendRedirect("/");
    }
%>

<jsp:include page="<%=Consts.JSP_HEADER%>" flush="true" />

<div class="searcharea">
    <center>
    <img src="/VAADIN/themes/simple/images/logo_large.png" alt="Logo">
    <div class="search-section">
    <form method="POST" action="j_security_check">
        <table>
            <tr>
                <td>Username:</td>
                <td><input type="text" name="j_username" /></td>
            </tr>
            <tr>
                <td>Password:</td>
                <td><input type="password" name="j_password" /></td>
            </tr>
            <tr>
                <td>&nbsp;</td>
                <td>
                    <input type="submit" name="ok" value="Login" />
                    <input type="button" onclick="javascript:history.back()" name="back" value="Cancel" />
                </td>
            </tr>
        </table>
    </form>
    </div>
    </center>
</div>
</body>
</html>
