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

        // Select board size
        System.out.print("Enter board size (e.g., 9): ");
        size = scanner.nextInt();
        //board size
        board = new char[size][size];

        initializeBoard();
        consecutivePasses = 0;
        stonesCapturedX = 0;
        stonesCapturedO = 0;

        while (!gameEnded) {
            System.out.println("Current Player: " + currentPlayer);
            System.out.print("Enter row (0-" + (size - 1) + ") or 'e' to end the game: ");
            String input = scanner.next();

            if (input.equals("e")) {
                declareWinner();
                break;
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
        // Create a temporary board to compare with previous board
        char[][] tempBoard = new char[size][size];
        for (int i = 0; i < size; i++) {
            tempBoard[i] = Arrays.copyOf(board[i], size);
        }
        // Check if the temporary board matches the previous board
        return !Arrays.deepEquals(tempBoard, previousBoard);
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
        if (isCaptured(row - 1, col)) {
            stonesCapturedO += removeCapturedStones(row - 1, col);
        }
    }
    if (row < size - 1 && board[row + 1][col] == opponentPlayer) {
        if (isCaptured(row + 1, col)) {
            stonesCapturedO += removeCapturedStones(row + 1, col);
        }
    }
    if (col > 0 && board[row][col - 1] == opponentPlayer) {
        if (isCaptured(row, col - 1)) {
            stonesCapturedO += removeCapturedStones(row, col - 1);
        }
    }
    if (col < size - 1 && board[row][col + 1] == opponentPlayer) {
        if (isCaptured(row, col + 1)) {
            stonesCapturedO += removeCapturedStones(row, col + 1);
        }
    }
}

    private boolean isCaptured(int row, int col) {
        char player = board[row][col];
        boolean[][] visited = new boolean[size][size];
        return !hasLiberty(row, col, player, visited);
    }

    private boolean hasLiberty(int row, int col, char player, boolean[][] visited) {
        if (row < 0 || row >= size || col < 0 || col >= size || visited[row][col]) {
            return false;
        }
        if (board[row][col] == '.') {
            return true;
        }
        if (board[row][col] != player) {
            return false;
        }

        visited[row][col] = true;

        return hasLiberty(row - 1, col, player, visited)
                || hasLiberty(row + 1, col, player, visited)
                || hasLiberty(row, col - 1, player, visited)
                || hasLiberty(row, col + 1, player, visited);
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
        } 
        
        else  {
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
        System.out.println();
        System.out.println("Current Player: " + currentPlayer);
        System.out.println("Now it's " + ((currentPlayer == 'X') ? "White" : "Black") + " stone's move.");
        System.out.println("White (X) Stones Captured: " + stonesCapturedX);
        System.out.println("Black (O) Stones Captured: " + stonesCapturedO);
    }

    public static void main(String[] args) {
        GoGame game = new GoGame(9, 6.5); // Start with a default board size of 9x9 and komi of 6.5
        game.play();
    }
}
