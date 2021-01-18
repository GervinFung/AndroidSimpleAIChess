package com.example.chess.engine.board;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.chess.R;
import com.example.chess.engine.pieces.Piece;

import java.util.List;

public final class BoardUtils {

    public static final boolean[] FIRST_COLUMN = initColumn(0);
    public static final boolean[] SECOND_COLUMN = initColumn(1);
    public static final boolean[] SEVENTH_COLUMN = initColumn(6);
    public static final boolean[] EIGHTH_COLUMN = initColumn(7);

    public static final boolean[] FIRST_ROW = initRow(0);
    public static final boolean[] SECOND_ROW = initRow(8);
    public static final boolean[] THIRD_ROW = initRow(16);
    public static final boolean[] FIFTH_ROW = initRow(32);
    public static final boolean[] SEVENTH_ROW = initRow(48);
    public static final boolean[] EIGHTH_ROW = initRow(56);

    @RequiresApi(api = Build.VERSION_CODES.R)
    public static final List<String> ALGEBRAIC_NOTATION = initializeAlgebraicNotation();

    @RequiresApi(api = Build.VERSION_CODES.R)
    private static List<String> initializeAlgebraicNotation() {
        return List.of(
                "a8", "b8", "c8", "d8", "e8", "f8", "g8", "h8",
                "a7", "b7", "c7", "d7", "e7", "f7", "g7", "h7",
                "a6", "b6", "c6", "d6", "e6", "f6", "g6", "h6",
                "a5", "b5", "c5", "d5", "e5", "f5", "g5", "h5",
                "a4", "b4", "c4", "d4", "e4", "f4", "g4", "h4",
                "a3", "b3", "c3", "d3", "e3", "f3", "g3", "h3",
                "a2", "b2", "c2", "d2", "e2", "f2", "g2", "h2",
                "a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1");
    }

    public static int getPieceImage(final Piece piece) {
        if (piece.getLeague().isWhite()) {
            if ("K".equals(piece.toString())) { return R.mipmap.wk; }

            else if ("Q".equals(piece.toString())) { return R.mipmap.wq; }

            else if ("R".equals(piece.toString())) { return R.mipmap.wr; }

            else if ("B".equals(piece.toString())) { return R.mipmap.wb; }

            else if ("N".equals(piece.toString())) { return R.mipmap.wn; }

            else { return R.mipmap.wp; }

        } else {
            if ("K".equals(piece.toString())) { return R.mipmap.bk; }

            else if ("Q".equals(piece.toString())) { return R.mipmap.bq; }

            else if ("R".equals(piece.toString())) { return R.mipmap.br; }

            else if ("B".equals(piece.toString())) { return R.mipmap.bb; }

            else if ("N".equals(piece.toString())) { return R.mipmap.bn; }

            else { return R.mipmap.bp; }
        }
    }

    public static final int NUM_TILES = 64;
    public static final int NUM_TILES_PER_ROW = 8;

    private static boolean[] initColumn(int columnNumber) {
        final boolean[] column = new boolean[NUM_TILES];
        do {
            column[columnNumber] = true;
            columnNumber += NUM_TILES_PER_ROW;
        } while (columnNumber < NUM_TILES);
        return column;
    }

    private static boolean[] initRow(int rowNumber) {
        final boolean[] row = new boolean[NUM_TILES];
        do {
            row[rowNumber] = true;
            rowNumber ++;
        } while (rowNumber % NUM_TILES_PER_ROW != 0);
        return row;
    }

    private BoardUtils() {
        throw new RuntimeException("You cannot instantiate me");
    }

    public static boolean isValidTileCoordinate(final int coordinate) { return coordinate >= 0 && coordinate < NUM_TILES; }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public static String getPositionAtCoordinate(final int destinationCoordinate) { return ALGEBRAIC_NOTATION.get(destinationCoordinate); }
}