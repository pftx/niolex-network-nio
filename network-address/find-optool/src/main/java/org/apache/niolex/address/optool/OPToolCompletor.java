package org.apache.niolex.address.optool;

import java.util.List;

import jline.console.completer.Completer;

/**
 * Complete the console command.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 */
class OPToolCompletor implements Completer {

    // The internal core shell main.
    private ShellMain main;

    /**
     * The only constructor.
     *
     * @param main
     */
    public OPToolCompletor(ShellMain main) {
        this.main = main;
    }

    /**
     * Override super method
     *
     * @see jline.console.completer.Completer#complete(java.lang.String, int, java.util.List)
     */
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {
        // Guarantee that the final token is the one we're expanding
        buffer = buffer.substring(0, cursor);
        String token = "";
        String[] tokens = buffer.split("\\s+");
        if (tokens.length > 1) {
            token = tokens[tokens.length - 1];
        } else {
            return completeCommand(buffer, buffer, candidates);
        }
        if (token.startsWith("/")) {
            return completeZNode(buffer, token, candidates);
        } else if (token.startsWith("./") || token.startsWith("../")) {
            return completeZNode(buffer, ShellMain.EVN.getAbsolutePath(token), candidates);
        }
        return cursor;
    }

    private int completeZNode(String buffer, String token, List<CharSequence> candidates) {
        String path = token;
        int idx = path.lastIndexOf("/") + 1;
        String prefix = path.substring(idx);
        // Only the root path can end in a /, so strip it off every other prefix
        String dir = idx == 1 ? "/" : path.substring(0, idx - 1);
        if (!main.optool.exists(dir)) {
            return buffer.length();
        }
        List<String> children = main.optool.getChildren(dir);
        for (String child : children) {
            if (child.startsWith(prefix)) {
                candidates.add(child);
            }
        }
        return candidates.size() == 0 ? buffer.length() : buffer.lastIndexOf('/') + 1;
    }

    private int completeCommand(String buffer, String token, List<CharSequence> candidates) {
        for (String cmd : ShellMain.COMMAND_MAP.keySet()) {
            if (cmd.startsWith(token)) {
                candidates.add(cmd);
            }
        }
        return buffer.lastIndexOf(' ') + 1;
    }
}
