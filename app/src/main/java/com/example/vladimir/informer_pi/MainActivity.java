package com.example.vladimir.informer_pi;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BaseBundle;
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
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.perm.kate.api.Api;
import com.perm.kate.api.KException;
import com.perm.kate.api.WallMessage;

import org.json.JSONException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static final String APP_PREFERENCES = "studentsetting";
    public static final String APP_PREFERENCES_COURSE = "studentcourse";
    public static final String APP_PREFERENCES_GROUP = "studentgroup";
    public static final String APP_TITLE_ARRAY = "titleapparray";
    public static final String APP_TAG_ARRAY = "tagapparray";
    public static final String APP_TEXT_ARRAY = "textapparray";
    public static final String APP_LIKES_ARRAY = "likesapparray";
    public static final String APP_DATE_ARRAY = "dateapparray";
    public static final String APP_TAG = "tag";
    public static final String APP_TITLE = "title";
    public static final String APP_TEXT = "text";
    public static final String APP_LIKES = "likes";
    public static final String APP_DATE = "date";
    public static final String URL_POLYTECHNIC = "http://polytech.sfu-kras.ru";
    public static final Long GROUP_ID = -30617342l;
    public static final String API_ID = "4656198";
    public static final int REQUEST_LOGIN = 1;
    public static Api api;
    public static Account account;
    SharedPreferences studentPreference;
    boolean bRadioOn;
    Integer count = 10, course, group;
    ArrayList<HashMap<String, String>> myArrList = new ArrayList<HashMap<String, String>>();
    ArrayList<WallMessage> wallMessages = null;
    HashMap<String, String> map;
    NewsTask newsTask;
    Intent radioIntent = null;
    ListView list;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        account = new Account();
        account.restore(this);
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

                        }
                    }
                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
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
        getPreference();
        if (studentPreference.contains(APP_TAG_ARRAY + 1) == true) {
            loadNews();
        } else {
            if (!isOnline()) {// проверка подключения
                Toast.makeText(getApplicationContext(),
                        "Нет соединения с интернетом!", Toast.LENGTH_LONG).show();
            } else {

                if (account.access_token != null) {
                    api = new Api(account.access_token, API_ID);
                    if (newsTask != null) {
                    } else {
                        newsTask = new NewsTask();
                        newsTask.execute();
                    }
                } else {
                    Intent intent = new Intent();
                    intent.setClass(this, LoginActivity.class);
                    startActivityForResult(intent, REQUEST_LOGIN);
                    if (account.access_token != null) {
                        if (newsTask != null) {
                        } else {
                            newsTask = new NewsTask();
                            newsTask.execute();
                        }
                    }
                }
            }
        }
        setNewsIcon();

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

            // загрузка окна с настройками
            return true;
        }
        if (id == R.id.inst) {
            //загрузка статичной окошка с структурой политеха
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item_news clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_news) {
            if (!isOnline()) {
                Toast.makeText(getApplicationContext(),
                        "Нет соединения с интернетом!", Toast.LENGTH_LONG).show();
            } else {
                if (newsTask != null) {
                } else {
                    newsTask = new NewsTask();
                    newsTask.execute();
                }
            }

        } else if (id == R.id.nav_timetable) {
            progressBar.setVisibility(ProgressBar.VISIBLE);
            AsyncHandleShedJSON mAsyncHandleShedJSON = new AsyncHandleShedJSON();
            mAsyncHandleShedJSON.execute(MainActivity.this);
        } else if (id == R.id.nav_struct) {

        } else if (id == R.id.nav_web) {
            if (!isOnline()) {
                Toast.makeText(getApplicationContext(),
                        "Нет соединения с интернетом!", Toast.LENGTH_LONG).show();
            } else {
                // создаём намерение для вызова поиска в интернете информации об институте
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL_POLYTECHNIC));
                // "Политехнический институт СФУ"
                // запускаем браузер, выводим результат поиска
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(this, R.string.app_not_available,
                            Toast.LENGTH_LONG).show();
                }
            }
        } else if (id == R.id.nav_techTV) {

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

    public void setNewsIcon() {
//    newsTag.put("#Новости@pisfu",1);//1
//    newsTag.put("#Фото@pisfu",2);//2
//    newsTag.put("#Утро@pisfu",3);//3
//    newsTag.put("#ППОС@pisfu",4);//4
//    newsTag.put("#PIFM@pisfu",5);//5
//    newsTag.put("#СТК@pisfu",6);//6
//    newsTag.put("#6Фактов@pisfu",7);//7
//    newsTag.put("#Гид@pisfu",8);//8
//    newsTag.put("#Спорт@pisfu",9);//9
//    newsTag.put("#hi_tech@pisfu",10);//10
//    newsTag.put("#Наука@pisfu",11);//11
//    newsTag.put("#Конкурс@pisfu",12);//12
//    newsTag.put("#ПятнИца@pisfu",13);//13
    }

    private void showTable() {
        SimpleAdapter VKSimpleAdapter = new SimpleAdapter(getApplicationContext(), myArrList, R.layout.item_news,
                new String[]{APP_TAG, APP_TITLE, APP_TEXT, APP_LIKES, APP_DATE},
                new int[]{R.id.textViewTag, R.id.textViewTitle, R.id.textViewNews, R.id.textViewLikes, R.id.textViewDate});
        list = (ListView) findViewById(R.id.listView);
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

    protected void getPreference() {
        studentPreference = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if (studentPreference.contains(APP_PREFERENCES_GROUP) == true &&
                studentPreference.contains(APP_PREFERENCES_COURSE) == true) {
            course = studentPreference.getInt(APP_PREFERENCES_COURSE, 0);
            group = studentPreference.getInt(APP_PREFERENCES_GROUP, 0);

        } else {

            DialogGroupFragment dialogGroupFragment = new DialogGroupFragment();
            FragmentManager groupManager = getSupportFragmentManager();
            FragmentTransaction groupTransaction = groupManager.beginTransaction();
            dialogGroupFragment.show(groupTransaction, "dialog");

            DialogCourseFragment dialogCourseFragment = new DialogCourseFragment();
            FragmentManager courseManager = getSupportFragmentManager();
            FragmentTransaction courseTransaction = courseManager.beginTransaction();
            dialogCourseFragment.show(courseTransaction, "dialog");
        }

    }

    protected void setPreference() {
        SharedPreferences.Editor editor = studentPreference.edit();
        editor.putInt(APP_PREFERENCES_COURSE, course);
        editor.putInt(APP_PREFERENCES_GROUP, group);
        editor.apply();
    }

    protected void loadNews() {

        for (int i = 0; i < count; i++) {
            map = new HashMap<String, String>();
            map.put(APP_TAG, studentPreference.getString(APP_TAG_ARRAY + i, ""));
            map.put(APP_TITLE, studentPreference.getString(APP_TITLE_ARRAY + i, ""));
            map.put(APP_TEXT, studentPreference.getString(APP_TEXT_ARRAY + i, ""));
            map.put(APP_LIKES, studentPreference.getString(APP_LIKES_ARRAY + i, ""));
            map.put(APP_DATE, studentPreference.getString(APP_DATE_ARRAY + i, ""));
            myArrList.add(map);
        }
        showTable();
    }

    protected void saveNews(int i) {
        SharedPreferences.Editor editor = studentPreference.edit();
        editor.putString(APP_TAG_ARRAY + i, map.get(APP_TAG)); //складываем элементы массива
        editor.putString(APP_TITLE_ARRAY + i, map.get(APP_TITLE));
        editor.putString(APP_TEXT_ARRAY + i, map.get(APP_TEXT));
        editor.putString(APP_LIKES_ARRAY + i, map.get(APP_LIKES));
        editor.putString(APP_DATE_ARRAY + i, map.get(APP_DATE));
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
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    // CALL_STATE_OFFHOOK;
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    // CALL_STATE_RINGING
                    if (bRadioOn == true) {
                        if (radioIntent == null) {

                        } else {
                            startService(radioIntent);
                        }
                    }
                    break;
                default:
                    break;
            }
        }


    }

    private class NewsTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }


        @Override
        protected Void doInBackground(Void... params) {

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
                map.put(APP_LIKES, wallMessages.get(i).like_count + " лаек");
//                            map.put("photo",); // получение фотографии
                if (wallMessages.get(i).copy_history == null) {
                    String s1 = wallMessages.get(i).text;
                    //ТЭГ
                    char[] buf = new char[s1.indexOf(" |") - s1.indexOf("#")];
                    s1.getChars(s1.indexOf("#"), s1.indexOf(" |"), buf, 0);
                    String TAG = new String(buf);
                    map.put(APP_TAG, TAG);
                    //заголовок
                    buf = new char[(s1.indexOf("\n\n")) - (s1.indexOf("| ") + 2)];
                    s1.getChars(s1.indexOf("| ") + 2, s1.indexOf("\n\n"), buf, 0);
                    String Title = new String(buf);
                    map.put(APP_TITLE, Title);
                    //новое заполнение даты
                    map.put(APP_DATE, new SimpleDateFormat("dd MMM yyyy HH:mm:ss").format(new Date(wallMessages.get(i).date * (long) 1000)));
                    //новости
                    buf = new char[(s1.length()) - (s1.indexOf("\n\n") + 2)];
                    s1.getChars(s1.indexOf("\n\n") + 2, s1.length(), buf, 0);
                    s1 = new String(buf);
                    map.put(APP_TEXT, s1);
                    myArrList.add(map);

                } else {
                    ArrayList<WallMessage> wallMessagesRepost = wallMessages.get(i).copy_history;
                    String s1 = wallMessagesRepost.get(0).text;
                    while (wallMessagesRepost.get(0).copy_history != null) {
                        wallMessagesRepost = wallMessagesRepost.get(0).copy_history;
                        s1 = s1 + wallMessagesRepost.get(0).text;
                    }
                    map.put(APP_TEXT, s1);
                    myArrList.add(map);
                }
                saveNews(i);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            showTable();
            progressBar.setVisibility(ProgressBar.INVISIBLE);
            newsTask = null;
            super.onPostExecute(aVoid);
        }
    }

    protected boolean isOnline() {
        String cs = Context.CONNECTIVITY_SERVICE;
        ConnectivityManager cm = (ConnectivityManager)
                getSystemService(cs);
        if (cm.getActiveNetworkInfo() == null) {
            return false;
        } else {
            return true;
        }
    }
}