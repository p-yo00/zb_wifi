<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
    <style>
        body {
          margin: 0 auto
        }
    </style>
</head>
<body>
    <%
        int totalCount = (int) request.getAttribute("totalCount");
    %>
  <h1><%=totalCount%>개의 WIFI 정보를 정상적으로 저장하였습니다.</h1>
  <a href="/">홈 으로 가기</a>
</body>
</html>
