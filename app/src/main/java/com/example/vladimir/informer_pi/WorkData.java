package com.example.vladimir.informer_pi;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.widget.ArrayAdapter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by Vladimir Kadochnikov on 09.12.15.
 */
public class WorkData extends AsyncTask<String, Integer, String> {

    private static String Inform;

    public void connectAndDownload(String urlStr) {

        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection)
                    url.openConnection();
            conn.setReadTimeout(100000);
            conn.setConnectTimeout(100000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            InputStream stream = conn.getInputStream();
            Inform = convertStreamToString(stream);
//        saveData(Inform);
            stream.close();
            conn.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    static String convertStreamToString(InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public static void saveData(
            OutputStreamWriter outputStreamWriter, String InputDataStream) {
        try {
            //создание файла и сохранение
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
            bufferedWriter.write(InputDataStream);
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String loadData(InputStreamReader inputStreamReader) {

        try {
            // открываем поток для чтения
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            Inform = "";
            // читаем содержимое
            while ((Inform = Inform + bufferedReader.readLine()) != null) {
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Inform;
    }


    @Override
    protected String doInBackground(String... URLString) {
        connectAndDownload(MainActivity.URL_ROOT + URLString[0]);
//        saveData(new OutputStreamWriter(MainActivity.openFileOutput(URLString[0], Context.MODE_PRIVATE)),Inform);
        return Inform;
    }


    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        s = Inform;
    }
}
