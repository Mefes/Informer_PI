package com.example.vladimir.informer_pi;


import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.perm.kate.api.Api;
import com.perm.kate.api.KException;
import com.perm.kate.api.WallMessage;

import org.json.JSONException;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static final String APP_PREFERENCES = "studentsetting";
    public static final String APP_PREFERENCES_COURSE = "studentcourse";
    public static final String APP_PREFERENCES_GROUP = "studentgroup";

    HashMap<String, String> map;
    Handler handler;
    ArrayList<HashMap<String, String>> myArrList = new ArrayList<HashMap<String, String>>();
    ArrayList<WallMessage> wallMessages = null;
    public static Api api;
    public static Long GROUP_ID = -30617342l;
    public final int REQUEST_LOGIN = 1;
    public static String API_ID = "4656198";
    Integer count, course;
    boolean bRadioOn;
    Intent radioIntent = null;
    public static Account account;
    ArrayList<String> news = new ArrayList<String>();
    String group;
    private SharedPreferences studentPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        count = 20;
        account = new Account();
        account.restore(this);
        if (account.access_token != null) {
            api = new Api(account.access_token, API_ID);
        } else {
            Intent intent = new Intent();
            intent.setClass(this, LoginActivity.class);
            startActivityForResult(intent, REQUEST_LOGIN);
        }
        TelephonyManager TelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        TelephonyMgr.listen(new TeleListener(),
                PhoneStateListener.LISTEN_CALL_STATE);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                {
                    if (radioIntent == null) {
                        bRadioOn = true;
                        radioIntent = new Intent(getApplicationContext(), Radio.class);
                        startService(radioIntent);
                        setnewadapter();
                    } else {
                        try {
                            if (bRadioOn == false) {
                                bRadioOn = true;
                            } else {
                                bRadioOn = false;
                            }
                            startService(radioIntent);
                            setnewadapter();
                        } catch (Exception e) {
                            Log.d("onCreate", "" + e);
                        }

                    }
//                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                            .setAction("Action", null).show();
                }
            }
        });


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item_news clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item_news clicks here.
        handler = new Handler() {
            @Override
            public void handleMessage(android.os.Message msg) {

                String text = (String) msg.obj;
                news.add(text);
                showTable();
            }
        };

        int id = item.getItemId();

        if (id == R.id.nav_news) {
            new Thread() {
                public void run() {

                    try {
                        wallMessages = api.getWallMessages(GROUP_ID, count, 0, "all");//Получение Новостей в формате JSON
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (KException e) {
                        e.printStackTrace();
                    }
                    for (int i = 0; i < count; ++i) {
                        map = new HashMap<String, String>();
//                        Integer likes = wallMessages.get(i).like_count;
                        map.put("likes", wallMessages.get(i).like_count + " лаек");
//                            map.put("photo",); // получение фотографии
                        if (wallMessages.get(i).copy_history == null) {
                            String s1 = wallMessages.get(i).text;
//                            int start = s1.indexOf("#");
//                            int end = s1.indexOf("@");
                            char[] buf = new char[s1.indexOf("@") - s1.indexOf("#")];
                            s1.getChars(s1.indexOf("#"), s1.indexOf("@"), buf, 0);
                            String TAG = new String(buf);
                            map.put("tag", TAG);
//                            //заголовок
//                            start = s1.indexOf("| ") + 2;
//                            end = s1.indexOf("\n\n");
                            buf = new char[(s1.indexOf("\n\n")) - (s1.indexOf("| ") + 2)];
                            s1.getChars(s1.indexOf("| ") + 2, s1.indexOf("\n\n"), buf, 0);
                            String Title = new String(buf);
                            map.put("title", Title);
                            //дата
//                            long time = wallMessages.get(i).date * (long) 1000;
//                            Date date = new Date(time);
//                            SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy HH:mm:ss"));
//                            String Time = format.format(date);
//новое заполнение даты
                            map.put("date", new SimpleDateFormat("dd MMM yyyy HH:mm:ss").format(new Date(wallMessages.get(i).date * (long) 1000)));
                            //новости
//                            start = s1.indexOf("\n\n") + 2;
//                            end = s1.length();
                            buf = new char[(s1.length()) - (s1.indexOf("\n\n") + 2)];
                            s1.getChars(s1.indexOf("\n\n") + 2, s1.length(), buf, 0);
                            s1 = new String(buf);
                            map.put("text", s1);
                            myArrList.add(map);
                            Message msg = new Message();
                            handler.sendMessage(msg);

                        } else {
                            ArrayList<WallMessage> wallMessagesRepost = wallMessages.get(i).copy_history;

                            String s1 = wallMessagesRepost.get(0).text;

                            while (wallMessagesRepost.get(0).copy_history != null) {
                                wallMessagesRepost = wallMessagesRepost.get(0).copy_history;
                                s1 = s1 + wallMessagesRepost.get(0).text;
                            }
                            Message msg = new Message();
                            map.put("text", s1);
                            myArrList.add(map);
                            handler.sendMessage(msg);

                        }

                    }
//
//                    FragmentManager fragmentManager = getSupportFragmentManager();
//                            fragmentManager.beginTransaction()
//                                    .replace(R.id.container,MainPageFragment.newInstance())
//                                    .commit();
                }

            }.start();
        } else if (id == R.id.nav_timetable) {
//            //необходимо произвести проверку на сушествовения данных об студенте
//            DialogTimetableFragment dialogTimetableFragment = new DialogTimetableFragment();
//            FragmentManager manager = getSupportFragmentManager();
//            //myDialogFragment.show(manager, "dialog");
//
//            FragmentTransaction transaction = manager.beginTransaction();
//            dialogTimetableFragment.show(transaction, "dialog");
//
//            AsyncHandleGroupNameJSON mAsyncHandleGroupNameJSON = new AsyncHandleGroupNameJSON();
//            mAsyncHandleGroupNameJSON.execute(MainActivity.this);
//            //тут поиск выбор группы
//            DialogTimetableFragment dialogFragment = new DialogTimetableFragment();
//            FragmentManager manager = getSupportFragmentManager();
//            FragmentTransaction transaction = manager.beginTransaction();
//            dialogFragment.show(transaction, "dialog");
//            AsyncHandleShedJSON mAsyncHandleShedJSON = new AsyncHandleShedJSON();
//            mAsyncHandleShedJSON.execute(this);
            //до сюда
        } else if (id == R.id.nav_struct) {

        } else if (id == R.id.nav_web) {
            // создаём намерение для вызова поиска в интернете информации об институте
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://polytech.sfu-kras.ru"));
            // пока что выводит только результат запроса
            // "Политехнический институт СФУ"
            // запускаем браузер, выводим результат поиска
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, R.string.app_not_available,
                        Toast.LENGTH_LONG).show();
            }

        } else if (id == R.id.nav_radio) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_LOGIN) {
            if (resultCode == RESULT_OK) {
                //авторизовались успешно
                account.access_token = data.getStringExtra("token");
                account.user_id = data.getLongExtra("user_id", 0);
                account.save(MainActivity.this);
                api = new Api(account.access_token, API_ID);
            }
        }
    }

    private void showTable() {

        SimpleAdapter VKSimpleAdapter = new SimpleAdapter(getApplicationContext(), myArrList, R.layout.item_news,
                new String[]{"tag", "title", "text", "likes", "date"},
                new int[]{R.id.textViewTag, R.id.textViewTitle, R.id.textViewNews, R.id.textViewLikes, R.id.textViewDate});
        ListView list = (ListView) findViewById(R.id.listView);
        // устанавливаем адаптер списку
        list.setAdapter(VKSimpleAdapter);
    }

    public void setnewadapter() {
//        mList = new ArrayList<HashMap<String, String>>();
//        if (bRadioOn==false){
//            for (int i = 0; i < mPageTitles.length; i++) {
//                HashMap<String, String> hm = new HashMap<String, String>();
//                hm.put(ITEM_MENU_TITLE, mPageTitles[i]);
//                hm.put(COUNT, " ");
//                hm.put(ITEM_MENU_ICON, Integer.toString(mItemsIcons[i]));
//                mList.add(hm);
//            }}else{
//            for (int i = 0; i < mPageTitles.length; i++) {
//                HashMap<String, String> hm = new HashMap<String, String>();
//                hm.put(ITEM_MENU_TITLE, mPageTitles[i]);
//                hm.put(COUNT, " ");
//                hm.put(ITEM_MENU_ICON, Integer.toString(mItemsIconsChange[i]));
//                mList.add(hm);
//
//            }}
//
//        String[] from = { ITEM_MENU_ICON, ITEM_MENU_TITLE, COUNT };
//
//        int[] to = { R.id.menu_item_icon, R.id.menu_item_title, R.id.count };
//
//        mAdapter = new SimpleAdapter(getActionBar().getThemedContext(), mList,
//                R.layout.drawer_list_item, from, to);
//
//        mMenuList.setAdapter(mAdapter);
    }

    public void setPreference() {
        studentPreference = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = studentPreference.edit();
        editor.putInt(APP_PREFERENCES_COURSE, course);
        editor.putString(APP_PREFERENCES_GROUP, group);
        editor.apply();
    }

    private class TeleListener extends PhoneStateListener {
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    // CALL_STATE_IDLE;
                    if (radioIntent == null) {
                    } else {
                        if (bRadioOn == true) {
                            startService(radioIntent);
                        }
                    }

                    Toast.makeText(getApplicationContext(), "CALL_STATE_IDLE",
                            Toast.LENGTH_LONG).show();
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    // CALL_STATE_OFFHOOK;
                    Toast.makeText(getApplicationContext(), "CALL_STATE_OFFHOOK",
                            Toast.LENGTH_LONG).show();
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    // CALL_STATE_RINGING
                    if (bRadioOn == true) {
                        if (radioIntent == null) {

                        } else {
                            startService(radioIntent);
                        }
                    }
                    Toast.makeText(getApplicationContext(), incomingNumber,
                            Toast.LENGTH_LONG).show();

                    break;
                default:
                    break;
            }
        }


    }
}