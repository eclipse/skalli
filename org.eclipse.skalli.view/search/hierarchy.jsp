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
<%@ page import="org.eclipse.skalli.model.Severity" %>
<%@ page import="org.eclipse.skalli.model.Issue" %>
<%@ page import="org.eclipse.skalli.services.issues.Issues" %>
<%@ page import="org.eclipse.skalli.services.user.LoginUtils" %>
<%@ page import="org.eclipse.skalli.services.group.GroupUtils" %>
<%@ page import="java.util.LinkedList" %>
<%@ page import="java.util.Comparator" %>
<%@ page import="java.io.IOException" %>
<%@ page import="org.eclipse.skalli.model.Project" %>
<%@ page import="org.eclipse.skalli.services.project.ProjectNode" %>
<%@ page import="java.util.List" %>
<%@ page import="org.eclipse.skalli.services.project.ProjectService" %>
<%@ page import="org.eclipse.skalli.services.project.ProjectUtils" %>
<%@ page import="org.eclipse.skalli.services.issues.IssuesService" %>
<%@ page import="org.eclipse.skalli.services.Services" %>
<%@ page import="org.eclipse.skalli.view.Consts" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<title>All Projects - ${pagetitle}</title>
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
    Comparator<Project> comp = new Comparator<Project>() {
      public int compare(Project project1, Project project2) {
        return project1.getName().compareToIgnoreCase(project2.getName());
      }
    };
    String projectId = request.getParameter(Consts.PARAM_ID);
    ProjectService service = Services.getService(ProjectService.class);
    List<ProjectNode> nodes = null;
    if (projectId == null) {
      nodes = service.getRootProjectNodes(comp);
    } else {
      Project project = service.getProjectByProjectId(projectId);
      if (project!=null) {
        ProjectNode node = service.getProjectNode(project.getUuid(), comp);
        nodes = new LinkedList<ProjectNode>();
        nodes.add(node);
      } else {
        out.append("Project not found!<br><br>");
        out.append("<a href='"+Consts.URL_ALLPROJECTS+"'>All Projects</a>");
      }
    }
    if (nodes!=null) {
      out.append("<a href=\"javascript:expandAll();\">[+] expand</a>   ");
      out.append("<a href=\"javascript:collapseAll();\">[-] collapse</a>   ");
      if (projectId!=null) {
       out.append("<a href='"+Consts.URL_ALLPROJECTS+"'>[&lt;&lt;] back</a>");
      }
      out.append("<br>");
      String userId = (String) request.getAttribute("userId");
      if (request.getAttribute(ATTRIBUTE_USERID) == null) {
        LoginUtils loginUtil = new LoginUtils(request);
        userId = loginUtil.getLoggedInUserId();
        request.setAttribute(ATTRIBUTE_USERID, userId);
      }
      for (ProjectNode rootNode : nodes) {
        boolean showIssues = false;
        if (userId!=null && (GroupUtils.isAdministrator(userId) || ProjectUtils.isProjectAdmin(userId, rootNode.getProject())))
          showIssues = true;
        traverseSubProjects(request, out, rootNode, 0, showIssues);
      }
    }
%>
</pre>
</div>
</body>
</html>
<%!
  private IssuesService issuesService = Services.getService(IssuesService.class);
  private final String ATTRIBUTE_USERID = "userId";

  private void traverseSubProjects(ServletRequest request, JspWriter out, ProjectNode projectNode, int tab, boolean showIssues) throws IOException{
    Project project = projectNode.getProject();
    for (int i=0; i<tab; i++)
          out.append("     ");
    out.append("<a class='projectlink"+tab+"' href='/projects/"+project.getProjectId()+"' target='_top'>");
    out.append(project.getName());
    int sizeChildren = countSubProjects(projectNode, true);
    if (sizeChildren>0)
      out.append(" ("+sizeChildren+")");

    if (showIssues && issuesService!=null) {
      Issues issues = issuesService.getByUUID(projectNode.getProject().getUuid());
      if (issues!=null && issues.getIssues().size()>0) {
        String tooltip = Issue.getMessage("The following issues were found ", issues.getIssues());
        //tooltip = tooltip.replaceAll("- ", "\n ");
        out.append(" <img class='issueicon' src=\"");
        if (issues.getIssues(Severity.FATAL).size()>0) {
          out.append("/VAADIN/themes/simple/icons/issues/fatal.png\" alt=\"Fatal\"");
        } else if (issues.getIssues(Severity.ERROR).size()>0) {
          out.append("/VAADIN/themes/simple/icons/issues/error.png\" alt=\"Error\"");
        } else if (issues.getIssues(Severity.WARNING).size()>0) {
          out.append("/VAADIN/themes/simple/icons/issues/warning.png\" alt=\"Warning\"");
        } else {
          out.append("/VAADIN/themes/simple/icons/issues/info.png\" alt=\"Info\"");
        }
        out.append(" title=\""+ tooltip +"\" />");
      }
    }
    out.append("</a>");

    if (sizeChildren>0) {
      out.append("<a class='optionlink' href=\"javascript:toggle('"+project.getUuid()+"');\">expand/collapse</a>");
      out.append("<a class='optionlink' href=\""+Consts.URL_ALLPROJECTS+"&"+Consts.PARAM_ID+"="+project.getProjectId()+"\">browse</a>");
    }
    out.append("<br/>");
    out.append("<span id=\""+project.getUuid()+"\" name='projectNode'>");
    if (sizeChildren>0) {
      List<ProjectNode> subNodes = projectNode.getSubProjects();
      for (ProjectNode subNode : projectNode.getSubProjects()) {
        String userId = (String) request.getAttribute(ATTRIBUTE_USERID);
        boolean showIssuesChild = showIssues;
        if (showIssuesChild==false && userId!=null && ProjectUtils.isProjectAdmin(userId, subNode.getProject())) {
          showIssuesChild = true;
        }
        traverseSubProjects(request, out, subNode, tab+1, showIssuesChild);
      }
    }
    out.append("</span>");
  }

  private int countSubProjects(ProjectNode projectNode, boolean includeHierarchyOfChildren) {
    int sizeChildren = projectNode.getSubProjects().size();
    if (includeHierarchyOfChildren) {
      for (ProjectNode children : projectNode.getSubProjects()) {
        sizeChildren += countSubProjects(children, includeHierarchyOfChildren);
      }
    }
    return sizeChildren;
  }
%>
