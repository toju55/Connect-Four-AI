package de.toju.connectfourai.ai;

import de.toju.connectfourai.model.Board;
import de.toju.connectfourai.model.Player;
import org.springframework.stereotype.Component;

@Component
public class WeightedHeuristicAI implements AIPlayer {

    private static final int WIN_WEIGHT = 1000;
    private static final int THREE_WEIGHT = 50;
    private static final int TWO_WEIGHT = 10;
    private static final int CENTER_WEIGHT = 5;

    @Override
    public int chooseMove(Board board, Player player) {
        int bestMove = 0;
        int bestScore = Integer.MIN_VALUE;

        for (int col = 0; col < board.getCols(); col++) {
            if (board.isColumnFull(col)) continue;
            Board copy = board.copy();
            copy.makeMove(col, player);
            int score = evaluateStatic(copy, player);
            if (score > bestScore) {
                bestScore = score;
                bestMove = col;
            }
        }

        return bestMove;
    }

    /** Static evaluation so HybridAI can reuse */
    public static int evaluateStatic(Board board, Player player) {
        int score = 0;
        score += WIN_WEIGHT * board.countPotentialWins(player);
        score += THREE_WEIGHT * board.countRowsOf(player, 3);
        score += TWO_WEIGHT * board.countRowsOf(player, 2);
        score += CENTER_WEIGHT * board.countCenterControl(player);

        Player opponent = player.opposite();
        score -= WIN_WEIGHT * board.countPotentialWins(opponent);
        score -= THREE_WEIGHT * board.countRowsOf(opponent, 3);
        score -= TWO_WEIGHT * board.countRowsOf(opponent, 2);
        score -= CENTER_WEIGHT * board.countCenterControl(opponent);

        return score;
    }
}