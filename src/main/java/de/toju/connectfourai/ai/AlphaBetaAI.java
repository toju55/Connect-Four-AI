package de.toju.connectfourai.ai;

import de.toju.connectfourai.model.Board;
import de.toju.connectfourai.model.Player;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class AlphaBetaAI implements AIPlayer {

    private static final int MAX_DEPTH = 5;

    @Override
    public int chooseMove(Board board, Player player) {
        int bestScore = Integer.MIN_VALUE;
        int bestMove = 0;

        for (int col : getAvailableMoves(board)) {
            Board copy = board.copy();
            copy.makeMove(col, player);
            int score = alphaBeta(copy, MAX_DEPTH - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false, player);
            if (score > bestScore) {
                bestScore = score;
                bestMove = col;
            }
        }

        return bestMove;
    }

    private int alphaBeta(Board board, int depth, int alpha, int beta, boolean maximizing, Player aiPlayer) {
        Player winner = board.checkWinner();
        if (winner == aiPlayer) return 1000;
        if (winner == aiPlayer.opposite()) return -1000;
        if (board.isFull() || depth == 0) return evaluate(board, aiPlayer);

        if (maximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (int col : getAvailableMoves(board)) {
                Board copy = board.copy();
                copy.makeMove(col, aiPlayer);
                int eval = alphaBeta(copy, depth - 1, alpha, beta, false, aiPlayer);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break; // prune
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            Player opponent = aiPlayer.opposite();
            for (int col : getAvailableMoves(board)) {
                Board copy = board.copy();
                copy.makeMove(col, opponent);
                int eval = alphaBeta(copy, depth - 1, alpha, beta, true, aiPlayer);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break; // prune
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

    private int evaluate(Board board, Player player) {
        // Use same heuristic as your HeuristicAI
        return WeightedHeuristicAI.evaluateStatic(board, player);
    }
}