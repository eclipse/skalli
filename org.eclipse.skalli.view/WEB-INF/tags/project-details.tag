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

<%@ attribute name="project" type="org.eclipse.skalli.services.search.SearchHit" required="true" rtexprvalue="true" %>
<%@ attribute name="parentChain" type="java.util.List" required="false" rtexprvalue="true" %>
<%@ attribute name="style" required="true" rtexprvalue="true" %>

<c:set var="p" value="${pageScope.project}" />
<c:set var="uuid" value="${p.singleValues['uuid']}" />

<div class="${pageScope.style}">
    <div>
      <c:forEach var="parent" items="${parentChain}" >
        <a class="projectlinks" href='/projects/${parent.projectId}' target='_top'><c:out value="${parent.orConstructShortName}" /></a> &gt; 
      </c:forEach>
    </div>

    <c:choose>
        <c:when test="${natures[uuid] == 'PROJECT'}">
            <img class="natureicon" src="/VAADIN/themes/simple/icons/nature/project16x16.png" title="Project of People" />
        </c:when>
        <c:when test="${natures[uuid] == 'COMPONENT'}">
            <img class="natureicon" src="/VAADIN/themes/simple/icons/nature/component16x16.png" title="Component" />
        </c:when>
        <c:otherwise/>
    </c:choose>
    <a class="projectlink" href='/projects/${p.singleValues["projectId"]}' target='_top'>
        ${html:clean(p.singleValuesHighlighted["name"])}
    </a>

    <c:if test="${user != null}">
      <c:choose>
        <c:when test="${favorites[uuid] != null}">
          <a id="a_${uuid}" class="favicon" href="javascript:toggleFavorite('${uuid}');" title="Remove from My Favorites">
            <img class="favorite" id="img_${uuid}" src="/VAADIN/themes/simple/icons/button/fav_yes.png" alt='favorite' />
          </a>
        </c:when>
        <c:otherwise>
          <a id="a_${uuid}" class="favicon" href="javascript:toggleFavorite('${uuid}');" title="Add to My Favorites">
            <img class="favorite" id="img_${uuid}" src="/VAADIN/themes/simple/icons/button/fav_no.png" alt='nofavorite' />
          </a>
        </c:otherwise>
      </c:choose>
    </c:if>

    <div class="projectdetails">
        <c:if test="${p.singleValuesHighlighted['description'] != null}">
            ${html:clean(p.singleValuesHighlighted['description'])}
        </c:if>
        <div class="projectlinks">
            <c:if test="${p.singleValues['pageUrl'] != null}">
                <img class="linkicon" src="/VAADIN/themes/simple/icons/search/homepage.png" alt="Homepage" />
                <a href='${p.singleValues["pageUrl"]}' target='_top'>Homepage</a>
            </c:if>

            <c:if test="${p.singleValues['scmUrl'] != null}">
                <img class="linkicon" src="/VAADIN/themes/simple/icons/devinf/code.png" alt="Source" />
                <a href='${p.singleValues["scmUrl"]}' target='_top'>Source</a>
            </c:if>

            <c:if test="${p.singleValues['ciUrl'] != null}">
                <img class="linkicon" src="/VAADIN/themes/simple/icons/devinf/ci_server.png" alt="Build" />
                <a href='${p.singleValues["ciUrl"]}' target='_top'>Build</a>
            </c:if>

            <c:if test="${p.singleValues['metricsUrl'] != null}">
                <img class="linkicon" src="/VAADIN/themes/simple/icons/devinf/metrics.png" alt="Metrics" />
                <a href='${p.singleValues["metricsUrl"]}' target='_top'>Metrics</a>
            </c:if>

            <c:if test="${p.singleValues['bugtrackerUrl'] != null}">
                <img class="linkicon" src="/VAADIN/themes/simple/icons/devinf/bug.png" alt="Bug Tracker" />
                <a href='${p.singleValues["bugtrackerUrl"]}' target='_top'>Bug Tracker</a>
            </c:if>

            <c:if test="${parents[uuid] != null}">
                <img class="linkicon" src="/VAADIN/themes/simple/icons/search/projects.png" alt="Parent Project" />
                <a href="/projects/${parents[uuid].projectId}" target="_top">
                    Parent Project (${html:clean(parents[uuid].name)})
                </a>
            </c:if>
        </div>
        <div>
            <!-- render all project tags  -->
            <c:if test="${not empty p.multiValues['tags']}" >
                Tags:&nbsp;
                <c:forEach var="tag" items="${p.multiValues['tags']}" varStatus="status">
                    <a href="/projects?tag=${tag}" target="_top">${p.multiValuesHighlighted['tags'][status.index]}</a>
            </c:forEach>
            </c:if>
        </div>
        <div>
            <!-- render links to all subprojects -->
            <c:if test="${subprojects[uuid] != null && style != 'parentproject'}" >
                Subprojects:<br/>
                <div style="margin-left:10px">
                    <c:forEach var="child" items="${subprojects[uuid]}">
                        <span style="white-space:nowrap">
                            <img class="linkicon" src="VAADIN/themes/simple/icons/search/projects.png"
                                alt="Subproject" width="14px" height="14px" />
                            <a href="/projects/${child.singleValues['projectId']}" target="_top">
                                ${html:clean(child.singleValuesHighlighted['name'])}
                            </a>
                        </span>
                    </c:forEach>
                </div>
            </c:if>
        </div>
    </div>
</div>
