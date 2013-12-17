package org.apache.niolex.address.optool;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import jline.console.ConsoleReader;

import org.apache.niolex.address.cmd.ICommand;
import org.apache.niolex.address.cmd.impl.CDCommand;
import org.apache.niolex.address.cmd.impl.CopyVersionCommand;
import org.apache.niolex.address.cmd.impl.ExitCommand;
import org.apache.niolex.address.cmd.impl.GetCommand;
import org.apache.niolex.address.cmd.impl.LSCommand;
import org.apache.niolex.address.cmd.impl.ListAuthCommand;
import org.apache.niolex.address.cmd.impl.PWDCommand;
import org.apache.niolex.address.cmd.impl.SetCommand;
import org.apache.niolex.address.cmd.impl.UsageCommand;
import org.apache.niolex.commons.codec.StringUtil;
import org.apache.niolex.commons.util.SystemUtil;
import org.apache.niolex.zookeeper.core.ZKException;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The shell Main class. OP Tool starts here.
 *
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 */
public class ShellMain {

    protected static final Logger LOG = LoggerFactory.getLogger(ShellMain.class);

    /**
     * Store all the environments.
     */
    protected static final Environment EVN = Environment.getInstance();

    /**
     * Store all the commands.
     */
    protected static final Map<String, ICommand> COMMAND_MAP = new HashMap<String, ICommand>();

    static {
        // -- Usage
        COMMAND_MAP.put("u", new UsageCommand());
        COMMAND_MAP.put("usage", new UsageCommand());
        // -- Common
        COMMAND_MAP.put("quit", new ExitCommand());
        COMMAND_MAP.put("exit", new ExitCommand());
        COMMAND_MAP.put("pwd", new PWDCommand());
        COMMAND_MAP.put("cd", new CDCommand());
        COMMAND_MAP.put("ls", new LSCommand());
        // -- Node
        COMMAND_MAP.put("get", new GetCommand());
        COMMAND_MAP.put("set", new SetCommand());
        COMMAND_MAP.put("acl", new ListAuthCommand());
        COMMAND_MAP.put("create", new ExitCommand());
        COMMAND_MAP.put("delete", new ExitCommand());
        COMMAND_MAP.put("deleteTree", new ExitCommand());
        // -- Tree
        COMMAND_MAP.put("initService", new CopyVersionCommand());
        COMMAND_MAP.put("addService", new CopyVersionCommand());
        COMMAND_MAP.put("addVersion", new CopyVersionCommand());
        COMMAND_MAP.put("addState", new CopyVersionCommand());
        COMMAND_MAP.put("copyVersion", new CopyVersionCommand());
        // -- Permission
        COMMAND_MAP.put("addOp", new ExitCommand());
        COMMAND_MAP.put("deleteOp", new ExitCommand());
        COMMAND_MAP.put("listOp", new ExitCommand());
        COMMAND_MAP.put("addClient", new ExitCommand());
        COMMAND_MAP.put("addServer", new ExitCommand());
        COMMAND_MAP.put("addAuth", new ExitCommand());
        COMMAND_MAP.put("deleteAuth", new ExitCommand());
        COMMAND_MAP.put("listAuth", new ListAuthCommand());
        // -- Meta
        COMMAND_MAP.put("getMeta", new ExitCommand());
        COMMAND_MAP.put("setMeta", new ExitCommand());
        // -- List
        COMMAND_MAP.put("listService", new ExitCommand());
    }

    /**
     * The entrance.
     *
     * @param args
     * @throws Exception
     */
    public static void main(String args[]) throws Exception {
        ShellMain main = new ShellMain(args);
        main.mainLoop();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    // END OF STATIC FIELDS AND METHODS
    //////////////////////////////////////////////////////////////////////////////////////////////


    protected OPToolService optool;

    protected ShellMain(String args[]) throws Exception {
        EVN.parseOptions(args);
        if (!EVN.validate()) {
            System.exit(0);
        }
        initOPToolService();
    }

    protected void initOPToolService() throws Exception {
        System.out.println("Connecting to " + EVN.host);
        optool = new OPToolService(EVN.host, EVN.timeout);
        optool.setRoot(EVN.root);
        // Check init tree.
        if (EVN.isInit) {
            System.out.println("                              Attention!!!");
            System.out.println("  System will now try to init a new root [" + EVN.root +"]. [" + EVN.userName + "] will be the");
            System.out.println("super user. Once proceed, this process can not be undone.");
            System.out.println("Are you sure to continue?(y/n):");
            int input = System.in.read();
            if (input == 'y') {
                System.out.println("\nSystem is initializing the root ...");
                optool.initTree(EVN.userName, EVN.password);
                System.out.println("Init done.");
            } else {
                System.out.println("Init abouted.");
            }
        }
        // Try login.
        login();

        if (EVN.userName.equals(optool.getSuperUser())) {
            EVN.isSuper = true;
        }
    }

    protected void login() throws Exception {
        boolean isLogin = false;
        switch (EVN.loginType) {
            case OP:
                isLogin = optool.loginOp(EVN.userName, EVN.password);
                break;
            case SVR:
                isLogin = optool.loginServer(EVN.userName, EVN.password);
                break;
            default:
                isLogin = optool.loginClient(EVN.userName, EVN.password);
                break;
        }
        if (isLogin) {
            System.out.println("Welcome ~ " + EVN.userName + "!");
        } else {
            System.out.println("Login failed, please check your username or password.");
            System.exit(0);
        }
    }

    protected void mainLoop() throws Exception {
        System.out.println("\n\t\"FIND\" Console\n");
        System.out.println("Type u(usage) for help messages.");

        final ConsoleReader reader = new ConsoleReader();
        boolean isJline = reader.clearScreen();
        String line;
        if (isJline) {
            reader.addCompleter(new OPToolCompletor(this));
            System.out.println("JLine enabled, please use <tab> key to help you speed up.");
            while ((line = reader.readLine(getPrompt())) != null) {
                executeLine(line);
            }
        } else {
            System.out.println("JLine disabled, fall back to native console.");
            System.out.print(getPrompt());
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            while ((line = br.readLine()) != null) {
                executeLine(line);
                System.out.print(getPrompt());
            }
        }
    }

    protected String getPrompt() {
        if (EVN.isSuper)
            return "[" + EVN.userName + ":" + EVN.curpath + "]# ";
        else
            return "[" + EVN.userName + ":" + EVN.curpath + "]$ ";
    }

    public void executeLine(String line) {
        try {
            if (!StringUtil.isBlank(line) && EVN.parseCommand(line)) {
                processCmd();
            }
        } catch (ZKException e) {
            switch (e.getCode()) {
                case NO_NODE:
                    error("NO NODE: " + ((KeeperException)e.getCause()).getPath());
                    break;
                case NO_AUTH:
                    error("NO AUTH!");
                    break;
                default:
                    error("Ex: " + e.toString());
                    break;
            }
        } catch (Exception e) {
            error("Ex: " + e.toString());
        }
    }

    protected void error(String str) {
        System.err.println(str);
        System.err.flush();
        SystemUtil.sleep(5);
    }

    protected void processCmd() throws Exception {
        ICommand command = COMMAND_MAP.get(EVN.command);
        if (command == null) {
            error("Command not found!");
        } else {
            command.processCmd(optool, EVN.cmdArgs);
        }
    }

}
