package de.toju.connectfourai.model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

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

    /** Count rows of exactly 'length' for the given player */
    public int countRowsOf(Player player, int length) {
        int count = 0;

        // horizontal
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c <= COLS - length; c++) {
                boolean match = true;
                for (int i = 0; i < length; i++) {
                    if (grid[r][c + i] != player) {
                        match = false;
                        break;
                    }
                }
                if (match) count++;
            }
        }

        // vertical
        for (int c = 0; c < COLS; c++) {
            for (int r = 0; r <= ROWS - length; r++) {
                boolean match = true;
                for (int i = 0; i < length; i++) {
                    if (grid[r + i][c] != player) {
                        match = false;
                        break;
                    }
                }
                if (match) count++;
            }
        }

        // diagonal /
        for (int r = 0; r <= ROWS - length; r++) {
            for (int c = 0; c <= COLS - length; c++) {
                boolean match = true;
                for (int i = 0; i < length; i++) {
                    if (grid[r + i][c + i] != player) {
                        match = false;
                        break;
                    }
                }
                if (match) count++;
            }
        }

        // diagonal \
        for (int r = length - 1; r < ROWS; r++) {
            for (int c = 0; c <= COLS - length; c++) {
                boolean match = true;
                for (int i = 0; i < length; i++) {
                    if (grid[r - i][c + i] != player) {
                        match = false;
                        break;
                    }
                }
                if (match) count++;
            }
        }

        return count;
    }

    /** Count potential winning lines for player */
    public int countPotentialWins(Player player) {
        // Very simple: count rows of length 3 with one empty cell to make 4
        int count = 0;
        count += countRowsOf(player, 3); // could refine to only "open-ended" sequences
        return count;
    }

    /** Count how many discs are in the center column (for center control) */
    public int countCenterControl(Player player) {
        int centerCol = COLS / 2;
        int count = 0;
        for (int r = 0; r < ROWS; r++) {
            if (grid[r][centerCol] == player) count++;
        }
        return count;
    }

    public Player checkWinner() {
        int rows = grid.length;
        int cols = grid[0].length;

        // Horizontal
        for (Player[] players : grid) {
            for (int c = 0; c <= cols - 4; c++) {
                Player p = players[c];
                if (p != null &&
                        p == players[c + 1] &&
                        p == players[c + 2] &&
                        p == players[c + 3]) {
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

    public int[] toFlatArray(Player perspective) {
        int[] flat = new int[ROWS * COLS];
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                Player cell = grid[r][c];
                int val = 0;
                if (cell != null) {
                    val = cell == perspective ? 1 : -1;
                }
                flat[r * COLS + c] = val;
            }
        }
        return flat;
    }

    public static Board fromFlatArray(int[] flatBoard) {
        if (flatBoard.length != ROWS * COLS)
            throw new IllegalArgumentException("Flat board must have length " + (ROWS * COLS));

        Board board = new Board();
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                int value = flatBoard[r * COLS + c];
                if (value == 1) board.grid[r][c] = Player.PLAYER1;
                else if (value == 2) board.grid[r][c] = Player.PLAYER2;
                else board.grid[r][c] = null;
            }
        }
        return board;
    }

    public List<Integer> getValidColumns() {
        List<Integer> valid = new ArrayList<>();
        for (int c = 0; c < 7; c++) {
            if (!isColumnFull(c)) {
                valid.add(c);
            }
        }
        return valid;
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