import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

public class Board {
  
    //default board is blank 
public static void initializeWithPredefinedBoard(char[][] board, int size) { 
        size = 9;
      GoGame.board = new char[][]   {
              
                  
            
              {'.', '.', '.', '.', '.', '.', '.', '.', '.'},
            {'.', '.', '.', 'X', 'O', '.', '.', '.', '.'},
            {'.', '.', '.', 'O', 'X', '.', '.', '.', '.'},
            {'.', '.', '.', '.', '.', '.', '.', '.', '.'},
            {'.', '.', '.', '.', '.', '.', '.', '.', '.'},
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
        
        List<String[]> patterns = new ArrayList<>(); // A list to store all patterns.
        List<String> currentPattern = new ArrayList<>();
        List<String> patternNames = new ArrayList<>(); // A list to store pattern names.
        
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            
            // Assuming each pattern starts with 'P', let's split the patterns based on this:
            if (line.length() > 0 && line.charAt(0) == 'P') {
                patternNames.add(line); // Store the pattern name.
                if(!currentPattern.isEmpty()) {
                    patterns.add(currentPattern.toArray(new String[0])); 
                    currentPattern.clear();
                }
            } else if (!line.isEmpty()) {
                currentPattern.add(line);
            }
        }
        if(!currentPattern.isEmpty()) {
            patterns.add(currentPattern.toArray(new String[0]));
        }
        scanner.close();
        
        // Now, let's randomly choose one pattern:
        Random rand = new Random();
        int randomPatternIndex = rand.nextInt(patterns.size());
        String[] chosenPattern = patterns.get(randomPatternIndex);
        
        System.out.println(patternNames.get(randomPatternIndex)); // Print only the chosen pattern's name
        
        for (int i = 0; i < size && i < chosenPattern.length; i++) {
            String rowInput = chosenPattern[i];
            for (int j = 0; j < size && j < rowInput.length(); j++) {
                GoGame.board[i][j] = rowInput.charAt(j);
            }
        }
        
    } catch (FileNotFoundException e) {
        System.out.println("Failed to load the file. Initializing with a predefined board instead.");
        initializeWithPredefinedBoard(board, size);
    }
}}
 

/*          {'.', '.', '.', 'X', 'O', '.', '.', '.', '.'},
            {'X', 'X', 'X', 'X', 'O', '.', '.', '.', '.'},
            {'O', 'O', 'O', 'O', 'O', '.', '.', '.', '.'},
            {'.', '.', '.', '.', '.', '.', '.', '.', '.'},
            {'.', '.', '.', '.', '.', '.', '.', '.', '.'},
            {'.', '.', '.', '.', '.', '.', '.', '.', '.'},
            {'.', '.', '.', '.', '.', '.', '.', '.', '.'},
            {'.', '.', '.', '.', '.', '.', '.', '.', '.'},
            {'.', '.', '.', '.', '.', '.', '.', '.', '.'}
            
            
            
            
              {'.', '.', '.', '.', '.', '.', '.', '.', '.'},
            {'.', '.', '.', 'X', 'O', '.', '.', '.', '.'},
            {'.', '.', '.', 'O', 'X', '.', '.', '.', '.'},
            {'.', '.', '.', '.', '.', '.', '.', '.', '.'},
            {'.', '.', '.', '.', '.', '.', '.', '.', '.'},
            {'.', '.', '.', '.', '.', '.', '.', '.', '.'},
            {'.', '.', '.', '.', '.', '.', '.', '.', '.'},
            {'.', '.', '.', '.', '.', '.', '.', '.', '.'},
            {'.', '.', '.', '.', '.', '.', '.', '.', '.'}
            
            

               {'.', '.', '.', '.', '.', '.', '.', '.', '.'},
            {'.', '.', '.', '.', '.', '.', '.', '.', '.'},
            {'.', '.', '.', '.', '.', '.', '.', '.', '.'},
            {'.', 'X', 'O', '.', '.', '.', '.', '.', '.'},
            {'.', 'X', 'O', '.', '.', '.', '.', '.', '.'},
            {'.', '.', '.', '.', '.', '.', '.', '.', '.'},
            {'.', '.', '.', '.', '.', '.', '.', '.', '.'},
            {'.', '.', '.', '.', '.', '.', '.', '.', '.'},
            {'.', '.', '.', '.', '.', '.', '.', '.', '.'}
            
            
            
            */