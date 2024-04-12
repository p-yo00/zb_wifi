<%@ page import="com.example.mission1.vo.BookmarkWifi" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>북마크 삭제</title>
    <link href="/css/table.css" rel="stylesheet" type="text/css">
</head>
<body>
<%
    BookmarkWifi bmWifi = (BookmarkWifi) request.getAttribute("bookmarkWifi");
%>
    <h1>북마크 삭제</h1>
    <a href="/">홈</a>|
    <a href="/history">위치 히스토리 목록</a>|
    <a href="/api">Open API 와이파이 정보 가져오기</a>
    <a href="/bookmark/view">북마크 보기</a>
    <a href="/bookmark/manage">북마크 그룹 관리</a>
    <br>
    <p>북마크를 삭제하시겠습니까?</p>

    <form method="post" action="/bookmark/delete">
        <input name="id" value="<%=bmWifi.getId()%>" hidden="hidden">
    <table>
        <tr>
            <th>북마크 이름</th>
            <td><%=bmWifi.getBookmarkNm()%></td>
        </tr>
        <tr>
            <th>와이파이명</th>
            <td><%=bmWifi.getWifiNm()%></td>
        </tr>
        <tr>
            <th>등록일자</th>
            <td><%=bmWifi.getCreatedDt()%></td>
        </tr>
        <tr>
            <td colspan="2">
                <a href="/bookmark/view">돌아가기</a>|
                <button>삭제</button>
            </td>
        </tr>
    </table>
    </form>
</body>
</html>
