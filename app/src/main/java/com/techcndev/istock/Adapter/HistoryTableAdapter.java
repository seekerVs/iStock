package com.techcndev.istock.Adapter;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.techcndev.istock.BrandActivity;
import com.techcndev.istock.DatabaseHelper.BrandDBHelper;
import com.techcndev.istock.DatabaseHelper.HistoryDBHelper;
import com.techcndev.istock.InventoryActivity;
import com.techcndev.istock.Model.HistoryModel;
import com.techcndev.istock.R;

import java.util.List;

public class HistoryTableAdapter extends RecyclerView.Adapter {
    List<HistoryModel> brandList;
    HistoryDBHelper DBHistory;
    BrandDBHelper DBBrand;
    Context context;
    String currentUser;


    public HistoryTableAdapter(List<HistoryModel> brandList) {
        this.brandList = brandList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.table_list_item, parent, false);
        DBHistory = new HistoryDBHelper(itemView.getContext());
        DBBrand = new BrandDBHelper(itemView.getContext());
//        currentUser = sharedPreferences.getString("current_user", null);
        return new RowViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RowViewHolder rowViewHolder = (RowViewHolder) holder;

        int rowPos = rowViewHolder.getAdapterPosition();

        if (rowPos == 0) {
            rowViewHolder.DateTextView.setBackgroundResource(R.drawable.table_header_cell_bg);
            rowViewHolder.QtyInTextView.setBackgroundResource(R.drawable.table_header_cell_bg);
            rowViewHolder.QtyOutTextView.setBackgroundResource(R.drawable.table_header_cell_bg);
            rowViewHolder.BalanceTextView.setBackgroundResource(R.drawable.table_header_cell_bg);
            rowViewHolder.SalesTextView.setBackgroundResource(R.drawable.table_header_cell_bg);

            rowViewHolder.DateTextView.setText("Date");
            rowViewHolder.QtyInTextView.setText("Quantity In");
            rowViewHolder.QtyOutTextView.setText("Quantity Out");
            rowViewHolder.BalanceTextView.setText("Balance");
            rowViewHolder.SalesTextView.setText("Sales");
        } else {
            HistoryModel modal = brandList.get(rowPos-1);

            rowViewHolder.DateTextView.setBackgroundResource(R.drawable.table_content_cell_bg);
            rowViewHolder.QtyInTextView.setBackgroundResource(R.drawable.table_content_cell_bg);
            rowViewHolder.QtyOutTextView.setBackgroundResource(R.drawable.table_content_cell_bg);
            rowViewHolder.BalanceTextView.setBackgroundResource(R.drawable.table_content_cell_bg);
            rowViewHolder.SalesTextView.setBackgroundResource(R.drawable.table_content_cell_bg);

            rowViewHolder.DateTextView.setText(modal.getDate());
            rowViewHolder.QtyInTextView.setText(modal.getQtyin()+"");
            rowViewHolder.QtyOutTextView.setText(modal.getQtyout()+"");
            rowViewHolder.BalanceTextView.setText(modal.getBalance()+"");
            rowViewHolder.SalesTextView.setText(modal.getSales()+"");
        }
    }

    @Override
    public int getItemCount() {
        return brandList.size()+1;
    }

    public class RowViewHolder extends RecyclerView.ViewHolder {
        protected TextView DateTextView;
        protected TextView QtyInTextView;
        protected TextView QtyOutTextView;
        protected TextView BalanceTextView;
        protected TextView SalesTextView;
        String HistoryId;

        public RowViewHolder(View itemView) {
            super(itemView);
            context = itemView.getContext();
            DateTextView = itemView.findViewById(R.id.date_textview);
            QtyInTextView = itemView.findViewById(R.id.qtyin_textview);
            QtyOutTextView = itemView.findViewById(R.id.qtyout_textview);
            BalanceTextView = itemView.findViewById(R.id.balance_textview);
            SalesTextView = itemView.findViewById(R.id.sales_textview);


        }

//        public void setOnLongClickListener(View.OnLongClickListener onLongClickListener, String id) {
//            HistoryId = id;
//            String strDate = DateTextView.getText().toString();
//
//            android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(context);
//            alertDialog.setTitle("Delete Date History");
//            alertDialog.setMessage("Are your sure you want to delete the history of this brand in \"" + strDate +"\"?");
//            alertDialog.setIcon(R.drawable.success_filled);
//            alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                public void onClick(DialogInterface dialog, int id) {
//                    DBHistory.delete_history(HistoryId,strDate);
//                    Intent intent = new Intent(context, InventoryActivity.class);
////                                adapter.notifyDataSetChanged();
//                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//                    context.startActivity(intent);
//                    dialog.dismiss();
//                }
//            });
//            alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    dialog.dismiss();
//                }
//            });
//            android.app.AlertDialog dialog_create = alertDialog.create();
//            dialog_create.show();
//            }
    }
}
