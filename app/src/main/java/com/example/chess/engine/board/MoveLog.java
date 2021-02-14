package com.example.chess.engine.board;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MoveLog implements Serializable {

    private final static long serialVersionUID = 3L;

    private final List<Move> moves;
    private final List<Integer> movesNumber;

    public MoveLog() {
        this.moves = new ArrayList<>();
        this.movesNumber = new ArrayList<>();
    }

    public List<Move> getMoves() { return Collections.unmodifiableList(this.moves); }

    public List<Integer> getMovesNumber() { return Collections.unmodifiableList(this.movesNumber); }

    public void addMove(final Move move) {
        this.moves.add(move);
        this.movesNumber.add(this.moves.size());
    }

    public int size() { return this.moves.size(); }

    public void clear() {
        this.moves.clear();
        this.movesNumber.clear();
    }

    public Move removeMove() {
        final int index = this.moves.size() - 1;
        this.movesNumber.remove(index);
        return this.moves.remove(this.moves.size() - 1);
    }

    @Override
    public String toString() { return this.moves.toString(); }

}