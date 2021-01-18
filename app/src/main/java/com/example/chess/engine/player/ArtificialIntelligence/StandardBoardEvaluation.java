package com.example.chess.engine.player.ArtificialIntelligence;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.chess.engine.board.Board;
import com.example.chess.engine.pieces.Piece;
import com.example.chess.engine.player.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;

public final class StandardBoardEvaluation {

    private static final int CHECK_KING = 50;
    private static final int CHECK_MATE = 10000;
    private static final int DEPTH_BONUS = 100;
    private static final int CASTLE_BONUS = 60;

    private static final int[] kingEvaluation = {
            -30,-40,-40,-50,-50,-40,-40,-30,
            -30,-40,-40,-50,-50,-40,-40,-30,
            -30,-40,-40,-50,-50,-40,-40,-30,
            -30,-40,-40,-50,-50,-40,-40,-30,
            -20,-30,-30,-40,-40,-30,-30,-20,
            -10,-20,-20,-20,-20,-20,-20,-10,
            20, 20,  0,  0,  0,  0, 20, 20,
            20, 30, 10,  0,  0, 10, 30, 20
    };

    private static final int[] queenEvaluation = {
            -20,-10,-10, -5, -5,-10,-10,-20,
            -10,  0,  0,  0,  0,  0,  0,-10,
            -10,  0,  5,  5,  5,  5,  0,-10,
            -5,  0,  5,  5,  5,  5,  0, -5,
            0,  0,  5,  5,  5,  5,  0, -5,
            -10,  5,  5,  5,  5,  5,  0,-10,
            -10,  0,  5,  0,  0,  0,  0,-10,
            -20,-10,-10, -5, -5,-10,-10,-20
    };

    private static final int[] rookEvaluation = {
            0,  0,  0,  0,  0,  0,  0,  0,
            5, 10, 10, 10, 10, 10, 10,  5,
            -5,  0,  0,  0,  0,  0,  0, -5,
            -5,  0,  0,  0,  0,  0,  0, -5,
            -5,  0,  0,  0,  0,  0,  0, -5,
            -5,  0,  0,  0,  0,  0,  0, -5,
            -5,  0,  0,  0,  0,  0,  0, -5,
            0,  0,  0,  5,  5,  0,  0,  0
    };

    private static final int[] bishopEvaluation = {
            -20,-10,-10,-10,-10,-10,-10,-20,
            -10,  0,  0,  0,  0,  0,  0,-10,
            -10,  0,  5, 10, 10,  5,  0,-10,
            -10,  5,  5, 10, 10,  5,  5,-10,
            -10,  0, 10, 10, 10, 10,  0,-10,
            -10, 10, 10, 10, 10, 10, 10,-10,
            -10,  5,  0,  0,  0,  0,  5,-10,
            -20,-10,-10,-10,-10,-10,-10,-20
    };

    private static final int[] knightEvaluation = {
            -50,-40,-30,-30,-30,-30,-40,-50,
            -40,-20,  0,  0,  0,  0,-20,-40,
            -30,  0, 10, 15, 15, 10,  0,-30,
            -30,  5, 15, 20, 20, 15,  5,-30,
            -30,  0, 15, 20, 20, 15,  0,-30,
            -30,  5, 10, 15, 15, 10,  5,-30,
            -40,-20,  0,  5,  5,  0,-20,-40,
            -50,-40,-30,-30,-30,-30,-40,-50
    };

    private static final int[] pawnEvaluation = {
            0,  0,  0,  0,  0,  0,  0,  0,
            50, 50, 50, 50, 50, 50, 50, 50,
            10, 10, 20, 30, 30, 20, 10, 10,
            5,  5, 10, 25, 25, 10,  5,  5,
            0,  0,  0, 20, 20,  0,  0,  0,
            5, -5,-10,  0,  0,-10, -5,  5,
            5, 10, 10,-20,-20, 10, 10,  5,
            0,  0,  0,  0,  0,  0,  0,  0
    };

    @RequiresApi(api = Build.VERSION_CODES.N)
    public int evaluate(final Board board, final int depth) {
        return - scorePlayer(board.blackPlayer(), depth) + scorePlayer(board.whitePlayer(), depth);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private static int scorePlayer(final Player player, final int depth) {
        return pieceValue(player) + mobility(player) +
                check(player) + checkMate(player, depth) +
                castled(player) + positionEvaluation(player);
    }

    private static int castled(final Player player) {
        return player.isCastled() ? CASTLE_BONUS : 0;
    }

    private static int checkMate(final Player player, final int depth) { return player.getOpponent().isInCheckmate() ? CHECK_MATE * depthBonus(depth) : 0; }

    private static int depthBonus(final int depth) {
        return depth == 0 ? 1 : DEPTH_BONUS * depth;
    }

    private static int check(final Player player) {
        return player.getOpponent().isInCheck() ? CHECK_KING : 0;
    }

    private static int mobility(final Player player) {
        return player.getLegalMoves().size();
    }

    private static int pieceValue(final Player player) {
        int pieceValueScore = 0;
        for (final Piece piece : player.getActivePieces()) {
            pieceValueScore += piece.getPieceValue();
        }
        return pieceValueScore;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private static int positionEvaluation(final Player player) {
        int piecePositionScore = 0;
        for (final Piece piece : player.getActivePieces()) {
            final int position = piece.getPiecePosition();
            piecePositionScore += positionValue(piece).get(position);
        }
        return piecePositionScore;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private static List<Integer> positionValue(final Piece piece) {
        final boolean isWhite = piece.getLeague().isWhite();

        if ("K".equals(piece.toString())) { return getPiecePositionValue(isWhite, kingEvaluation); }

        else if ("Q".equals(piece.toString())) { return getPiecePositionValue(isWhite, queenEvaluation); }

        else if ("R".equals(piece.toString())) { return getPiecePositionValue(isWhite, rookEvaluation); }

        else if ("B".equals(piece.toString())) { return getPiecePositionValue(isWhite, bishopEvaluation); }

        else if ("N".equals(piece.toString())) { return getPiecePositionValue(isWhite, knightEvaluation); }

        else { return getPiecePositionValue(isWhite, pawnEvaluation); }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private static List<Integer> getPiecePositionValue(final boolean isWhite, final int[] positionValue) {
        if (isWhite) {
            return Collections.unmodifiableList(Arrays.stream(positionValue).boxed().collect(Collectors.toList()));
        }
        return reversePositionEvaluation(positionValue);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private static List<Integer> reversePositionEvaluation(final int[] positionValue) {
        final List<Integer> piecePositionValue = Arrays.stream(positionValue).boxed().collect(Collectors.toList());
        Collections.reverse(piecePositionValue);
        return Collections.unmodifiableList(piecePositionValue);
    }
}