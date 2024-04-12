<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Title</title>
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
    LAT: <input id="lat">
    LNT: <input id="lnt">
    <button onclick="getLocation()">내 위치 가져오기</button>
    <button onclick="getWifiInfo()">근처 WIFI 정보 보기</button><br><br>

    <table id="tb_wifi">
        <tr>
            <th>거리(Km)</th>
            <th>관리번호</th>
            <th>자치구</th>
            <th>와이파이명</th>
            <th>도로명주소</th>
            <th>상세주소</th>
            <th>설치위치(층)</th>
            <th>설치유형</th>
            <th>설치기관</th>
            <th>서비스구분</th>
            <th>망종류</th>
            <th>설치년도</th>
            <th>실내외구분</th>
            <th>WIFI접속환경</th>
            <th>X좌표</th>
            <th>Y좌표</th>
            <th>작업일자</th>
        </tr>
        <tr id="no-content">
            <td colspan="17">위치 정보를 입력한 후에 조회해 주세요.</td>
        </tr>
    </table>
</body>
<script>
    function getLocation() {
        navigator.geolocation.getCurrentPosition((pos)=>{
            $("#lat").val(pos.coords.latitude);
            $("#lnt").val(pos.coords.longitude);
        })
    }

    function getWifiInfo() {
        var coords = {
            lat: $("#lat").val(),
            lnt: $("#lnt").val()
        }

        $.ajax({
            url: "/wifi",
            type: "get",
            contentType: "application/json; charset-utf-8",
            data: coords,
            dataType: "json",
            success: function(data) {
                $("#no-content").remove();
                $(".tb").remove();

                for (i in data.list) {
                    // 테이블에 와이파이 리스트 행 추가
                    var wifiObj = JSON.parse(data.list[i]);
                    var query = "?no="+wifiObj.X_SWIFI_MGR_NO+"&dist="+wifiObj.distance;
                    var td = "<tr class=\"tb\"><td>"+wifiObj.distance+"</td>" +
                        "<td>"+wifiObj.X_SWIFI_MGR_NO+"</td>"+
                        "<td>"+wifiObj.X_SWIFI_WRDOFC+"</td>"+
                        "<td><a href=\"/wifiInfo"+query+"\">"+
                        wifiObj.X_SWIFI_MAIN_NM+"</a></td>"+
                        "<td>"+wifiObj.X_SWIFI_ADRES1+"</td>"+
                        "<td>"+wifiObj.X_SWIFI_ADRES2+"</td>"+
                        "<td>"+wifiObj.X_SWIFI_INSTL_FLOOR+"</td>"+
                        "<td>"+wifiObj.X_SWIFI_INSTL_TY+"</td>"+
                        "<td>"+wifiObj.X_SWIFI_INSTL_MBY+"</td>"+
                        "<td>"+wifiObj.X_SWIFI_SVC_SE+"</td>"+
                        "<td>"+wifiObj.X_SWIFI_CMCWR+"</td>"+
                        "<td>"+wifiObj.X_SWIFI_CNSTC_YEAR+"</td>"+
                        "<td>"+wifiObj.X_SWIFI_INOUT_DOOR+"</td>"+
                        "<td>"+wifiObj.X_SWIFI_REMARS3+"</td>"+
                        "<td>"+wifiObj.LAT+"</td>"+
                        "<td>"+wifiObj.LNT+"</td>"+
                        "<td>"+wifiObj.WORK_DTTM+"</td></tr>"
                    $("#tb_wifi").append(td);
                }
            }
        });
    }
</script>
</html>
