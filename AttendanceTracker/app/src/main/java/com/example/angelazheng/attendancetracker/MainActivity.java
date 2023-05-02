package com.example.angelazheng.attendancetracker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import java.lang.Math.*;
import android.content.BroadcastReceiver;
import java.util.concurrent.TimeUnit;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;

import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.ads.identifier.AdvertisingIdClient.Info;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;

import android.widget.Button;



public class MainActivity extends AppCompatActivity {
    private Context context;
    // UI Elements
    private TextView locationText;
    private TextView loggedBool;
    private TextView countdown;
    private TextView nextClassUp;
    private TextView classesMissed;
    private Button showLocationButton;
    private Button hideLocationButton;
    private Button refresh;

    // Location Elements
    private BroadcastReceiver bReceiver;

    // Date Elements
    private Date date;

    // Tests
    private ClassRoom duffAtrium;
    private ClassRoom upson;
    private ClassRoom starbucks;
    private List<ClassRoom> courses;

    // Databases
    private dbHandler handler;

    // Student (one only, because we want phones to be unique to the student)
    private Student stu;

    // get Unique Advertiser's ID
/*    AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
        @Override
        protected String doInBackground(Void... params) {
            String id = getIdThread();
            Log.d("AdID", id);
            return id;
        }

        @Override
        protected void onPostExecute(String id) {
            stu = new Student(id,"Angela", "Zheng");
            Toast.makeText(getApplicationContext(), id, Toast.LENGTH_SHORT).show();
        }
    };*/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.activity_main);

        // Initialize UI
        showLocationButton = findViewById(R.id.showLocationButton);
        hideLocationButton = findViewById(R.id.hideLocationButton);
        locationText = findViewById(R.id.locationDisplay);
        loggedBool = findViewById(R.id.loggedInBool);
        countdown = findViewById(R.id.countdown);
        nextClassUp = findViewById(R.id.nextclass);
        classesMissed = findViewById(R.id.classesMissed);
        refresh = findViewById(R.id.refresh);

        // task.execute();
        hideLocationButton.setText("Hide Location");
        hideLocationButton.setVisibility(View.GONE);
        showLocationButton.setText("Show Location");
        showLocationButton.setVisibility(View.VISIBLE);
        classesMissed.setText("Total Classes Missed: ");

        stu = new Student("1","Angela", "Zheng");

        // create Duffield Atrium Course
        // start: 1:00PM, end 2:30 PM
        List<LatLng> duffCoords = new ArrayList<>();
        LatLng one = new LatLng(42.444317427422966, -76.48220447885632);
        LatLng two = new LatLng(42.44406902577963, -76.48219911443829);
        LatLng three = new LatLng(42.44405813962929, -76.48247001754879);
        LatLng four = new LatLng(42.444313468838736, -76.48252097952007);
        duffCoords.add(one);
        duffCoords.add(two);
        duffCoords.add(three);
        duffCoords.add(four);
        duffAtrium = new ClassRoom(1, duffCoords, "13:00:00", "14:30:00", "duffAtrium");

        // create Upson Course
        // start: 2:40, end: 3:25;
        List<LatLng> upsonCoords = new ArrayList<>();
        LatLng uOne = new LatLng(42.4440409668529, -76.48293350164568);
        LatLng uTwo = new LatLng(42.444054821956776, -76.48259822551881);
        LatLng uThree = new LatLng(42.44390043634038, -76.48256603901063);
        LatLng uFour = new LatLng(42.44388460189669, -76.48292277280962);
        upsonCoords.add(uOne);
        upsonCoords.add(uTwo);
        upsonCoords.add(uThree);
        upsonCoords.add(uFour);
        upson = new ClassRoom(2, upsonCoords, "14:40:00", "15:25:00", "upson");

        // create "Starbucks" Course
        // start 10:00AM to 1:00PM
        List<LatLng> starCoords = new ArrayList<>();
        LatLng starOne = new LatLng(42.441841974427206, -76.48562661363178);
        LatLng starTwo = new LatLng(42.44184791253765, -76.48529670192295);
        LatLng starThree = new LatLng(42.44163216083002, -76.48529401971393);
        LatLng starFour = new LatLng(42.4416262226991, -76.48560783816868);
        starCoords.add(starOne);
        starCoords.add(starTwo);
        starCoords.add(starThree);
        starCoords.add(starFour);
        starbucks = new ClassRoom(3, starCoords,"10:00:00", "13:00:00", "starbucks");

        courses = new ArrayList<>(); // classes added in order of time
        courses.add(starbucks);
        courses.add(duffAtrium);
        courses.add(upson);

        // add courses to Database
        handler = new dbHandler(this,null);
        //
        handler.addStudentHandler(stu);

        handler.addCourseHandler(duffAtrium);
        handler.addCourseHandler(upson);
        handler.addCourseHandler(starbucks);

        // add courses and student to Attendance Database
        handler.addAttendanceHandler(duffAtrium, stu);
        handler.addAttendanceHandler(upson, stu);
        handler.addAttendanceHandler(starbucks, stu);

        final HashMap courseInfo = handler.loadCourseStart();

        Intent intent = new Intent(this, GoogleService.class);

        try {
            ArrayList<String> n = nextClass(handler);
            nextClassUp.setText("Next Class:");
            String upcomingTime = String.valueOf(courseInfo.get(n.get(0)));
            countdown.setText(n.get(0)+" at "+upcomingTime);
            int numClassesMissed = handler.retrieveAttendanceInfo(stu);
            classesMissed.setText("Total Classes Missed: " + String.valueOf(numClassesMissed));
        } catch (ParseException e) {
            e.printStackTrace();
            Log.d("TimeTo", "Well that did not work");
        }
        refresh.setVisibility(View.GONE);
        // referenced stackOverflow for Broadcast Receiver help
        startService(intent);
        bReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle extras = intent.getExtras();
                if (extras!=null) {
                    Bundle loc = extras.getBundle("location");
                    double[] locArray = loc.getDoubleArray("location");
                    final String latlon = Double.toString(locArray[0]) + ", " + Double.toString(locArray[1]);
                    // Button Listener
                    Log.d("location", latlon);
                    Boolean anyClasses = false;
                    for (int i=0; i<courses.size();i++){
                        ClassRoom c = courses.get(i);
                        String start = c.startTime;
                        String end = c.endTime;
                        try {
                            if (inRange(start,end)) {
                                anyClasses = true;
                                handler.increaseTotalAttendanceCount(c,stu);
                                if (isHere(locArray, c.location())) {
                                    loggedBool.setText("Yay! You're in Class");
                                    handler.increaseAttendance(c, stu);
                                    Log.d("here", "True");
                                } else {
                                    String missingClass = c.title;
                                    loggedBool.setText("Why aren't you in "+missingClass+"??");
                                    countdown.setText(missingClass+" now!");
                                    Log.d("here", "not in class!/ set to false");
                                }
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    if (anyClasses==false){
                        loggedBool.setText("You Don't Have Any Classes Right Now!");
                    }
                    showLocationButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            locationText.setText(latlon);
                            hideLocationButton.setVisibility(View.VISIBLE);
                            showLocationButton.setVisibility(View.GONE);
                        }
                    });
                    hideLocationButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            locationText.setText("");
                            showLocationButton.setVisibility(View.VISIBLE);
                            hideLocationButton.setVisibility(View.GONE);
                        }
                    });
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(bReceiver, new IntentFilter("BROADCAST_ACTION"));
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("BROADCAST_ACTION"));
    }

    // this function returns a boolean to see if the current time is within range of class time
    public boolean inRange(String classStart, String classEnd) throws ParseException {
        // reformat the date to hours:min:ss
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String formatted = sdf.format(currentTime);
        Calendar current = Calendar.getInstance();
        Date now = new SimpleDateFormat("HH:mm:ss").parse(formatted);
        current.setTime(now);

        // get start class date
        // referenced StackOverFlow
        try {
            // initialize start time
            Date startTime = new SimpleDateFormat("HH:mm:ss").parse(classStart);
            Calendar start = Calendar.getInstance();
            start.setTime(startTime);

            // initialize end time
            Date endTime = new SimpleDateFormat("HH:mm:ss").parse(classEnd);
            Calendar end = Calendar.getInstance();
            end.setTime(endTime);
            end.add(Calendar.DATE, 1);

            if (now.after(startTime) && now.before(endTime)) {
                Log.d("Time:" , "TRUE");
                return true;
            } else {
                return false;
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
        // get end class time
        return false;
    };

    // function for checking if the actual points are within a room
    // ray-casting algorithm according to rosettacode.org
    // referenced stackoverflow (https://stackoverflow.com/questions/15816928/test-of-point-inside-polygon-in-android)
    public boolean isHere(double[] given, PolygonOptions room) {
        int cross = 0;
        List<LatLng> roomPoints = room.getPoints(); // list of LatLng points making up polygon
        int numPoints = roomPoints.size();

        // loop through all the line segments made out of points a&b that make up the classroom
        for (int startIndex = 0; startIndex < roomPoints.size(); startIndex++) {
            // check if given point crosses line segment
            int endIndex = startIndex + 1;
            if (endIndex >= numPoints) {
                endIndex = 0;
                LatLng start = roomPoints.get(startIndex);
                LatLng end = roomPoints.get(endIndex);

                // get x,y (long-vertical, lat-horizontal) coordinate points for all
                double startX = start.longitude;
                double startY = start.latitude;

                double endX = start.longitude;
                double endY = start.latitude;

                if (start.latitude > end.latitude) {
                    startX = end.longitude;
                    startY = end.latitude;
                }

                if (checkCross(given[1],given[0],startX,startY,endX,endY)) {
                    cross = cross + 1;
                }
            } else {
                endIndex = startIndex + 1;
                LatLng start = roomPoints.get(startIndex);
                LatLng end = roomPoints.get(endIndex);

                // get x,y (long-vertical, lat-horizontal) coordinate points for all
                double startX = start.longitude;
                double startY = start.latitude;

                double endX = end.longitude;
                double endY = end.latitude;

                if (start.latitude > end.latitude) {
                    startX = end.longitude;
                    startY = end.latitude;
                    endX = start.longitude;
                    endY = start.latitude;
                }
                if (checkCross(given[1],given[0],startX,startY,endX,endY)) {
                    cross = cross + 1;
                }
            }
        }
        return (cross%2==1);
    }

    // function to check if ray has crossed (referenced Stackoverflow in same article)
    public boolean checkCross(double givenX, double givenY, double startX, double startY, double endX, double endY) {
        // correct longitude, if less than 0;
        if (givenX < 0) {givenX += 360;}
        if (startX < 0) {startX += 360;}
        if (endX < 0) {endX += 360;}

        // correct latitude slightly if it's the same ?
        if (givenY == startY || givenY == endY) {
            givenY += .00000000001;
        }
        if ((givenY > endY || givenY < startY) || (givenX > Math.max(startX, startY))) {
            return false;}
        else if (givenX < Math.min(startX, endX)){
            return true;}
        else {
            // create the rays
            double slopeOne = (startX != endX) ? ((endY - startY) / (endX - startX)) : Double.POSITIVE_INFINITY;
            double slopeTwo = (startX != givenX) ? ((givenY - startY) / (givenX - startX)) : Double.POSITIVE_INFINITY;
            return (slopeTwo >= slopeOne);
        }
    }

    // function that finds the next class
    public ArrayList<String> nextClass(dbHandler db) throws ParseException {
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String formatted = sdf.format(currentTime);
        Date now = new SimpleDateFormat("HH:mm:ss").parse(formatted);

        // get the time distances from current time to times of every class
        HashMap<String, String> startTimeMap = db.loadCourseStart();

        // new HashMap of (course name, time left to course)
        HashMap<String, String> timeToMap = new HashMap<>();

        // find the shortest distance
        Iterator it = startTimeMap.entrySet().iterator();
        while (it.hasNext()) {
            HashMap.Entry pair = (HashMap.Entry) it.next();
            Date time = sdf.parse((String) pair.getValue());
            long dist = now.getTime()-time.getTime(); // if dist is negative, shows that time is still up ahead
            timeToMap.put(String.valueOf(pair.getKey()), String.valueOf(dist));
        }
        // iterate through the timeToMap to find shortest distance
        Iterator it2 = timeToMap.entrySet().iterator();
        long shortest = 0;
        long ns = 0;
        String next = "";
        while (it2.hasNext()) {
            HashMap.Entry set = (HashMap.Entry) it2.next();
            long val = Long.parseLong((String) set.getValue());
            boolean valPos = val>0;
            if (valPos) {
                val = -val;
                // add 24 hours to current time then subtract value
                if (val<ns){
                    ns = val;
                    next = String.valueOf(set.getKey());
                }
            }
            else if (val < shortest) {
                shortest = val;
                next = String.valueOf(set.getKey());
            }
        }
        if (ns!=0) {
            shortest = ns-82800000;
        }
        int seconds = (int) java.lang.StrictMath.abs((shortest / 1000) % 60) ;
        int minutes = (int) java.lang.StrictMath.abs(((shortest / (1000*60)) % 60));
        int hours   = (int) java.lang.StrictMath.abs((shortest / (1000*60*60)) % 24);
        String timeString = String.valueOf(hours)+" Hours "+String.valueOf(minutes)+" Minutes "+String.valueOf(seconds)+" Seconds ";

        Log.d("NextTIme", next+" in "+timeString);

        ArrayList<String> lst = new ArrayList<>();
        lst.add(next);
        lst.add(timeString);
        return lst;
    }
}

