package com.example.arr_pose1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.arr_pose1.HealthNews.Adapter.HealthNewsAdapter;
import com.example.arr_pose1.HealthNews.HealthNewsRepository;
import com.example.arr_pose1.HealthNews.bean.HealthNews;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class HealthyNews extends AppCompatActivity {
  private List<HealthNews> healthNewsList = new ArrayList<>();
  private HealthNewsAdapter healthNewsAdapter;
  private int page = 1;

  @SuppressLint("NotifyDataSetChanged")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_healthy_news);

    overridePendingTransition(R.anim.bottom_in, R.anim.bottom_silent);

    RecyclerView recyclerView = findViewById(R.id.recycler_view);
    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    recyclerView.setLayoutManager(layoutManager);
    healthNewsAdapter = new HealthNewsAdapter(healthNewsList);
    recyclerView.setAdapter(healthNewsAdapter);

    Executor executor = Executors.newSingleThreadExecutor();
    executor.execute(new Runnable() {
      @Override
      public void run() {
        try {
          loadPage(page);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });

    // 设置列表项点击事件，进入健康资讯新闻详情页面
    healthNewsAdapter.setOnItemClickListener(new HealthNewsAdapter.OnItemClickListener() {
      @Override
      public void onItemClick(HealthNews healthNews) {
        Intent intent = new Intent(HealthyNews.this, HealthNewsDetail.class);
        intent.putExtra("url", healthNews.getUrl());
        intent.putExtra("title", healthNews.getTitle());
        startActivity(intent);
      }
    });

    recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override
      public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        LinearLayoutManager layoutManager1 = (LinearLayoutManager) recyclerView.getLayoutManager();
        int visibleItemCount = layoutManager1.getChildCount();
        int totalItemCount = layoutManager1.getItemCount();
        int firstVisibleItemPosition = layoutManager1.findFirstVisibleItemPosition();
        // 滑动到底部，加载下一页
        if (!recyclerView.canScrollVertically(1) && (totalItemCount - visibleItemCount) <= firstVisibleItemPosition) {
          page += 1;
          Executor executor = Executors.newSingleThreadExecutor();
          executor.execute(new Runnable() {
            @Override
            public void run() {
              try {
                loadPage(page);
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          });
        }
      }
    });
  }

  @Override
  public void finish() {
    super.finish();
    overridePendingTransition(R.anim.bottom_silent, R.anim.bottom_out);
  }

  private void loadPage(int page) throws Exception {
    // 加载健康资讯新闻列表
    HealthNewsRepository healthNewsRepository = HealthNewsRepository.getInstance();
    List<HealthNews> healthNewsList = healthNewsRepository.getHealthNews("7489a44a34cf0054b704ba1829cda829", 10, page);
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (healthNewsList != null) {
          HealthyNews.this.healthNewsList.addAll(healthNewsList);
          healthNewsAdapter.notifyDataSetChanged();
        }
      }
    });
  }
}