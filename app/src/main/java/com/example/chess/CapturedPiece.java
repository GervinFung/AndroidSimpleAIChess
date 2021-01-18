package com.example.chess;

import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chess.engine.board.BoardUtils;
import com.example.chess.engine.board.Move;
import com.example.chess.engine.pieces.Piece;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.chess.MainActivity.MoveLog;
import com.example.chess.engine.League;

public class CapturedPiece extends RecyclerView.Adapter<CapturedPiece.ViewHolder>{
    private final List<Piece> takenPieces;
    private final HashMap<Piece, Integer> takenPiecesMap;

    @RequiresApi(api = Build.VERSION_CODES.N)
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
        this.takenPieces = this.takenPiecesMap.keySet().stream().sorted(Comparator.comparingInt(Piece::getPieceValue)).collect(Collectors.toList());
        this.notifyDataSetChanged();
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