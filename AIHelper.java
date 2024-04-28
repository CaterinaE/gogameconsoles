import java.util.Arrays;

public class AIHelper {

   //--------------------the AI helper for move suggestions--------------------
    static void provideAIHelp(char[][] board, char currentPlayer, int size ) {
        
    System.out.println("AI is analyzing the board...");

    int bestRow = -1;
    int bestCol = -1;
    int maxScore = Integer.MIN_VALUE;

     // Simulate placing a stone on each empty position and calculate the score
    for (int row = 0; row < size; row++) {
        for (int col = 0; col < size; col++) {
             if (board[row][col] == '.' && GoGame.isValidMove(row, col)) {
                char[][] tempBoard = new char[size][size];
                for (int i = 0; i < size; i++) {
                    tempBoard[i] = Arrays.copyOf(board[i], size);
                }
                tempBoard[row][col] = currentPlayer;
                int capturedStones =  GoGame.simulateCaptureStones(row, col, tempBoard);
                int eyeCount =  GoGame.countEyes(tempBoard);

                // Calculate a score based on the number of captured stones, eye count, and position on the board
                int positionWeight;
                if ((row == 0 || row == size - 1) && (col == 0 || col == size - 1)) {
                    // Corner positions get the highest weight
                    positionWeight = 3;
                } else if (row <= 1 || row >= size - 2 || col <= 1 || col >= size - 2) {
                    // Side positions get the second highest weight
                    positionWeight = 2;
                } else {
                    // Center positions get the lowest weight
                    positionWeight = 1;
                }
                int score = capturedStones * 10 + eyeCount * 5 + positionWeight;

                if (score > maxScore) {
                    bestRow = row;
                    bestCol = col;
                    maxScore = score;
                }
            }
        }
    }

    if (bestRow != -1 && bestCol != -1) {
        System.out.println("AI suggests placing a stone at row " + bestRow + ", col " + bestCol);
    } else {
        System.out.println("AI coudn't suggust a turn.");
    }



    
}




 // ----------end of ai helper----------------------------------------------------------
 
}