 

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;

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
            {'.', 'O', '.', '.', 'X', '.', 'O', '.', '.'},
            {'.', 'X', 'O', 'O', 'X', 'O', 'O', 'O', 'O'},
            {'.', '.', '.', '.', 'O', '.', 'O', '.', '.'},
            {'.', '.', '.', '.', '.', '.', '.', '.', '.'},
            {'.', '.', '.', '.', 'X', 'X', 'X', 'X', 'X'},
            {'.', '.', '.', '.', 'X', 'O', 'O', 'O', 'O'},
            {'.', '.', '.', '.', 'X', 'O', '.', '.', '.'}
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
  
private void provideCaptureHelpAI(Scanner scanner, char currentPlayer) {
      int bestRow = -1;
    int bestCol = -1;
    int maxCapturedStones = 0;
    int maxLiberties = Integer.MAX_VALUE; // Initialize maxLiberties to a high value
// Depth level to think two moves ahead
  int depth = 3;
    
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

 
    // Create a temporary board to simulate capturing
    // Simulate the best move to capture the opponent's stone
char[][] tempBoard = new char[size][size];
   for (int i = 0; i < size; i++) {
        tempBoard[i] = Arrays.copyOf(board[i], size);
    }

  

    // Generate all possible moves (empty points on the board) for the old approach
    List<int[]> possibleMoves = new ArrayList<>();
    int[][] surroundingPositions = {{row - 1, col}, {row + 1, col}, {row, col - 1}, {row, col + 1}};
    for (int[] position : surroundingPositions) {
        int r = position[0];
        int c = position[1];
        if (r >= 0 && r < size && c >= 0 && c < size && tempBoard[r][c] == '.') {
            possibleMoves.add(new int[]{r, c});
        }
    }

    // Simulate and evaluate each move using minimax algorithm for the old approach
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
 // Find group liberties using the new approach
Set<String> groupLiberties = findGroupLiberties(row, col, opponentPlayer);

// Simulate capturing the group and count the number of stones captured
int capturedStonesInGroup = 0;
char[][] tempBoardCaptureGroup = new char[size][size];
for (int i = 0; i < size; i++) {
    tempBoardCaptureGroup[i] = Arrays.copyOf(tempBoard[i], size);
}

for (String liberty : groupLiberties) {
    String[] libertyCoordinates = liberty.split(",");
    int libertyRow = Integer.parseInt(libertyCoordinates[0]);
    int libertyCol = Integer.parseInt(libertyCoordinates[1]);

    if (tempBoardCaptureGroup[libertyRow][libertyCol] != '.') {
        // Mark the stone as captured and remove it from the board
        tempBoardCaptureGroup[libertyRow][libertyCol] = '.';
        capturedStonesInGroup++;
    }
}

if (capturedStonesInGroup > 0) {
    String[] libertyCoordinates = groupLiberties.iterator().next().split(",");
    int libertyRow = Integer.parseInt(libertyCoordinates[0]);
    int libertyCol = Integer.parseInt(libertyCoordinates[1]);

    System.out.println("AI suggests placing a stone at row " + libertyRow + ", col " + libertyCol +
            " to capture " + capturedStonesInGroup + " stone(s) in a group and limit opponent's liberties.");
    return;
}

     // Use the minimax method to get the best score and set the flag to true before calling it
    int bestScore = minimax(tempBoard, currentPlayer, depth, true);

    if (bestRow != -1 && bestCol != -1) {
        System.out.println("AI suggests placing a stone at row " + bestRow + ", col " + bestCol +
                " to capture " + maxCapturedStones + " stone(s) and limit opponent's liberties.\n");
    }

       // Simulate the best move to capture the opponent's stone
    char[][] newBoardAfterCapture = simulateMove(board, bestRow, bestCol, currentPlayer);

    int bestResponseRow = -1;
    int bestResponseCol = -1;
    int bestResponseScore = Integer.MIN_VALUE;

    for (int r = 0; r < size; r++) {
        for (int c = 0; c < size; c++) {
            if (newBoardAfterCapture[r][c] == '.') {
                char[][] newTempBoard = simulateMove(newBoardAfterCapture, r, c, opponentPlayer);
                int score = minimax(newTempBoard, currentPlayer, depth - 1, false);
                if (score > bestResponseScore) {
                    bestResponseScore = score;
                    bestResponseRow = r;
                    bestResponseCol = c;
                }
            }
        }
    }
   
             // the second move for the minimax without taking into acount what the player selected player
       
        // the second move for the player
        char[][] tempBoardPlayerMove = simulateMove(newBoardAfterCapture, bestResponseRow, bestResponseCol, currentPlayer);
        int[] playerMove = findBestCaptureMove(tempBoardPlayerMove, maxCapturedStones, currentPlayer);
        int playerMoveRow = playerMove[0];
        int playerMoveCol = playerMove[1];
        System.out.println("\nPlayer should place a stone at row " + playerMoveRow + ", col " + playerMoveCol +
                " to capture " + playerMove[2] + " stone(s) and limit opponent's liberties.\n");
 
 
                // the second move for the player
    if (bestResponseRow != -1 && bestResponseCol != -1) {
        System.out.println("The minimax's  best move  capturing the opponent's stone, your best next move is at row "
                + bestResponseRow + ", col " + bestResponseCol + ". " +maxCapturedStones);



    }  else {
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
            System.out.println("Stone can't be placed because of the Ko rule.\n");
        } else {
            System.out.println("AI couldn't find a capture move for the specified stone.\n");
        }

        
    }
}




private int[] findBestCaptureMove(char[][] board, int opponentCapturedStones, char currentPlayer) {
    int[] bestMove = new int[]{-1, -1, 0}; // {row, col, capturedStones}
    for (int row = 0; row < size; row++) {
        for (int col = 0; col < size; col++) {
            if (board[row][col] == '.') {
                char[][] tempBoard = simulateMove(board, row, col, currentPlayer);
                int capturedStones = simulateCaptureStones(row, col, tempBoard);

                if (capturedStones > bestMove[2] && capturedStones > opponentCapturedStones) {
                    bestMove[0] = row;
                    bestMove[1] = col;
                    bestMove[2] = capturedStones;
                }
            }
        }
    }
    return bestMove;
}


private Set<String> findGroupLiberties(int row, int col, char player) {
    Set<String> groupLiberties = new HashSet<>();
    boolean[][] visited = new boolean[size][size];
    Stack<int[]> stack = new Stack<>();
    stack.push(new int[]{row, col});
    while (!stack.isEmpty()) {
        int[] current = stack.pop();
        int r = current[0];
        int c = current[1];
        if (r < 0 || r >= size || c < 0 || c >= size || visited[r][c] || board[r][c] != player) continue;
        visited[r][c] = true;
        
        int[][] surroundingPositions = {{r - 1, c}, {r + 1, c}, {r, c - 1}, {r, c + 1}};
        boolean isEye = true; // Assume it's an eye until proven otherwise
        for (int[] position : surroundingPositions) {
            int nr = position[0];
            int nc = position[1];
            if (nr >= 0 && nr < size && nc >= 0 && nc < size) {
                if (board[nr][nc] == '.') {
                    groupLiberties.add(nr + "," + nc);
                } else if (board[nr][nc] != player) {
                    isEye = false; // Not an eye if the surrounding point isn't the player's stone
                }
            } else {
                isEye = false; // Not an eye if the point is on the edge of the board
            }
        }
        
        // If it's an eye, remove it from liberties
        if (isEye) {
            groupLiberties.remove(r + "," + c);
        }
        
        // Continue the search with neighboring stones of the same player
        for (int[] position : surroundingPositions) {
            int nr = position[0];
            int nc = position[1];
            if (nr >= 0 && nr < size && nc >= 0 && nc < size && board[nr][nc] == player) {
                stack.push(new int[]{nr, nc});
            }
        }
    }
    return groupLiberties;
}
public void printBoard(char[][] board) {
    for (int row = 0; row < board.length; row++) {
        for (int col = 0; col < board[0].length; col++) {
            System.out.print(board[row][col] + " ");
        }
        System.out.println();
    }
}
boolean printFlag = true; // Add this flag before the loop

private int minimax(char[][] board, char player, int depth, boolean isMaximizingPlayer) {
    if (depth == 0) {
        // If the depth is reached, evaluate the board state
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

                    // Recursively call minimax for the next depth with the opponent as the minimizing player
                    int score = minimax(tempBoard, opponentPlayer, depth - 1, false);

                    // Adjust the score by the difference in liberties
                    score += capturedStones;

                    bestScore = Math.max(bestScore, score);

                if (capturedStones > 0 && score == bestScore && printFlag) {
    System.out.println("\n(......Move you didnt select for capture which is the best move, MIN_VALUE Depth: " + depth + ", Row: " + row + ", Col: " + col + ", capturedStones: " + capturedStones +"......)\n");
   // printBoard(tempBoard);
    printFlag = false; // Set the flag to false after printing once

}
   //   if (capturedStones > 0 && score == bestScore && (row== 4 && col==1)  ) {
   // System.out.println("\n(......Move you didnt select for capture which is the best move, MIN_VALUE Depth: " + depth + ", Row: " + row + ", Col: " + col + ", capturedStones: " + capturedStones +".....score"+ bestScore+"......)\n");
    // printBoard(tempBoard);}

   
   // printFlag = false; // Set the flag to false after printing once
      //}
            
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

                    // Recursively call minimax for the next depth with the opponent as the maximizing player
                    int score = minimax(tempBoard, opponentPlayer, depth - 1, true);

                    // Adjust the score by the difference in liberties
                    score -= capturedStones;

                    bestScore = Math.min(bestScore, score);
 // if (capturedStones > 0 && score == bestScore && (row== 3 && col==0)  ) {
  //  System.out.println("\n(......Move you didnt select for capture which is the best move, MIN_VALUE Depth: " + depth + ", Row: " + row + ", Col: " + col + ", capturedStones: " + capturedStones +".....score"+ bestScore+"......)\n");
   //  printBoard(tempBoard);}
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
    if (row < 0 || row >= board.length || col < 0 || col >= board[0].length) {
        // Invalid move, return the original board
        return board;
    }

    char[][] newBoard = new char[board.length][board[0].length];
    for (int i = 0; i < board.length; i++) {
        newBoard[i] = Arrays.copyOf(board[i], board[i].length);
    }

    if (newBoard[row][col] != '.') {
        // Invalid move, return the original board
        return board;
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

