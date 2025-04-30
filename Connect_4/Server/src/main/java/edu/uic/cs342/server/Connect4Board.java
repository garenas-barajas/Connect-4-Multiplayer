package edu.uic.cs342.server;

public class Connect4Board {
    // Generate board
    public static final int ROWS = 6;
    public static final int COLS = 7;
    private final int[][] grid = new int[ROWS][COLS];

    // Drop token for player. Return row index where it landed or -1 if full
    public int dropToken(int col, int playerId) {
        if (col < 0 || col >= COLS) return -1;

        for(int row = ROWS - 1; row >= 0; row--) {
            // Check for empty spot
            if(grid[row][col] == 0) {
                // If spot is empty, place token
                grid[row][col] = playerId;
                return row;
            }
        }
        // Column is full
        return -1;
    }

    // Check for win condition
    public boolean checkWin(int row, int col) {
        int player = grid[row][col];
        if (player == 0) return false;

        // Checks all directions/ Horizontal, Vertical, Diagonal
        int[][] directionVectors = {
                {0,1}, // row = 0, col = +1 moves right
                {1,0}, // row = +1, col = 0 moves down
                {1,1}, // row = +1, col = +1 moves down right
                {1,-1} // row = +1, col = -1 moves down left
        };

        for (int[] vector : directionVectors) {
            int rowDelta = vector[0], colDelta = vector[1];
            // Scan and count forward
            int count = countMatchesInDirection(row, col, rowDelta, colDelta, player)
            // Scan and count backward
            + countMatchesInDirection(row, col, -rowDelta, -colDelta, player)
            // Add one for current piece
            + 1;

            // Check for 4 in a row
            if (count >= 4) return true;
        }
        // Win condition not met
        return false;
    }

    // Count tokens in a direction
    private int countMatchesInDirection(int startRow, int startCol, int rowDelta, int colDelta, int playerId) {
        int count = 0;
        int r = startRow + rowDelta;
        int c = startCol + colDelta;

        // while on the game board and while tokens belong to player
        while (r >= 0 && r < ROWS && c >= 0 && c < COLS && grid[r][c] == playerId) {
            // Increment count
            count++;
            // Move row and column
            r += rowDelta;
            c += colDelta;
        }
        return count;
    }

    // Check if board is full
    public boolean isFull(){
        // check entire board for 0's
        for (int col = 0; col < COLS; col++) {
            if (grid[0][col] == 0) return false;
        }
        return true;
    }
}
