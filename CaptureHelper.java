import java.util.*;

public class CaptureHelper {

    /* =========================
       CAPTURE HELP ENTRY POINT
       ========================= */

    static void provideCaptureHelpAI(Scanner scanner, char currentPlayer, int size, char[][] board) {

        System.out.print("Enter row of the opponent's stone to capture: ");
        int targetRow = scanner.nextInt();
        System.out.print("Enter col of the opponent's stone to capture: ");
        int targetCol = scanner.nextInt();

        if (!inBounds(targetRow, targetCol, size) || board[targetRow][targetCol] == '.') {
            System.out.println("Invalid opponent stone.");
            return;
        }

        char opponent = (currentPlayer == 'X') ? 'O' : 'X';
        if (board[targetRow][targetCol] != opponent) {
            System.out.println("That stone does not belong to the opponent.");
            return;
        }

        /* ===== FIND INITIAL LIBERTIES ===== */

        Set<String> liberties =
                findGroupLiberties(targetRow, targetCol, opponent, size, board);

        /* ===== PRINT CAPTURING MOVES (SORTED BY LIBERTIES DESC) ===== */

        System.out.println("\nCapturing Moves:");

        List<int[]> movesWithLibs = new ArrayList<>();

        for (String lib : liberties) {
            String[] p = lib.split(",");
            int r = Integer.parseInt(p[0]);
            int c = Integer.parseInt(p[1]);

            int oppLibs =
                    GoGame.calculateOpponentLiberties(r, c, opponent, size, board);

            movesWithLibs.add(new int[]{r, c, oppLibs});
        }

        // Sort: higher opponent liberties first
        movesWithLibs.sort((a, b) -> Integer.compare(b[2], a[2]));

        for (int[] m : movesWithLibs) {
            System.out.println(
                "Row: " + m[0] +
                ", Col: " + m[1] +
                " Opponent's Liberties: " + m[2]
            );
        }

        /* ===== CAPTURE OPTIONS (CORRECT LOGIC) ===== */

        System.out.println("\nCapture options:");

        boolean immediateFound = false;
        int immediateRow = -1;
        int immediateCol = -1;

        for (int[] m : movesWithLibs) {
            if (m[2] == 0) {
                immediateFound = true;
                immediateRow = m[0];
                immediateCol = m[1];
                System.out.println(
                    "- Immediate capture: play at (" + m[0] + ", " + m[1] + ")"
                );
            }
        }

        for (int[] m : movesWithLibs) {
            if (m[2] > 0) {
                System.out.println(
                    "- Setup move: play at (" + m[0] + ", " + m[1] +
                    "), then capture on the next move"
                );
            }
        }

        /* ===== AI CAPTURE SEQUENCE ===== */

        System.out.println("\nAI capture sequence:");

        // Immediate capture → single move only
        if (immediateFound) {
            System.out.println(
                "1) " + currentPlayer +
                " plays (" + immediateRow + ", " + immediateCol + ")"
            );
            return;
        }

        // Otherwise, build setup sequence
        List<int[]> sequence = new ArrayList<>();
        char[][] workingBoard = copyBoard(board);

        while (true) {
            Set<String> libs =
                    findGroupLiberties(targetRow, targetCol, opponent, size, workingBoard);

            if (libs.isEmpty()) break;

            int bestRow = -1;
            int bestCol = -1;
            int minRemainingLibs = Integer.MAX_VALUE;

            for (String lib : libs) {
                String[] p = lib.split(",");
                int r = Integer.parseInt(p[0]);
                int c = Integer.parseInt(p[1]);

                if (!GoGame.isValidMove(r, c)) continue;

                char[][] temp = copyBoard(workingBoard);
                temp[r][c] = currentPlayer;
                GoGame.simulateCaptureStones(r, c, temp);

                int remaining =
                        findGroupLiberties(targetRow, targetCol, opponent, size, temp).size();

                if (remaining < minRemainingLibs) {
                    minRemainingLibs = remaining;
                    bestRow = r;
                    bestCol = c;
                }
            }

            if (bestRow == -1) break;

            workingBoard[bestRow][bestCol] = currentPlayer;
            GoGame.simulateCaptureStones(bestRow, bestCol, workingBoard);
            sequence.add(new int[]{bestRow, bestCol});

            if (findGroupLiberties(targetRow, targetCol, opponent, size, workingBoard).isEmpty()) {
                break;
            }
        }

        for (int i = 0; i < sequence.size(); i++) {
            int[] m = sequence.get(i);
            System.out.println(
                (i + 1) + ") " + currentPlayer +
                " plays (" + m[0] + ", " + m[1] + ")"
            );
        }
    }

    /* =========================
       GROUP LIBERTIES
       ========================= */

    static Set<String> findGroupLiberties(
            int row, int col, char player, int size, char[][] board) {

        Set<String> liberties = new HashSet<>();
        boolean[][] visited = new boolean[size][size];
        Stack<int[]> stack = new Stack<>();
        stack.push(new int[]{row, col});

        while (!stack.isEmpty()) {
            int[] cur = stack.pop();
            int r = cur[0];
            int c = cur[1];

            if (!inBounds(r, c, size) || visited[r][c] || board[r][c] != player) {
                continue;
            }

            visited[r][c] = true;

            int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
            for (int[] d : dirs) {
                int nr = r + d[0];
                int nc = c + d[1];

                if (!inBounds(nr, nc, size)) continue;

                if (board[nr][nc] == '.') {
                    liberties.add(nr + "," + nc);
                } else if (board[nr][nc] == player) {
                    stack.push(new int[]{nr, nc});
                }
            }
        }
        return liberties;
    }

    /* =========================
       UTILITIES
       ========================= */

    private static char[][] copyBoard(char[][] board) {
        char[][] copy = new char[board.length][board.length];
        for (int i = 0; i < board.length; i++) {
            copy[i] = Arrays.copyOf(board[i], board.length);
        }
        return copy;
    }

    private static boolean inBounds(int r, int c, int size) {
        return r >= 0 && r < size && c >= 0 && c < size;
    }
}
