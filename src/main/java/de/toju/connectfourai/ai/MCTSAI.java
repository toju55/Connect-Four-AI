package de.toju.connectfourai.ai;

import de.toju.connectfourai.model.Board;
import de.toju.connectfourai.model.Player;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

@Component
public class MCTSAI implements AIPlayer {

    static class MCTSNode {
        Board board;
        Player player; // player to move
        MCTSNode parent;
        List<MCTSNode> children = new ArrayList<>();
        int visits = 0;
        double wins = 0;
        int move; // move that led here

        public MCTSNode(Board board, Player player, MCTSNode parent, int move) {
            this.board = board;
            this.player = player;
            this.parent = parent;
            this.move = move;
        }
    }

    private static final int ITERATIONS = 2000;
    private static final double EXPLORATION = 1.41; // sqrt(2)

    private final Random random = new Random();

    @Override
    public int chooseMove(Board board, Player player) {
        MCTSNode root = new MCTSNode(board.copy(), player, null, -1);

        for (int i = 0; i < ITERATIONS; i++) {
            MCTSNode node = select(root);
            if (node.visits > 0 && node.board.checkWinner() == null) {
                expand(node);

                if (!node.children.isEmpty()) {
                    node = node.children.get(random.nextInt(node.children.size()));
                }
            }            double result = simulate(node);
            backpropagate(node, result);
        }

        return root.children.stream()
                .max(Comparator.comparingInt(n -> n.visits))
                .map(n -> n.move)
                .orElse(0);
    }

    private MCTSNode select(MCTSNode node) {
        while (!node.children.isEmpty()) {
            node = node.children.stream()
                    .max(Comparator.comparingDouble(this::uct))
                    .orElseThrow();
        }
        return node;
    }

    private double uct(MCTSNode node) {
        if (node.visits == 0) return Double.MAX_VALUE;

        double winRate = node.wins / node.visits;
        double exploration = EXPLORATION *
                Math.sqrt(Math.log(node.parent.visits) / node.visits);

        return winRate + exploration;
    }

    private void expand(MCTSNode node) {
        if (!node.children.isEmpty()) return; // prevent double expansion

        for (int col = 0; col < node.board.getCols(); col++) {
            if (node.board.isColumnFull(col)) continue;

            Board copy = node.board.copy();
            copy.makeMove(col, node.player);

            node.children.add(new MCTSNode(
                    copy,
                    node.player.opposite(),
                    node,
                    col
            ));
        }
    }

    private double simulate(MCTSNode node) {
        Board board = node.board.copy();
        Player current = node.player;

        while (board.checkWinner() == null && !board.isFull()) {
            List<Integer> moves = getMoves(board);
            int move = moves.get(random.nextInt(moves.size()));
            board.makeMove(move, current);
            current = current.opposite();
        }

        Player winner = board.checkWinner();

        if (winner == null) return 0.5;
        return winner == node.player ? 0 : 1;
    }

    private void backpropagate(MCTSNode node, double result) {
        while (node != null) {
            node.visits++;
            node.wins += result;
            result = 1 - result; // switch perspective
            node = node.parent;
        }
    }

    private List<Integer> getMoves(Board board) {
        List<Integer> moves = new ArrayList<>();
        for (int c = 0; c < board.getCols(); c++) {
            if (!board.isColumnFull(c)) moves.add(c);
        }
        return moves;
    }
}