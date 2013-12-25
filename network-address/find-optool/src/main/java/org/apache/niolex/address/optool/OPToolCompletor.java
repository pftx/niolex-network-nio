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
        String[] tokens = buffer.substring(0, cursor).split("\\s+");
        switch (tokens.length) {
            case 1:
                return completeCommand(buffer, candidates);
            case 2:
                return completeZNode(buffer, tokens[1], candidates);
            default:
                return cursor;
        }
    }

    private int completeCommand(String token, List<CharSequence> candidates) {
        token = token.toLowerCase();
        for (String cmd : ShellMain.COMMAND_MAP.keySet()) {
            if (cmd.toLowerCase().startsWith(token)) {
                candidates.add(cmd);
            }
        }
        return 0;
    }

    private int completeZNode(String buffer, String origPath, List<CharSequence> candidates) {
        String path = origPath;
        if (!path.startsWith("/")) {
            path = Environment.getInstance().getAbsolutePath(path);
        }
        int idx = path.lastIndexOf("/");
        String prefix = path.substring(0, idx);
        String last = path.substring(idx + 1);
        // For root path, it will be empty.
        if (prefix.length() == 0) {
            prefix = "/";
        }
        if (!main.optool.exists(prefix)) {
            return buffer.length();
        }
        List<String> children = main.optool.getChildren(prefix);
        for (String child : children) {
            if (child.startsWith(last)) {
                candidates.add(child);
            }
        }
        if (candidates.size() == 0) {
            return buffer.length();
        }
        int i = buffer.lastIndexOf(origPath);
        idx = origPath.lastIndexOf("/");
        return  i + idx + 1;
    }
}
