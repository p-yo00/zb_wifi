<%@ page import="com.example.mission1.vo.WifiInfo" %>
<%@ page import="com.example.mission1.vo.Bookmark" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<html>
<head>
    <title>와이파이 정보</title>
    <link href="/css/table.css" rel="stylesheet" type="text/css">
    <script src="https://code.jquery.com/jquery-3.7.1.js"></script>
</head>
<body>
<%
    Map<String, Object> map = (Map<String, Object>) request.getAttribute("param");

    WifiInfo wifi = (WifiInfo) map.get("wifiInfo");
    List<Bookmark> bookmarks = (List<Bookmark>)map.get("bookmarkGroup");
    System.out.println(map);
%>
    <h1>와이파이 정보 구하기</h1>
    <a href="/">홈</a> |
    <a href="/history">위치 히스토리 목록</a> |
    <a href="/api">Open API 와이파이 정보 가져오기</a> |
    <a href="/bookmark/view">북마크 보기</a> |
    <a href="/bookmark/manage">북마크 그룹 관리</a><br>
    <select id="bmGroup" name="bookmark">
        <option>북마크 그룹 이름 선택</option>
        <%
            for (Bookmark bm : bookmarks) {
        %>
        <option value="<%=bm.getId()%>"><%=bm.getName()%></option>
        <%
            }
        %>
    </select>
    <button onclick="addBookmark()">북마크 추가하기</button><br><br>
    <table>
        <tr>
            <th>거리(Km)</th>
            <td><%=wifi.getDistance()%></td>
        </tr>
        <tr>
            <th>관리번호</th>
            <td id="wifiNo"><%=wifi.getNo()%></td>
        </tr>
        <tr>
            <th>자치구</th>
            <td><%=wifi.getDistrict()%></td>
        </tr>
        <tr>
            <th>와이파이명</th>
            <td><%=wifi.getWifiNm()%></td>
        </tr>
        <tr>
            <th>도로명주소</th>
            <td><%=wifi.getAddr1()%></td>
        </tr>
        <tr>
            <th>상세주소</th>
            <td><%=wifi.getAddr2()%></td>
        </tr>
        <tr>
            <th>설치위치(층)</th>
            <td><%=wifi.getFloor()%></td>
        </tr>
        <tr>
            <th>설치유형</th>
            <td><%=wifi.getInstallType()%></td>
        </tr>
        <tr>
            <th>설치기관</th>
            <td><%=wifi.getInstitute()%></td>
        </tr>
        <tr>
            <th>서비스구분</th>
            <td><%=wifi.getServiceType()%></td>
        </tr>
        <tr>
            <th>망종류</th>
            <td><%=wifi.getNetworkType()%></td>
        </tr>
        <tr>
            <th>설치년도</th>
            <td><%=wifi.getYear()%></td>
        </tr>
        <tr>
            <th>실내외구분</th>
            <td><%=wifi.getInOutDoor()%></td>
        </tr>
        <tr>
            <th>WIFI접속환경</th>
            <td><%=wifi.getConnEnvir()%></td>
        </tr>
        <tr>
            <th>X좌표</th>
            <td><%=wifi.getLat()%></td>
        </tr>
        <tr>
            <th>Y좌표</th>
            <td><%=wifi.getLnt()%></td>
        </tr>
        <tr>
            <th>작업일자</th>
            <td><%=wifi.getWorkDt()%></td>
        </tr>
    </table>
</body>
<script>
    function addBookmark() {
        var data = {
            "bookmarkId": $("select[name=bookmark]").val(),
            "wifiId": $("#wifiNo").text()
        }
        console.log(data);
        $.ajax({
            url: "/add/bmWifi",
            type: "get",
            contentType: "application/json; charset-utf-8",
            data: data,
            success: function () {
                alert("북마크 추가 완료");
            }
        });
    }
</script>
</html>
