import java.util.Random;

public class Evaluate {
	private static final Random random = new Random();
	public static boolean isOver(Board board) {
		if (winner(board) != null) return true;
		return board.numberEmptySquares() == 0;
	}

	public static String winner(Board board) {
		long x = board.get(Player.X);
		long o = board.get(Player.O);

		for (Line line : Line.lines) {
			long mask = line.positions();

			if ((x & mask) == mask)
				return "X Wins";

			if ((o & mask) == mask)
				return "O Wins";
		}

		if (board.numberEmptySquares() == 0)
			return "Tie";

		return null; // game not over
	}
	public static int score(Board board) {
		String w = winner(board);
		if (w == null) return 0;
		if (w.equals("X Wins")) return +500000000;
		if (w.equals("O Wins")) return -500000000;
		return 0; // tie
	}

	public static int evaluate(Board board) {
		String w = winner(board);
		if (w != null) return score(board);

		long xmask = board.get(Player.X);
		long omask = board.get(Player.O);
		long allOccupied = xmask | omask;

		int xThrees = 0;      // 3 in a row
		int oThrees = 0;
		int xTwos = 0;        // 2 in a row unblocked
		int oTwos = 0;
		int xOnes = 0;        // 1 in a row unblocked
		int oOnes = 0;

		long xTwoMask = 0L;   // Track which lines have 2 in a row for fork detection
		long oTwoMask = 0L;
		long xThreeMask = 0L; // Track which lines have 3 in a row
		long oThreeMask = 0L;
		long blockedLines = 0L;
		for (int i = 0; i < Line.lines.length; i++) {

			Line line = Line.lines[i];
			long mask = line.positions();

			long xMasked = xmask & mask;
			long oMasked = omask & mask;

			int xCount = Long.bitCount(xMasked);
			int oCount = Long.bitCount(oMasked);

			if (xCount > 0 && oCount > 0) {
				blockedLines |= (1L << i);
				continue;
			}

			// X's pieces in this line
			if (oCount == 0) {
				if (xCount == 3) {
					xThrees++;
					xThreeMask |= (1L << i);
				} else if (xCount == 2) {
					xTwoMask |= (1L << i);
					xTwos++;
				} else if (xCount == 1) {
					xOnes++;
				}
			}
			// O's pieces in this line
			else if (xCount == 0) {
				if (oCount == 3) {
					oThrees++;
					oThreeMask |= (1L << i);
				} else if (oCount == 2) {
					oTwoMask |= (1L << i);
					oTwos++;
				} else if (oCount == 1) {
					oOnes++;
				}
			}
		}

		// Fork detection
		int xForks = countForks(xTwoMask, xmask, omask);
		int oForks = countForks(oTwoMask, omask, xmask);

		// Center control
		int xCenter = countCenterControl(xmask);
		int oCenter = countCenterControl(omask);

		int xForcedWins = checkForcedWins(xmask, omask, allOccupied);
		int oForcedWins = checkForcedWins(omask, xmask, allOccupied);

		int score = 0;

		// Forced wins (four corners, four centers in a plane)
		score += (xForcedWins - oForcedWins) * 50000;

		score += (xThrees - oThrees) * 500;

		score += (xForks - oForks) * 10000;

		// 2 in a rows
		score += (xTwos - oTwos) * 200;

		// 1 in a row
		score += (xOnes - oOnes) * 30;

		// Control valuable squares
		score += (xCenter - oCenter) * 50;
		if (Long.bitCount(allOccupied) < 10) {
			score += random.nextInt(5) - 2; // Random value between -2 and +2
		}
		return score;
	}

	private static int countForks(long twoMask, long playerBoard, long opponentBoard) {
		int forkCount = 0;
		long allOccupied = playerBoard | opponentBoard;

		// For each 2 in a row, check what happens if we extend it
		for (int lineIdx = 0; lineIdx < Line.lines.length; lineIdx++) {
			if ((twoMask & (1L << lineIdx)) == 0) continue;

			Line line = Line.lines[lineIdx];
			long linePos = line.positions();
			long emptyInLine = linePos & ~allOccupied;

			// For each empty square in this 2 in a row
			for (int emptyPos : Bit.ones(emptyInLine)) {
				// Temporarily place a piece
				long testBoard = playerBoard | (1L << emptyPos);

				// Count how many unblocked 3 in a rows this creates
				int threatsCreated = 0;
				for (Line checkLine : Line.lines) {
					long mask = checkLine.positions();

					// Only check lines containing this position
					if (!checkLine.contains(emptyPos)) continue;

					int playerCount = Long.bitCount(testBoard & mask);
					int opponentCount = Long.bitCount(opponentBoard & mask);

					// Unblocked 3 in a row
					if (playerCount == 3 && opponentCount == 0) {
						threatsCreated++;
					}
				}

				if (threatsCreated >= 2) {
					forkCount++;
				}
			}
		}

		return forkCount;
	}

	private static int countCenterControl(long board) {
		int control = 0;

		//Center cubes (8 positions)
		int[] centerCubes = {
				Coordinate.position(1, 1, 1), Coordinate.position(1, 1, 2),
				Coordinate.position(1, 2, 1), Coordinate.position(1, 2, 2),
				Coordinate.position(2, 1, 1), Coordinate.position(2, 1, 2),
				Coordinate.position(2, 2, 1), Coordinate.position(2, 2, 2)
		};

		for (int pos : centerCubes) {
			if (Bit.isSet(board, pos)) {
				control += 3;
			}
		}

		//Adjacent to center (8 positions)
		int[] adjacentCubes = {
				Coordinate.position(0, 1, 1), Coordinate.position(0, 1, 2),
				Coordinate.position(0, 2, 1), Coordinate.position(0, 2, 2),
				Coordinate.position(3, 1, 1), Coordinate.position(3, 1, 2),
				Coordinate.position(3, 2, 1), Coordinate.position(3, 2, 2),
				Coordinate.position(1, 0, 1), Coordinate.position(1, 0, 2),
				Coordinate.position(1, 3, 1), Coordinate.position(1, 3, 2),
				Coordinate.position(2, 0, 1), Coordinate.position(2, 0, 2),
				Coordinate.position(2, 3, 1), Coordinate.position(2, 3, 2)
		};

		for (int pos : adjacentCubes) {
			if (Bit.isSet(board, pos)) {
				control += 1;
			}
		}

		return control;
	}
	private static int checkForcedWins(long playerBoard, long opponentBoard, long allOccupied) {
		int forcedWins = 0;

		// Check each plane for corner/center
		for (Plane plane : Plane.planes) {
			long planeMask = plane.positions();
			long playerInPlane = playerBoard & planeMask;
			long opponentInPlane = opponentBoard & planeMask;

			// Skip if opponent has any pieces in this plane
			if (opponentInPlane != 0) continue;

			int playerCount = Long.bitCount(playerInPlane);

			// Four corners pattern
			if (playerCount == 4) {
				if (isFourCorners(playerInPlane, planeMask)) {
					forcedWins++;
				}
			}

			// Four centers pattern (2x2 center block)
			if (playerCount == 4) {
				if (isFourCenters(playerInPlane, planeMask)) {
					forcedWins++;
				}
			}
		}

		// Check for two intersecting 3-in-a-rows with shared empty square
		forcedWins += checkIntersectingThrees(playerBoard, opponentBoard, allOccupied);

		return forcedWins;
	}

	private static boolean isFourCorners(long playerInPlane, long planeMask) {
		int[] positions = new int[16];
		int count = 0;
		for (int pos : Bit.ones(planeMask)) {
			positions[count++] = pos;
		}

		if (count != 16) return false;

		// Check if the 4 corners are occupied
		long corners = 0L;
		corners |= (1L << positions[0]);
		corners |= (1L << positions[3]);
		corners |= (1L << positions[12]);
		corners |= (1L << positions[15]);

		return (playerInPlane & corners) == corners;
	}


	private static boolean isFourCenters(long playerInPlane, long planeMask) {
		int[] positions = new int[16];
		int count = 0;
		for (int pos : Bit.ones(planeMask)) {
			positions[count++] = pos;
		}

		if (count != 16) return false;

		// Center 2x2 block in a 4x4 grid is at indices 5, 6, 9, 10
		long centers = 0L;
		centers |= (1L << positions[5]);
		centers |= (1L << positions[6]);
		centers |= (1L << positions[9]);
		centers |= (1L << positions[10]);

		return (playerInPlane & centers) == centers;
	}

	private static int checkIntersectingThrees(long playerBoard, long opponentBoard, long allOccupied) {
		int intersectingThrees = 0;

		// Find all lines with 3 pieces
		for (int i = 0; i < Line.lines.length; i++) {
			Line line1 = Line.lines[i];
			long mask1 = line1.positions();

			int playerCount1 = Long.bitCount(playerBoard & mask1);
			int opponentCount1 = Long.bitCount(opponentBoard & mask1);

			// Skip if not exactly 3 or blocked
			if (playerCount1 != 3 || opponentCount1 != 0) continue;

			// Find the empty square in this line
			long empty1 = mask1 & ~allOccupied;
			if (Long.bitCount(empty1) != 1) continue;

			// Check if another 3-in-a-row intersects at this empty square
			for (int j = i + 1; j < Line.lines.length; j++) {
				Line line2 = Line.lines[j];
				long mask2 = line2.positions();

				int playerCount2 = Long.bitCount(playerBoard & mask2);
				int opponentCount2 = Long.bitCount(opponentBoard & mask2);

				if (playerCount2 != 3 || opponentCount2 != 0) continue;

				long empty2 = mask2 & ~allOccupied;

				// If both lines share the same empty square, it's a forced win
				if (empty1 == empty2) {
					intersectingThrees++;
				}
			}
		}
		return intersectingThrees;
	}
}