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
<%@ page import="org.eclipse.skalli.view.internal.filter.ext.JiraFilter" %>
<%@ page import="org.eclipse.skalli.view.Consts" %>
<%@ page errorPage="/error" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<title>Create JIRA Project - ${html:escapeHtml(pagetitle)}</title>
<style type="text/css">
@import "<%=Consts.JSP_STYLE%>";
.inputhelp {
  color:grey;
}
.errorMessage {
  color:red;
}
.warningMessage {
  color:#FF8000;
}
.disabled {
  color: grey;
}
</style>
<script language="JavaScript" type="text/javascript" src="/search/inputhelp.js"></script>
</head>
<body>

<%-- header area --%>
<jsp:include page="<%= Consts.JSP_HEADER %>" flush="true" />
<jsp:include page="<%= Consts.JSP_HEADER_SEARCH %>" flush="true" />

<%-- navigation menu on left side --%>
<jsp:include page="<%=Consts.JSP_NAVIGATIONBAR%>" flush="true" />

<%-- JIRA Form --%>
<div class="projectarearight" style="max-width:600px;">
<h3>
<img src="/img/jira_logo.png" alt="JIRA Logo" style="width:32px; height:32px; margin-right:5px; vertical-align:middle;"/> Add Project to JIRA
</h3>
Click on the link below to open a JIRA dialog on the JIRA server in another browser window. Don't close the current window and process the form in the JIRA window. You may want to use the following data:<br/>
<br/>

<table border="0" style="padding-left: 40px;">
 <colgroup>
    <col width="80"/>
    <col width="400"/>
  </colgroup>

<tr align="left">
<td>Name:</td>
<td><em>${html:escapeHtml(project.name)}</em></td>
</tr>
<tr align="left">
<td>Key:</td>
<td><em>${project.projectId}</em></td>
</tr>
<tr align="left">
<td>Url: </td>
<td><em>${projectUrl}</em></td>
</tr>
<tr align="left">
<td>Description: </td>
<td><em>${html:clean(project.description)}</em></td>
</tr>
</table>
<br/>
Copy the key of the created JIRA project to the clipboard before you close the JIRA browser window.
<br/><br/>
<a href="${remoteJiraUrl}" target="_blank">
Follow this link to open a JIRA dialog on JIRA server.</a>
<br/><br/>
Paste the copied project key from clipboard to the following input field. Choose if you want to set the JIRA project as BugTracker and/or as Scrum Backlog to this project '${project.name}' and click on 'Save' if you want to persist this data. Click on 'Cancel' if you don't want to save any data to your project. <br/><br/>
<form id="jiraform" name="jiraform" action="jira" method="post">
  <input type="hidden" name="<%= Consts.PARAM_ID %>" value="${project.projectId}"/>
  <table>
    <tr>
      <td colspan="2">
        ${jiraBaseUrl}
        <input id="jiraprojectkey" type="text" name="<%= JiraFilter.PARAMETER_JIRA %>" value="${jiraProjectKey}" class="searchfield"/>
      </td>
    </tr>
    <tr>
      <td>
        <div style="white-space: nowrap;">The Jira Project will be used</div><br/>
      </td>
      <td width="100%">
        <c:choose>
          <c:when test="${devInfExtInherited}">
            <input type="checkbox" disabled="disabled"/>
            <span class="disabled">to track bugs (not available as Development Information is inherited)</span>
          </c:when>
          <c:when test="${addBugTracker}">
            <input type="checkbox" onclick="toggleBooleanValue('addBugTracker');document.jiraform.submit();" checked="checked" />
            to track bugs
          </c:when>
          <c:when test="${!addBugTracker}">
            <input type="checkbox" onclick="toggleBooleanValue('addBugTracker');document.jiraform.submit();"/>
            to track bugs
          </c:when>
        </c:choose>
        <input type="hidden" id="addBugTracker" name="addBugTracker" value="${ addBugTracker }" />
        <br/>

        <c:choose>
          <c:when test="${scrumExtInherited}">
            <input type="checkbox" disabled="disabled" />
            <span class="disabled">as a Scrum backlog (not available as Scrum Information is inherited)</span>
          </c:when>
          <c:when test="${addScrumBacklog}">
            <input type="checkbox" onclick="toggleBooleanValue('addScrumBacklog');document.jiraform.submit();" checked="checked" />
            as a Scrum backlog
          </c:when>
          <c:when test="${!addScrumBacklog}">
            <input type="checkbox" onclick="toggleBooleanValue('addScrumBacklog');document.jiraform.submit();" />
            as a Scrum backlog
          </c:when>
        </c:choose>
        <input type="hidden" id="addScrumBacklog" name="addScrumBacklog" value="${ addScrumBacklog }" />
        <br/>
      </td>
    </tr>
  </table>
  <br/>
  <c:choose>
    <c:when test="${addBugTracker || addScrumBacklog}">
      <input type="submit" value="Save" name="<%= JiraFilter.PARAMETER_SAVE %>" class="searchsubmit"/>
    </c:when>
    <c:otherwise>
      <input type="submit" value="Save" name="<%= JiraFilter.PARAMETER_SAVE %>" class="searchsubmit" disabled="disabled"/>
    </c:otherwise>
  </c:choose>
  <input type="submit" value="Cancel" name="<%= JiraFilter.PARAMETER_CANCEL %>" class="searchsubmit"/>
</form>
<script type="text/javascript">
  inputHelp('jiraprojectkey', 'JIRA project key');
  initForm('jiraform');
</script>

<br/>

<c:choose>
  <c:when test="${validationException != null }">
    <span class="errorMessage">
    ${validationException.message}
    </span>
  </c:when>
  <c:when test="${invalidUrl == true}">
    <span class="errorMessage">Please enter a valid URL!</span>
  </c:when>
  <c:otherwise>
    <c:if test="${bugTracker != null && addBugTracker}">
      <div class="warningMessage">
      Bug Tracker is already defined for this project ('${bugTracker}'), click on 'Save' to overwrite the current setting.
      </div>
    </c:if>
    <c:if test="${scrumBacklog != null && addScrumBacklog}">
      <div class="warningMessage">
      Scrum Backlog is already defined for this project ('${scrumBacklog}'), click on 'Save' to overwrite the current setting.
      </div>
    </c:if>
  </c:otherwise>
</c:choose>

</div>
</body>
</html>
