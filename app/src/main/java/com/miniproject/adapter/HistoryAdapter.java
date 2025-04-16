package com.miniproject.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import com.miniproject.R;
import com.miniproject.model.HistoryItem;
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<HistoryItem> historyList;

    public HistoryAdapter(List<HistoryItem> historyList) {
        this.historyList = historyList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textPhoneNumber, textRisk, textTime;

        public ViewHolder(View itemView) {
            super(itemView);
            textPhoneNumber = itemView.findViewById(R.id.textPhoneNumber);
            textRisk = itemView.findViewById(R.id.textRisk);
            textTime = itemView.findViewById(R.id.textTime);
        }
    }

    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.history_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HistoryAdapter.ViewHolder holder, int position) {
        HistoryItem item = historyList.get(position);
        holder.textPhoneNumber.setText("Number: " + item.getPhoneNumber());
        holder.textRisk.setText("Risk: " + item.getRiskLevel());
        holder.textTime.setText("Time: " + item.getTime());
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }
}
