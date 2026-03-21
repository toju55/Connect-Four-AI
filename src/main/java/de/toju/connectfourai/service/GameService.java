package de.toju.connectfourai.service;

import de.toju.connectfourai.model.Board;
import de.toju.connectfourai.model.Player;
import lombok.Getter;
import org.springframework.stereotype.Service;

@Service
@Getter
public class GameService {

    private Board board = new Board();
    private Player currentPlayer = Player.HUMAN;

    public boolean playMove(int col) {
        boolean success = board.makeMove(col, currentPlayer);

        if (success) {
            currentPlayer = currentPlayer.opposite();
        }

        return success;
    }

    public void reset() {
        this.board = new Board();
        this.currentPlayer = Player.HUMAN;
    }

    public Player getWinner() {
        return board.checkWinner();
    }

    public boolean isGameOver() {
        return getWinner() != null || board.isFull();
    }
}