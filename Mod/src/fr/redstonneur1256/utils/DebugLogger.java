package fr.redstonneur1256.utils;

import arc.util.Log;
import arc.util.Strings;
import fr.redstonneur1256.redutilities.io.streams.EmptyOutputStream;
import fr.redstonneur1256.redutilities.reflection.Reflect;
import mindustry.Vars;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;

import static arc.Core.settings;
import static arc.util.Log.*;

public class DebugLogger implements LogHandler {

    private static final String[] tags = { "[green][D][]", "[royal][I][]", "[yellow][W][]", "[scarlet][E][]", "" };
    private static final String[] systemTags = { "[D]", "[I]", "[W]", "[E]", "" };

    private PrintStream realOut;
    private PrintStream realErr;
    private Writer writer;

    public DebugLogger() {
    }

    public void init() {
        realOut = System.out;
        realErr = System.err;
        writer = settings.getDataDirectory().child("last_log.txt").writer(true);

        System.setOut(createOut(LogLevel.info));
        System.setErr(createOut(LogLevel.err));
    }

    private PrintStream createOut(LogLevel level) {
        // Don't judge my hacky solutions
        PrintStream stream = new PrintStream(new EmptyOutputStream());
        BufferedWriter writer = Reflect.get(stream, "textOut");
        Reflect.set(writer, "out", new LogMessageWriter(level));
        return stream;
    }

    @Override
    public void log(LogLevel level, String message) {
        PrintStream output = level == LogLevel.err || level == LogLevel.warn ? realErr : realOut;

        String source = getCaller();
        String name = source == null ? "Unknown" : source;
        int ordinal = level.ordinal();

        String consoleMessage = String.format("%s [%s] %s", systemTags[ordinal], name, message);
        output.println(consoleMessage);

        try {
            writer.write(consoleMessage + System.lineSeparator());
            writer.flush();
        }catch(IOException ignored) {
        }

        Vars.ui.scriptfrag.addMessage(Strings.format("@ [@] @", tags[ordinal], name, message));
    }

    private String getCaller() {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        String source = null;
        for(StackTraceElement element : trace) {
            source = element.getClassName();

            if(!source.equals("arc.util.Log") &&
                    !source.equals("fr.redstonneur1256.utils.DebugLogger") &&
                    !source.equals("fr.redstonneur1256.utils.DebugLogger$LogMessageWriter") &&
                    !source.contains("java.lang.Thread")) {
                break;
            }
        }
        if(source != null) {
            int index = source.lastIndexOf('.');
            return index == -1 ? source : source.substring(index + 1);
        }
        return null;
    }

    private static class LogMessageWriter extends Writer {

        private LogLevel level;
        private StringBuilder messageBuilder;

        public LogMessageWriter(LogLevel level) {
            this.level = level;
            this.messageBuilder = new StringBuilder();
        }

        @Override
        public void write(char[] buffer, int off, int len) {
            messageBuilder.setLength(0);
            messageBuilder.append(buffer, off, len);
            Log.log(level, messageBuilder.toString());
        }

        @Override
        public void flush() {

        }

        @Override
        public void close() {

        }

    }

}
