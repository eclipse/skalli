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
<%@ page import="java.io.IOException" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.SortedSet" %>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<%@ page import="org.eclipse.skalli.model.Issue" %>
<%@ page import="org.eclipse.skalli.model.Project" %>
<%@ page import="org.eclipse.skalli.model.Project.CompareByProjectName" %>
<%@ page import="org.eclipse.skalli.model.Severity" %>
<%@ page import="org.eclipse.skalli.services.Services" %>
<%@ page import="org.eclipse.skalli.services.group.GroupUtils" %>
<%@ page import="org.eclipse.skalli.services.issues.Issues" %>
<%@ page import="org.eclipse.skalli.services.issues.IssuesService" %>
<%@ page import="org.eclipse.skalli.services.project.ProjectService" %>
<%@ page import="org.eclipse.skalli.services.project.ProjectUtils" %>
<%@ page import="org.eclipse.skalli.view.Consts" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>All Projects - ${html:escapeHtml(pagetitle)}</title>
<style type="text/css">
@import "<%=Consts.JSP_STYLE%>";
</style>
<script src="/js/jquery/1.4.1/jquery.min.js"></script>
<script type="text/javascript">
  function toggle(objId) {
    $('#' + objId).css('display', function(index, oldValue) {
      return (oldValue != 'none') ? 'none' : '';
    });
  }
  function expandAll() {
    $('span[name|="projectNode"]').each(function(index, elem) {
      $(elem).css('display', '');
    });
  }
  function collapseAll() {
    $('span[name|="projectNode"]').each(function(index, elem) {
      $(elem).css('display', 'none');
    });
  }
  $(function() {
    $(document).tooltip();
  });
</script>
</head>
<body>

<%-- header area --%>
<jsp:include page="<%=Consts.JSP_HEADER%>" flush="true" />
<jsp:include page="<%=Consts.JSP_HEADER_SEARCH%>" flush="true" />

<%-- navigation menu on left side --%>
<jsp:include page="<%=Consts.JSP_NAVIGATIONBAR%>" flush="true" />

<%-- search results and pagination --%>
<div class="hierarchyarea">
<pre>
<%
    String userId = (String) request.getAttribute(Consts.ATTRIBUTE_USERID);
    String projectId = request.getParameter(Consts.PARAM_ID);
    renderHierarchy(out, projectId, userId);
%>
</pre>
</div>
</body>
</html>
<%!
    private ProjectService projectService = Services.getRequiredService(ProjectService.class);
    private IssuesService issuesService = Services.getService(IssuesService.class);

    private void renderHierarchy(JspWriter out, String projectId, String userId) throws IOException {
        ArrayList<Project> nodes = new ArrayList<Project>();
        if (projectId != null) {
            Project project = projectService.getProjectByProjectId(projectId);
            if (project != null) {
                nodes.add(project);
            }
        }
        if (nodes.isEmpty()) {
            nodes.addAll(projectService.getProjects(new CompareByProjectName()));
        }

        out.append("<a href=\"javascript:expandAll();\">[+] expand</a>   ");
        out.append("<a href=\"javascript:collapseAll();\">[-] collapse</a>   ");
        if (projectId != null) {
            out.append("<a href='" + Consts.URL_ALLPROJECTS + "'>[&lt;&lt;] back</a>");
        }
        out.append("<br>");

        for (Project node : nodes) {
            if (projectId != null || node.getParentEntity() == null) {
                boolean showIssues = false;
                if (userId != null && (GroupUtils.isAdministrator(userId) || ProjectUtils.isProjectAdmin(userId, node))) {
                    showIssues = true;
                }
                traverseProject(out, node, 0, showIssues, userId);
            }
        }
    }

    private void traverseProject(JspWriter out, Project project, int tab,
            boolean showIssues, String userId) throws IOException {

        SortedSet<Project> subprojects = project.getSubProjects(new CompareByProjectName());
        int sizeChildren = subprojects.size();

        appendTabs(out, tab);
        out.append("<a class='projectlink" + tab + "' href='/projects/" + project.getProjectId() + "' target='_top'>");
        out.append(StringEscapeUtils.escapeHtml(project.getName()));
        if (sizeChildren > 0) {
            out.append(" (" + sizeChildren + ")");
        }
        appendIssues(out, project, showIssues);
        out.append("</a>");

        if (sizeChildren > 0) {
            out.append("<a class='optionlink' href=\"javascript:toggle('" + project.getUuid() + "');\">expand/collapse</a>");
            out.append("<a class='optionlink' href=\"" + Consts.URL_ALLPROJECTS + "&" + Consts.PARAM_ID + "="
                    + project.getProjectId() + "\">browse</a>");
        }
        out.append("<br/>");
        out.append("<span id=\"" + project.getUuid() + "\" name='projectNode'>");
        if (sizeChildren > 0) {
            for (Project subproject : subprojects) {
                boolean showIssuesChild = showIssues;
                if (showIssuesChild == false && userId != null && ProjectUtils.isProjectAdmin(userId, subproject)) {
                    showIssuesChild = true;
                }
                traverseProject(out, subproject, tab + 1, showIssuesChild, userId);
            }
        }
        out.append("</span>");
    }

    private void appendTabs(JspWriter out, int tab) throws IOException {
        for (int i = 0; i < tab; i++) {
            out.append("     ");
        }
    }

    private void appendIssues(JspWriter out, Project project, boolean showIssues) throws IOException {
        if (showIssues && issuesService != null) {
            Issues issues = issuesService.getByUUID(project.getUuid());
            if (issues != null && issues.getIssues().size() > 0) {
                appendIssueIcon(out, issues);
                appendIssueTooltip(out, issues);
            }
        }
    }

    private void appendIssueIcon(JspWriter out, Issues issues) throws IOException {
        out.append(" <img class='issueicon' src=\"");
        if (issues.getIssues(Severity.FATAL).size() > 0) {
            out.append("/VAADIN/themes/simple/icons/issues/fatal.png\" alt=\"Fatal\"");
        } else if (issues.getIssues(Severity.ERROR).size() > 0) {
            out.append("/VAADIN/themes/simple/icons/issues/error.png\" alt=\"Error\"");
        } else if (issues.getIssues(Severity.WARNING).size() > 0) {
            out.append("/VAADIN/themes/simple/icons/issues/warning.png\" alt=\"Warning\"");
        } else {
            out.append("/VAADIN/themes/simple/icons/issues/info.png\" alt=\"Info\"");
        }
    }

    private void appendIssueTooltip(JspWriter out, Issues issues) throws IOException {
        String tooltip = Issue.getMessage("The following issues were found ", issues.getIssues());
        out.append(" title=\"" + StringEscapeUtils.escapeHtml(tooltip) + "\" />");
    }
%>
