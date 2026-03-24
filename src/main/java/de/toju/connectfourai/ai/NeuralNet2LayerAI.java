package de.toju.connectfourai.ai;

import de.toju.connectfourai.model.Board;
import de.toju.connectfourai.model.Player;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.List;
import java.util.Random;

/**
 * Neural Network AI with 2 hidden layers for Connect Four.
 * Improves slightly after each game by training on the last game result.
 */
@Component
public class NeuralNet2LayerAI implements TrainableAIPlayer {

    private final int inputSize = 6 * 7;
    private final int hidden1Size = 128;
    private final int hidden2Size = 64;
    private final int outputSize = 7;
    private final double learningRate = 0.015;

    private final double[][] weightsInputHidden1;
    private final double[][] weightsHidden1Hidden2;
    private final double[][] weightsHidden2Output;

    private final Random random = new Random();

    public NeuralNet2LayerAI() {
        weightsInputHidden1 = new double[inputSize][hidden1Size];
        weightsHidden1Hidden2 = new double[hidden1Size][hidden2Size];
        weightsHidden2Output = new double[hidden2Size][outputSize];

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

        // Debug
        if (bestMove == -1) {
            System.err.println("No valid move found! Outputs:");
            for (int c = 0; c < 7; c++) {
                System.err.println("c=" + c + " output=" + output[c] + " full=" + board.isColumnFull(c));
            }
        }

        double epsilon = 0.30;
        int chosenMove;
        if (random.nextDouble() < epsilon) {
            // explore: zufälliger erlaubter Move
            List<Integer> validMoves = board.getValidColumns();
            chosenMove = validMoves.get(random.nextInt(validMoves.size()));
        } else {
            // exploit: bester Move laut Netz
            chosenMove = bestMove;
        }

        return chosenMove;
    }

    @Override
    public void train(int[] board, int move, Player player, double result) {
        if (move < 0 || move >= outputSize) return;

        // ===== Forward pass =====

        double[] hidden1 = new double[hidden1Size];
        for (int j = 0; j < hidden1Size; j++) {
            double sum = 0;
            for (int i = 0; i < inputSize; i++) {
                sum += board[i] * weightsInputHidden1[i][j];
            }
            hidden1[j] = Math.tanh(sum);
        }

        double[] hidden2 = new double[hidden2Size];
        for (int j = 0; j < hidden2Size; j++) {
            double sum = 0;
            for (int i = 0; i < hidden1Size; i++) {
                sum += hidden1[i] * weightsHidden1Hidden2[i][j];
            }
            hidden2[j] = Math.tanh(sum);
        }

        double[] output = new double[outputSize];
        for (int j = 0; j < outputSize; j++) {
            double sum = 0;
            for (int i = 0; i < hidden2Size; i++) {
                sum += hidden2[i] * weightsHidden2Output[i][j];
            }
            output[j] = sum;
        }

        // ===== Backprop =====

        double error = result - output[move];

        // Hidden2 errors (nur für gewählten Move relevant)
        double[] hidden2Errors = new double[hidden2Size];

        for (int i = 0; i < hidden2Size; i++) {
            double oldWeight = weightsHidden2Output[i][move];

            // Update Hidden2 -> Output
            weightsHidden2Output[i][move] += learningRate * error * hidden2[i];

            // Fehler für Hidden2
            double derivative2 = 1 - hidden2[i] * hidden2[i];
            hidden2Errors[i] = error * oldWeight * derivative2;
        }

        // Hidden1 errors sammeln
        double[] hidden1Errors = new double[hidden1Size];

        for (int j = 0; j < hidden1Size; j++) {
            double sum = 0;

            for (int i = 0; i < hidden2Size; i++) {
                sum += hidden2Errors[i] * weightsHidden1Hidden2[j][i];

                // Update Hidden1 -> Hidden2
                weightsHidden1Hidden2[j][i] += learningRate * hidden2Errors[i] * hidden1[j];
            }

            double derivative1 = 1 - hidden1[j] * hidden1[j];
            hidden1Errors[j] = sum * derivative1;
        }

        // Input -> Hidden1
        for (int j = 0; j < hidden1Size; j++) {
            for (int k = 0; k < inputSize; k++) {
                weightsInputHidden1[k][j] += learningRate * hidden1Errors[j] * board[k];
            }
        }

        saveWeights();
    }

    private void saveWeights() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(getWeightsFileName()))) {
            out.writeObject(weightsInputHidden1);
            out.writeObject(weightsHidden1Hidden2);
            out.writeObject(weightsHidden2Output);
        } catch (IOException e) {
            System.err.println("Failed to save NeuralNetAI2Layer weights: " + e.getMessage());
        }
    }

    private void loadWeights() {
        File f = new File(getWeightsFileName());
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