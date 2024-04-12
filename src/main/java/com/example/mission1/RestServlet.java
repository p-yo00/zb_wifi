package com.example.mission1;

import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 페이지 이동이 없고 아무 것도 return 하지 않거나 json 데이터를 전달하는 Servlet
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

            if (parts[1].equals("wifi")) {
                double lat = Double.parseDouble(req.getParameter("lat"));
                double lnt = Double.parseDouble(req.getParameter("lnt"));
                JsonObject jsonObj = service.getNearNWifi(20, lat, lnt);
                resp.setContentType("application/json; charset=utf-8");
                resp.getWriter().print(jsonObj);
            }

            else if (parts[1].equals("delHistory")) {
                int id = Integer.parseInt(req.getParameter("id"));
                service.deleteHistory(id);
            }

            else if (parts[1].equals("add") && parts[2].equals("bmWifi")) {
                service.addBookmarkWifi(queryStr);
            }

            else if (parts[1].equals("delBookmark")) {
                int id = Integer.parseInt(req.getParameter("id"));
                service.deleteBookmark(id);
            }
        }
    }
}
