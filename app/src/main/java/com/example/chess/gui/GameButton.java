package com.example.chess.gui;

import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.chess.MainActivity;
import com.example.chess.R;
import com.example.chess.engine.FEN.GameDataUtil;
import com.example.chess.engine.board.Board;
import com.example.chess.engine.board.Move;

public final class GameButton {

    public GameButton(final MainActivity mainActivity) {
        this.initGameMenu(mainActivity);
        this.initGameOption(mainActivity);
        this.initAIButton(mainActivity);
        this.initGamePreferences(mainActivity);
    }

    private void initGamePreferences(final MainActivity mainActivity) {
        mainActivity.findViewById(R.id.gamePreferences).setOnClickListener(V ->
                        new AlertDialog.Builder(mainActivity).setMultiChoiceItems(
                new String[]{"Highlight Legal Moves", "Show human move made", "Show AI move made"},
                new boolean[] {mainActivity.getHighlightLegalMoves(), mainActivity.isShowHumanMove(), mainActivity.isShowAIMove()},
                (dialog, which, isChecked) -> {
            dialog.dismiss();
            switch (which) {
                case 0:
                    mainActivity.inverseHighlightLegalMoves();
                    break;
                case 1:
                    mainActivity.inverseShowHumanMove();
                    break;
                case 2:
                    mainActivity.inverseShowAIMove();
                    break;
                default:
                    break;
            }
        }).show());
    }

    private void initAIButton(final MainActivity mainActivity) {
        mainActivity.findViewById(R.id.setupAI).setOnClickListener(V ->
                new AlertDialog.Builder(mainActivity).setTitle("Choose AI Player").setItems(new String[]{"None", "White", "Black", "Both"}, (dialog, which) -> {
          switch (which) {
              case 0:
                  mainActivity.setWhitePlayerType(false);
                  mainActivity.setBlackPlayerType(false);
                  mainActivity.firePropertyChange();
                  break;
              case 1:
                  mainActivity.setWhitePlayerType(true);
                  mainActivity.setBlackPlayerType(false);
                  this.selectLevel(mainActivity);
                  break;
              case 2:
                  mainActivity.setWhitePlayerType(false);
                  mainActivity.setBlackPlayerType(true);
                  this.selectLevel(mainActivity);
                  break;
              case 3:
                  mainActivity.setWhitePlayerType(true);
                  mainActivity.setBlackPlayerType(true);
                  this.selectLevel(mainActivity);
                  break;
              default:
                  break;
          }

        }).show());
    }

    private void selectLevel(final MainActivity mainActivity) {
        new AlertDialog.Builder(mainActivity).setTitle("Select AI level").setItems(new String[]{"Level 1", "Level 2", "Level 3", "Level 4"}, (dialog1, which1) -> {
            mainActivity.setAILevel(which1 + 1);
            mainActivity.firePropertyChange();
        }).show();
    }

    private void initGameOption(final MainActivity mainActivity) {
        mainActivity.findViewById(R.id.gameOption).setOnClickListener(V ->
                new AlertDialog.Builder(mainActivity).setItems(new String[]{"Flip Board", "Change Board Color", "Undo Move"}, (dialog, which) -> {
            dialog.dismiss();
            switch (which) {
                case 0:
                    mainActivity.changeBoardOrientation();
                    mainActivity.drawBoard();
                    break;
                case 1:
                    changeBoardColor(mainActivity);
                    break;
                case 2:
                    this.undoPlayerMove(mainActivity);
                    break;
                default:
                    break;
            }
        }).show());
    }

    private void changeBoardColor(final MainActivity mainActivity) {
        final String[] list = {BoardColor.CLASSIC.toString(), BoardColor.BUMBLEBEE.toString(), BoardColor.DARK_BLUE.toString(),
                                BoardColor.DARK_GRAY.toString(), BoardColor.LIGHT_BLUE.toString(), BoardColor.LIGHT_GRAY.toString()};
        new AlertDialog.Builder(mainActivity).setItems(list, (dialog, which) -> {
            dialog.dismiss();
            switch (which) {
                case 0:
                    mainActivity.setBoardColor(BoardColor.CLASSIC);
                    mainActivity.drawBoard();
                    break;
                case 1:
                    mainActivity.setBoardColor(BoardColor.BUMBLEBEE);
                    mainActivity.drawBoard();
                    break;
                case 2:
                    mainActivity.setBoardColor(BoardColor.DARK_BLUE);
                    mainActivity.drawBoard();
                    break;
                case 3:
                    mainActivity.setBoardColor(BoardColor.DARK_GRAY);
                    mainActivity.drawBoard();
                    break;
                case 4:
                    mainActivity.setBoardColor(BoardColor.LIGHT_BLUE);
                    mainActivity.drawBoard();
                    break;
                case 5:
                    mainActivity.setBoardColor(BoardColor.LIGHT_GRAY);
                    mainActivity.drawBoard();
                    break;
                default:
                    break;
            }
        }).show();
    }

    private void undo(final MainActivity mainActivity) {
        if (mainActivity.getMoveLog().size() != 0) {
            final Move lastMove = mainActivity.getMoveLog().removeMove();
            mainActivity.updateUI(lastMove);
            mainActivity.updateBoard(mainActivity.getChessBoard().currentPlayer().undoMove(lastMove).getPreviousBoard());
        }
        else { Toast.makeText(mainActivity, "No Move to Undo", Toast.LENGTH_LONG).show(); }
    }

    private void undoPlayerMove(final MainActivity mainActivity) {
        if (mainActivity.isAIPlayer(mainActivity.getChessBoard().currentPlayer())
                && !mainActivity.isAIPlayer(mainActivity.getChessBoard().currentPlayer().getOpponent())) {
            mainActivity.setAIThinking(false);
            this.undo(mainActivity);
        } else if (!mainActivity.isAIPlayer(mainActivity.getChessBoard().currentPlayer())
                && mainActivity.isAIPlayer(mainActivity.getChessBoard().currentPlayer().getOpponent())) {
            mainActivity.getMoveLog().removeMove();
            this.undo(mainActivity);
        } else if (!mainActivity.isAIPlayer(mainActivity.getChessBoard().currentPlayer())
                && !mainActivity.isAIPlayer(mainActivity.getChessBoard().currentPlayer().getOpponent())) {
            this.undo(mainActivity);
        }
    }

    private void initGameMenu(final MainActivity mainActivity) {
        mainActivity.findViewById(R.id.gameMenuButton).setOnClickListener(V->
                new AlertDialog.Builder(mainActivity).setItems(new String[]{"New Game", "Save Game", "Load Game", "Exit Game"}, (dialog, which) -> {
                    dialog.dismiss();
            switch (which) {
                case 0:
                    showNewGameDialog(mainActivity);
                    break;
                case 1:
                    showSaveGameDialog(mainActivity);
                    break;
                case 2:
                    showLoadGameDialog(mainActivity);
                    break;
                case 3:
                    mainActivity.onBackPressed();
                    break;
                default:
                    break;
            }
        }).show());
    }

    private void showNewGameDialog(final MainActivity mainActivity) {
        if (mainActivity.getMoveLog().size() != 0) {
            new AlertDialog.Builder(mainActivity)
                    .setTitle("Game Menu")
                    .setMessage("Request confirmation to start a new game and save the current one")
                    .setPositiveButton("yes", (dialog, which) -> {
                        mainActivity.setAIThinking(false);
                        mainActivity.renderProgressBarInvisible();
                        GameDataUtil.writeMoveToFiles(mainActivity.getMoveLog(), mainActivity);
                        mainActivity.restart(Board.createStandardBoard());
                    })
                    .setNegativeButton("no", (dialog, which) -> {
                        mainActivity.setAIThinking(false);
                        mainActivity.renderProgressBarInvisible();
                        mainActivity.restart(Board.createStandardBoard());
                    })
                    .setNeutralButton("cancel", (dialog, which) -> {})
                    .show();
        }
        else { Toast.makeText(mainActivity, "A New Game is created", Toast.LENGTH_LONG).show(); }
    }

    //save game button
    private void showSaveGameDialog(final MainActivity mainActivity) {
        new AlertDialog.Builder(mainActivity)
                .setTitle("Save Game")
                .setMessage("Request confirmation to save game")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> GameDataUtil.writeMoveToFiles(mainActivity.getMoveLog(), mainActivity))
                .setNegativeButton(android.R.string.no, (dialog, which) -> {})
                .show();
    }

    //load game button
    private void showLoadGameDialog(final MainActivity mainActivity) {
        new AlertDialog.Builder(mainActivity)
                .setTitle("Load Game")
                .setMessage("Request confirmation to load saved game")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    mainActivity.setAIThinking(false);
                    mainActivity.renderProgressBarInvisible();
                    mainActivity.resume(GameDataUtil.readFileToMoveLog(mainActivity));
                })
                .setNegativeButton(android.R.string.no, (dialog, which) -> {})
                .show();
    }
}