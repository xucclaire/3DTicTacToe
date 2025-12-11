import java.util.Stack;
import java.util.Scanner;

public class TicTacToe {

    private static int maxPlies = 2;
    private static boolean trace = false;
    private static Player aiPlayer = Player.X;
    private static Player humanPlayer = Player.O;

    public static void main(String[] args) throws Exception {

        Parameters params = new Parameters(args);

        if (params.first()) {
            aiPlayer = Player.X;
            humanPlayer = Player.O;
        } else {
            aiPlayer = Player.O;
            humanPlayer = Player.X;
        }

        trace = params.trace();
        maxPlies = params.plies();

        System.out.println("AI plays as: " + aiPlayer);
        System.out.println("Search depth: " + maxPlies);
        if (trace) System.out.println("Trace mode: " + "on");
        else System.out.println("Trace mode: " + "off");
        System.out.println();

        // Game State
        Stack<Board> history = new Stack<>();
        Board board = new Board();
        history.push(board);

        System.out.println("Initial board:");
        board.print();
        System.out.println();

        Scanner input = new Scanner(System.in);

        while (!Evaluate.isOver(board)) {
            // Human turn
            if (board.turn() == humanPlayer) {
                System.out.println("Your move:");

                try {
                    int pos = Coordinate.askPosition(input, board);

                    board = board.next(pos);
                    history.push(board);

                    board.print();
                    System.out.println();

                } catch (Game.UndoException e) {
                    if (history.size() > 1) {
                        history.pop();
                        history.pop(); // undo last move
                        board = history.peek();
                        System.out.println("Move undone.\n");
                        board.print();
                    } else {
                        System.out.println("No moves to undo.\n");
                    }
                    continue;

                } catch (Game.QuitException e) {
                    System.out.println("You quit the game.");
                    return;
                }

            } else {
                // AI turn
                System.out.println("AI is thinking...");

                int bestMove = minimaxRoot(board, maxPlies);
                int x = Coordinate.getX(bestMove);
                int y = Coordinate.getY(bestMove);
                int z = Coordinate.getZ(bestMove);

                System.out.println("AI move: " + Coordinate.toString(x, y, z));
                board = board.next(bestMove);
                history.push(board);

                board.print();
                System.out.println();
            }
        }

        // Game Over
        String result = Evaluate.winner(board);

        if (result != null && result.equals("Tie")) {
            System.out.println("Game over: Tie!");
        } else if (result != null) {
            System.out.println("Game over: " + result);

            // Find and display the winning line
            long xPositions = board.get(Player.X);
            long oPositions = board.get(Player.O);

            for (Line line : Line.lines) {
                long mask = line.positions();
                if ((xPositions & mask) == mask) {
                    System.out.println("Winning line: " + line.name());
                    break;
                }
                if ((oPositions & mask) == mask) {
                    System.out.println("Winning line: " + line.name());
                    break;
                }
            }
        }
    }

    // Mini Max
    private static int minimaxRoot(Board board, int depth) {

        boolean maximizing = (board.turn() == Player.X);
        int bestScore = maximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        int bestMove = -1;

        for (int move : board.moves()) {
            Board next = board.next(move);
            int score = minimax(next, depth - 1,
                    Integer.MIN_VALUE, Integer.MAX_VALUE);

            if (trace) {
                int x = Coordinate.getX(move);
                int y = Coordinate.getY(move);
                int z = Coordinate.getZ(move);
                System.out.println("Move " + Coordinate.toString(x, y, z)
                        + " → score = " + score);
            }

            if (maximizing) {
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
            } else {
                if (score < bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
            }
        }

        return bestMove;
    }

    // Minimax with Alpha-Beta
    private static int minimax(Board board, int depth, int alpha, int beta) {

        if (depth == 0 || Evaluate.isOver(board))
            return Evaluate.evaluate(board);

        boolean maximizing = (board.turn() == Player.X);

        if (maximizing) {
            int value = Integer.MIN_VALUE;
            for (int move : board.moves()) {
                value = Math.max(value,
                        minimax(board.next(move), depth - 1, alpha, beta));
                alpha = Math.max(alpha, value);
                if (beta <= alpha) break; // prune
            }
            return value;

        } else {
            int value = Integer.MAX_VALUE;
            for (int move : board.moves()) {
                value = Math.min(value,
                        minimax(board.next(move), depth - 1, alpha, beta));
                beta = Math.min(beta, value);
                if (beta <= alpha) break; // prune
            }
            return value;
        }
    }
}