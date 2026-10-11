package io.github.chazpounder78.keylightkeeper;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A single long-lived {@code su} shell. Commands are piped in over stdin, so we
 * pay the cost of spawning root once instead of on every write. When this
 * process (or its pipes) goes away the shell sees EOF and exits, so uninstalling
 * the app leaves nothing running behind.
 */
final class RootShell {

    private Process process;
    private OutputStream stdin;

    synchronized boolean start() {
        if (process != null && process.isAlive()) return true;
        try {
            process = new ProcessBuilder("su").redirectErrorStream(true).start();
            stdin = process.getOutputStream();
            return true;
        } catch (IOException e) {
            process = null;
            stdin = null;
            return false;
        }
    }

    synchronized boolean isAlive() {
        return process != null && process.isAlive();
    }

    synchronized void exec(String command) {
        if (!start()) return;
        try {
            stdin.write((command + "\n").getBytes());
            stdin.flush();
        } catch (IOException e) {
            // Pipe broke; drop it so the next call restarts root.
            stop();
        }
    }

    synchronized void stop() {
        try {
            if (stdin != null) {
                stdin.write("exit\n".getBytes());
                stdin.flush();
                stdin.close();
            }
        } catch (IOException ignored) {
        }
        if (process != null) process.destroy();
        process = null;
        stdin = null;
    }
}
