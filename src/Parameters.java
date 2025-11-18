public class Parameters {

    public static class InvalidOptionException extends IllegalArgumentException {
        public InvalidOptionException(String s) { super(s); }
    }

    private int plies = 3;               // Number of plies to be searched
    private boolean first = false;       // Computer plays first
    private boolean alphaBeta = false;   // Use alpha-beta pruning
    private boolean ordering = false;    // Use move ordering
    private boolean statistics = false;  // Print statistics
    private boolean verbose = false;     // Verbose output
    private boolean trace = false;       // Produce tracing (debug) output
    private boolean errors = false;      // Errors detected during parsing
    private String board = "";           // Starting board configuration

    public Parameters(String[] args) {
        String option = null;

        for (String arg : args) {
            if (option != null) {
                try {
                    switch (option) {
                        case "-plies" -> plies = Integer.parseInt(arg);
                    }
                } catch (NumberFormatException e) {
                    String message = String.format("Invalid value for %s: %s", option, arg);
                    System.err.println(message);
                    errors = true;
                }
                option = null;
                continue;
            }

            switch(arg) {
                case "-first":
                    first = true;
                    break;

                case "-second":
                    first = false;
                    break;

                case "alpha":
                case "alphabeta":
                case "alpha_beta":
                    alphaBeta = true;
                    break;

                case "minimax":
                    alphaBeta = false;
                    break;

                case "order":
                case "ordering":
                case "move_ordering":
                    ordering = true;
                    break;

                case "-stats":
                case "-statistics":    
                    statistics = true;
                    break;

                case "-trace":
                    trace = true;
                    break;

                case "-verbose":
                    verbose = true;
                    break;

                case "-plies":
                    option = arg;
                    break;

                default:
                    if (arg.startsWith("-")) {
                        String message = String.format("Invalid option: %s", arg);
                        System.err.println(message);
                        errors = true;
                    } else if (option == null) {
                        if (board != null) {
                            board += arg;
                        } else {
                            board = arg;
                        }
                    }
            }
        }

        if (option != null) {
            String message = String.format("No value given for %s", option);
            System.err.println(message);
            errors = true;
        }
    }


    public int plies()         { return this.plies; }
    public boolean first()     { return this.first; }
    public boolean alphaBeta() { return this.alphaBeta; }
    public boolean ordering()  { return this.ordering; }
    public boolean verbose()   { return this.verbose; }
    public boolean statistics(){ return this.statistics; }
    public boolean trace()     { return this.trace; }
    public boolean errors()    { return this.errors; }
    public String board()      { return this.board; }

    public static void main(String[] args) {
        Parameters params = new Parameters(args);

        System.out.println("Plies = " + params.plies());
        System.out.println("First = " + params.first());
        System.out.println("Alpha = " + params.alphaBeta());
        System.out.println("Order = " + params.ordering());
        System.out.println("Stats = " + params.statistics());
        System.out.println("Trace = " + params.trace());
        System.out.println("Verbose = " + params.verbose());
        System.out.println("Errors = " + params.errors());

        Board board = Board.valueOf(params.board());
        board.print();
    }
}
