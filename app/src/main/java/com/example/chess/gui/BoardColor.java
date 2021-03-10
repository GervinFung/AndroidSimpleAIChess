package com.example.chess.gui;

import android.graphics.Color;

public enum BoardColor {
    CLASSIC {
        @Override
        public int DARK_TILE() { return Color.rgb(181, 136, 99); }
        @Override
        public int LIGHT_TILE() { return Color.rgb(240, 217, 181); }
        @Override
        public int HIGHLIGHT_LEGAL_MOVE_DARK_TILE() { return Color.rgb(105, 105, 105); }
        @Override
        public int HIGHLIGHT_LEGAL_MOVE_LIGHT_TILE() { return Color.rgb(169, 169, 169); }
    },

    DARK_BLUE {
        @Override
        public int DARK_TILE() { return Color.rgb(29, 61, 99); }
        @Override
        public int LIGHT_TILE() { return Color.WHITE; }
        @Override
        public int HIGHLIGHT_LEGAL_MOVE_DARK_TILE() { return Color.rgb(105, 105, 105); }
        @Override
        public int HIGHLIGHT_LEGAL_MOVE_LIGHT_TILE() { return Color.rgb(169, 169, 169); }
    },

    LIGHT_BLUE{
        @Override
        public int DARK_TILE() { return Color.rgb(137, 171, 227); }
        @Override
        public int LIGHT_TILE() { return Color.WHITE; }
        @Override
        public int HIGHLIGHT_LEGAL_MOVE_DARK_TILE() { return Color.rgb(105, 105, 105); }
        @Override
        public int HIGHLIGHT_LEGAL_MOVE_LIGHT_TILE() { return Color.rgb(169, 169, 169); }
    },

    BUMBLEBEE{
        @Override
        public int DARK_TILE() { return Color.rgb(64, 64, 64); }
        @Override
        public int LIGHT_TILE() { return Color.rgb(254, 231, 21); }
        @Override
        public int HIGHLIGHT_LEGAL_MOVE_DARK_TILE() { return Color.rgb(105, 105, 105); }
        @Override
        public int HIGHLIGHT_LEGAL_MOVE_LIGHT_TILE() { return Color.rgb(169, 169, 169); }
    },

    DARK_GRAY{
        @Override
        public int DARK_TILE() { return Color.rgb(105, 105, 105); }
        @Override
        public int LIGHT_TILE() { return Color.WHITE; }
        @Override
        public int HIGHLIGHT_LEGAL_MOVE_DARK_TILE() { return Color.rgb(255, 252, 84); }
        @Override
        public int HIGHLIGHT_LEGAL_MOVE_LIGHT_TILE() { return Color.rgb(255, 253, 156); }
    },

    LIGHT_GRAY{
        @Override
        public int DARK_TILE() { return Color.rgb(177, 179, 179); }
        @Override
        public int LIGHT_TILE() { return Color.WHITE; }
        @Override
        public int HIGHLIGHT_LEGAL_MOVE_DARK_TILE() { return Color.rgb(255, 252, 84); }
        @Override
        public int HIGHLIGHT_LEGAL_MOVE_LIGHT_TILE() { return Color.rgb(255, 253, 156); }
    };

    public abstract int DARK_TILE();
    public abstract int LIGHT_TILE();
    public abstract int HIGHLIGHT_LEGAL_MOVE_DARK_TILE();
    public abstract int HIGHLIGHT_LEGAL_MOVE_LIGHT_TILE();
}