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
<%@ page import="org.eclipse.skalli.view.internal.filter.ext.GitGerritFilter" %>
<%@ page import="org.eclipse.skalli.view.Consts" %>
<%@ page errorPage="/error" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<title>Create Git/Gerrit Repository - ${pagetitle}</title>
<style type="text/css">
  @import "<%=Consts.JSP_STYLE%>";

  .projectarearight {
    max-width: 600px;
  }
  .warningmessage {
    color: #9E0000;
  }
  .errormessage {
    color: red;
    font-weight: bold;
  }
  .hint {
    font-style: italic;
  }
  .buttonOnlyForm {
    display: inline;
  }
  .cancel, .cancel:active, .cancel:visited, .cancel:hover {
    color: #000000;
    font-family: arial,helvetica,verdana,sans-serif;
    font-size: 13px;
    padding-left:8px;
    padding-right:8px;
    padding-top:1px;
    padding-bottom:1px;
    background-color: #EBE9ED;
    text-decoration: none;
    cursor:default;
  }
  .marginTop {
    margin-top:10px;
  }
</style>
<script src="/js/jquery/1.4.1/jquery.min.js"></script>
</head>
<body>

<%-- header area --%>
<jsp:include page="<%= Consts.JSP_HEADER %>" flush="true" />
<jsp:include page="<%= Consts.JSP_HEADER_SEARCH %>" flush="true" />

<%-- navigation menu on left side --%>
<jsp:include page="<%= Consts.JSP_NAVIGATIONBAR%>" flush="true" />

<%-- Gerrit Form --%>
<div class="projectarearight">
<h3>
<img src="/img/git_logo.png" alt="Git Logo" style="width:32px; height:32px; margin-right:5px; vertical-align:middle;" /> Create Git/Gerrit Repository
</h3>
  <c:choose>
    <c:when test="${empty param.action}">
      <p>Use this form to create a new repository on '${gerritHost}'.</p>
      <p>
        Enter a new group name or select one from the proposals based on the project hierarchy.<br/>
        Furthermore specify the new repository name that will be used for creation.
      </p>
      <p>Once created the repository will be added as SCM location to your project.</p>
      <form name="toggleGroupProposals" action="gitgerrit" method="get">
        <p>
          <input type="hidden" name="id" value="${project.projectId}"/>
          <input type="radio" name="proposeExistingGroups" value="new" <c:if test="${empty param.proposeExistingGroups || param.proposeExistingGroups == 'new'}">checked="checked"</c:if> onclick="document.toggleGroupProposals.submit();" />
          <small>Enter a new group name</small>
          <input type="radio" name="proposeExistingGroups" value="related" <c:if test="${param.proposeExistingGroups == 'related'}">checked="checked"</c:if> onclick="document.toggleGroupProposals.submit();" />
          <small>Choose from an existing Gerrit group related to this project</small>
          <input type="radio" name="proposeExistingGroups" value="all" <c:if test="${param.proposeExistingGroups == 'all'}">checked="checked"</c:if> onclick="document.toggleGroupProposals.submit();" />
          <small>Choose from all existing Gerrit groups</small>
        </p>
        <p id="submitButton">
          <input type="submit" name="btnSubmit" value="Go" class="searchsubmit"/>
        </p>
        <script type="text/javascript">
          $('#submitButton').css('display', 'none');
        </script>
      </form>
      <form id="gitgerritform" name="input" action="gitgerrit?id=${project.projectId}" method="post" class="marginTop">
          <table>
            <tr>
              <td>Gerrit Group:</td>
              <td>
                 <c:choose>
                   <c:when test="${(param.proposeExistingGroups == 'related')  || (param.proposeExistingGroups == 'all')}">
                      <select name="<%= GitGerritFilter.PARAMETER_GROUP %>" style="width:100%;">
                        <c:forEach var="group" items="${proposedExistingGroups}">
                          <option>${group}</option>
                        </c:forEach>
                       </select>
                    </c:when>
                    <c:otherwise>
                      <input type="text" name="<%= GitGerritFilter.PARAMETER_GROUP %>" value="${proposedGroup}" class="searchfield"/>
                    </c:otherwise>
                  </c:choose>
              </td>
            </tr>
              <tr>
              <td>Git Repository:</td>
              <td>
                <input type="text" name="<%= GitGerritFilter.PARAMETER_REPO %>" value="${proposedRepo}" class="searchfield"/>
              </td>
            </tr>
          </table>
          <p>
            <input type="hidden" name="action" value="<%= GitGerritFilter.ACTION_CHECK %>"/>
            <input type="submit" name="submit" value="Check" class="searchsubmit"/>
            <a href="/projects/${project.projectId}" class="cancel searchsubmit">Cancel</a>
        </p>
      </form>
      <p class="hint">Your values must not be blank and must not contain any whitespaces.</p>
      <p class="hint">Click 'Check' to contact Gerrit and find out whether the group and/or the repository exist.</p>
      <p class="hint">This action won't do any changes.</p>
    </c:when>
    <c:when test="${param.action == 'check'}">
      <p>Your input has been checked:</p>
        <table>
           <tr valign="top" align="left">
             <td>Gerrit Group:</td>
             <td>
               <strong>'${param.group}'</strong>
            <c:if test="${invalidGroup}">is invalid<br />(must not be blank, no whitespaces)</c:if>
            <c:if test="${!invalidGroup && groupExists}">already exists</c:if>
            <c:if test="${!invalidGroup && !groupExists}">will be created</c:if>
             </td>
           </tr>
           <c:if test="${!invalidGroup && !groupExists && not empty knownAccounts}">
           <tr valign="top" align="left">
             <td>&nbsp;</td>
             <td>
               <em>
                 adding 
              <c:forEach var="accountId" items="${knownAccounts}">
                (${accountId})
              </c:forEach>
            </em>
             </td>
           </tr>
           </c:if>
             <tr valign="top" align="left">
             <td>Git Repository:</td>
             <td>
               <strong>'${param.repo}'</strong>
            <c:if test="${invalidRepo}">is invalid<br />(must not be blank, no whitespaces)</c:if>
            <c:if test="${!invalidRepo && repoExists}">already exists</c:if>
            <c:if test="${!invalidRepo && !repoExists}">will be created</c:if>
             </td>
           </tr>
         </table>
         <div class="marginTop">
        <form name="save" action="gitgerrit?id=${project.projectId}" method="post" class="buttonOnlyForm">
          <input type="hidden" name="<%= GitGerritFilter.PARAMETER_GROUP %>" value="${param.group}" />
          <input type="hidden" name="<%= GitGerritFilter.PARAMETER_REPO %>" value="${param.repo}" />
             <input type="hidden" name="action" value="<%= GitGerritFilter.ACTION_SAVE %>"/>
             <input type="submit" name="submit" value="Proceed" class="searchsubmit" <c:if test="${invalidGroup || invalidRepo || (!groupExists && repoExists) || (!groupExists && !repoExists && empty knownAccounts)}">disabled="disabled"</c:if> />
           </form>
        <form name="back" action="gitgerrit?id=${project.projectId}" method="post" class="buttonOnlyForm">
          <input type="hidden" name="<%= GitGerritFilter.PARAMETER_GROUP %>" value="${param.group}" />
          <input type="hidden" name="<%= GitGerritFilter.PARAMETER_REPO %>" value="${param.repo}" />
             <input type="submit" name="submit" value="Back" class="searchsubmit" />
           </form>
           <a href="/projects/${project.projectId}" class="cancel searchsubmit">Cancel</a>
      </div>
        <c:if test="${!invalidGroup && !invalidRepo}">
        <c:choose>
          <c:when test="${groupExists && repoExists}">
            <p class="hint">Click 'Proceed' to add the existing SCM location to your project.</p>
          </c:when>
          <c:when test="${!groupExists && !repoExists && not empty knownAccounts}">
            <p class="warningmessage">
              Note that creating a group or a repository on Gerrit cannot be undone.<br />
              Also note that only accounts known to Gerrit are added to the group.
            </p>
            <c:if test="${noGerritUser}">
              <p class="warningmessage">Your account is not known to Gerrit. You would not be able to administer the group and/or project by yourself. However your team members could.</p>
            </c:if>
            <p class="hint">Click 'Proceed' to create the group, the repository and assign the group to it.</p>
          </c:when>
          <c:when test="${groupExists && !repoExists}">
            <p class="warningmessage">Note that creating a group or a repository on Gerrit cannot be undone.</p>
            <p class="hint">Click 'Proceed' to create the repository and assign it to the existing group.</p>
          </c:when>
          <c:when test="${!groupExists && repoExists}">
            <p class="errormessage">It is not possible to assign new groups to existing repositories. Hence this request cannot be processed.</p>
          </c:when>
          <c:when test="${!groupExists && !repoExists && empty knownAccounts}">
            <p class="errormessage">No one of your team has a known Gerrit account. You would not be able to administer the group and/or project by yourself. Hence this request cannot be processed.</p>
            <p class="hint">Log on once to <a href="https://git.wdf.sap.corp:8080/" target="_new">Gerrit</a> to create your account and try again.</p>
          </c:when>
        </c:choose>
      </c:if>
      <c:if test="${invalidGroup || invalidRepo}">
        <p class="errormessage">Invalid input. Hence this request cannot be processed.</p>
      </c:if>
      <p class="hint">Click 'Back' to change your input.</p>
    </c:when>
    <c:when test="${param.action == 'save'}">
     <c:choose>
      <c:when test="${dataSaved}">
        <p>Your request was successfully processed.</p>
          <c:if test="${!invalidGroup && !groupExists}">
            <p>
              <strong>'${param.group}' was created.</strong> Make sure that you are listed as
              member and that you can edit the group and related projects. If not, contact the Gerrit team.
            </p>
          </c:if>
          <c:if test="${!invalidRepo && !repoExists}">
            <p>
              <strong>'${param.repo}' was created.</strong>
            </p>
          </c:if>
          <p><a href="/projects/${project.projectId}">Back to project</a></p>
         </c:when>
        <c:otherwise>
          <p>Sorry, but something went wrong and you request could not be processed.</p>
          <form name="check" action="gitgerrit?id=${project.projectId}" method="post">
            <input type="hidden" name="<%= GitGerritFilter.PARAMETER_GROUP %>" value="${param.group}" />
            <input type="hidden" name="<%= GitGerritFilter.PARAMETER_REPO %>" value="${param.repo}" />
               <input type="hidden" name="action" value="<%= GitGerritFilter.ACTION_CHECK %>"/>
              <input type="submit" name="submit" value="Check again" class="searchsubmit" />
            <p><a href="/projects/${project.projectId}" class="searchsubmit cancel">Back to project</a></p>
          </form>
          <p class="hint">Click 'Check again' to revalidate your input.</p>
        </c:otherwise>
      </c:choose>
    </c:when>
  </c:choose>
<c:if test="${not empty gerritContact}">
    <div><a href="${gerritContact}" target="_blank">Gerrit Contacts</a></div>
</c:if>
</div>
</body>
</html>
