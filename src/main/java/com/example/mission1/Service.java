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
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

public class Service {
    private final String sqlitePath = "E:/Users/tmshd/Documents/제로베이스/Mission1/identifier.sqlite";
    private final String key;

    {
        try {
            key = new Properties().load(new FileInputStream("key.properties")).getProperty("key");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Connection conn = null;
    private PreparedStatement pstmt = null;
    private ResultSet rs = null;

    private void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + sqlitePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

    private void setPstmt(Object obj, String[] order) {
        Field[] fields = obj.getClass().getDeclaredFields();
        HashMap<String, Object> map = new HashMap<>();

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

    private JsonObject httpApi(String url, String path) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url.toString()).build();
        Response response = client.newCall(request).execute();
        String json = response.body().string();

        JsonObject jsonObj = ((JsonObject) JsonParser.parseString(json)).get(path).getAsJsonObject();

        return jsonObj;
    }
    
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

    public int getWifiApi() throws IOException {
        // API 가져오기
        StringBuffer url = new StringBuffer();
        url.append("http://openapi.seoul.go.kr:8088/")
                .append(key)
                .append("/json/TbPublicWifiInfo/1/5/");
        JsonObject jsonObj = httpApi(url.toString(), "TbPublicWifiInfo");
        int totalCount = jsonObj.get("list_total_count").getAsInt();

        long before = System.currentTimeMillis();
        ArrayList<WifiInfo> wifiInfoList = new ArrayList<>();
        for (int i=1; i<=totalCount; i+=1000) { // 천개씩 호출
            url = new StringBuffer();
            url.append("http://openapi.seoul.go.kr:8088/")
                    .append(key)
                    .append("/json/TbPublicWifiInfo/")
                    .append(i+"/")
                    .append(i+999);

            jsonObj = httpApi(url.toString(), "TbPublicWifiInfo");
            JsonArray jsonArr = (JsonArray) jsonObj.get("row");

            for (JsonElement item : jsonArr) {
                String jsonStr = item.getAsJsonObject().toString();
                WifiInfo wifiInfo = new Gson().fromJson(jsonStr, WifiInfo.class);
                if (wifiInfo != null) {
                    wifiInfoList.add(wifiInfo);
                }
            }
        }

        // 가져온 데이터를 DB에 insert
        connect();
        System.out.println("api 가져오기 소요시간:"+(System.currentTimeMillis()-before)/1000);
        before = System.currentTimeMillis();
        try {
            pstmt = conn.prepareStatement
                    ("insert or ignore into wifi_info " +
                            "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            int batchSize = 10000;
            int cur = 0;

            for (WifiInfo wifiInfo : wifiInfoList) {
                cur++;
                // 배열 컬럼 순서대로 wifiInfo의 값을 pstmt에 set
                setPstmt(wifiInfo, new String[]{"no","district","wifiNm","addr1","addr2",
                        "floor","installType","institute","serviceType","networkType",
                        "networkType","year","inOutDoor","connEnvir","lat","lnt","workDt"});

                pstmt.addBatch();
                if (cur == batchSize) {
                    pstmt.executeBatch();
                    pstmt.clearBatch();
                    System.out.println("배치 소요시간:"+(System.currentTimeMillis()-before)/1000);
                    before = System.currentTimeMillis();
                    cur = 0;
                }
            }
            before=System.currentTimeMillis();
            pstmt.executeBatch();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }

        return totalCount;
    }

    private Object resultSetToObject(Object obj) {
        Field[] fields = obj.getClass().getDeclaredFields();

        try {
            for (Field field : fields) {
                field.setAccessible(true);
                field.set(obj, rs.getObject(field.getName()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    public JsonObject getNearNWifi(int n, double lat, double lnt) {
        connect();
        String sql = "select *, (6371*acos(cos(radians(lat))*cos(radians(?))*cos(radians(lnt)" +
                "-radians(?))+sin(radians(lat))*sin(radians(?))))" +
                "AS distance FROM wifi_info " +
                "ORDER BY distance " +
                "Limit "+n;
        try {
            // 위경도를 비교해 거리 순으로 가까운 (하버사인 공식) n(20)개의 wifi를 가져온다.
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

            // 조회 기록 테이블에 추가
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

    private Map<String, Object> getParameterMap(String queryStr) {
        String[] queries = queryStr.split("&");
        Map<String, Object> map = new HashMap<>();

        for (String query : queries) {
            String[] keyValue = query.split("=");
            map.put(keyValue[0], keyValue[1]);
        }

        return map;
    }

    public WifiInfo getWifiInfo(String queryStr) {
        Map<String, Object> queryMap = getParameterMap(queryStr);
        connect();
        WifiInfo wifiInfo = null;
        try {
            String sql = "select * from wifi_info where no=?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, (String) queryMap.get("no"));
            rs = pstmt.executeQuery();
            if (rs.next()) {
                wifiInfo = (WifiInfo) resultSetToObject(new WifiInfo());
                wifiInfo.setDistance(Double.parseDouble((String)queryMap.get("dist")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
        return wifiInfo;
    }

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

    public void addBookmarkWifi(String queryStr) {
        Map<String, Object> map = getParameterMap(queryStr);
        connect();
        try {
            pstmt = conn.prepareStatement("insert into bookmark_wifi(bookmark_id, wifi_id) values(?,?)");
            pstmt.setInt(1, Integer.parseInt((String) map.get("bookmarkId")));
            pstmt.setString(2, (String) map.get("wifiId"));
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }

    public List<BookmarkWifi> getBookmarkWifi() {
        List<BookmarkWifi> list = new ArrayList<>();
        connect();

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

    public void deleteBookmarkWifi(int id) {
        connect();
        try {
            pstmt = conn.prepareStatement("delete from bookmark_wifi where id=?");
            pstmt.setInt(1, id);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }

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

    public void deleteBookmark(int id) {
        connect();
        try {
            pstmt = conn.prepareStatement("delete from bookmark where id=?");
            pstmt.setInt(1, id);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }

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
