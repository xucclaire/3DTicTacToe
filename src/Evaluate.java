public class Evaluate {

	public static boolean isOver(Board board) {
		// Need to check for a win
		return board.numberEmptySquares() == 0;
	}

	public static String winner(Board board) {
		return "Tie"; // !!! TODO !!!
	}

	public static int score(Board board) {
		return 0; // tie !!! TODO !!!
	}

	public static int evaluate(Board board) {
		return 0; // !!! TODO !!!
	}

}

