package com.example.vladimir.informer_pi;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class AsyncHandleShedJSON extends AsyncTask<AppCompatActivity, Integer, SimpleAdapter> {
    private String times[];
    private String dates[] = new String[6];
    private String titles[];
    private String lecturers[];
    private String rooms[];
    private MainActivity mMainActivity;
    private String urlString;

    @Override
    protected void onPreExecute() {
//        mMainActivity.ProgressBar.setVisibility(ProgressBar.VISIBLE);
        super.onPreExecute();
    }

    @Override
    protected SimpleAdapter doInBackground(AppCompatActivity... params) {

        SimpleAdapter mShedSimpleAdapter = null;

        mMainActivity = (MainActivity) params[0];

        int Course = mMainActivity.Course;

        switch (Course) {
            case 0:
                urlString = "https://drive.google.com/uc?export=download&confirm=no_antivirus&id=0B8QN8De7yDDUMl9ZdWU1SGY1d1U";
                break;
            case 1:
                urlString = "https://drive.google.com/uc?export=download&confirm=no_antivirus&id=0B8QN8De7yDDUMl9ZdWU1SGY1d1U";
                break;
            case 2:
                urlString = "https://drive.google.com/uc?export=download&confirm=no_antivirus&id=0B8QN8De7yDDUMl9ZdWU1SGY1d1U";
                break;
            case 3:
                urlString = "https://drive.google.com/uc?export=download&confirm=no_antivirus&id=0B8QN8De7yDDUMl9ZdWU1SGY1d1U";
                break;
            case 4:
                urlString = "https://drive.google.com/uc?export=download&confirm=no_antivirus&id=0B8QN8De7yDDUMl9ZdWU1SGY1d1U";
                break;
            default:
                urlString = "https://drive.google.com/uc?export=download&confirm=no_antivirus&id=0B8QN8De7yDDUMl9ZdWU1SGY1d1U";
                break;
        }

        try {

            URL url = new URL(urlString);


            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setReadTimeout(100000);
            conn.setConnectTimeout(100000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            InputStream stream = conn.getInputStream();
            String data = convertStreamToString(stream);

            int index = mMainActivity.Group;

            JSONArray main = new JSONArray(data);

            JSONObject group = main.getJSONObject(index);

            JSONArray weekdays = group.getJSONArray("weekday");

            JSONObject monday = weekdays.getJSONObject(0);

            JSONArray weeks = monday.getJSONArray("week");

            JSONObject monday0 = weeks.getJSONObject(0);

            JSONArray lessons = monday0.getJSONArray("lessonweek");

            times = new String[lessons.length()];
            titles = new String[lessons.length()];
            lecturers = new String[lessons.length()];
            rooms = new String[lessons.length()];

            for (int i = 0; i < lessons.length(); i++) {
                times[i] = " ";
                titles[i] = " ";
                lecturers[i] = " ";
                rooms[i] = " ";
            }

            for (int i = 0; i < lessons.length(); i++) {

                JSONObject lesson = lessons.getJSONObject(i);
                times[i] = lesson.getString("time");
                titles[i] = lesson.getString("lesson");
                lecturers[i] = lesson.getString("lecturer");
                rooms[i] = lesson.getString("room");
            }

            ArrayList<HashMap<String, String>> myArrList = new ArrayList<HashMap<String, String>>();
            HashMap<String, String> map;


            for (int i = 0; i < times.length; i++) {
                map = new HashMap<String, String>();
                map.put("times", times[i]);
                map.put("titles", titles[i]);
                map.put("lecturers", lecturers[i]);
                map.put("rooms", rooms[i]);


                myArrList.add(map);
            }

            mShedSimpleAdapter = new SimpleAdapter(mMainActivity.getApplicationContext(), myArrList, R.layout.item_timetable,
                    new String[]{"times", "titles", "lecturers", "rooms"},
                    new int[]{R.id.textViewTimeBegin, R.id.textViewTitle, R.id.textViewLector, R.id.textViewRoom});

            stream.close();
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();// ошибку поймал
        }

        return mShedSimpleAdapter;
    }

    @Override
    protected void onPostExecute(SimpleAdapter listAdapter) {

        mMainActivity.ProgressBar.setVisibility(ProgressBar.INVISIBLE);
        super.onPostExecute(listAdapter);
        mMainActivity.List.setAdapter(listAdapter);

    }

    static String convertStreamToString(InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}

