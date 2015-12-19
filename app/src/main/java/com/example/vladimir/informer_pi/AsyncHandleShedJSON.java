package com.example.vladimir.informer_pi;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
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
    String WhatSearching;
    String Data;
    //    String[] SearchingArray;
    String FILENAME = "timetable.txt";
    String sss;
    ArrayList<HashMap<String, String>> myArrList = new ArrayList<HashMap<String, String>>();
    HashMap<String, String> map;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected SimpleAdapter doInBackground(AppCompatActivity... params) {
        SimpleAdapter mShedSimpleAdapter = null;
        mMainActivity = (MainActivity) params[0];
//        int Course = mMainActivity.Course;
        WhatSearching = mMainActivity.SearchingTextView.getText().toString();
        for (int i = 0; i < mMainActivity.SearchingArray.length; i++) {
            if (WhatSearching == mMainActivity.SearchingArray[i]) {

            }
        }
        WhatSearching.toUpperCase();
        WhatSearching.replace("М", "M"); // механика,магистратура
        WhatSearching.replace("Т", "T"); // технологический,транспорт
        WhatSearching.replace("Ф", "F"); // факультет
        WhatSearching.replace("Э", "E"); // энергетика
        WhatSearching.replace("С", "С"); // специалисты
        WhatSearching.replace("Б", "B"); // бакалавриат
        WhatSearching.replace("ПОДГРУППА", "p");

        //TODO: если текст в компоненте не совподает с названием курса из всех названий курсов  то выходи, а иначе делай то

        urlString = "http://www.dennismorosoff.com/files/" + WhatSearching + ".txt";
        FILENAME = WhatSearching + ".txt";
        try {
            Context context = mMainActivity;
            File file = new File(context.getFilesDir(), FILENAME);
            if (file.exists() && file.isFile()) {
                Data = WorkData.loadData(new InputStreamReader(mMainActivity.openFileInput(FILENAME)));
            } else {
//                Data =WorkData.connectAndDownload(urlString);
                WorkData.saveData(new OutputStreamWriter(mMainActivity.openFileOutput(FILENAME, Context.MODE_PRIVATE)), Data);
            }
//            JSONArray main = new JSONArray(Data);//Err загрузка долгая


            // TODO: проверка какая группа введена и выдавать расписание, а также сделать для каждого дня недели
//            int index = mMainActivity.Group;

//            mMainActivity.SearchingTextView.setAdapter(new ArrayAdapter<String>(mMainActivity));
//            for (int groupCount = 0; groupCount < main.length(); groupCount++) {
//                if (main.getString(groupCount).equals(WhatSearching)) {
            JSONObject group = new JSONObject(Data);// TODO: переработать JSON
            for (int dayCount = 0; dayCount < 6; dayCount++) {
                JSONArray weekdays = group.getJSONArray("weekday");
                JSONObject monday = weekdays.getJSONObject(dayCount);
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

                for (int i = 0; i < times.length; i++) {
                    map = new HashMap<String, String>();
                    map.put("times", times[i]);
                    map.put("titles", titles[i]);
                    map.put("lecturers", lecturers[i]);
                    map.put("rooms", rooms[i]);
                    myArrList.add(map);
                }
            }
//                    break;
//                }
//            }
            mShedSimpleAdapter = new SimpleAdapter(mMainActivity.getApplicationContext(), myArrList, R.layout.item_timetable,
                    new String[]{"times", "titles", "lecturers", "rooms"},
                    new int[]{R.id.textViewTimeBegin, R.id.textViewTitle, R.id.textViewLector, R.id.textViewRoom});

        } catch (Exception e) {
            e.printStackTrace();// Err ошибку поймал
        }

        return mShedSimpleAdapter;
    }

    @Override
    protected void onPostExecute(SimpleAdapter listAdapter) {

//        mMainActivity.SearchingTextView.setAdapter(new ArrayAdapter<String>(mMainActivity, android.R.layout.simple_dropdown_item_1line, mMainActivity.SearchingArray));
        mMainActivity.ProgressBar.setVisibility(ProgressBar.INVISIBLE);
        super.onPostExecute(listAdapter);
        mMainActivity.List.setAdapter(listAdapter);

    }

//    static String convertStreamToString(InputStream is) {
//        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
//        return s.hasNext() ? s.next() : "";
//    }

//    private void saveData(String InputDataStream) {
//        try {
//
//            //создание файла и сохранение
//            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(
//                    mMainActivity.openFileOutput(FILENAME, Context.MODE_PRIVATE)));
//            bufferedWriter.write(InputDataStream);
//            bufferedWriter.flush();
//            bufferedWriter.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void loadData() {
//
//        try {
//            // открываем поток для чтения
//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
//                    mMainActivity.openFileInput(FILENAME)));
//            Data = "";
//            // читаем содержимое
//            while ((Data = Data + bufferedReader.readLine()) != null) {
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void connectAndDownload(String urlStr) {
//        try {
//            URL url = new URL(urlStr);
//            HttpURLConnection conn = (HttpURLConnection)
//                    url.openConnection();
//            conn.setReadTimeout(100000);
//            conn.setConnectTimeout(100000);
//            conn.setRequestMethod("GET");
//            conn.setDoInput(true);
//            InputStream stream = conn.getInputStream();
//            Data = convertStreamToString(stream);
//            saveData(Data);
//            stream.close();
//            conn.disconnect();
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        } catch (ProtocolException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
//    /**
//     * Копирует файл из ресурсов
//     */
//    public static void copyFileFromRaw(Context mContext,String FILE_NAME)
//    {
//
////        getCurrentVersionCode();
//        File file1 = new File(FILE_PATH);
//        try
//        {
//            //проверяем существует ли такой файл, если нет, то выпадет ошибка и нужно копровать, если нет, то ничего делать не будем
//            mContext.openFileInput(FILE_NAME);
//        }
//        catch (IOException e)
//        {
//            e.printStackTrace();
//
//            try
//            {
//                InputStream inputStream = this.getResources().openRawResource(R.raw.file);
//
//                //Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE - дает доступ другим приложениям к этому файлу, если оне не нужен то можно
//                //поставить Context.Context.MODE_PRIVATE,
//                FileOutputStream fout = mContext.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
//                copyFromTo(inputStream, fout);
//            }
//            catch (IOException e1)
//            {
//                e1.printStackTrace();
//            }
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//        }
//
//    }
//
//    //выполняет запрос для текстового файла
//    public static String executeHttpGet(String uri) throws Exception
//    {
//
//        String result = "";
//        try
//        {
//
//            URL url = new URL(uri);
//            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
//            String str;
//            while ((str = in.readLine()) != null)
//            {
//                result +=str;
//            }
//            in.close();
//        }
//        catch (MalformedURLException e)
//        {
//            e.printStackTrace();
//        }
//        catch (IOException e)
//        {
//            e.printStackTrace();
//        }
//        return result;
//    }
//
//    //получет по ссылке новое содержимое файла, и перезаписывает
//    public void rewriteFileFromUrl(String url)
//    {
//        String file_souce = executeHttpGet(url);
//        rewriteUpdateFile(file_souce,FILE_NAME);
//    }
//
//    /**
//     * Переписывает файл
//     * тут, когда вы получите текст по ссылке, то просто посылаем в этот метод
//     * @param str - текст файла
//     */
//    private void rewriteUpdateFile(String str, String FILE_NAME)
//    {
//
//        try
//        {
//            InputStream is = new ByteArrayInputStream(str.getBytes());
//            FileOutputStream fout = mContext.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
//            copyFromTo(is, fout);
//        }
//        catch (IOException e1)
//        {
//            e1.printStackTrace();
//        }
//
//    }
//
//    /**
//     * Копирует из одного потока в другой информацию
//     * @param fin поток копируемого файла
//     * @param fout поток файла в который будет все записано
//     * @throws IOException
//     */
//    private void copyFromTo(InputStream fin, OutputStream fout) throws IOException
//    {
//        byte[] b = new byte[1024];
//        int noOfBytes = 0;
//
//        while ((noOfBytes = fin.read(b)) != -1)
//        {
//            fout.write(b, 0, noOfBytes);
//        }
//
//        fin.close();
//        fout.close();
//    }
//}
//
//
