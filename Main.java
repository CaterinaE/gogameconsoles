import java.util.Scanner;

public class Main {
     public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in); // For user input
 
        // Initialize and start the game
        GoGame12 game = new GoGame12(9); // You can adjust the board size
        game.play();
        
        // Close the scanner when done
        scanner.close();
    }

    
}
 
