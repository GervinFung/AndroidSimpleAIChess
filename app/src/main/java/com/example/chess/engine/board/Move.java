package com.example.chess.engine.board;

import androidx.annotation.NonNull;

import com.example.chess.engine.pieces.Pawn;
import com.example.chess.engine.pieces.Piece;
import com.example.chess.engine.pieces.Rook;

import java.io.Serializable;

import static com.example.chess.engine.board.Board.*;

public abstract class Move implements Serializable {

    private final static long serialVersionUID = 2L;

    protected final Board board;
    protected final Piece movePiece;
    protected final int destinationCoordinate;
    protected final boolean isFirstMove;

    private Move(final Board board, final Piece movePiece, final int destinationCoordinate) {
        this.board = board;
        this.movePiece = movePiece;
        this.destinationCoordinate = destinationCoordinate;
        this.isFirstMove = movePiece.isFirstMove();
    }

    private Move(final Board board, final int destinationCoordinate) {
        this.board = board;
        this.destinationCoordinate = destinationCoordinate;
        this.movePiece = null;
        this.isFirstMove = false;
    }
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + this.destinationCoordinate;
        result = 31 * result + this.movePiece.hashCode();
        result = 31 * result + this.movePiece.getPiecePosition();
        result = result + (isFirstMove ? 1 : 0);
        return result;
    }

    @Override
    public boolean equals(final Object object) {

        if (this == object) { return true; }

        if (!(object instanceof Move)) { return false; }

        final Move otherMove = (Move) object;
        return getCurrentCoordinate() == otherMove.getCurrentCoordinate() &&
                getDestinationCoordinate() == otherMove.getDestinationCoordinate() &&
                getMovedPiece().equals(otherMove.getMovedPiece());
    }


    public final Board getBoard() {
        return this.board;
    }

    public int getCurrentCoordinate() {
        return this.getMovedPiece().getPiecePosition();
    }

    public final int getDestinationCoordinate() {
        return this.destinationCoordinate;
    }

    public final Piece getMovedPiece() {
        return this.movePiece;
    }

    public boolean isAttack() {
        return false;
    }

    public boolean isCastlingMove() { return false; }

    public Piece getAttackedPiece() {
        return null;
    }


    public Board execute() {

        final Builder builder = new Builder(this.board.getMoveCount() + 1, this.board.currentPlayer().getOpponent().getLeague(), null);

        for (final Piece piece : this.board.currentPlayer().getActivePieces()) {
            if (!this.movePiece.equals(piece)) {
                builder.setPiece(piece);
            }
        }

        for (final Piece piece : this.board.currentPlayer().getOpponent().getActivePieces()) {
            builder.setPiece(piece);
        }

        builder.setPiece(this.movePiece.movedPiece(this));
        builder.setTransitionMove(this);

        return builder.build();
    }

    public static final class MajorMove extends Move {

        public MajorMove(final Board board, final Piece movePiece, final int destinationCoordinate) {
            super(board, movePiece, destinationCoordinate);
        }

        @Override
        public boolean equals(final Object object) { return this == object || object instanceof  MajorMove && super.equals(object); }

        @Override
        public String toString() { return getMovedPiece().getPieceType().toString() + BoardUtils.getPositionAtCoordinate(this.destinationCoordinate); }
    }

    public static class AttackMove extends Move {

        private final Piece attackedPiece;

        public AttackMove(final Board board, final Piece movePiece, final int destinationCoordinate, final Piece attackedPiece) {
            super(board, movePiece, destinationCoordinate);
            this.attackedPiece = attackedPiece;
        }

        @Override
        public int hashCode() { return this.attackedPiece.hashCode() + super.hashCode(); }

        @Override
        public boolean equals(final Object object) {
            if (this == object) {
                return true;
            }

            if (!(object instanceof AttackMove)) {
                return false;
            }

            final AttackMove otherAttackMove = (AttackMove)object;
            return super.equals(otherAttackMove) && getAttackedPiece().equals(otherAttackMove.getAttackedPiece());
        }

        @Override
        public boolean isAttack() {
            return true;
        }

        @Override
        public Piece getAttackedPiece() {
            return this.attackedPiece;
        }
    }

    public static final class MajorAttackMove extends AttackMove {
        public MajorAttackMove(final Board board, final Piece piece, final int destinationCoordinate, final Piece pieceAttacked) {
            super(board, piece, destinationCoordinate, pieceAttacked);
        }

        @Override
        public boolean equals(final Object object) { return this == object || object instanceof MajorAttackMove && super.equals(object); }

        @Override
        public String toString() { return getMovedPiece().getPieceType() + BoardUtils.getPositionAtCoordinate(this.destinationCoordinate) + "x" + this.getAttackedPiece(); }
    }

    public static final class PawnMove extends Move {

        public PawnMove(final Board board, final Piece movePiece, final int destinationCoordinate) {
            super(board, movePiece, destinationCoordinate);
        }

        @Override
        public boolean equals(final Object object) { return this == object || object instanceof  PawnMove && super.equals(object); }

        @Override
        @NonNull
        public String toString() { return BoardUtils.getPositionAtCoordinate(this.destinationCoordinate); }
    }

    public static class PawnAttackMove extends AttackMove {

        public PawnAttackMove(final Board board, final Piece movePiece, final int destinationCoordinate, final Piece attackedPiece) {
            super(board, movePiece, destinationCoordinate, attackedPiece);
        }

        @Override
        public boolean equals(final Object object) { return this == object || object instanceof  PawnAttackMove && super.equals(object); }

        @Override
        @NonNull
        public String toString() { return BoardUtils.getPositionAtCoordinate(this.movePiece.getPiecePosition()).charAt(0) + "x" + BoardUtils.getPositionAtCoordinate(this.destinationCoordinate); }
    }

    public static final class PawnEnPassantAttackMove extends PawnAttackMove {

        public PawnEnPassantAttackMove(final Board board, final Piece movePiece, final int destinationCoordinate, final Piece attackedPiece) {
            super(board, movePiece, destinationCoordinate, attackedPiece);
        }

        @Override
        public boolean equals(final Object object) { return this == object || object instanceof PawnEnPassantAttackMove && super.equals(object); }

        @Override
        public Board execute() {
            final Builder builder = new Builder(this.board.getMoveCount() + 1, this.board.currentPlayer().getOpponent().getLeague(), null);

            for (final Piece piece : this.board.currentPlayer().getActivePieces()) {
                if (!this.movePiece.equals(piece)) {
                    builder.setPiece(piece);
                }
            }

            for (final Piece piece : this.board.currentPlayer().getOpponent().getActivePieces()) {
                if (!piece.equals(this.getAttackedPiece())) {
                    builder.setPiece(piece);
                }
            }

            builder.setPiece(this.movePiece.movedPiece(this));
            builder.setTransitionMove(this);

            return builder.build();
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString();
        }
    }

    public static final class PawnPromotion extends Move {

        private final Move decoratedMove;
        private final Pawn promotedPawn;
        private Piece promotedPiece;
        private final Piece MinimaxPromotionPiece;

        public PawnPromotion(final Move decoratedMove, final Piece MinimaxPromotionPiece) {
            super(decoratedMove.getBoard(), decoratedMove.getMovedPiece(), decoratedMove.getDestinationCoordinate());
            this.decoratedMove = decoratedMove;
            this.promotedPawn = (Pawn)decoratedMove.getMovedPiece();
            this.MinimaxPromotionPiece = MinimaxPromotionPiece;
        }

        public void setPromotedPiece(final Piece promotedPiece) { this.promotedPiece = promotedPiece; }

        public Pawn getPromotedPawn() { return this.promotedPawn; }
        public Piece getPromotedPiece() { return this.promotedPiece; }
        public Move getDecoratedMove() { return this.decoratedMove; }

        @Override
        public Board execute() {

            final Board pawnMoveBoard = this.decoratedMove.execute();
            final Builder builder = new Builder(this.board.getMoveCount() + 1, pawnMoveBoard.currentPlayer().getLeague(), null);

            for (final Piece piece : this.board.currentPlayer().getActivePieces()) {
                if (!this.promotedPawn.equals(piece)) {
                    builder.setPiece(piece);
                }
            }

            for (final Piece piece : this.board.currentPlayer().getOpponent().getActivePieces()) {
                builder.setPiece(piece);
            }

            this.promotedPiece = this.MinimaxPromotionPiece;
            builder.setPiece(this.MinimaxPromotionPiece.movedPiece(this));
            builder.setTransitionMove(this);

            return builder.build();
        }

        @Override
        public boolean isAttack() {
            return this.decoratedMove.isAttack();
        }

        @Override
        public Piece getAttackedPiece() {
            return this.decoratedMove.getAttackedPiece();
        }

        @Override
        public String toString() { return BoardUtils.getPositionAtCoordinate(destinationCoordinate) + "=" +this.promotedPiece.toString().charAt(0); }

        @Override
        public int hashCode() { return this.decoratedMove.hashCode() + (31 * this.promotedPawn.hashCode()); }

        @Override
        public boolean equals(final Object object) { return this == object || object instanceof PawnPromotion && (super.equals(object)); }
    }

    public static final class PawnJump extends Move {

        public PawnJump(final Board board, final Piece movePiece, final int destinationCoordinate) {
            super(board, movePiece, destinationCoordinate);
        }

        @Override
        public Board execute() {
            final Pawn movedPawn = (Pawn)this.movePiece.movedPiece(this);
            final Builder builder = new Builder(this.board.getMoveCount() + 1, this.board.currentPlayer().getOpponent().getLeague(), movedPawn);

            for (final Piece piece : this.board.currentPlayer().getActivePieces()) {
                if (!this.movePiece.equals(piece)) {
                    builder.setPiece(piece);
                }
            }

            for (final Piece piece : this.board.currentPlayer().getOpponent().getActivePieces()) {
                builder.setPiece(piece);
            }

            builder.setPiece(movedPawn);
            builder.setTransitionMove(this);

            return builder.build();
        }

        @Override
        @NonNull
        public String toString() { return BoardUtils.getPositionAtCoordinate(destinationCoordinate); }
    }

    private static abstract class CastleMove extends Move {

        protected final Rook castleRook;

        protected final int castleRookStart;

        protected final int castleRookDestination;

        public CastleMove(final Board board, final Piece movePiece, final int destinationCoordinate,
                          final Rook castleRook, final int castleRookStart, final int castleRookDestination) {
            super(board, movePiece, destinationCoordinate);
            this.castleRook = castleRook;
            this.castleRookStart = castleRookStart;
            this.castleRookDestination = castleRookDestination;
        }

        public Rook getCastleRook() {
            return this.castleRook;
        }

        @Override
        public boolean isCastlingMove() {
            return true;
        }

        @Override
        public Board execute() {

            final Builder builder = new Builder(this.board.getMoveCount() + 1, this.board.currentPlayer().getOpponent().getLeague(), null);

            for (final Piece piece : this.board.getAllPieces()) {
                if (!this.movePiece.equals(piece) && !this.castleRook.equals(piece)) {
                    builder.setPiece(piece);
                }
            }
            builder.setPiece(this.movePiece.movedPiece(this));
            builder.setPiece(new Rook(this.castleRook.getLeague(), this.castleRookDestination, false));
            builder.setTransitionMove(this);

            return builder.build();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + this.castleRook.hashCode();
            result = prime * result + this.castleRookDestination;
            return result;
        }

        @Override
        public boolean equals(final Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof CastleMove)) {
                return false;
            }
            final CastleMove castleMove = (CastleMove)object;
            return super.equals(castleMove) && this.castleRook.equals(castleMove.getCastleRook());
        }
    }

    public static final class KingSideCastleMove extends CastleMove {

        public KingSideCastleMove(final Board board, final Piece movePiece, final int destinationCoordinate,
                                  final Rook castleRook, final int castleRookStart, final int castleRookDestination) {
            super(board, movePiece, destinationCoordinate, castleRook, castleRookStart, castleRookDestination);
        }

        @Override
        public boolean equals(final Object object) { return this == object || object instanceof KingSideCastleMove && super.equals(object); }

        @Override
        public String toString() {
            return "O-O";
        }
    }

    public static final class QueenSideCastleMove extends CastleMove {

        public QueenSideCastleMove(final Board board, final Piece movePiece, final int destinationCoordinate,
                                   final Rook castleRook, final int castleRookStart, final int castleRookDestination) {
            super(board, movePiece, destinationCoordinate, castleRook, castleRookStart, castleRookDestination);
        }

        @Override
        public boolean equals(final Object object) { return this == object || object instanceof QueenSideCastleMove && super.equals(object); }

        @Override
        public String toString() {
            return "O-O-O";
        }
    }

    public static final class NullMove extends Move {
        public NullMove() {
            super(null, 65);
        }

        @Override
        public Board execute() {
            return null;
        }

        @Override
        public int getCurrentCoordinate() {
            return -1;
        }
    }

    public static final class MoveFactory {

        private static final Move NULL_MOVE = new NullMove();

        private MoveFactory() {
            throw new RuntimeException ("Not instantiatable");
        }

        public static Move getNullMove() { return NULL_MOVE; }

        public static Move createMove(final Board board, final Piece piece, final int currentCoordinate, final int destinationCoordinate) {
            for (final Move move : piece.calculateLegalMoves(board)) {
                if (move.getCurrentCoordinate() == currentCoordinate && move.getDestinationCoordinate() == destinationCoordinate) {
                    return move;
                }
            }
            return NULL_MOVE;
        }
    }
}