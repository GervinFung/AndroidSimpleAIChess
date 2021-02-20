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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.chess.engine.FEN.GameDataUtil;
import com.example.chess.engine.League;
import com.example.chess.engine.board.Board;
import com.example.chess.engine.board.BoardUtils;
import com.example.chess.engine.board.Move;
import com.example.chess.engine.board.MoveLog;
import com.example.chess.engine.pieces.Piece;
import com.example.chess.engine.player.ArtificialIntelligence.MiniMax;
import com.example.chess.engine.board.MoveTransition;

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
    private final int legalMovesLightTileColor, legalMovesDarkTileColor;
    private RecyclerView moveHistory, whiteCapturedPiece, blackCapturedPiece;
    private MoveLog moveLog;
    private Spinner AILevelSpinner;
    private Spinner whoIsAISpinner;
    private boolean AIThinking, gameEnded;
    private ProgressBar AIProgressBar;

    public void updateBoard(final Board chessBoard) {
        this.chessBoard = chessBoard;
        if (chessBoard.currentPlayer().getLeague().isBlack()) {
            this.inverseBoard();
        } else {
            this.drawBoard();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle savedInstanceState) {
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putSerializable(MainActivity.SERIALIZABLE_MAIN_ACTIVITY, this);
    }

    //getter
    public Board getChessBoard() { return this.chessBoard; }

    public MainActivity() {
        this.tilesView = new ImageView[64];
        this.moveLog = new MoveLog();
        this.legalMovesLightTileColor = Color.rgb(169, 169, 169);
        this.legalMovesDarkTileColor = Color.rgb(105, 105, 105);
        this.AIThinking = false;
        this.gameEnded = false;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        this.initComponents();
        if (savedInstanceState != null) {
            final MainActivity mainActivity = (MainActivity)savedInstanceState.getSerializable(MainActivity.SERIALIZABLE_MAIN_ACTIVITY);
            this.updateBoard(mainActivity.getChessBoard());
            this.moveLog = mainActivity.moveLog;
            this.whiteCapturedPiece.setAdapter(new CapturedPiece(this.moveLog, League.WHITE));
            this.blackCapturedPiece.setAdapter(new CapturedPiece(this.moveLog, League.BLACK));
            this.moveHistory.setAdapter(new MoveHistory(this.moveLog));
            this.gameEnded = mainActivity.gameEnded;
            this.AIThinking = mainActivity.AIThinking;
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

    private void initComponents() {

        this.moveHistory = new MoveHistoryRecyclerViewBuilder(this, R.id.moveHistory).build();
        this.whiteCapturedPiece = new CapturedPieceRecyclerViewBuilder(this, R.id.whiteCapturedPiece).build();
        this.blackCapturedPiece = new CapturedPieceRecyclerViewBuilder(this, R.id.blackCapturePieces).build();

        new GameButton(this);

        this.AIProgressBar = findViewById(R.id.AIProgressBar);
        this.AIProgressBar.setVisibility(View.INVISIBLE);

        this.AILevelSpinner = new GameSpinnerBuilder(this, R.id.AILevelSpinner, R.array.level).build();
        this.whoIsAISpinner = new GameSpinnerBuilder(this, R.id.whoIsAISpinner, R.array.AI).build();
        this.whoIsAISpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
                if (parent.getSelectedItemPosition() == 1 || parent.getSelectedItemPosition() == 2) {
                    MainActivity.this.AIThinking = true;
                    if (((parent.getSelectedItemPosition() == 1 && MainActivity.this.getChessBoard().currentPlayer().getLeague().isWhite())
                            || (parent.getSelectedItemPosition() == 2 && MainActivity.this.getChessBoard().currentPlayer().getLeague().isBlack()))) {
                        if (parent.getSelectedItemPosition() == 1) {
                            MainActivity.this.drawBoard();
                        } else if (parent.getSelectedItemPosition() == 2) {
                            MainActivity.this.inverseBoard();
                        }
                        MainActivity.this.runAI();
                    }
                } else {
                    MainActivity.this.AIThinking = false;
                }
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {}
        });
    }

    private void updateUI(final Move move) {
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

    private final static class GameSpinnerBuilder{
        private final MainActivity mainActivity;
        private final int id,resource;

        private GameSpinnerBuilder(final MainActivity mainActivity, final int id, final int resource) {
            this.mainActivity = mainActivity;
            this.id = id;
            this.resource = resource;
        }

        private Spinner build() {
            final Spinner spinner = mainActivity.findViewById(this.id);
            final ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(mainActivity, this.resource, R.layout.choose_level);
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(arrayAdapter);
            return spinner;
        }
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

    private final static class GameButton {

        private GameButton(final MainActivity mainActivity) {
            this.initStartButton(mainActivity);
            this.initUndoButton(mainActivity);
            this.initExitButton(mainActivity);
            this.initSaveGameButton(mainActivity);
            this.initLoadGameButton(mainActivity);
        }

        private void initStartButton(final MainActivity mainActivity) {
            final Button button = mainActivity.findViewById(R.id.newGameButton);
            button.setOnClickListener(V-> {
                if (mainActivity.moveLog.size() != 0) {
                    new AlertDialog.Builder(mainActivity)
                            .setTitle("New Game")
                            .setMessage("Request confirmation to start a new game and save the current one")
                            .setPositiveButton("yes", (dialog, which) -> {
                                GameDataUtil.writeMoveToFiles(mainActivity.moveLog, mainActivity);
                                mainActivity.restart(Board.createStandardBoard());
                            })
                            .setNegativeButton("no", (dialog, which) -> mainActivity.restart(Board.createStandardBoard()))
                            .setNeutralButton("cancel", (dialog, which) -> {})
                            .show();
                }
                else { Toast.makeText(mainActivity, "A New Game is created", Toast.LENGTH_LONG).show(); }
            });
        }

        //undo move button
        private void initUndoButton(final MainActivity mainActivity) {
            final Button button = mainActivity.findViewById(R.id.undoMoveButton);
            button.setOnClickListener(V-> {
                if (mainActivity.moveLog.size() != 0) {
                    new AlertDialog.Builder(mainActivity)
                            .setTitle("Undo Move")
                            .setMessage("Request confirmation to undo previous move")
                            .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                                final Move lastMove = mainActivity.moveLog.removeMove();
                                mainActivity.updateUI(lastMove);
                                mainActivity.updateBoard(mainActivity.getChessBoard().currentPlayer().undoMove(lastMove).getPreviousBoard());
                            })
                            .setNegativeButton(android.R.string.no, (dialog, which) -> {})
                            .show();
                }
                else { Toast.makeText(mainActivity, "No Move to Undo", Toast.LENGTH_LONG).show(); }
            });
        }

        //exit game button
        private void initExitButton(final MainActivity mainActivity) {
            final Button button = mainActivity.findViewById(R.id.exitGameButton);
            button.setOnClickListener(V-> mainActivity.onBackPressed());
        }

        //save game button
        private void initSaveGameButton(final MainActivity mainActivity) {
            final Button button = mainActivity.findViewById(R.id.saveGameButton);
            button.setOnClickListener(V->
                    new AlertDialog.Builder(mainActivity)
                            .setTitle("Save Game")
                            .setMessage("Request confirmation to save game")
                            .setPositiveButton(android.R.string.yes, (dialog, which) -> GameDataUtil.writeMoveToFiles(mainActivity.moveLog, mainActivity))
                            .setNegativeButton(android.R.string.no, (dialog, which) -> {})
                            .show());
        }

        //load game button
        private void initLoadGameButton(final MainActivity mainActivity) {
            final Button button = mainActivity.findViewById(R.id.resumeGameButton);
            button.setOnClickListener(V->
                    new AlertDialog.Builder(mainActivity)
                            .setTitle("Load Game")
                            .setMessage("Request confirmation to load saved game")
                            .setPositiveButton(android.R.string.yes, (dialog, which) -> mainActivity.resume(GameDataUtil.readFileToMoveLog(mainActivity)))
                            .setNegativeButton(android.R.string.no, (dialog, which) -> {})
                            .show());
        }
    }

    private void resume(final MoveLog moveLog) {
        this.gameEnded = false;
        //Reinitialise spinner
        this.AILevelSpinner = new GameSpinnerBuilder(this, R.id.AILevelSpinner, R.array.level).build();
        this.whoIsAISpinner = new GameSpinnerBuilder(this, R.id.whoIsAISpinner, R.array.AI).build();
        this.whoIsAISpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
                if (parent.getSelectedItemPosition() == 1 || parent.getSelectedItemPosition() == 2) {
                    MainActivity.this.AIThinking = true;
                    if (((parent.getSelectedItemPosition() == 1 && MainActivity.this.getChessBoard().currentPlayer().getLeague().isWhite())
                            || (parent.getSelectedItemPosition() == 2 && MainActivity.this.getChessBoard().currentPlayer().getLeague().isBlack()))) {
                        if (parent.getSelectedItemPosition() == 1) {
                            MainActivity.this.drawBoard();
                        } else if (parent.getSelectedItemPosition() == 2) {
                            MainActivity.this.inverseBoard();
                        }
                        MainActivity.this.runAI();
                    }
                } else {
                    MainActivity.this.AIThinking = false;
                }
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {}
        });

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

    private void restart(final Board board) {
        this.gameEnded = false;
        //Reinitialise spinner
        this.AILevelSpinner = new GameSpinnerBuilder(this, R.id.AILevelSpinner, R.array.level).build();
        this.whoIsAISpinner = new GameSpinnerBuilder(this, R.id.whoIsAISpinner, R.array.AI).build();
        this.whoIsAISpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
                if (parent.getSelectedItemPosition() == 1 || parent.getSelectedItemPosition() == 2) {
                    MainActivity.this.AIThinking = true;
                    if (((parent.getSelectedItemPosition() == 1 && MainActivity.this.getChessBoard().currentPlayer().getLeague().isWhite())
                            || (parent.getSelectedItemPosition() == 2 && MainActivity.this.getChessBoard().currentPlayer().getLeague().isBlack()))) {
                        if (parent.getSelectedItemPosition() == 1) {
                            MainActivity.this.drawBoard();
                        } else if (parent.getSelectedItemPosition() == 2) {
                            MainActivity.this.inverseBoard();
                        }
                        MainActivity.this.runAI();
                    }
                } else {
                    MainActivity.this.AIThinking = false;
                }
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {}
        });

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
            tileColor = (index % 2 == 0 ? Color.rgb(255, 255, 255) : Color.rgb(29 ,61 ,99));
        } else {
            tileColor = (index % 2 != 0 ? Color.rgb(255, 255, 255) : Color.rgb(29 ,61 ,99));
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
                    this.tilesView[coordinate].setBackgroundColor((coordinate % 2 == 0 ? this.legalMovesLightTileColor : this.legalMovesDarkTileColor));
                } else {
                    this.tilesView[coordinate].setBackgroundColor((coordinate % 2 != 0 ? this.legalMovesLightTileColor : this.legalMovesDarkTileColor));
                }
            }
        }
    }

    private void runAI() {
        final Handler handler2 = new Handler();
        final MiniMax miniMax = new MiniMax(MainActivity.this.AILevelSpinner.getSelectedItemPosition() + 1);
        final AtomicBoolean runProgressBar = new AtomicBoolean(true);
        this.AIProgressBar.setVisibility(View.VISIBLE);
        final int MAX = this.chessBoard.currentPlayer().getLegalMoves().size();
        new Thread(() -> {
            while (runProgressBar.get()) {
                handler2.post(() -> this.AIProgressBar.setProgress(100 * miniMax.getMoveCount() / MAX));
            }
        }).start();
        final Handler handler = new Handler();
        new Thread(() -> {
            final Move bestMove = miniMax.execute(MainActivity.this.getChessBoard());
            runProgressBar.lazySet(false);
            handler.post(() -> {
                this.updateBoard(bestMove.execute());
                this.moveLog.addMove(bestMove);
                this.updateUI(bestMove);
                this.AIProgressBar.setVisibility(View.INVISIBLE);
                this.AIProgressBar.setProgress(0);
            });
        }).start();
    }

    private int initTileView(final int index, final View view) {
        view.setOnClickListener(V-> {
            try {
                if (this.humanMovePiece == null && !this.gameEnded) {
                    if (this.chessBoard.currentPlayer().getLeague().isBlack()) {
                        this.inverseBoard();
                    } else {
                        this.drawBoard();
                    }
                    this.humanMovePiece = this.chessBoard.getTile(index).getPiece();
                    if (this.humanMovePiece.getLeague() != this.chessBoard.currentPlayer().getLeague()) {
                        this.humanMovePiece = null;
                    }
                    this.highlightMove(this.chessBoard);
                } else if (this.humanMovePiece != null && !this.gameEnded){
                    final Move move = MoveFactory.createMove(this.chessBoard, this.humanMovePiece, this.humanMovePiece.getPiecePosition(), index);
                    final MoveTransition transition = this.chessBoard.currentPlayer().makeMove(move);
                    if (transition.getMoveStatus().isDone()) {
                        this.humanMovePiece = null;
                        this.chessBoard = transition.getLatestBoard();
                        if (move instanceof PawnPromotion) {
                            ((PawnPromotion)move).setContext(this).startPromotion();
                        }
                        this.moveLog.addMove(move);
                        this.updateUI(move);
                        if (this.AIThinking && !this.gameEnded) {
                            this.runAI();
                        }
                        if (!this.AIThinking) {
                            if (this.chessBoard.currentPlayer().getLeague().isBlack()) {
                                this.inverseBoard();
                            } else {
                                this.drawBoard();
                            }
                        } else {
                            if (this.whoIsAISpinner.getSelectedItemPosition() == 1) {
                                this.drawBoard();
                            } else if (this.whoIsAISpinner.getSelectedItemPosition() == 2){
                                this.inverseBoard();
                            }
                        }
                    } else {
                        final Piece tempPiece = getPiece(this.humanMovePiece, index);

                        if (tempPiece != null) {
                            if (this.chessBoard.currentPlayer().getLeague().isBlack()) {
                                this.inverseBoard();
                            } else {
                                this.drawBoard();
                            }
                            this.humanMovePiece = this.chessBoard.getTile(index).getPiece();
                            this.highlightMove(this.chessBoard);
                        } else {
                            this.humanMovePiece = null;
                            System.out.println("XXXX");
                            if (this.chessBoard.currentPlayer().getLeague().isBlack()) {
                                this.inverseBoard();
                            } else {
                                this.drawBoard();
                            }
                        }
                    }
                }
            } catch (final NullPointerException ignored) {}
        });
        this.tilesView[index] = (ImageView)view;
        this.drawTile(index);
        if (this.chessBoard.currentPlayer().getLeague().isBlack()) {
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

    private void drawBoard() {
        int index = this.initTileView(0, this.findViewById(R.id.view00));
        index = this.initTileView(index, this.findViewById(R.id.view01));
        index = this.initTileView(index, this.findViewById(R.id.view02));
        index = this.initTileView(index, this.findViewById(R.id.view03));
        index = this.initTileView(index, this.findViewById(R.id.view04));
        index = this.initTileView(index, this.findViewById(R.id.view05));
        index = this.initTileView(index, this.findViewById(R.id.view06));
        index = this.initTileView(index, this.findViewById(R.id.view07));

        index = this.initTileView(index, this.findViewById(R.id.view08));
        index = this.initTileView(index, this.findViewById(R.id.view09));
        index = this.initTileView(index, this.findViewById(R.id.view10));
        index = this.initTileView(index, this.findViewById(R.id.view11));
        index = this.initTileView(index, this.findViewById(R.id.view12));
        index = this.initTileView(index, this.findViewById(R.id.view13));
        index = this.initTileView(index, this.findViewById(R.id.view14));
        index = this.initTileView(index, this.findViewById(R.id.view15));

        index = this.initTileView(index, this.findViewById(R.id.view16));
        index = this.initTileView(index, this.findViewById(R.id.view17));
        index = this.initTileView(index, this.findViewById(R.id.view18));
        index = this.initTileView(index, this.findViewById(R.id.view19));
        index = this.initTileView(index, this.findViewById(R.id.view20));
        index = this.initTileView(index, this.findViewById(R.id.view21));
        index = this.initTileView(index, this.findViewById(R.id.view22));
        index = this.initTileView(index, this.findViewById(R.id.view23));

        index = this.initTileView(index, this.findViewById(R.id.view24));
        index = this.initTileView(index, this.findViewById(R.id.view25));
        index = this.initTileView(index, this.findViewById(R.id.view26));
        index = this.initTileView(index, this.findViewById(R.id.view27));
        index = this.initTileView(index, this.findViewById(R.id.view28));
        index = this.initTileView(index, this.findViewById(R.id.view29));
        index = this.initTileView(index, this.findViewById(R.id.view30));
        index = this.initTileView(index, this.findViewById(R.id.view31));

        index = this.initTileView(index, this.findViewById(R.id.view32));
        index = this.initTileView(index, this.findViewById(R.id.view33));
        index = this.initTileView(index, this.findViewById(R.id.view34));
        index = this.initTileView(index, this.findViewById(R.id.view35));
        index = this.initTileView(index, this.findViewById(R.id.view36));
        index = this.initTileView(index, this.findViewById(R.id.view37));
        index = this.initTileView(index, this.findViewById(R.id.view38));
        index = this.initTileView(index, this.findViewById(R.id.view39));

        index = this.initTileView(index, this.findViewById(R.id.view40));
        index = this.initTileView(index, this.findViewById(R.id.view41));
        index = this.initTileView(index, this.findViewById(R.id.view42));
        index = this.initTileView(index, this.findViewById(R.id.view43));
        index = this.initTileView(index, this.findViewById(R.id.view44));
        index = this.initTileView(index, this.findViewById(R.id.view45));
        index = this.initTileView(index, this.findViewById(R.id.view46));
        index = this.initTileView(index, this.findViewById(R.id.view47));

        index = this.initTileView(index, this.findViewById(R.id.view48));
        index = this.initTileView(index, this.findViewById(R.id.view49));
        index = this.initTileView(index, this.findViewById(R.id.view50));
        index = this.initTileView(index, this.findViewById(R.id.view51));
        index = this.initTileView(index, this.findViewById(R.id.view52));
        index = this.initTileView(index, this.findViewById(R.id.view53));
        index = this.initTileView(index, this.findViewById(R.id.view54));
        index = this.initTileView(index, this.findViewById(R.id.view55));

        index = this.initTileView(index, this.findViewById(R.id.view56));
        index = this.initTileView(index, this.findViewById(R.id.view57));
        index = this.initTileView(index, this.findViewById(R.id.view58));
        index = this.initTileView(index, this.findViewById(R.id.view59));
        index = this.initTileView(index, this.findViewById(R.id.view60));
        index = this.initTileView(index, this.findViewById(R.id.view61));
        index = this.initTileView(index, this.findViewById(R.id.view62));
        this.initTileView(index, this.findViewById(R.id.view63));
    }

    private void inverseBoard() {
        int index = this.initTileView(63, this.findViewById(R.id.view00));
        index = this.initTileView(index, this.findViewById(R.id.view01));
        index = this.initTileView(index, this.findViewById(R.id.view02));
        index = this.initTileView(index, this.findViewById(R.id.view03));
        index = this.initTileView(index, this.findViewById(R.id.view04));
        index = this.initTileView(index, this.findViewById(R.id.view05));
        index = this.initTileView(index, this.findViewById(R.id.view06));
        index = this.initTileView(index, this.findViewById(R.id.view07));

        index = this.initTileView(index, this.findViewById(R.id.view08));
        index = this.initTileView(index, this.findViewById(R.id.view09));
        index = this.initTileView(index, this.findViewById(R.id.view10));
        index = this.initTileView(index, this.findViewById(R.id.view11));
        index = this.initTileView(index, this.findViewById(R.id.view12));
        index = this.initTileView(index, this.findViewById(R.id.view13));
        index = this.initTileView(index, this.findViewById(R.id.view14));
        index = this.initTileView(index, this.findViewById(R.id.view15));

        index = this.initTileView(index, this.findViewById(R.id.view16));
        index = this.initTileView(index, this.findViewById(R.id.view17));
        index = this.initTileView(index, this.findViewById(R.id.view18));
        index = this.initTileView(index, this.findViewById(R.id.view19));
        index = this.initTileView(index, this.findViewById(R.id.view20));
        index = this.initTileView(index, this.findViewById(R.id.view21));
        index = this.initTileView(index, this.findViewById(R.id.view22));
        index = this.initTileView(index, this.findViewById(R.id.view23));

        index = this.initTileView(index, this.findViewById(R.id.view24));
        index = this.initTileView(index, this.findViewById(R.id.view25));
        index = this.initTileView(index, this.findViewById(R.id.view26));
        index = this.initTileView(index, this.findViewById(R.id.view27));
        index = this.initTileView(index, this.findViewById(R.id.view28));
        index = this.initTileView(index, this.findViewById(R.id.view29));
        index = this.initTileView(index, this.findViewById(R.id.view30));
        index = this.initTileView(index, this.findViewById(R.id.view31));

        index = this.initTileView(index, this.findViewById(R.id.view32));
        index = this.initTileView(index, this.findViewById(R.id.view33));
        index = this.initTileView(index, this.findViewById(R.id.view34));
        index = this.initTileView(index, this.findViewById(R.id.view35));
        index = this.initTileView(index, this.findViewById(R.id.view36));
        index = this.initTileView(index, this.findViewById(R.id.view37));
        index = this.initTileView(index, this.findViewById(R.id.view38));
        index = this.initTileView(index, this.findViewById(R.id.view39));

        index = this.initTileView(index, this.findViewById(R.id.view40));
        index = this.initTileView(index, this.findViewById(R.id.view41));
        index = this.initTileView(index, this.findViewById(R.id.view42));
        index = this.initTileView(index, this.findViewById(R.id.view43));
        index = this.initTileView(index, this.findViewById(R.id.view44));
        index = this.initTileView(index, this.findViewById(R.id.view45));
        index = this.initTileView(index, this.findViewById(R.id.view46));
        index = this.initTileView(index, this.findViewById(R.id.view47));

        index = this.initTileView(index, this.findViewById(R.id.view48));
        index = this.initTileView(index, this.findViewById(R.id.view49));
        index = this.initTileView(index, this.findViewById(R.id.view50));
        index = this.initTileView(index, this.findViewById(R.id.view51));
        index = this.initTileView(index, this.findViewById(R.id.view52));
        index = this.initTileView(index, this.findViewById(R.id.view53));
        index = this.initTileView(index, this.findViewById(R.id.view54));
        index = this.initTileView(index, this.findViewById(R.id.view55));

        index = this.initTileView(index, this.findViewById(R.id.view56));
        index = this.initTileView(index, this.findViewById(R.id.view57));
        index = this.initTileView(index, this.findViewById(R.id.view58));
        index = this.initTileView(index, this.findViewById(R.id.view59));
        index = this.initTileView(index, this.findViewById(R.id.view60));
        index = this.initTileView(index, this.findViewById(R.id.view61));
        index = this.initTileView(index, this.findViewById(R.id.view62));
        this.initTileView(index, this.findViewById(R.id.view63));
    }
}