package com.example.chess.engine.board;

import androidx.annotation.NonNull;

import com.example.chess.engine.pieces.Piece;

import java.io.Serializable;

public abstract class Tile implements Serializable {

    private final static long serialVersionUID = 6L;

    protected final int tileCoordinate;

    public static Tile createTile(final int tileCoordinate, final Piece piece) { return piece != null ? new OccupiedTile(tileCoordinate, piece) : new EmptyTile(tileCoordinate); }

    private Tile(final int tileCoordinate) {
        this.tileCoordinate = tileCoordinate;
    }

    public abstract boolean isTileOccupied();

    public abstract Piece getPiece();

    public final int getTileCoordinate() { return this.tileCoordinate; }

    public static final class EmptyTile extends Tile {
        private EmptyTile(final int coordinate) {
            super(coordinate);
        }

        @Override
        public String toString() {
            return "-";
        }

        @Override
        public boolean isTileOccupied() {
            return false;
        }

        @Override
        public Piece getPiece() {
            return null;
        }
    }

    public static final class OccupiedTile extends Tile {
        private final Piece pieceOnTile;

        private OccupiedTile(final int tileCoordinate, final Piece pieceOnTile) {
            super(tileCoordinate);
            this.pieceOnTile = pieceOnTile;
        }

        @Override
        @NonNull
        public String toString() { return getPiece().getLeague().isBlack() ? getPiece().toString().toLowerCase() : getPiece().toString(); }

        @Override
        public boolean isTileOccupied() {
            return true;
        }

        @Override
        public Piece getPiece() {
            return this.pieceOnTile;
        }
    }
}