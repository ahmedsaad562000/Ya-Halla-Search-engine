package Logger_custom;

import java.util.MissingResourceException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * The type Logger custom.
 */
public class Logger_custom extends java.util.logging.Logger {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    /**
     * Protected method to construct a logger for a named subsystem.
     * <p>
     * The logger will be initially configured with a null Level
     * and with useParentHandlers set to true.
     *
     * @param name               A name for the logger.  This should                           be a dot-separated name and should normally                           be based on the package name or class name                           of the subsystem, such as java.net                           or javax.swing.  It may be null for anonymous Loggers.
     * @param resourceBundleName name of ResourceBundle to be used for localizing                           messages for this logger.  May be null if none                           of the messages require localization.
     * @throws MissingResourceException if the resourceBundleName is non-null and                                  no corresponding resource can be found.
     */
    public Logger_custom(String name, String resourceBundleName) throws MissingResourceException {
        super(name, resourceBundleName);
        ConsoleHandler handler = new ConsoleHandler();
        // Create a custom Formatter
        LoggingFormatter formatter = new LoggingFormatter();
        // Set the Formatter on the handler
        handler.setFormatter(formatter);
        // Add the handler to the logger
        this.addHandler(handler);
        handler.setLevel(Level.OFF);
        this.setLevel(Level.OFF);

    }

    /**
     * The type Logging formatter.
     */
    public static class LoggingFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            String color;
            Level level = record.getLevel();

            if (Level.SEVERE.equals(level)) {
                color = ANSI_RED;
            } else if (Level.WARNING.equals(level)) {
                color = ANSI_YELLOW;
            } else if (Level.INFO.equals(level)) {
                color = ANSI_GREEN;
            } else if (Level.CONFIG.equals(level)) {
                color = ANSI_BLUE;
            } else if (Level.FINE.equals(level)) {
                color = ANSI_CYAN;
            } else if (Level.FINER.equals(level)) {
                color = ANSI_PURPLE;
            } else {
                color = ANSI_WHITE;
            }
            return color + "[" + record.getLevel() + "]" + "[" + record.getSourceClassName() + "]" + ": " + record.getMessage() + "\n" + ANSI_RESET;
        }
    }

}
