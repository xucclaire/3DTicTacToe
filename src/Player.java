public enum Player {

    X() {
        @Override
        public Player other() {
            return Player.O;
        }
    },

    O() {
        @Override
        public Player other() {
            return Player.X;
        }
    };

    public static final Player EMPTY = null;


    public abstract Player other();

    public static Player valueOf(char c) {
        switch(c) {
            case 'x':
            case 'X':
                return X;

            case 'o':
            case 'O':
                return O;

            default:
                return EMPTY;
        }
    }

    public static String toString(Player player) {
        // Handles blank board positions (player is null)
        return player != EMPTY ? player.toString() : ".";
    }
}
