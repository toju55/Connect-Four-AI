package de.toju.connectfourai.model;

public enum PlayerType {
    HUMAN,
    RANDOM_AI,
    HEURISTIC_AI,
    MINIMAX_AI,
    MINIMAX_HEURISTIC_AI;

    public boolean isHuman() {
        return this == HUMAN;
    }
}