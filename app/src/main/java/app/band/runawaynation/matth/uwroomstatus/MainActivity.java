package app.band.runawaynation.matth.uwroomstatus;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

import app.band.runawaynation.matth.uwroomstatus.model.CourseItem;
import app.band.runawaynation.matth.uwroomstatus.services.MyService;
import app.band.runawaynation.matth.uwroomstatus.utils.NetworkHelper;


public class MainActivity extends AppCompatActivity {
    private static final String BUILDING = "BUILDING";
    private static final String DAY = "DAY";
    private static final String ROOM = "ROOM";
    private static String info = "";
    public String dayS = "M";
    RadioButton Mon, Tues, Thurs, Fri, Wed;
    RadioGroup dayRG;
    private String URL, building, day, room;
    EditText buildingET, roomET;
    public String buildingS, roomS, timeS;
    // HttpClient client;
    String[] courseNameArray = new String[100];
    String[] endTimeArray = new String[100];
    String[] startTimeArray = new String[100];
    TextView output;
    Button submit;

    private int i = 0;
    private boolean networkOk;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("MYTAG", "returning");
            processResponse(intent.getStringExtra(MyService.MY_SERVICE_PAYLOAD));
            Log.i("done?", "done?");
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState != null) {
            this.building = savedInstanceState.getString(BUILDING);
            this.room = savedInstanceState.getString(ROOM);
            this.day = savedInstanceState.getString(DAY);
        }
        this.buildingET = (EditText) findViewById(R.id.buildingEditText);
        this.roomET = (EditText) findViewById(R.id.roomEditText);
        this.dayRG = (RadioGroup) findViewById(R.id.dayRadioGroup);
        this.Mon = (RadioButton) findViewById(R.id.radio_Monday);
        this.Tues = (RadioButton) findViewById(R.id.radio_Tuesday);
        this.Wed = (RadioButton) findViewById(R.id.radio_Wednesday);
        this.Thurs = (RadioButton) findViewById(R.id.radio_Thursday);
        this.Fri = (RadioButton) findViewById(R.id.radio_Friday);
        this.output = (TextView) findViewById(R.id.tvOutput);
        this.submit = (Button) findViewById(R.id.submitButton);

        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(mBroadcastReceiver,
                        new IntentFilter(MyService.MY_SERVICE_MESSAGE));

        networkOk = NetworkHelper.hasNetworkAccess(this);

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

        LocalBroadcastManager.getInstance(getApplicationContext())
                .unregisterReceiver(mBroadcastReceiver);
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(BUILDING, this.building);
        outState.putString(ROOM, this.room);
        outState.putString(DAY, this.day);
    }

    public void onSubmitClicked() {
        i = 0;
        buildingS = buildingET.getText().toString();
        roomS = roomET.getText().toString();
        if (buildingS.isEmpty() || roomS.isEmpty() || dayS.equalsIgnoreCase("")) {
            output.setText("Invalid Input");
            return;
        }
        resetCourseList();
        URL = "https://api.uwaterloo.ca/v2/buildings/" + buildingS + "/" + roomS + "/courses.json?key=123afda14d0a233ecb585591a95e0339";
        if (networkOk) {
            Intent intent = new Intent(this, MyService.class);
            intent.setData(Uri.parse(URL));
            startService(intent);
            processResponse(info);
            printCourseList();
            Toast.makeText(this, "Done", Toast.LENGTH_LONG);
        } else {
            Toast.makeText(this, "Network not available!", Toast.LENGTH_SHORT).show();
        }
    }

    // HELPERS
    public void resetCourseList() {
        for (int i = 0; i < 24; i++) {
            this.startTimeArray[i] = "";
            this.endTimeArray[i] = "";
            this.courseNameArray[i] = "";
        }
    }
    void insertToCourseList(String startTime, String endTime, String courseName) {
        output.append(courseName + " " + startTime + " " + endTime + "\n");
        /*this.courseNameArray[i] = courseName;
        this.startTimeArray[i] = startTime;
        this.endTimeArray[i] = endTime;
        i++;
        return;
            /*} else if (timeAsNum(this.startTimeArray[i]) > timeAsNum(startTime)) {
                for (int j = i; j < 24 && !courseName.equalsIgnoreCase(""); j++) {
                    String tempCourseName = this.courseNameArray[j];
                    String tempStartTime = this.startTimeArray[j];
                    String tempEndTime = this.endTimeArray[j];
                    this.courseNameArray[j] = courseName;
                    this.startTimeArray[j] = startTime;
                    this.endTimeArray[j] = endTime;
                    courseName = tempCourseName;
                    startTime = tempStartTime;
                    endTime = tempEndTime;
                }
                return;
            } else {
                i++;
            }*/
    }

    void printCourseList() {
        String mycourselist = "";
        for (int i = 0; i < 24; i++) {
            if (this.courseNameArray[i].equalsIgnoreCase("")) {
                output.setText(mycourselist);
            } else {
                mycourselist = mycourselist + startTimeArray[i] + "-" + endTimeArray[i] + ": " + courseNameArray[i] + "\n";
            }
        }
    }
    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.radio_Monday:
                if (checked) {
                    this.dayS = "M";
                    return;
                }
                return;
            case R.id.radio_Tuesday:
                if (checked) {
                    this.dayS = "T";
                    return;
                }
                return;
            case R.id.radio_Wednesday:
                if (checked) {
                    this.dayS = "W";
                    return;
                }
                return;
            case R.id.radio_Thursday:
                if (checked) {
                    this.dayS = "Th";
                    return;
                }
                return;
            case R.id.radio_Friday:
                if (checked) {
                    this.dayS = "F";
                    return;
                }
                return;
            default:
                return;
        }
    }
    public Boolean isSameDay(String day) {
        if (this.dayS.equals("T") && (tCount(day) == 2 || (day.contains(this.dayS) && !day.contains("h")))) {
            return Boolean.valueOf(true);
        }
        if (this.dayS.equals("T") || !day.contains(this.dayS)) {
            return Boolean.valueOf(false);
        }
        return Boolean.valueOf(true);
    }
    int tCount(String day) {
        int count = 0;
        for (int i = 0; i < day.length() - 1; i++) {
            if (day.substring(i, i + 1).equals("T")) {
                count++;
            }
        }
        return count;
    }
    // Time Helpers
    public void updateTime(int hour, int minute) {
        String hourS = hour + "";
        String minuteS = minute + "";
        if (hour < 10) {
            hourS = "0" + hourS;
        }
        if (minute < 10) {
            minuteS = "0" + minuteS;
        }
        this.timeS = hourS + ":" + minuteS;
    }
    public Boolean isSameTime(String start_time, String end_time) {
        if (timeAsNum(start_time) > timeAsNum(this.timeS) || timeAsNum(end_time) < timeAsNum(this.timeS)) {
            return Boolean.valueOf(false);
        }
        return Boolean.valueOf(true);
    }
    int timeAsNum(String time) {
        return Integer.parseInt(time.substring(0, 2) + time.substring(3, 5));
    }

    public void processResponse(String response) {
        // remove metadata
        int x = 0;
        while (x < 200 && response.length() > x) { response = response.substring(1); x++;
            if (response.substring(0, 1).equals("[")) break; }
        JSONArray arr;
        Log.d("TAG", response);
        try {
            arr = new JSONArray(response);
            if (arr.length() < 1) return;
            for (int i = 0; i < arr.length(); i++) {
                try {
                    JSONObject json = arr.getJSONObject(i);
                    insertToCourseList(json.getString("start_time"), json.getString("end_time"), json.getString("title"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
