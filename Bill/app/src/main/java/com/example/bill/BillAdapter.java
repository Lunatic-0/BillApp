package com.example.bill;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BillAdapter extends RecyclerView.Adapter<BillAdapter.BillViewHolder> {

    private List<Transaction> dataList;
    private OnItemLongClickListener longClickListener;

    public BillAdapter(List<Transaction> list) {
        this.dataList = list;
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    @NonNull
    @Override
    public BillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bill, parent, false);
        return new BillViewHolder(v);
    }



    @Override
    public void onBindViewHolder(@NonNull BillViewHolder holder, int position) {
        Transaction item = dataList.get(position);
        holder.tvDate.setText(item.getDate());
        holder.tvDesc.setText(item.getDescription());
        holder.tvAmount.setText((item.isIncome() ? "+¥" : "-¥") + String.format("%.2f", item.getAmount()));
        holder.tvAmount.setTextColor(item.isIncome() ? Color.GREEN : Color.RED);
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(position);
                return true;
            }
            return false;
        });
    }
    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class BillViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvDesc, tvAmount;


        public BillViewHolder(View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvDesc = itemView.findViewById(R.id.tv_desc);
            tvAmount = itemView.findViewById(R.id.tv_amount);


        }
    }


}

