package de.toju.connectfourai.controller;

import de.toju.connectfourai.ai.AIPlayer;
import de.toju.connectfourai.model.PlayerType;
import de.toju.connectfourai.service.SimulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class SimulationController {

    private final SimulationService simulationService;

    @GetMapping("/simulate")
    public String simulationPage(Model model) {
        model.addAttribute("aiPlayerTypes", Arrays.stream(PlayerType.values()).filter(pt -> !pt.isHuman()).toList());
        return "simulate";
    }

    @PostMapping("/simulate")
    public String runSimulation(@RequestParam String ai1,
                                @RequestParam String ai2,
                                @RequestParam int numGames,
                                Model model) {

        Map<String, Integer> results = simulationService.simulateGames(PlayerType.valueOf(ai1), PlayerType.valueOf(ai2), numGames);

        model.addAttribute("results", results);
        model.addAttribute("ai1", ai1);
        model.addAttribute("ai2", ai2);
        model.addAttribute("numGames", numGames);
        return "simulationResults";
    }

    @GetMapping("/simulateElo")
    public String simulateElo(@RequestParam(defaultValue = "100") int numMatches, Model model) {
        List<SimulationService.AIRanking> rankings = simulationService.simulateRandomMatches(numMatches);
        model.addAttribute("rankings", rankings);
        return "eloRanking";
    }}