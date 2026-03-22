package de.toju.connectfourai.ai;

import de.toju.connectfourai.model.Board;
import de.toju.connectfourai.model.Player;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class HybridAI implements AIPlayer {

    private static final int MAX_DEPTH = 3; // shallow minimax for performance

    @Override
    public int chooseMove(Board board, Player player) {
        int bestMove = 0;
        int bestScore = Integer.MIN_VALUE;

        for (int col : getAvailableMoves(board)) {
            Board copy = board.copy();
            copy.makeMove(col, player);
            int score = minimax(copy, MAX_DEPTH - 1, false, player);
            if (score > bestScore) {
                bestScore = score;
                bestMove = col;
            }
        }

        return bestMove;
    }

    private int minimax(Board board, int depth, boolean maximizing, Player aiPlayer) {
        Player winner = board.checkWinner();
        if (winner == aiPlayer) return 1000;
        if (winner == aiPlayer.opposite()) return -1000;
        if (board.isFull() || depth == 0) return WeightedHeuristicAI.evaluateStatic(board, aiPlayer);

        if (maximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (int col : getAvailableMoves(board)) {
                Board copy = board.copy();
                copy.makeMove(col, aiPlayer);
                int eval = minimax(copy, depth - 1, false, aiPlayer);
                maxEval = Math.max(maxEval, eval);
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            Player opponent = aiPlayer.opposite();
            for (int col : getAvailableMoves(board)) {
                Board copy = board.copy();
                copy.makeMove(col, opponent);
                int eval = minimax(copy, depth - 1, true, aiPlayer);
                minEval = Math.min(minEval, eval);
            }
            return minEval;
        }
    }

    private List<Integer> getAvailableMoves(Board board) {
        List<Integer> moves = new ArrayList<>();
        for (int c = 0; c < board.getCols(); c++) {
            if (!board.isColumnFull(c)) moves.add(c);
        }
        return moves;
    }
}