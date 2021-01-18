package com.example.chess.engine.pieces;

import com.example.chess.engine.League;
import com.example.chess.engine.board.Board;
import com.example.chess.engine.board.Move;
import com.example.chess.engine.player.MoveTransition;

import java.util.Collection;
import java.util.Objects;

import static com.example.chess.engine.board.Move.*;

public abstract class Piece {

    protected final PieceType pieceType;
    protected final int piecePosition;
    protected final League league;
    private final boolean isFirstMove;

    public Piece(final PieceType pieceType, final int piecePosition, final League league, final boolean isFirstMove) {
        this.pieceType = pieceType;
        this.piecePosition = piecePosition;
        this.league = league;
        this.isFirstMove = isFirstMove;
    }

    //prior to JDK 7, a manual hashCode is needed
    @Override
    public int hashCode() { return Objects.hash(pieceType.hashCode(), piecePosition, league.hashCode(), isFirstMove); }

    @Override
    public boolean equals(final Object object) {

        if (this == object) { return true; }

        if (!(object instanceof Piece)) { return false; }

        final Piece otherPiece = (Piece)object;
        return piecePosition == otherPiece.getPiecePosition() && pieceType == otherPiece.getPieceType() &&
                league == otherPiece.getLeague() && isFirstMove == otherPiece.isFirstMove();
    }

    public boolean isFirstMove() {
        return this.isFirstMove;
    }

    public abstract Collection<Move> calculateLegalMoves(final Board board);

    protected boolean isLegalMove(final Board board, final int candidateDestinationCoordinate) {
        try {
            //make a move, if the move is safe, return true, else false
            final MoveTransition moveTransition = board.currentPlayer().makeMove(new MajorMove(board, this, candidateDestinationCoordinate));
            return moveTransition.getMoveStatus().isDone();
        } catch (final RuntimeException e) {
            //for catching null board at the beginning of the game
            return true;
        }
    }

    public abstract Piece movedPiece(final Move move);

    public League getLeague() {
        return this.league;
    }

    public int getPiecePosition() {
        return this.piecePosition;
    }

    public PieceType getPieceType() {
        return this.pieceType;
    }

    public int getPieceValue() {
        return this.pieceType.getPieceValue();
    }
}