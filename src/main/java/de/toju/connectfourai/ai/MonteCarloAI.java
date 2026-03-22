package de.toju.connectfourai.ai;

import de.toju.connectfourai.model.Board;
import de.toju.connectfourai.model.Player;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class MonteCarloAI implements AIPlayer {

    private static final int SIMULATIONS = 500;
    private final Random random = new Random();

    @Override
    public int chooseMove(Board board, Player player) {
        List<Integer> moves = getAvailableMoves(board);
        int bestMove = moves.get(0);
        double bestScore = Double.NEGATIVE_INFINITY;

        for (int col : moves) {
            double totalScore = 0;
            for (int i = 0; i < SIMULATIONS; i++) {
                Board copy = board.copy();
                copy.makeMove(col, player);
                totalScore += simulateRandomGame(copy, player);
            }
            if (totalScore > bestScore) {
                bestScore = totalScore;
                bestMove = col;
            }
        }
        return bestMove;
    }

    private double simulateRandomGame(Board board, Player aiPlayer) {
        Player current = aiPlayer.opposite();
        while (board.checkWinner() == null && !board.isFull()) {
            List<Integer> moves = getAvailableMoves(board);
            int move = moves.get(random.nextInt(moves.size()));
            board.makeMove(move, current);
            current = current.opposite();
        }
        Player winner = board.checkWinner();
        if (winner == aiPlayer) return 1.0;
        if (winner == aiPlayer.opposite()) return -1.0;
        return 0; // draw
    }

    private List<Integer> getAvailableMoves(Board board) {
        List<Integer> moves = new ArrayList<>();
        for (int c = 0; c < board.getCols(); c++) {
            if (!board.isColumnFull(c)) moves.add(c);
        }
        return moves;
    }
}