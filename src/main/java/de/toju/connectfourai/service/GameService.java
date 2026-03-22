package de.toju.connectfourai.service;

import de.toju.connectfourai.ai.AIPlayer;
import de.toju.connectfourai.ai.HeuristicAI;
import de.toju.connectfourai.ai.RandomAI;
import de.toju.connectfourai.model.Board;
import de.toju.connectfourai.model.Player;
import de.toju.connectfourai.model.PlayerType;
import lombok.Getter;
import org.springframework.stereotype.Service;

@Service
@Getter
public class GameService {

    private Board board;
    private Player currentPlayer;

    private PlayerType player1Type;
    private PlayerType player2Type;

    private final AIPlayer randomAI;
    private final AIPlayer heuristicAI;

    public GameService(RandomAI randomAI, HeuristicAI heuristicAI) {
        this.randomAI = randomAI;
        this.heuristicAI = heuristicAI;
        reset();
    }

    public void playNextMove() {
        if (isGameOver()) return;

        PlayerType type = getCurrentPlayerType();

        if (type == PlayerType.HUMAN) return;

        AIPlayer ai = switch (type) {
            case RANDOM_AI -> randomAI;
            case HEURISTIC_AI -> heuristicAI;
            default -> null;
        };

        if (ai != null) {
            int move = ai.chooseMove(board, currentPlayer);
            playMove(move);
        }
    }

    public void playMove(int col) {
        if (isGameOver()) return;

        boolean success = board.makeMove(col, currentPlayer);

        if (success) {
            currentPlayer = currentPlayer.opposite();
        }
    }

    public void startNewGame(PlayerType player1Type, PlayerType player2Type) {
        this.board = new Board();
        this.currentPlayer = Player.PLAYER1;

        this.player1Type = player1Type;
        this.player2Type = player2Type;
    }

    public void reset() {
        this.board = new Board();
        this.currentPlayer = Player.PLAYER1;
    }

    public PlayerType getCurrentPlayerType() {
        return currentPlayer == Player.PLAYER1 ? player1Type : player2Type;
    }

    public Player getWinner() {
        return board.checkWinner();
    }

    public boolean isGameOver() {
        return getWinner() != null || board.isFull();
    }
}