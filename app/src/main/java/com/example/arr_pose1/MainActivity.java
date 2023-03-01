package com.example.arr_pose1;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.telephony.SmsManager;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.example.arr_pose1.room.ContactDatabase;
import com.example.arr_pose1.room.Graph;
import com.example.arr_pose1.room.GraphDatabase;
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

  private ContactDatabase mDatabase;

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
  // 当 radio 达到 0.25 时，开始累加，当 quickFallAndNotReachGroundFlag <= 50 时，只需要检测高度
  private int quickFallAndNotReachGroundFlag = 0;
  boolean isOpenDialDialog = false;
  private boolean flag = false;
  private GraphDatabase graphDatabase;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    initPermission();

    mDatabase = ContactDatabase.getInstance(this);
    graphDatabase = GraphDatabase.getInstance(this);

    warningIfNoContact();

    contactTxt = findViewById(R.id.contactTxt);
    captureTxt = findViewById(R.id.captureTxt);

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

  private void warningIfNoContact() {
    if (mDatabase.getContactDao().getAllContact().size() == 0 && !flag) {
      flag = true;
      AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
      builder
              .setTitle("设置紧急联系人")
              .setMessage("你还未设置紧急联系人，是否前往设置？")
              .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                  flag = false;
                  startActivity(new Intent(MainActivity.this, ContactListView.class));
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

          // 两肩中心点在10s内的变化
          double v = Math.abs(Math.sqrt(
                  Math.pow(midShoulderX_start - midShoulderX_end, 2) + Math.pow(midShoulderY_start - midShoulderY_end, 2)));
          // 脚跟到肩中心点在10s内的变化 - 人的近似感知高度
          double seeHeight = Math.abs(Math
                  .sqrt(Math.pow(midHeelX_end - midShoulderX_start, 2) + Math.pow(midHeelY_end - midShoulderY_start, 2)));
          // (两肩中心点在10s内的变化)/(脚跟到肩中心点在10s内的变化)
          double radio = v / seeHeight;

          // 两髋中心点在10s内的变化
          double hipToHeelHeight = Math.abs(midHipY_end - midHeelY_end);
          double radio_hip = hipToHeelHeight / seeHeight;

          // 当出现突然跌倒情况时，立即报警
          if (radio_hip < 0.27 && radio > 0.25) {
            if (!isOpenDialDialog) {
              isOpenDialDialog = true;
              saveImage();
              try {
                saveGraphData();
              } catch (Exception e) {
                e.printStackTrace();
              }
              callForHelp();
            }
            quickFallAndNotReachGroundFlag = 0;
          }

          // 当快速下落时，这时设置一个判断条件，3s 内如果髋中心点与脚跟中心点距离小于一个阈值，则报警
          if (radio > 0.25 && quickFallAndNotReachGroundFlag == 0) {
            quickFallAndNotReachGroundFlag += 1;
          }
          if (quickFallAndNotReachGroundFlag > 0) {
            quickFallAndNotReachGroundFlag += 1;
            // each frame == 0.7s
            if (quickFallAndNotReachGroundFlag <= 5) {
              if (radio_hip < 0.27) {
                quickFallAndNotReachGroundFlag = 0;
                if (!isOpenDialDialog) {
                  isOpenDialDialog = true;
                  saveImage();
                  try {
                    saveGraphData();
                  } catch (Exception e) {
                    e.printStackTrace();
                  }
                  callForHelp();
                }
              }
            } else {
              quickFallAndNotReachGroundFlag = 0;
            }
          }
        }
      });
      frame = 0;
    }
    return poseLandmarkStr;
  }

  // 保存报警时的经纬度和时间戳
  private void saveGraphData() throws Exception {
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
          // 获取时间戳
          long fallTime = System.currentTimeMillis();
          long oneWeekAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000;
          graphDatabase.getGraphDao().deleteOneWeekAgoGraph(oneWeekAgo);
          graphDatabase.getGraphDao().insertOneGraph(new Graph(latitude, longitude, fallTime));
          mLocationClient.stopLocation();
        }
      }
    });
    mLocationClient.startLocation();
  }

  // 拍照 TODO
  private void saveImage() {
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

  private void callForHelp() {
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

                    SmsManager smsManager = SmsManager.getDefault();
                    String message = "test";
                    smsManager.sendTextMessage(phone, null, message, null, null);

                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + phone));
                    if (intent.resolveActivity(getPackageManager()) != null) {
                      startActivity(intent);
                    }
                  }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialogInterface, int i) {
                    isOpenDialDialog = false;
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
}