public class Game {

    public static class UndoException extends Exception {
        public UndoException() {
            super("Undo requested");
        }
    }

    public static class QuitException extends Exception {
        public QuitException() {
            super("Quit requested");
        }
    }
}