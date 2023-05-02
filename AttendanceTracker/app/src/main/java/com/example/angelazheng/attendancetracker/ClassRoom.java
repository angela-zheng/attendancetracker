package com.example.angelazheng.attendancetracker;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.List;

// Test Classrooms
public class ClassRoom {
    // PolygonOptions building;
    int id;
    String startTime;
    String endTime;
    List<LatLng> coordinates;
    String title;

    ClassRoom(int courseId, List<LatLng> coordinateLst, String startTime, String endTime, String className) {
        id = courseId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.coordinates = coordinateLst;
        title = className;
    }

    public PolygonOptions location () {
        PolygonOptions building = new PolygonOptions();
        building.addAll(this.coordinates);
        return building;
    }

    public void setId(int ID) {
        this.id = ID;
    }
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
    public void setCoordinates(List<LatLng> coordinateLst) {
        this.coordinates = coordinateLst;
    }
    public void setTitle(String courseName) {
        this.title = courseName;
    }
}

