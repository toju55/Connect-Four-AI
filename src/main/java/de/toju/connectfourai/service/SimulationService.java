package de.toju.connectfourai.service;

import de.toju.connectfourai.ai.AIPlayer;
import de.toju.connectfourai.ai.AlphaBetaAI;
import de.toju.connectfourai.ai.HeuristicAI;
import de.toju.connectfourai.ai.HybridAI;
import de.toju.connectfourai.ai.MCTSAI;
import de.toju.connectfourai.ai.MinimaxAI;
import de.toju.connectfourai.ai.MinimaxHeuristicAI;
import de.toju.connectfourai.ai.MonteCarloAI;
import de.toju.connectfourai.ai.NeuralNet2LayerAI;
import de.toju.connectfourai.ai.NeuralNet2LayerHeuristicAI;
import de.toju.connectfourai.ai.NeuralNetAI;
import de.toju.connectfourai.ai.NeuralNetHeuristicAI;
import de.toju.connectfourai.ai.RandomAI;
import de.toju.connectfourai.ai.TrainableAIPlayer;
import de.toju.connectfourai.ai.WeightedHeuristicAI;
import de.toju.connectfourai.model.AIRanking;
import de.toju.connectfourai.model.Board;
import de.toju.connectfourai.model.MatchStats;
import de.toju.connectfourai.model.Player;
import de.toju.connectfourai.model.PlayerType;
import de.toju.connectfourai.model.SimulationResult;
import de.toju.connectfourai.persistence.GameEntity;
import de.toju.connectfourai.persistence.GameEntityRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class SimulationService {

    private final Map<PlayerType, AIPlayer> aiMap = new HashMap<>();

    private final Random random = new Random();


    @Getter
    private volatile boolean simulationRunning = false;

    @Getter
    private volatile SimulationResult lastSimulationResult;

    private final GameEntityRepository gameEntityRepository;

    // Register AI players by type
    public SimulationService(GameEntityRepository gameEntityRepository) {
        this.gameEntityRepository = gameEntityRepository;
        initAiMap();
    }

    /**
     * Simulate 'numGames' between two specific AIs
     */
    public Map<String, Integer> simulateGames(PlayerType ai1Type, PlayerType ai2Type, int numGames) {
        AIPlayer ai1 = aiMap.get(ai1Type);
        AIPlayer ai2 = aiMap.get(ai2Type);

        Map<String, Integer> results = new HashMap<>();
        results.put("AI1Wins", 0);
        results.put("AI2Wins", 0);
        results.put("Draws", 0);


        for (int i = 0; i < numGames; i++) {
            Board board = new Board();
            StringBuilder moveNotationBuilder = new StringBuilder();
            List<BoardState> p1Moves = new ArrayList<>();
            List<BoardState> p2Moves = new ArrayList<>();

            Player current = Player.PLAYER1;

            while (board.checkWinner() == null && !board.isFull()) {
                int move = current == Player.PLAYER1
                        ? ai1.chooseMove(board, current)
                        : ai2.chooseMove(board, current);

                board.makeMove(move, current);

                moveNotationBuilder.append(move + 1);

                // Remember move for training if NeuralNet is involved
                rememberMove(current, ai1Type, ai2Type, move, p1Moves, p2Moves, board);

                current = current.opposite();
            }

            int winnerForEntity;

            Player winner = board.checkWinner();
            if (winner == Player.PLAYER1) {
                results.put("AI1Wins", results.get("AI1Wins") + 1);
                winnerForEntity = 1;
            }
            else if (winner == Player.PLAYER2) {
                results.put("AI2Wins", results.get("AI2Wins") + 1);
                winnerForEntity = 2;
            }
            else {
                results.put("Draws", results.get("Draws") + 1);
                winnerForEntity = 0;
            }

            GameEntity gameEntity = new GameEntity(ai1Type, ai2Type, winnerForEntity, moveNotationBuilder.toString());
            gameEntityRepository.save(gameEntity);

            // Train NeuralNetAIs after the game
            trainNeuralNetGeneric(p1Moves, winner, Player.PLAYER1, aiMap.get(ai1Type));
            trainNeuralNetGeneric(p2Moves, winner, Player.PLAYER2, aiMap.get(ai2Type));
        }

        return results;
    }

    @Async
    public CompletableFuture<Void> simulateRandomMatchesAsync(int numGamesPerMatch) {
        simulationRunning = true;

        try {
            // Ergebnisse persistent im Service halten
            lastSimulationResult = simulateRandomMatches(numGamesPerMatch);
        } finally {
            simulationRunning = false;
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Simulate 'numGames' random matches between all registered AIs and return ELO ranking.
     */
    public SimulationResult simulateRandomMatches(int numGamesPerMatch) {

        // Initialize stats and ELO
        Map<PlayerType, AIRanking> rankingMap = new HashMap<>();
        aiMap.keySet().forEach(type -> rankingMap.put(type, new AIRanking(type, 1500)));

        Map<String, Map<String, MatchStats>> matchStats = initMatchStats();

        List<PlayerType> ais = new ArrayList<>(aiMap.keySet());

        for (int i = 0; i < numGamesPerMatch; i++) {
            // Pick first AI randomly
            PlayerType p1 = ais.get(random.nextInt(ais.size()));

            // Pick second AI randomly, different from first
            PlayerType p2;
            do {
                p2 = ais.get(random.nextInt(ais.size()));
            } while (p2 == p1);

            playTournamentGame(p1, p2, matchStats, rankingMap);
        }

        List<AIRanking> rankings = new ArrayList<>(rankingMap.values());
        rankings.sort(Comparator.comparingInt(AIRanking::getElo).reversed());

        return new SimulationResult(rankings, matchStats);
    }

    @Async
    public CompletableFuture<Void> simulateRoundRobinAsync(int numRounds) {
        simulationRunning = true;

        try {
            // Ergebnisse persistent im Service halten
            lastSimulationResult = simulateRoundRobin(numRounds);
        } finally {
            simulationRunning = false;
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Simulate 'numGames' random matches between all registered AIs and return ELO ranking.
     */
    public SimulationResult simulateRoundRobin(int numRounds) {

        // Initialize stats and ELO
        Map<PlayerType, AIRanking> rankingMap = new HashMap<>();
        aiMap.keySet().forEach(type -> rankingMap.put(type, new AIRanking(type, 1500)));

        Map<String, Map<String, MatchStats>> matchStats = initMatchStats();

        List<PlayerType> ais = new ArrayList<>(aiMap.keySet());

        for (int i = 0; i < numRounds; i++) {
            for (PlayerType p1 : ais) {
                for (PlayerType p2 : ais) {
                    if (p1 == p2) {
                        continue;
                    }

                    playTournamentGame(p1, p2, matchStats, rankingMap);
                }

            }
        }

        List<AIRanking> rankings = new ArrayList<>(rankingMap.values());
        rankings.sort(Comparator.comparingInt(AIRanking::getElo).reversed());

        return new SimulationResult(rankings, matchStats);
    }

    @Async
    public CompletableFuture<Void> trainAiAsync(PlayerType ai, int numGames) {
        simulationRunning = true;

        try {
            int nextLogAt = 10;

            for (int i = 1; i <= numGames; i++) {
                List<PlayerType> opponents = aiMap.keySet().stream()
                        .filter(p -> p != ai) // exclude self
                        .toList();

                PlayerType randomAi = opponents.get(random.nextInt(opponents.size()));
                PlayerType player1, player2;

                if (i % 2 == 0) {
                    player1 = ai;
                    player2 = randomAi;
                } else {
                    player1 = randomAi;
                    player2 = ai;
                }

                simulateGames(player1, player2, 1);

                if (i == nextLogAt) {
                    System.out.println("Training progress: " + i + "/" + numGames);

                    if (nextLogAt < 100) {
                        nextLogAt += 10;       // 10,20,...,100
                    } else if (nextLogAt < 1000) {
                        nextLogAt += 100;      // 100,200,...,1000
                    } else if (nextLogAt < 10000) {
                        nextLogAt += 1000;      // 1000,2000,...,10000
                    } else {
                        nextLogAt += 10000;     // 10000,20000,...
                    }
                }            }
        } finally {
            simulationRunning = false;
        }
        return CompletableFuture.completedFuture(null);
    }

    private void playTournamentGame(PlayerType p1, PlayerType p2, Map<String, Map<String, MatchStats>> matchStats, Map<PlayerType, AIRanking> rankingMap) {

        Map<String, Integer> result = simulateGames(p1, p2, 1); // 1 game per random match

        MatchStats matchStats1 = matchStats.get(p1.name()).get(p2.name());
        MatchStats matchStats2 = matchStats.get(p2.name()).get(p1.name());

        AIRanking r1 = rankingMap.get(p1);
        AIRanking r2 = rankingMap.get(p2);

        int s1, s2;
        if (result.get("AI1Wins") == 1) {
            s1 = 1;
            s2 = 0;
            matchStats1.recordWin();
            matchStats2.recordLoss();
        } else if (result.get("AI2Wins") == 1) {
            s1 = 0;
            s2 = 1;
            matchStats1.recordLoss();
            matchStats2.recordWin();
        } else { // Draw
            s1 = 0;
            s2 = 0;
            matchStats1.recordDraw();
            matchStats2.recordDraw();
        }

        r1.recordResult(s1, s2); // wins, losses
        r2.recordResult(s2, s1);

        // Update ELO
        updateElo(r1, r2, s1, s2);
    }

    private void updateElo(AIRanking r1, AIRanking r2, int s1, int s2) {
        double k = 32.0;

        double e1 = 1.0 / (1 + Math.pow(10, (r2.getElo() - r1.getElo()) / 400.0));
        double e2 = 1.0 / (1 + Math.pow(10, (r1.getElo() - r2.getElo()) / 400.0));

        double newElo1 = r1.getElo() + k * (s1 - e1);
        double newElo2 = r2.getElo() + k * (s2 - e2);

        int roundedElo1 = (int) Math.floor(newElo1);
        int roundedElo2 = (int) Math.ceil(newElo2);

        r1.setElo(roundedElo1);
        r2.setElo(roundedElo2);
    }

    private void initAiMap() {
        for (PlayerType ai : PlayerType.values()) {
            if (ai.isHuman()) {
                continue;
            }

            switch (ai) {
                case ALPHA_BETA_AI -> aiMap.put(ai, new AlphaBetaAI());
                case HEURISTIC_AI -> aiMap.put(ai, new HeuristicAI());
                case HYBRID_AI -> aiMap.put(ai, new HybridAI());
                case MCTS_AI -> aiMap.put(ai, new MCTSAI());
                case MINIMAX_AI -> aiMap.put(ai, new MinimaxAI());
                case MINIMAX_HEURISTIC_AI -> aiMap.put(ai, new MinimaxHeuristicAI());
                case MONTE_CARLO_AI -> aiMap.put(ai, new MonteCarloAI());
                case NEURAL_NET_2_LAYERS_AI -> aiMap.put(ai, new NeuralNet2LayerAI());
                case NEURAL_NET_2_LAYER_HEURISTIC_AI -> aiMap.put(ai, new NeuralNet2LayerHeuristicAI());
                case NEURAL_NET_AI -> aiMap.put(ai, new NeuralNetAI());
                case NEURAL_NET_HEURISTIC_AI -> aiMap.put(ai, new NeuralNetHeuristicAI());
                case RANDOM_AI -> aiMap.put(ai, new RandomAI());
                case WEIGHTED_HEURISTIC_AI -> aiMap.put(ai, new WeightedHeuristicAI());
            }
        }
    }

    private Map<String, Map<String, MatchStats>> initMatchStats() {
        Map<String, Map<String, MatchStats>> matchStats = new HashMap<>();
        List<PlayerType> ais = Arrays.stream(PlayerType.values())
                .filter(pt -> !pt.isHuman())
                .toList();

        for (PlayerType p1 : ais) {
            Map<String, MatchStats> row = new HashMap<>();
            for (PlayerType p2 : ais) {
                if (!p1.equals(p2)) {
                    row.put(p2.name(), new MatchStats()); // 0-0-0 initial
                }
            }
            matchStats.put(p1.name(), row);
        }

        return matchStats;
    }

    private void trainNeuralNetGeneric(List<BoardState> moves, Player winner, Player player, AIPlayer ai) {
        // Determine reward: 1 for win, 0 for draw, -1 for loss
        double reward;
        if (winner == player) reward = 1.0;
        else if (winner == null) reward = 0;
        else reward = -1.0;

        for (BoardState state : moves) {
            // Only train on moves made by the current player
            if (state.player != player) continue;

            int move = state.lastMove;
            if (move < 0 || move >= 7) {
                // Invalid move, skip training and print debug
                System.err.println("Skipping training for invalid move: " + move + ", player: " + player + ", AIPlayer: " + ai);
                continue;
            }

            // Call the AI-specific train method
            if (ai instanceof TrainableAIPlayer trainable) {
                trainable.train(state.flatBoard, move, state.player, reward);
            } else {
                System.err.println("Unknown AI type for training: " + ai.getClass());
            }
        }
    }

    private void rememberMove(Player current,
                              PlayerType ai1Type,
                              PlayerType ai2Type,
                              int move,
                              List<BoardState> p1Moves,
                              List<BoardState> p2Moves,
                              Board board) {

        PlayerType currentAI =
                (current == Player.PLAYER1) ? ai1Type : ai2Type;

        AIPlayer ai = aiMap.get(currentAI);

        if (ai instanceof TrainableAIPlayer) {
            if (move >= 0) {
                BoardState state = new BoardState(board.toFlatArray(current), current, move);

                if (current == Player.PLAYER1) p1Moves.add(state);
                else p2Moves.add(state);
            }
        }
    }

    // Helper classes
    public record BoardState(int[] flatBoard, Player player, int lastMove) {}
}