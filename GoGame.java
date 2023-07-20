import java.util.Arrays;
import java.util.Scanner;

public class GoGame {
    private int size, stonesCapturedX, stonesCapturedO;
    private char[][] board;
    private char currentPlayer;
   private double scoreX, scoreO;
    private boolean gameEnded;
    private int consecutivePasses;
    private double komi;
    private char[][] previousBoard; // keep track of previous board positions

    public GoGame(int size, double komi) {
        this.size = size;
       // this.komi = komi;
        board = new char[size][size];
        currentPlayer = 'O'; // Black goes first
        previousBoard = new char[size][size];
    }

    public void play() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Do you want to enter the initial board state manually? (Y/N)");
        String choice = scanner.next();

        if (choice.equalsIgnoreCase("Y")) {
            System.out.println("Enter the initial board state (use '.' for empty spaces, 'X' for White, 'O' for Black):");
            for (int i = 0; i < size; i++) {
                String rowInput = scanner.next(); 
                for (int j = 0; j < size; j++) {
                    board[i][j] = rowInput.charAt(j);
                }
            }
        } else {
            // Use predefined board
            board = new char[][]{
                {'.', '.', '.', '.', '.', '.', '.', '.', '.'},
                {'.', '.', '.', '.', '.', '.', '.', '.', '.'},
                {'.', '.', '.', '.', '.', '.', '.', '.', '.'},
                {'.', '.', '.', '.', '.', '.', '.', '.', '.'},
                {'.', '.', '.', '.', '.', '.', '.', '.', '.'},
                {'.', '.', '.', '.', '.', '.', '.', '.', '.'},
                {'.', '.', '.', '.', '.', '.', 'O', '.', '.'},
                {'.', '.', '.', '.', '.', 'O', 'X', '.', '.'},
                {'.', '.', '.', '.', '.', '.', '.', '.', '.'}
            };
        }

        consecutivePasses = 0;
        stonesCapturedX = 0;
        stonesCapturedO = 0;

        while (!gameEnded) {
            System.out.println("Current Player: " + currentPlayer);
            System.out.print("Enter row (0-" + (size - 1) + ") or 'e' to end the game, or 'a' for AI help: ");
            String input = scanner.next();

           
           /*  if (input.equals("e")) {
                declareWinner();
               break;
            }*/

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

              //  if (isGameOver()) {
                  //  declareWinner();
                //    break;
              //  }

                currentPlayer = (currentPlayer == 'X') ? 'O' : 'X'; // Switch player
            } 
            
            else if (row == -1 && col == -1) {
                consecutivePasses++;
                currentPlayer = (currentPlayer == 'X') ? 'O' : 'X'; // Switch player
               // if (consecutivePasses >= 2) {
                  //  declareWinner();
                  //  break;
               // }
            } 
            
            else {
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

    int stonesCaptured = 0; // Variable to keep track of the captured stones

    if (row > 0 && board[row - 1][col] == opponentPlayer) {
        if (isCaptured(row - 1, col)) {
            stonesCaptured += removeCapturedStones(row - 1, col);
        }
    }
    if (row < size - 1 && board[row + 1][col] == opponentPlayer) {
        if (isCaptured(row + 1, col)) {
            stonesCaptured += removeCapturedStones(row + 1, col);
        }
    }
    if (col > 0 && board[row][col - 1] == opponentPlayer) {
        if (isCaptured(row, col - 1)) {
            stonesCaptured += removeCapturedStones(row, col - 1);
        }
    }
    if (col < size - 1 && board[row][col + 1] == opponentPlayer) {
        if (isCaptured(row, col + 1)) {
            stonesCaptured += removeCapturedStones(row, col + 1);
        }
    }

    // Update the score for the appropriate player
    if (currentPlayer == 'O') {
        stonesCapturedX += stonesCaptured;
    } else {
        stonesCapturedO += stonesCaptured;
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

   
   // private boolean isGameOver() {
      //  return consecutivePasses >= 2;
   // }

    private void printBoard() {
        System.out.println("  0 1 2 3 4 5 6 7 8");
        for (int i = 0; i < size; i++) {
            System.out.print(i + " ");
            for (int j = 0; j < size; j++) {
                System.out.print(board[i][j] + " ");
            }
            System.out.println();
        }
       // System.out.println("Now it's " + ((currentPlayer == 'X') ? "White" : "Black") + " stone's move.");
        System.out.println("White (X) Stones Captured: " + stonesCapturedX);
        System.out.println("Black (O) Stones Captured: " + stonesCapturedO);
    }

    public static void main(String[] args) {
        GoGame game = new GoGame(9, 6.5); // Start with a default board size of 9x9 and komi of 6.5
        game.play();
    }}





/*private void declareWinner() {
    char[][] territories = calculateTerritories(); // Calculate the territories
    int[] points = countTerritoryPoints(territories); // Count the territory points for each player
    int territoryPointsX = points[0];
    int territoryPointsO = points[1];

    // Calculate the final score based on territory and captured stones
    double scoreX = territoryPointsX+komi - stonesCapturedX;
    double scoreO = territoryPointsO - stonesCapturedO;

    System.out.println("Game Over!");
    System.out.println("Final Score:");
    System.out.println("White (X) Territory Points: " + territoryPointsX);
    System.out.println("White (X) Stones Captured: " + stonesCapturedX);
    System.out.println("White (X) Score: " + scoreX);
    System.out.println("Black (O) Territory Points: " + territoryPointsO);
    System.out.println("Black (O) Stones Captured: " + stonesCapturedO);
    System.out.println("Black (O) Score: " + scoreO);

    if (scoreX > scoreO) {
        System.out.println("White (X) wins!");
    } else if (scoreX < scoreO) {
        System.out.println("Black (O) wins!");
    } else {
        System.out.println("It's a tie!");
    }

    gameEnded = true;
}




private char[][] calculateTerritories() {
    char[][] territories = new char[size][size];

    // Initialize all territories as empty
    for (int i = 0; i < size; i++) {
        Arrays.fill(territories[i], '.');
    }

    // Iterate over each position on the board
    for (int row = 0; row < size; row++) {
        for (int col = 0; col < size; col++) {
            if (board[row][col] == '.') {
                // If the position is empty, check if it is surrounded by a single player
                char player = getSurroundingPlayer(row, col);
                if (player != '.') {
                    territories[row][col] = player;
                }
            }
        }
    }

    return territories;
}


private char getSurroundingPlayer(int row, int col) {
    char player = '.';
    boolean hasMultiplePlayers = false;

    // Check the neighboring positions
    if (row > 0) {
        if (player == '.') {
            player = board[row - 1][col];
        } else if (player != board[row - 1][col]) {
            hasMultiplePlayers = true;
        }
    }
    if (row < size - 1) {
        if (player == '.') {
            player = board[row + 1][col];
        } else if (player != board[row + 1][col]) {
            hasMultiplePlayers = true;
        }
    }
    if (col > 0) {
        if (player == '.') {
            player = board[row][col - 1];
        } else if (player != board[row][col - 1]) {
            hasMultiplePlayers = true;
        }
    }
    if (col < size - 1) {
        if (player == '.') {
            player = board[row][col + 1];
        } else if (player != board[row][col + 1]) {
            hasMultiplePlayers = true;
        }
    }

    if (hasMultiplePlayers) {
        return '.'; // Return '.' if there are multiple players surrounding the position
    }

    return player; // Return the single player if found
}

private double calculateScore(char player, char[][] territories) {
    double score = 0;

    for (int row = 0; row < size; row++) {
        for (int col = 0; col < size; col++) {
            if (territories[row][col] == player) {
                score++;
            }
        }
    }

    return score;
}


 private int[] countTerritoryPoints(char[][] territories) {
    int[] points = new int[2]; // Index 0 for player X, Index 1 for player O

    for (int row = 0; row < size; row++) {
        for (int col = 0; col < size; col++) {
            if (territories[row][col] == 'X') {
                points[0]++;
            } else if (territories[row][col] == 'O') {
                points[1]++;
            }
        }
    }

    return points;
}

 private void updateScore() {
    char[][] territories = calculateTerritories(); // Calculate the territories
    scoreX = calculateScore('X', territories); // Calculate the score for player X
    scoreO = calculateScore('O', territories); // Calculate the score for player O

    // Add komi to white player's score
    scoreX += komi;
} */


