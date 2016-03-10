package com.presisco.example.networktest;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Pair;
import android.util.Xml;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import org.w3c.dom.Entity;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public static final String root_url = "http://yuyue.juneberry.cn";
    WebView mContentWeb=null;
    TextView mContentText=null;
    CookieManager loginCookieManager;

    public String event_validation="";
    public String view_state="";

    Map<String,List<String>> conn_infos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //mContentWeb=(WebView)findViewById(R.id.webView);
        mContentText=(TextView)findViewById(R.id.textView);
    }

    public void onRequest(View v){
        new NetwortTask().execute();
    }

    private class NetwortTask extends AsyncTask<Void,Void,String>{
        LoginParamsXmlPullParser loginXmlPullParser;

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            mContentText.setText(result);
            //mContentWeb.loadData(result,"text/html",null);
        }

        @Override
        protected String doInBackground(Void... params) {
            String result="";
            try {
                getLoginParams();
                Login();
                result=getFloorInfo();
            }catch(Exception e){
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mContentText.setText("");
        }
    }

    public void getLoginParams() throws Exception{
        URL url = new URL(root_url);
        HttpURLConnection conn=(HttpURLConnection)url.openConnection();
        conn.setReadTimeout(10000);
        conn.setConnectTimeout(10000);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();
        InputStream is=conn.getInputStream();
        LoginParamsXmlPullParser loginXmlPullParser =new LoginParamsXmlPullParser();
        loginXmlPullParser.parse(is, new String[]{"html", "body", "form"}, "input");
        is.close();
        conn.disconnect();
    }

    public String Login() throws Exception{
        URL url = new URL(root_url+"/Login.aspx");
        loginCookieManager=new CookieManager();
        CookieHandler.setDefault(loginCookieManager);
        HttpURLConnection conn=(HttpURLConnection)url.openConnection();
        conn.setReadTimeout(10000);
        conn.setConnectTimeout(10000);
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        //conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        //conn.setRequestProperty("Charset", "UTF-8");
        conn.setUseCaches(false);
        conn.connect();
        OutputStream out=conn.getOutputStream();

        List<Pair> formParams=new ArrayList<Pair>();
        formParams.add(new Pair("__VIEWSTATE", view_state));
        formParams.add(new Pair("__EVENTVALIDATION", event_validation));
        formParams.add(new Pair("subCmd", "Login"));
        formParams.add(new Pair("txt_LoginID", "201200620807"));
        formParams.add(new Pair("txt_Password", "201200620807"));
        formParams.add(new Pair("selSchool", "15"));
        String final_params=getFormParams(formParams).toString();
        out.write(final_params.getBytes());
        out.flush();
        out.close();

        //String cookie=conn.getHeaderField("Set-Cookie");
//        Map<String, List<String>> headerFields=conn.getHeaderFields();
//        List<String> cookiesHeader=headerFields.get("Set-Cookie");
//        if(cookiesHeader!=null){
//            for(String cookie:cookiesHeader){
//                HttpCookie var=HttpCookie.parse(cookie).get(0);
//                loginCookieManager.getCookieStore().add(new URI(root_url), var);
//            }
//        }

        conn_infos=conn.getHeaderFields();

        InputStream is=conn.getInputStream();
        String result=getFullStringFromConnection(is, "UTF-8");
        is.close();
        conn.disconnect();
        return result;
    }

    public String getFloorInfo () throws Exception {
        //get floor info
        URL url=new URL(root_url+"/ReadingRoomInfos/ReadingRoomState.aspx");
        HttpURLConnection conn=(HttpURLConnection)url.openConnection();
        conn.setReadTimeout(10000);
        conn.setConnectTimeout(10000);
        conn.setRequestMethod("GET");
        for(Map.Entry<String,List<String>> entry:conn_infos.entrySet()){
            if(entry.getKey()!=null)
                conn.setRequestProperty(entry.getKey(),TextUtils.join(";",entry.getValue()));
        }
        conn.setDoInput(true);
        conn.connect();
        InputStream is=conn.getInputStream();
        String result=getFullStringFromConnection(is, "UTF-8");
        is.close();
        conn.disconnect();
        return result;
    }

    public String getFullStringFromConnection(InputStream is,String format) throws IOException{
        BufferedReader buff = new BufferedReader(new InputStreamReader(is, format));
        StringBuffer resultBuff=new StringBuffer();
        String line="";
        while((line=buff.readLine())!=null){
            resultBuff.append(line);
        }
        return resultBuff.toString();
    }

    public String getFormParams (List<Pair> orig_params) throws Exception{
        List<String> cooked_params=new ArrayList<>();
        for(Pair pair:orig_params){
            cooked_params.add(pair.first + "=" + URLEncoder.encode((String)pair.second,"UTF-8"));
        }
        return TextUtils.join("&",cooked_params);
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

    private class LoginParamsXmlPullParser {
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
