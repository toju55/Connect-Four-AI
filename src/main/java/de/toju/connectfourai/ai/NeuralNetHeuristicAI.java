package de.toju.connectfourai.ai;

import de.toju.connectfourai.model.Board;
import de.toju.connectfourai.model.Player;
import org.springframework.stereotype.Component;

@Component
public class NeuralNetHeuristicAI extends NeuralNetAI {

    @Override
    public void train(int[] board, int move, Player player, double result) {
        // Compute heuristic reward instead of simple win/loss
        double reward = computeHeuristicReward(board, player, result);

        // Call original training logic using this reward
        super.train(board, move, player, reward);
    }

    private double computeHeuristicReward(int[] board, Player player, double result) {
        // Beispiel: direkter Gewinn bekommt maximalen Reward
        if (result == 1.0 && player == Player.PLAYER1) return 1.0;
        if (result == 1.0 && player == Player.PLAYER2) return 1.0;
        if (result == 0.0) return 0.0; // draw

        Board realBoard = Board.fromFlatArray(board);

        // Heuristische Bewertung des Boardzustands
        double reward = 0;
        reward += 0.6 * realBoard.countRowsOf(player, 3);  // 3-in-a-row
        reward += 0.3 * realBoard.countRowsOf(player, 2);  // 2-in-a-row
        reward += 0.1 * realBoard.countCenterControl(player);

        Player opponent = player.opposite();
        reward -= 0.6 * realBoard.countRowsOf(opponent, 3);
        reward -= 0.3 * realBoard.countRowsOf(opponent, 2);
        reward -= 0.1 * realBoard.countCenterControl(opponent);

        // Clamp zwischen -1 und 1
        return Math.max(-1, Math.min(1, reward));
    }
}