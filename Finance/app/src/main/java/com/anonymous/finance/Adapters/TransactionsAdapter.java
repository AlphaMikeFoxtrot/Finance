package com.anonymous.finance.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.anonymous.finance.Classes.Transaction;
import com.anonymous.finance.R;

import java.util.ArrayList;

/**
 * Created by ANONYMOUS on 23-Mar-18.
 */

public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.TransactionViewHolder> {

    Context context;
    ArrayList<Transaction> transactions;

    public TransactionsAdapter(Context context, ArrayList<Transaction> transactions) {
        this.context = context;
        this.transactions = transactions;
    }

    @Override
    public TransactionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.transactions_item, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TransactionViewHolder holder, int position) {

        holder.comment.setText(this.transactions.get(position).getComment());
        String amountFinal;
        if(this.transactions.get(position).getType().contains("debit")){
            StringBuilder text = new StringBuilder();
            holder.amount.setText(this.transactions.get(position).getAmount());
            holder.amount.setTextColor(Color.parseColor("#008000"));
        } else if (this.transactions.get(position).getType().contains("credit")) {
            StringBuilder text = new StringBuilder();
            holder.amount.setText(this.transactions.get(position).getAmount());
            holder.amount.setTextColor(Color.RED);
        }
        holder.amount.setText(this.transactions.get(position).getAmount());

    }

    @Override
    public int getItemCount() {
        return this.transactions.size();
    }

    public class TransactionViewHolder extends RecyclerView.ViewHolder{

        TextView comment, amount;

        public TransactionViewHolder(View itemView) {
            super(itemView);

            this.comment = itemView.findViewById(R.id.comment);
            this.amount = itemView.findViewById(R.id.amount);
        }
    }

}
