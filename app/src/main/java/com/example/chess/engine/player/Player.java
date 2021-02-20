package com.example.chess.engine.player;

import com.example.chess.engine.League;
import com.example.chess.engine.board.Board;
import com.example.chess.engine.board.Move;
import com.example.chess.engine.pieces.King;
import com.example.chess.engine.pieces.Piece;
import com.example.chess.engine.board.MoveTransition;
import com.example.chess.engine.board.MoveStatus;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public abstract class Player implements Serializable {

    private final static long serialVersionUID = 5L;

    protected final Board board;
    protected final King playerKing;
    protected final Collection<Move> legalMoves;
    private final boolean isInCheck;

    public Player(final Board board, final Collection<Move> legalMoves, final Collection<Move> opponentLegalMoves) {
        this.board = board;
        this.playerKing = establishKing();
        final List<Move> legal = new ArrayList<>(legalMoves);
        this.isInCheck = !Player.calculateAttacksOnTile(this.playerKing.getPiecePosition(), opponentLegalMoves).isEmpty();
        //for ai
        legal.addAll(calculateKingCastles(opponentLegalMoves));
        this.legalMoves = legal;
    }

    public final King getPlayerKing() {
        return this.playerKing;
    }

    public final Collection<Move> getLegalMoves() { return Collections.unmodifiableCollection(this.legalMoves); }

    public static List<Move> calculateAttacksOnTile(final int piecePosition, final Collection<Move> moves) {
        final List<Move> attackMove = new ArrayList<>();

        for (final Move move : moves) {
            if(piecePosition == move.getDestinationCoordinate()) {
                attackMove.add(move);
            }
        }
        return Collections.unmodifiableList(attackMove);
    }

    private King establishKing() {
        for (final Piece piece : getActivePieces()) {
            if (piece.getPieceType().isKing()) {
                return (King) piece;
            }
        }
        throw new RuntimeException("Invalid board");
    }

    public abstract Collection<Piece> getActivePieces();

    public abstract League getLeague();

    public abstract Player getOpponent();

    public final boolean isInCheck() {
        return this.isInCheck;
    }

    public final boolean isInCheckmate() {
        return this.isInCheck && noEscapeMoves();
    }

    public final boolean isInStalemate() {
        return !this.isInCheck && noEscapeMoves();
    }

    protected abstract Collection<Move> calculateKingCastles(final Collection<Move> opponentLegals);

    public final boolean isCastled() {
        return this.playerKing.isCastled();
    }


    protected final boolean noEscapeMoves() {

        for (final Move move : this.legalMoves) {
            final MoveTransition transition = makeMove(move);

            if (transition.getMoveStatus().isDone()) {
                return false;
            }
        }
        return true;
    }

    public final MoveTransition makeMove(final Move move) {

        final Board transitionBoard = move.execute();
        if (transitionBoard != null) {
            final Collection<Move> currentPlayerLegals = transitionBoard.currentPlayer().getLegalMoves();
            final List<Move> kingAttacks = Player.calculateAttacksOnTile(transitionBoard.currentPlayer().getOpponent().getPlayerKing().getPiecePosition(), currentPlayerLegals);

            if (!kingAttacks.isEmpty()) {
                return new MoveTransition(this.board, this.board, MoveStatus.LEAVES_PLAYER_IN_CHECK);
            }

            return new MoveTransition(transitionBoard, this.board, MoveStatus.DONE);
        }
        return new MoveTransition(null, null, MoveStatus.Illegal_Move);
    }

    public final MoveTransition undoMove(final Move move) { return new MoveTransition(this.board, move.getBoard(), MoveStatus.DONE); }
}