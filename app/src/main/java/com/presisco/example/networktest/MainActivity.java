package com.presisco.example.networktest;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
        SampleXMLPullParser sampleXMLPullParser;

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            mContentText.setText(result);
            Log.d("event_validation", sampleXMLPullParser.event_validation);
            Log.d("view_state",sampleXMLPullParser.view_state);
        }

        @Override
        protected String doInBackground(Void... params) {
            String result="";
            try {
                URL url = new URL("http://yuyue.juneberry.cn/");
                HttpURLConnection conn=(HttpURLConnection)url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();
                InputStream is=conn.getInputStream();
//                Reader r=new InputStreamReader(is,"UTF-8");
//                char[] buffer = new char[1000000];
//                r.read(buffer);
//                result= new String(buffer);

                sampleXMLPullParser=new SampleXMLPullParser();
                sampleXMLPullParser.parse(is);
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

    private class SampleXMLPullParser{
        public String event_validation="";
        public String view_state="";
        private final String ns = null;

        public void parse(InputStream in) throws XmlPullParserException, IOException {
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(in, null);
                parser.nextTag();
                readFeed(parser);
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
//                    parser.require(XmlPullParser.START_TAG, ns, "body");
//                    while(parser.next()!=XmlPullParser.END_TAG){
//                        if (parser.getEventType() != XmlPullParser.START_TAG) {
//                            continue;
//                        }
//                        String name2=parser.getName();
//                        if(name2.equals("input")){
//                            readData(parser);
//                        }else{
//                            skip(parser);
//                        }
//                    }
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
            if(name=="__VIEWSTATE"&&id=="__VIEWSTATE"){
                view_state=parser.getAttributeValue(null,"value");
            }else if(name=="__EVENTVALIDATION"){
                event_validation=parser.getAttributeValue(null,"value");
            }
            parser.nextTag();
            parser.require(XmlPullParser.END_TAG, ns, "input");
        }

        // Processes title tags in the feed.
        private String readTitle(XmlPullParser parser) throws IOException, XmlPullParserException {
            parser.require(XmlPullParser.START_TAG, ns, "title");
            String title = readText(parser);
            parser.require(XmlPullParser.END_TAG, ns, "title");
            return title;
        }

        // Processes link tags in the feed.
        private String readLink(XmlPullParser parser) throws IOException, XmlPullParserException {
            String link = "";
            parser.require(XmlPullParser.START_TAG, ns, "link");
            String tag = parser.getName();
            String relType = parser.getAttributeValue(null, "rel");
            if (tag.equals("link")) {
                if (relType.equals("alternate")){
                    link = parser.getAttributeValue(null, "href");
                    parser.nextTag();
                }
            }
            parser.require(XmlPullParser.END_TAG, ns, "link");
            return link;
        }

        // Processes summary tags in the feed.
        private String readSummary(XmlPullParser parser) throws IOException, XmlPullParserException {
            parser.require(XmlPullParser.START_TAG, ns, "summary");
            String summary = readText(parser);
            parser.require(XmlPullParser.END_TAG, ns, "summary");
            return summary;
        }

        // For the tags title and summary, extracts their text values.
        private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
            String result = "";
            if (parser.next() == XmlPullParser.TEXT) {
                result = parser.getText();
                parser.nextTag();
            }
            return result;
        }
    }
}
