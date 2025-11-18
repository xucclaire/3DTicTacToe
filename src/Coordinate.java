import java.util.Scanner;

public class Coordinate {

    public static final int N = 4;
    public static final int NSquared = N * N;
    public static final int NCubed = N * N * N;


    // Conversions between (x,y,z) coordinates and positions (indexes)

    public static int position(int x, int y, int z) {
        assert isValid(x, y, z);
        return z * NSquared + y * N + x;
    }

    public static int getX(int position) {
        assert isValid(position);
        return position % N;
    }

    public static int getY(int position) {
        assert isValid(position);
        return (position / N) % N;
    }

    public static int getZ(int position) {
        assert isValid(position);
        return position / NSquared;
    }

    public static boolean isValid(int position) {
        return (position >= 0) && (position < NCubed);
    }

    public static boolean isValid(int x, int y, int z) {
        return (x >= 0 && x < N) && (y >= 0 && y < N) && (z >= 0 && z < N);
    }

    public static int msb(long x) {
        // Find the position of the first bit set in the mask
        return 63 - Bit.countLeadingZeros(x);
    }

    // Formatting

    public static String toString(int x, int y, int z) {
        return String.format("(%d,%d,%d)", x+1, y+1, z+1);
    }

    public static String toString(int position) {
        return toString(getX(position), getY(position), getZ(position));
    }

    // Ask user to input coordinates from console

    private static class RetryException extends Exception {}  // Retry entering coordinates (restart from X)

    private static int ask(Scanner input, String prompt)
		throws Game.UndoException, Game.QuitException, RetryException
	{
		String response = "";
		int result = 0;
        do {
            System.out.print("Enter " + prompt + ": ");
            try {
                result = 0;
                response = input.nextLine().trim();
                switch (response) {
                    case "":      continue;
                    case "quit":  throw new Game.QuitException();
                    case "undo":  throw new Game.UndoException();
                    case "retry": throw new RetryException();
                    default:      result = Integer.parseInt(response);
                }
                if (result > 0 && result <= N) return result - 1; // Zero-based
                System.err.printf("Invalid value for %s: %s\n", prompt, response);
            } catch (NumberFormatException e) {
                System.err.printf("Invalid value for %s: %s\n", prompt, response);
            }
        } while (true);
    }

    public static int askPosition(Scanner input, Board board) throws Game.UndoException, Game.QuitException {
        do { 
            int x = 0;
            int y = 0;
            int z = 0;
            try {
                x = ask(input, "X");
                y = ask(input, "Y");
                z = ask(input, "Z");
            } catch (RetryException e) {
            }
            int position = position(x, y, z);
            if (!board.isEmpty(position)) {
                System.err.printf("Square %s is not empty", toString(position));
            } else {
                return position;
            }
        } while (true);
    }
}
