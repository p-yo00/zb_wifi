package com.example.mission1.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Bookmark {
    int id;
    String name;
    int order;
    String createdDt;
    String modifiedDt;

    public Bookmark(){}
}
