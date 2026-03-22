package de.toju.connectfourai.model;

import lombok.Getter;

@Getter
public class MatchStats {
    private int wins = 0;
    private int losses = 0;
    private int draws = 0;

    public void recordWin() { wins++; }
    public void recordLoss() { losses++; }
    public void recordDraw() { draws++; }
}