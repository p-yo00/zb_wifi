<%@ page import="com.example.mission1.vo.Bookmark" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>북마크 그룹</title>
    <link href="/css/table.css" rel="stylesheet" type="text/css">
    <script src="https://code.jquery.com/jquery-3.7.1.js"></script>
</head>
<body>
    <h1>북마크 그룹</h1>
    <a href="/">홈</a> |
    <a href="/history">위치 히스토리 목록</a> |
    <a href="/api">Open API 와이파이 정보 가져오기</a> |
    <a href="/bookmark/view">북마크 보기</a> |
    <a href="/bookmark/manage">북마크 그룹 관리</a>
    <%
        List<Bookmark> bookmarks = (ArrayList<Bookmark>)request.getAttribute("bookmarkGroup");
    %>
    <br>
    <button onclick="location.href='/add-bookmark.jsp'">북마크 그룹 이름 추가</button><br><br>
    <table>
        <tr>
            <th>ID</th>
            <th>북마크 이름</th>
            <th>순서</th>
            <th>등록일자</th>
            <th>수정일자</th>
            <th>비고</th>
        </tr>
        <%
            for (Bookmark bm : bookmarks) {
        %>
        <tr id="<%=bm.getId()%>">
            <td><%=bm.getId()%></td>
            <td><%=bm.getName()%></td>
            <td><%=bm.getOrder()%></td>
            <td><%=bm.getCreatedDt()%></td>
            <td><%=bm.getModifiedDt()%></td>
            <td>
                <a href="/bmGroup/modify?id=<%=bm.getId()%>">수정</a>
                <button onclick="delBt(<%=bm.getId()%>)">삭제</button>
            </td>
        </tr>
        <%
            }
        %>
    </table>
</body>
<script>
    function delBt(id) {
        $.ajax({
            url: "/delBookmark",
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
