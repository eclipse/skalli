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
<%@ taglib tagdir="/WEB-INF/tags" prefix="pp" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="org.eclipse.skalli.view.Consts" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>${html:escapeHtml(title)} - ${html:escapeHtml(pagetitle)}</title>
<style type="text/css">
@import "<%=Consts.JSP_STYLE%>";
</style>

<script type="text/javascript">
    function pageResults(targetPage) {
      var start_at = (targetPage - 1) * ${viewSize};
      if(window.location.search == "") {
        window.location.href = window.location.href + "?start="+start_at;
      } else {
        if(location.href.indexOf("start=") > 1) {
          window.location.href = window.location.href.replace(/start=[^&]+/, "start="+start_at);
        } else {
          window.location.href = window.location.href + "&start="+start_at;
        }
      }
    }

    function resultsPerPage(count) {
        if(window.location.search == "") {
          window.location.href = window.location.href + "?count="+count;
        } else {
          if(location.href.indexOf("count=") > 1) {
            window.location.href = window.location.href.replace(/count=[^&]+/, "count="+count);
          } else {
            window.location.href = window.location.href + "&count="+count;
          }
        }
      }
</script>

<script type="text/javascript" src="<%=Consts.TOGGLE_JS%>"></script>

</head>
<body class="searchresult">

<%-- for test purposes
viewsize = ${viewSize} @ start = ${start} @ currentPage = ${currentPage} @ pages = ${pages}
 --%>

<%-- header area --%>
<jsp:include page="<%=Consts.JSP_HEADER%>" flush="true" />
<jsp:include page="<%=Consts.JSP_HEADER_SEARCH%>" flush="true" />

<%-- navigation menu on left side --%>
<jsp:include page="<%=Consts.JSP_NAVIGATIONBAR%>" flush="true" />

<%-- search results and pagination --%>
<div class="rightarea">
    <c:if test="${pages > 1}">
        <div class="resultsperpage">
            Results per page:
            <a class="${10 == viewSize ? 'selected' : 'navigator'}" href="javascript:resultsPerPage(10);">[10]</a>
            <a class="${25 == viewSize ? 'selected' : 'navigator'}" href="javascript:resultsPerPage(25);">[25]</a>
            <a class="${50 == viewSize ? 'selected' : 'navigator'}" href="javascript:resultsPerPage(50);">[50]</a>
        </div>
    </c:if>

    <c:forEach var="project" items="${projects}" >
        <%-- render the project details, see /search/tags/project-details.tag --%>
        <c:set var="uuid" value="${project.singleValues['uuid']}" />
        <pp:project-details project="${project}" parentChain="${parentChains[uuid]}"
            srcLinks="${sourceLinks[uuid]}" style="project" />
    </c:forEach>

    <%-- page navigator --%>
    <center>
        <div class="pagination">
            <c:if test="${pages > 1}">
                <c:if test="${currentPage > 1}">
                    <a class="navigator" href="javascript:pageResults('1');">|&lt;</a>
                    <a class="navigator" href="javascript:pageResults('${currentPage - 1}');">&lt;&lt;</a>
                </c:if>

                <c:forEach var="i" begin="1" end="${pages}">
                    <c:if test="${i == currentPage}">
                        <a class="currentpage" href="javascript:pageResults('${i}');">${i}</a>
                    </c:if>
                    <c:if test="${i != currentPage}">
                        <a href="javascript:pageResults('${i}');">${i}</a>
                    </c:if>
                </c:forEach>

                <c:if test="${currentPage < pages}">
                    <a class="navigator" href="javascript:pageResults('${currentPage + 1}');">&gt;&gt;</a>
                    <a class="navigator" href="javascript:pageResults('${pages}');">&gt;|</a>
                </c:if>
            </c:if>
        </div>
    </center>
</div>
</body>
</html>
