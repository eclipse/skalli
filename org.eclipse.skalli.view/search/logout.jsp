<%--
    Copyright (c) 2010-2014 SAP AG and others.
    All rights reserved. This program and the accompanying materials
    are made available under the terms of the Eclipse Public License v1.0
    which accompanies this distribution, and is available at
    http://www.eclipse.org/legal/epl-v10.html

    Contributors:
        SAP AG - initial API and implementation
 --%>

<%@ taglib prefix="html" uri="http://www.eclipse.org/skalli/taglib" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="org.eclipse.skalli.view.Consts" %>
<%@ page session="true" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Login</title>
<style type="text/css">
@import "<%=Consts.JSP_STYLE%>";
</style>
</head>
<body class="welcomepage">
<jsp:include page="<%=Consts.JSP_HEADER%>" flush="true" />

<div class="searcharea">
    <center>
    <img src="/VAADIN/themes/simple/images/logo_large.png" alt="Logo">
    <div class="search-section">
    <h1>You have been logged out!</h1>
    <a href="/">Back to Homepage</a>
    </div>
    </center>
</div>
<% session.invalidate(); %>
</body>
</html>
