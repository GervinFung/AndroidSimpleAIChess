package com.example.chess.engine.player.ArtificialIntelligence;

import com.example.chess.engine.board.Board;
import com.example.chess.engine.board.Move;
import com.example.chess.engine.player.MoveTransition;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.TimeUnit;

public final class MiniMax{

    private final StandardBoardEvaluation boardEvaluation;
    private final int searchDepth, nThreads;
    private int moveCount;

    public MiniMax(final int searchDepth) {
        this.boardEvaluation = new StandardBoardEvaluation();
        this.nThreads = Runtime.getRuntime().availableProcessors();
        if (this.nThreads > 4) {
            this.searchDepth = searchDepth + 1;
        } else {
            this.searchDepth = searchDepth;
        }
        this.moveCount = 0;
    }

    public Move execute(final Board board) {

        final long startTime = System.nanoTime();

        final AtomicReference<Move> bestMove = new AtomicReference<>();

        final AtomicReference<Integer> highestSeenValue = new AtomicReference<>(Integer.MIN_VALUE);
        final AtomicReference<Integer> lowestSeenValue = new AtomicReference<>(Integer.MAX_VALUE);
        final AtomicReference<Integer> currentValue = new AtomicReference<>(0);

        final ExecutorService executorService = Executors.newFixedThreadPool(this.nThreads);

        for (final Move move : board.currentPlayer().getLegalMoves()) {

            final MoveTransition moveTransition = board.currentPlayer().makeMove(move);

            executorService.execute(() -> {
                if (moveTransition.getMoveStatus().isDone()) {

                    final int currentVal = board.currentPlayer().getLeague().isWhite() ? min(moveTransition.getLatestBoard(), searchDepth, Integer.MIN_VALUE, Integer.MAX_VALUE) :
                            max(moveTransition.getLatestBoard(), searchDepth, Integer.MIN_VALUE, Integer.MAX_VALUE);
                    currentValue.set(currentVal);
                    this.moveCount++;
                    if (board.currentPlayer().getLeague().isWhite() && currentValue.get() > highestSeenValue.get()) {
                        highestSeenValue.set(currentValue.get());
                        bestMove.set(move);

                    } else if (board.currentPlayer().getLeague().isBlack() && currentValue.get() < lowestSeenValue.get()) {
                        lowestSeenValue.set(currentValue.get());
                        bestMove.set(move);
                    }
                }
            });
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Time taken to search best move: " + (System.nanoTime() - startTime) + " nanoseconds");
        return bestMove.get();
    }

    public int getMoveCount() {
        return this.moveCount;
    }

    private static boolean isEndGameScenario(final Board board) {
        return board.currentPlayer().isInCheckmate() ||
                board.currentPlayer().isInStalemate();
    }

    private int min(final Board board, final int depth, final int alpha, int beta) {

        if(depth == 0 || isEndGameScenario(board)) {
            return this.boardEvaluation.evaluate(board, depth);
        }
        int lowestSeenValue = Integer.MAX_VALUE;
        for (final Move move : board.currentPlayer().getLegalMoves()) {

            final MoveTransition moveTransition = board.currentPlayer().makeMove(move);

            if (moveTransition.getMoveStatus().isDone()) {
                final int currentValue = max(moveTransition.getLatestBoard(), depth - 1, alpha, beta);

                if (currentValue <= lowestSeenValue) {
                    lowestSeenValue = currentValue;
                }
                beta = Math.min(beta, currentValue);
                if (beta <= alpha) {
                    break;
                }
            }
        }
        return lowestSeenValue;
    }

    private int max(final Board board, final int depth, int alpha, final int beta) {

        if(depth == 0  || isEndGameScenario(board)) {
            return this.boardEvaluation.evaluate(board, depth);
        }
        int highestSeenValue = Integer.MIN_VALUE;
        for (final Move move : board.currentPlayer().getLegalMoves()) {

            final MoveTransition moveTransition = board.currentPlayer().makeMove(move);

            if (moveTransition.getMoveStatus().isDone()) {
                final int currentValue = min(moveTransition.getLatestBoard(), depth - 1, alpha, beta);

                if (currentValue >= highestSeenValue) {
                    highestSeenValue = currentValue;
                }
                alpha = Math.max(alpha, currentValue);
                if (beta <= alpha) {
                    break;
                }
            }
        }
        return highestSeenValue;
    }
}