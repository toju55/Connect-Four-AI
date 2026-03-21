package de.toju.connectfourai.ai;

import de.toju.connectfourai.model.Board;
import de.toju.connectfourai.model.Player;

public interface AIPlayer {

    int chooseMove(Board board, Player player);

}