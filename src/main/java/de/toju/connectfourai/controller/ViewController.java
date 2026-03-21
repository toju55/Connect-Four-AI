package de.toju.connectfourai.controller;

import de.toju.connectfourai.ai.AIPlayer;
import de.toju.connectfourai.model.Player;
import de.toju.connectfourai.service.GameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ViewController {

    private final GameService gameService;
    private final AIPlayer aiPlayer;

    @GetMapping("/")
    public String game(Model model) {
        model.addAttribute("board", gameService.getBoard());
        model.addAttribute("currentPlayer", gameService.getCurrentPlayer());
        model.addAttribute("winner", gameService.getWinner());
        model.addAttribute("boardFull", gameService.getBoard().isFull());
        model.addAttribute("gameOver", gameService.isGameOver());
        return "game";
    }

    @GetMapping("/move")
    public String makeMove(@RequestParam int col) {
        if (gameService.isGameOver()) {
            return "redirect:/";
        }

        if (gameService.getCurrentPlayer() == Player.HUMAN) {
            gameService.playMove(col);
        }

        if (gameService.isGameOver()) {
            return "redirect:/";
        }

        if (gameService.getCurrentPlayer() == Player.AI) {
            int aiMove = aiPlayer.chooseMove(
                    gameService.getBoard(),
                    Player.AI
            );
            gameService.playMove(aiMove);
        }

        return "redirect:/";
    }
    @GetMapping("/reset")
    public String reset() {
        gameService.reset();
        return "redirect:/";
    }
}