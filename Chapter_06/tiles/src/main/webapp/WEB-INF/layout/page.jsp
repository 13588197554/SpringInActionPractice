<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="t" %>
<%@ page session="false" %>
<html>
  <head>
    <title>Spittr</title>
    <link rel="stylesheet" 
          type="text/css" 
          href="<s:url value="/resources/style.css" />" >
  </head>
  <body>
    <div id="header">
      <t:insertAttribute name="header" />         <%--插入头部--%>
    </div>
    <div id="content">
      <t:insertAttribute name="body" />               <%--插入主题内容--%>
    </div>
    <div id="footer">
      <t:insertAttribute name="footer" />              <%--插入底部--%>
    </div>
  </body>
</html>
