package com.example.chess.gui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chess.R;
import com.example.chess.engine.board.BoardUtils;
import com.example.chess.engine.board.Move;
import com.example.chess.engine.board.MoveLog;
import com.example.chess.engine.pieces.Piece;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.example.chess.engine.League;

public final class CapturedPiece extends RecyclerView.Adapter<CapturedPiece.ViewHolder>{
    private final List<Piece> takenPieces;
    private final HashMap<Piece, Integer> takenPiecesMap;

    public CapturedPiece(final MoveLog moveLog, final League league) {
        this.takenPiecesMap = new HashMap<>();
        for (final Move move: moveLog.getMoves()) {
            if (move.isAttack()) {
                final Piece takenPiece = move.getAttackedPiece();
                if (takenPiece.getLeague() == league) {
                    if (this.notContainSamePiece(this.takenPiecesMap, takenPiece)) {
                        this.takenPiecesMap.put(takenPiece, 1);
                    }
                }
            }
        }
        this.takenPieces = this.sortedPieces(this.takenPiecesMap);
        this.notifyDataSetChanged();
    }

    private List<Piece> sortedPieces(final HashMap<Piece, Integer> takenPiecesMap) {
        final List<Piece> unsortedPieces = new ArrayList<>(takenPiecesMap.keySet());
        Collections.sort(unsortedPieces, (piece1, piece2) -> {
            if (piece1.getPieceValue() > piece2.getPieceValue()) { return 1; }
            else if (piece1.getPieceValue() < piece2.getPieceValue()) { return -1; }
            return 0;
        });
        return Collections.unmodifiableList(unsortedPieces);
    }

    private boolean notContainSamePiece(final HashMap<Piece, Integer> takenPieces, final Piece takenPiece) {
        for (final Piece piece : takenPieces.keySet()) {

            if (takenPiece.toString().equals(piece.toString())) {
                final int quantity = takenPieces.get(piece) + 1;
                takenPieces.remove(piece);
                takenPieces.put(takenPiece, quantity);
                return false;
            }
        }
        return true;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.captured_piece, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final String text = this.takenPiecesMap.get(this.takenPieces.get(position)) + " x";
        holder.quantity.setText(text);
        holder.quantity.setTextSize(20);
        holder.quantity.setTextColor(Color.WHITE);
        holder.capturedPieceImageView.setImageResource(BoardUtils.getPieceImage(this.takenPieces.get(position)));
    }

    @Override
    public int getItemCount() { return this.takenPieces.size(); }

    protected static final class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView quantity;
        private final ImageView capturedPieceImageView;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            this.quantity = itemView.findViewById(R.id.textViewQuantityCaptured);
            this.capturedPieceImageView = itemView.findViewById(R.id.imageViewCapturedPiece);
        }
    }
}