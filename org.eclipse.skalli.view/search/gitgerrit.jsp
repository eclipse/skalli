<%--
    Copyright (c) 2010, 2011 SAP AG and others.
    All rights reserved. This program and the accompanying materials
    are made available under the terms of the Eclipse Public License v1.0
    which accompanies this distribution, and is available at
    http://www.eclipse.org/legal/epl-v10.html

    Contributors:
        SAP AG - initial API and implementation
 --%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" uri="http://www.eclipse.org/skalli/taglib"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page errorPage="/error"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Create Git/Gerrit Repository - ${html:escapeHtml(pagetitle)}</title>
<style type="text/css">
@import "/search/style.css";

.projectarearight {
    position: relative;
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

.formheader {
    border-top-style: dotted;
    border-top-width: 1px;
    padding-top: 12px;
    font-style: italic;
}

.formlabel {
    padding-right: 10px;
    width: 80px;
    vertical-align: top;
}

.formfield {
    width: 35em;
    border-width: 1px;
    border-style: solid;
    border-color: #BBBBBB;
}

.buttonOnlyForm {
    display: inline;
}

.cancel,.cancel:active,.cancel:visited,.cancel:hover {
    color: #000000;
    font-family: arial, helvetica, verdana, sans-serif;
    font-size: 13px;
    padding-left: 8px;
    padding-right: 8px;
    padding-top: 2px;
    padding-bottom: 1px;
    background-color: #EBE9ED;
    text-decoration: none;
    cursor: default;
}

.marginTop {
    margin-top: 20px;
}

.logo {
    width: 32px;
    height: 32px;
    margin-right: 5px;
    vertical-align: middle;
}
</style>
</head>
<body>
  <jsp:include page="/search/includes/header.jsp" flush="true" />
  <jsp:include page="/search/includes/searchheader.jsp" flush="true" />
  <jsp:include page="/search/includes/navigationbar.jsp" flush="true" />

  <div class="projectarearight">
    <h3><img src="/img/git_logo.png" alt="Gerrit" class="logo" />Create Git Repository</h3>
    <form id="gerritform" name="gerritform"
      action="gitgerrit?id=${project.projectId}" method="post"
      accept-charset="UTF-8" class="marginTop">
      <input type="hidden" name="action" value="" />
      <c:choose>
        <c:when test="${empty param.action || param.action == 'refresh'}">
          <p>Use this form to create a new Git repository.</p>
          <p>Once created the repository will be added as SCM
            location to your project.</p>
          <c:if test="${fn:length(gerritServers) > 1}">
            <p class="formheader">
              Choose the <b>Gerrit Server</b> you want your Git repository to be hosted on
              or accept the proposal (recommended).<br />
            </p>
            <table>
              <tr>
                <td class="formlabel">Gerrit Server</td>
                <td><select name="gerritId" onchange="submitWithAction('serverSelected')" class="formfield">
                    <c:forEach var="next" items="${gerritServers}">
                      <option value="${next.id}"
                        <c:if test="${next.id == gerritServer.id}">selected="selected"</c:if>>
                        ${next.name}&nbsp;&nbsp;&nbsp;(${next.host})</option>
                    </c:forEach>
                  </select></td>
              </tr>
            </table>
          </c:if>
          <p class="formheader">
            Choose a <b>Repository Name</b> or accept the proposal based
            on the project hierarchy (recommended).<br />
            This name will also be assigned to the Gerrit project to be created.
          </p>
          <table>
            <tr>
              <td class="formlabel">Git Repository</td>
              <td><input type="text" name="repository" value="${html:escapeHtml(proposedRepo)}"
                class="formfield" /></td>
            </tr>
          </table>
          <p class="formheader">
            Choose a <b>Gerrit Group</b> or accept the proposal based on
            the project hierarchy (recommended).<br />
            The Gerrit group defines access rights and permissions for your
            Git repository.
          </p>
          <p>
            <input id="groupModeNew" type="radio" name="groupMode" value="new" onclick="submitWithAction('refresh')"
              <c:if test="${empty param.groupMode || param.groupMode == 'new'}">checked="checked"</c:if> />
            <label for="groupModeNew">Enter a new group name</label><br />
            <input id="groupModeRelated" type="radio" name="groupMode" value="related" onclick="submitWithAction('refresh')"
              <c:if test="${param.groupMode == 'related'}">checked="checked"</c:if> />
            <label for="groupModeRelated">Choose an existing Gerrit group related to this project</label><br />
            <input id="groupModeAll" type="radio" name="groupMode" value="all" onclick="submitWithAction('refresh')"
              <c:if test="${param.groupMode == 'all'}">checked="checked"</c:if> />
            <label for="groupModeAll">Choose from all existing Gerrit groups</label>
          </p>
          <table>
            <tr>
              <td class="formlabel">Gerrit Group</td>
              <td><c:choose>
                  <c:when test="${(param.groupMode == 'related') || (param.groupMode == 'all')}">
                    <select name="group" class="formfield">
                      <option <c:if test="${proposedGroup == ''}">selected="selected"</c:if>></option>
                      <c:forEach var="next" items="${proposedGroups}">
                        <option <c:if test="${next == proposedGroup}">selected="selected"</c:if>>${next}</option>
                      </c:forEach>
                    </select>
                  </c:when>
                  <c:otherwise>
                    <input type="text" name="group" value="${html:escapeHtml(proposedGroup)}" class="formfield" />
                  </c:otherwise>
                </c:choose></td>
            </tr>
          </table>
          <p class="formheader">
            Choose a Gerrit project as <b>Parent Project</b> or accept
            the proposed default (recommended).<br />
            Your project will inherit basic access rights and permissions from the
            specified parent project.
          </p>
          <p style="margin-top: 8px">
            <input id="parentModeNew" type="radio" name="parentMode" value="new" onclick="submitWithAction('refresh')"
              <c:if test="${empty param.parentMode || param.parentMode == 'new'}">checked="checked"</c:if> />
            <label for="parentModeNew">Enter a parent project</label><br />
            <input id="parentModeRelated" type="radio" name="parentMode" value="related" onclick="submitWithAction('refresh')"
              <c:if test="${param.parentMode == 'related'}">checked="checked"</c:if> />
            <label for="parentModeRelated">Choose from related projects</label><br />
            <input id="parentModePermissions" type="radio" name="parentMode" value="permissions" onclick="submitWithAction('refresh')"
              <c:if test="${param.parentMode == 'permissions'}">checked="checked"</c:if> />
            <label for="parentModePermissions">Choose from permissions-only projects</label><br />
            <input id="parentModeAll" type="radio" name="parentMode" value="all" onclick="submitWithAction('refresh')"
              <c:if test="${param.parentMode == 'all'}">checked="checked"</c:if> />
            <label for="parentModeAll">Choose from all existing projects</label>
          </p>
          <table>
            <tr>
              <td class="formlabel">Parent Project</td>
              <td><c:choose>
                  <c:when test="${(param.parentMode == 'permissions')
                        || (param.parentMode == 'related')
                        || (param.parentMode == 'all')}">
                    <select name="parent" class="formfield">
                      <option <c:if test="${proposedParent == ''}">selected="selected"</c:if>></option>
                      <c:forEach var="next" items="${proposedProjects}">
                        <option <c:if test="${next == proposedParent}">selected="selected"</c:if>>${next}</option>
                      </c:forEach>
                    </select>
                  </c:when>
                  <c:otherwise>
                    <input type="text" name="parent" value="${html:escapeHtml(proposedParent)}" class="formfield" />
                  </c:otherwise>
                </c:choose></td>
            </tr>
          </table>
          <p class="formheader">
            Create the project as <b>Permissions-Only Project</b>. The
            sole purpose of this kind of Gerrit projects is to serve as
            parent for other projects and define access rights and
            permissions.
          </p>
          <p>
            <input id="permitsOnly" type="checkbox" name="permitsOnly" value="permitsOnly"
              <c:if test="${proposedPermitsOnly}">checked="checked"</c:if> />
            <label for="permitsOnly">Permissions-Only Project</label>
          </p>
          <p class="formheader">
            Create the Git repository with an empty <b>Initial Commit</b> (recommended).<br />
            Some tools cannot handle completely empty repositories. However, in some cases, for
            example when migrating an existing repository, you might want Gerrit to skip the
            creation of the initial commit.
          </p>
          <p>
            <input id="emptyCommit" type="checkbox" name="emptyCommit" value="emptyCommit"
              <c:if test="${proposedEmptyCommit}">checked="checked"</c:if> />
            <label for="emptyCommit">Create Repository with Initial Commit</label>
          </p>
          <p class="formheader">
            <input type="button" name="check" value="Check" class="searchsubmit" onclick="submitWithAction('check')" />
            <a href="/projects/${project.projectId}" class="cancel searchsubmit">Cancel</a>
          </p>
          <p class="hint">Click 'Check' to contact Gerrit and find
            out whether the repository and/or the related group already exist.</p>
          <p class="hint">Afterwards you may proceed and actually
            create the repository and/or the related group, or come back
            to this page and amend your settings.</p>
        </c:when>
        <c:when test="${param.action == 'check'}">
          <p>Your input has been checked:</p>
          <table>
            <tr>
              <td class="formlabel">Gerrit Server</td>
              <td>${gerritServer.name} (${gerritServer.host})</td>
            </tr>
            <tr">
              <td class="formlabel">Git Repository</td>
              <td><strong>'${html:escapeHtml(param.repository)}'</strong><br />
                <c:if test="${invalidRepo}">is not a valid repository name: ${invalidRepoMsg}.</c:if>
                <c:if test="${!invalidRepo && repoExists}">already exists.</c:if>
                <c:if test="${!invalidRepo && !repoExists}">will be created.</c:if>
              </td>
            </tr>
            <tr>
              <td class="formlabel">Gerrit Group</td>
              <td><strong>'${html:escapeHtml(param.group)}'</strong><br />
                <c:if test="${invalidGroup}">is not a valid group name: ${invalidGroupMsg}.</c:if>
                <c:if test="${!invalidGroup && groupExists}">already exists.</c:if>
                <c:if test="${!invalidGroup && !groupExists && empty knownAccounts}">will be created.</c:if>
                <c:if test="${!invalidGroup && !groupExists && not empty knownAccounts}">
                  will be created with the following initial committers:<br />
                  <em><c:forEach var="accountId" items="${knownAccounts}"> &nbsp;&nbsp; '${accountId}'</c:forEach></em>
                </c:if></td>
            </tr>
            <tr>
              <td class="formlabel">Parent Project</td>
              <td><strong>'${html:escapeHtml(param.parent)}'</strong>
                <c:if test="${invalidParent}">is not a valid parent project.</c:if>
              </td>
            </tr>
          </table>
          <div class="marginTop">
            <input type="hidden" name="gerritId" value="${param.gerritId}" />
            <input type="hidden" name="groupMode" value="${param.groupMode}" />
            <input type="hidden" name="group" value="${param.group}" />
            <input type="hidden" name="repository" value="${param.repository}" />
            <input type="hidden" name="parentMode" value="${param.parentMode}" />
            <input type="hidden" name="parent" value="${param.parent}" />
            <input type="hidden" name="permitsOnly" value="${param.permitsOnly}" />
            <input type="hidden" name="emptyCommit" value="${param.emptyCommit}" />
            <input type="button" name="proceed" value="Proceed" class="searchsubmit" onclick="submitWithAction('save')"
              <c:if test="${invalidGroup || invalidRepo || invalidParent || (!groupExists && repoExists)
                || (!groupExists && !repoExists && empty knownAccounts)}">disabled="disabled"</c:if> />
            <input type="button" name="back" value="Back" class="searchsubmit" onclick="submitWithAction('refresh')" />
            <a href="/projects/${project.projectId}" class="cancel searchsubmit">Cancel</a>
          </div>
          <c:if
            test="${!invalidGroup && !invalidRepo && !invalidParent}">
            <c:choose>
              <c:when test="${groupExists && repoExists}">
                <p class="hint">Click 'Proceed' to add the existing
                  Git repository to the SCM locations of your project.</p>
              </c:when>
              <c:when test="${!groupExists && !repoExists && empty knownAccounts}">
                <p class="errormessage">No one of your team has a
                  known Gerrit account. You will not be able to
                  administrate the Gerrit project.</p>
                <p class="hint">Log on once to Gerrit to create your
                  account and then try again.</p>
              </c:when>
              <c:when test="${!groupExists && !repoExists && not empty knownAccounts}">
                <c:if test="${noProjectMember}">
                  <p class="warningmessage">You will not be assigned
                    to the Gerrit group to be created since you are not
                    a project member. You will not be able to
                    administrate the Gerrit project.</p>
                </c:if>
                <c:if test="${noGerritUser}">
                  <p class="warningmessage">Your account is not
                    known to Gerrit. You will not be able to
                    administrate the Gerrit project.</p>
                </c:if>
                <p class="hint">Click 'Proceed' to create the
                  specified Git repository and Gerrit group.</p>
              </c:when>
              <c:when test="${groupExists && !repoExists}">
                <p class="hint">Click 'Proceed' to create the
                  specified Git repository and assign it to the existing
                  Gerrit group.</p>
              </c:when>
              <c:when test="${!groupExists && repoExists}">
                <p class="errormessage">It is not possible to assign
                  a new Gerrit group to an existing Git repository.</p>
              </c:when>
            </c:choose>
          </c:if>
          <c:if test="${invalidGroup || invalidRepo || invalidParent}">
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
                  <strong>'${param.group}' was created.</strong> Make
                  sure that you are listed as member and that you can
                  edit the group and related projects.
                </p>
              </c:if>
              <c:if test="${!invalidRepo && !repoExists}">
                <p>
                  <strong>'${param.repository}' was created.</strong>
                </p>
              </c:if>
              <p>
                <a href="/projects/${project.projectId}">Back to project</a>
              </p>
            </c:when>
            <c:otherwise>
              <p>Sorry, but something went wrong and your request
                could not be processed.</p>
              <input type="hidden" name="gerritId" value="${param.gerritId}" />
              <input type="hidden" name="groupMode" value="${param.groupMode}" />
              <input type="hidden" name="group" value="${param.group}" />
              <input type="hidden" name="repository" value="${param.repository}" />
              <input type="hidden" name="parentMode" value="${param.parentMode}" />
              <input type="hidden" name="parent" value="${param.parent}" />
              <input type="hidden" name="permitsOnly" value="${param.permitsOnly}" />
              <input type="hidden" name="emptyCommit" value="${param.emptyCommit}" />
              <input type="button" name="check" value="Check" class="searchsubmit" onclick="submitWithAction('check')" />
              <a href="/projects/${project.projectId}" class="searchsubmit cancel">Back to project</a>
              <p class="hint">Click 'Check' to revalidate your input.</p>
            </c:otherwise>
          </c:choose>
        </c:when>
      </c:choose>
    </form>
    <c:if test="${not empty gerritContact}">
      <div>
        <a href="mailto:${gerritContact}">Gerrit Contacts</a>
      </div>
    </c:if>
  </div>
  <script>
    function submitWithAction(action) {
      var oForm = document.getElementById("gerritform");
      var formElements = oForm.elements;
      formElements.action.value = action;
      if (action === "serverSelected") {
        if (!formElements.groupMode[0].checked) {
          formElements.group.value = "";
          formElements.groupMode[0].checked = true;
        }
        formElements.parent.value = "";
        formElements.parentMode[0].checked = true;
        formElements.action.value = "refresh";
      }
      oForm.submit();
    }
  </script>
</body>
</html>
