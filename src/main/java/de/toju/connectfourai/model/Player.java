package de.toju.connectfourai.model;

public enum Player {
    PLAYER1,
    PLAYER2;

    public Player opposite() {
        return this == PLAYER1 ? PLAYER2 : PLAYER1;
    }
}