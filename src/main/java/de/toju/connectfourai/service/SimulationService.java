package de.toju.connectfourai.service;

import de.toju.connectfourai.ai.AIPlayer;
import de.toju.connectfourai.ai.HeuristicAI;
import de.toju.connectfourai.ai.MinimaxAI;
import de.toju.connectfourai.ai.MinimaxHeuristicAI;
import de.toju.connectfourai.ai.RandomAI;
import de.toju.connectfourai.model.Board;
import de.toju.connectfourai.model.Player;
import de.toju.connectfourai.model.PlayerType;
import lombok.RequiredArgsConstructor;
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
            Player current = Player.PLAYER1;

            while (board.checkWinner() == null && !board.isFull()) {
                int move = current == Player.PLAYER1
                        ? ai1.chooseMove(board, current)
                        : ai2.chooseMove(board, current);

                board.makeMove(move, current);
                current = current.opposite();
            }

            Player winner = board.checkWinner();
            if (winner == Player.PLAYER1) results.put("AI1Wins", results.get("AI1Wins") + 1);
            else if (winner == Player.PLAYER2) results.put("AI2Wins", results.get("AI2Wins") + 1);
            else results.put("Draws", results.get("Draws") + 1);
        }

        return results;
    }

    /**
     * Simulate 'numGames' random matches between all registered AIs and return ELO ranking.
     */
    public List<AIRanking> simulateRandomMatches(int numGamesPerMatch) {
        // Initialize stats and ELO
        Map<PlayerType, AIRanking> rankingMap = new HashMap<>();
        aiMap.keySet().forEach(type -> rankingMap.put(type, new AIRanking(type, 1500)));

        List<PlayerType> ais = new ArrayList<>(aiMap.keySet());
        Random random = new Random();

        for (int i = 0; i < numGamesPerMatch; i++) {
            PlayerType p1 = ais.get(random.nextInt(ais.size()));
            PlayerType p2;
            do {
                p2 = ais.get(random.nextInt(ais.size()));
            } while (p1 == p2);

            Map<String, Integer> result = simulateGames(p1, p2, 1); // 1 game per random match

            AIRanking r1 = rankingMap.get(p1);
            AIRanking r2 = rankingMap.get(p2);

            int s1 = 0, s2 = 0;
            if (result.get("AI1Wins") == 1) {
                s1 = 1; s2 = 0;
            } else if (result.get("AI2Wins") == 1) {
                s1 = 0; s2 = 1;
            } else { // Draw
                s1 = 0; s2 = 0; 
            }

            r1.recordResult(s1, 1 - s1); // wins, losses
            r2.recordResult(s2, 1 - s2);

            // Update ELO
            updateElo(r1, r2, s1, s2);
        }

        List<AIRanking> rankings = new ArrayList<>(rankingMap.values());
        rankings.sort(Comparator.comparingInt(AIRanking::getElo).reversed());
        return rankings;
    }

    private void updateElo(AIRanking r1, AIRanking r2, int s1, int s2) {
        double k = 32.0;
        double e1 = 1.0 / (1 + Math.pow(10, (r2.getElo() - r1.getElo()) / 400.0));
        double e2 = 1.0 / (1 + Math.pow(10, (r1.getElo() - r2.getElo()) / 400.0));

        r1.setElo((int) Math.round(r1.getElo() + k * (s1 - e1)));
        r2.setElo((int) Math.round(r2.getElo() + k * (s2 - e2)));
    }

    public static class AIRanking {
        private final PlayerType ai;
        private int elo;
        private int wins;
        private int losses;
        private int draws;

        public AIRanking(PlayerType ai, int initialElo) {
            this.ai = ai;
            this.elo = initialElo;
        }

        public void recordResult(int win, int loss) {
            if (win == 1) wins++;
            else if (loss == 1) losses++;
            else draws++;
        }

        public PlayerType getAi() { return ai; }
        public int getElo() { return elo; }
        public void setElo(int elo) { this.elo = elo; }
        public int getWins() { return wins; }
        public int getLosses() { return losses; }
        public int getDraws() { return draws; }
    }

    private void initAiMap() {
        for (PlayerType ai : PlayerType.values()) {
            if (ai.isHuman()) {
                continue;
            }

            switch (ai) {
                case RANDOM_AI -> aiMap.put(ai, new RandomAI());
                case HEURISTIC_AI -> aiMap.put(ai, new HeuristicAI());
                case MINIMAX_AI -> aiMap.put(ai, new MinimaxAI());
                case MINIMAX_HEURISTIC_AI -> aiMap.put(ai, new MinimaxHeuristicAI());
            }
        }
    }
}