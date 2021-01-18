package com.example.chess;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.chess.engine.FEN.FenUtilities;
import com.example.chess.engine.League;
import com.example.chess.engine.board.Board;
import com.example.chess.engine.board.BoardUtils;
import com.example.chess.engine.board.Move;
import com.example.chess.engine.pieces.Piece;
import com.example.chess.engine.player.ArtificialIntelligence.MiniMax;
import com.example.chess.engine.player.MoveTransition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.example.chess.engine.board.Move.*;

public final class MainActivity extends AppCompatActivity implements Serializable{

    private static final String SERIALIZABLE_MAIN_ACTIVITY = "MAIN_ACTIVITY";

    private final ImageView[] tilesView;
    private Piece humanMovePiece;
    private Board chessBoard;
    private final int legalMovesLightTileColor, legalMovesDarkTileColor;
    private RecyclerView moveHistory, whiteCapturedPiece, blackCapturedPiece;
    private MoveLog moveLog;
    private final ArtificialIntelligence artificialIntelligence;
    private Spinner AILevelSpinner;
    private boolean AIThinking;

    @RequiresApi(api = Build.VERSION_CODES.R)
    public void updateBoard(final Board chessBoard) {
        this.chessBoard = chessBoard;
        this.drawBoard();
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
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
        this.artificialIntelligence = new ArtificialIntelligence(this);
        this.AIThinking = false;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
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
        } else {
            this.updateBoard(Board.createStandardBoard());
        }
    }


    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Exit Gane")
                .setMessage("Request confirmation to exit game and save the current one")
                .setPositiveButton("yes", (dialog, which) -> {
                    FenUtilities.writeFENToFile(this);
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

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void initComponents() {

        this.moveHistory = new MoveHistoryRecyclerViewBuilder(this, R.id.moveHistory).build();
        this.whiteCapturedPiece = new CapturedPieceRecyclerViewBuilder(this, R.id.whiteCapturedPiece).build();
        this.blackCapturedPiece = new CapturedPieceRecyclerViewBuilder(this, R.id.blackCapturePieces).build();

        GameButton.initGameButton(this);

        this.AILevelSpinner = new GameSpinnerBuilder(this, R.id.AILevelSpinner, R.array.level).build();
        final Spinner whoIsAISpinner = new GameSpinnerBuilder(this, R.id.whoIsAISpinner, R.array.AI).build();
        whoIsAISpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
                if ((parent.getSelectedItemPosition() == 1 && MainActivity.this.getChessBoard().currentPlayer().getLeague().isWhite())
                    || (parent.getSelectedItemPosition() == 2 && MainActivity.this.getChessBoard().currentPlayer().getLeague().isBlack())) {
                    MainActivity.this.AIThinking = true;
                    MainActivity.this.artificialIntelligence.execute(MainActivity.this.AILevelSpinner.getSelectedItemPosition());
                } else {
                    MainActivity.this.AIThinking = false;
                }
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {
                if (MainActivity.this.AIThinking = true) {
                    MainActivity.this.artificialIntelligence.execute(MainActivity.this.AILevelSpinner.getSelectedItemPosition());
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
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
    }

    private final static class ArtificialIntelligence {

        private final MainActivity mainActivity;

        private ArtificialIntelligence(final MainActivity mainActivity) { this.mainActivity = mainActivity; }

        @RequiresApi(api = Build.VERSION_CODES.R)
        //did not use Async task due to deprecation in API >= 30
        private void execute(final int level) {
            final MiniMax miniMax = new MiniMax(level + 1);
            final Handler handler = new Handler();
            new Thread() {
                @Override
                public void run() {
                    handler.post(() -> {
                            final Move bestMove = miniMax.execute(ArtificialIntelligence.this.mainActivity.getChessBoard());
                            ArtificialIntelligence.this.mainActivity.updateBoard(bestMove.execute());
                            ArtificialIntelligence.this.mainActivity.moveLog.addMove(bestMove);
                            ArtificialIntelligence.this.mainActivity.updateUI(bestMove);
                        }
                    );
                }
            }.start();
        }
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

        private GameButton() { throw new RuntimeException("Cannot instantiate GameButton"); }

        @RequiresApi(api = Build.VERSION_CODES.R)
        private static void initGameButton(final MainActivity mainActivity) {
            initStartButton(mainActivity);
            initUndoButton(mainActivity);
            initExitButton(mainActivity);
            initSaveGameButton(mainActivity);
            initLoadGameButton(mainActivity);
        }

        @RequiresApi(api = Build.VERSION_CODES.R)
        private static void initStartButton(final MainActivity mainActivity) {
            final Button button = mainActivity.findViewById(R.id.newGameButton);
            button.setOnClickListener(V-> {
                if (mainActivity.moveLog.size() != 0) {
                    new AlertDialog.Builder(mainActivity)
                            .setTitle("New Game")
                            .setMessage("Request confirmation to start a new game and save the current one")
                            .setPositiveButton("yes", (dialog, which) -> {
                                FenUtilities.writeFENToFile(mainActivity);
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
        @RequiresApi(api = Build.VERSION_CODES.R)
        private static void initUndoButton(final MainActivity mainActivity) {
            final Button button = mainActivity.findViewById(R.id.undoMoveButton);
            button.setOnClickListener(V-> {
                if (mainActivity.moveLog.size() != 0) {
                    new AlertDialog.Builder(mainActivity)
                            .setTitle("Undo Move")
                            .setMessage("Request confirmation to undo previous move")
                            .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                                final Move lastMove = mainActivity.moveLog.removeMove();
                                mainActivity.updateUI(lastMove);
                                mainActivity.updateBoard(lastMove.getBoard());
                            })
                            .setNegativeButton(android.R.string.no, (dialog, which) -> {})
                            .show();
                }
                else { Toast.makeText(mainActivity, "No Move to Undo", Toast.LENGTH_LONG).show(); }
            });
        }

        //exit game button
        @RequiresApi(api = Build.VERSION_CODES.R)
        private static void initExitButton(final MainActivity mainActivity) {
            final Button button = mainActivity.findViewById(R.id.exitGameButton);
            button.setOnClickListener(V-> mainActivity.onBackPressed());
        }

        //save game button
        @RequiresApi(api = Build.VERSION_CODES.R)
        private static void initSaveGameButton(final MainActivity mainActivity) {
            final Button button = mainActivity.findViewById(R.id.saveGameButton);
            button.setOnClickListener(V->
                    new AlertDialog.Builder(mainActivity)
                    .setTitle("Save Game")
                    .setMessage("Request confirmation to save game")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> FenUtilities.writeFENToFile(mainActivity))
                    .setNegativeButton(android.R.string.no, (dialog, which) -> {})
                    .show());
        }

        //load game button
        @RequiresApi(api = Build.VERSION_CODES.R)
        private static void initLoadGameButton(final MainActivity mainActivity) {
            final Button button = mainActivity.findViewById(R.id.resumeGameButton);
            button.setOnClickListener(V->
                    new AlertDialog.Builder(mainActivity)
                    .setTitle("Load Game")
                    .setMessage("Request confirmation to load saved game")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> mainActivity.restart(FenUtilities.createGameFromFEN(mainActivity)))
                    .setNegativeButton(android.R.string.no, (dialog, which) -> {})
                    .show());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void restart(final Board board) {

        //Reinitialise spinner
        this.AILevelSpinner = new GameSpinnerBuilder(this, R.id.AILevelSpinner, R.array.level).build();
        final Spinner whoIsAISpinner = new GameSpinnerBuilder(this, R.id.whoIsAISpinner, R.array.AI).build();
        whoIsAISpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
                if ((parent.getSelectedItemPosition() == 1 && MainActivity.this.getChessBoard().currentPlayer().getLeague().isWhite())
                        || (parent.getSelectedItemPosition() == 2 && MainActivity.this.getChessBoard().currentPlayer().getLeague().isBlack())) {
                    MainActivity.this.AIThinking = true;
                    MainActivity.this.artificialIntelligence.execute(MainActivity.this.AILevelSpinner.getSelectedItemPosition());
                } else {
                    MainActivity.this.AIThinking = false;
                }
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {
                if (MainActivity.this.AIThinking = true) {
                    MainActivity.this.artificialIntelligence.execute(MainActivity.this.AILevelSpinner.getSelectedItemPosition());
                }
            }
        });

        this.updateBoard(board);
        //Clear move history
        this.moveLog.clear();
        this.moveHistory.setAdapter(new MoveHistory(this.moveLog));

        //Clear captured Pieces
        this.whiteCapturedPiece.setAdapter(new CapturedPiece(this.moveLog, this.chessBoard.currentPlayer().getLeague()));
        this.blackCapturedPiece.setAdapter(new CapturedPiece(this.moveLog, this.chessBoard.currentPlayer().getLeague()));
    }

    public static final class MoveLog {

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
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void drawTile(final int index) {
        int tileColor;
        if (BoardUtils.FIRST_ROW[index] || BoardUtils.THIRD_ROW[index] || BoardUtils.FIFTH_ROW[index] || BoardUtils.SEVENTH_ROW[index]) {
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

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void highlightMove(final Board board) {
        int tileColor;
        for (final Move move : this.pieceLegalMoves(board)) {
            final int coordinate = move.getDestinationCoordinate();
            if (move.isAttack() || move instanceof PawnPromotion && ((PawnPromotion)move).getDecoratedMove().isAttack()) {
                //dark red
                this.tilesView[coordinate].setBackgroundColor(Color.rgb(204, 0, 0));
            } else {
                if (BoardUtils.FIRST_ROW[coordinate] ||
                        BoardUtils.THIRD_ROW[coordinate] ||
                        BoardUtils.FIFTH_ROW[coordinate] ||
                        BoardUtils.SEVENTH_ROW[coordinate]) {
                    tileColor = (coordinate % 2 == 0 ? this.legalMovesLightTileColor : this.legalMovesDarkTileColor);
                } else {
                    tileColor = (coordinate % 2 != 0 ? this.legalMovesLightTileColor : this.legalMovesDarkTileColor);
                }
                this.tilesView[coordinate].setBackgroundColor(tileColor);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private int initTileView(final int index, final View view) {
        view.setOnClickListener(V-> {
            try {
                if (this.humanMovePiece == null) {
                    this.humanMovePiece = this.chessBoard.getTile(index).getPiece();
                    if (this.humanMovePiece.getLeague() != this.chessBoard.currentPlayer().getLeague()) {
                        this.humanMovePiece = null;
                    }
                    this.highlightMove(this.chessBoard);
                } else {
                    final Move move = MoveFactory.createMove(this.chessBoard, this.humanMovePiece, this.humanMovePiece.getPiecePosition(), index);
                    final MoveTransition transition = this.chessBoard.currentPlayer().makeMove(move);
                    if (transition.getMoveStatus().isDone()) {
                        this.chessBoard = transition.getLatestBoard();
                        if (move instanceof PawnPromotion) {
                            ((PawnPromotion)move).setContext(this).startPromotion();
                        }
                        this.moveLog.addMove(move);
                        this.updateUI(move);
                        if (this.AIThinking) {
                            this.artificialIntelligence.execute(this.AILevelSpinner.getSelectedItemPosition());
                        }
                    }
                    this.humanMovePiece = null;
                    this.drawBoard();
                }
            } catch (final NullPointerException ignored) {}
        });
        this.tilesView[index] = (ImageView)view;
        this.drawTile(index);
        return index + 1;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
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
}