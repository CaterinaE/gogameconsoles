import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

public class Board {
  
    //default board is blank 
public static void initializeWithPredefinedBoard(char[][] board, int size) { 
        size = 9;
      GoGame.board = new char[][]   {
            {'O', '.', '.', '.', '.', '.', '.', '.', '.'},
            {'.', '.', '.', '.', '.', '.', '.', '.', '.'},
            {'.', 'O', '.', '.', '.', '.', '.', 'O', '.'},
            {'.', 'X', '.', '.', '.', '.', '.', 'X', 'O'},
            {'.', 'O', '.', '.', '.', '.', '.', 'O', '.'},
            {'.', '.', '.', '.', '.', '.', '.', '.', '.'},
            {'.', '.', '.', '.', '.', '.', '.', '.', '.'},
            {'.', '.', '.', '.', '.', '.', '.', '.', '.'},
            {'.', '.', '.', '.', '.', '.', '.', '.', '.'}
        };
    }

public static void initializeManually(Scanner scanner, char[][] board, int size) { 
        System.out.println("Enter the initial board state (use '.' for empty spaces, 'X' for White, 'O' for Black):");
      for (int i = 0; i < GoGame.size; i++) {
    String rowInput = scanner.next();
    for (int j = 0; j < GoGame.size; j++) {
        GoGame.board[i][j] = rowInput.charAt(j);
    }
}

         
    }

public static void initializeFromFile(char[][] board, int size) { 
        try {
            File file = new File("gopatterns.txt");
            Scanner scanner = new Scanner(file);
            boolean patternNameFound = false;
            
            // Read the file line by line
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.length() > 0 && line.charAt(0) == 'P' && !patternNameFound) {
                    System.out.println(line); // Print the pattern name
                    patternNameFound = true;
                } else if (!line.isEmpty()) {
                    for (int i = 0; i < GoGame.size; i++) {
    String rowInput = line;
    for (int j = 0; j < GoGame.size; j++) {
        GoGame.board[i][j] = rowInput.charAt(j);
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
            initializeWithPredefinedBoard(board, size);
        }
    }
}

 /*     {'.', '.', '.', 'X', 'O', '.', '.', '.', '.'},
            {'X', 'X', 'X', 'X', 'O', '.', '.', '.', '.'},
            {'O', 'O', 'O', 'O', 'O', '.', '.', '.', '.'},
            {'.', '.', '.', '.', '.', '.', '.', '.', '.'},
            {'.', '.', '.', '.', '.', '.', '.', '.', '.'},
            {'.', '.', '.', '.', '.', '.', '.', '.', '.'},
            {'.', '.', '.', '.', '.', '.', '.', '.', '.'},
            {'.', '.', '.', '.', '.', '.', '.', '.', '.'},
            {'.', '.', '.', '.', '.', '.', '.', '.', '.'}
 */