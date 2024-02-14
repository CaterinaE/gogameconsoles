import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class GoGame {
    private int stonesCapturedX, stonesCapturedO, consecutivePasses;
    private static char currentPlayer;
    private boolean gameEnded;
    private static char[][] previousBoard; // keep track of previous board positions
    private String patternName;
    public Board boardInstance;
    public static int size;
    public static char[][] board;

    public GoGame(int size) {
        GoGame.size = size;
        board = new char[size][size];
        currentPlayer = 'O'; // Black goes first
        boardInstance = new Board();
        previousBoard = new char[size][size];
    }

    // ---------the
    // menu------------------------------------------------------------------
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
                Board.initializeWithPredefinedBoard(board, size);
                break;
            case 2:
                Board.initializeManually(scanner, board, size);
                break;
            case 3:
                Board.initializeFromFile(board, size);
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
                    System.out.println("1. Move Stone Suggestion");
                    System.out.println("2. Capture Help");
                    System.out.println("3. Quit");// will just go to board
                    int aiHelpChoice = scanner.nextInt();

                    switch (aiHelpChoice) {
                        case 1:
                            AIHelper.provideAIHelp(board, currentPlayer, size);
                            break;
                        case 2:
                            // Call provideCaptureHelpAI with the current player before making any move
                            CaptureHelper.provideCaptureHelpAI(scanner, currentPlayer, size, board);

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
                    printBoard();

                }
                // Print the board after AI help is provided
                printBoard();
                // Skip parsing the row input and continue to the next iteration of the loop
                continue;
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
    // ------------------end of
    // menu--------------------------------------------------------------------------------

    // This method is used by the AI helper
    private static boolean hasLibertyAIHelper(int row, int col, char player, boolean[][] visited, char[][] tempBoard) {
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
                || hasLibertyAIHelper(row, col - 1, player, visited, tempBoard)
                || hasLibertyAIHelper(row, col + 1, player, visited, tempBoard);
    }

    // -----end of ai methond section---------

    // --------------the rules of go--------------------------

    //  isKoRuleViolation the ko rule for game
    static boolean isKoRuleViolation(int row, int col, char[][] tempBoard) {
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

    // countEyes is used in the ai helper move suggustion
    static int countEyes(char[][] tempBoard) {
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

    // isEye is used in the ai helper move suggustion
    private static boolean isEye(int row, int col, char player, char[][] tempBoard) {
        // Check if the position is empty
        if (tempBoard[row][col] != '.') {
            return false;
        }

        // Check if surrounded by stones of the current player
        char opponentPlayer = (player == 'X') ? 'O' : 'X';

        boolean isSurrounded = true;
        if (row > 0)
            isSurrounded &= (tempBoard[row - 1][col] != opponentPlayer);
        if (row < size - 1)
            isSurrounded &= (tempBoard[row + 1][col] != opponentPlayer);
        if (col > 0)
            isSurrounded &= (tempBoard[row][col - 1] != opponentPlayer);
        if (col < size - 1)
            isSurrounded &= (tempBoard[row][col + 1] != opponentPlayer);

        return isSurrounded;
    }

    static void blockPotentialEyes(char[][] tempBoard, char currentPlayer, int row, int col) {
        char opponentPlayer = (currentPlayer == 'X') ? 'O' : 'X';

        // Detect the group of stones connected to the given stone
        List<int[]> stoneGroup = getGroupOfStones(row, col, opponentPlayer);

        // Generate capturing moves against the group
        List<int[]> capturingMoves = generateCapturingMoves(tempBoard, currentPlayer, stoneGroup, size);
        Collections.sort(capturingMoves, (move1, move2) -> Integer.compare(move2[3], move1[3]));

        System.out.println("\nCapturing Moves:");
        for (int[] move : capturingMoves) {
            row = move[0];
            col = move[1];
            int opponentLiberties = calculateOpponentLiberties(row, col, opponentPlayer, size, board);

            System.out.println("Row: " + row + ", Col: " + col +
                    " Opponent's Liberties: " + opponentLiberties);
        }
        System.out.println();

        // Choose the best capturing move
        int[] bestMove = findBestCaptureMoveForGroup(tempBoard, stoneGroup, 0, currentPlayer);
        List<int[]> potentialEyes = CaptureHelper.detectPotentialEyesForGroup(stoneGroup, currentPlayer, size, board);

        // Print the stone group in a readable format
        System.out.println("\nStone Group: ");
        for (int[] stone : stoneGroup) {
            System.out.print("[" + stone[0] + ", " + stone[1] + "] ");
        }
        System.out.println();

        // Print potential eyes
        System.out.println("\nPotential Eyes:");
        for (int[] eye : potentialEyes) {
            System.out.println("Row: " + eye[0] + ", Col: " + eye[1]);
        }
        System.out.println();

        if (bestMove[0] != -1 && bestMove[1] != -1) {
            int r = bestMove[0];
            int c = bestMove[1];
            tempBoard[r][c] = currentPlayer;
            System.out.println("AI suggests placing a stone at row " + r + ", col " + c +
                    " to capture " + bestMove[2] + " stone(s) and limit opponent's liberties.");
        } else {
           // System.out.println("AI couldn't find a suitable move to capture the specified stone group.");
        }
    }

    static int calculateOpponentLiberties(int row, int col, char opponentPlayer, int size, char[][] board) {
        int liberties = 0;
        int[][] surroundingPositions = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } }; // Adjacent positions

        for (int[] position : surroundingPositions) {
            int r = row + position[0];
            int c = col + position[1];

            if (r >= 0 && r < size && c >= 0 && c < size && board[r][c] == '.') {
                liberties++;
            }
        }

        return liberties;
    }

    static List<int[]> generateCapturingMoves(char[][] tempBoard, char currentPlayer, List<int[]> stoneGroup,
            int size) {
        List<int[]> capturingMoves = new ArrayList<>();

        for (int[] stone : stoneGroup) {
            int row = stone[0];
            int col = stone[1];

            int[][] surroundingPositions = { { row - 1, col }, { row + 1, col }, { row, col - 1 }, { row, col + 1 } };
            for (int[] position : surroundingPositions) {
                int r = position[0];
                int c = position[1];

                if (r >= 0 && r < size && c >= 0 && c < size && tempBoard[r][c] == '.') {
                    char[][] newTempBoard = new char[size][size];
                    for (int i = 0; i < size; i++) {
                        newTempBoard[i] = Arrays.copyOf(tempBoard[i], size);
                    }
                    newTempBoard[r][c] = currentPlayer;
                    int newCapturedStones = GoGame.simulateCaptureStones(r, c, newTempBoard);
                    int opponentLiberties = calculateOpponentLiberties(row, col, currentPlayer, size, board);
                    capturingMoves.add(new int[] { r, c, newCapturedStones, opponentLiberties });
                }
            }
        }

        // Sort capturing moves based on opponent's liberties (descending order)
        capturingMoves.sort((move1, move2) -> Integer.compare(move2[3], move1[3]));

        return capturingMoves;
    }

    private static int[] findBestCaptureMoveForGroup(char[][] board, List<int[]> stoneGroup, int opponentCapturedStones,
            char currentPlayer) {
        int[] bestMove = new int[] { -1, -1, 0 }; // {row, col, capturedStones}

        for (int[] stone : stoneGroup) {
            int row = stone[0];
            int col = stone[1];

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
        return bestMove;
    }

    // Method to get a group of stones for a specified location
    static List<int[]> getGroupOfStones(int row, int col, char player) {
        List<int[]> group = new ArrayList<>();
        boolean[][] visited = new boolean[size][size];
        dfs(row, col, player, visited, group);
        return group;
    }

    // used to help count the group of stones
    private static void dfs(int row, int col, char player, boolean[][] visited, List<int[]> group) {
        if (row < 0 || row >= size || col < 0 || col >= size || visited[row][col] || board[row][col] != player)
            return;

        visited[row][col] = true;
        group.add(new int[] { row, col });

        dfs(row + 1, col, player, visited, group);
        dfs(row - 1, col, player, visited, group);
        dfs(row, col + 1, player, visited, group);
        dfs(row, col - 1, player, visited, group);
    }

    //  countLiberties , the liberties will be counted  for use of the ai helper
    static int countLiberties(char player, char[][] board) {
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
    private static int countLibertiesHelper(int row, int col, char player, boolean[][] visited, char[][] board) {
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
    private static int simulateRemoveCapturedStones(int row, int col, char[][] tempBoard) {
        char player = tempBoard[row][col];
        boolean[][] visited = new boolean[size][size];
        return removeCapturedStonesHelperTempBoard(row, col, player, visited, tempBoard);
    }

    private static int removeCapturedStonesHelperTempBoard(int row, int col, char player, boolean[][] visited,
            char[][] tempBoard) {
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

    // simulate move shows the moves to make
    public static char[][] simulateMove(char[][] board, int row, int col, char player) {
        char[][] tempBoard = new char[board.length][board[0].length];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                tempBoard[i][j] = board[i][j];
            }
        }
        tempBoard[row][col] = player;
        return tempBoard;
    }

    // Evaluate the board state based on the number of opponent's stones
    static int evaluateBoard(char[][] board, char player) {
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

    // simulateCaptureStones for the ai helpers
    static int simulateCaptureStones(int row, int col, char[][] tempBoard) {
        char opponentPlayer = (currentPlayer == 'X') ? 'O' : 'X';

        int capturedStones = 0;
        List<int[]> adjacentOpponentStones = new ArrayList<>();

        // Place the current player's stone on the board.
        tempBoard[row][col] = currentPlayer;

        if (row > 0 && tempBoard[row - 1][col] == opponentPlayer) {
            adjacentOpponentStones.add(new int[] { row - 1, col });
        }
        if (row < size - 1 && tempBoard[row + 1][col] == opponentPlayer) {
            adjacentOpponentStones.add(new int[] { row + 1, col });
        }
        if (col > 0 && tempBoard[row][col - 1] == opponentPlayer) {
            adjacentOpponentStones.add(new int[] { row, col - 1 });
        }
        if (col < size - 1 && tempBoard[row][col + 1] == opponentPlayer) {
            adjacentOpponentStones.add(new int[] { row, col + 1 });
        }

        boolean[][] visited = new boolean[size][size];
        for (int[] stone : adjacentOpponentStones) {
            if (!visited[stone[0]][stone[1]] && isGroupCaptured(stone[0], stone[1], tempBoard)) {
                capturedStones += simulateRemoveCapturedStones(stone[0], stone[1], tempBoard);
            }
        }

        return capturedStones;
    }

    static boolean isValidMove(int row, int col) {
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

        // Check if the temporary board matches the previous board and if it results in
        // self-capture or violates the ko rule
        return !Arrays.deepEquals(tempBoard, previousBoard) && !isKoRuleViolation(row, col, tempBoard);
    }

    // these arent used for the ai these show the update of capture
    // stones-----------------
    private void makeMove(int row, int col) {
        board[row][col] = currentPlayer;

        // Update previous board
        for (int i = 0; i < size; i++) {

            previousBoard[i] = Arrays.copyOf(board[i], size);
        }
    }

    private static boolean hasLiberty(int row, int col, char player, boolean[][] visited) {
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

    // isGroupCaptured with correct parameters
    private static boolean isGroupCaptured(int row, int col, char[][] tempBoard) {
        char player = tempBoard[row][col];
        boolean[][] visited = new boolean[size][size];
        return !hasLibertyAIHelper(row, col, player, visited, tempBoard);
    }

    static boolean isStoneCaptured(int row, int col) {
        char player = board[row][col];
        boolean[][] visited = new boolean[size][size];
        return !hasLiberty(row, col, player, visited);
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

    // ------------------- these arent used for the ai these show the update of
    // capture stones-----------------

    private void printBoard() {
        // Print the pattern name if available
        if (patternName != null) {
            System.out.println(patternName);
        }

        System.out.print("  ");
        for (int k = 0; k < size; k++) {
            System.out.print(k + " ");
        }
        System.out.println();

        for (int i = 0; i < size; i++) {
            System.out.print(i + " ");

            for (int j = 0; j < size; j++) {
                if (board[i][j] == 'X') {
                    System.out.print("● "); // Smaller circle for white stone
                } else if (board[i][j] == 'O') {
                    System.out.print("◯ "); // Smaller filled circle for black stone
                } else {
                    System.out.print("· "); // Using dot for empty space
                }
            }

            System.out.println();
        }

        System.out.println("White (X) Stones Captured: " + stonesCapturedX);
        System.out.println("Black (O) Stones Captured: " + stonesCapturedO);
    }

}