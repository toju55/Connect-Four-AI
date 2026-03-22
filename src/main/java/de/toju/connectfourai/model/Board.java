package de.toju.connectfourai.model;

import lombok.Getter;

@Getter
public class Board {

    private static final int ROWS = 6;
    private static final int COLS = 7;

    private final Player[][] grid;

    public Board() {
        this.grid = new Player[ROWS][COLS];
    }

    public boolean isValidMove(int col) {
        return col >= 0 && col < COLS && grid[0][col] == null;
    }

    public boolean makeMove(int col, Player player) {
        if (!isValidMove(col)) return false;

        for (int row = ROWS - 1; row >= 0; row--) {
            if (grid[row][col] == null) {
                grid[row][col] = player;
                return true;
            }
        }
        return false;
    }

    public int getCols() {
        return COLS;
    }

    public int getRows() {
        return ROWS;
    }

    public Player checkWinner() {
        int rows = grid.length;
        int cols = grid[0].length;

        // Horizontal
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c <= cols - 4; c++) {
                Player p = grid[r][c];
                if (p != null &&
                        p == grid[r][c+1] &&
                        p == grid[r][c+2] &&
                        p == grid[r][c+3]) {
                    return p;
                }
            }
        }

        // Vertical
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r <= rows - 4; r++) {
                Player p = grid[r][c];
                if (p != null &&
                        p == grid[r+1][c] &&
                        p == grid[r+2][c] &&
                        p == grid[r+3][c]) {
                    return p;
                }
            }
        }

        // Diagonal
        for (int r = 0; r <= rows - 4; r++) {
            for (int c = 0; c <= cols - 4; c++) {
                Player p = grid[r][c];
                if (p != null &&
                        p == grid[r+1][c+1] &&
                        p == grid[r+2][c+2] &&
                        p == grid[r+3][c+3]) {
                    return p;
                }
            }
        }

        // Diagonal
        for (int r = 0; r <= rows - 4; r++) {
            for (int c = 3; c < cols; c++) {
                Player p = grid[r][c];
                if (p != null &&
                        p == grid[r+1][c-1] &&
                        p == grid[r+2][c-2] &&
                        p == grid[r+3][c-3]) {
                    return p;
                }
            }
        }

        // No winner yet
        return null;
    }

    public boolean isFull() {
        for (Player[] row : grid) {
            for (Player cell : row) {
                if (cell == null) return false;
            }
        }
        return true;
    }

    public boolean isColumnFull(int col) {
        // Check if the top cell of the column is occupied
        return grid[0][col] != null;
    }

    public Board copy() {
        // Create a deep copy of the board to simulate moves
        Board copy = new Board();
        for (int r = 0; r < getRows(); r++) {
            System.arraycopy(this.grid[r], 0, copy.grid[r], 0, getCols());
        }
        return copy;
    }

    public void printBoard() {
        System.out.println();
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (grid[r][c] == null) {
                    System.out.print(". ");
                } else if (grid[r][c] == Player.PLAYER1) {
                    System.out.print("X ");
                } else if (grid[r][c] == Player.PLAYER2) {
                    System.out.print("O ");
                }
            }
            System.out.println();
        }
        // optional: Spaltennummern
        for (int c = 0; c < COLS; c++) {
            System.out.print(c + " ");
        }
        System.out.println("\n");
    }
}