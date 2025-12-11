public class EvaluateMore {

    public static boolean isOver(Board board) {
        if (winner(board) != null) return true;
        // Need to check for a win
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
        if (w.equals("X Wins")) return +100000;
        if (w.equals("O Wins")) return -100000;
        return 0; // tie
    }

    public static int evaluate(Board board) {
        String w = winner(board);
        if (w != null) return score(board);

        long xmask = board.get(Player.X);
        long omask = board.get(Player.O);

        // Pre-calculate line analysis
        int xThrees = 0;      // 3 in a row
        int oThrees = 0;
        int xTwos = 0;        // 2 in a row unblocked
        int oTwos = 0;
        int xOnes = 0;        // 1 in a row unblocked
        int oOnes = 0;

        long xTwoMask = 0L;   // Track which lines have 2 in a row for fork detection
        long oTwoMask = 0L;

        // Analyze all lines
        for (int i = 0; i < Line.lines.length; i++) {
            Line line = Line.lines[i];
            long mask = line.positions();

            long xMasked = xmask & mask;
            long oMasked = omask & mask;

            int xCount = Long.bitCount(xMasked);
            int oCount = Long.bitCount(oMasked);

            // Blocked lines are worthless
            if (xCount > 0 && oCount > 0) continue;

            // X's pieces in this line
            if (oCount == 0) {
                if (xCount == 3) {
                    xThrees++;
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

        int xPlanes = countClusteredPieces(xmask);
        int oPlanes = countClusteredPieces(omask);

        int score = 0;

        score += (xThrees - oThrees) * 500;

        score += (xForks - oForks) * 7000;

        // 2 in a rows
        score += (xTwos - oTwos) * 200;

        // 1 in a row
        score += (xOnes - oOnes) * 30;

        // Control valuable squares
        score += (xCenter - oCenter) * 50;

        score += (xPlanes - oPlanes) * 20;

        return score;
    }


    private static int countForks(long twoMask, long playerBoard, long opponentBoard) {
        int forkCount = 0;
        long allOccupied = playerBoard | opponentBoard;

        // For each 2 in a row, check what happens if we extend it
        for (int lineIdx = 0; lineIdx < Line.lines.length; lineIdx++) {
            if ((twoMask & (1L << lineIdx)) == 0) continue;

            long linePos = Line.lines[lineIdx].positions();
            long emptyInLine = linePos & ~allOccupied;

            // For each empty square in this 2 in a row
            for (int emptyPos = 0; emptyPos < 64; emptyPos++) {
                if ((emptyInLine & (1L << emptyPos)) == 0) continue;

                // Temporarily place a piece
                long testBoard = playerBoard | (1L << emptyPos);

                // Count how many unblocked 3 in a rows this creates
                int threatsCreated = 0;
                for (Line line : Line.lines) {
                    long mask = line.positions();

                    if ((mask & (1L << emptyPos)) == 0) continue;

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


    private static int countClusteredPieces(long board) {
        int control = 0;

        for (Plane plane : Plane.planes) {
            long intersection = board & plane.positions();
            int count = Long.bitCount(intersection);

            if (count >= 2) {
                control += count * count;
            } else if (count > 0) {
                control += count;
            }
        }

        return control;
    }
}