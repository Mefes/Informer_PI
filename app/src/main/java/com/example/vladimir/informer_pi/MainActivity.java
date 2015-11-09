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
    //  public static final   int [] NewsIcons = new int[R.drawable.ic_action_play];
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
    public static final String DATE_FORMAT = "dd MMM yyyy HH:mm:ss";
    public static final String URL_POLYTECHNIC = "http://polytech.sfu-kras.ru";
    public static final Long GROUP_ID = -30617342l;
    public static final String API_ID = "4656198";
    static final private int REQUEST_LOGIN = 1;
    public static final Integer NEWS_COUNT = 10;
    static final private int REQUEST_SETTINGS = 0;
    public static Api Api;
    public static Account Account;
    SharedPreferences studentPreference;
    boolean bRadioOn;
    Integer Course, Group;
    ArrayList<HashMap<String, String>> NewsArrList = new ArrayList<HashMap<String, String>>();
    ArrayList<WallMessage> WallMessages = null;
    HashMap<String, String> Map;
    NewsTask NewsTask;
    Intent RadioIntent = null;
    ListView List;
    ProgressBar ProgressBar;
    Toolbar Toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setInterface();
        setSupportActionBar(Toolbar);
        Account = new Account();
        Account.restore(this);
        TelephonyManager TelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        TelephonyMgr.listen(new TeleListener(),
                PhoneStateListener.LISTEN_CALL_STATE);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                {
                    if (RadioIntent == null) {
                        bRadioOn = true;
                        RadioIntent = new Intent(getApplicationContext(), Radio.class);
                        startService(RadioIntent);
                        setnewadapter();
                    } else {
                        try {
                            bRadioOn = !bRadioOn;
                            startService(RadioIntent);
                            setnewadapter();
                        } catch (Exception e) {
//Log.d("const",)
                        }
                    }
                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, Toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        getPreference();
        if (studentPreference.contains(APP_TAG_ARRAY + 1)) {
            loadNews();
        } else {
            if (!isOnline()) {// проверка подключения
                Toast.makeText(getApplicationContext(),
                        "Нет соединения с интернетом!", Toast.LENGTH_LONG).show();
            } else {

                if (Account.access_token != null) {
                    Api = new Api(Account.access_token, API_ID);
                    if (NewsTask != null) {
                    } else {
                        NewsTask = new NewsTask();
                        NewsTask.execute();
                    }
                } else {
                    Intent intent = new Intent();
                    intent.setClass(this, LoginActivity.class);
                    startActivityForResult(intent, REQUEST_LOGIN);
                    if (Account.access_token != null) {
                        if (NewsTask != null) {
                        } else {
                            NewsTask = new NewsTask();
                            NewsTask.execute();
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
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            intent.putExtra("course", Course);
            intent.putExtra("group", Group);
            startActivityForResult(intent, REQUEST_SETTINGS);

            // загрузка окна с настройками
            return true;
        }
        if (id == R.id.inst) {
            Intent intent = new Intent(MainActivity.this, InstStructActivity.class);
            startActivity(intent);
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
                if (NewsTask != null) {

                } else {
                    NewsTask = new NewsTask();
                    NewsTask.execute();
                }
            }

        } else if (id == R.id.nav_timetable) {
            ProgressBar.setVisibility(ProgressBar.VISIBLE);
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
        if (requestCode == REQUEST_SETTINGS) {
            if (resultCode == RESULT_OK) {
                Course = data.getIntExtra("course", -1);
                Group = data.getIntExtra("group", -1);
                setPreference();
            } else {
//                infoTextView.setText(""); // стираем текст
            }
        }
        if (requestCode == REQUEST_LOGIN) {
            if (resultCode == RESULT_OK) {
                //авторизовались успешно
                Account.access_token = data.getStringExtra("token");
                Account.user_id = data.getLongExtra("user_id", 0);
                Account.save(MainActivity.this);
                Api = new Api(Account.access_token, API_ID);
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
        SimpleAdapter VKSimpleAdapter = new SimpleAdapter(getApplicationContext(), NewsArrList, R.layout.item_news,
                new String[]{APP_TAG, APP_TITLE, APP_TEXT, APP_LIKES, APP_DATE},
                new int[]{R.id.textViewTag, R.id.textViewTitle, R.id.textViewNews, R.id.textViewLikes, R.id.textViewDate});
        // устанавливаем адаптер списку
        List.setAdapter(VKSimpleAdapter);
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
//        studentPreference = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        if (studentPreference.contains(APP_PREFERENCES_GROUP) &&
                studentPreference.contains(APP_PREFERENCES_COURSE)) {
            Course = studentPreference.getInt(APP_PREFERENCES_COURSE, 0);
            Group = studentPreference.getInt(APP_PREFERENCES_GROUP, 0);

        } else {
            DialogGroupFragment dialogGroupFragment = new DialogGroupFragment();
            FragmentManager groupManager = getSupportFragmentManager();
            FragmentTransaction groupTransaction = groupManager.beginTransaction();
            dialogGroupFragment.show(groupTransaction, "dialog1");

            DialogCourseFragment dialogCourseFragment = new DialogCourseFragment();
            FragmentManager courseManager = getSupportFragmentManager();
            FragmentTransaction courseTransaction = courseManager.beginTransaction();
            dialogCourseFragment.show(courseTransaction, "dialog2");
        }

    }

    protected void setPreference() {
        SharedPreferences.Editor editor = studentPreference.edit();
        editor.putInt(APP_PREFERENCES_COURSE, Course);
        editor.putInt(APP_PREFERENCES_GROUP, Group);
        editor.apply();
    }

    protected void loadNews() {

        for (int i = 0; i < NEWS_COUNT; i++) {
            Map = new HashMap<>();
            Map.put(APP_TAG, studentPreference.getString(APP_TAG_ARRAY + i, ""));
            Map.put(APP_TITLE, studentPreference.getString(APP_TITLE_ARRAY + i, ""));
            Map.put(APP_TEXT, studentPreference.getString(APP_TEXT_ARRAY + i, ""));
            Map.put(APP_LIKES, studentPreference.getString(APP_LIKES_ARRAY + i, ""));
            Map.put(APP_DATE, studentPreference.getString(APP_DATE_ARRAY + i, ""));
            NewsArrList.add(Map);
        }
        showTable();
    }

    protected void saveNews(int i) {
        SharedPreferences.Editor editor = studentPreference.edit();
        editor.putString(APP_TAG_ARRAY + i, Map.get(APP_TAG)); //складываем элементы массива
        editor.putString(APP_TITLE_ARRAY + i, Map.get(APP_TITLE));
        editor.putString(APP_TEXT_ARRAY + i, Map.get(APP_TEXT));
        editor.putString(APP_LIKES_ARRAY + i, Map.get(APP_LIKES));
        editor.putString(APP_DATE_ARRAY + i, Map.get(APP_DATE));
        editor.apply();
    }

    private class TeleListener extends PhoneStateListener {
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    // CALL_STATE_IDLE;
                    if (RadioIntent == null) {
                    } else {
                        if (bRadioOn) {
                            startService(RadioIntent);
                        }
                    }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    // CALL_STATE_OFFHOOK;
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    // CALL_STATE_RINGING
                    if (bRadioOn) {
                        if (RadioIntent == null) {
                        } else {
                            startService(RadioIntent);
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
            ProgressBar.setVisibility(ProgressBar.VISIBLE);
        }


        @Override
        protected Void doInBackground(Void... params) {
            if (Api == null) {
                Api = new Api(Account.access_token, API_ID);
            }
            try {
                WallMessages = Api.getWallMessages(GROUP_ID, NEWS_COUNT, 0, "all");//Получение Новостей в формате JSON
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (KException e) {
                e.printStackTrace();
            }
            NewsArrList.clear();
            for (int i = 0; i < NEWS_COUNT; ++i) {
                Map = new HashMap<String, String>();
                Map.put(APP_LIKES, WallMessages.get(i).like_count + " лаек");
//                            map.put("photo",); // получение фотографии
                if (WallMessages.get(i).copy_history == null) {
                    String s1 = WallMessages.get(i).text;
                    //ТЭГ
                    char[] buf = new char[s1.indexOf(" |") - s1.indexOf("#")];
                    s1.getChars(s1.indexOf("#"), s1.indexOf(" |"), buf, 0);
                    String TAG = new String(buf);
                    Map.put(APP_TAG, TAG);
                    //заголовок
                    buf = new char[(s1.indexOf("\n\n")) - (s1.indexOf("| ") + 2)];
                    s1.getChars(s1.indexOf("| ") + 2, s1.indexOf("\n\n"), buf, 0);
                    String Title = new String(buf);
                    Map.put(APP_TITLE, Title);
                    //новое заполнение даты
                    Map.put(APP_DATE, new SimpleDateFormat(DATE_FORMAT).format(new Date(WallMessages.get(i).date * (long) 1000)));
                    //новости
                    buf = new char[(s1.length()) - (s1.indexOf("\n\n") + 2)];
                    s1.getChars(s1.indexOf("\n\n") + 2, s1.length(), buf, 0);
                    s1 = new String(buf);
                    Map.put(APP_TEXT, s1);
                    NewsArrList.add(Map);

                } else {
                    ArrayList<WallMessage> wallMessagesRepost = WallMessages.get(i).copy_history;
                    String s1 = wallMessagesRepost.get(0).text;
                    while (wallMessagesRepost.get(0).copy_history != null) {
                        wallMessagesRepost = wallMessagesRepost.get(0).copy_history;
                        s1 = s1 + wallMessagesRepost.get(0).text;
                    }
                    Map.put(APP_TEXT, s1);
                    NewsArrList.add(Map);
                }
                saveNews(i);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            showTable();
            ProgressBar.setVisibility(ProgressBar.INVISIBLE);
            NewsTask = null;
            super.onPostExecute(aVoid);
        }
    }

    private void setInterface() {
        studentPreference = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        List = (ListView) findViewById(R.id.listView);
        ProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        Toolbar = (Toolbar) findViewById(R.id.toolbar);

    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);
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