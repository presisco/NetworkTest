package com.presisco.example.networktest;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;
import android.util.Xml;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    WebView mContentWeb=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContentWeb=(WebView)findViewById(R.id.webView);
    }

    public void onRequest(View v){
        new NetwortTask().execute();
    }

    private class NetwortTask extends AsyncTask<Void,Void,String>{
        LoginXmlPullParser loginXmlPullParser;

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            mContentWeb.loadData(result,"text/html",null);
        }

        @Override
        protected String doInBackground(Void... params) {
            String result="";
            try {
                //get necessary params
                URL url = new URL("http://yuyue.juneberry.cn/");
                HttpURLConnection conn=(HttpURLConnection)url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();
                InputStream is=conn.getInputStream();
                loginXmlPullParser =new LoginXmlPullParser();
                loginXmlPullParser.parse(is, new String[]{"html", "body", "form"}, "input");
                is.close();
                conn.disconnect();

                //login
                conn=(HttpURLConnection)url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.connect();
                DataOutputStream out=new DataOutputStream(conn.getOutputStream());

                List<Pair> formParams=new ArrayList<Pair>();
                formParams.add(new Pair("subCmd", "Login"));
                formParams.add(new Pair("txt_LoginID", "201100800169"));
                formParams.add(new Pair("txt_Password", "011796"));
                formParams.add(new Pair("selSchool", "15"));
                formParams.add(new Pair("__EVENTVALIDATION", loginXmlPullParser.event_validation));
                formParams.add(new Pair("__VIEWSTATE", loginXmlPullParser.view_state));
                out.writeUTF(genForm(formParams));
                out.flush();
                out.close();
                conn.disconnect();

                //get floor info
                url=new URL("http://yuyue.juneberry.cn/ReadingRoomInfos/ReadingRoomState.aspx");
                conn=(HttpURLConnection)url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();
                is=conn.getInputStream();

                Reader reader = null;
                reader = new InputStreamReader(is, "UTF-8");
                char[] buffer = new char[10000];
                reader.read(buffer);
                result=new String(buffer);
                //FloorInfoXmlPullParser floorInfoXmlPullParser=new FloorInfoXmlPullParser();
                //floorInfoXmlPullParser.parse(is,new String[]{"html","body","form","div","div"},"ul");
            }catch(Exception e){
                e.printStackTrace();
            }
            return result;
        }
    }

    public static String genForm(List<Pair> data){
        String result="";
        if(data.size()<1)
            return result;
        for(int i=0;i<data.size()-1;++i){
            Pair valuePair=data.get(i);
            result+=valuePair.first+"="+valuePair.second+"&";
        }
        Pair valuePair=data.get(data.size()-1);
        result+=valuePair.first+"="+valuePair.second;
        return result;
    }

    private class LoginXmlPullParser {
        public String event_validation="";
        public String view_state="";
        private final String ns = null;
        private String path[];
        private String elementEntry;

        public void parse(InputStream in,String _path[],String entry) throws XmlPullParserException, IOException {
            try {
                path=_path;
                elementEntry=entry;
                XmlPullParser parser = Xml.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(in, null);
                parser.nextTag();
                readFeed(parser,0);
            } finally {
                in.close();
            }
        }

        private void readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
            parser.require(XmlPullParser.START_TAG, ns, "html");
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();
                // Starts by looking for the entry tag
                if (name.equals("body")) {
                    break;
                } else {
                    skip(parser);
                }
            }
        }

        private void readFeed(XmlPullParser parser,int depth) throws XmlPullParserException,IOException{
            parser.require(XmlPullParser.START_TAG, ns, path[depth++]);
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();
                // Starts by looking for the entry tag
                if(depth==path.length){
                    if(name.equals(elementEntry))
                        readData(parser);
                    else
                        skip(parser);
                }
                else if (name.equals(path[depth])) {
                    readFeed(parser,depth);
                } else {
                    skip(parser);
                }
            }
        }

        private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                throw new IllegalStateException();
            }
            int depth = 1;
            while (depth != 0) {
                switch (parser.next()) {
                    case XmlPullParser.END_TAG:
                        depth--;
                        break;
                    case XmlPullParser.START_TAG:
                        depth++;
                        break;
                }
            }
        }

        // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them off
        // to their respective "read" methods for processing. Otherwise, skips the tag.
        private void readData(XmlPullParser parser) throws XmlPullParserException, IOException {
            parser.require(XmlPullParser.START_TAG, ns, "input");
            String name=parser.getAttributeValue(null,"name");
            String id=parser.getAttributeValue(null,"id");
            if(name.equals("__VIEWSTATE")&&id.equals("__VIEWSTATE")){
                view_state=parser.getAttributeValue(null,"value");
            }else if(name.equals("__EVENTVALIDATION")&&id.equals("__EVENTVALIDATION")){
                event_validation=parser.getAttributeValue(null,"value");
            }
            parser.nextTag();
            parser.require(XmlPullParser.END_TAG, ns, "input");
        }
    }

    private class FloorInfoXmlPullParser{

        public String event_validation="";
        public String view_state="";
        private final String ns = null;
        private String path[];
        private String elementEntry;

        public void parse(InputStream in,String _path[],String entry) throws XmlPullParserException, IOException {
            try {
                path=_path;
                elementEntry=entry;
                XmlPullParser parser = Xml.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(in, null);
                parser.nextTag();
                readFeed(parser,0);
            } finally {
                in.close();
            }
        }

        private void readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
            parser.require(XmlPullParser.START_TAG, ns, "html");
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();
                // Starts by looking for the entry tag
                if (name.equals("body")) {
                    break;
                } else {
                    skip(parser);
                }
            }
        }

        private void readFeed(XmlPullParser parser,int depth) throws XmlPullParserException,IOException{
            parser.require(XmlPullParser.START_TAG, ns, path[depth++]);
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();
                // Starts by looking for the entry tag
                if(depth==path.length){
                    if(name.equals(elementEntry))
                        readData(parser);
                    else
                        skip(parser);
                }
                else if (name.equals(path[depth])) {
                    readFeed(parser,depth);
                } else {
                    skip(parser);
                }
            }
        }

        private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                throw new IllegalStateException();
            }
            int depth = 1;
            while (depth != 0) {
                switch (parser.next()) {
                    case XmlPullParser.END_TAG:
                        depth--;
                        break;
                    case XmlPullParser.START_TAG:
                        depth++;
                        break;
                }
            }
        }

        // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them off
        // to their respective "read" methods for processing. Otherwise, skips the tag.
        private void readData(XmlPullParser parser) throws XmlPullParserException, IOException {
            parser.require(XmlPullParser.START_TAG, ns, "input");
            String name=parser.getAttributeValue(null,"name");
            String id=parser.getAttributeValue(null,"id");
            if(name.equals("__VIEWSTATE")&&id.equals("__VIEWSTATE")){
                view_state=parser.getAttributeValue(null,"value");
            }else if(name.equals("__EVENTVALIDATION")&&id.equals("__EVENTVALIDATION")){
                event_validation=parser.getAttributeValue(null,"value");
            }
            parser.nextTag();
            parser.require(XmlPullParser.END_TAG, ns, "input");
        }
    }
}
