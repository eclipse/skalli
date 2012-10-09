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
<%@ page import="org.eclipse.skalli.services.Services" %>
<%@ page import="org.osgi.framework.Version" %>
<%@ page import="org.eclipse.skalli.view.Consts" %>

<div class="mainheader">
    <div class="mainheader-left">
       <c:forEach var="toplinkConfig" items="${toplinksConfig.topLinks}" >
         <a href ="${toplinkConfig.url}">${html:escapeHtml(toplinkConfig.displayName)}</a>
       </c:forEach>
    </div>
    <div class="mainheader-right">
      <div align="right">
        <c:choose>
          <c:when test="${user!=null}">
            Welcome
            <a href="<%=Consts.URL_MYPROJECTS%>">${html:escapeHtml(userDisplayName)}</a>
            <a href="/logout" onclick="javascript:document.getElementById('logoutinput').value=document.location.href;document.forms['logout'].submit()">(Logout)</a>
            <c:if test="${feedbackConfig != null }">
              <span class="vertical_separator"><img src="/VAADIN/themes/simple/images/separator.png" alt="separator"></span>
              <a href="${feedbackConfig.url}">${html:escapeHtml(feedbackConfig.displayName)}</a>
            </c:if>
          </c:when>
          <c:otherwise>
            Anonymous User <a href="/" onclick="javascript:document.getElementById('logininput').value=document.location.href;document.forms['login'].submit()">(Login)</a>
          </c:otherwise>
        </c:choose>
        <c:choose>
          <c:when test="${newsConfig != null}">
            <span class="vertical_separator"><img src="/VAADIN/themes/simple/images/separator.png" alt="separator"></span>
            <a href="${newsConfig.url}">Version <%= Services.API_VERSION.getQualifier() %></a>
          </c:when>
          <c:otherwise>
            <span class="vertical_separator"><img src="/VAADIN/themes/simple/images/separator.png" alt="separator"></span>
            Version <%= Services.API_VERSION.getQualifier() %>
          </c:otherwise>
        </c:choose>
      </div>
    </div>
</div>

<form id="login" method="get" action="/authenticate">
<input id="logininput" type="hidden" name="returnUrl" />
</form>
<form id="logout" method="get" action="/logout">
<input id="logoutinput" type="hidden" name="returnUrl" />
</form>

