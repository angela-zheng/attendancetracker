package com.example.angelazheng.attendancetracker;

// relied HEAVILY on this: https://dzone.com/articles/create-a-database-android-application-in-android-s

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class dbHandler extends SQLiteOpenHelper {
    private static final int VERSION = 20;

    private static final String DATABASE_NAME = "coursesDB.db";

    // Course Table
    private static final String TABLE_NAME = "Courses";
    private static final String COLUMN_ID = "CourseId";
    private static final String COLUMN_START = "StartTime";
    private static final String COLUMN_END = "EndTime";
    private static final String COLUMN_COURSENAME = "CourseName";

    // Student table
    private static final String TABLE_STUDENT = "Students";
    private static final String COLUMN_STUDENTID = "StudentID";
    private static final String COLUMN_FIRSTNAME = "StudentName";
    private static final String COLUMN_LASTNAME = "LastName";

    // Attendance table (Many-to-Many)
    private static final String TABLE_ATTENDANCE = "Attendance";
    private static final String COLUMN_ATTENDID = "AttendanceID";
    private static final String COLUMN_STUDENTFK = "StudentIDFK";
    private static final String COLUMN_COURSEFK = "CourseIDFK";
    private static final String COLUMN_ATTENDANCE = "TimesAttended";
    private static final String COLUMN_TOTAL_NUM = "TotalNumOfClasses";

    public dbHandler(Context context, SQLiteDatabase.CursorFactory factory) {
        super(context, DATABASE_NAME, factory, VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        // create tables
        // create Courses table
        String CREATE = "CREATE TABLE " + TABLE_NAME + " (" + COLUMN_ID + " PRIMARYKEY, " + COLUMN_START + " TEXT, " + COLUMN_END + " TEXT, " + COLUMN_COURSENAME + " TEXT )";
        db.execSQL(CREATE);

        // create Student table
        db.execSQL("CREATE TABLE " + TABLE_STUDENT + " (" + COLUMN_STUDENTID + " PRIMARYKEY, " + COLUMN_FIRSTNAME + " TEXT, " + COLUMN_LASTNAME + " TEXT ) ");

        // create Attendance table
        db.execSQL("CREATE TABLE " + TABLE_ATTENDANCE + " (" + COLUMN_ATTENDID + " AUTO_INCREMENT, " + COLUMN_STUDENTFK + " FOREIGNKEY, " + COLUMN_COURSEFK + " FOREIGNKEY, " + COLUMN_TOTAL_NUM + " INT, "+ COLUMN_ATTENDANCE + " INT )");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STUDENT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ATTENDANCE);
        onCreate(db);
    }

    // SQL Query Statement ("SELECT * FROM table_name")
    public HashMap<String, String> loadCourseStart() {
        String query = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        HashMap<String, String> map = new HashMap<>();
        while (cursor.moveToNext()) {
            String courseName = cursor.getString(3);
            String startTime = cursor.getString(1);
            map.put(courseName, startTime);
        }
        return map;
    }

    // adding new records to Courses table
    public void addCourseHandler(ClassRoom course) {
        ContentValues val = new ContentValues();
        val.put(COLUMN_ID, course.id);
        val.put(COLUMN_START, course.startTime);
        val.put(COLUMN_END, course.endTime);
        val.put(COLUMN_COURSENAME, course.title);
        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(TABLE_NAME, null, val);
        db.close();
    }

    // adding new records to Students table
    public void addStudentHandler(Student student) {
        ContentValues val = new ContentValues();
        val.put(COLUMN_STUDENTID, student.getId());
        val.put(COLUMN_FIRSTNAME, student.getFirst());
        val.put(COLUMN_LASTNAME, student.getLast());
        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(TABLE_STUDENT, null, val);
        db.close();
    }

    // adding new records to Attendance table
    public void addAttendanceHandler(ClassRoom course, Student student) {
        ContentValues val = new ContentValues();
        val.put(COLUMN_STUDENTFK, student.getId());
        val.put(COLUMN_COURSEFK, course.id);
        val.put(COLUMN_TOTAL_NUM, 0);
        val.put(COLUMN_ATTENDANCE, 0);
        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(TABLE_ATTENDANCE, null, val);
        db.close();
    }

    // function for incrementing attendance integer for a class if student attends
    public void increaseAttendance(ClassRoom course, Student student) {
        String query = "SELECT * FROM " + TABLE_ATTENDANCE + " WHERE " + COLUMN_STUDENTFK + "=" + student.getId() + " AND " + COLUMN_COURSEFK + "=" + course.id;
        SQLiteDatabase dbAttendance = this.getWritableDatabase();
        Cursor cursorAtt = dbAttendance.rawQuery(query, null);
        if (cursorAtt.moveToFirst()) {
            int count = Integer.parseInt(cursorAtt.getString(4));
            int total = Integer.parseInt(cursorAtt.getString(3));
            ContentValues newC = new ContentValues();
            newC.put(COLUMN_COURSEFK, course.id);
            newC.put(COLUMN_STUDENTFK, student.getId());
            newC.put(COLUMN_TOTAL_NUM, total);
            newC.put(COLUMN_ATTENDANCE, count + 1);
            dbAttendance.update(TABLE_ATTENDANCE,newC,COLUMN_STUDENTFK+"="+student.getId()+" AND "+COLUMN_COURSEFK+ "="+course.id, null);
        }
    }

    public void increaseTotalAttendanceCount(ClassRoom course, Student student) {
        String query = "SELECT * FROM " + TABLE_ATTENDANCE + " WHERE " + COLUMN_STUDENTFK + "=" + student.getId() + " AND " + COLUMN_COURSEFK + "=" + course.id;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            ContentValues val = new ContentValues();
            int count = Integer.parseInt(cursor.getString(4));
            int total = Integer.parseInt(cursor.getString(3));
            val.put(COLUMN_COURSEFK, course.id);
            val.put(COLUMN_STUDENTFK, student.getId());
            val.put(COLUMN_TOTAL_NUM, total+1);
            val.put(COLUMN_ATTENDANCE, count);
            db.update(TABLE_ATTENDANCE, val, COLUMN_STUDENTFK+"="+student.getId()+" AND "+COLUMN_COURSEFK+ "="+course.id, null);
        }
    }

    // Number of Classes Missed
    public int retrieveAttendanceInfo(Student student) {
        String query = "SELECT * FROM " + TABLE_ATTENDANCE + " WHERE " + COLUMN_STUDENTFK + "=" + student.getId();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        int numClassesMissed = 0;
        if (cursor.moveToFirst()) {
            while (cursor.moveToNext()) {
                int classesAttended = Integer.parseInt(cursor.getString(4));
                int totalClasses = Integer.parseInt((cursor.getString(3)));
                numClassesMissed = numClassesMissed + (totalClasses-classesAttended);
            }
        }
        return numClassesMissed;
    }

}
