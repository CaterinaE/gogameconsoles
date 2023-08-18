import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;

 
public class CaptureHelper {
     
 static void provideCaptureHelpAI(Scanner scanner, char currentPlayer, int size, char[][] board) {
 
    int maxCapturedStones = 0;
    int maxLiberties = Integer.MAX_VALUE; // Initialize maxLiberties to a high value
// Depth level to think two moves ahead
  //int depth = 4;
    
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

            // Update the best move based on the maximized captured stones and minimized liberties
            if (newCapturedStones > maxCapturedStones ||
                    (newCapturedStones == maxCapturedStones && opponentLiberties < maxLiberties)) {
                 
                maxCapturedStones = newCapturedStones;
                maxLiberties = opponentLiberties;
            }

        }
    } 
      
    
   
   
    
   /*  // Generate all possible capturing moves
    List<int[]> capturingMoves = generateCapturingMoves(tempBoard, opponentPlayer, row, col, size);

    int bestCaptureScore = Integer.MIN_VALUE;
    int[] bestCaptureMove = null;

    // Evaluate capturing moves
   for (int[] captureMove : capturingMoves) {
        int captureRow = captureMove[0];
        int captureCol = captureMove[1];
 
        // Simulate capturing the stone
        char[][] capturedTempBoard = GoGame.simulateMove(tempBoard, captureRow, captureCol, currentPlayer);
        int capturedStones = GoGame.simulateCaptureStones(captureRow, captureCol, capturedTempBoard);

        // Count opponent liberties after the capture
        int newOpponentLiberties = GoGame.countLiberties(opponentPlayer, capturedTempBoard);

        // Calculate the capture score based on the change in opponent liberties
        int captureScore = newOpponentLiberties - newOpponentLiberties;

        if (captureScore > bestCaptureScore) {
            bestCaptureScore = captureScore;
            bestCaptureMove = captureMove;
        }
    }

    if (bestCaptureMove != null) {
        int bestCaptureRow = bestCaptureMove[0];
        int bestCaptureCol = bestCaptureMove[1];
        int capturedStones = bestCaptureMove[2];

        System.out.println("AI suggests placing a stone at row " + bestCaptureRow + ", col " + bestCaptureCol +
                " to capture " + capturedStones + " stone(s) and limit opponent's liberties.");

     } else {
        System.out.println("AI couldn't find a capture move for the specified stone.");
    }
 */

// Find group liberties using the new approach
    Set<String> groupLiberties =   findGroupLiberties(row, col, opponentPlayer,   size,  board);

    // Initialize the best moves
    // {row, col, capturedStones}
    int[] bestFirstMove = {-1, -1, 0}; 
     // {row, col, capturedStones}
    int[] bestSecondMove = {-1, -1, 0};



// Iterate through all possible first moves and their possible second moves
    for (String liberty : groupLiberties) {
        String[] libertyCoordinates = liberty.split(",");
        int libertyRow = Integer.parseInt(libertyCoordinates[0]);
        int libertyCol = Integer.parseInt(libertyCoordinates[1]);

        if (GoGame.isValidMove(libertyRow, libertyCol)) {
            // Simulate making the move
            char[][] newTempBoard = GoGame.simulateMove(tempBoard, libertyRow, libertyCol, currentPlayer);
            int newCapturedStones = GoGame.simulateCaptureStones(libertyRow, libertyCol, newTempBoard);

            // Use the minimax method to find the best score after the initial capture move
            int bestScore = 1;
            int[] playerMove = findBestCaptureMove(newTempBoard, newCapturedStones, currentPlayer, size);
            // Update the best first move based on the maximized captured stones and minimax score
            if (newCapturedStones > bestFirstMove[2] || (newCapturedStones == bestFirstMove[2] && bestScore > bestFirstMove[2])) {
                bestFirstMove[0] = libertyRow;
                bestFirstMove[1] = libertyCol;
                bestFirstMove[2] = newCapturedStones;

                // Update the best second move based on the minimax score
                bestSecondMove[0] = playerMove[0];
                bestSecondMove[1] = playerMove[1];
                bestSecondMove[2] = playerMove[2];
            }  

        }
    }

  

     GoGame.blockPotentialEyes(tempBoard, currentPlayer, row, col);
int capturedStones = GoGame.simulateCaptureStones(row, col, tempBoard);

 int[] bestMove = minimax(tempBoard, 2, true, currentPlayer, size, row, col); // 2-depth search
if (bestMove[0] != -1 && bestMove[1] != -1) {
    System.out.println("AI suggests placing a stone at row " + bestMove[0] + ", col " + bestMove[1] +   "to capture " + capturedStones   );
} else {
    System.out.println("AI couldn't find a capture move for the specified stone.");
}
     // fix the eyes 
 if (bestFirstMove[0] != -1 && bestFirstMove[1] != -1) {
        System.out.println("AI suggests placing a stone at row " + bestFirstMove[0] + ", col " + bestFirstMove[1] +
                " to capture " + bestFirstMove[2] + " stone(s) and limit opponent's liberties.");

        if (bestSecondMove[0] != -1 && bestSecondMove[1] != -1) {
            System.out.println("Your best next move is at row " + bestSecondMove[0] + ", col " + bestSecondMove[1] +
                    " to capture " + bestSecondMove[2] + " stone(s) and limit opponent's liberties." );
        } else {
             System.out.println("AI couldn't find a capture move for the specified stone.");
        }
    } else {
        System.out.println("AI couldn't find a capture move for the specified stone.");
    }
}





public static List<int[]> generateCapturingMoves(char[][] tempBoard, char opponentPlayer,
                                                 int opponentRow, int opponentCol, int size) {
    List<int[]> capturingMoves = new ArrayList<>();

    int[][] surroundingPositions = {
            {opponentRow - 1, opponentCol}, {opponentRow + 1, opponentCol},
            {opponentRow, opponentCol - 1}, {opponentRow, opponentCol + 1}
    };

    for (int[] position : surroundingPositions) {
        int r = position[0];
        int c = position[1];

        if (r >= 0 && r < size && c >= 0 && c < size && tempBoard[r][c] == '.') {
            char[][] newTempBoard = GoGame.simulateMove(tempBoard, r, c, opponentPlayer);
            int capturedStones = GoGame.simulateCaptureStones(r, c, newTempBoard);

            capturingMoves.add(new int[]{r, c, capturedStones});
        }
    }

    return capturingMoves;
}



public static List<int[]> generateCapturingMoves(char[][] tempBoard, char currentPlayer, List<int[]> stoneGroup, int  size) {
    Set<String> uniqueMoves = new HashSet<>(); // Keep track of unique moves
    List<int[]> capturingMoves = new ArrayList<>();
    
    for (int[] stone : stoneGroup) {
        int row = stone[0];
        int col = stone[1];
        
        int[][] surroundingPositions = {{row - 1, col}, {row + 1, col}, {row, col - 1}, {row, col + 1}};
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

                // Create a unique string representation of the move
                String moveStr = r + "," + c + "," + newCapturedStones;
                
                // Only add the move if it's not already added
                if (!uniqueMoves.contains(moveStr)) {
                    capturingMoves.add(new int[]{r, c, newCapturedStones});
                    uniqueMoves.add(moveStr); // Add the move to the set
                }
            }
        }
    }
    
    return capturingMoves;
}
static int[] findBestCaptureMove(char[][] board, int opponentCapturedStones, char currentPlayer, int size) {
    int[] bestMove = new int[]{-1, -1, 0}; // {row, col, capturedStones}
   
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




static Set<String> findGroupLiberties(int row, int col, char player, int size, char [][] board) {
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


public  static List<int[]> detectPotentialEyesForGroup(List<int[]> group, char player, int size,char [][]board) {
    List<int[]> potentialEyes = new ArrayList<>();
    Set<String> checkedPositions = new HashSet<>(); // to keep track of already checked positions

    int maxAdjacentCount = 0; // To store the maximum adjacent count for potential eyes

    for (int[] stone : group) {
        int row = stone[0];
        int col = stone[1];

        int[][] adjacentPositions = {{row - 1, col}, {row + 1, col}, {row, col - 1}, {row, col + 1}};

        for (int[] position : adjacentPositions) {
            int r = position[0];
            int c = position[1];

            String posKey = r + "," + c;
            if (r >= 0 && r < size && c >= 0 && c < size && board[r][c] == '.' && !checkedPositions.contains(posKey)) {
                checkedPositions.add(posKey); // mark the position as checked

                int opponentCount = 0;
                int[][] surroundingPositions = {{r - 1, c}, {r + 1, c}, {r, c - 1}, {r, c + 1}};

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
                    potentialEyes.add(new int[]{r, c});
                }
            }
        }
    }
    return potentialEyes;
}

/*public static int[] findBestMove(char[][] board, char player, int size) {
    int bestScore = Integer.MIN_VALUE;
    int[] bestMove = {-1, -1};
    for (int row = 0; row < size; row++) {
        for (int col = 0; col < size; col++) {
            if (board[row][col] == '.') {
                char[][] tempBoard = GoGame.simulateMove(board, row, col, player);
               int score = minimax(tempBoard, player, 4, false, size );  // depth is set to 7
                if (score > bestScore) {
                    bestScore = score;
                    bestMove[0] = row;
                    bestMove[1] = col;
                }
            }
        }
    }
    return bestMove;
}*/

 
  

  //  static boolean printFlag = true; // Add this flag before the loop
public static void printBoard(char[][] board) {
    for (int row = 0; row < board.length; row++) {
        for (int col = 0; col < board[0].length; col++) {
            System.out.print(board[row][col] + " ");
        }
        System.out.println();
    }
}
public static int[] minimax(char[][] board, int depth, boolean isMaximizing, char currentPlayer, int size, int targetRow, int targetCol) {
    char opponentPlayer = (currentPlayer == 'X') ? 'O' : 'X';

    if (depth == 0) {
        return new int[]{-1, -1, 0}; // Default value for the leaf nodes.
    }

    int bestScore = isMaximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;
    int[] bestMove = {-1, -1, bestScore}; 

    int[][] surroundingPositions = {{targetRow - 1, targetCol}, {targetRow + 1, targetCol}, {targetRow, targetCol - 1}, {targetRow, targetCol + 1}};

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

            if (isMaximizing && currentScore > bestScore) {
                bestScore = currentScore;
                bestMove = new int[]{i, j, bestScore};
            } else if (!isMaximizing && currentScore < bestScore) {
                bestScore = currentScore;
                bestMove = new int[]{i, j, bestScore};
            }
        }
    }

    return bestMove;
}

 


}
 
 
 