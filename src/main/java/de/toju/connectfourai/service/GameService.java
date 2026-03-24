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
import de.toju.connectfourai.ai.WeightedHeuristicAI;
import de.toju.connectfourai.model.Board;
import de.toju.connectfourai.model.Player;
import de.toju.connectfourai.model.PlayerType;
import lombok.Getter;
import org.springframework.stereotype.Service;

@Service
@Getter
public class GameService {

    private Board board;
    private Player currentPlayer;

    private PlayerType player1Type;
    private PlayerType player2Type;

    private final AIPlayer alphaBetaAi;
    private final AIPlayer heuristicAI;
    private final AIPlayer hybridAi;
    private final AIPlayer mctsAI;
    private final AIPlayer minimaxAI;
    private final AIPlayer minimaxHeuristicAI;
    private final AIPlayer monteCarloAI;
    private final AIPlayer neuralNet2LayerAI;
    private final AIPlayer neuralNet2LayerHeuristicAI;
    private final AIPlayer neuralNetAI;
    private final AIPlayer neuralNetHeuristicAI;
    private final AIPlayer randomAI;
    private final AIPlayer weightedHeuristicAI;

    public GameService(AlphaBetaAI alphaBetaAi, HeuristicAI heuristicAI, HybridAI hybridAi, MCTSAI mctsAI,
                       MinimaxAI minimaxAI, MinimaxHeuristicAI minimaxHeuristicAI, MonteCarloAI monteCarloAI,
                       NeuralNet2LayerAI neuralNet2LayerAI, NeuralNet2LayerHeuristicAI neuralNet2LayerHeuristicAI, NeuralNetAI neuralNetAI, NeuralNetHeuristicAI neuralNetHeuristicAI, RandomAI randomAI,
                       WeightedHeuristicAI weightedHeuristicAI) {
        this.alphaBetaAi = alphaBetaAi;
        this.heuristicAI = heuristicAI;
        this.hybridAi = hybridAi;
        this.mctsAI = mctsAI;
        this.minimaxAI = minimaxAI;
        this.minimaxHeuristicAI = minimaxHeuristicAI;
        this.monteCarloAI = monteCarloAI;
        this.neuralNet2LayerAI = neuralNet2LayerAI;
        this.neuralNet2LayerHeuristicAI = neuralNet2LayerHeuristicAI;
        this.neuralNetAI = neuralNetAI;
        this.neuralNetHeuristicAI = neuralNetHeuristicAI;
        this.randomAI = randomAI;
        this.weightedHeuristicAI = weightedHeuristicAI;
        reset();
    }

    public void playNextMove() {
        if (isGameOver()) return;

        PlayerType type = getCurrentPlayerType();

        if (type == PlayerType.HUMAN) return;

        AIPlayer ai = switch (type) {
            case ALPHA_BETA_AI -> alphaBetaAi;
            case HEURISTIC_AI -> heuristicAI;
            case HYBRID_AI -> hybridAi;
            case MCTS_AI -> mctsAI;
            case MINIMAX_AI -> minimaxAI;
            case MINIMAX_HEURISTIC_AI ->  minimaxHeuristicAI;
            case MONTE_CARLO_AI -> monteCarloAI;
            case NEURAL_NET_2_LAYERS_AI ->  neuralNet2LayerAI;
            case NEURAL_NET_2_LAYER_HEURISTIC_AI ->   neuralNet2LayerHeuristicAI;
            case NEURAL_NET_AI -> neuralNetAI;
            case NEURAL_NET_HEURISTIC_AI -> neuralNetHeuristicAI;
            case RANDOM_AI -> randomAI;
            case WEIGHTED_HEURISTIC_AI -> weightedHeuristicAI;
            default -> null;
        };

        if (ai != null) {
            int move = ai.chooseMove(board, currentPlayer);
            playMove(move);
        }
    }

    public void playMove(int col) {
        if (isGameOver()) return;

        boolean success = board.makeMove(col, currentPlayer);

        if (success) {
            currentPlayer = currentPlayer.opposite();
        }
    }

    public void startNewGame(PlayerType player1Type, PlayerType player2Type) {
        this.board = new Board();
        this.currentPlayer = Player.PLAYER1;

        this.player1Type = player1Type;
        this.player2Type = player2Type;
    }

    public void reset() {
        this.board = new Board();
        this.currentPlayer = Player.PLAYER1;
    }

    public PlayerType getCurrentPlayerType() {
        return currentPlayer == Player.PLAYER1 ? player1Type : player2Type;
    }

    public Player getWinner() {
        return board.checkWinner();
    }

    public PlayerType getWinnerType() {
        Player winner = getWinner();

        return winner == null ? null : switch (winner) {
            case PLAYER1 -> player1Type;
            case PLAYER2 -> player2Type;
        };
    }

    public boolean isGameOver() {
        return getWinner() != null || board.isFull();
    }
}