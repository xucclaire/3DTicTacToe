import java.util.Iterator;

public class Board implements State<Integer> {

    private final long x; // Boolean vector of positions containing X's
    private final long o; // Boolean vector of positions containing O's
    private final Player turn; // Whose turn is it to play?

    // Constructors.

    public Board() { // Starting board configuration
        this.x = 0;
        this.o = 0;
        this.turn = Player.X;
    }

    private Board(long x, long o, Player turn) {
        this.x = x;
        this.o = o;
        this.turn = turn;
    }

    public Board(Board board, int position) { // New board for given move
        switch (board.turn) {
            case X:
                this.x = Bit.set(board.x, position);
                this.o = board.o;
                break;

            case O:
                this.x = board.x;
                this.o = Bit.set(board.o, position);
                break;

            default:
                throw new IllegalArgumentException();
        }
        this.turn = board.turn.other();
    }


    public static Board valueOf(String s) {
        long xPositions = 0;
        long oPositions = 0;
        int position = 0;
        int xCount = 0;
        int oCount = 0;
        Player turn;

        for (int i= 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case 'x':
                case 'X':
                    xPositions = Bit.set(xPositions, position++);
                    xCount++;
                    break;

                case 'o':
                case 'O':
                    oPositions = Bit.set(oPositions, position++);
                    oCount++;
                    break;

                case '.':
                    // Empty square
                    position++;
                    break;

                case ' ':
                case '_':
                case '|':
                    // Separator
                    break;

                default:
                    throw new IllegalArgumentException("Invalid player: " + c);
            }
        }

        switch (xCount - oCount) {
            case 0:  turn = Player.X; break;
            case 1:  turn = Player.O; break;
            default: turn = Player.EMPTY; break;
        }

        return new Board(xPositions, oPositions, turn);
    }

    // Selectors

    public Player turn() {
        return this.turn;
    }

    public boolean isEmpty(int position) {
        assert Coordinate.isValid(position);
        return ! Bit.isSet(this.x | this.o, position);
    }

    public boolean isEmpty(int x, int y, int z) {
        return this.isEmpty(Coordinate.position(x, y, z));
    }

    public int numberEmptySquares() {
        return Bit.countOnes(~(this.x | this.o));
    }

    public long emptySquares() {
        return ~(this.x | this.o);
    }

    public long get(Player player) {
        if (player == Player.EMPTY) {
            return emptySquares();
        } else if (player == Player.X) {
            return this.x;
        } else {
            return this.o;
        }
    }

    public Player get(int position) {
        assert Coordinate.isValid(position);
        if (Bit.isSet(this.x, position)) return Player.X;
        if (Bit.isSet(this.o, position)) return Player.O;
        return Player.EMPTY;
    }

    public Player get(int x, int y, int z) {
        return get(Coordinate.position(x, y, z));
    }

    @Override
    public boolean isOver() {
		return Evaluate.isOver(this);
    }

    @Override
    public int score() {
		return Evaluate.score(this);
    }

	@Override
	public String winner() {
		return Evaluate.winner(this);
	}

    @Override
    public int evaluate() {
		return Evaluate.evaluate(this);
    }

    // Equality

    public boolean equals(Board other) {
        return this.o == other.o && this.x == other.x;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Board && this.equals((Board) other);
    }

    @Override
    public int hashCode() {
        return Long.hashCode(this.x) ^ Long.hashCode(this.o);
    }

    // Image & printing functions

    @Override
    public String toString() {
        int N = Coordinate.N;
        int N2 = Coordinate.NSquared;
        int N3 = Coordinate.NCubed;
        String result = "";
        String separator = "";

        for (int position = 0; position < N3; position++) {
            result += separator;
            result += Player.toString(this.get(position));
            if (position % N2 == 0) {
                separator = " | ";
            } else if (position % N == 0) {
                separator = " ";
            } else {
                separator = "";
            }
        }
        return result;
    }


    public void print() {
        int N = Coordinate.N;
        System.out.println();
        for (int y = N-1; y >= 0; y--) {
            for (int z = 0; z < N; z++) {
                for (int x = 0; x < N; x++) {
                    System.out.print(Player.toString(this.get(x, y, z)));
                }
                System.out.print("    ");
            }
            System.out.println();
        }
        System.out.println();
    }


    // Generate new board for a given move

    public Board next(int position) {
        assert this.isEmpty(position);
        return new Board(this, position);
    }

    @Override
    public Board next(Integer position) {
        return next(position.intValue());
    }

    public Board next(int x, int y, int z) {
        return next(Coordinate.position(x, y, z));
    }

    @Override
    public Iterable<Integer> moves() {
        return Bit.ones(emptySquares());
    }
}
