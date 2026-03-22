package de.toju.connectfourai.ai;

import de.toju.connectfourai.model.Board;
import de.toju.connectfourai.model.Player;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Random;

/**
 * Neural Network AI with 2 hidden layers for Connect Four.
 * Improves slightly after each game by training on the last game result.
 */
@Component
public class NeuralNet2LayerAI implements AIPlayer {

    private final int inputSize = 6 * 7;
    private final int hidden1Size = 64;
    private final int hidden2Size = 32;
    private final int outputSize = 7;
    private final double learningRate = 0.1;

    private final double[][] weightsInputHidden1;
    private final double[][] weightsHidden1Hidden2;
    private final double[][] weightsHidden2Output;

    private final String saveFilePath = "neuralnet2layerai.weights";

    public NeuralNet2LayerAI() {
        weightsInputHidden1 = new double[inputSize][hidden1Size];
        weightsHidden1Hidden2 = new double[hidden1Size][hidden2Size];
        weightsHidden2Output = new double[hidden2Size][outputSize];

        Random random = new Random();
        for (int i = 0; i < inputSize; i++)
            for (int j = 0; j < hidden1Size; j++)
                weightsInputHidden1[i][j] = random.nextDouble() * 2 - 1;

        for (int i = 0; i < hidden1Size; i++)
            for (int j = 0; j < hidden2Size; j++)
                weightsHidden1Hidden2[i][j] = random.nextDouble() * 2 - 1;

        for (int i = 0; i < hidden2Size; i++)
            for (int j = 0; j < outputSize; j++)
                weightsHidden2Output[i][j] = random.nextDouble() * 2 - 1;

        loadWeights();
    }

    @Override
    public int chooseMove(Board board, Player player) {
        int[] input = board.toFlatArray(player);
        double[] hidden1 = new double[hidden1Size];
        double[] hidden2 = new double[hidden2Size];
        double[] output = new double[outputSize];

        // Forward pass: input -> hidden1
        for (int j = 0; j < hidden1Size; j++) {
            double sum = 0;
            for (int i = 0; i < inputSize; i++) sum += input[i] * weightsInputHidden1[i][j];
            hidden1[j] = Math.tanh(sum);
        }

        // hidden1 -> hidden2
        for (int j = 0; j < hidden2Size; j++) {
            double sum = 0;
            for (int i = 0; i < hidden1Size; i++) sum += hidden1[i] * weightsHidden1Hidden2[i][j];
            hidden2[j] = Math.tanh(sum);
        }

        // hidden2 -> output
        for (int j = 0; j < outputSize; j++) {
            double sum = 0;
            for (int i = 0; i < hidden2Size; i++) sum += hidden2[i] * weightsHidden2Output[i][j];
            output[j] = sum;
        }

        // Choose best valid move
        int bestMove = -1;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (int c = 0; c < outputSize; c++) {
            if (!board.isColumnFull(c) && output[c] > bestScore) {
                bestScore = output[c];
                bestMove = c;
            }
        }

        // Fallback: pick first available column
        if (bestMove == -1) {
            for (int c = 0; c < outputSize; c++) {
                if (!board.isColumnFull(c)) {
                    bestMove = c;
                    break;
                }
            }
        }

        return bestMove;
    }

    /**
     * Train the network after a game
     * @param board flattened board
     * @param move the move played
     * @param player the player who played the move
     * @param result 1 win, 0.5 draw, 0 loss
     */
    public void train(int[] board, int move, Player player, double result) {
        // Forward pass
        double[] hidden1 = new double[hidden1Size];
        for (int j = 0; j < hidden1Size; j++) {
            double sum = 0;
            for (int i = 0; i < inputSize; i++) sum += board[i] * weightsInputHidden1[i][j];
            hidden1[j] = Math.tanh(sum);
        }

        double[] hidden2 = new double[hidden2Size];
        for (int j = 0; j < hidden2Size; j++) {
            double sum = 0;
            for (int i = 0; i < hidden1Size; i++) sum += hidden1[i] * weightsHidden1Hidden2[i][j];
            hidden2[j] = Math.tanh(sum);
        }

        double[] output = new double[outputSize];
        for (int j = 0; j < outputSize; j++) {
            double sum = 0;
            for (int i = 0; i < hidden2Size; i++) sum += hidden2[i] * weightsHidden2Output[i][j];
            output[j] = sum;
        }

        // Error at output
        double error = result - output[move];

        // Hidden2 → Output
        for (int i = 0; i < hidden2Size; i++) {
            double oldWeight = weightsHidden2Output[i][move];
            weightsHidden2Output[i][move] += learningRate * error * hidden2[i];

            // Hidden1 → Hidden2
            double derivative2 = 1 - hidden2[i] * hidden2[i];
            double hidden2Error = error * oldWeight * derivative2;

            for (int j = 0; j < hidden1Size; j++) {
                double derivative1 = 1 - hidden1[j] * hidden1[j];
                double hidden1Error = hidden2Error * weightsHidden1Hidden2[j][i] * derivative1;
                weightsHidden1Hidden2[j][i] += learningRate * hidden1Error * hidden1[j];

                for (int k = 0; k < inputSize; k++) {
                    weightsInputHidden1[k][j] += learningRate * hidden1Error * board[k];
                }
            }
        }

        saveWeights();
    }

    private void saveWeights() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(saveFilePath))) {
            out.writeObject(weightsInputHidden1);
            out.writeObject(weightsHidden1Hidden2);
            out.writeObject(weightsHidden2Output);
        } catch (IOException e) {
            System.err.println("Failed to save NeuralNetAI2Layer weights: " + e.getMessage());
        }
    }

    private void loadWeights() {
        File f = new File(saveFilePath);
        if (!f.exists()) return;

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(f))) {
            double[][] w1 = (double[][]) in.readObject();
            double[][] w2 = (double[][]) in.readObject();
            double[][] w3 = (double[][]) in.readObject();

            for (int i = 0; i < inputSize; i++) System.arraycopy(w1[i], 0, weightsInputHidden1[i], 0, hidden1Size);
            for (int i = 0; i < hidden1Size; i++) System.arraycopy(w2[i], 0, weightsHidden1Hidden2[i], 0, hidden2Size);
            for (int i = 0; i < hidden2Size; i++) System.arraycopy(w3[i], 0, weightsHidden2Output[i], 0, outputSize);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load NeuralNetAI2Layer weights: " + e.getMessage());
        }
    }
}