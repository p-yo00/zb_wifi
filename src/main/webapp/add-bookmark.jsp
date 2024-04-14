<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>북마크 그룹 추가</title>
    <link href="/css/table.css" rel="stylesheet" type="text/css">
</head>
<body>
    <h1>북마크 그룹 추가</h1>
    <a href="/">홈</a>|
    <a href="/history">위치 히스토리 목록</a>|
    <a href="/api">Open API 와이파이 정보 가져오기</a>
    <a href="/bookmark/view">북마크 보기</a>
    <a href="/bookmark/manage">북마크 그룹 관리</a><br><br>

    <form action="/bookmark/add" method="post" accept-charset="UTF-8">
        <table>
            <tr>
                <th>북마크 이름</th>
                <td><input id="name" name="name"></td>
            </tr>
            <tr>
                <th>순서</th>
                <td><input id="order" name="order"></td>
            </tr>
            <tr>
                <td colspan="2"><button>추가</button></td>
            </tr>
        </table>
    </form>
</body>

<script>

</script>
</html>
