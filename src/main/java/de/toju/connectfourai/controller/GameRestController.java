package de.toju.connectfourai.controller;

import de.toju.connectfourai.ai.AIPlayer;
import de.toju.connectfourai.model.Board;
import de.toju.connectfourai.model.Player;
import de.toju.connectfourai.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
public class GameRestController {

    private final GameService gameService;
    private final AIPlayer aiPlayer;

    @PostMapping("/move/{col}")
    public Board makeMove(@PathVariable int col) {

        // Mensch
        if (gameService.getCurrentPlayer() == Player.HUMAN) {
            gameService.playMove(col);
        }

        // KI
        if (gameService.getCurrentPlayer() == Player.AI) {
            int aiMove = aiPlayer.chooseMove(
                    gameService.getBoard(),
                    Player.AI
            );
            gameService.playMove(aiMove);
        }

        return gameService.getBoard();
    }

    @GetMapping
    public Board getBoard() {
        return gameService.getBoard();
    }
}