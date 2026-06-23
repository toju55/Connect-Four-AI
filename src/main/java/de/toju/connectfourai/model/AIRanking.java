package de.toju.connectfourai.model;

public class AIRanking {
        private final PlayerType ai;
        private int elo;
        private int wins;
        private int losses;
        private int draws;

        public AIRanking(PlayerType ai, int initialElo) {
            this.ai = ai;
            this.elo = initialElo;
        }

        public void recordResult(double ownScore, double opponentScore) {
            if (ownScore == 1.0) {
                wins++;
            } else if (opponentScore == 1.0) {
                losses++;
            } else {
                draws++;
            }
        }

        public PlayerType getAi() { return ai; }
        public int getElo() { return elo; }
        public void setElo(int elo) { this.elo = elo; }
        public int getWins() { return wins; }
        public int getLosses() { return losses; }
        public int getDraws() { return draws; }
    }
