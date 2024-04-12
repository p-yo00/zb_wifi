package com.example.mission1;

import com.example.mission1.vo.Bookmark;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(urlPatterns = {"/", "/bookmark/*", "/wifiInfo/*", "/api"})
public class HomeServlet extends HttpServlet {
    private Service service;
    private String url;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String pathInfo = req.getRequestURI();
        String queryStr = req.getQueryString();
        service = new Service();

        if (pathInfo != null) {
            String[] parts = pathInfo.split("/");
            if (parts.length == 0) {
                url = "/home.jsp";
            }
            else {
                if (parts[1].equals("wifiInfo")) {
                    url = "/wifi-info.jsp";
                    Map<String, Object> map = new HashMap<>();
                    map.put("wifiInfo", service.getWifiInfo(queryStr));
                    map.put("bookmarkGroup", service.getBookmarkGroup());
                    req.setAttribute("param", map);
                }
                else if (parts[1].equals("history")) {
                    url = "/history.jsp";
                    req.setAttribute("historyList", service.getHistory());
                }
                else if (parts[1].equals("api")) {
                    url = "/load-wifi.jsp";
                    int totalCount = service.getWifiApi();
                    req.setAttribute("totalCount", totalCount);
                }
                else if (parts[1].equals("bookmark") && parts[2].equals("view")) {
                    url = "/view-bookmark.jsp";
                    req.setAttribute("bookmarkWifi", service.getBookmarkWifi());
                }
                else if (parts[1].equals("bookmark") && parts[2].equals("manage")) {
                    url = "/manage-bookmark.jsp";
                    req.setAttribute("bookmarkGroup", service.getBookmarkGroup());
                }
                else if (parts[1].equals("bookmark") && parts[2].equals("delete")) {
                    url = "/delete-bookmark.jsp";
                    req.setAttribute("bookmarkWifi", service.getBookmarkWifiById(queryStr));
                }
                else if (parts[1].equals("bmGroup") && parts[2].equals("modify")) {
                    url = "/edit-bookmark.jsp";
                    req.setAttribute("bookmark", service.getBookmarkById(queryStr));
                }
            }
        }

        RequestDispatcher dispatcher = req.getRequestDispatcher(url);
        dispatcher.forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String pathInfo = req.getRequestURI();
        service = new Service();

        if (pathInfo != null) {
            String[] parts = pathInfo.split("/");

            if (parts[1].equals("bookmark") && parts[2].equals("add")) {
                url = "/bookmark/manage";
                String name = req.getParameter("name");
                int order = Integer.parseInt(req.getParameter("order"));
                service.addBookmarkGroup(name, order);
            }
            else if (parts[1].equals("bookmark") && parts[2].equals("delete")) {
                url = "/bookmark/view";
                int id = Integer.parseInt(req.getParameter("id"));
                service.deleteBookmarkWifi(id);
            }
            else if (parts[1].equals("bmGroup") && parts[2].equals("modify")) {
                url = "/bookmark/manage";
                Bookmark bookmark = new Bookmark();
                bookmark.setId(Integer.parseInt(req.getParameter("id")));
                bookmark.setName(req.getParameter("name"));
                bookmark.setOrder(Integer.parseInt(req.getParameter("order")));
                service.updateBookmark(bookmark);
            }
        }

        resp.sendRedirect(url);
    }
}
