package com.example.chess.gui;

import android.app.Dialog;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.chess.MainActivity;
import com.example.chess.R;
import com.example.chess.engine.board.Board;
import com.example.chess.engine.board.BoardUtils;
import com.example.chess.engine.board.Move;
import com.example.chess.engine.pieces.Piece;

import java.util.List;

public final class PawnPromotionUI {

    private final MainActivity mainActivity;

    public PawnPromotionUI(final MainActivity mainActivity) { this.mainActivity = mainActivity; }

    private Board promotePawn(final Move.PawnPromotion pawnPromotion, final Board board) {
        final Board.Builder builder = new Board.Builder(board.getMoveCount(), board.currentPlayer().getLeague(), null);

        for (final Piece piece : board.currentPlayer().getActivePieces()) {
            if (!pawnPromotion.getPromotedPawn().equals(piece)) {
                builder.setPiece(piece);
            }
        }

        for (final Piece piece : board.currentPlayer().getOpponent().getActivePieces()) {
            builder.setPiece(piece);
        }

        builder.setPiece(pawnPromotion.getPromotedPiece().movedPiece(pawnPromotion));
        return builder.build();
    }


    private int[] pawnPromotionInterface(final List<Piece> getPromotionPieces) {
        final int[] icons = new int[4];
        for (int i = 0; i < 4; i++) {
            icons[i] = BoardUtils.getPieceImage(getPromotionPieces.get(i));
        }
        return icons;
    }

    private void getImageViewOfPromotePiece(final Move.PawnPromotion pawnPromotion, final Piece promotedPiece, final Dialog dialog, final int iconsResource, final int resource) {
        final ImageView pieceImageView = dialog.findViewById(resource);
        pieceImageView.setImageResource(iconsResource);
        pieceImageView.setOnClickListener(V->{
            pawnPromotion.setPromotedPiece(promotedPiece);
            this.mainActivity.updateBoard(promotePawn(pawnPromotion, this.mainActivity.getChessBoard()));
            this.mainActivity.updateUI(pawnPromotion);
            this.mainActivity.drawBoard();
            this.mainActivity.highlightHumanMove();
            this.mainActivity.firePropertyChange();
            dialog.cancel();
        });
        pieceImageView.setEnabled(true);
    }

    public void startPromotion(final Move.PawnPromotion pawnPromotion) {
        final List<Piece> getPromotionPieces = pawnPromotion.getPromotedPawn().getPromotionPieces(pawnPromotion.getDestinationCoordinate());
        final int[] iconsResource = pawnPromotionInterface(getPromotionPieces);

        final Dialog dialog = new Dialog(mainActivity);
        dialog.setContentView(R.layout.pawn_promotion);
        dialog.setCancelable(false);

        getImageViewOfPromotePiece(pawnPromotion, getPromotionPieces.get(0), dialog, iconsResource[0], R.id.queen);
        getImageViewOfPromotePiece(pawnPromotion, getPromotionPieces.get(1), dialog, iconsResource[1], R.id.rook);
        getImageViewOfPromotePiece(pawnPromotion, getPromotionPieces.get(2), dialog, iconsResource[2], R.id.bishop);
        getImageViewOfPromotePiece(pawnPromotion, getPromotionPieces.get(3), dialog, iconsResource[3], R.id.knight);

        dialog.show();
        Toast.makeText(this.mainActivity, "You MUST promote your pawn!", Toast.LENGTH_LONG).show();
    }
}