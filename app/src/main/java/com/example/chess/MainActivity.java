package com.example.chess;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.chess.engine.FEN.GameDataUtil;
import com.example.chess.engine.League;
import com.example.chess.engine.board.Board;
import com.example.chess.engine.board.BoardUtils;
import com.example.chess.engine.board.Move;
import com.example.chess.engine.board.MoveLog;
import com.example.chess.engine.pieces.Piece;
import com.example.chess.engine.player.ArtificialIntelligence.MiniMax;
import com.example.chess.engine.board.MoveTransition;
import com.example.chess.engine.player.Player;
import com.example.chess.gui.BoardColor;
import com.example.chess.gui.BoardOrientation;
import com.example.chess.gui.CapturedPiece;
import com.example.chess.gui.GameButton;
import com.example.chess.gui.MoveHistory;
import com.example.chess.gui.PawnPromotionUI;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.example.chess.engine.board.Move.*;

public final class MainActivity extends AppCompatActivity implements Serializable{

    private static final String SERIALIZABLE_MAIN_ACTIVITY = "MAIN_ACTIVITY";

    private final ImageView[] tilesView;
    private Piece humanMovePiece;
    private Board chessBoard;
    private BoardOrientation boardOrientation;
    private BoardColor boardColor;
    private RecyclerView moveHistory, whiteCapturedPiece, blackCapturedPiece;
    private int AILevel;
    private MoveLog moveLog;
    private volatile boolean AIThinking;
    private boolean gameEnded, showHumanMove, showAIMove, showHighlightMove;
    private ProgressBar AIProgressBar;
    private final PropertyChangeSupport propertyChangeSupport;
    private PLAYER_TYPE whitePlayerType, blackPlayerType;
    private Move humanMove, aiMove;

    public MoveLog getMoveLog() { return this.moveLog; }
    public void setBoardColor(final BoardColor boardColor) { this.boardColor = boardColor; }
    public void changeBoardOrientation() { this.boardOrientation = this.boardOrientation.getOpposite(); }
    public void drawBoard() {
        this.boardOrientation.drawBoard(this);
        this.highlightHumanMove();
        this.highlightAIMove();
    }
    public void setAILevel(final int AILevel) {
        if (AILevel > 0 && AILevel < 5) {
            this.AILevel = AILevel;
        } else {
            throw new IllegalArgumentException("AI Level 1 TO 4 only");
        }
    }
    public void firePropertyChange() { this.propertyChangeSupport.firePropertyChange(null, null, null); }
    public void setAIThinking(final boolean AIThinking) { this.AIThinking = AIThinking; }
    public void inverseHighlightLegalMoves() { this.showHighlightMove = !this.showHighlightMove; }
    public void inverseShowHumanMove() { this.showHumanMove = !this.showHumanMove; }
    public void inverseShowAIMove() { this.showAIMove = !this.showAIMove; }
    public void renderProgressBarInvisible() { this.AIProgressBar.setVisibility(View.INVISIBLE); }
    public void setWhitePlayerType(final boolean changeToAI) { this.whitePlayerType = changeToAI ? PLAYER_TYPE.AI : PLAYER_TYPE.HUMAN; }
    public void setBlackPlayerType(final boolean changeToAI) { this.blackPlayerType = changeToAI ? PLAYER_TYPE.AI : PLAYER_TYPE.HUMAN; }

    public void updateBoard(final Board chessBoard) {
        this.chessBoard = chessBoard;
        this.drawBoard();
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle savedInstanceState) {
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putSerializable(MainActivity.SERIALIZABLE_MAIN_ACTIVITY, this);
    }

    //getter
    public Board getChessBoard() { return this.chessBoard; }
    public boolean getHighlightLegalMoves() { return this.showHighlightMove; }
    public boolean isShowHumanMove() { return this.showHumanMove; }
    public boolean isShowAIMove() { return this.showAIMove; }

    public MainActivity() {
        this.tilesView = new ImageView[64];
        final PropertyChangeListener propertyChangeListener = evt -> {
            if (this.isAIPlayer(this.chessBoard.currentPlayer()) && !this.gameEnded) {
                if(!this.AIThinking) {
                    this.AIThinking = true;
                    this.runAI();
                }
            }
            if (!this.isAIPlayer(this.chessBoard.currentPlayer())) {
                this.AIThinking = false;
            }
            this.displayEndGameMessage();
        };
        this.propertyChangeSupport = new PropertyChangeSupport(propertyChangeListener);
        this.propertyChangeSupport.addPropertyChangeListener(propertyChangeListener);
        this.whitePlayerType = PLAYER_TYPE.HUMAN;
        this.blackPlayerType = PLAYER_TYPE.HUMAN;
        this.moveLog = new MoveLog();
        this.boardColor = BoardColor.CLASSIC;
        this.boardOrientation = BoardOrientation.NORMAL;
        this.showHighlightMove = true;
        this.AIThinking = false;
        this.gameEnded = false;
        this.showHumanMove = true;
        this.showAIMove = true;
    }

    private enum PLAYER_TYPE {HUMAN, AI}

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        this.initComponents();
        if (savedInstanceState != null) {
            final MainActivity mainActivity = (MainActivity)savedInstanceState.getSerializable(MainActivity.SERIALIZABLE_MAIN_ACTIVITY);
            this.updateBoard(mainActivity.getChessBoard());
            this.humanMovePiece = mainActivity.humanMovePiece;
            this.boardOrientation = mainActivity.boardOrientation;
            this.boardColor = mainActivity.boardColor;
            this.whiteCapturedPiece.setAdapter(new CapturedPiece(this.moveLog, League.WHITE));
            this.blackCapturedPiece.setAdapter(new CapturedPiece(this.moveLog, League.BLACK));
            this.moveHistory.setAdapter(new MoveHistory(this.moveLog));
            this.AILevel = mainActivity.AILevel;
            this.moveLog = mainActivity.moveLog;
            this.gameEnded = mainActivity.gameEnded;
            this.AIThinking = mainActivity.AIThinking;
            this.showHighlightMove = mainActivity.showHighlightMove;
            this.showHumanMove = mainActivity.showHumanMove;
            this.showAIMove = mainActivity.showAIMove;
            this.AIProgressBar = mainActivity.AIProgressBar;
            this.whitePlayerType = mainActivity.whitePlayerType;
            this.blackPlayerType = mainActivity.blackPlayerType;
            this.humanMove = mainActivity.humanMove;
            this.aiMove = mainActivity.humanMove;
        } else {
            this.updateBoard(Board.createStandardBoard());
        }
    }

    private void displayEndGameMessage() {
        if (this.chessBoard.currentPlayer().isInCheckmate()) {
            this.gameEnded = true;
            new AlertDialog.Builder(this)
                    .setTitle("Checkmate")
                    .setMessage(this.chessBoard.currentPlayer().getLeague().toString() + " is in Checkmate!")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> this.showEndGameNavigationMessage())
                    .show();
        }
        else if (this.chessBoard.currentPlayer().isInStalemate()) {
            this.gameEnded = true;
            this.showEndGameNavigationMessage();
            new AlertDialog.Builder(this)
                    .setTitle("Stalemate")
                    .setMessage(this.chessBoard.currentPlayer().getLeague().toString() + " is in stalemate")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> this.showEndGameNavigationMessage())
                    .show();
        }
    }

    private void showEndGameNavigationMessage() {
        new AlertDialog.Builder(this)
                .setTitle("Game Over")
                .setMessage("1. New Game to start a new game\n2. Exit Game to exit this game")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {})
                .show();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Exit Game")
                .setMessage("Request confirmation to exit game and save the current one")
                .setPositiveButton("yes", (dialog, which) -> {
                    GameDataUtil.writeMoveToFiles(this.moveLog, this);
                    this.exitGame();
                })
                .setNegativeButton("no", (dialog, which) -> this.exitGame())
                .setNeutralButton("cancel", (dialog, which) -> {})
                .show();
    }

    private void exitGame() {
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        this.startActivity(intent);
        System.exit(0);
    }

    public boolean isAIPlayer(final Player player) {
        if(player.getLeague() == League.WHITE) {
            return this.whitePlayerType == PLAYER_TYPE.AI;
        }
        return this.blackPlayerType == PLAYER_TYPE.AI;
    }

    private void initComponents() {

        this.moveHistory = new MoveHistoryRecyclerViewBuilder(this, R.id.moveHistory).build();
        this.whiteCapturedPiece = new CapturedPieceRecyclerViewBuilder(this, R.id.whiteCapturedPiece).build();
        this.blackCapturedPiece = new CapturedPieceRecyclerViewBuilder(this, R.id.blackCapturePieces).build();

        new GameButton(this);

        this.AIProgressBar = findViewById(R.id.AIProgressBar);
        this.AIProgressBar.setVisibility(View.INVISIBLE);
    }

    public void updateUI(final Move move) {
        if (move.isAttack() || move instanceof PawnPromotion && ((PawnPromotion)move).getDecoratedMove().isAttack()) {
            if (this.chessBoard.currentPlayer().getLeague().isBlack()) {
                this.whiteCapturedPiece.setAdapter(new CapturedPiece(this.moveLog, League.BLACK));
            }
            else {
                this.blackCapturedPiece.setAdapter(new CapturedPiece(this.moveLog, League.WHITE));
            }
        }
        this.moveHistory.setAdapter(new MoveHistory(this.moveLog));
        this.displayEndGameMessage();
    }

    private final static class CapturedPieceRecyclerViewBuilder {

        private final MainActivity mainActivity;
        private final int id;

        public CapturedPieceRecyclerViewBuilder(final MainActivity mainActivity, final int id) {
            this.mainActivity = mainActivity;
            this.id = id;
        }

        private RecyclerView build() {
            final RecyclerView recyclerView = mainActivity.findViewById(this.id);
            recyclerView.setLayoutManager(new LinearLayoutManager(this.mainActivity, LinearLayoutManager.HORIZONTAL, false));
            recyclerView.setHasFixedSize(true);
            return recyclerView;
        }
    }

    private final static class MoveHistoryRecyclerViewBuilder {

        private final MainActivity mainActivity;
        private final int id;

        private MoveHistoryRecyclerViewBuilder(final MainActivity mainActivity, final int id) {
            this.mainActivity = mainActivity;
            this.id = id;
        }

        private DividerItemDecoration dividerItemDecoration(final int i) {
            final DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this.mainActivity, i);
            final GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{0xfff7f7f7, 0xfff7f7f7});
            drawable.setSize(1,3);
            dividerItemDecoration.setDrawable(drawable);
            return dividerItemDecoration;
        }

        private RecyclerView build() {
            final RecyclerView recyclerView = this.mainActivity.findViewById(this.id);
            recyclerView.setLayoutManager(new GridLayoutManager(this.mainActivity, 2, GridLayoutManager.HORIZONTAL, false));
            recyclerView.addItemDecoration(dividerItemDecoration(DividerItemDecoration.VERTICAL));
            recyclerView.addItemDecoration(dividerItemDecoration(DividerItemDecoration.HORIZONTAL));
            recyclerView.setHasFixedSize(true);
            return recyclerView;
        }
    }

    public void resume(final MoveLog moveLog) {
        this.gameEnded = false;

        //Clear move history
        this.moveLog.clear();
        for(final Move move: moveLog.getMoves()) {
            this.moveLog.addMove(move);
        }
        this.updateBoard(this.moveLog.getMoves().get(this.moveLog.size() - 1).execute());
        this.moveHistory.setAdapter(new MoveHistory(this.moveLog));

        //Clear captured Pieces
        this.whiteCapturedPiece.setAdapter(new CapturedPiece(this.moveLog, this.chessBoard.whitePlayer().getLeague()));
        this.blackCapturedPiece.setAdapter(new CapturedPiece(this.moveLog, this.chessBoard.blackPlayer().getLeague()));
    }

    public void restart(final Board board) {
        this.gameEnded = false;

        this.updateBoard(board);
        //Clear move history
        this.moveLog.clear();
        this.moveHistory.setAdapter(new MoveHistory(this.moveLog));

        //Clear captured Pieces
        this.whiteCapturedPiece.setAdapter(new CapturedPiece(this.moveLog, this.chessBoard.currentPlayer().getLeague()));
        this.blackCapturedPiece.setAdapter(new CapturedPiece(this.moveLog, this.chessBoard.currentPlayer().getLeague()));
    }

    private void drawTile(final int index) {
        final int tileColor;
        if (BoardUtils.FIRST_ROW.get(index) || BoardUtils.THIRD_ROW.get(index) || BoardUtils.FIFTH_ROW.get(index) || BoardUtils.SEVENTH_ROW.get(index)) {
            tileColor = (index % 2 == 0 ? this.boardColor.LIGHT_TILE() : this.boardColor.DARK_TILE());
        } else {
            tileColor = (index % 2 != 0 ? this.boardColor.LIGHT_TILE() : this.boardColor.DARK_TILE());
        }
        this.tilesView[index].setBackgroundColor(tileColor);
        if (this.chessBoard.getTile(index).isTileOccupied()) {
            this.tilesView[index].setImageResource(BoardUtils.getPieceImage(this.chessBoard.getTile(index).getPiece()));
        } else {
            this.tilesView[index].setImageResource(android.R.color.transparent);
        }
    }

    private Collection<Move> pieceLegalMoves(final Board board) {
        if(this.humanMovePiece != null && this.humanMovePiece.getLeague() == board.currentPlayer().getLeague()) { return this.humanMovePiece.calculateLegalMoves(board); }
        return Collections.emptyList();
    }

    private void highlightMove(final Board board) {
        for (final Move move : this.pieceLegalMoves(board)) {
            final int coordinate = move.getDestinationCoordinate();
            if (move.isAttack() || move instanceof PawnPromotion && ((PawnPromotion)move).getDecoratedMove().isAttack()) {
                //dark red
                this.tilesView[coordinate].setBackgroundColor(Color.rgb(204, 0, 0));
            } else {
                if (BoardUtils.FIRST_ROW.get(coordinate) || BoardUtils.THIRD_ROW.get(coordinate) || BoardUtils.FIFTH_ROW.get(coordinate) || BoardUtils.SEVENTH_ROW.get(coordinate)) {
                    this.tilesView[coordinate].setBackgroundColor((coordinate % 2 == 0 ? this.boardColor.HIGHLIGHT_LEGAL_MOVE_LIGHT_TILE() : this.boardColor.HIGHLIGHT_LEGAL_MOVE_DARK_TILE()));
                } else {
                    this.tilesView[coordinate].setBackgroundColor((coordinate % 2 != 0 ? this.boardColor.HIGHLIGHT_LEGAL_MOVE_LIGHT_TILE() : this.boardColor.HIGHLIGHT_LEGAL_MOVE_DARK_TILE()));
                }
            }
        }
    }

    private void runAI() {
        final Handler handler2 = new Handler();
        final MiniMax miniMax = new MiniMax(this.AILevel);
        final AtomicBoolean runProgressBar = new AtomicBoolean(true);
        this.AIProgressBar.setVisibility(View.VISIBLE);
        final int MAX = this.chessBoard.currentPlayer().getLegalMoves().size();
        new Thread(() -> {
            while (runProgressBar.get() && AIThinking) {
                handler2.post(() -> this.AIProgressBar.setProgress(100 * miniMax.getMoveCount() / MAX));
            }
            if (!AIThinking) {
                this.AIProgressBar.setVisibility(View.INVISIBLE);
                this.AIProgressBar.setProgress(0);
            }
        }).start();
        final Handler handler = new Handler();
        new Thread(() -> {
            final Move bestMove = miniMax.execute(MainActivity.this.getChessBoard());
            this.aiMove = bestMove;
            this.humanMove = null;
            runProgressBar.lazySet(false);
            if (AIThinking) {
                handler.post(() -> {
                    this.updateBoard(bestMove.execute());
                    this.moveLog.addMove(bestMove);
                    this.updateUI(bestMove);
                    this.AIProgressBar.setVisibility(View.INVISIBLE);
                    this.AIProgressBar.setProgress(0);
                    this.firePropertyChange();
                    this.AIThinking = false;
                    this.highlightAIMove();
                });
            }
        }).start();
    }

    private void highlightAIMove() {
        if(this.aiMove != null && this.showAIMove) {
            this.tilesView[this.aiMove.getCurrentCoordinate()].setBackgroundColor(Color.rgb(255,192,203));
            this.tilesView[this.aiMove.getDestinationCoordinate()].setBackgroundColor(Color.rgb(255, 51, 51));
        }
    }

    public void highlightHumanMove() {
        if (this.humanMove != null && this.showHumanMove) {
            this.tilesView[this.humanMove.getCurrentCoordinate()].setBackgroundColor(Color.rgb(102, 255, 102));
            this.tilesView[this.humanMove.getDestinationCoordinate()].setBackgroundColor(Color.rgb(50, 205, 50));
        }
    }

    public int initTileView(final int index, final View view) {
        view.setOnClickListener(V-> {
            try {
                if (this.humanMovePiece == null && !this.gameEnded && !this.AIThinking) {
                    this.drawBoard();
                    this.humanMovePiece = this.chessBoard.getTile(index).getPiece();
                    if (this.humanMovePiece.getLeague() != this.chessBoard.currentPlayer().getLeague()) {
                        this.humanMovePiece = null;
                    }
                    if (this.showHighlightMove) {
                        this.highlightMove(this.chessBoard);
                    }
                } else if (this.humanMovePiece != null && !this.gameEnded) {
                    final Move move = MoveFactory.createMove(this.chessBoard, this.humanMovePiece, this.humanMovePiece.getPiecePosition(), index);
                    final MoveTransition transition = this.chessBoard.currentPlayer().makeMove(move);
                    if (transition.getMoveStatus().isDone()) {
                        this.humanMovePiece = null;
                        this.chessBoard = transition.getLatestBoard();
                        this.humanMove = move;
                        this.moveLog.addMove(move);
                        this.aiMove = null;
                        if (move instanceof PawnPromotion) {
                            new PawnPromotionUI(this).startPromotion((PawnPromotion)move);
                        } else {
                            this.updateUI(move);
                            this.drawBoard();
                            this.highlightHumanMove();
                            this.firePropertyChange();
                        }
                    } else {
                        this.humanMovePiece = getPiece(this.humanMovePiece, index);
                        this.drawBoard();
                        if (this.humanMovePiece != null) {
                            if (this.showHighlightMove) {
                                this.highlightMove(this.chessBoard);
                            }
                        }
                    }
                }
            } catch (final NullPointerException ignored) {}
        });
        this.tilesView[index] = (ImageView)view;
        this.drawTile(index);
        if (this.boardOrientation == BoardOrientation.FLIPPED) {
            return index - 1;
        }
        return index + 1;
    }

    private Piece getPiece(final Piece humanPiece, final int index) {
        final Piece piece = this.chessBoard.getTile(index).getPiece();
        if (piece == null) {
            return null;
        }
        else if (piece.getPiecePosition() == index && humanPiece.getLeague() == piece.getLeague()) {
            return piece;
        }
        return null;
    }
}