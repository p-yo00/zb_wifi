<%@ page import="com.example.mission1.vo.BookmarkWifi" %>
<%@ page import="java.util.*"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>북마크 목록</title>
    <link href="/css/table.css" rel="stylesheet" type="text/css">
</head>
<body>
<%
    List<BookmarkWifi> bookmarkList = (List<BookmarkWifi>) request.getAttribute("bookmarkWifi");
%>
    <h1>북마크 목록</h1>
    <a href="/">홈</a> |
    <a href="/history">위치 히스토리 목록</a> |
    <a href="/api">Open API 와이파이 정보 가져오기</a> |
    <a href="/bookmark/view">북마크 보기</a> |
    <a href="/bookmark/manage">북마크 그룹 관리</a><br><br>

    <table>
        <tr>
            <th>ID</th>
            <th>북마크 이름</th>
            <th>와이파이명</th>
            <th>등록일자</th>
            <th>비고</th>
        </tr>
        <%
            for (BookmarkWifi bmWifi : bookmarkList) {
                String query = "/wifiInfo?no="+bmWifi.getWifiId()+"&dist=0";
        %>
            <tr>
                <td><%=bmWifi.getId()%></td>
                <td><%=bmWifi.getBookmarkNm()%></td>
                <td><a href="<%=query%>"><%=bmWifi.getWifiNm()%></td>
                <td><%=bmWifi.getCreatedDt()%></td>
                <td><a href="/bookmark/delete?id=<%=bmWifi.getId()%>">삭제</a></td>
            </tr>
        <%
            }
        %>
    </table>
</body>
</html>
