package com.example.chess.engine.player;

public enum MoveStatus {

    DONE {
        @Override
        public boolean isDone() {
            return true;
        }
    },
    Illegal_Move {
        @Override
        public boolean isDone() {
            return false;
        }
    },
    LEAVES_PLAYER_IN_CHECK {
        @Override
        public boolean isDone() {
            return false;
        }
    };

    public abstract boolean isDone();
}