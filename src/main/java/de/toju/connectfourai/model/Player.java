package de.toju.connectfourai.model;

public enum Player {
    HUMAN,
    AI;

    public Player opposite() {
        return this == HUMAN ? AI : HUMAN;
    }
}