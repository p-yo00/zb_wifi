package com.example.mission1;

import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *  작성자 : 박예온
 *  날짜 : 2024-04-14
 *  페이지 이동이 없고 아무 것도 return 하지 않거나 json 데이터를 전달하는 Servlet (ajax 비동기 호출)
 */
@WebServlet(urlPatterns = {"/wifi", "/delHistory", "/add/*", "/delBookmark"})
public class RestServlet extends HttpServlet {
    Service service;
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getRequestURI();
        String queryStr = req.getQueryString();
        service = new Service();

        if (pathInfo != null) {
            String[] parts = pathInfo.split("/");

            // url="/wifi", 근처 20개 wifi를 조회한다.
            if (parts[1].equals("wifi")) {
                double lat = Double.parseDouble(req.getParameter("lat"));
                double lnt = Double.parseDouble(req.getParameter("lnt"));
                JsonObject jsonObj = service.getNearNWifi(20, lat, lnt);
                resp.setContentType("application/json; charset=utf-8");
                resp.getWriter().print(jsonObj);
            }

            // url="/delHistory", id에 해당하는 검색 내역을 삭제한다.
            else if (parts[1].equals("delHistory")) {
                int id = Integer.parseInt(req.getParameter("id"));
                service.deleteHistory(id);
            }

            // url="/add/bmWifi", queryStr에 bookmark id와 wifi id가 저장되어있고, bookmark에 wifi를 추가한다.
            else if (parts[1].equals("add") && parts[2].equals("bmWifi")) {
                service.addBookmarkWifi(queryStr);
            }

            // url="/delBookmark", id에 해당하는 bookmark를 삭제한다.
            else if (parts[1].equals("delBookmark")) {
                int id = Integer.parseInt(req.getParameter("id"));
                service.deleteBookmark(id);
            }
        }
    }
}
