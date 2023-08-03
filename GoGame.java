


import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class GoGame {
    private int size, stonesCapturedX, stonesCapturedO;
    private char[][] board;
    private char currentPlayer;
    private boolean gameEnded;
    private int consecutivePasses;
    private char[][] previousBoard; // keep track of previous board positions
private String patternName;


    public GoGame(int size) {
        this.size = size;
        board = new char[size][size];
        currentPlayer = 'O'; // Black goes first
        previousBoard = new char[size][size];
    }


//---------the menu------------------------------------------------------------------
public void play() {
    Scanner scanner = new Scanner(System.in);

 
    System.out.println("How do you want to initialize the board?");
    System.out.println("1. Predefined board");
    System.out.println("2. Enter the initial board state manually");
    System.out.println("3. Load from a text file");
    System.out.println("4. Quit");

    int choice = scanner.nextInt();
 
    switch (choice) {
        case 1:
            initializeWithPredefinedBoard();
            break;
        case 2:
            initializeManually(scanner);
            break;
        case 3:
            initializeFromFile();
            break;
        case 4:
            System.out.println("Quitting the game.");
            scanner.close();
            return;
        default:
            System.out.println("Invalid choice. Exiting the game.");
            scanner.close();
            return;
    }
 


    consecutivePasses = 0;
    stonesCapturedX = 0;
    stonesCapturedO = 0;

    printBoard();

    while (!gameEnded) {
        System.out.println("Current Player: " + currentPlayer);
        System.out.print("Enter row (0-" + (size - 1) + "), 'a' for AI help, or 'q' to quit: ");
        String input = scanner.next();

        
       
   if (input.equals("a")) {
            while (true) {
                System.out.println("Which type of AI help do you want?");
                System.out.println("1. Move Suggestion");
                System.out.println("2. Capture Help");
                System.out.println("3. Quit AI Help");
                int aiHelpChoice = scanner.nextInt();

                switch (aiHelpChoice) {
                    case 1:
                        provideAIHelp();
                        break;
                    case 2:
   // Call provideCaptureHelpAI with the current player before making any move
        provideCaptureHelpAI(scanner, currentPlayer); // Pass the scanner as an argument
                        break;
                    case 3:
                        System.out.println("Exiting AI Help.");
                        break;
                    default:
                        System.out.println("Invalid choice.");
                        break;
                }

                if (aiHelpChoice == 3) {
                    break;
                }
            }
        }

        else if (input.equals("q")) {
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

            currentPlayer = (currentPlayer == 'X') ? 'O' : 'X'; // Switch player
        } else if (row == -1 && col == -1) {
            consecutivePasses++;
            currentPlayer = (currentPlayer == 'X') ? 'O' : 'X'; // Switch player
            if (consecutivePasses >= 2) {
                break;
            }
        }
        
        else {
            
            System.out.println("Invalid move. Try again.");
        }
    }

    scanner.close();
}

   



  private void initializeWithPredefinedBoard() {
        size = 9;
        board = new char[][]{
            {'.', '.', '.', '.', '.', '.', 'O', '.', '.'},
            {'.', '.', '.', '.', 'O', '.', 'O', '.', '.'},
            {'.', 'O', '.', '.', 'X', 'O', 'O', '.', '.'},
            {'.', 'X', 'O', 'O', 'X', 'O', 'O', 'O', 'O'},
            {'.', '.', '.', '.', 'O', '.', 'O', '.', '.'},
            {'.', '.', '.', '.', '.', '.', 'O', 'O', 'O'},
            {'.', '.', '.', '.', '.', '.', '.', 'X', '.'},
            {'.', '.', '.', '.', '.', '.', 'X', 'O', 'X'},
            {'.', '.', '.', '.', '.', '.', 'O', 'X', '.'}
        };
        currentPlayer = 'O';
        gameEnded = false;
        consecutivePasses = 0;
        stonesCapturedX = 0;
        stonesCapturedO = 0;
        previousBoard = new char[size][size];
    }

    private void initializeManually(Scanner scanner) {
        System.out.println("Enter the initial board state (use '.' for empty spaces, 'X' for White, 'O' for Black):");
        for (int i = 0; i < size; i++) {
            String rowInput = scanner.next();
            for (int j = 0; j < size; j++) {
                board[i][j] = rowInput.charAt(j);
            }
        }
    }

   private void initializeFromFile() {
    try {
        File file = new File("gopatterns.txt");
        Scanner scanner = new Scanner(file);

        boolean patternNameFound = false;

        // Read the file line by line
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            // Check if the line contains a pattern name (starting with 'P')
            if (line.length() > 0 && line.charAt(0) == 'P' && !patternNameFound) {
                System.out.println(line); // Print the pattern name
                patternNameFound = true;
            } else if (!line.isEmpty()) {
                // Reached the board state, read the board from the text file
                for (int i = 0; i < size; i++) {
                    String rowInput = line;
                    for (int j = 0; j < size; j++) {
                        board[i][j] = rowInput.charAt(j);
                    }
                    if (i < size - 1 && scanner.hasNextLine()) {
                        line = scanner.nextLine();
                    }
                }
                break; // Board reading complete
            }
        }

        scanner.close();
    } catch (FileNotFoundException e) {
        System.out.println("Failed to load the file. Initializing with a predefined board instead.");
        initializeWithPredefinedBoard();
    }
}
 //------------------end of menu--------------------------------------------------------------------------------

//--------------------the ai helpe for move suggustions--------------------
private void provideAIHelp() {
    System.out.println("AI is analyzing the board...");

    int bestRow = -1;
    int bestCol = -1;
    int maxCapturedStones = 0;
    int maxEyeCount = 0;

    // Simulate placing a stone on each empty position and calculate the number of captured stones and eye count
    for (int row = 0; row < size; row++) {
        for (int col = 0; col < size; col++) {
            if (board[row][col] == '.' && isValidMove(row, col)) { // Check if the move is valid (including Ko rule)
                char[][] tempBoard = new char[size][size];
                for (int i = 0; i < size; i++) {
                    tempBoard[i] = Arrays.copyOf(board[i], size);
                }
                tempBoard[row][col] = currentPlayer;
                int capturedStones = simulateCaptureStones(row, col, tempBoard);
                int eyeCount = countEyes(tempBoard);

                if (capturedStones > maxCapturedStones || (capturedStones == maxCapturedStones && eyeCount > maxEyeCount)) {
                    bestRow = row;
                    bestCol = col;
                    maxCapturedStones = capturedStones;
                    maxEyeCount = eyeCount;
                }
            }
        }
    }

    if (bestRow != -1 && bestCol != -1) {
        System.out.println("AI suggests placing a stone at row " + bestRow + ", col " + bestCol);
    } else {
        System.out.println("AI suggests passing the turn.");
    }
}



 
 // ----------end of ai helper----------------------------------------------------------
 
 

 // New method: countEyes
    private int countEyes(char[][] tempBoard) {
        int eyeCount = 0;

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (tempBoard[row][col] == '.') {
                    if (isEye(row, col, currentPlayer, tempBoard)) {
                        eyeCount++;
                    }
                }
            }
        }

        return eyeCount;
    }
// New method: isEye
private boolean isEye(int row, int col, char player, char[][] tempBoard) {
    // Check if the position is empty
    if (tempBoard[row][col] != '.') {
        return false;
    }

    // Check for two or more liberties
    int liberties = 0;
    if (row > 0 && tempBoard[row - 1][col] == '.') {
        liberties++;
    }
    if (row < size - 1 && tempBoard[row + 1][col] == '.') {
        liberties++;
    }
    if (col > 0 && tempBoard[row][col - 1] == '.') {
        liberties++;
    }
    if (col < size - 1 && tempBoard[row][col + 1] == '.') {
        liberties++;
    }

    if (liberties < 2) {
        return false;
    }

    // Check if surrounded by stones of the same color
    char opponentPlayer = (player == 'X') ? 'O' : 'X';
    if ((row > 0 && tempBoard[row - 1][col] != opponentPlayer) ||
            (row < size - 1 && tempBoard[row + 1][col] != opponentPlayer) ||
            (col > 0 && tempBoard[row][col - 1] != opponentPlayer) ||
            (col < size - 1 && tempBoard[row][col + 1] != opponentPlayer)) {
        return false;
    }

    // Check if all adjacent intersections are occupied by the player's stones
    return (row > 0 && tempBoard[row - 1][col] == player) &&
            (row < size - 1 && tempBoard[row + 1][col] == player) &&
            (col > 0 && tempBoard[row][col - 1] == player) &&
            (col < size - 1 && tempBoard[row][col + 1] == player);
}  
   
  
 //This method is used by the AI helper
    private boolean hasLibertyAIHelper(int row, int col, char player, boolean[][] visited, char[][] tempBoard) {
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

        return hasLibertyAIHelper(row - 1, col, player, visited, tempBoard)
                || hasLibertyAIHelper(row + 1, col, player, visited, tempBoard)
                ||hasLibertyAIHelper(row, col - 1, player, visited, tempBoard)
                ||hasLibertyAIHelper(row, col + 1, player, visited, tempBoard);
    }

//-----end of ai methond section---------

// New method: isKoRuleViolation
private boolean isKoRuleViolation(int row, int col, char[][] tempBoard) {
    char player = tempBoard[row][col];

    // Make the move on the temporary board
    tempBoard[row][col] = player;

    // Check if the current board position matches the previous board position
    if (Arrays.deepEquals(tempBoard, previousBoard)) {
        // Undo the move
        tempBoard[row][col] = '.';
        return true;
    }

    // Undo the move
    tempBoard[row][col] = '.';

    return false;
}

//-------------------------------------------------capture stone ai helper ---------------------
  
// New method: provideCaptureHelpAI for AI help in capturing stones
// New method: provideCaptureHelpAI for AI help in capturing stones
private void provideCaptureHelpAI(Scanner scanner, char currentPlayer) {
    System.out.print("Enter row of the opponent's stone to capture: ");
    int row = scanner.nextInt();
    System.out.print("Enter col of the opponent's stone to capture: ");
    int col = scanner.nextInt();

    if (row < 0 || row >= size || col < 0 || col >= size) {
        System.out.println("Invalid coordinates. Please try again.");
        return;
    }

    if (board[row][col] == '.') {
        System.out.println("There is no stone at the specified coordinates. Please try again.");
        return;
    }

    char opponentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
    if (board[row][col] != opponentPlayer) {
        System.out.println("The specified stone does not belong to the opponent. Please try again.");
        return;
    }

    int bestRow = -1;
    int bestCol = -1;
    int maxCapturedStones = 0;
    int maxLiberties = Integer.MAX_VALUE; // Initialize maxLiberties to a high value

    // Create a temporary board to simulate capturing
    char[][] tempBoard = new char[size][size];
    for (int i = 0; i < size; i++) {
        tempBoard[i] = Arrays.copyOf(board[i], size);
    }

    // Generate all possible moves (empty points on the board)
    List<int[]> possibleMoves = new ArrayList<>();
    int[][] surroundingPositions = {{row - 1, col}, {row + 1, col}, {row, col - 1}, {row, col + 1}};
    for (int[] position : surroundingPositions) {
        int r = position[0];
        int c = position[1];
        if (r >= 0 && r < size && c >= 0 && c < size && tempBoard[r][c] == '.') {
            possibleMoves.add(new int[]{r, c});
        }
    }

    // Simulate and evaluate each move using minimax algorithm
    for (int[] move : possibleMoves) {
        int r = move[0];
        int c = move[1];

        if (isValidMove(r, c)) { // Check if the move is valid (including Ko rule)
            // Simulate making the move
            char[][] newTempBoard = new char[size][size];
            for (int i = 0; i < size; i++) {
                newTempBoard[i] = Arrays.copyOf(tempBoard[i], size);
            }
            newTempBoard[r][c] = currentPlayer;
            int newCapturedStones = simulateCaptureStones(r, c, newTempBoard);

            // Count liberties for the opponent after making the move
            int opponentLiberties = countLiberties(opponentPlayer, newTempBoard);

            // Use minimax algorithm to evaluate the board state after the opponent's response
            int score = minimax(newTempBoard, opponentPlayer, 2, false);

            // Update the best move based on the maximized captured stones and minimized liberties
            if (newCapturedStones > maxCapturedStones ||
                (newCapturedStones == maxCapturedStones && opponentLiberties < maxLiberties)) {
                bestRow = r;
                bestCol = c;
                maxCapturedStones = newCapturedStones;
                maxLiberties = opponentLiberties;
            }
        }
    }

    if (bestRow != -1 && bestCol != -1) {
        System.out.println("AI suggests placing a stone at row " + bestRow + ", col " + bestCol +
                " to capture " + maxCapturedStones + " stone(s) and limit opponent's liberties.");
    } else {
        boolean koRuleViolation = false;
        for (int[] move : possibleMoves) {
            int r = move[0];
            int c = move[1];
            if (!isValidMove(r, c)) {
                koRuleViolation = true;
                break;
            }
        }

        if (koRuleViolation) {
            System.out.println("Stone can't be placed because of the Ko rule.");
        } else {
            System.out.println("AI couldn't find a capture move for the specified stone.");
        }
    }
}


// Minimax algorithm implementation

 private int minimax(char[][] board, char player, int depth, boolean isMaximizingPlayer) {
    if (depth == 0) {
        // If the depth is reached or the game has ended, evaluate the board state
        return evaluateBoard(board, player);
    }

    char opponentPlayer = (player == 'X') ? 'O' : 'X';
    int bestScore;

    if (isMaximizingPlayer) {
        bestScore = Integer.MIN_VALUE;
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (board[row][col] == '.') {
                    // Simulate making the move
                    char[][] tempBoard = simulateMove(board, row, col, player);
                    int capturedStones = simulateCaptureStones(row, col, tempBoard);

                    // Count liberties for the opponent after making the move
                    int opponentLiberties = countLiberties(opponentPlayer, tempBoard);

                    // Recursively call minimax for the next depth with the opponent as the maximizing player
                    int score = minimax(tempBoard, opponentPlayer, depth - 1, false);

                    // Adjust the score by the difference in liberties
                    score += capturedStones - opponentLiberties;

                    bestScore = Math.max(bestScore, score);
                }
            }
        }
    } else {
        bestScore = Integer.MAX_VALUE;
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (board[row][col] == '.') {
                    // Simulate making the move
                    char[][] tempBoard = simulateMove(board, row, col, player);
                    int capturedStones = simulateCaptureStones(row, col, tempBoard);

                    // Count liberties for the opponent after making the move
                    int opponentLiberties = countLiberties(opponentPlayer, tempBoard);

                    // Recursively call minimax for the next depth with the opponent as the minimizing player
                    int score = minimax(tempBoard, opponentPlayer, depth - 1, true);

                    // Adjust the score by the difference in liberties
                    score -= capturedStones + opponentLiberties;

                    bestScore = Math.min(bestScore, score);
                }
            }
        }
    }

    return bestScore;
}

// New method: countLiberties
private int countLiberties(char player, char[][] board) {
    int liberties = 0;
    boolean[][] visited = new boolean[size][size];

    for (int row = 0; row < size; row++) {
        for (int col = 0; col < size; col++) {
            if (board[row][col] == player && !visited[row][col]) {
                liberties += countLibertiesHelper(row, col, player, visited, board);
            }
        }
    }

    return liberties;
}

// Helper method for countLiberties
private int countLibertiesHelper(int row, int col, char player, boolean[][] visited, char[][] board) {
    if (row < 0 || row >= size || col < 0 || col >= size || visited[row][col]) {
        return 0;
    }

    if (board[row][col] == '.') {
        return 1;
    }

    if (board[row][col] == player) {
        visited[row][col] = true;
        return countLibertiesHelper(row - 1, col, player, visited, board)
                + countLibertiesHelper(row + 1, col, player, visited, board)
                + countLibertiesHelper(row, col - 1, player, visited, board)
                + countLibertiesHelper(row, col + 1, player, visited, board);
    }

    return 0;
}

 // used for the ai helper
    private int simulateRemoveCapturedStones(int row, int col, char[][] tempBoard) {
        char player = tempBoard[row][col];
        boolean[][] visited = new boolean[size][size];
        return removeCapturedStonesHelperTempBoard(row, col, player, visited, tempBoard);
    }
 

private int removeCapturedStonesHelperTempBoard(int row, int col, char player, boolean[][] visited, char[][] tempBoard) {
        if (row < 0 || row >= size || col < 0 || col >= size || visited[row][col]) {
            return 0;
        }
        if (tempBoard[row][col] == player) {
            tempBoard[row][col] = '.';
            visited[row][col] = true;
            return 1 + removeCapturedStonesHelperTempBoard(row - 1, col, player, visited, tempBoard)
                    + removeCapturedStonesHelperTempBoard(row + 1, col, player, visited, tempBoard)
                    + removeCapturedStonesHelperTempBoard(row, col - 1, player, visited, tempBoard)
                    + removeCapturedStonesHelperTempBoard(row, col + 1, player, visited, tempBoard);
        }
        return 0;
    }
 

private char[][] simulateMove(char[][] board, int row, int col, char player) {
    char[][] newBoard = new char[size][size];
    for (int i = 0; i < size; i++) {
        newBoard[i] = Arrays.copyOf(board[i], size);
    }
    newBoard[row][col] = player;
    return newBoard;
}

// Evaluate the board state based on the number of opponent's stones
private int evaluateBoard(char[][] board, char player) {
    char opponentPlayer = (player == 'X') ? 'O' : 'X';
    int opponentStones = 0;

    for (int row = 0; row < size; row++) {
        for (int col = 0; col < size; col++) {
            if (board[row][col] == opponentPlayer) {
                opponentStones++;
            }
        }
    }

    return opponentStones;
}

  
 // ----- end of capture heler ai section-----------

  // New method: simulateCaptureStones ai helper
 private int simulateCaptureStones(int row, int col, char[][] tempBoard) {
    char opponentPlayer = (currentPlayer == 'X') ? 'O' : 'X';

    int capturedStones = 0;
    List<int[]> adjacentOpponentStones = new ArrayList<>();

    if (row > 0 && tempBoard[row - 1][col] == opponentPlayer) {
        adjacentOpponentStones.add(new int[] {row - 1, col});
    }
    if (row < size - 1 && tempBoard[row + 1][col] == opponentPlayer) {
        adjacentOpponentStones.add(new int[] {row + 1, col});
    }
    if (col > 0 && tempBoard[row][col - 1] == opponentPlayer) {
        adjacentOpponentStones.add(new int[] {row, col - 1});
    }
    if (col < size - 1 && tempBoard[row][col + 1] == opponentPlayer) {
        adjacentOpponentStones.add(new int[] {row, col + 1});
    }

    boolean[][] visited = new boolean[size][size];
    for (int[] stone : adjacentOpponentStones) {
        if (!visited[stone[0]][stone[1]] && isGroupCaptured(stone[0], stone[1], tempBoard)) {
            capturedStones += simulateRemoveCapturedStones(stone[0], stone[1], tempBoard);
        }
    }

    return capturedStones;
}
// New method: isGroupCaptured with correct parameters
private boolean isGroupCaptured(int row, int col, char[][] tempBoard) {
    char player = tempBoard[row][col];
    boolean[][] visited = new boolean[size][size];
    return !hasLibertyAIHelper(row, col, player, visited, tempBoard);
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
    return !Arrays.deepEquals(tempBoard, previousBoard)   && !isKoRuleViolation(row, col, tempBoard);
}
 
  
    // these arent used for the ai these show the update of capture stones-----------------
private void makeMove(int row, int col) {
        board[row][col] = currentPlayer;
      
        // Update previous board
        for (int i = 0; i < size; i++) {
           
            previousBoard[i] = Arrays.copyOf(board[i], size);
        }
    }

    private boolean isStoneCaptured(int row, int col) {
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
 

    private void captureStones(int row, int col) {
        char opponentPlayer = (currentPlayer == 'X') ? 'O' : 'X';

        int stonesCaptured = 0; // Variable to keep track of the captured stones

        if (row > 0 && board[row - 1][col] == opponentPlayer) {
            if (isStoneCaptured(row - 1, col)) {
                stonesCaptured += removeCapturedStones(row - 1, col);
            }
        }
        if (row < size - 1 && board[row + 1][col] == opponentPlayer) {
            if (isStoneCaptured(row + 1, col)) {
                stonesCaptured += removeCapturedStones(row + 1, col);
            }
        }
        if (col > 0 && board[row][col - 1] == opponentPlayer) {
            if (isStoneCaptured(row, col - 1)) {
                stonesCaptured += removeCapturedStones(row, col - 1);
            }
        }
        if (col < size - 1 && board[row][col + 1] == opponentPlayer) {
            if (isStoneCaptured(row, col + 1)) {
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

//-------------------  these arent used for the ai these show the update of capture stones-----------------


    private void printBoard() {


    // Print the pattern name if available
    if (patternName != null) {
        System.out.println(patternName);
    }
       
        System.out.println("  0 1 2 3 4 5 6 7 8");
       
        for (int i = 0; i < size; i++) {
           
            System.out.print(i + " ");
           
            for (int j = 0; j < size; j++) {
              
                System.out.print(board[i][j] + " ");
            }
            
            System.out.println();
        }
       
        System.out.println("White (X) Stones Captured: " + stonesCapturedX);
        System.out.println("Black (O) Stones Captured: " + stonesCapturedO);
    }

    public static void main(String[] args) {
       
        GoGame game = new GoGame(9); // Start with a default board size of 9x9 and komi of 6.5
        game.play();

    }
}