package de.toju.connectfourai.ai;

import de.toju.connectfourai.model.Board;
import de.toju.connectfourai.model.Player;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class HeuristicAI implements AIPlayer {

    private final Random random = new Random();

    @Override
    public int chooseMove(Board board, Player player) {
        int cols = board.getCols();

        // Check for a winning move for AI
        for (int c = 0; c < cols; c++) {
            if (board.isColumnFull(c)) continue;

            Board copy = board.copy();
            copy.makeMove(c, player);

            if (copy.checkWinner() == player) {
                return c; // Play winning move
            }
        }

        // Check for opponent's winning move → block it
        Player opponent = player.opposite();
        for (int c = 0; c < cols; c++) {
            if (board.isColumnFull(c)) continue;

            Board copy = board.copy();
            copy.makeMove(c, opponent);

            if (copy.checkWinner() == opponent) {
                return c; // Block opponent
            }
        }

        // Otherwise, pick a random available column
        List<Integer> available = new ArrayList<>();
        for (int c = 0; c < cols; c++) {
            if (!board.isColumnFull(c)) {
                available.add(c);
            }
        }

        return available.get(random.nextInt(available.size()));
    }
}