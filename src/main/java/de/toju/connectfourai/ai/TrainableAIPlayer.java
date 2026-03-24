package de.toju.connectfourai.ai;

import de.toju.connectfourai.model.Player;

public interface TrainableAIPlayer extends AIPlayer {

    default String getWeightsFileName() {
        return this.getClass().getSimpleName() + "_weights.dat";
    }

    void train(int[] board, int move, Player player, double reward);

}