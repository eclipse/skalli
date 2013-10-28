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
<%@ page import="org.eclipse.skalli.view.Consts" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>${html:escapeHtml(project.name)} - ${html:escapeHtml(pagetitle)}</title>
<link rel="alternate" type="application/rss+xml" title="Latest changes to ${html:escapeHtml(project.name)}"
      href="${webLocator}/projects/api/${projectId}/timeline">
<style type="text/css">
@import "<%=Consts.JSP_STYLE%>";
</style>
</head>
<body>
<script type="text/javascript">
//<![CDATA[
if(!vaadin || !vaadin.vaadinConfigurations) {
if(!vaadin) { var vaadin = {}}
vaadin.vaadinConfigurations = {};
if (!vaadin.themesLoaded) { vaadin.themesLoaded = {}; }
vaadin.debug = true;
document.write('<iframe tabIndex="-1" id="__gwt_historyFrame" style="position:absolute;width:0;height:0;border:0;overflow:hidden;" src="javascript:false"></iframe>');
document.write("<script language='javascript' src='/VAADIN/widgetsets/com.vaadin.terminal.gwt.DefaultWidgetSet/com.vaadin.terminal.gwt.DefaultWidgetSet.nocache.js'><\/script>");
}
vaadin.vaadinConfigurations["project"] = {appUri:'${appUri}', windowName: '${windowName}', themeUri:'/VAADIN/themes/simple', versionInfo : {vaadinVersion:"6.4.0",applicationVersion:"NONVERSIONED"},"comErrMsg": {"caption":"Communication problem","message" : "Take note of any unsaved data, and <u>click here</u> to continue.","url" : null}};
//]]>
</script>
<script type="text/javascript">
//<![CDATA[
if(!vaadin.themesLoaded['simple']) {
var stylesheet = document.createElement('link');
stylesheet.setAttribute('rel', 'stylesheet');
stylesheet.setAttribute('type', 'text/css');
stylesheet.setAttribute('href', '/VAADIN/themes/simple/styles.css');
document.getElementsByTagName('head')[0].appendChild(stylesheet);
vaadin.themesLoaded['simple'] = true;
}
//]]>
</script>
<script type="text/javascript">
//<![CDATA[
setTimeout('if (typeof com_vaadin_terminal_gwt_DefaultWidgetSet == "undefined") {alert("Failed to load the widgetset: /VAADIN/widgetsets/com.vaadin.terminal.gwt.DefaultWidgetSet/com.vaadin.terminal.gwt.DefaultWidgetSet.nocache.js")};',15000);
//]]>
</script>
<script type="text/javascript" src="<%=Consts.TOGGLE_JS%>"></script>


<%-- header area --%>
<jsp:include page="<%=Consts.JSP_HEADER%>" flush="true" />
<jsp:include page="<%=Consts.JSP_HEADER_SEARCH%>" flush="true" />

<%-- navigation menu on left side --%>
<jsp:include page="<%=Consts.JSP_NAVIGATIONBAR%>" flush="true" />

<%-- project details --%>
<div class="projectarearight">
    <%-- project issues --%>
    <jsp:include page="<%=Consts.JSP_ISSUES%>" flush="true" />

    <%-- project header --%>
    <c:if test="${project!=null && editmode==false}">
        <div class="projectheader">
            <c:choose>
                <c:when test="${nature == 'PROJECT'}">
                    <img src="/VAADIN/themes/simple/icons/nature/project32x32.png" alt="projectnature" title="Project" />
                </c:when>
                <c:when test="${nature == 'COMPONENT'}">
                    <img src="/VAADIN/themes/simple/icons/nature/component32x32.png" alt="componentnature"
                        title="Component" />
                </c:when>
                <c:otherwise>
                    <img src="/VAADIN/themes/simple/icons/nature/none32x32.png" alt="error" />
                </c:otherwise>
            </c:choose>
            ${html:clean(project.name)}
            <c:if test="${user!=null}">
                <c:choose>
                    <c:when test="${favorites[projectUUID] != null}">
                        <a id="a_${projectUUID}" class="favicon" href="javascript:toggleFavorite('${projectUUID}');"
                            title="Remove from My Favorites"> <img class="favorite" id="img_${projectUUID}"
                            src="/VAADIN/themes/simple/icons/button/fav_yes.png" alt="favorite" /> </a>
                    </c:when>
                    <c:otherwise>
                        <a id="a_${projectUUID}" class="favicon" href="javascript:toggleFavorite('${projectUUID}');"
                            title="Add to My Favorites"> <img class="favorite" id="img_${projectUUID}"
                            src="/VAADIN/themes/simple/icons/button/fav_no.png" alt="nofavorite" /> </a>
                    </c:otherwise>
                </c:choose>
            </c:if>
            <c:if test="${project.deleted}">
                <br />
                <span class="deletedmessage">Project is DELETED.</span>
            </c:if>
        </div>
    </c:if>

    <%-- info boxes --%>
    <center>
        <div id="project" class="v-app v-app-loading v-theme-simple v-app-ProjectApplication"></div>
    </center>
</div>

</body>
</html>
