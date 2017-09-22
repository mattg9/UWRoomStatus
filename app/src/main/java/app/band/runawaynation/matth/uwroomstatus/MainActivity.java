package app.band.runawaynation.matth.uwroomstatus;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import app.band.runawaynation.matth.uwroomstatus.services.MyService;
import app.band.runawaynation.matth.uwroomstatus.utils.NetworkHelper;


public class MainActivity extends AppCompatActivity {
    
    // Global variables all private to the Main Activity
    private static final String BUILDING = "BUILDING";
    private static final String DAY = "DAY";
    private static final String ROOM = "ROOM";
    private boolean networkOk;
    private String building;
    private String day;
    private String room;
    private EditText buildingET, roomET;
    private TextView output;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // keep existing values when returning to the app
        if (savedInstanceState != null) {
            building = savedInstanceState.getString(BUILDING);
            room = savedInstanceState.getString(ROOM);
            day = savedInstanceState.getString(DAY);
        }
        
        // assign view objects to corresponding declared variables
        buildingET = (EditText) findViewById(R.id.buildingEditText);
        roomET = (EditText) findViewById(R.id.roomEditText);
        output = (TextView) findViewById(R.id.tvOutput);
        Button submit = (Button) findViewById(R.id.submitButton);

        // register the broadcast manager for my service
        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(mBroadcastReceiver,
                        new IntentFilter(MyService.MY_SERVICE_MESSAGE));
        networkOk = NetworkHelper.hasNetworkAccess(this);

        // setup onclick event
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSubmitClicked();
            }
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // unregister broadcast manager for my service
        LocalBroadcastManager.getInstance(getApplicationContext())
                .unregisterReceiver(mBroadcastReceiver);
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(BUILDING, building);
        outState.putString(ROOM, room);
        outState.putString(DAY, day);
    }

    // what happens when I click submit button
    private void onSubmitClicked() {
        output.setText(""); // clear the output log to screen
        building = buildingET.getText().toString();
        room = roomET.getText().toString();
        if (building.isEmpty() || room.isEmpty() || day.isEmpty()) {
            output.setText(R.string.invalid_input);
            return;
        }
        // set the url of REST GET request
        String URL = "https://api.uwaterloo.ca/v2/buildings/" + building + "/" + room
                + "/courses.json?key=123afda14d0a233ecb585591a95e0339";
        if (networkOk) {
            // start the service
            Intent intent = new Intent(this, MyService.class);
            intent.setData(Uri.parse(URL));
            startService(intent);
            Toast.makeText(this, "Done", Toast.LENGTH_LONG);
        } else {
            Toast.makeText(this, "Network not available!", Toast.LENGTH_SHORT).show();
        }
    }
    
    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.radio_Monday: if (checked) { day = "M"; break; }
            case R.id.radio_Tuesday: if (checked) { day = "T"; break; }
            case R.id.radio_Wednesday: if (checked) { day = "W"; break; }
            case R.id.radio_Thursday: if (checked) { day = "Th"; break; }
            case R.id.radio_Friday: if (checked) { day = "F"; break; }
        }
    }

    private void printCourse(String startTime, String endTime, String courseName) {
        output.append(courseName + " " + startTime + "-" + endTime + "\n");
    }
   
    private void processCourseData(String response) {
        // remove metadata
        int x = 0;
        while (x < 200 && response.length() > x) { response = response.substring(1); x++;
            if (response.substring(0, 1).equals("[")) break; }
        JSONArray arr;
        // go after every course
        try {
            arr = new JSONArray(response);
            if (arr.length() < 1) return;
            for (int i = 0; i < arr.length(); i++) {
                try {
                    JSONObject json = arr.getJSONObject(i);
                    // print only for the given day
                    if (isSameDay(json.getString("weekdays"))) {
                        printCourse(json.getString("start_time"), json.getString("end_time")
                                , json.getString("subject") + " " + json.getString("catalog_number"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean isSameDay(String weekdays) {
        switch(day) {
            case "M": case "W": case "F":case "Th":
                return weekdays.contains(day);
            case "T":
                int tCount = weekdays.length() - weekdays.replace("T", "").length();
                // either there is 2 T's - Tuesday!, or T and no H - also Tuesday! otherwise FALSE
                return tCount == 1 && !weekdays.contains("h") || tCount == 2;
            default:
                return false;
        }
    }

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            processCourseData(intent.getStringExtra(MyService.MY_SERVICE_PAYLOAD));
        }
    };
}
