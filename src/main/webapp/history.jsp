<%@ page import="java.util.*" %>
<%@ page import="com.example.mission1.vo.History" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>와이파이 정보 구하기</title>
    <link href="css/table.css" rel="stylesheet" type="text/css">
    <script src="https://code.jquery.com/jquery-3.7.1.js"></script>
</head>
<body>
  <h1>와이파이 정보 구하기</h1>
  <a href="/">홈</a> |
  <a href="/history">위치 히스토리 목록</a> |
  <a href="/api">Open API 와이파이 정보 가져오기</a> |
  <a href="/bookmark/view">북마크 보기</a> |
  <a href="/bookmark/manage">북마크 그룹 관리</a><br>
  <%
    List<History> list = (List<History>) request.getAttribute("historyList");
  %>
  <table>
    <tr>
      <th>ID</th>
      <th>X좌표</th>
      <th>Y좌표</th>
      <th>조회일자</th>
      <th>비고</th>
    </tr>
    <%
      for (History hist : list) {
    %>
      <tr id="<%=hist.getId()%>">
        <td><%=hist.getId()%></td>
        <td><%=hist.getLat()%></td>
        <td><%=hist.getLnt()%></td>
        <td><%=hist.getDt()%></td>
        <td><button onclick="delBt(<%=hist.getId()%>)">삭제</button></td>
      </tr>
    <%
      }
    %>
  </table>
</body>
<script>
    function delBt(id) {
      $.ajax({
        url: "/delHistory",
        type: "get",
        contentType: "application/json; charset-utf-8",
        data: {"id": id},
        success: function () {
          $("#" + id).remove();
        }
      });
    }
</script>
</html>
