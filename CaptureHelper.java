import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;

public class CaptureHelper {

    static void provideCaptureHelpAI(Scanner scanner, char currentPlayer, int size, char[][] board) {

        int maxCapturedStones = 0;
        int maxLiberties = Integer.MAX_VALUE; // Initialize maxLiberties to a high value
  
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
        int[][] surroundingPositions = { { row - 1, col }, { row + 1, col }, { row, col - 1 }, { row, col + 1 } };
        for (int[] position : surroundingPositions) {
            int r = position[0];
            int c = position[1];
            if (r >= 0 && r < size && c >= 0 && c < size && tempBoard[r][c] == '.') {
                possibleMoves.add(new int[] { r, c });
            }
        }

        // Simulate and evaluate each move using minimax algorithm for the old approach
        for (int[] move : possibleMoves) {
            int r = move[0];
            int c = move[1];

            if (GoGame.isValidMove(r, c)) { // Check if the move is valid (including Ko rule)
                // Simulate making the move
                char[][] newTempBoard = new char[size][size];
                for (int i = 0; i < size; i++) {
                    newTempBoard[i] = Arrays.copyOf(tempBoard[i], size);
                }
                newTempBoard[r][c] = currentPlayer;
                int newCapturedStones = GoGame.simulateCaptureStones(r, c, newTempBoard);

                // Count liberties for the opponent after making the move
                int opponentLiberties = GoGame.countLiberties(opponentPlayer, newTempBoard);

                // Update the best move based on the maximized captured stones and minimized
                // liberties
                if (newCapturedStones > maxCapturedStones ||
                        (newCapturedStones == maxCapturedStones && opponentLiberties < maxLiberties)) {

                    maxCapturedStones = newCapturedStones;
                    maxLiberties = opponentLiberties;
                }

            }
        }








        
        // Find group liberties using the new approach
        Set<String> groupLiberties = findGroupLiberties(row, col, opponentPlayer, size, board);

        // Initialize the best moves
        // {row, col, capturedStones}
        int[] bestFirstMove = { -1, -1, 0 };
        // {row, col, capturedStones}
        int[] bestSecondMove = { -1, -1, 0 };

        // Iterate through all possible first moves and their possible second moves
        for (String liberty : groupLiberties) {
            String[] libertyCoordinates = liberty.split(",");
            int libertyRow = Integer.parseInt(libertyCoordinates[0]);
            int libertyCol = Integer.parseInt(libertyCoordinates[1]);

            if (GoGame.isValidMove(libertyRow, libertyCol)) {
                // Simulate making the first move
                char[][] newTempBoard = GoGame.simulateMove(tempBoard, libertyRow, libertyCol, currentPlayer);
                int newCapturedStones = GoGame.simulateCaptureStones(libertyRow, libertyCol, newTempBoard);

                // Use the minimax method to find the best second move
                int[] minimaxMove = minimax(newTempBoard, 4, true, currentPlayer, size, row, col); // 4 depth search

                // Find the best move to make after capturing new stones
                int[] bestCaptureMove = findBestCaptureMove(newTempBoard, newCapturedStones, currentPlayer, size);

                // Choose between the two methods (minimax or findBestCaptureMove) based on some
                // criterion
                // Here, I'm just using the number of captured stones as an example criterion
                int[] playerMove = (minimaxMove[2] > bestCaptureMove[2]) ? minimaxMove : bestCaptureMove;
                int bestScore = playerMove[2];

                // Update the best first move based on the maximized captured stones and best
                // score
                if (newCapturedStones > bestFirstMove[2]
                        || (newCapturedStones == bestFirstMove[2] && bestScore > bestFirstMove[2])) {
                    bestFirstMove[0] = libertyRow;
                    bestFirstMove[1] = libertyCol;
                    bestFirstMove[2] = newCapturedStones;

                    // Update the best second move based on the chosen method's result
                    bestSecondMove[0] = playerMove[0];
                    bestSecondMove[1] = playerMove[1];
                    bestSecondMove[2] = playerMove[2];
                }
            }
        }
   
       blockPotentialEyes(tempBoard, currentPlayer, row, col,size,board);
     //  int capturedStones = GoGame.simulateCaptureStones(row, col, tempBoard);

        // fix the eyes
        if (bestFirstMove[0] != -1 && bestFirstMove[1] != -1) {
            System.out.println("AI suggests placing a stone at row " + bestFirstMove[0] + ", col " + bestFirstMove[1] +
                    " to capture " + bestFirstMove[2] + " stone(s) and limit opponent's liberties.");

            if (bestSecondMove[0] != -1 && bestSecondMove[1] != -1) {
                System.out.println("Your best next move is at row " + bestSecondMove[0] + ", col " + bestSecondMove[1] +
                        " to capture " + bestSecondMove[2] + " stone(s) and limit opponent's liberties.");
            } 
            
            else {
                
                //System.out.println("AI couldn't find a capture move for the specified stone.");
            }
        }
        
        else {

            System.out.println("AI couldn't find a capture move for the specified stone.");
        }
    }



  //--- this will tell the player if the other player has an eye or will  
    static void blockPotentialEyes(char[][] tempBoard, char currentPlayer, int row, int col, int size, char[][] board) {
        char opponentPlayer = (currentPlayer == 'X') ? 'O' : 'X';

        // Detect the group of stones connected to the given stone
        List<int[]> stoneGroup =  GoGame.getGroupOfStones(row, col, opponentPlayer);

        // Generate capturing moves against the group
        List<int[]> capturingMoves =  generateCapturingMoves(tempBoard, currentPlayer, stoneGroup, size);
        Collections.sort(capturingMoves, (move1, move2) -> Integer.compare(move2[3], move1[3]));

        System.out.println("\nCapturing Moves:");
        for (int[] move : capturingMoves) {
            row = move[0];
            col = move[1];
            int opponentLiberties = GoGame.calculateOpponentLiberties(row, col, opponentPlayer, size, board);

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

  private static int[] findBestCaptureMoveForGroup(char[][] board, List<int[]> stoneGroup, int opponentCapturedStones,
            char currentPlayer) {
        int[] bestMove = new int[] { -1, -1, 0 }; // {row, col, capturedStones}

        for (int[] stone : stoneGroup) {
            int row = stone[0];
            int col = stone[1];

            if (board[row][col] == '.') {
                char[][] tempBoard =  GoGame.simulateMove(board, row, col, currentPlayer);
                int capturedStones =  GoGame.simulateCaptureStones(row, col, tempBoard);

                if (capturedStones > bestMove[2] && capturedStones > opponentCapturedStones) {
                    bestMove[0] = row;
                    bestMove[1] = col;
                    bestMove[2] = capturedStones;
                }
            }
        }
        return bestMove;
    }
    public static List<int[]> generateCapturingMoves(char[][] board, char currentPlayer, List<int[]> stoneGroup, int size) {
        List<int[]> capturingMoves = new ArrayList<>();

        for (int[] stone : stoneGroup) {
            int row = stone[0];
            int col = stone[1];

            int[][] surroundingPositions = {{row - 1, col}, {row + 1, col}, {row, col - 1}, {row, col + 1}};
            for (int[] position : surroundingPositions) {
                int r = position[0];
                int c = position[1];

                if (r >= 0 && r < size && c >= 0 && c < size && board[r][c] == '.') {
                    char[][] newTempBoard = new char[size][size];
                    for (int i = 0; i < size; i++) {
                        newTempBoard[i] = Arrays.copyOf(board[i], size);
                    }
                    newTempBoard[r][c] = currentPlayer;
                    // Note: simulateCaptureStones needs to be accessible here; consider passing it as a parameter or making it public static in GoGame
                    int newCapturedStones = GoGame.simulateCaptureStones(r, c, newTempBoard);
                    int opponentLiberties = GoGame.calculateOpponentLiberties(r, c, (currentPlayer == 'X') ? 'O' : 'X', size, board);
                    capturingMoves.add(new int[]{r, c, newCapturedStones, opponentLiberties});
                }
            }
        }

        // Sort capturing moves based on opponent's liberties (descending order)
        capturingMoves.sort((move1, move2) -> Integer.compare(move2[3], move1[3]));

        return capturingMoves;
    }
 

// the minimax isnt working with Potential eyes


    
    // public static List<int[]> generateCapturingMoves(char[][] tempBoard, char currentPlayer, List<int[]> stoneGroup,
    //         int size) {
    //     Set<String> uniqueMoves = new HashSet<>(); // Keep track of unique moves
    //     List<int[]> capturingMoves = new ArrayList<>();

    //     for (int[] stone : stoneGroup) {
    //         int row = stone[0];
    //         int col = stone[1];

    //         int[][] surroundingPositions = { { row - 1, col }, { row + 1, col }, { row, col - 1 }, { row, col + 1 } };
    //         for (int[] position : surroundingPositions) {
    //             int r = position[0];
    //             int c = position[1];

    //             if (r >= 0 && r < size && c >= 0 && c < size && tempBoard[r][c] == '.') {
    //                 char[][] newTempBoard = new char[size][size];
    //                 for (int i = 0; i < size; i++) {
    //                     newTempBoard[i] = Arrays.copyOf(tempBoard[i], size);
    //                 }
    //                 newTempBoard[r][c] = currentPlayer;
    //                 int newCapturedStones = GoGame.simulateCaptureStones(r, c, newTempBoard);

    //                 // Create a unique string representation of the move
    //                 String moveStr = r + "," + c + "," + newCapturedStones;

    //                 // Only add the move if it's not already added
    //                 if (!uniqueMoves.contains(moveStr)) {
    //                     capturingMoves.add(new int[] { r, c, newCapturedStones });
    //                     uniqueMoves.add(moveStr); // Add the move to the set
    //                 }
    //             }
    //         }
    //     }

    //     return capturingMoves;
    // }

    static int[] findBestCaptureMove(char[][] board, int opponentCapturedStones, char currentPlayer, int size) {
        int[] bestMove = new int[] { -1, -1, 0 }; // {row, col, capturedStones}

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (board[row][col] == '.') {
                    char[][] tempBoard = GoGame.simulateMove(board, row, col, currentPlayer);
                    int capturedStones = GoGame.simulateCaptureStones(row, col, tempBoard);

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

    static Set<String> findGroupLiberties(int row, int col, char player, int size, char[][] board) {
        Set<String> groupLiberties = new HashSet<>();
        boolean[][] visited = new boolean[size][size];
        Stack<int[]> stack = new Stack<>();
        stack.push(new int[] { row, col });
        while (!stack.isEmpty()) {
            int[] current = stack.pop();
            int r = current[0];
            int c = current[1];
            if (r < 0 || r >= size || c < 0 || c >= size || visited[r][c] || board[r][c] != player)
                continue;
            visited[r][c] = true;

            int[][] surroundingPositions = { { r - 1, c }, { r + 1, c }, { r, c - 1 }, { r, c + 1 } };
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
                    stack.push(new int[] { nr, nc });
                }
            }
        }
        return groupLiberties;
    }

    public static List<int[]> detectPotentialEyesForGroup(List<int[]> group, char player, int size, char[][] board) {
        List<int[]> potentialEyes = new ArrayList<>();
        Set<String> checkedPositions = new HashSet<>(); // to keep track of already checked positions

        int maxAdjacentCount = 0; // To store the maximum adjacent count for potential eyes

        for (int[] stone : group) {
            int row = stone[0];
            int col = stone[1];

            int[][] adjacentPositions = { { row - 1, col }, { row + 1, col }, { row, col - 1 }, { row, col + 1 } };

            for (int[] position : adjacentPositions) {
                int r = position[0];
                int c = position[1];

                String posKey = r + "," + c;
                if (r >= 0 && r < size && c >= 0 && c < size && board[r][c] == '.'
                        && !checkedPositions.contains(posKey)) {
                    checkedPositions.add(posKey); // mark the position as checked

                    int opponentCount = 0;
                    int[][] surroundingPositions = { { r - 1, c }, { r + 1, c }, { r, c - 1 }, { r, c + 1 } };

                    for (int[] surroundingPos : surroundingPositions) {
                        int sr = surroundingPos[0];
                        int sc = surroundingPos[1];

                        if (sr >= 0 && sr < size && sc >= 0 && sc < size) {
                            if (board[sr][sc] == player || board[sr][sc] == '.') {
                                opponentCount++;
                            }
                        } else {
                            opponentCount++; // counting the board's edge
                        }
                    }

                    if (opponentCount > maxAdjacentCount) {
                        maxAdjacentCount = opponentCount;
                        potentialEyes.clear(); // clear previous potential eyes
                        potentialEyes.add(new int[] { r, c });
                    }
                }
            }
        }
        return potentialEyes;
    }

    // static boolean printFlag = true; // Add this flag before the loop
    // public static void printBoard(char[][] board) {
    //     for (int row = 0; row < board.length; row++) {
    //         for (int col = 0; col < board[0].length; col++) {
    //             System.out.print(board[row][col] + " ");
    //         }
    //         System.out.println();
    //     }
    // }

    public static int[] minimax(char[][] board, int depth, boolean isMaximizing, char currentPlayer, int size,
            int targetRow, int targetCol) {
        char opponentPlayer = (currentPlayer == 'X') ? 'O' : 'X';

        if (depth == 0) {
            return new int[] { -1, -1, 0 }; // Default value for the leaf nodes.
        }

        int bestScore = isMaximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        int[] bestMove = { -1, -1, bestScore };

        int[][] surroundingPositions = { { targetRow - 1, targetCol }, { targetRow + 1, targetCol },
                { targetRow, targetCol - 1 }, { targetRow, targetCol + 1 } };

        for (int[] position : surroundingPositions) {
            int i = position[0];
            int j = position[1];

            if (i >= 0 && i < size && j >= 0 && j < size && board[i][j] == '.') {
                char[][] newTempBoard = new char[size][size];
                for (int x = 0; x < size; x++) {
                    newTempBoard[x] = Arrays.copyOf(board[x], size);
                }

                newTempBoard[i][j] = isMaximizing ? currentPlayer : opponentPlayer;

                int capturedStones = GoGame.simulateCaptureStones(i, j, newTempBoard);
                int currentScore = isMaximizing ? capturedStones : -capturedStones;
                ;
                if (isMaximizing && currentScore > bestScore) {
                    bestScore = currentScore;
                    bestMove = new int[] { i, j, bestScore };
                } else if (!isMaximizing && currentScore < bestScore) {
                    bestScore = currentScore;
                    bestMove = new int[] { i, j, bestScore };
                }
            }
        }

        return bestMove;
    }

}
