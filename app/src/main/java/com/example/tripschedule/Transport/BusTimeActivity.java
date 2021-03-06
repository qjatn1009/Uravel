package com.example.tripschedule.Transport;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.example.tripschedule.R;
import com.odsay.odsayandroidsdk.API;
import com.odsay.odsayandroidsdk.ODsayData;
import com.odsay.odsayandroidsdk.ODsayService;
import com.odsay.odsayandroidsdk.OnResultCallbackListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class BusTimeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private SpeedAdapter adapter;
    Handler handler;
    String apiKey="Uthu/s+j11jLrUlntMUggGbRbvmhzv5Of48rWLmtXT4";


    private ArrayList<SpeedBus> arrayList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_time);

        arrayList=new ArrayList<>();
        Intent intent = getIntent();
        int code = intent.getIntExtra("code", 0);
        int endStationID = 3600728;
        layoutManager=new LinearLayoutManager(this);
        recyclerView=findViewById(R.id.rv_speedBus);
        // 싱글톤 생성, Key 값을 활용하여 객체 생성
        ODsayService odsayService = ODsayService.init(this, apiKey);
        // 서버 연결 제한 시간(단위(초), default : 5초)
        odsayService.setReadTimeout(5000);
        // 데이터 획득 제한 시간(단위(초), default : 5초)
        odsayService.setConnectionTimeout(5000);
        // 콜백 함수 구현
        OnResultCallbackListener onResultCallbackListener = new OnResultCallbackListener() {
            // 호출 성공 시 실행
            @Override
            public void onSuccess(ODsayData odsayData, API api) {
                try {
                    // API Value 는 API 호출 메소드 명을 따라갑니다.
                    if (api == API.INTERCITY_SERVICE_TIME) {
                        String[] scheduleArr;
                        JSONArray json2 = odsayData.getJson().getJSONObject("result").getJSONArray("station");
                        for(int k=0;k<json2.length();k++){

                            JSONObject json3 =json2.getJSONObject(k);

                            String fare;
                            String startTerminal=json3.getString("startTerminal");
                            String destTerminal=json3.getString("destTerminal");
                            String wasteTime=json3.getString("wasteTime");
                            String normalFare=json3.getString("normalFare");
                            String nightFare=json3.getString("nightFare");
                            String schedule=json3.getString("schedule");
                            String nightSchedule = json3.getString("nightSchedule");


                            scheduleArr=schedule.split("/|\n");
                            if(!scheduleArr[0].equals("")) {
                                for (int i = 0; i < scheduleArr.length; i++) {
                                    fare = null;
                                    if (scheduleArr[i].contains("(우등)")) {
                                        arrayList.add(new SpeedBus(startTerminal.split("/")[1], destTerminal.split("/")[1],
                                                wasteTime, fare, scheduleArr[i]));
                                    } else {
                                        fare = normalFare;
                                        arrayList.add(new SpeedBus(startTerminal.split("/")[1], destTerminal.split("/")[1],
                                                wasteTime, fare, scheduleArr[i]));
                                    }
                                }
                            }

                            scheduleArr=nightSchedule.split("/|\n");
                            if(!scheduleArr[0].equals("")) {
                                for (int j = 0; j < scheduleArr.length; j++) {
                                    fare = null;
                                    if (scheduleArr[j].contains("(우등)")) {
                                        arrayList.add(new SpeedBus(startTerminal.split("/")[1], destTerminal.split("/")[1],
                                                wasteTime, fare, scheduleArr[j]));
                                    } else {
                                        fare = nightFare;
                                        arrayList.add(new SpeedBus(startTerminal.split("/")[1], destTerminal.split("/")[1],
                                                wasteTime, fare, scheduleArr[j]));
                                    }
                                }
                            }
                        }


                        Collections.sort(arrayList, new Comparator<SpeedBus>() {
                            @Override
                            public int compare(SpeedBus o1, SpeedBus o2) {
                                if(o1.getSchedule().compareTo(o2.getSchedule())<0)
                                    return -1;
                                else if(o1.getSchedule().compareTo(o2.getSchedule())>0)
                                    return 1;
                                return 0;
                            }
                        });
                    }
                }catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            // 호출 실패 시 실행
            @Override
            public void onError(int i, String s, API api) {
                if (api == API.INTERCITY_SERVICE_TIME) {}
            }
        };
        // API 호출
        odsayService.requestIntercityServiceTime(String.valueOf(code),String.valueOf(endStationID), onResultCallbackListener);

        adapter=new SpeedAdapter(arrayList);


        handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setHasFixedSize(true);
            }
        },1000);





    }
    @SuppressLint("StaticFieldLeak")
    public class SpeedBusTask extends AsyncTask<Void,Void,String> {

        private String url;
        private ContentValues values;

        SpeedBusTask(String url, ContentValues values){
            this.url=url;
            this.values=values;

        }
        @Override
        protected String doInBackground(Void... voids) {
            String result; //요청 결과 저장 변수
            BusTimeActivity.RequestHttpURLConnection requestHttpURLConnection = new BusTimeActivity.RequestHttpURLConnection();
            result = requestHttpURLConnection.request(url,values);




            return result;
        }
    }
    public class RequestHttpURLConnection{
        String request(String _url, ContentValues _params){
            HttpURLConnection urlConn=null;
            StringBuffer sbParams=new StringBuffer();


            try {
                URL url=new URL(_url);
                urlConn=(HttpURLConnection) url.openConnection();

                urlConn.setRequestMethod("POST");
                urlConn.setRequestProperty("Accept-Charset","UTF-8");
                urlConn.setRequestProperty("Context_Type", "application/x-www-form-urlencoded;charset=UTF-8");

                String strParams = sbParams.toString();
                OutputStream os=urlConn.getOutputStream();
                os.write(strParams.getBytes("UTF-8"));
                os.flush();
                os.close();


                if(urlConn.getResponseCode()!= HttpURLConnection.HTTP_OK)
                    return null;


                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream(),"UTF-8"));

                String line;
                StringBuilder page= new StringBuilder();

                while((line=reader.readLine())!=null){
                    page.append(line);
                }
                try{
                    String[] scheduleArr;


                    JSONObject json=new JSONObject(page.toString());
                    JSONObject json1=json.getJSONObject("result");


                    JSONArray json2=json1.getJSONArray("station");

                    for(int k=0;k<json2.length();k++){

                        JSONObject json3 =json2.getJSONObject(k);

                        String fare;
                        String startTerminal=json3.getString("startTerminal");
                        String destTerminal=json3.getString("destTerminal");
                        String wasteTime=json3.getString("wasteTime");
                        String normalFare=json3.getString("normalFare");
                        String nightFare=json3.getString("nightFare");
                        String schedule=json3.getString("schedule");
                        String nightSchedule = json3.getString("nightSchedule");


                        scheduleArr=schedule.split("/|\n");
                        if(!scheduleArr[0].equals("")) {
                            for (int i = 0; i < scheduleArr.length; i++) {
                                fare = null;
                                if (scheduleArr[i].contains("(우등)")) {
                                    arrayList.add(new SpeedBus(startTerminal.split("/")[1], destTerminal.split("/")[1],
                                            wasteTime, fare, scheduleArr[i]));
                                } else {
                                    fare = normalFare;
                                    arrayList.add(new SpeedBus(startTerminal.split("/")[1], destTerminal.split("/")[1],
                                            wasteTime, fare, scheduleArr[i]));
                                }
                            }
                        }

                        scheduleArr=nightSchedule.split("/|\n");
                        if(!scheduleArr[0].equals("")) {
                            for (int j = 0; j < scheduleArr.length; j++) {
                                fare = null;
                                if (scheduleArr[j].contains("(우등)")) {
                                    arrayList.add(new SpeedBus(startTerminal.split("/")[1], destTerminal.split("/")[1],
                                            wasteTime, fare, scheduleArr[j]));
                                } else {
                                    fare = nightFare;
                                    arrayList.add(new SpeedBus(startTerminal.split("/")[1], destTerminal.split("/")[1],
                                            wasteTime, fare, scheduleArr[j]));
                                }
                            }
                        }
                    }


                    Collections.sort(arrayList, new Comparator<SpeedBus>() {
                        @Override
                        public int compare(SpeedBus o1, SpeedBus o2) {
                            if(o1.getSchedule().compareTo(o2.getSchedule())<0)
                                return -1;
                            else if(o1.getSchedule().compareTo(o2.getSchedule())>0)
                                return 1;
                            return 0;
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

    }
}
