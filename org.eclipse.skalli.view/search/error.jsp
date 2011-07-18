<%@page import="java.io.OutputStreamWriter"%>
<%@page import="java.beans.XMLEncoder"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.io.StringWriter"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="org.eclipse.skalli.common.Consts"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>ProjectPortal</title>
<style type="text/css">
@import "<%=Consts.JSP_STYLE%>";
</style>
</head>
<body>

<!-- header area -->

<jsp:include page="<%= Consts.JSP_HEADER %>" flush="true" />

<jsp:include page="<%= Consts.JSP_HEADER_SEARCH %>" flush="true" />

<!-- navigation menu on left side -->

<jsp:include page="<%=Consts.JSP_NAVIGATIONBAR%>" flush="true" />

<!-- Error -->
<div class="projectarearight" >
<div class="error">

<p style=" color: red;"> Oops, unexpected internal error occured.</p>

<c:if test="${exception != null}">
  <c:if test="${feedbackConfig != null }">
    Please inform the ProjectPortal team about this exception: 
    <a title="${feedbackConfig.displayName}" href="${feedbackConfig.url}">Send Bug Report</a>
    <br><br>
    Please include the following information:
    <br>
    1) Step-by-Step description how to reproduce this exception.<br>
    2) Copy+Paste the following exception call stack:<br><br>
  </c:if>
    <%
    Object o = request.getAttribute("exception");
    int length = 1;
    if (o instanceof Exception) {
        Exception exception = (Exception) request.getAttribute("exception");
        length = exception.getStackTrace().length;
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);		
    %>

    <textarea name="callstack" rows=<%= length %> wrap=OFF cols=100 readonly ><% out.print(sw); %></textarea>
    <%
    } else {
        out.println("Callstack not available");
    }
    %>
</c:if>
</div>
</div>
</body>
</html>
