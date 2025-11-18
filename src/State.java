public interface State<Move> {

    public int score();               // Score when game is over
    public int evaluate();            // Board evaluation function
    public boolean isOver();          // Is the game over?
    public void print();              // Display the state of the game
    public String winner();           // Display text for the winner
    public Iterable<Move> moves();    // Iterator for valid moves in this state
    public State<Move> next(Move action);   // Next state for the given move

}
