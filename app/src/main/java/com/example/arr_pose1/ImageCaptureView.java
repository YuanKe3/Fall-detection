package com.example.arr_pose1;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.TextView;

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
import com.example.arr_pose1.room.Graph;
import com.example.arr_pose1.room.GraphDatabase;
import com.google.gson.Gson;

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
  private List<Graph> graphList;
  private String[] x = new String[7];
  private int[] y = new int[7];
  private HashMap<String, String> hashMap;

  @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    overridePendingTransition(R.anim.bottom_in, R.anim.bottom_silent);
    setContentView(R.layout.activity_image_capture);

    graphDatabase = GraphDatabase.getInstance(this);

    hashMap = new HashMap<>();
    hashMap.put("MONDAY", "??????");
    hashMap.put("TUESDAY", "??????");
    hashMap.put("WEDNESDAY", "??????");
    hashMap.put("THURSDAY", "??????");
    hashMap.put("FRIDAY", "??????");
    hashMap.put("SATURDAY", "??????");
    hashMap.put("SUNDAY", "??????");

    // ????????????
    AMapLocationClient.updatePrivacyShow(this, true, true);
    AMapLocationClient.updatePrivacyAgree(this, true);

    // ????????????
    mapView = (MapView) findViewById(R.id.map);
    mapView.onCreate(savedInstanceState);
    if (aMap == null) {
      aMap = mapView.getMap();
    }
    // ??????????????????
    myLocationStyle = new MyLocationStyle();
    myLocationStyle.interval(2000);
    myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE);
    aMap.setMyLocationStyle(myLocationStyle);
    aMap.setMyLocationEnabled(true);
    aMap.moveCamera(CameraUpdateFactory.zoomTo(18));
    aMap.getUiSettings().setMyLocationButtonEnabled(true);

    graphList = graphDatabase.getGraphDao().getAllGraph();

    // ???7??????????????????????????????????????????
    if (graphList.size() > 0) {
      for (Graph graph : graphList) {
        addPoint(graph.getLatitude(), graph.getLongitude(), aMap);
      }
    }

    // ??????????????????????????????????????????
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
            // ???????????????
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
                    // ??????????????????????????????????????? POI ?????????????????? POI ???
                    PoiItemV2 poiItem = poiItemList.get(0);
                    // ????????? POI ???????????????????????????
                    String poiName = poiItem.getTitle();
                    LatLonPoint poiLaLonPoint = poiItem.getLatLonPoint();
                    double poiLatitude = poiLaLonPoint.getLatitude();
                    double poiLongitude = poiLaLonPoint.getLongitude();
                    // ??????????????????????????????????????????
                    TextView locationView = findViewById(R.id.location);
                    TextView longitudeAndLatitude = findViewById(R.id.longitudeAndLatitude);
                    String txt1 = "??????????????????" + poiName + " ??????";
                    String txt2 = "?????????" + poiLatitude + "????????????" + poiLongitude;
                    locationView.setText(txt1);
                    longitudeAndLatitude.setText(txt2);
                  }
                }

                @Override
                public void onPoiItemSearched(PoiItemV2 poiItemV2, int i) {

                }
              });
              poiSearch.searchPOIAsyn();
              // ???????????? POI ??????
            } catch (AMapException e) {
              e.printStackTrace();
            }
          }
          mLocationClient.stopLocation(); // ??????????????????????????????????????????
        }
      });
      mLocationClient.startLocation();
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
      // ??????????????????????????????
      // ??????????????????
      ZoneId zoneId = ZoneId.of("Asia/Shanghai");
      // ???????????????????????? 7 ???????????????????????????
      long[] timestampRange = new long[7];
      for (int i = 0; i < 7; i += 1) {
        LocalDate date = LocalDate.now(zoneId).minusDays(i);
        long startOfDay = date.atStartOfDay(zoneId).toEpochSecond();
        long endOfDay = date.plusDays(1).atStartOfDay(zoneId).toEpochSecond() - 1;
        timestampRange[i] = endOfDay;
      }
      for (int i = 0; i < 7; i += 1) {
        // ????????? x ???
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestampRange[i]), zoneId);
        String chineseDayOfWeek = dateTime.getDayOfWeek().toString();
        if (chineseDayOfWeek.length() > 0) {
          x[i] = hashMap.get(chineseDayOfWeek);
        }
      }
      if (x.length > 0) {
        x[0] = x[0] + "(??????)";
      }
      for (int i = 0; i < graphList.size(); i += 1) {
        // ????????? y ???
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

    // ??????
    WebView webView = findViewById(R.id.ordercharts_main);
    webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    webView.setBackgroundColor(0);
    webView.getSettings().setAllowFileAccess(true);
    webView.getSettings().setJavaScriptEnabled(true);
    webView.addJavascriptInterface(new JsInterface(), "nativeMethod");
    webView.setWebChromeClient(new WebChromeClient());
    webView.loadUrl("");
  }

  // ?????????
  private void addPoint(double latitude, double longitude, AMap aMap) {
    LatLng point = new LatLng(latitude, longitude);
    MarkerOptions markerOptions = new MarkerOptions();
    markerOptions.position(point);
    aMap.addMarker(markerOptions);
  }

  // Android ????????????????????????
  class JsInterface {
    @JavascriptInterface
    public String x() {
      return gson.toJson(x);
    }

    @JavascriptInterface
    public String y() {
      return gson.toJson(y);
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