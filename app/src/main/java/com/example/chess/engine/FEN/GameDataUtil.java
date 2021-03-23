package com.example.chess.engine.FEN;

import android.content.Context;
import android.widget.Toast;

import com.example.chess.MainActivity;
import com.example.chess.engine.board.MoveLog;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public final class GameDataUtil {

    private static final String filepath = "chessGame.txt" ;

    private GameDataUtil() {
        throw new RuntimeException("Non instantiable");
    }

    public static void writeMoveToFiles(final MoveLog moveLog, final MainActivity mainActivity) {
        try {
            final ObjectOutputStream objectOutputStream = new ObjectOutputStream(mainActivity.openFileOutput(filepath, Context.MODE_PRIVATE));
            objectOutputStream.writeObject(moveLog);
            objectOutputStream.close();
            Toast.makeText(mainActivity, "Game saved successfully!", Toast.LENGTH_SHORT).show();
        } catch (final IOException e) {
            Toast.makeText(mainActivity, "Game failed to save\nPlease try again", Toast.LENGTH_SHORT).show();
        }
    }

    public static MoveLog readFileToMoveLog(final MainActivity mainActivity) {
        try {
            final ObjectInputStream objectInputStream = new ObjectInputStream(mainActivity.openFileInput(filepath));
            final MoveLog moveLog = (MoveLog)objectInputStream.readObject();
            objectInputStream.close();
            Toast.makeText(mainActivity, "Game loaded successfully!", Toast.LENGTH_SHORT).show();
            return moveLog;
        } catch (final IOException | ClassNotFoundException e) {
            Toast.makeText(mainActivity, "Game failed to load\nOr there's no game to load\nPlease try again", Toast.LENGTH_SHORT).show();
            throw new RuntimeException("Path for FEN file is invalid");
        }
    }
}