package com.example.mission1.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookmarkWifi {
    int id;
    String wifiId;
    int bookmarkId;
    String wifiNm;
    String bookmarkNm;
    String createdDt;

    public BookmarkWifi() {}
}
