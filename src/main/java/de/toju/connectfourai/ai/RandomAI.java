package de.toju.connectfourai.ai;

import de.toju.connectfourai.model.Board;
import de.toju.connectfourai.model.Player;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class RandomAI implements AIPlayer {

    private final Random random = new Random();

    @Override
    public int chooseMove(Board board, Player player) {
        int col;

        do {
            col = random.nextInt(board.getCols());
        } while (!board.isValidMove(col));

        return col;
    }
}