package de.toju.connectfourai.ai;

import de.toju.connectfourai.model.Board;
import de.toju.connectfourai.model.Player;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class MinimaxHeuristicAI implements AIPlayer {

    private static final int MAX_DEPTH = 4;

    @Override
    public int chooseMove(Board board, Player player) {
        int bestScore = Integer.MIN_VALUE;
        int bestMove = 0;

        for (int col : getAvailableMoves(board)) {
            Board copy = board.copy();
            copy.makeMove(col, player);

            int score = minimax(copy, MAX_DEPTH - 1, false, player);

            if (score > bestScore) {
                bestScore = score;
                bestMove = col;
            }
        }

        log.debug("MinimaxHeuristicAI chooses col {} with score {}", bestMove, bestScore);
        return bestMove;
    }

    private int minimax(Board board, int depth, boolean maximizing, Player aiPlayer) {

        Player winner = board.checkWinner();
        if (winner != null) {
            if (winner == aiPlayer) return 1000;
            else return -1000;
        }
        if (board.isFull() || depth == 0) {
            return evaluate(board, aiPlayer);
        }

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
            if (!board.isColumnFull(c)) {
                moves.add(c);
            }
        }
        return moves;
    }

    /**
     * Simple heuristic evaluation:
     * +100 for each 3-in-a-row AI, -100 for each 3-in-a-row opponent
     * +10 for each 2-in-a-row AI, -10 for each 2-in-a-row opponent
     */
    private int evaluate(Board board, Player player) {
        Player opponent = player.opposite();
        int score = 0;

        score += 100 * countNInARow(board, player, 3);
        score -= 100 * countNInARow(board, opponent, 3);

        score += 10 * countNInARow(board, player, 2);
        score -= 10 * countNInARow(board, opponent, 2);

        return score;
    }

    /**
     * Counts how many sequences of length `n` the player has on the board.
     */
    private int countNInARow(Board board, Player player, int n) {
        int count = 0;

        int rows = board.getRows();
        int cols = board.getCols();
        Player[][] grid = board.getGrid();

        // Horizontal
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c <= cols - n; c++) {
                boolean sequence = true;
                for (int i = 0; i < n; i++) {
                    if (grid[r][c + i] != player) sequence = false;
                }
                if (sequence) count++;
            }
        }

        // Vertical
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r <= rows - n; r++) {
                boolean sequence = true;
                for (int i = 0; i < n; i++) {
                    if (grid[r + i][c] != player) sequence = false;
                }
                if (sequence) count++;
            }
        }

        // Diagonal /
        for (int r = n - 1; r < rows; r++) {
            for (int c = 0; c <= cols - n; c++) {
                boolean sequence = true;
                for (int i = 0; i < n; i++) {
                    if (grid[r - i][c + i] != player) sequence = false;
                }
                if (sequence) count++;
            }
        }

        // Diagonal \
        for (int r = 0; r <= rows - n; r++) {
            for (int c = 0; c <= cols - n; c++) {
                boolean sequence = true;
                for (int i = 0; i < n; i++) {
                    if (grid[r + i][c + i] != player) sequence = false;
                }
                if (sequence) count++;
            }
        }

        return count;
    }
}