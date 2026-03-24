package de.toju.connectfourai.model;

public enum PlayerType {
    HUMAN,
    ALPHA_BETA_AI,
    HEURISTIC_AI,
    HYBRID_AI,
    MCTS_AI,
    MINIMAX_AI,
    MINIMAX_HEURISTIC_AI,
    MONTE_CARLO_AI,
    NEURAL_NET_2_LAYERS_AI,
    NEURAL_NET_2_LAYER_HEURISTIC_AI,
    NEURAL_NET_AI,
    NEURAL_NET_HEURISTIC_AI,
    RANDOM_AI,
    WEIGHTED_HEURISTIC_AI;

    public boolean isHuman() {
        return this == HUMAN;
    }
}