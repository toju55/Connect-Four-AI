package de.toju.connectfourai.ai;

import de.toju.connectfourai.model.Player;

public interface TrainableAIPlayer extends AIPlayer {

    void train(int[] board, int move, Player player, double reward);
}