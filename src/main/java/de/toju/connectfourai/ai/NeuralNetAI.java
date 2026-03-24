package de.toju.connectfourai.ai;

import de.toju.connectfourai.model.Board;
import de.toju.connectfourai.model.Player;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Random;

/**
 * Simple Neural Network AI for Connect Four.
 * Uses a flat board representation and a small feedforward network.
 * Improves slightly after each game by training on the last game result.
 */
@Component
public class NeuralNetAI implements TrainableAIPlayer {

    private final int inputSize = 6 * 7; // board flattened
    private final int hiddenSize = 64;   // arbitrary hidden layer size
    private final int outputSize = 7;    // one per column
    private final double learningRate = 0.005;

    private final double[][] weightsInputHidden;
    private final double[][] weightsHiddenOutput;

    private final String saveFilePath = "neuralnetai.weights";

    private final Random random = new Random();

    public NeuralNetAI() {
        // Initialize weights randomly between -1 and 1
        weightsInputHidden = new double[inputSize][hiddenSize];
        weightsHiddenOutput = new double[hiddenSize][outputSize];

        for (int i = 0; i < inputSize; i++)
            for (int j = 0; j < hiddenSize; j++)
                weightsInputHidden[i][j] = random.nextDouble() * 2 - 1;

        for (int i = 0; i < hiddenSize; i++)
            for (int j = 0; j < outputSize; j++)
                weightsHiddenOutput[i][j] = random.nextDouble() * 2 - 1;

        // Try to load persisted weights
        loadWeights();
    }

    @Override
    public int chooseMove(Board board, Player player) {
        int[] input = board.toFlatArray(player);
        double[] hidden = new double[hiddenSize];
        double[] output = new double[outputSize];

        // Forward pass: input -> hidden
        for (int j = 0; j < hiddenSize; j++) {
            double sum = 0;
            for (int i = 0; i < inputSize; i++) {
                sum += input[i] * weightsInputHidden[i][j];
            }
            hidden[j] = Math.tanh(sum); // activation
        }

        // Debug
        for (int i = 0; i < hiddenSize; i++) {
            for (int j = 0; j < outputSize; j++) {
                if (Double.isNaN(weightsHiddenOutput[i][j])) {
                    System.err.println("NaN in weightsHiddenOutput!");
                }
            }
        }

        // Hidden -> output
        for (int j = 0; j < outputSize; j++) {
            double sum = 0;
            for (int i = 0; i < hiddenSize; i++) {
                sum += hidden[i] * weightsHiddenOutput[i][j];
            }
            output[j] = Math.max(-10, Math.min(10, sum));
        }

        // Debug
        for (int c = 0; c < 7; c++) {
            if (Double.isNaN(output[c])) {
                System.err.println("NaN detected in output at column " + c);
            }
        }

        // Choose the move with the highest score among available columns
        int bestMove = -1;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (int c = 0; c < 7; c++) {
            if (!board.isColumnFull(c) && output[c] > bestScore) {
                bestScore = output[c];
                bestMove = c;
            }
        }

        // Debug
        if (bestMove == -1) {
            System.err.println("No valid move found! Outputs:");
            for (int c = 0; c < 7; c++) {
                System.err.println("c=" + c + " output=" + output[c] + " full=" + board.isColumnFull(c));
            }
        }

        // Fallback
        if (bestMove == -1) {
            List<Integer> validMoves = board.getValidColumns();
            bestMove = validMoves.get(random.nextInt(validMoves.size()));
        }

        double epsilon = 0.30;
        int chosenMove;
        if (random.nextDouble() < epsilon) {
            // explore
            List<Integer> validMoves = board.getValidColumns();
            chosenMove = validMoves.get(random.nextInt(validMoves.size()));
        } else {
            // exploit
            chosenMove = bestMove;
        }

        return chosenMove;
    }

    @Override
    public void train(int[] board, int move, Player player, double result) {
        if (move < 0 || move >= outputSize) return;

        double[] hidden = new double[hiddenSize];

        // Forward pass: input -> hidden
        for (int j = 0; j < hiddenSize; j++) {
            double sum = 0;
            for (int i = 0; i < inputSize; i++) {
                sum += board[i] * weightsInputHidden[i][j];
            }
            hidden[j] = Math.tanh(sum);
        }

        // Hidden -> output (nur für Fehlerberechnung)
        double[] output = new double[outputSize];
        for (int j = 0; j < outputSize; j++) {
            double sum = 0;
            for (int i = 0; i < hiddenSize; i++) {
                sum += hidden[i] * weightsHiddenOutput[i][j];
            }
            output[j] = sum;
        }

        // Fehler nur für den gespielten Move
        double error = result - output[move];

        // Update Hidden -> Output
        for (int i = 0; i < hiddenSize; i++) {
            weightsHiddenOutput[i][move] += learningRate * error * hidden[i];
            weightsHiddenOutput[i][move] = Math.max(-5, Math.min(5, weightsHiddenOutput[i][move]));
        }

        saveWeights();
    }

    /**
     * Save weights to disk
     */
    private void saveWeights() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(saveFilePath))) {
            out.writeObject(weightsInputHidden);
            out.writeObject(weightsHiddenOutput);
        } catch (IOException e) {
            System.err.println("Failed to save NeuralNetAI weights: " + e.getMessage());
        }
    }

    /**
     * Load weights from disk
     */
    private void loadWeights() {
        File f = new File(saveFilePath);
        if (!f.exists()) return;

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(f))) {
            double[][] wih = (double[][]) in.readObject();
            double[][] who = (double[][]) in.readObject();
            for (int i = 0; i < inputSize; i++) System.arraycopy(wih[i], 0, weightsInputHidden[i], 0, hiddenSize);
            for (int i = 0; i < hiddenSize; i++) System.arraycopy(who[i], 0, weightsHiddenOutput[i], 0, outputSize);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load NeuralNetAI weights: " + e.getMessage());
        }
    }
}