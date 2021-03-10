package com.example.chess.gui;

import com.example.chess.MainActivity;
import com.example.chess.R;

public enum BoardOrientation {

    NORMAL {
        @Override
        public void drawBoard(final MainActivity mainActivity) {
            int index = mainActivity.initTileView(0, mainActivity.findViewById(R.id.view00));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view01));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view02));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view03));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view04));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view05));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view06));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view07));

            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view08));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view09));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view10));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view11));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view12));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view13));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view14));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view15));

            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view16));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view17));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view18));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view19));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view20));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view21));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view22));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view23));

            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view24));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view25));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view26));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view27));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view28));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view29));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view30));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view31));

            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view32));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view33));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view34));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view35));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view36));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view37));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view38));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view39));

            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view40));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view41));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view42));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view43));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view44));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view45));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view46));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view47));

            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view48));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view49));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view50));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view51));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view52));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view53));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view54));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view55));

            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view56));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view57));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view58));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view59));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view60));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view61));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view62));
            mainActivity.initTileView(index, mainActivity.findViewById(R.id.view63));
        }

        @Override
        public BoardOrientation getOpposite() { return FLIPPED; }

    }, FLIPPED {
        @Override
        public void drawBoard(final MainActivity mainActivity) {
            int index = mainActivity.initTileView(63, mainActivity.findViewById(R.id.view00));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view01));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view02));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view03));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view04));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view05));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view06));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view07));

            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view08));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view09));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view10));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view11));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view12));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view13));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view14));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view15));

            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view16));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view17));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view18));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view19));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view20));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view21));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view22));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view23));

            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view24));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view25));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view26));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view27));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view28));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view29));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view30));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view31));

            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view32));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view33));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view34));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view35));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view36));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view37));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view38));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view39));

            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view40));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view41));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view42));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view43));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view44));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view45));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view46));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view47));

            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view48));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view49));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view50));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view51));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view52));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view53));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view54));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view55));

            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view56));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view57));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view58));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view59));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view60));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view61));
            index = mainActivity.initTileView(index, mainActivity.findViewById(R.id.view62));
            mainActivity.initTileView(index, mainActivity.findViewById(R.id.view63));
        }

        @Override
        public BoardOrientation getOpposite() { return NORMAL; }
    };
    public abstract void drawBoard(final MainActivity mainActivity);
    public abstract BoardOrientation getOpposite();
}
