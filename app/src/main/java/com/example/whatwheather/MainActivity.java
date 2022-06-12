package com.example.whatwheather;

import static com.example.whatwheather.TransLocalPoint.TO_GRID;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
{

    TextView timeView;
    String url = "https://www.google.com/search?q=%EB%82%A0%EC%94%A8&sourceid=chrome&ie=UTF-8";
    String bt1_url = "https://www.musinsa.com/app/styles/views/27196?use_yn_360=&style_type=&brand=&model=&tag_no=&max_rt=&min_rt=&display_cnt=60&list_kind=big&sort=date&page=1",
            bt2_url = "https://www.musinsa.com/app/styles/views/27234?use_yn_360=&style_type=&brand=&model=&tag_no=&max_rt=&min_rt=&display_cnt=60&list_kind=big&sort=date&page=1";
    String temp, rain, htemp, ltemp, ws;
    int temp_int,differ_int, rain_int, wind_int;
    final Bundle bundle = new Bundle();

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private TransLocalPoint transLocalPoint;
    private GpsTracker gpsTracker;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView textView = (TextView) findViewById(R.id.temperatureView);
        final TextView timeText = (TextView) findViewById(R.id.timeView);
        final TextView rainText = (TextView) findViewById(R.id.rainView);
        final TextView diffText = (TextView) findViewById(R.id.differView);
        final TextView wsText = (TextView) findViewById(R.id.wsView);
        final TextView commentText = (TextView) findViewById(R.id.commentView);
        final ImageButton suggest1 = (ImageButton) findViewById(R.id.imageView1);
        final ImageButton suggest2 = (ImageButton) findViewById(R.id.imageView2);

        Handler handler = new Handler(){        //온도 웹크롤링 코드 시작

            @Override
            public void handleMessage(Message msg){
                Bundle bundle = msg.getData();
                textView.setText(bundle.getString("temperature") + "°C");      //52번재 줄처럼 인터페이스에 있는 id랑 연결시켜야 함.
                rainText.setText("강수 확률: " + bundle.getString("rain"));
                diffText.setText(bundle.getString("htemp")
                        + "°C / " + bundle.getString("ltemp") + "°C");
                wsText.setText("풍속: " + bundle.getString("windspeed"));

                if(differ_int > 15){
                    commentText.setText("일교차가 큽니다. 외투를 챙기세요.");
                }
                if(rain_int > 50){
                    commentText.setText("강수 확률이 높습니다. 우산을 챙기세요.");
                }
                if(wind_int > 10){
                    commentText.setText("바람이 많이 붑니다. 외투를 챙기세요.");
                }

                if(temp_int >= 20){
                    suggest1.setImageResource(R.drawable.hot_1);
                    suggest2.setImageResource(R.drawable.hot_2);

                    bt1_url = "https://www.musinsa.com/app/styles/views/27196?use_yn_360=&style_type=&brand=&model=&tag_no=&max_rt=&min_rt=&display_cnt=60&list_kind=big&sort=date&page=1";
                    bt2_url = "https://www.musinsa.com/app/styles/views/27234?use_yn_360=&style_type=&brand=&model=&tag_no=&max_rt=&min_rt=&display_cnt=60&list_kind=big&sort=date&page=1";

                }
                else if (temp_int < 20 && temp_int >= 15){
                    suggest1.setImageResource(R.drawable.warm_1);
                    suggest2.setImageResource(R.drawable.warm_2);

                    bt1_url = "https://www.musinsa.com/app/styles/views/25805?use_yn_360=&style_type=&brand=&model=&tag_no=&max_rt=&min_rt=&display_cnt=60&list_kind=big&sort=date&page=12";
                    bt2_url = "https://www.musinsa.com/app/styles/views/25843?use_yn_360=&style_type=&brand=&model=&tag_no=&max_rt=&min_rt=&display_cnt=60&list_kind=big&sort=date&page=11";
                }
                else if (temp_int < 15 && temp_int >= 10){
                    suggest1.setImageResource(R.drawable.cold_1);
                    suggest2.setImageResource(R.drawable.cold_2);

                    bt1_url = "https://www.musinsa.com/app/styles/views/25240";
                    bt2_url = "https://www.musinsa.com/app/styles/views/25020";
                }
                else if (temp_int < 10 && temp_int >= 5){
                    suggest1.setImageResource(R.drawable.v_cold_1);
                    suggest2.setImageResource(R.drawable.v_cold_2);

                    bt1_url = "https://www.musinsa.com/app/styles/views/24890";
                    bt2_url = "https://www.musinsa.com/app/styles/views/24605";
                }
                else if (temp_int < 5){
                    suggest1.setImageResource(R.drawable.super_cold_1);
                    suggest2.setImageResource(R.drawable.super_cold_2);

                    bt1_url = "https://www.musinsa.com/app/styles/views/25292";
                    bt2_url = "https://www.musinsa.com/app/styles/views/24615";
                }

            }
        };

        suggest1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent urlintent = new Intent(Intent.ACTION_VIEW, Uri.parse(bt1_url));
                startActivity(urlintent);
            }
        });
        suggest2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent urlintent = new Intent(Intent.ACTION_VIEW, Uri.parse(bt2_url));
                startActivity(urlintent);
            }
        });


        new Thread(){
            @Override
            public void run() {
                Document doc = null;
                try {
                    doc = Jsoup.connect(url).get();
                    Element elements = doc.select("#wob_tm").first();   //온도
                    Element elements2 = doc.select("#wob_pp").first();  //강수확률
                    Element elements3 = doc.select(".gNCp2e span").first();  //highest 온도
                    Element elements4 = doc.select(".QrNVmd.ZXCv8e span").first();  //lowest 온도
                    Element elements5 = doc.select("#wob_ws").first();  //풍속

                    temp = elements.text();
                    rain = elements2.text();
                    htemp = elements3.text();
                    ltemp = elements4.text();
                    ws = elements5.text();

                    temp_int = Integer.parseInt(temp);
                    differ_int = Integer.parseInt(htemp) - (Integer.parseInt(ltemp) + 1);
                    rain_int = Integer.parseInt(rain.substring(0,rain.length() - 1));
                    wind_int = Integer.parseInt(ws.substring(0,ws.length() - 3));

                    bundle.putString("temperature", temp);
                    bundle.putString("rain", rain);
                    bundle.putString("htemp", htemp);
                    bundle.putString("ltemp", ltemp);
                    bundle.putString("windspeed", ws);

                    Message msg = handler.obtainMessage();
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();          //온도 웹크롤링 코드 끝

        (new Thread(new Runnable()
        {

            @Override
            public void run()
            {
                while (!Thread.interrupted())
                    try
                    {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() // start actions in UI thread
                        {

                            @Override
                            public void run()
                            {
                                timeText.setText(getCurrentTime());
                            }
                        });
                    }
                    catch (InterruptedException e)
                    {
                        // ooops
                    }
            }
        })).start();

        if (!checkLocationServicesStatus()) {

            showDialogForLocationServiceSetting();
        }else {

            checkRunTimePermission();
        }

        final TextView textview_address = (TextView)findViewById(R.id.locationView);


        GpsTracker gpsTracker = new GpsTracker(MainActivity.this);

        double latitude = gpsTracker.getLatitude();
        double longitude = gpsTracker.getLongitude();

        String address = getCurrentAddress(latitude, longitude);
        textview_address.setText(address);

        transLocalPoint = new TransLocalPoint();
        TransLocalPoint.LatXLngY tmp = transLocalPoint.convertGRID_GPS(TO_GRID, latitude, longitude);

        long mNow = System.currentTimeMillis();
        Date mReDate;

    }

    public String getCurrentTime(){
        long time = System.currentTimeMillis();

        SimpleDateFormat dayTime = new SimpleDateFormat("yyyy년MM월dd일 hh시mm분ss초");

        String str = dayTime.format(new Date(time));

        return str;
    }

    /*
     * ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드입니다.
     */
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        super.onRequestPermissionsResult(permsRequestCode, permissions, grandResults);
        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면

            boolean check_result = true;


            // 모든 퍼미션을 허용했는지 체크합니다.

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }


            if (check_result) {

                //위치 값을 가져올 수 있음
                ;
            } else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();


                } else {

                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();

                }
            }

        }
    }

    void checkRunTimePermission(){

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)


            // 3.  위치 값을 가져올 수 있음



        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(MainActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }

    }


    public String getCurrentAddress( double latitude, double longitude) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }



        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        }

        Address address = addresses.get(0);
        return address.getAddressLine(0).toString()+"\n";

    }


    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                        checkRunTimePermission();
                        return;
                    }
                }

                break;
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

}