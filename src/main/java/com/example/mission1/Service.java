package com.example.mission1;

import com.example.mission1.vo.Bookmark;
import com.example.mission1.vo.BookmarkWifi;
import com.example.mission1.vo.History;
import com.example.mission1.vo.WifiInfo;
import com.google.gson.*;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.sql.*;
import java.util.*;

/**
 *  작성자: 박예온
 *  date: 24-04-14
 *  서블릿에서 실행하게 되고 주로 db 관련 작업을 수행하는 클래스
 */
public class Service {
    // 프로젝트의 경로를 적어주어야 함!
    private final String projectPath = "E:/Users/tmshd/Documents/제로베이스/Mission1";

    private Connection conn = null;
    private PreparedStatement pstmt = null;
    private ResultSet rs = null;

    /**
     *  db connection 생성
     */
    private void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + projectPath + "/identifier.sqlite");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *  db connection, PreparedStatement, ResultSet 연결 해제
     */
    private void disconnect() {
        try {
            if (conn != null) {
                conn.close();
            }
            if (pstmt != null) {
                pstmt.close();
            }
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     *  obj에 저장된 값들을 order순서로 PreparedStatement에 set한다. = pstmt.setInt(1, obj.get(""))
     */
    private void setPstmt(Object obj, String[] order) {
        Field[] fields = obj.getClass().getDeclaredFields();
        HashMap<String, Object> map = new HashMap<>();

        // 먼저 Map에 필드의 이름을 key, 값을 value로 저장
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                map.put(field.getName(), field.get(obj));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        for (int i=0; i<order.length; i++) {
            Object o = map.get(order[i]);
            try {
                if (o.getClass() == String.class) {
                    pstmt.setString(i+1, (String) o);
                } else if (o.getClass() == Integer.class) {
                    pstmt.setInt(i+1, (int) o);
                } else if (o.getClass() == Double.class) {
                    pstmt.setDouble(i+1, (double) o);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    /**
     *  url로 http 접속을 하고 key가 path인 JsonObject를 리턴한다.
     */
    private JsonObject httpApi(String url, String path) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url.toString()).build();
        Response response = client.newCall(request).execute();
        String json = response.body().string();

        JsonObject jsonObj = ((JsonObject) JsonParser.parseString(json)).get(path).getAsJsonObject();

        return jsonObj;
    }

    /**
     *  history 테이블에서 전체 데이터를 가져와서 List 리턴
     */
    public List<History> getHistory() {
        List<History> list = new ArrayList<>();
        connect();
        try {
            rs = conn.prepareStatement("select * from history order by id desc").executeQuery();
            while (rs.next()) {
                History history = (History) resultSetToObject(new History());
                list.add(history);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
        return list;
    }

    /**
     *  wifi 데이터를 open api로 가져오고 db에 insert 한 후, 총 가져온 갯수를 리턴
     */
    public int getWifiApi() throws IOException {
        // API 가져오기
        StringBuffer url = new StringBuffer();
        Properties prop = new Properties();
        String key;
        try {
            prop.load(new FileInputStream(projectPath+"/key.properties"));
            key = (String) prop.get("key");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        url.append("http://openapi.seoul.go.kr:8088/")
                .append(key)
                .append("/json/TbPublicWifiInfo/1/5/");
        JsonObject jsonObj = httpApi(url.toString(), "TbPublicWifiInfo");
        int totalCount = jsonObj.get("list_total_count").getAsInt(); // 데이터 전체 갯수

        ArrayList<WifiInfo> wifiInfoList = new ArrayList<>();
        for (int i=1; i<=totalCount; i+=1000) { // 천개씩 호출해서 List 저장
            url = new StringBuffer();
            url.append("http://openapi.seoul.go.kr:8088/")
                    .append(key)
                    .append("/json/TbPublicWifiInfo/")
                    .append(i+"/")
                    .append(i+999);

            jsonObj = httpApi(url.toString(), "TbPublicWifiInfo");
            JsonArray jsonArr = (JsonArray) jsonObj.get("row");

            for (JsonElement item : jsonArr) { // json 문자열을 wifiInfo 클래스로 바꿔서 List 저장
                String jsonStr = item.getAsJsonObject().toString();
                WifiInfo wifiInfo = new Gson().fromJson(jsonStr, WifiInfo.class);
                if (wifiInfo != null) {
                    wifiInfoList.add(wifiInfo);
                }
            }
        }
        // 가져온 데이터를 DB에 insert
        connect();
        try {
            conn.setAutoCommit(false); // 시간 단축을 위해 insert 전체를 트랜잭션으로 실행
            int batchSize = 10000;
            int cur = 0;

            pstmt = conn.prepareStatement
                    ("insert or ignore into wifi_info " +
                            "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            for (WifiInfo wifiInfo : wifiInfoList) {
                cur++;
                // 배열 컬럼 순서대로 wifiInfo의 값을 pstmt에 set
                setPstmt(wifiInfo, new String[]{"no","district","wifiNm","addr1","addr2",
                        "floor","installType","institute","serviceType","networkType",
                        "year","inOutDoor","connEnvir","lat","lnt","workDt"});

                pstmt.addBatch(); // 만개 단위로 insert
                pstmt.clearParameters();
                if (cur%batchSize == 0) {
                    pstmt.executeBatch();
                    pstmt.clearBatch();
                }
            }
            pstmt.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }

        return totalCount;
    }

    /**
     *  resultSet 값을 obj의 필드에 set한다. = obj.set##(rs.getInt("##"))
     */
    private Object resultSetToObject(Object obj) {
        Field[] fields = obj.getClass().getDeclaredFields();

        try {
            for (Field field : fields) {
                field.setAccessible(true);
                if (rs.getObject(field.getName()) == null) continue; // 필드값이 null이면 넣지 않음
                field.set(obj, rs.getObject(field.getName()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    /**
     *  lat, lnt를 비교해 거리 순으로 가까운 n개의 wifi를 가져와서 jsonArr 리턴
     */
    public JsonObject getNearNWifi(int n, double lat, double lnt) {
        connect();
        // 하버사인 공식으로 위경도를 비교한다.
        String sql = "select *, (6371*acos(cos(radians(lat))*cos(radians(?))*cos(radians(lnt)" +
                "-radians(?))+sin(radians(lat))*sin(radians(?))))" +
                "AS distance FROM wifi_info " +
                "ORDER BY distance " +
                "Limit "+n;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setDouble(1, lat);
            pstmt.setDouble(2, lnt);
            pstmt.setDouble(3, lat);

            rs = pstmt.executeQuery();
            ArrayList<WifiInfo> list = new ArrayList<>();
            JsonArray jsonArr = new JsonArray();
            while (rs.next()) {
                WifiInfo wifiInfo = (WifiInfo) resultSetToObject(new WifiInfo());

                list.add(wifiInfo);
                jsonArr.add(new Gson().toJson(wifiInfo));
            }

            // 조회 기록 (history) 테이블에 추가
            pstmt = conn.prepareStatement("insert into history(lat, lnt)" +
                    "values(?,?)");
            pstmt.setDouble(1, lat);
            pstmt.setDouble(2, lnt);
            pstmt.executeUpdate();

            JsonObject jsonObj = new JsonObject();

            jsonObj.add("list", jsonArr);
            jsonObj.addProperty("totalCount", jsonArr.size());

            return jsonObj;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
        return null;
    }

    /**
     *  query 문자열 (key=value&)에서 key와 value를 Map으로 변환
     */
    private Map<String, Object> getParameterMap(String queryStr) {
        String[] queries = queryStr.split("&");
        Map<String, Object> map = new HashMap<>();

        for (String query : queries) {
            String[] keyValue = query.split("=");
            try {
                map.put(keyValue[0], URLDecoder.decode(keyValue[1], "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        return map;
    }

    /**
     *  primary key로 특정 wifi 상세 정보를 조회한다.
     */
    public WifiInfo getWifiInfo(String queryStr) {
        Map<String, Object> queryMap = getParameterMap(queryStr);
        connect();
        WifiInfo wifiInfo = null;
        System.out.println(queryMap.get("no"));
        try {
            String sql = "select * from wifi_info where no=?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, (String) queryMap.get("no"));
            rs = pstmt.executeQuery();
            if (rs.next()) {
                wifiInfo = (WifiInfo) resultSetToObject(new WifiInfo());
                wifiInfo.setDistance(Double.parseDouble((String)queryMap.get("dist")));
                System.out.println(wifiInfo.getDistance());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
        return wifiInfo;
    }

    /**
     *  조회 내역 (history)에서 id에 해당하는 데이터를 삭제한다.
     */
    public void deleteHistory(int id) {
        connect();
        try {
            pstmt = conn.prepareStatement("delete from history where id=?");
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }

    /**
     *  bookmark 테이블에 데이터를 추가한다.
     */
    public void addBookmarkGroup(String name, int order) {
        connect();
        try {
            pstmt = conn.prepareStatement("insert into bookmark(name,\"order\",modifiedDt) values(?,?,\"\")");
            pstmt.setString(1, name);
            pstmt.setInt(2, order);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }

    /**
     *  모든 bookmark를 order 순으로 가져온다.
     */
    public List<Bookmark> getBookmarkGroup() {
        connect();
        List<Bookmark> list = new ArrayList<>();
        try {
            pstmt = conn.prepareStatement("select * from bookmark order by \"order\"");
            rs = pstmt.executeQuery();
            while(rs.next()) {
                Bookmark bookmark = (Bookmark) resultSetToObject(new Bookmark());
                list.add(bookmark);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
        return list;
    }

    /**
     *  queryStr에서 bookmark id와 wifi id를 가져와 bookmark에 wifi를 저장한다.
     */
    public void addBookmarkWifi(String queryStr) {
        Map<String, Object> map = getParameterMap(queryStr);
        connect();
        try {
            pstmt = conn.prepareStatement("insert into bookmark_wifi(bookmarkId, wifiId) values(?,?)");
            pstmt.setInt(1, Integer.parseInt((String) map.get("bookmarkId")));
            pstmt.setString(2, (String) map.get("wifiId"));
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }

    /**
     *  bookmark에 저장된 wifi 정보를 가져온다.
     */
    public List<BookmarkWifi> getBookmarkWifi() {
        List<BookmarkWifi> list = new ArrayList<>();
        connect();

        // bookmark id와 wifi id를 이용해 bookmark 테이블과 wifi 테이블을 조인해서 name을 가져온다.
        String sql = "select bw.id, bookmarkId, wifiId, name as bookmarkNm, wifiNm, bw.createdDt" +
                " from bookmark_wifi bw" +
                " inner join bookmark b on bw.bookmarkId = b.id" +
                " inner join wifi_info w on bw.wifiId = w.no";
        try {
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                BookmarkWifi bmWifi = (BookmarkWifi) resultSetToObject(new BookmarkWifi());
                list.add(bmWifi);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
        return list;
    }

    /**
     *  id로 bookmark에 저장된 와이파이의 상세정보를 조회한다.
     */
    public BookmarkWifi getBookmarkWifiById(String queryStr) {
        Map<String, Object> map = getParameterMap(queryStr);
        BookmarkWifi bmWifi = null;
        connect();

        String sql = "select bw.id, bookmarkId, wifiId, name as bookmarkNm, wifiNm, bw.createdDt" +
                " from bookmark_wifi bw" +
                " inner join bookmark b on bw.bookmarkId = b.id" +
                " inner join wifi_info w on bw.wifiId = w.no" +
                " where bw.id=?";
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, Integer.parseInt((String)map.get("id")));
            rs = pstmt.executeQuery();
            bmWifi = (BookmarkWifi) resultSetToObject(new BookmarkWifi());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
        return bmWifi;
    }

    /**
     *  bookmark에 저장된 wifi를 삭제한다.
     */
    public void deleteBookmarkWifi(int id) {
        connect();
        try {
            pstmt = conn.prepareStatement("delete from bookmark_wifi where id=?");
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }

    /**
     *  id로 특정 bookmark 정보를 가져온다.
     */
    public Bookmark getBookmarkById(String queryStr) {
        Map<String, Object> map = getParameterMap(queryStr);
        connect();
        Bookmark bookmark = null;
        try {
            pstmt = conn.prepareStatement("select * from bookmark where id=?");
            pstmt.setInt(1, Integer.parseInt((String)map.get("id")));
            rs = pstmt.executeQuery();
            bookmark = (Bookmark) resultSetToObject(new Bookmark());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
        return bookmark;
    }

    /**
     *  id에 해당하는 bookmark를 삭제한다.
     */
    public void deleteBookmark(int id) {
        connect();
        try {
            pstmt = conn.prepareStatement("delete from bookmark where id=?");
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }

    /**
     *  bookmark의 name, order를 수정하고 수정날짜를 현재 시간으로 저장한다.
     */
    public void updateBookmark(Bookmark bookmark) {
        connect();
        String sql = "update bookmark " +
                "set name=?, \"order\"=?," +
                "modifiedDt=datetime('now', 'localtime') " +
                "where id=?";
        try {
            pstmt = conn.prepareStatement(sql);
            setPstmt(bookmark, new String[]{"name","order","id"});
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }
}
