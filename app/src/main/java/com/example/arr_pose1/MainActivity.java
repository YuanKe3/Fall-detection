package com.example.arr_pose1;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItemV2;
import com.amap.api.services.poisearch.PoiResultV2;
import com.amap.api.services.poisearch.PoiSearchV2;
import com.example.arr_pose1.room.Contact.ContactDatabase;
import com.example.arr_pose1.room.Graph.Graph;
import com.example.arr_pose1.room.Graph.GraphDatabase;
import com.example.arr_pose1.room.PersonInfo.PersonInfoDatabase;
import com.example.arr_pose1.room.Record.Record;
import com.example.arr_pose1.room.Record.RecordDatabase;
import com.google.mediapipe.components.CameraHelper;
import com.google.mediapipe.components.CameraXPreviewHelper;
import com.google.mediapipe.components.ExternalTextureConverter;
import com.google.mediapipe.components.FrameProcessor;
import com.google.mediapipe.components.PermissionHelper;
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmark;
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmarkList;
import com.google.mediapipe.framework.AndroidAssetUtil;
import com.google.mediapipe.framework.PacketGetter;
import com.google.mediapipe.glutil.EglManager;
import com.google.protobuf.InvalidProtocolBufferException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
  private static final String TAG = "MainActivity";
  private static final String BINARY_GRAPH_NAME = "pose_tracking_gpu.binarypb";
  private static final String INPUT_VIDEO_STREAM_NAME = "input_video";
  private static final String OUTPUT_VIDEO_STREAM_NAME = "output_video";
  private static final String OUTPUT_LANDMARKS_STREAM_NAME = "pose_landmarks";
  private CameraHelper.CameraFacing CAMERA_FACING = CameraHelper.CameraFacing.BACK;
  // 将相机预览帧垂直翻转，然后将其发送到 FrameProcessor
  // 在 MediaPipe 图形中处理，并在显示处理后的帧时将其翻转回来。
  // 这是必需的，因为 OpenGL 表示假设图像原点位于左下角的图像，而 MediaPipe 通常假设图像原点位于左上角。
  private static final boolean FLIP_FRAMES_VERTICALLY = true;

  static {
    // 加载应用程序所需的所有 libraries
    System.loadLibrary("mediapipe_jni");
    System.loadLibrary("opencv_java3");
  }

  // {@link SurfaceTexture} 其中可以访问相机预览帧
  private SurfaceTexture previewFrameTexture;
  // {@link SurfaceView} 显示由 MediaPipe 图形处理的相机预览帧
  private SurfaceView previewDisplayView;
  // 创建并管理一个 {@link EGLContext}.
  private EglManager eglManager;
  // 将相机预览帧发送到 MediaPipe 图形中进行处理，并将处理后的帧显示到 {@link Surface}
  private FrameProcessor processor;
  // 将 Android 相机的 GL_TEXTURE_EXTERNAL_OES texture 转换为 regular texture，供｛@link
  // FrameProcessor｝和底层 MediaPipe 图形使用
  private ExternalTextureConverter converter;
  // 通过｛@link CameraX｝Jetpack 支持库处理相机访问
  private CameraXPreviewHelper cameraHelper;

  private TextView contactTxt;
  private TextView captureTxt;
  private TextView healthTxt;
  private TextView personTxt;
  private TextView chatTxt;

  private ContactDatabase mDatabase;
  private RecordDatabase recordDatabase;
  private PersonInfoDatabase personInfoDatabase;
  private String poiName;

  // 权限管理
  // 1.本 Activity 需要申请两个权限：发信息和写入存储
  String[] permissions = new String[]{
          Manifest.permission.SEND_SMS,
          Manifest.permission.WRITE_EXTERNAL_STORAGE,
          Manifest.permission.CAMERA,
          Manifest.permission.READ_CONTACTS,
          Manifest.permission.WRITE_CONTACTS,
          Manifest.permission.INTERNET,
          Manifest.permission.ACCESS_NETWORK_STATE,
          Manifest.permission.ACCESS_WIFI_STATE,
          Manifest.permission.READ_PHONE_STATE,
          Manifest.permission.ACCESS_COARSE_LOCATION,
          Manifest.permission.ACCESS_FINE_LOCATION
  };
  // 2.创建一个 mPermissionList，逐个判断哪些权限未授予，未授予的权限存储到 mPermissionList 中
  List<String> mPermissionList = new ArrayList<>();
  private static final int mRequestCode = 100;

  private int frame = 0;
  // 左肩 x/y 坐标 - start
  private float leftShoulderX_start = 0;
  private float leftShoulderY_start = 0;
  // 右肩 x/y 坐标 - start
  private float rightShoulderX_start = 0;
  private float rightShoulderY_start = 0;
  // 左脚跟 x/y 坐标 - start
  private float leftHeelX_start = 0;
  private float leftHeelY_start = 0;
  // 右脚跟 x/y 坐标 - start
  private float rightHeelX_start = 0;
  private float rightHeelY_start = 0;
  // 两肩中点 - start
  private float midShoulderX_start = 0;
  private float midShoulderY_start = 0;
  // 两脚跟中点 - start
  private float midHeelX_start = 0;
  private float midHeelY_start = 0;
  // 两肩中点到两脚跟中点 - start
  private double shoulderToHeel_start = 0;
  // 左右髋 x/y 坐标 - start
  private float leftHipX_start = 0;
  private float rightHipX_start = 0;
  private float leftHipY_start = 0;
  private float rightHipY_start = 0;
  // 两髋中心点 - start
  private float midHipX_start = 0;
  private float midHipY_start = 0;
  // 膝盖 x/y 坐标 - start
  private float leftKneeX_start = 0;
  private float rightKneeX_start = 0;
  private float leftKneeY_start = 0;
  private float rightKneeY_start = 0;
  private float midKneeX_start = 0;
  private float midKneeY_start = 0;
  // 眼睛 x/y 坐标 - start
  private float leftEyeX_start = 0;
  private float rightEyeX_start = 0;
  private float leftEyeY_start = 0;
  private float rightEyeY_start = 0;
  private float midEyeX_start = 0;
  private float midEyeY_start = 0;

  private int quickFallAndNotReachGroundFlag = 0;
  private int headToGroundFlag = 0;
  boolean isOpenDialDialog = false;
  public boolean flag = false;
  private GraphDatabase graphDatabase;
  public boolean alFlag = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    initPermission();

    mDatabase = ContactDatabase.getInstance(this);
    graphDatabase = GraphDatabase.getInstance(this);
    recordDatabase = RecordDatabase.getInstance(this);
    personInfoDatabase = PersonInfoDatabase.getInstance(this);

    warningIfNoContact();

    contactTxt = findViewById(R.id.contactTxt);
    captureTxt = findViewById(R.id.captureTxt);
    healthTxt = findViewById(R.id.healthTxt);
    personTxt = findViewById(R.id.personTxt);

    // 权限申明
    AMapLocationClient.updatePrivacyShow(this, true, true);
    AMapLocationClient.updatePrivacyAgree(this, true);

    personTxt.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent(MainActivity.this, PersonInfo.class));
      }
    });
    contactTxt.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        startActivity(new Intent(MainActivity.this, ContactListView.class));
      }
    });
    captureTxt.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        startActivity(new Intent(MainActivity.this, ImageCaptureView.class));
      }
    });
    healthTxt.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        startActivity(new Intent(MainActivity.this, HealthyNews.class));
      }
    });

    previewDisplayView = new SurfaceView(this);
    setupPreviewDisplayView();

    // 初始化 asset manager，以便 MediaPipe 本地库可以访问 app assets，例如 binary graphs
    AndroidAssetUtil.initializeNativeAssetManager(this);
    eglManager = new EglManager(null);
    processor = new FrameProcessor(
            this,
            eglManager.getNativeContext(),
            BINARY_GRAPH_NAME,
            INPUT_VIDEO_STREAM_NAME,
            OUTPUT_VIDEO_STREAM_NAME);
    processor
            .getVideoSurfaceOutput()
            .setFlipY(FLIP_FRAMES_VERTICALLY);

    processor.addPacketCallback(
            OUTPUT_LANDMARKS_STREAM_NAME,
            (packet) -> {
              Log.v(TAG, "Received Pose landmarks packet.");
              try {
                byte[] landmarksRaw = PacketGetter.getProtoBytes(packet);
                NormalizedLandmarkList poseLandmarks = NormalizedLandmarkList.parseFrom(landmarksRaw);
                Log.v(TAG, "[TS:" + packet.getTimestamp() + "] " + getPoseLandmarksDebugString(poseLandmarks));
                SurfaceHolder srh = previewDisplayView.getHolder();
              } catch (InvalidProtocolBufferException exception) {
                Log.e(TAG, "failed to get proto.", exception);
              }
            });
  }

  public void warningIfNoContact() {
    if (mDatabase.getContactDao().getAllContact().size() == 0 && !flag) {
      flag = true;
      AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
      builder
              .setTitle("设置紧急联系人")
              .setMessage("你还未设置紧急联系人，是否前往设置？")
              .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
//                  flag = false;
//                  startActivity(new Intent(MainActivity.this, ContactListView.class));
                }
              })
              .setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                  flag = false;
                  startActivity(new Intent(MainActivity.this, ContactListView.class));
                }
              })
              .create()
              .show();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    isOpenDialDialog = false;
    converter = new ExternalTextureConverter(
            eglManager.getContext(), 2);
    converter.setFlipY(FLIP_FRAMES_VERTICALLY);
    converter.setConsumer(processor);
    if (PermissionHelper.cameraPermissionsGranted(this)) {
      startCamera();
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    converter.close();
    // 隐藏预览显示，直到我们再次打开相机
    previewDisplayView.setVisibility(View.GONE);
  }

  protected void onCameraStarted(SurfaceTexture surfaceTexture) {
    previewFrameTexture = surfaceTexture;
    // 使显示视图可见以开始显示预览。这将触发添加到 previewDisplayView（的持有者）的 SurfaceHolder.Callback
    previewDisplayView.setVisibility(View.VISIBLE);
  }

  protected Size cameraTargetResolution() {
    return null; // 没有偏好，让 camera（helper）决定
  }

  public void startCamera() {
    cameraHelper = new CameraXPreviewHelper();
    cameraHelper.setOnCameraStartedListener(
            surfaceTexture -> {
              // 监听相机输出画面，每一帧都会保存到 previewFrameTexture 里面去
              onCameraStarted(surfaceTexture);
            });
    cameraHelper.startCamera(
            this, CAMERA_FACING, /* unusedSurfaceTexture= */ null, cameraTargetResolution());
  }

  protected Size computeViewSize(int width, int height) {
    return new Size(width, height);
  }

  protected void onPreviewDisplaySurfaceChanged(
          SurfaceHolder holder, int format, int width, int height) {
    // 基于包含显示的SurfaceView的大小，计算(重新计算)相机预览显示的理想大小（相机预览帧渲染到的区域，可能具有缩放和旋转）
    Size viewSize = computeViewSize(width, height);
    Size displaySize = cameraHelper.computeDisplaySizeFromViewSize(viewSize);
    boolean isCameraRotated = cameraHelper.isCameraRotated();
    // 将转换器连接到相机预览帧作为其输入（通过previewFrameTexture），并将输出宽度和高度配置为计算的显示大小
    converter.setSurfaceTextureAndAttachToGLContext(
            previewFrameTexture,
            isCameraRotated ? displaySize.getHeight() : displaySize.getWidth(),
            isCameraRotated ? displaySize.getWidth() : displaySize.getHeight());
  }

  private void setupPreviewDisplayView() {
    previewDisplayView.setVisibility(View.GONE);
    ViewGroup viewGroup = findViewById(R.id.preview_display_layout);
    viewGroup.addView(previewDisplayView);

    previewDisplayView
            .getHolder()
            .addCallback(
                    new SurfaceHolder.Callback() {
                      @Override
                      public void surfaceCreated(SurfaceHolder holder) {
                        processor.getVideoSurfaceOutput().setSurface(holder.getSurface());
                        Log.d("Surface", "Surface Created");
                      }

                      @Override
                      public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                        onPreviewDisplaySurfaceChanged(holder, format, width, height);
                        // here is width , height is 720,1280
                        Log.d("Surface", "Surface Changed");
                      }

                      @Override
                      public void surfaceDestroyed(SurfaceHolder holder) {
                        processor.getVideoSurfaceOutput().setSurface(null);
                        Log.d("Surface", "Surface destroy");
                      }
                    });
  }

  // 可以从该代码中提取 landmark 的坐标
  // [0.0 , 1.0] -> image width, height
  private String getPoseLandmarksDebugString(NormalizedLandmarkList poseLandmarks) {
    String poseLandmarkStr = "Pose landmarks: " + poseLandmarks.getLandmarkCount() + "\n";
    ArrayList<PoseLandMark> poseMarkers = new ArrayList<PoseLandMark>();

    for (NormalizedLandmark landmark : poseLandmarks.getLandmarkList()) {
      PoseLandMark marker = new PoseLandMark(landmark.getX(), landmark.getY(), landmark.getVisibility());
      poseMarkers.add(marker);
    }

    double bodyAngle = getAngle(poseMarkers.get(12), poseMarkers.get(24), poseMarkers.get(26));

    if (frame == 0) {
      // 左肩 x/y 坐标 - start
      leftShoulderX_start = poseMarkers.get(11).x;
      leftShoulderY_start = poseMarkers.get(11).y;
      // 右肩 x/y 坐标 - start
      rightShoulderX_start = poseMarkers.get(12).x;
      rightShoulderY_start = poseMarkers.get(12).y;
      // 左脚跟 x/y 坐标 - start
      leftHeelX_start = poseMarkers.get(29).x;
      leftHeelY_start = poseMarkers.get(29).y;
      // 右左脚跟 x/y 坐标 - start
      rightHeelX_start = poseMarkers.get(30).x;
      rightHeelY_start = poseMarkers.get(30).y;
      // 两肩中点 - start
      midShoulderX_start = (leftShoulderX_start + rightShoulderX_start) / 2;
      midShoulderY_start = (leftShoulderY_start + rightShoulderY_start) / 2;
      // 两脚跟中点 - start
      midHeelX_start = (leftHeelX_start + rightHeelX_start) / 2;
      midHeelY_start = (leftHeelY_start + rightHeelY_start) / 2;
      // 左右髋 x/y 坐标 - start
      leftHipX_start = poseMarkers.get(23).x;
      rightHipX_start = poseMarkers.get(24).x;
      leftHipY_start = poseMarkers.get(23).y;
      rightHipY_start = poseMarkers.get(24).y;
      // 两髋中心点 - start
      midHipX_start = (leftHipX_start + rightHipX_start) / 2;
      midHipY_start = (leftHipY_start + rightHipY_start) / 2;
      // 膝盖 x/y 坐标 - start
      leftKneeX_start = poseMarkers.get(25).x;
      rightKneeX_start = poseMarkers.get(26).x;
      leftKneeY_start = poseMarkers.get(25).y;
      rightKneeY_start = poseMarkers.get(26).y;
      // 两膝中心点 - start
      midKneeX_start = (leftKneeX_start + rightKneeX_start) / 2;
      midKneeY_start = (leftKneeY_start + rightKneeY_start) / 2;
      // 眼睛 x/y 坐标 - start
      leftEyeX_start = poseMarkers.get(2).x;
      rightEyeX_start = poseMarkers.get(5).x;
      leftEyeY_start = poseMarkers.get(2).y;
      rightEyeY_start = poseMarkers.get(5).y;
      // 两眼中心点 - start
      midEyeX_start = (leftEyeX_start + rightEyeX_start) / 2;
      midEyeY_start = (leftEyeY_start + rightEyeY_start) / 2;
    }
    frame += 1;
    // 每 10 帧一次
    if (frame == 10) {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          // 左肩 x/y 坐标 - end
          float leftShoulderX_end = poseMarkers.get(11).x;
          float leftShoulderY_end = poseMarkers.get(11).y;
          // 右肩 x/y 坐标 - end
          float rightShoulderX_end = poseMarkers.get(12).x;
          float rightShoulderY_end = poseMarkers.get(12).y;
          // 左脚跟 x/y 坐标 - end
          float leftHeelX_end = poseMarkers.get(29).x;
          float leftHeelY_end = poseMarkers.get(29).y;
          // 右左脚跟 x/y 坐标 - end
          float rightHeelX_end = poseMarkers.get(30).x;
          float rightHeelY_end = poseMarkers.get(30).y;
          // 两肩中点 - end
          float midShoulderX_end = (leftShoulderX_end + rightShoulderX_end) / 2;
          float midShoulderY_end = (leftShoulderY_end + rightShoulderY_end) / 2;
          // 两脚跟中点 - end
          float midHeelX_end = (leftHeelX_end + rightHeelX_end) / 2;
          float midHeelY_end = (leftHeelY_end + rightHeelY_end) / 2;
          // 左右髋 x/y 坐标 - end
          float leftHipX_end = poseMarkers.get(23).x;
          float rightHipX_end = poseMarkers.get(24).x;
          float leftHipY_end = poseMarkers.get(23).y;
          float rightHipY_end = poseMarkers.get(24).y;
          // 两髋中心点 - end
          float midHipX_end = (leftHipX_end + rightHipX_end) / 2;
          float midHipY_end = (leftHipY_end + rightHipY_end) / 2;
          // 左右膝 x/y 坐标 - end
          float leftKneeX_end = poseMarkers.get(25).x;
          float rightKneeX_end = poseMarkers.get(26).x;
          float leftKneeY_end = poseMarkers.get(25).y;
          float rightKneeY_end = poseMarkers.get(26).y;
          // 两膝中心点 - end
          float midKneeX_end = (leftKneeX_end + rightHipX_end) / 2;
          float midKneeY_end = (leftKneeY_end + rightKneeY_end) / 2;
          // 左右眼 x/y 坐标 - end
          float leftEyeX_end = poseMarkers.get(2).x;
          float rightEyeX_end = poseMarkers.get(5).x;
          float leftEyeY_end = poseMarkers.get(2).y;
          float rightEyeY_end = poseMarkers.get(5).y;
          // 两眼中心点 - end
          float midEyeX_end = (leftEyeX_end + rightEyeX_end) / 2;
          float midEyeY_end = (leftEyeY_end + rightEyeY_end) / 2;

          // 两肩中心点在10s内的变化
          double v = Math.sqrt(
                  Math.pow(midShoulderX_start - midShoulderX_end, 2) + Math.pow(midShoulderY_start - midShoulderY_end, 2));
          // 脚跟到肩中心点在10s内的变化 - 人的近似感知高度
          double seeHeight = Math
                  .sqrt(Math.pow(midHeelX_end - midShoulderX_start, 2) + Math.pow(midHeelY_end - midShoulderY_start, 2));
          // (两肩中心点在10s内的变化)/(脚跟到肩中心点在10s内的变化)
          double radio = v / seeHeight;
          // 两髋中心点到两脚跟中心点在10s内的变化
          double hipToHeelHeight = Math.sqrt(Math.pow(midHeelY_end - midHeelY_start, 2) + Math.pow(midHipY_end - midHipY_start, 2));
          double radio_hip = hipToHeelHeight / seeHeight;
          // 髋部到地面距离
          double hipAndHeelDistance = (midHeelY_end - midHipY_end) / seeHeight;
          // 当为跪状时立即报警
          double kneeToHeelDistance = midHeelY_end - midKneeY_end;

          // 跪状时报警
          if (!alFlag) {
            if (kneeToHeelDistance <= 0) {
              if (!isOpenDialDialog) {
                saveImage();
                isOpenDialDialog = true;
                try {
                  saveGraphData();
                } catch (Exception e) {
                  e.printStackTrace();
                }
                callForHelp("kneeSetting");
                alFlag = true;
              }
            }
          }

          // 当髋与肩加速度较大且身体角度弓到一定角度后，开启定时，当3s内髋部离地较近时，则为跌倒
          if (!alFlag) {
            if (quickFallAndNotReachGroundFlag > 0) {
              quickFallAndNotReachGroundFlag += 1;
            }
            // 如果检测到弯腰下蹲姿势，进入下一轮判断。并且如果 quickFallAndNotReachGroundFlag 不等于 1 的情况下能加速判断
            if (radio_hip > 0.22 && radio > 0.27 && bodyAngle < 120) {
              quickFallAndNotReachGroundFlag += 1;
            }
            // 大约4.2s内
            if (quickFallAndNotReachGroundFlag >= 6) {
              if (hipAndHeelDistance <= 0.15) {
                if (!isOpenDialDialog) {
                  saveImage();
                  isOpenDialDialog = true;
                  try {
                    saveGraphData();
                  } catch (Exception e) {
                    e.printStackTrace();
                  }
                  callForHelp("main");
                }
              }
              quickFallAndNotReachGroundFlag = 0;
              alFlag = true;
            }
          }


          // 当髋部距离地面较近时，开启计数器，当大约3-4s内如果头部距离仍是很近的话，则报警
          if (!alFlag) {
            if (headToGroundFlag > 0) {
              headToGroundFlag += 1;
            }
            if (hipAndHeelDistance <= 0.15) {
              headToGroundFlag += 1;
            }
            if (headToGroundFlag >= 8) {
              if (midEyeY_end - midHeelY_end < 0.1) {
                if (!isOpenDialDialog) {
                  saveImage();
                  isOpenDialDialog = true;
                  try {
                    saveGraphData();
                  } catch (Exception e) {
                    e.printStackTrace();
                  }
                  callForHelp("lieDown");
                }
              }
              headToGroundFlag = 0;
              alFlag = true;
            }
          }

        }
      });
      frame = 0;
      alFlag = false;
    }
    return poseLandmarkStr;
  }

  // 保存报警时的经纬度和时间戳
  private void saveGraphData() throws Exception {
    PoiSearchV2.Query query = new PoiSearchV2.Query("", "", "");
    query.setPageSize(10);
    query.setPageNum(0);
    query.setDistanceSort(true);

    // 获取经纬度
    final AMapLocationClient mLocationClient = new AMapLocationClient(getApplicationContext());
    AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
    mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
    mLocationOption.setInterval(0);
    mLocationClient.setLocationOption(mLocationOption);
    mLocationClient.setLocationListener(new AMapLocationListener() {
      @Override
      public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null) {
          double latitude = aMapLocation.getLatitude();
          double longitude = aMapLocation.getLongitude();

          LatLonPoint latLonPoint = new LatLonPoint(latitude, longitude);
          PoiSearchV2.SearchBound searchBound = new PoiSearchV2.SearchBound(latLonPoint, 1000);
          try {
            PoiSearchV2 poiSearch = new PoiSearchV2(MainActivity.this, query);
            poiSearch.setBound(searchBound);
            poiSearch.setOnPoiSearchListener(new PoiSearchV2.OnPoiSearchListener() {
              @Override
              public void onPoiSearched(PoiResultV2 poiResultV2, int i) {
                ArrayList<PoiItemV2> poiItemList = poiResultV2.getPois();
                if (poiItemList != null && poiItemList.size() > 0) {
                  PoiItemV2 poiItem = poiItemList.get(0);
                  poiName = poiItem.getTitle();
                }
                // 获取时间戳
                long fallTime = System.currentTimeMillis();
                long oneWeekAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000;
                graphDatabase.getGraphDao().deleteOneWeekAgoGraph(oneWeekAgo);
                graphDatabase.getGraphDao().insertOneGraph(new Graph(latitude, longitude, fallTime, poiName));
              }

              @Override
              public void onPoiItemSearched(PoiItemV2 poiItemV2, int i) {
              }
            });
            poiSearch.searchPOIAsyn();
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
        mLocationClient.stopLocation();
      }
    });
    mLocationClient.startLocation();
  }

  // 拍照 TODO
  private void saveImage() {
//    File file = new File(getExternalMediaDirs()[0], System.currentTimeMillis() + ".jpg");
//    Log.e(TAG, "saveImage: " + file.getAbsolutePath());
//
//    cameraHelper.takePicture(file, new ImageCapture.OnImageSavedCallback() {
//      @Override
//      public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
//        Toast.makeText(getApplicationContext(), "写入成功", Toast.LENGTH_SHORT).show();
//      }
//
//      @Override
//      public void onError(@NonNull ImageCaptureException exception) {
//        Toast.makeText(getApplicationContext(), "写入失败", Toast.LENGTH_SHORT).show();
//      }
//    });
  }

  private void callForHelp(String type) {
    if (mDatabase.getContactDao().getAllContact().size() == 0) {
      warningIfNoContact();
    }
    if (isOpenDialDialog) {
      if (mDatabase.getContactDao().getAllContact().size() != 0) {
        String phone = mDatabase.getContactDao().getAllContact().get(0).getPhone();
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder
                .setTitle("确认联系家人吗？(并默认发送短信给紧急联系人)")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialogInterface, int i) {
                    isOpenDialDialog = false;

                    switch (type) {
                      case "main": {
                        if (recordDatabase.getRecordDao().getRecords().size() == 0) {
                          recordDatabase.getRecordDao().insertWrongWarningItem(new Record(0, 1, 0, 1, 0, 0, 0, 0));
                        } else {
                          Record record = recordDatabase.getRecordDao().getRecords().get(0);
                          record.setWarningTimes(record.getWarningTimes() + 1);
                          record.setMainAlgorithm(record.getMainAlgorithm() + 1);
                          recordDatabase.getRecordDao().updateRecord(record);
                        }
                      }
                      case "kneeSetting": {
                        if (recordDatabase.getRecordDao().getRecords().size() == 0) {
                          recordDatabase.getRecordDao().insertWrongWarningItem(new Record(0, 1, 1, 0, 0, 0, 0, 0));
                        } else {
                          Record record = recordDatabase.getRecordDao().getRecords().get(0);
                          record.setWarningTimes(record.getWarningTimes() + 1);
                          record.setKneeSettingAlgorithm(record.getKneeSettingAlgorithm() + 1);
                          recordDatabase.getRecordDao().updateRecord(record);
                        }
                      }
                      case "lieDown": {
                        if (recordDatabase.getRecordDao().getRecords().size() == 0) {
                          recordDatabase.getRecordDao().insertWrongWarningItem(new Record(0, 1, 0, 0, 1, 0, 0, 0));
                        } else {
                          Record record = recordDatabase.getRecordDao().getRecords().get(0);
                          record.setWarningTimes(record.getWarningTimes() + 1);
                          record.setLieDownAlgorithm(record.getLieDownAlgorithm() + 1);
                          recordDatabase.getRecordDao().updateRecord(record);
                        }
                      }
                    }


                    SmsManager smsManager = SmsManager.getDefault();
                    List<com.example.arr_pose1.room.PersonInfo.PersonInfo> personInfoList = personInfoDatabase.getPersonInfoDao().getAllPersonInfo();

                    com.example.arr_pose1.room.PersonInfo.PersonInfo personInfo = personInfoList.size() != 0 ? personInfoList.get(0) : null;
                    String name = personInfo != null ? personInfo.getName() : "";
                    String age = personInfo != null ? personInfo.getAge() : "";
                    String disease = personInfo != null ? personInfo.getDisease() : "";
                    String allergy = personInfo != null ? personInfo.getAllergy() : "";
                    String address = personInfo != null ? personInfo.getAddress() : "";
                    String other = personInfo != null ? personInfo.getOther() : "";
                    String message = personInfoDatabase.getPersonInfoDao().getAllPersonInfo().size() != 0
                            ? name + "老人跌倒了\n位置：" + poiName + "\n年龄：" + age + "岁\n疾病史为：" + disease + "\n过敏史为：" + allergy + "\n补充说明为：" + other
                            : "老人跌倒了。\n位置是" + poiName;
                    smsManager.sendTextMessage(phone, null, message, null, null);

                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + phone));
                    if (intent.resolveActivity(getPackageManager()) != null) {
                      startActivity(intent);
                    }
                  }
                })
                .setNegativeButton("这是误报", new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialogInterface, int i) {
                    isOpenDialDialog = false;

                    switch (type) {
                      case "main": {
                        if (recordDatabase.getRecordDao().getRecords().size() == 0) {
                          recordDatabase.getRecordDao().insertWrongWarningItem(new Record(1, 1, 0, 1, 0, 1, 0, 0));
                        } else {
                          Record record = recordDatabase.getRecordDao().getRecords().get(0);
                          record.setWrongWarningTimes(record.getWrongWarningTimes() + 1);
                          record.setWarningTimes(record.getWarningTimes() + 1);
                          record.setMainAlgorithm(record.getMainAlgorithm() + 1);
                          record.setWrongMainAlgorithm(record.getWrongMainAlgorithm() + 1);
                          recordDatabase.getRecordDao().updateRecord(record);
                        }
                      }
                      case "kneeSetting": {
                        if (recordDatabase.getRecordDao().getRecords().size() == 0) {
                          recordDatabase.getRecordDao().insertWrongWarningItem(new Record(1, 1, 1, 0, 0, 0, 1, 0));
                        } else {
                          Record record = recordDatabase.getRecordDao().getRecords().get(0);
                          record.setWrongKneeSettingAlgorithm(record.getWrongKneeSettingAlgorithm() + 1);
                          record.setWarningTimes(record.getWarningTimes() + 1);
                          record.setKneeSettingAlgorithm(record.getKneeSettingAlgorithm() + 1);
                          record.setWrongWarningTimes(record.getWrongWarningTimes() + 1);
                          recordDatabase.getRecordDao().updateRecord(record);
                        }
                      }
                      case "lieDown": {
                        if (recordDatabase.getRecordDao().getRecords().size() == 0) {
                          recordDatabase.getRecordDao().insertWrongWarningItem(new Record(1, 1, 0, 0, 1, 0, 0, 1));
                        } else {
                          Record record = recordDatabase.getRecordDao().getRecords().get(0);
                          record.setWrongWarningTimes(record.getWrongWarningTimes() + 1);
                          record.setWarningTimes(record.getWarningTimes() + 1);
                          record.setLieDownAlgorithm(record.getLieDownAlgorithm() + 1);
                          record.setWrongLieDownAlgorithm(record.getWrongLieDownAlgorithm() + 1);
                          recordDatabase.getRecordDao().updateRecord(record);
                        }
                      }
                    }
                  }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                  @Override
                  public void onDismiss(DialogInterface dialogInterface) {
                    isOpenDialDialog = false;
                  }
                })
                .create()
                .show();
      }
    }
  }

  public void changeCamera(View view) {
    converter.close();
    // 隐藏预览显示，直到我们再次打开相机
    previewDisplayView.setVisibility(View.GONE);
    converter = new ExternalTextureConverter(
            eglManager.getContext(), 2);
    converter.setFlipY(FLIP_FRAMES_VERTICALLY);
    converter.setConsumer(processor);
    if (CAMERA_FACING == CameraHelper.CameraFacing.FRONT) {
      CAMERA_FACING = CameraHelper.CameraFacing.BACK;
    } else {
      CAMERA_FACING = CameraHelper.CameraFacing.FRONT;
    }
    startCamera();
  }

  // 权限判断和申请
  private void initPermission() {
    mPermissionList.clear();
    // 逐个判断你要的权限是否通过
    for (int i = 0; i < permissions.length; i += 1) {
      if (ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
        mPermissionList.add(permissions[i]); // 添加还未授权的权限
      }
    }
    // 申请权限
    if (mPermissionList.size() > 0) {
      ActivityCompat.requestPermissions(this, permissions, mRequestCode);
    }
  }

  static double getAngle(PoseLandMark firstPoint, PoseLandMark midPoint, PoseLandMark lastPoint) {
    double result =
            Math.toDegrees(
                    Math.atan2(lastPoint.getY() - midPoint.getY(), lastPoint.getX() - midPoint.getX())
                            - Math.atan2(firstPoint.getY() - midPoint.getY(), firstPoint.getX() - midPoint.getX()));
    result = Math.abs(result); // Angle should never be negative
    if (result > 180) {
      result = (360.0 - result); // Always get the acute representation of the angle
    }
    return result;
  }
}