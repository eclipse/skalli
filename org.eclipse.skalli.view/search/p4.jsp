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
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ page import="org.eclipse.skalli.view.internal.filter.ext.JiraFilter" %>
<%@ page import="org.eclipse.skalli.view.Consts" %>
<%@ page errorPage="/error" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<title>Request Perforce Project - ${pagetitle}</title>
<style type="text/css">
@import "<%=Consts.JSP_STYLE%>";

.leftMargin {
    margin-left: 40px;
}
.important {
    color:red;
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
<div class="projectarearight" style="max-width:600px;">
    <h3>
        <img src="/img/p4_logo.png" alt="Perforce Logo" style="width:32px; height:32px; margin-right:5px; vertical-align:middle;"/> Request Perforce Project
    </h3>
    <p>A Perforce project can be requested via an IT/IBC ticket on component <strong>DEV-NWPROD-PERF-OPS</strong>.
    Follow the link below to create a new ticket in a separate browser window.
    The component will automatically be selected for you.</p>
    <p class="leftMargin"><a href="https://css.wdf.sap.corp/itibc/default.htm?~bcsm10-themkext=0000042595" target="_blank">Create a new ticket now</a></p>
    <p>Make sure to mention the following data:</p>
    <table border="0" class="leftMargin">
        <colgroup>
            <col width="120"/>
            <col width="480"/>
          </colgroup>
        <tr align="left">
            <td>Name:</td>
            <td><em>${project.name}</em></td>
        </tr>
        <tr align="left">
            <td>Key:</td>
            <td><em>${project.projectId}</em></td>
        </tr>
        <tr align="left">
            <td>URL:</td>
            <td><em>${projectUrl}</em></td>
        </tr>
        <tr align="left">
            <td>Proposal:</td>
            <td><em>${proposedName}</em></td>
        </tr>
        <tr align="left">
            <td>Codelines:</td>
            <td><em>dev</em></td>
        </tr>
        <tr align="left">
            <td>Preferred Server:</td>
            <td><em>(if known, otherwise leave empty)</em></td>
        </tr>
        <tr align="left">
            <td>User(s):</td>
            <td><em>${committers}</em></td>
        </tr>
        <tr align="left">
            <td>Process:</td>
            <td><em>https://wiki.wdf.sap.corp/x/T4oEMw</em></td>
        </tr>
    </table>
    <p>or simply copy the above to your clipboard:&nbsp;
        <object classid="clsid:d27cdb6e-ae6d-11cf-96b8-444553540000"
                  width="110"
                  height="14"
                  id="clippy" >
            <param name="movie" value="/VAADIN/themes/simple/flash/clippy.swf"/>
            <param name="allowScriptAccess" value="always" />
            <param name="quality" value="high" />
            <param name="scale" value="noscale" />
            <param name="FlashVars" value="text=Name: ${project.name}, Key: ${project.projectId}, URL: ${projectUrl}, Proposal: ${proposedName}, Codelines: dev, Prefered Server: , User(s): ${committers}, Process: https://wiki.wdf.sap.corp/x/T4oEMw" />
            <param name="bgcolor" value="#FFFFFF" />
            <embed src="/VAADIN/themes/simple/flash/clippy.swf"
                   width="110"
                   height="14"
                   name="clippy"
                   quality="high"
                   allowScriptAccess="always"
                   type="application/x-shockwave-flash"
                   pluginspage="http://www.macromedia.com/go/getflashplayer"
                   FlashVars="text=Name: ${project.name}, Key: ${project.projectId}, URL: ${projectUrl}, Proposal: ${proposedName}, Codelines: dev, Prefered Server: , User(s): ${committers}, Process: https://wiki.wdf.sap.corp/x/T4oEMw"
                   bgcolor="#FFFFFF"
               />
        </object>
    </p>
    <p class="important">Note: The new Perforce project will <strong>not</strong> automatically be added to your SCM locations, but needs to be added manually once it has been created by the Production team.</p>
    <p><a href="/projects/${project.projectId}">Back to project</a></p>
</div>
</body>
</html>
