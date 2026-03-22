package de.toju.connectfourai.service;

import de.toju.connectfourai.ai.AIPlayer;
import de.toju.connectfourai.ai.AlphaBetaAI;
import de.toju.connectfourai.ai.HeuristicAI;
import de.toju.connectfourai.ai.HybridAI;
import de.toju.connectfourai.ai.MCTSAI;
import de.toju.connectfourai.ai.MinimaxAI;
import de.toju.connectfourai.ai.MinimaxHeuristicAI;
import de.toju.connectfourai.ai.MonteCarloAI;
import de.toju.connectfourai.ai.NeuralNetAI;
import de.toju.connectfourai.ai.RandomAI;
import de.toju.connectfourai.ai.WeightedHeuristicAI;
import de.toju.connectfourai.model.AIRanking;
import de.toju.connectfourai.model.Board;
import de.toju.connectfourai.model.MatchStats;
import de.toju.connectfourai.model.Player;
import de.toju.connectfourai.model.PlayerType;
import de.toju.connectfourai.model.SimulationResult;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SimulationService {

    private final Map<PlayerType, AIPlayer> aiMap = new HashMap<>();

    // Register AI players by type
    public SimulationService() {
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
            List<BoardState> p1Moves = new ArrayList<>();
            List<BoardState> p2Moves = new ArrayList<>();

            Player current = Player.PLAYER1;

            while (board.checkWinner() == null && !board.isFull()) {
                int move = current == Player.PLAYER1
                        ? ai1.chooseMove(board, current)
                        : ai2.chooseMove(board, current);

                board.makeMove(move, current);

                // Remember move for training if NeuralNetAI
                if (((current == Player.PLAYER1) && (ai1Type == PlayerType.NEURAL_NET_AI)) || ((current == Player.PLAYER2) && (ai2Type == PlayerType.NEURAL_NET_AI))) {
                    BoardState state = new BoardState(board.toFlatArray(current), current, move);
                    if (current == Player.PLAYER1) p1Moves.add(state);
                    else p2Moves.add(state);
                }

                current = current.opposite();
            }

            Player winner = board.checkWinner();
            if (winner == Player.PLAYER1) results.put("AI1Wins", results.get("AI1Wins") + 1);
            else if (winner == Player.PLAYER2) results.put("AI2Wins", results.get("AI2Wins") + 1);
            else results.put("Draws", results.get("Draws") + 1);

            // Train NeuralNetAIs after the game
            if (ai1Type == PlayerType.NEURAL_NET_AI) trainNeuralNet(p1Moves, winner, Player.PLAYER1);
            if (ai2Type == PlayerType.NEURAL_NET_AI) trainNeuralNet(p2Moves, winner, Player.PLAYER2);
        }

        return results;
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
        Random random = new Random();

        for (int i = 0; i < numGamesPerMatch; i++) {
            // Pick first AI randomly
            PlayerType p1 = ais.get(random.nextInt(ais.size()));

            // Pick second AI randomly, different from first
            PlayerType p2;
            do {
                p2 = ais.get(random.nextInt(ais.size()));
            } while (p2 == p1);

            // If neither is NeuralNetAI, replace one randomly with NeuralNetAI
            if (p1 != PlayerType.NEURAL_NET_AI && p2 != PlayerType.NEURAL_NET_AI) {
                if (random.nextBoolean()) {
                    p1 = PlayerType.NEURAL_NET_AI;
                } else {
                    p2 = PlayerType.NEURAL_NET_AI;
                }
            }

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

        List<AIRanking> rankings = new ArrayList<>(rankingMap.values());
        rankings.sort(Comparator.comparingInt(AIRanking::getElo).reversed());

        return new SimulationResult(rankings, matchStats);
    }

    private void updateElo(AIRanking r1, AIRanking r2, int s1, int s2) {
        double k = 32.0;
        double e1 = 1.0 / (1 + Math.pow(10, (r2.getElo() - r1.getElo()) / 400.0));
        double e2 = 1.0 / (1 + Math.pow(10, (r1.getElo() - r2.getElo()) / 400.0));

        r1.setElo((int) Math.round(r1.getElo() + k * (s1 - e1)));
        r2.setElo((int) Math.round(r2.getElo() + k * (s2 - e2)));
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
                case NEURAL_NET_AI -> aiMap.put(ai, new NeuralNetAI());
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

    private void trainNeuralNet(List<BoardState> moves, Player winner, Player player) {
        // Simple training: for each move, give reward = 1 if player won, 0.5 if draw, 0 if lost
        double reward;
        if (winner == player) reward = 1.0;
        else if (winner == null) reward = 0.5;
        else reward = 0.0;

        NeuralNetAI ai = (NeuralNetAI) aiMap.get(PlayerType.NEURAL_NET_AI);
        for (BoardState state : moves) {
            if (state.player == player) {
                ai.train(state.flatBoard, state.lastMove, state.player, reward);
            }
        }
    }

    // Helper classes
    public record BoardState(int[] flatBoard, Player player, int lastMove) {}
}