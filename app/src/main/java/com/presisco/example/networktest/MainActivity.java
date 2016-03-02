package com.presisco.example.networktest;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    TextView mContentText=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContentText=(TextView)findViewById(R.id.contentTextView);
    }

    public void onRequest(View v){
        new NetwortTask().execute();
    }

    private class NetwortTask extends AsyncTask<Void,Void,String>{
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            mContentText.setText(result);
        }

        @Override
        protected String doInBackground(Void... params) {
            String result=null;
            try {
                URL url = new URL("http://yuyue.juneberry.cn/");
                HttpURLConnection conn=(HttpURLConnection)url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();
                InputStream is=conn.getInputStream();
                Reader r=new InputStreamReader(is,"UTF-8");
                char[] buffer = new char[100000];
                r.read(buffer);
                result= new String(buffer);
//                conn.setRequestMethod("POST");
//                conn.setDoOutput(true);
//                conn.setRequestProperty("subCmd","Login");
//                conn.setRequestProperty("txt_LoginID","");
//                conn.setRequestProperty("txt_password","");
//                conn.setRequestProperty("selSchool","15");
//
//                postParameters.add(new BasicNameValuePair("subCmd", "Login"));
//                postParameters
//                        .add(new BasicNameValuePair("txt_LoginID", id));
//                postParameters.add(new BasicNameValuePair("txt_Password", password));
//                postParameters.add(new BasicNameValuePair("selSchool", "15"));
//
//                String reqUrl = "http://yuyue.juneberry.cn/";
//                String varString = HttpTools.GetHTTPRequest(reqUrl, client);
//                String []strs = parseEandVAttri(varString).split(",");
//                postParameters.add(new BasicNameValuePair("__EVENTVALIDATION", strs[1]));
//                postParameters.add(new BasicNameValuePair("__VIEWSTATE", strs[0]));
            }catch(Exception e){
                e.printStackTrace();
            }
            return result;
        }
    }
}
