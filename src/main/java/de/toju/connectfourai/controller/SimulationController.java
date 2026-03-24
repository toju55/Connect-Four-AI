package de.toju.connectfourai.controller;

import de.toju.connectfourai.model.PlayerType;
import de.toju.connectfourai.model.SimulationResult;
import de.toju.connectfourai.service.SimulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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

    @GetMapping("/simulateRandom")
    public String simulateRandom(@RequestParam(defaultValue = "100") int numMatches, Model model) {
        simulationService.simulateRandomMatchesAsync(numMatches);
        return "redirect:/eloRanking";
    }

    @GetMapping("/simulateRoundRobin")
    public String simulateRoundRobin(@RequestParam(defaultValue = "1") int numRounds, Model model) {
        simulationService.simulateRoundRobinAsync(numRounds);
        return "redirect:/eloRanking";
    }

    @GetMapping("/eloRanking")
    public String tournamentStatus(Model model) {
        model.addAttribute("running", simulationService.isSimulationRunning());

        SimulationResult simulationResult = simulationService.getLastSimulationResult();
        if (simulationResult != null) {
            model.addAttribute("simulationResult", simulationResult);
            model.addAttribute("ais", Arrays.stream(PlayerType.values()).filter(pt -> !pt.isHuman()).map(Enum::name).toList());
        }

        return "eloRanking";
    }

    @GetMapping("/train")
    public String train(Model model) {

        List<String> trainableAis = new ArrayList<>();
        trainableAis.add(PlayerType.NEURAL_NET_AI.name());
        trainableAis.add(PlayerType.NEURAL_NET_2_LAYERS_AI.name());

        model.addAttribute("trainableAis", trainableAis);

        return "train";

    }

    @GetMapping("/train-async")
    public String trainAsync(@RequestParam(defaultValue = "1000") int numRounds, @RequestParam String ai) {

        simulationService.trainAiAsync(PlayerType.valueOf(ai), numRounds);

        return "redirect:/simulationStatus";
    }

    @GetMapping("/simulationStatus")
    public String simulationStatus(Model model) {

        model.addAttribute("running", simulationService.isSimulationRunning());

        return "simulationStatus";
    }

    @GetMapping("/simulationStatus/json")
    @ResponseBody
    public Map<String, Boolean> simulationStatusJson() {
        return Map.of("running", simulationService.isSimulationRunning());
    }
}