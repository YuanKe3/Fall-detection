package com.example.arr_pose1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItemV2;
import com.amap.api.services.poisearch.PoiResultV2;
import com.amap.api.services.poisearch.PoiSearchV2;
import com.example.arr_pose1.model.SendMailData;
import com.example.arr_pose1.room.Graph.Graph;
import com.example.arr_pose1.room.Graph.GraphDatabase;
import com.example.arr_pose1.room.PersonInfo.PersonInfo;
import com.example.arr_pose1.room.PersonInfo.PersonInfoDatabase;
import com.example.arr_pose1.room.Record.Record;
import com.example.arr_pose1.room.Record.RecordDatabase;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.io.File;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ImageCaptureView extends AppCompatActivity {
  Gson gson = new Gson();
  private MapView mapView = null;
  private AMap aMap;
  private MyLocationStyle myLocationStyle;
  private GraphDatabase graphDatabase;
  private RecordDatabase recordDatabase;
  private PersonInfoDatabase personInfoDatabase;
  private List<Graph> graphList;
  private String[] x = new String[7];
  private int[] y = new int[7];
  private HashMap<String, String> hashMap;
  private TextView fallTimesTxt;
  private TextView wrongTimesTxt;
  private TextView wrongRateTxt;
  private TextView recordTimesTxt;
  private FloatingActionButton sendBtn;
  private String poiName = "";

  @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    overridePendingTransition(R.anim.bottom_in, R.anim.bottom_silent);
    setContentView(R.layout.activity_image_capture);

    fallTimesTxt = findViewById(R.id.fallTimesTxt);
    wrongTimesTxt = findViewById(R.id.wrongTimesTxt);
    wrongRateTxt = findViewById(R.id.wrongRateTxt);
    recordTimesTxt = findViewById(R.id.recordTimesTxt);
    sendBtn = findViewById(R.id.fab_send);

    graphDatabase = GraphDatabase.getInstance(this);
    recordDatabase = RecordDatabase.getInstance(this);
    personInfoDatabase = PersonInfoDatabase.getInstance(this);

    hashMap = new HashMap<>();
    hashMap.put("MONDAY", "周一");
    hashMap.put("TUESDAY", "周二");
    hashMap.put("WEDNESDAY", "周三");
    hashMap.put("THURSDAY", "周四");
    hashMap.put("FRIDAY", "周五");
    hashMap.put("SATURDAY", "周六");
    hashMap.put("SUNDAY", "周日");

    // 权限申明
    AMapLocationClient.updatePrivacyShow(this, true, true);
    AMapLocationClient.updatePrivacyAgree(this, true);

    // 地图显示
    mapView = (MapView) findViewById(R.id.map);
    mapView.onCreate(savedInstanceState);
    if (aMap == null) {
      aMap = mapView.getMap();
    }
    // 实现定位蓝点
    myLocationStyle = new MyLocationStyle();
    myLocationStyle.interval(2000);
    myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE);
    aMap.setMyLocationStyle(myLocationStyle);
    aMap.setMyLocationEnabled(true);
    aMap.moveCamera(CameraUpdateFactory.zoomTo(18));
    aMap.getUiSettings().setMyLocationButtonEnabled(true);

    graphList = graphDatabase.getGraphDao().getAllGraph();

    // 将7日内所有摔倒的地点都进行标点
    if (graphList.size() > 0) {
      for (Graph graph : graphList) {
        addPoint(graph.getLatitude(), graph.getLongitude(), aMap);
      }
    }

    // 获取最近的一个点的名称和坐标
    PoiSearchV2.Query query = new PoiSearchV2.Query("", "", "");
    query.setPageSize(10);
    query.setPageNum(0);
    query.setDistanceSort(true);

    try {
      final AMapLocationClient mLocationClient = new AMapLocationClient(getApplicationContext());
      AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
      mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
      mLocationOption.setInterval(0);
      mLocationClient.setLocationOption(mLocationOption);
      mLocationClient.setLocationListener(new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
          if (aMapLocation != null) {
            // 获取经纬度
            double latitude = aMapLocation.getLatitude();
            double longitude = aMapLocation.getLongitude();
            LatLonPoint latLonPoint = new LatLonPoint(latitude, longitude);
            PoiSearchV2.SearchBound searchBound = new PoiSearchV2.SearchBound(latLonPoint, 1000);
            try {
              PoiSearchV2 poiSearch = new PoiSearchV2(ImageCaptureView.this, query);
              poiSearch.setBound(searchBound);
              poiSearch.setOnPoiSearchListener(new PoiSearchV2.OnPoiSearchListener() {
                @Override
                public void onPoiSearched(PoiResultV2 poiResult, int i) {
                  ArrayList<PoiItemV2> poiItemList = poiResult.getPois();
                  if (poiItemList != null && poiItemList.size() > 0) {
                    // 取出距离当前位置最近的一个 POI 点，即第一个 POI 点
                    PoiItemV2 poiItem = poiItemList.get(0);
                    // 获取该 POI 点的名称和坐标信息
                    poiName = poiItem.getTitle();
                    LatLonPoint poiLaLonPoint = poiItem.getLatLonPoint();
                    double poiLatitude = poiLaLonPoint.getLatitude();
                    double poiLongitude = poiLaLonPoint.getLongitude();
                    // 输出最近的一个点的名称和坐标
                    TextView locationView = findViewById(R.id.location);
                    TextView longitudeAndLatitude = findViewById(R.id.longitudeAndLatitude);
                    String txt1 = "上为七日内摔倒时的地点标点。你当前位于：" + poiName + " 附近";
                    String txt2 = "纬度：" + poiLatitude + "，经度：" + poiLongitude;
                    locationView.setText(txt1);
                    longitudeAndLatitude.setText(txt2);

                    int warningTimes = recordDatabase.getRecordDao().getRecords().get(0).getWarningTimes();
                    int wrongTimes = recordDatabase.getRecordDao().getRecords().get(0).getWrongWarningTimes();
                    int recordTimes = graphList.size();
                    fallTimesTxt.setText(String.valueOf(warningTimes));
                    wrongTimesTxt.setText(String.valueOf(wrongTimes));
                    double warningTimesD = Double.parseDouble(String.valueOf(warningTimes));
                    double wrongTimesD = Double.parseDouble(String.valueOf(wrongTimes));
                    double wrongRate = wrongTimesD / warningTimesD;
                    NumberFormat nt = NumberFormat.getPercentInstance();
                    nt.setMinimumFractionDigits(2);
                    String result = nt.format(wrongRate);
                    wrongRateTxt.setText(result);
                    recordTimesTxt.setText(String.valueOf(recordTimes));
                  }
                }

                @Override
                public void onPoiItemSearched(PoiItemV2 poiItemV2, int i) {

                }
              });
              poiSearch.searchPOIAsyn();
              // 查询周边 POI 信息
            } catch (AMapException e) {
              e.printStackTrace();
            }
          }
          mLocationClient.stopLocation(); // 获取到定位结果，立即停止定位
        }
      });
      mLocationClient.startLocation();
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
      // 获取七天内每天的数据
      // 获取北京时间
      ZoneId zoneId = ZoneId.of("Asia/Shanghai");
      // 计算今天及其之前 7 天每天的时间戳范围
      long[] timestampRange = new long[7];
      for (int i = 0; i < 7; i += 1) {
        LocalDate date = LocalDate.now(zoneId).minusDays(i);
        long startOfDay = date.atStartOfDay(zoneId).toEpochSecond();
        long endOfDay = date.plusDays(1).atStartOfDay(zoneId).toEpochSecond() - 1;
        timestampRange[i] = endOfDay;
      }
      for (int i = 0; i < 7; i += 1) {
        // 图形的 x 轴
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestampRange[i]), zoneId);
        String chineseDayOfWeek = dateTime.getDayOfWeek().toString();
        if (chineseDayOfWeek.length() > 0) {
          x[i] = hashMap.get(chineseDayOfWeek);
        }
      }
      if (x.length > 0) {
        x[0] = x[0] + "(今日)";
      }
      for (int i = 0; i < graphList.size(); i += 1) {
        // 图形的 y 轴
        long fallTime = graphList.get(i).getFallTime() / 1000;
        if (fallTime > timestampRange[1] && fallTime <= timestampRange[0]) {
          y[0] += 1;
        } else if (fallTime > timestampRange[2] && fallTime <= timestampRange[1]) {
          y[1] += 1;
        } else if (fallTime > timestampRange[3] && fallTime <= timestampRange[2]) {
          y[2] += 1;
        } else if (fallTime > timestampRange[4] && fallTime <= timestampRange[3]) {
          y[3] += 1;
        } else if (fallTime > timestampRange[5] && fallTime <= timestampRange[4]) {
          y[4] += 1;
        } else if (fallTime > timestampRange[6] && fallTime <= timestampRange[5]) {
          y[5] += 1;
        } else if (fallTime <= timestampRange[6]) {
          y[6] += 1;
        }
      }
      Collections.reverse(Arrays.asList(x));
      for (int i = 0, j = y.length - 1; i < j; i++, j--) {
        int temp = y[i];
        y[i] = y[j];
        y[j] = temp;
      }
    }

    // 初始化
    if (recordDatabase.getRecordDao().getRecords().size() == 0) {
      recordDatabase.getRecordDao().insertWrongWarningItem(new Record(0, 0, 0, 0, 0, 0, 0, 0));
    }

    sendBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        int warningTimes = recordDatabase.getRecordDao().getRecords().get(0).getWarningTimes();
        int wrongTimes = recordDatabase.getRecordDao().getRecords().get(0).getWrongWarningTimes();

        List<PersonInfo> personInfoList = personInfoDatabase.getPersonInfoDao().getAllPersonInfo();
        PersonInfo personInfo = personInfoList.size() != 0 ? personInfoList.get(0) : null;
        List<Record> recordList = recordDatabase.getRecordDao().getRecords();
        Record record = recordList.size() != 0 ? recordList.get(0) : null;

        String address = personInfo != null ? personInfo.getAddress() : "";
        int kneeSettingAlgorithmTimes = record != null ? record.getKneeSettingAlgorithm() : 0;
        int wrongKneeSettingAlgorithmTimes = record != null ? record.getWrongKneeSettingAlgorithm() : 0;
        int mainAlgorithmTimes = record != null ? record.getMainAlgorithm() : 0;
        int wrongMainAlgorithmTimes = record != null ? record.getWrongMainAlgorithm() : 0;
        int lieDownAlgorithmTimes = record != null ? record.getLieDownAlgorithm() : 0;
        int wrongLieDownAlgorithmTimes = record != null ? record.getWrongLieDownAlgorithm() : 0;

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
//        emailIntent.setType("application/octet-stream");
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "统计数据");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"cctvyxy@qq.com"});
        emailIntent.putExtra(
                Intent.EXTRA_TEXT, "报警次数: " + warningTimes + "\n"
                        + "误报次数: " + wrongTimes + "\n"
                        + "邮箱发送地址: " + poiName + "\n"
                        + "家庭住址: " + address + "\n"
                        + "加速度、角度联合判断算法使用次数: " + mainAlgorithmTimes + "\n"
                        + "加速度、角度联合判断算法误报次数: " + wrongMainAlgorithmTimes + "\n"
                        + "跪坐判断算法使用次数: " + kneeSettingAlgorithmTimes + "\n"
                        + "跪坐判断算法误报次数: " + wrongKneeSettingAlgorithmTimes + "\n"
                        + "髋部长时间着地算法使用次数: " + lieDownAlgorithmTimes + "\n"
                        + "髋部长时间着地算法误报次数: " + wrongLieDownAlgorithmTimes
        );
//        emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        startActivity(Intent.createChooser(emailIntent, "统计数据 - 通过邮件发送"));
      }
    });


    // 图表
    WebView webView = findViewById(R.id.ordercharts_main);
    webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    webView.setBackgroundColor(0);
    webView.getSettings().setAllowFileAccess(true);
    webView.getSettings().setJavaScriptEnabled(true);
    webView.addJavascriptInterface(new JsInterface(), "nativeMethod");
    webView.setWebChromeClient(new WebChromeClient());
    webView.loadUrl("file:///android_asset/chart.html");
  }

  // 添加点
  private void addPoint(double latitude, double longitude, AMap aMap) {
    LatLng point = new LatLng(latitude, longitude);
    MarkerOptions markerOptions = new MarkerOptions();
    markerOptions.position(point);
    aMap.addMarker(markerOptions);
  }

  // Android 向网页中传递数据
  class JsInterface {
    @JavascriptInterface
    public String x() {
      return gson.toJson(x);
    }

    @JavascriptInterface
    public String y() {
      return gson.toJson(y);
    }

    @JavascriptInterface
    public int rightValue() {
      return recordDatabase.getRecordDao().getRecords().get(0).getWarningTimes() - recordDatabase.getRecordDao().getRecords().get(0).getWrongWarningTimes();
    }

    @JavascriptInterface
    public int wrongValue() {
      return recordDatabase.getRecordDao().getRecords().get(0).getWrongWarningTimes();
    }
  }

  @Override
  public void finish() {
    super.finish();
    overridePendingTransition(R.anim.bottom_silent, R.anim.bottom_out);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mapView.onDestroy();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mapView.onResume();
  }

  @Override
  protected void onPause() {
    super.onPause();
    mapView.onPause();
  }

  @Override
  protected void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }
}