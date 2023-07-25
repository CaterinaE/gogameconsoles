import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
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


//---------the menu----------------------
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
            {'.', '.', '.', '.', '.', '.', 'O', '.', '.'},
            {'.', '.', '.', '.', '.', '.', 'O', '.', '.'},
            {'.', '.', '.', '.', '.', '.', 'O', 'O', 'O'},
            {'.', '.', '.', '.', 'O', '.', 'O', '.', '.'},
            {'.', '.', '.', '.', '.', '.', 'O', 'O', 'O'},
            {'.', '.', '.', '.', '.', '.', '.', '.', '.'},
            {'.', '.', '.', '.', '.', '.', '.', 'O', '.'},
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
    // New method: provideAIHelp
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
        int maxEyeCount = 0;

        // Simulate placing a stone on each empty position and calculate the number of captured stones and eye count
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (tempBoard[row][col] == '.') {
                    tempBoard[row][col] = currentPlayer;
                    int capturedStones = simulateCaptureStones(row, col, tempBoard);
                    int eyeCount = countEyes(tempBoard);

                    if (capturedStones > maxCapturedStones || (capturedStones == maxCapturedStones && eyeCount > maxEyeCount)) {
                        bestRow = row;
                        bestCol = col;
                        maxCapturedStones = capturedStones;
                        maxEyeCount = eyeCount;
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
 // ----------end of ai helper----------------------------------------------------------

    // New method: simulateCaptureStones ai helper
    private int simulateCaptureStones(int row, int col, char[][] tempBoard) {
        char opponentPlayer = (currentPlayer == 'X') ? 'O' : 'X';

        int capturedStones = 0;

        if (row > 0 && tempBoard[row - 1][col] == opponentPlayer) {
            if (isGroupCaptured(row - 1, col, tempBoard)) {
                capturedStones += simulateRemoveCapturedStones(row - 1, col, tempBoard);
            }
        }
        if (row < size - 1 && tempBoard[row + 1][col] == opponentPlayer) {
            if (isGroupCaptured(row + 1, col, tempBoard)) {
                capturedStones += simulateRemoveCapturedStones(row + 1, col, tempBoard);
            }
        }
        if (col > 0 && tempBoard[row][col - 1] == opponentPlayer) {
            if (isGroupCaptured(row, col - 1, tempBoard)) {
                capturedStones += simulateRemoveCapturedStones(row, col - 1, tempBoard);
            }
        }
        if (col < size - 1 && tempBoard[row][col + 1] == opponentPlayer) {
            if (isGroupCaptured(row, col + 1, tempBoard)) {
                capturedStones += simulateRemoveCapturedStones(row, col + 1, tempBoard);
            }
        }

        return capturedStones;
    }
// New method: is used for the ai helper 
    private boolean isGroupCaptured(int row, int col, char[][] tempBoard) {
        char player = tempBoard[row][col];
        boolean[][] visited = new boolean[size][size];
        return !hasLibertyAIHelper(row, col, player, visited, tempBoard) && !isKoRuleViolation(row, col, tempBoard);
    }
 // used for the ai helper
    private int simulateRemoveCapturedStones(int row, int col, char[][] tempBoard) {
        char player = tempBoard[row][col];
        boolean[][] visited = new boolean[size][size];
        return removeCapturedStonesHelperTempBoard(row, col, player, visited, tempBoard);
    }



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



//-------------------------------------------------capture stone ai helper ---------------------
 
 
// New method: provideCaptureHelpAI for AI help in capturing stones
private void provideCaptureHelpAI(Scanner scanner, char currentPlayer) {
    System.out.print("Enter row of the stone to capture: ");
    int row = scanner.nextInt();
    System.out.print("Enter col of the stone to capture: ");
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
    char[][] tempBoard = new char[size][size];
    for (int i = 0; i < size; i++) {
        tempBoard[i] = Arrays.copyOf(board[i], size);
    }

    // Check all the positions around the opponent's stone
    int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
    for (int[] direction : directions) {
        int newRow = row + direction[0];
        int newCol = col + direction[1];

        if (newRow >= 0 && newRow < size && newCol >= 0 && newCol < size && tempBoard[newRow][newCol] == '.') {
            // Simulate capturing the specified stone
            tempBoard[newRow][newCol] = currentPlayer;
            int capturedStones = simulateCaptureStones(newRow, newCol, tempBoard);

            if (capturedStones > 0) {
                System.out.println("AI suggests placing a stone at row " + newRow + ", col " + newCol + " to capture " + capturedStones + " stone(s).");
                return;
            }
        }
    }

    System.out.println("AI couldn't find a capture move for the specified stone.");
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
    return !Arrays.deepEquals(tempBoard, previousBoard) &&   !isGroupCaptured(row, col, tempBoard)  && !isKoRuleViolation(row, col, tempBoard);
}

 
 
 // ----- enf of capture heler ai section-----------

  
 
  
 
 
 
 private void makeMove(int row, int col) {
        board[row][col] = currentPlayer;
      
        // Update previous board
        for (int i = 0; i < size; i++) {
           
            previousBoard[i] = Arrays.copyOf(board[i], size);
        }
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


    // these arent used for the ai these show the update of capture stones-----------------
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
