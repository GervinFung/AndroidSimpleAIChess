package com.example.chess;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chess.engine.board.MoveLog;

public final class MoveHistory extends RecyclerView.Adapter<MoveHistory.ViewHolder>{

    private final MoveLog movelog;

    public MoveHistory(final MoveLog movelog) {
        this.movelog = movelog;
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.move_history, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final String text = this.movelog.getMovesNumber().get(position).toString() + ". " + this.movelog.getMoves().get(position).toString();
        holder.history.setText(text);
        holder.history.setTextSize(20);
        holder.history.setTextColor(Color.WHITE);
    }

    @Override
    public int getItemCount() { return this.movelog.getMoves().size(); }

    protected static final class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView history;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            this.history = itemView.findViewById(R.id.history);
        }
    }
}