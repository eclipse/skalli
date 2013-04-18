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
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<div class="searchheader">
  <div class="searchheader-left">
    <a href="/"> <img src="/VAADIN/themes/simple/images/logo_medium.png" alt="Logo" />
    </a>
  </div>
  <div class="searchheader-right">
    <form method="get" id="searchform" action="/projects">
      <c:choose>
        <c:when test="${query != null }">
          <input type="text" value="${html:escapeHtml(query)}" name="query" id="query"
            class="searchfield" />
        </c:when>
        <c:when test="${tagquery != null }">
          <input type="text" value="${html:escapeHtml(tagquery)}" name="query" id="query"
            class="searchfield" />
        </c:when>
        <c:when test="${userquery != null }">
          <input type="text" value="${html:escapeHtml(userquery)}" name="query"
            id="query" class="searchfield" />
        </c:when>
        <c:otherwise>
          <input type="text" value="" name="query" id="query"
            class="searchfield" />
        </c:otherwise>
      </c:choose>
      <input type="submit" value="Search" id="searchsubmit"
        class="searchsubmit" />
    </form>
    <c:choose>
      <c:when test="${query != null }">${resultSize} projects found for '${html:escapeHtml(query)}' in ${duration} ms</c:when>
      <c:when test="${tagquery != null }">${resultSize} projects found for '${html:escapeHtml(tagquery)}' in ${duration} ms</c:when>
      <c:when test="${userquery != null }">${resultSize} projects found for '${html:escapeHtml(userquery)}' in ${duration} ms</c:when>
      <c:otherwise />
    </c:choose>
  </div>
</div>

