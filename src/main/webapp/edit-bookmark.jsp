<%@ page import="com.example.mission1.vo.Bookmark" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>북마크 그룹 수정</title>
    <link href="/css/table.css" rel="stylesheet" type="text/css">
</head>
<body>
<%
    Bookmark bookmark = (Bookmark) request.getAttribute("bookmark");
%>
    <h1>북마크 그룹 수정</h1>
    <a href="/">홈</a>|
    <a href="/history">위치 히스토리 목록</a>|
    <a href="/api">Open API 와이파이 정보 가져오기</a>
    <a href="/bookmark/view">북마크 보기</a>
    <a href="/bookmark/manage">북마크 그룹 관리</a>

    <form action="/bmGroup/modify" method="post">
        <input name="id" value="<%=bookmark.getId()%>" hidden="hidden">
    <table>
        <tr>
          <th>북마크 이름</th>
          <td><input name="name" value="<%=bookmark.getName()%>"></td>
        </tr>
        <tr>
          <th>순서</th>
          <td><input name="order" value="<%=bookmark.getOrder()%>"></td>
        </tr>
        <tr>
            <td colspan="2">
                <a href="/bookmark/manage">돌아가기</a> |
                <button>수정</button>
            </td>
        </tr>
    </table>
    </form>
</body>
</html>
