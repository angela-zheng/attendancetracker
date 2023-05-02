package com.example.angelazheng.attendancetracker;

class Student {
    private String id;
    private String first;
    private String last;

    Student(String idNum, String firstName, String lastName){
        this.id = idNum;
        this.first = firstName;
        this.last = lastName;
    }

    public void setId(String idNum){
        this.id = idNum;
    }
    public void setFirst(String firstName){
        this.first = firstName;
    }
    public void setLast(String lastName){
        this.last = lastName;
    }
    public String getId() {
        return this.id;
    }
    public String getFirst(){
        return this.first;
    }
    public String getLast(){
        return this.last;
    }
}
