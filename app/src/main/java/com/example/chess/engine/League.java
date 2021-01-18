package com.example.chess.engine;

import com.example.chess.engine.board.BoardUtils;
import com.example.chess.engine.player.BlackPlayer;
import com.example.chess.engine.player.Player;
import com.example.chess.engine.player.WhitePlayer;

public enum League {
    WHITE {
        @Override
        public int getDirection() {
            return -1;
        }
        @Override
        public boolean isWhite() {
            return true;
        }
        @Override
        public boolean isBlack() {
            return false;
        }
        @Override
        public int getOppositeDirection () {return 1;}
        @Override
        public Player choosePlayer(final WhitePlayer whitePlayer, final BlackPlayer blackPlayer) { return whitePlayer; }
        @Override
        public boolean isPawnPromotionSquare(final int position) { return BoardUtils.FIRST_ROW[position]; }
    },
    BLACK {
        @Override
        public int getDirection() {
            return 1;
        }
        @Override
        public boolean isWhite() {
            return false;
        }
        @Override
        public boolean isBlack() {
            return true;
        }
        @Override
        public int getOppositeDirection () {return -1;}
        @Override
        public Player choosePlayer(final WhitePlayer whitePlayer, final BlackPlayer blackPlayer) { return blackPlayer; }
        @Override
        public boolean isPawnPromotionSquare(final int position) { return BoardUtils.EIGHTH_ROW[position]; }
    };

    public abstract int getDirection();
    public abstract int getOppositeDirection();
    public abstract boolean isWhite();
    public abstract boolean isBlack();
    public abstract boolean isPawnPromotionSquare(final int position);

    public abstract Player choosePlayer(final WhitePlayer whitePlayer, final BlackPlayer blackPlayer);
}