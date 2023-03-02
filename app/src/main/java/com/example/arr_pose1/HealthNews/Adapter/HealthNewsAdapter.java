package com.example.arr_pose1.HealthNews.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.arr_pose1.HealthNews.bean.HealthNews;
import com.example.arr_pose1.R;

import java.util.List;

public class HealthNewsAdapter extends RecyclerView.Adapter<HealthNewsAdapter.ViewHolder> {
  private List<HealthNews> mHealthNewsList;
  private OnItemClickListener mListener;

  public HealthNewsAdapter(List<HealthNews> healthNewsList) {
    mHealthNewsList = healthNewsList;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_health_news, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    HealthNews healthNews = mHealthNewsList.get(position);
    holder.mTitle.setText(healthNews.getTitle());
    holder.mSummary.setText(healthNews.getDescription());
    holder.mPubDate.setText((CharSequence) healthNews.getCtime());
    holder.itemView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (mListener != null) {
          mListener.onItemClick(healthNews);
        }
      }
    });
  }

  @Override
  public int getItemCount() {
    return mHealthNewsList.size();
  }

  public void setOnItemClickListener(OnItemClickListener listener) {
    mListener = listener;
  }

  static class ViewHolder extends RecyclerView.ViewHolder {
    TextView mTitle;
    TextView mSummary;
    TextView mPubDate;
    TextView mSource;

    ViewHolder(View itemView) {
      super(itemView);
      mTitle = itemView.findViewById(R.id.tv_title);
      mSummary = itemView.findViewById(R.id.tv_summary);
      mPubDate = itemView.findViewById(R.id.tv_date);
      mSource = itemView.findViewById(R.id.tv_source);
    }

  }
  public interface OnItemClickListener {
    void onItemClick(HealthNews healthNews);
  }
}
