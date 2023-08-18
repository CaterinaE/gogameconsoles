import java.util.Scanner;

public class Main {
     public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in); // For user input
 
        // Initialize and start the game
        GoGame game = new GoGame(9); // You can adjust the board size
        game.play();
        
        // Close the scanner when done
        scanner.close();
    }

    
}
 
