package de.toju.connectfourai.controller;

import de.toju.connectfourai.model.Board;
import de.toju.connectfourai.persistence.GameEntity;
import de.toju.connectfourai.persistence.GameEntityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/replay")
public class ReplayController {

    private final GameEntityRepository gameEntityRepository;

    public ReplayController(GameEntityRepository gameEntityRepository) {
        this.gameEntityRepository = gameEntityRepository;
    }

    @GetMapping
    public String listReplays(Model model, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Page<GameEntity> gameEntityPage = gameEntityRepository.findAll(PageRequest.of(page, size));

        model.addAttribute("games", gameEntityPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", gameEntityPage.getTotalPages());
        model.addAttribute("pageSize", size);

        return "replayList";
    }

    @GetMapping("/{id}")
    public String viewReplay(@PathVariable Long id, Model model) {
        GameEntity game = gameEntityRepository.findById(id).orElseThrow();
        model.addAttribute("game", game);

        // prepare initial empty board
        Board board = new Board();
        model.addAttribute("replayBoard", board);

        // Moves as list of integers
        List<Integer> moves = Arrays.stream(game.getMoves().split(""))
                                    .map(Integer::parseInt)
                                    .collect(Collectors.toList());
        model.addAttribute("moves", moves);
        model.addAttribute("currentMoveIndex", 0);

        return "replayViewer";
    }
}