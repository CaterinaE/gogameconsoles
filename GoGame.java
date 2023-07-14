import java.util.Arrays;
import java.util.Scanner;

public class GoGame {
    private int size;
    private char[][] board;
    private char currentPlayer;
    private double scoreX, scoreO;
    private boolean gameEnded;
    private int consecutivePasses;
    private double komi;
    private int stonesCapturedX, stonesCapturedO;
    private char[][] previousBoard; // keep track of previous board positions

    public GoGame(int size, double komi) {
        this.size = size;
        this.komi = komi;
        board = new char[size][size];
        currentPlayer = 'O'; // Black goes first
        previousBoard = new char[size][size];
    }

    public void play() {
        Scanner scanner = new Scanner(System.in);

        initializeBoard();
        consecutivePasses = 0;
        stonesCapturedX = 0;
        stonesCapturedO = 0;

        while (!gameEnded) {
            System.out.println("Current Player: " + currentPlayer);
            System.out.print("Enter row (0-" + (size - 1) + ") or 'e' to end the game, or 'a' for AI help: ");
            String input = scanner.next();

            if (input.equals("e")) {
                declareWinner();
                break;
            }

            if (input.equals("a")) {
                provideAIHelp();
                continue;
            }

            int row;
            try {
                row = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Try again.");
                continue;
            }

            System.out.print("Enter col (0-" + (size - 1) + "): ");
            int col = scanner.nextInt();

            if (isValidMove(row, col)) {
                makeMove(row, col);
                captureStones(row, col);
                printBoard();
                consecutivePasses = 0;

                if (isGameOver()) {
                    declareWinner();
                    break;
                }

                currentPlayer = (currentPlayer == 'X') ? 'O' : 'X'; // Switch player
            } else if (row == -1 && col == -1) {
                consecutivePasses++;
                currentPlayer = (currentPlayer == 'X') ? 'O' : 'X'; // Switch player
                if (consecutivePasses >= 2) {
                    declareWinner();
                    break;
                }
            } else {
                System.out.println("Invalid move. Try again.");
            }
        }

        scanner.close();
    }

    private void provideAIHelp() {
        System.out.println("AI is analyzing the board...");

        // Create a temporary board to simulate possible moves
        char[][] tempBoard = new char[size][size];
        for (int i = 0; i < size; i++) {
            tempBoard[i] = Arrays.copyOf(board[i], size);
        }

        int bestRow = -1;
        int bestCol = -1;
        int maxCapturedStones = 0;

        // Simulate placing a stone on each empty position and calculate the number of captured stones
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (tempBoard[row][col] == '.') {
                    tempBoard[row][col] = currentPlayer;
                    int capturedStones = simulateCaptureStones(row, col, tempBoard);

                    if (capturedStones > maxCapturedStones) {
                        bestRow = row;
                        bestCol = col;
                        maxCapturedStones = capturedStones;
                    }

                    // Reset the temporary board
                    tempBoard[row][col] = '.';
                }
            }
        }

        if (bestRow != -1 && bestCol != -1) {
            System.out.println("AI suggests placing a stone at row " + bestRow + ", col " + bestCol);
        } else {
            System.out.println("AI suggests passing the turn.");
        }
    }

    private int simulateCaptureStones(int row, int col, char[][] tempBoard) {
        char opponentPlayer = (currentPlayer == 'X') ? 'O' : 'X';

        int capturedStones = 0;

        if (row > 0 && tempBoard[row - 1][col] == opponentPlayer) {
            if (isCaptured(row - 1, col, tempBoard)) {
                capturedStones += removeCapturedStones(row - 1, col, tempBoard);
            }
        }
        if (row < size - 1 && tempBoard[row + 1][col] == opponentPlayer) {
            if (isCaptured(row + 1, col, tempBoard)) {
                capturedStones += removeCapturedStones(row + 1, col, tempBoard);
            }
        }
        if (col > 0 && tempBoard[row][col - 1] == opponentPlayer) {
            if (isCaptured(row, col - 1, tempBoard)) {
                capturedStones += removeCapturedStones(row, col - 1, tempBoard);
            }
        }
        if (col < size - 1 && tempBoard[row][col + 1] == opponentPlayer) {
            if (isCaptured(row, col + 1, tempBoard)) {
                capturedStones += removeCapturedStones(row, col + 1, tempBoard);
            }
        }

        return capturedStones;
    }

    private int removeCapturedStones(int row, int col, char[][] tempBoard) {
        char player = tempBoard[row][col];
        boolean[][] visited = new boolean[size][size];
        return removeCapturedStonesHelper(row, col, player, visited, tempBoard);
    }

    private int removeCapturedStonesHelper(int row, int col, char player, boolean[][] visited, char[][] tempBoard) {
        if (row < 0 || row >= size || col < 0 || col >= size || visited[row][col]) {
            return 0;
        }
        if (tempBoard[row][col] == player) {
            tempBoard[row][col] = '.';
            visited[row][col] = true;
            return 1 + removeCapturedStonesHelper(row - 1, col, player, visited, tempBoard)
                    + removeCapturedStonesHelper(row + 1, col, player, visited, tempBoard)
                    + removeCapturedStonesHelper(row, col - 1, player, visited, tempBoard)
                    + removeCapturedStonesHelper(row, col + 1, player, visited, tempBoard);
        }
        return 0;
    }

    // fills in board
    private void initializeBoard() {
        for (int i = 0; i < size; i++) {
            Arrays.fill(board[i], '.');
        }
    }

    private boolean isValidMove(int row, int col) {
        if (row < 0 || row >= size || col < 0 || col >= size) {
            return false;
        }
        if (board[row][col] != '.') {
            return false;
        }

        // Create a temporary board to compare with the previous board
        char[][] tempBoard = new char[size][size];
        for (int i = 0; i < size; i++) {
            tempBoard[i] = Arrays.copyOf(board[i], size);
        }

        // Make the move on the temporary board
        tempBoard[row][col] = currentPlayer;

        // Check if the temporary board matches the previous board and if it results in self-capture or violates the ko rule
        return !Arrays.deepEquals(tempBoard, previousBoard) && (capturesOpponentStones(row, col, tempBoard) || !isSelfCapture(row, col, tempBoard)) && !isKoRuleViolation(tempBoard);
    }

    private boolean capturesOpponentStones(int row, int col, char[][] tempBoard) {
        // Check if the move captures at least one opponent stone
        char opponentPlayer = (currentPlayer == 'X') ? 'O' : 'X';

        if (row > 0 && tempBoard[row - 1][col] == opponentPlayer && isCaptured(row - 1, col, tempBoard)) {
            return true;
        }
        if (row < size - 1 && tempBoard[row + 1][col] == opponentPlayer && isCaptured(row + 1, col, tempBoard)) {
            return true;
        }
        if (col > 0 && tempBoard[row][col - 1] == opponentPlayer && isCaptured(row, col - 1, tempBoard)) {
            return true;
        }
        if (col < size - 1 && tempBoard[row][col + 1] == opponentPlayer && isCaptured(row, col + 1, tempBoard)) {
            return true;
        }

        return false;
    }

    private boolean isSelfCapture(int row, int col, char[][] tempBoard) {
        // Check if the move results in self-capture
        char opponentPlayer = (currentPlayer == 'X') ? 'O' : 'X';

        // Check if there is at least one neighboring opponent stone
        boolean hasOpponentNeighbor =
                (row > 0 && tempBoard[row - 1][col] == opponentPlayer) ||
                        (row < size - 1 && tempBoard[row + 1][col] == opponentPlayer) ||
                        (col > 0 && tempBoard[row][col - 1] == opponentPlayer) ||
                        (col < size - 1 && tempBoard[row][col + 1] == opponentPlayer);

        // Check if self-capture occurs only when there is at least one neighboring opponent stone
        return hasOpponentNeighbor && isCaptured(row, col, tempBoard);
    }

    private boolean isKoRuleViolation(char[][] tempBoard) {
        // Check if the current board position matches the previous board position
        return Arrays.deepEquals(tempBoard, previousBoard);
    }

    private boolean isCaptured(int row, int col, char[][] tempBoard) {
        char player = tempBoard[row][col];
        boolean[][] visited = new boolean[size][size];
        return !hasLiberty(row, col, player, visited, tempBoard);
    }

    private boolean hasLiberty(int row, int col, char player, boolean[][] visited, char[][] tempBoard) {
        if (row < 0 || row >= size || col < 0 || col >= size || visited[row][col]) {
            return false;
        }
        if (tempBoard[row][col] == '.') {
            return true;
        }
        if (tempBoard[row][col] != player) {
            return false;
        }

        visited[row][col] = true;

        return hasLiberty(row - 1, col, player, visited, tempBoard)
                || hasLiberty(row + 1, col, player, visited, tempBoard)
                || hasLiberty(row, col - 1, player, visited, tempBoard)
                || hasLiberty(row, col + 1, player, visited, tempBoard);
    }

    private void makeMove(int row, int col) {
        board[row][col] = currentPlayer;
        // Update previous board
        for (int i = 0; i < size; i++) {
            previousBoard[i] = Arrays.copyOf(board[i], size);
        }
    }

    private void captureStones(int row, int col) {
        char opponentPlayer = (currentPlayer == 'X') ? 'O' : 'X';

        if (row > 0 && board[row - 1][col] == opponentPlayer) {
            if (isCaptured(row - 1, col, board)) {
                stonesCapturedO += removeCapturedStones(row - 1, col);
            }
        }
        if (row < size - 1 && board[row + 1][col] == opponentPlayer) {
            if (isCaptured(row + 1, col, board)) {
                stonesCapturedO += removeCapturedStones(row + 1, col);
            }
        }
        if (col > 0 && board[row][col - 1] == opponentPlayer) {
            if (isCaptured(row, col - 1, board)) {
                stonesCapturedO += removeCapturedStones(row, col - 1);
            }
        }
        if (col < size - 1 && board[row][col + 1] == opponentPlayer) {
            if (isCaptured(row, col + 1, board)) {
                stonesCapturedO += removeCapturedStones(row, col + 1);
            }
        }
    }

    private int removeCapturedStones(int row, int col) {
        char player = board[row][col];
        boolean[][] visited = new boolean[size][size];
        return removeCapturedStonesHelper(row, col, player, visited);
    }

    private int removeCapturedStonesHelper(int row, int col, char player, boolean[][] visited) {
        if (row < 0 || row >= size || col < 0 || col >= size || visited[row][col]) {
            return 0;
        }
        if (board[row][col] == player) {
            board[row][col] = '.';
            visited[row][col] = true;
            return 1 + removeCapturedStonesHelper(row - 1, col, player, visited)
                    + removeCapturedStonesHelper(row + 1, col, player, visited)
                    + removeCapturedStonesHelper(row, col - 1, player, visited)
                    + removeCapturedStonesHelper(row, col + 1, player, visited);
        }
        return 0;
    }

    private boolean isGameOver() {
        return consecutivePasses >= 2;
    }

    private void declareWinner() {
        updateScore();
        System.out.println("Game Over!");
        System.out.println("Final Score:");
        System.out.println("White (X): " + scoreX + " points");
        System.out.println("Black (O): " + scoreO + " points");
        System.out.println("White (X) Stones Captured: " + stonesCapturedX);
        System.out.println("Black (O) Stones Captured: " + stonesCapturedO);

        if (scoreX + komi > scoreO) {
            System.out.println("White (X) wins!");
        } else {
            System.out.println("Black (O) wins!");
        }

        gameEnded = true;
    }

    private void updateScore() {
        scoreX = 0;
        scoreO = 0;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board[i][j] == 'X') {
                    scoreX++;
                } else if (board[i][j] == 'O') {
                    scoreO++;
                }
            }
        }

        if (scoreX + komi > scoreO) {
            scoreX += komi; // Add komi to white player's score
        } else {
            scoreO += komi; // Add komi to black player's score
        }
    }

    private void printBoard() {
        System.out.println("  0 1 2 3 4 5 6 7 8");
        for (int i = 0; i < size; i++) {
            System.out.print(i + " ");
            for (int j = 0; j < size; j++) {
                System.out.print(board[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println("Now it's " + ((currentPlayer == 'X') ? "White" : "Black") + " stone's move.");
        System.out.println("White (X) Stones Captured: " + stonesCapturedX);
        System.out.println("Black (O) Stones Captured: " + stonesCapturedO);
    }

    public static void main(String[] args) {
        GoGame game = new GoGame(9, 6.5); // Start with a default board size of 9x9 and komi of 6.5
        game.play();
    }
}
