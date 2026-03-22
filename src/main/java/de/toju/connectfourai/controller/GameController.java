package de.toju.connectfourai.controller;

import de.toju.connectfourai.model.PlayerType;
import de.toju.connectfourai.service.GameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@Controller
@RequiredArgsConstructor
@Slf4j
public class GameController {

    private final GameService gameService;

    @GetMapping("/")
    public String index(Model model) {
        return "index";
    }

    @GetMapping("/selectPlayers")
    public String selectPlayers(Model model) {
        model.addAttribute("playerTypes", PlayerType.values());
        return "selectPlayers";
    }

    @PostMapping("/start")
    public String startGame(@RequestParam PlayerType player1, @RequestParam PlayerType player2) {
        gameService.startNewGame(player1, player2);
        return "redirect:/game";
    }

    @GetMapping("/game")
    public String game(Model model) {
        model.addAttribute("board", gameService.getBoard());
        model.addAttribute("currentPlayer", gameService.getCurrentPlayer());
        model.addAttribute("currentPlayerType", gameService.getCurrentPlayerType());
        model.addAttribute("winner", gameService.getWinner());
        model.addAttribute("winnerType",  gameService.getWinnerType());
        model.addAttribute("boardFull", gameService.getBoard().isFull());
        model.addAttribute("gameOver", gameService.isGameOver());
        return "game";
    }

    @GetMapping("/move")
    public String makeMove(@RequestParam int col) {
        if (gameService.getCurrentPlayerType() == PlayerType.HUMAN) {
            gameService.playMove(col);
        }
        return "redirect:/game";
    }

    @GetMapping("/nextMove")
    public String nextMove() {
        gameService.playNextMove();
        return "redirect:/game";
    }

    @GetMapping("/reset")
    public String reset() {
        gameService.reset();
        return "redirect:/";
    }
}