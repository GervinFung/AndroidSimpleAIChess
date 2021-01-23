package com.example.chess.engine.pieces;

import com.example.chess.engine.League;
import com.example.chess.engine.board.Board;
import com.example.chess.engine.board.BoardUtils;
import com.example.chess.engine.board.Move;
import com.example.chess.engine.board.Tile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.example.chess.engine.board.Move.*;

public final class Queen extends Piece{

    private static final int[] MOVE_VECTOR_COORDINATE = {-9, -8, -7, -1, 1, 7, 8, 9};

    public Queen(final League league, final int piecePosition) {
        super(PieceType.QUEEN, piecePosition, league, true);
    }

    public Queen(final League league, final int piecePosition, final boolean isFirstMove) { super(PieceType.QUEEN, piecePosition, league, isFirstMove); }

    @Override
    public Collection<Move> calculateLegalMoves(final Board board) {
        final List<Move> legalMoves = new ArrayList<>();

        for (final int CoordinateOFFSET : MOVE_VECTOR_COORDINATE) {

            int candidateDestinationCoordinate = this.piecePosition;

            while (BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)) {

                if (isEighthColumnExclusion(candidateDestinationCoordinate, CoordinateOFFSET) ||
                        isFirstColumnExclusion(candidateDestinationCoordinate, CoordinateOFFSET)) {
                    break;
                }

                candidateDestinationCoordinate += CoordinateOFFSET;

                if (BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)) {
                    final Tile candidateDestinationTile = board.getTile(candidateDestinationCoordinate);

                    if (!candidateDestinationTile.isTileOccupied() && this.isLegalMove(board, candidateDestinationCoordinate)) {
                        legalMoves.add(new MajorMove(board, this, candidateDestinationCoordinate));

                    } else if (candidateDestinationTile.isTileOccupied()) {
                        final Piece pieceDestination = candidateDestinationTile.getPiece();
                        final League league = pieceDestination.getLeague();

                        if (this.getLeague() != league && this.isLegalMove(board, candidateDestinationCoordinate)) {
                            legalMoves.add(new MajorAttackMove(board, this, candidateDestinationCoordinate, pieceDestination));
                        }
                        break;
                    }
                }
            }
        }

        return Collections.unmodifiableList(legalMoves);
    }

    @Override
    public Queen movedPiece(Move move) { return new Queen(move.getMovedPiece().getLeague(), move.getDestinationCoordinate(), false); }

    @Override
    public String toString() {
        return PieceType.QUEEN.toString();
    }

    private static boolean isFirstColumnExclusion(final int currentPosition, final int candidateOFFSET) {
        return BoardUtils.FIRST_COLUMN.get(currentPosition) && (candidateOFFSET == -9 || candidateOFFSET == 7 || candidateOFFSET == -1);
    }

    private static boolean isEighthColumnExclusion(final int currentPosition, final int candidateOFFSET) {
        return BoardUtils.EIGHTH_COLUMN.get(currentPosition) && (candidateOFFSET == 9 || candidateOFFSET == -7 || candidateOFFSET == 1);
    }
}