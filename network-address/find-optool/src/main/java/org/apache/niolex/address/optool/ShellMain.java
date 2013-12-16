package org.apache.niolex.address.optool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import jline.console.ConsoleReader;

import org.apache.niolex.address.cmd.ICommand;
import org.apache.niolex.address.cmd.impl.ExitCommand;
import org.apache.niolex.address.util.PathUtil;
import org.apache.niolex.commons.codec.StringUtil;
import org.apache.niolex.zookeeper.core.ZKException;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs.Perms;
import org.apache.zookeeper.data.ACL;
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

    /**
     * The usage description.
     */
    private static final String USAGE =
            " OPTool -server host:port -timeout <timeout> -auth username:password -login <login type> -root <root>\n\n"
                    +
                    " Node Path Structure is\n" +
                    " \t/<root>/services/<service>/versions/<version>/<stats>/<node>\n" +
                    " \t........................../clients/<version>/<clientName> ==> [data]\n" +
                    " \t......./operators/<operator>\n" +
                    " \t......./servers/<server>\n" +
                    " \t......./clients/<client>\n\n"
                    +
                    " COMMON COMMANDS\n\n"
                    +
                    "\t quit|exit\n"
                    +
                    "\t cd <fullpath|relativepath>\n"
                    +
                    "\t ls <fullpath|relativepath|empty>\n"
                    +
                    "\n"
                    +
                    " NODE OPERATIONS\n\n"
                    +
                    "\t create <fullpath|relativepath> <data|empty for no data>\n"
                    +
                    "\t delete <fullpath|relativepath>\n"
                    +
                    "\t delete -r <fullpath|relativepath>\n\t\t--Only For Delete Version\n"
                    +
                    "\t copyVersion <fromVersionNum> <toVersionNum> <copyClients>\n\t\t--true for copy clients, false or empty for not copy.\n"
                    +
                    "\t set <fullpath|relativepath> <data>\n"
                    +
                    "\t get <fullpath|relativepath>\n"
                    +
                    "\n"
                    +
                    " PERMISSION OPERATIONS\n\n"
                    +
                    "\t addOp <userName> <password>\n\t\t--Only For Super User\n"
                    +
                    "\t deleteOp <userName>\n\t\t--Only For Super User\n"
                    +
                    "\t listOp\n\t\t--Only For Super User\n"
                    +
                    "\t addClient <userName> <password>\n"
                    +
                    "\t addServer <userName> <password>\n"
                    +
                    "\t addAuth <fullpath|relativepath> <userName>\n\t\t--Only Work For Service Node\n"
                    +
                    "\t deleteAuth <fullpath|relativepath> <userName>\n\t\t--Only Work For Service Node\n"
                    +
                    "\t listAuth <fullpath|relativepath>\n"
                    +
                    "\n"
                    +
                    " META OPERATIONS\n\n"
                    +
                    "\t getMeta <userName> <key|empty for get all metas>\n\t\t--Must in a <version> directory\n"
                    +
                    "\t setMeta <userName> <Key> <Value>\n\t\t--Must in a <version> directory\n"
                    +
                    "\n"
                    +
                    " LIST OPERATIONS\n\n" +
                    "\t listService byname <servicePrefixName>\n" +
                    "\t listService byip <machineIP>\n";

    static {
        // -- Common
        COMMAND_MAP.put("quit", new ExitCommand());
        COMMAND_MAP.put("exit", new ExitCommand());
        COMMAND_MAP.put("cd", new ExitCommand());
        COMMAND_MAP.put("ls", new ExitCommand());
        // -- Node
        COMMAND_MAP.put("create", new ExitCommand());
        COMMAND_MAP.put("delete", new ExitCommand());
        COMMAND_MAP.put("copyVersion", new ExitCommand());
        COMMAND_MAP.put("set", new ExitCommand());
        COMMAND_MAP.put("get", new ExitCommand());
        // -- Permission
        COMMAND_MAP.put("addOp", new ExitCommand());
        COMMAND_MAP.put("deleteOp", new ExitCommand());
        COMMAND_MAP.put("listOp", new ExitCommand());
        COMMAND_MAP.put("addClient", new ExitCommand());
        COMMAND_MAP.put("addServer", new ExitCommand());
        COMMAND_MAP.put("addAuth", new ExitCommand());
        COMMAND_MAP.put("deleteAuth", new ExitCommand());
        COMMAND_MAP.put("listAuth", new ExitCommand());
        // -- Meta
        COMMAND_MAP.put("getMeta", new ExitCommand());
        COMMAND_MAP.put("setMeta", new ExitCommand());
        // -- List
        COMMAND_MAP.put("listService", new ExitCommand());
    }

    static protected void usage() {
        System.out.println(USAGE);
    }

    /**
     * The entrance.
     *
     * @param args
     * @throws IOException
     * @throws KeeperException
     * @throws InterruptedException
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
        System.out.println("Welcome to \"FIND\" Console!");

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
            System.out.println("JLine disabled, fall back to native console");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            while ((line = br.readLine()) != null) {
                executeLine(line);
                System.out.print(getPrompt());
            }
        }
    }

    protected String getPrompt() {
        if (EVN.isSuper)
            return "[ZK:" + EVN.curpath + "]#";
        else
            return "[ZK:" + EVN.curpath + "]$";
    }

    public void executeLine(String line) {
        try {
            if (!StringUtil.isBlank(line)) {
                EVN.parseCommand(line);
                processCmd();
            }
        } catch (ZKException e) {
            switch (e.getCode()) {
                case NO_NODE:
                    System.err.println("NO NODE: " + ((KeeperException)e.getCause()).getPath());
                    break;
                case NO_AUTH:
                    System.err.println("NO AUTH!");
                    break;
                default:
                    System.err.println("Ex: " + e.toString());
                    break;
            }
        } catch (Exception e) {
            System.err.println("Ex: " + e.toString());
        }
    }

    protected void processCmd() throws Exception {
        ICommand command = COMMAND_MAP.get(EVN.command);
        if (command == null) {
            usage();
        } else {
            command.processCmd(optool, EVN.cmdArgs);
        }
    }

    /*
        String returnStr = null;
        if (cmd.equals("quit") || cmd.equals("exit")) {
            //ggg
        } else if (cmd.equals("cd") && args.length == 2) {
            String absPath = genAbstractPath(args[1]);
            if (optool.exists(absPath)) {
                EVN.curpath = absPath;
            } else {
                System.out.println("NO NODE: " + absPath);
            }
        } else if (cmd.equals("ls")) {
            if (args.length == 1)
                System.out.println(optool.getChildren(EVN.curpath));
            else if (args.length == 2)
                System.out.println(optool.getChildren(genAbstractPath(args[1])));
            else
                System.err.println("WRONG ARGS");
        } else if (cmd.equals("create") && (args.length == 2 || args.length == 3)) {
            String data = args.length == 3 ? args[2] : null;
            String path = genAbstractPath(args[1]);
            if (!optool.isInServicePath(path)) {
                System.err.println("You are not allowed to create this path.");
                return;
            } else {
                returnStr = optool.create(path, data, optool.getAllPerm4Op());
                System.out.println("Created: " + returnStr);
            }
        } else if (cmd.equals("delete") && (args.length == 2 || args.length == 3)) {
            if (args.length == 3) {
                if (args[1].equals("-r")) {
                    System.out.print("Are you sure want to recursively delete this path(And it's children)?(y/n):");
                    int input = System.in.read();
                    if (input == 'y') {
                        String path = genAbstractPath(args[2]);
                        if (optool.isInsideVersionPath(path)) {
                            optool.delete(path, true);
                            System.out.println("Delete OK");
                        } else {
                            System.err.println("Delete recursively only allowed inside version node.");
                        }
                    } else {
                        System.out.println("Delete abouted.");
                    }
                }
                else {
                    System.err.println("Invalid Argument!");
                }
            } else {
                System.out.print("Are you sure want to delete this path?(y/n):");
                int input = System.in.read();
                if (input == 'y') {
                    optool.delete(genAbstractPath(args[1]), false);
                    System.out.println("Delete OK");
                } else {
                    System.out.println("Delete abouted.");
                }
            }
        } else if (cmd.equals("copyVersion") && (args.length == 3 || args.length == 4)) {
            boolean copyClients = false;
            if (args.length == 4) {
                copyClients = Boolean.parseBoolean(args[3]);
            }
            if (!optool.isAtServicePath(EVN.curpath)) {
                System.err.println("copyVersion Only work at a Service node!");
                return;
            }
            // 1. copy version node.
            if (optool.copyServiceVersion(EVN.curpath, args[1], args[2])) {
                if (copyClients) {
                    // 2. copy clients node.
                    if (optool.copyClientsVersion(EVN.curpath, args[1], args[2])) {
                        System.out.println("Copy ServiceVersion & ClientsVersion OK");
                    } else {
                        System.out.println("WRONG: New Client Version Already Exists!");
                    }
                } else {
                    System.out.println("Copy ServiceVersion OK");
                }
            } else {
                System.out.println("WRONG: New Service Version Already Exists!");
            }
        } else if (cmd.equals("set") && args.length == 3) {
            optool.setDataStr(genAbstractPath(args[1]), args[2]);
            System.out.println("OK");
        } else if (cmd.equals("get") && args.length == 2) {
            System.out.println("DATA: " + optool.getDataStr(genAbstractPath(args[1])));
            // -----------------------------------------------------
            // From now permissions.
            // -----------------------------------------------------
        } else if (cmd.equals("addOp") && args.length == 3) {
            if (EVN.isSuper) {
                optool.create(optool.getOpPath() + "/" + args[1], null, optool.getAllPerm4Super());
                ACL newOp = new ACL(Perms.READ, optool.generateDigest(args[1], args[2]));
                List<ACL> rlist = Collections.singletonList(newOp);
                System.out.println("Add Op OK, System will now add permissions for this new Op...");
                optool.addACLs(optool.getOpPath(), rlist);
                System.out.println("Permission added for OP Node.");
                ACL cdrOp = new ACL(Perms.READ | Perms.CREATE | Perms.DELETE, optool.generateDigest(args[1], args[2]));
                List<ACL> cdlist = Collections.singletonList(cdrOp);
                optool.addACLs(optool.getServersPath(), cdlist);
                System.out.println("Permission added for Servers Node.");
                optool.addACLs(optool.getClientsPath(), cdlist);
                System.out.println("Permission added for Clients Node.");
                // We now try to add permission to service path.
                ACL allOp = new ACL(Perms.ALL, optool.generateDigest(args[1], args[2]));
                List<ACL> alllist = Collections.singletonList(allOp);
                optool.addACLs(optool.getServicePath(), alllist);
                List<String> serviceList = optool.getChildren(optool.getServicePath());
                for (String service : serviceList) {
                    optool.addACLs4Service(service, alllist);
                    System.out.println("Permission added for Service [" + service + "].");
                }
                System.out.println("addOp Done.");
            } else {
                System.err.println("Only Super Can Execute this Command!");
            }
        } else if (cmd.equals("deleteOp") && args.length == 2) {
            if (EVN.isSuper) {
                System.out.println("deleteOp Will be supported latter....");
            } else {
                System.err.println("Only Super Can Execute this Command!");
            }
        } else if (cmd.equals("listOp") && args.length == 1) {
            if (EVN.isSuper) {
                System.out.println(optool.getChildren(optool.getOpPath()));
            } else {
                System.err.println("Only Super Can Execute this Command!");
            }
        } else if (cmd.equals("addClient") && args.length == 3) {
            if (!optool.exists(optool.getClientsPath() + "/" + args[1])) {
                ACL newOp = new ACL(Perms.READ, optool.generateDigest(args[1], args[2]));
                List<ACL> rlist = Collections.singletonList(newOp);
                optool.create(optool.getClientsPath() + "/" + args[1], null, rlist);
                System.out.println("addClient Done.");
            } else {
                System.err.println("This Client Already Exists!");
            }
        } else if (cmd.equals("addServer") && args.length == 3) {
            if (!optool.exists(optool.getServersPath() + "/" + args[1])) {
                ACL newOp = new ACL(Perms.READ, optool.generateDigest(args[1], args[2]));
                List<ACL> rlist = Collections.singletonList(newOp);
                optool.create(optool.getServersPath() + "/" + args[1], null, rlist);
                System.out.println("addServer Done.");
            } else {
                System.err.println("This Server Already Exists!");
            }
        } else if (cmd.equals("addAuth") && args.length == 3) {
            String path = this.genAbstractPath(args[1]);
            if (!optool.isAtServicePath(path)) {
                System.err.println("addAuth Only work at a Service node!");
                return;
            }
            String clientName = args[2];
            List<ACL> clientACL = null;
            boolean isClient = false;
            List<String> cli = optool.getChildren(optool.getClientsPath());
            for (String c : cli) {
                if (c.equals(clientName)) {
                    isClient = true;
                    clientACL = optool.getACLs(optool.getClientsPath() + "/" + clientName);
                    break;
                }
            }
            boolean isServer = false;
            if (!isClient) {
                List<String> svrli = optool.getChildren(optool.getServersPath());
                for (String svr : svrli) {
                    if (svr.equals(clientName)) {
                        isServer = true;
                        clientACL = optool.getACLs(optool.getServersPath() + "/" + clientName);
                        break;
                    }
                }
            }
            if (!isClient && !isServer) {
                System.err.println("addAuth Only work for Client or Server Account!");
                return;
            }
            // We will start to add ACL.
            if (optool.exists(path)) {
                // Add ACL for service.
                optool.addACLs(path, clientACL);
                List<String> list = optool.getChildren(path);
                // versions or clients
                for (String spVer : list) {
                    optool.addACLs(path + "/" + spVer, clientACL);
                    List<String> vers = optool.getChildren(path + "/" + spVer);
                    // versions
                    for (String v : vers) {
                        optool.addACLs(path + "/" + spVer + "/" + v, clientACL);
                        List<String> stats = optool.getChildren(path + "/" + spVer + "/" + v);
                        List<ACL> statAcl = null;
                        if (spVer.equals(PathUtil.VERSIONS) && !stats.isEmpty() && isServer) {
                            // For server, we want to add CDR to the states node.
                            statAcl = new ArrayList<ACL>();
                            statAcl.add(new ACL(Perms.CREATE | Perms.DELETE | Perms.READ, clientACL.get(0).getId()));
                        } else {
                            statAcl = clientACL;
                        }
                        // stats
                        for (String st : stats) {
                            optool.addACLs(path + "/" + spVer + "/" + v + "/" + st, statAcl);
                        }
                    }
                }
                System.out.println("addAuth Done.");
            } else {
                System.err.println("This Path does not exist!");
            }
        }  else if (cmd.equals("deleteAuth") && args.length == 3) {
            String path = this.genAbstractPath(args[1]);
            if (!optool.isAtServicePath(path)) {
                System.err.println("deleteAuth Only work at a Service node!");
                return;
            }
            String clientName = args[2];
            List<ACL> clientACL = null;
            boolean isClient = false;
            List<String> cli = optool.getChildren(optool.getClientsPath());
            for (String c : cli) {
                if (c.equals(clientName)) {
                    isClient = true;
                    clientACL = optool.getACLs(optool.getClientsPath() + "/" + clientName);
                    break;
                }
            }
            boolean isServer = false;
            if (!isClient) {
                List<String> svrli = optool.getChildren(optool.getServersPath());
                for (String svr : svrli) {
                    if (svr.equals(clientName)) {
                        isServer = true;
                        clientACL = optool.getACLs(optool.getServersPath() + "/" + clientName);
                        break;
                    }
                }
            }
            if (clientACL == null) {
                System.err.println("deleteAuth Only work for Client or Server Account!");
                return;
            }
            // We will start to delete ACL.
            if (optool.exists(path)) {
                System.out.println("deleteAuth Will be supported latter....");
                if (isServer) {
                    System.out.println("Server is Good.");
                }
            } else {
                System.err.println("This Path does not exist!");
            }
        }else if (cmd.equals("listAuth") && args.length == 2) {
            String path = this.genAbstractPath(args[1]);
            if (optool.exists(path)) {
                List<ACL> list = optool.getACLs(path);
                for (ACL a : list) {
                    System.out.println(formatACL(a));
                }
            } else {
                System.err.println("This Path does not exist!");
            }
        }else if (cmd.equals("getMeta") && (args.length == 2 || args.length == 3)) {
            String path = this.genAbstractPath(EVN.curpath);
            if (!optool.isInsideVersionPath(path)) {
                System.err.println("getMeta only Work inside a Version Node.");
                return;
            }
            path = this.makeClientVersionPath(path);
            byte[] data = optool.getData(path + "/" + args[1]);
            if (args.length == 2) {
                System.out.println(optool.byte2str(data));
            } else {
                String key = args[2];
                HashMap<String, String> lines = optool.parseMap(data);
                if (lines.containsKey(key)) {
                    System.out.println("\t[" + key + "] = " + lines.get(key));
                } else {
                    System.out.println("\t[" + key + "] NOT FOUND.");
                }
            }
        }else if (cmd.equals("setMeta") && args.length == 4) {
            String path = this.genAbstractPath(EVN.curpath);
            if (!optool.isInsideVersionPath(path)) {
                System.err.println("setMeta only Work inside a Version Node.");
                return;
            }
            path = this.makeClientVersionPath(path);
            List<String> cli = optool.getChildren(path);
            if (!cli.contains(args[1])) {
                // Client not exist, so we need to create it.
                List<ACL> ali = optool.getACLs(path);
                optool.create(path + "/" + args[1], args[2] + "=" + args[3], ali);
            } else {
                // Client already Exist.
                byte[] data = optool.getData(path + "/" + args[1]);
                HashMap<String, String> lines = optool.parseMap(data);
                lines.put(args[2], args[3]);
                data = optool.toByteArray(lines);
                optool.setData(path + "/" + args[1], data);
            }
            // Update the client trigger.
            optool.updateClientTrigger(path, args[1]);
            System.out.println("setMeta Done.");
        } else if (cmd.equals("listService") && args.length == 3) {
            if (args[1].equals("byname")) {
                List<String> ss = optool.listServiceByPrefix(optool.getServicePath(), args[2]);
                for (String s : ss)
                    System.out.println("\t" + s);
            } else if (args[1].equals("byip")) {
                List<SVSM> ss = optool.listServiceByIP(optool.getServicePath(), args[2]);
                for (SVSM s : ss)
                    System.out.println("\t" + s.service + "[" + s.version + "/" + s.stat + "/" + s.node + "]");
            } else {
                System.err.println("Does not support this command!");
            }
        } else {
            usage();
        }
    }

    /**
     * Format the relative path into abstract path.
     *
     * @param appendix
     * @return the abstract path
     */
    protected String genAbstractPath(String appendix) {
        if (appendix.charAt(0) == '/') {
            return appendix;
        } else if (appendix.charAt(0) != '.') {
            return EVN.curpath + "/" + appendix;
        } else if (appendix.startsWith("./")) {
            return EVN.curpath + appendix.substring(1);
        } else if (appendix.equals(".")) {
            return EVN.curpath;
        }
        // OK, then there must be some ../...
        String[] breads = appendix.split("/");
        int i = 0, j = 0;
        for (; i < breads.length; ++i) {
            if (breads[i].equals("..")) {
                ++j;
            } else {
                break;
            }
        }
        StringBuilder ret = new StringBuilder();
        String[] curl = EVN.curpath.split("/");
        if (curl.length > j) {
            j = curl.length - j;
            // The first item in curl is empty.
            for (int k = 1; k < j; ++k) {
                ret.append('/').append(curl[k]);
            }
        }
        for (; i < breads.length; i++) {
            ret.append('/').append(breads[i]);
        }
        return ret.length() == 0 ? "/" : ret.toString();
    }

    /**
     * Make the version path to a new version path walk through the clients.
     *
     * @param path
     * @return the path
     */
    protected String makeClientVersionPath(String path) {
        String[] curl = path.split("/");
        StringBuilder ret = new StringBuilder();
        ret.append(optool.getRoot()).append("/").append(PathUtil.SERVICES).append("/");
        ret.append(curl[3]).append("/").append(PathUtil.CLI_ROOT).append("/");
        ret.append(curl[5]);
        return ret.toString();
    }

    /**
     * Generate formatted string from ACL.
     *
     * @param acl
     * @return formatted string
     */
    public String formatACL(ACL acl) {
        String name = acl.getId().getId();
        int i = name.indexOf(':');
        if (i != -1)
            name = name.substring(0, i);
        String perm = "";
        int p = acl.getPerms();
        if ((p & Perms.ADMIN) != 0) {
            perm += 'A';
        }
        if ((p & Perms.CREATE) != 0) {
            perm += 'C';
        }
        if ((p & Perms.DELETE) != 0) {
            perm += 'D';
        }
        if ((p & Perms.READ) != 0) {
            perm += 'R';
        }
        if ((p & Perms.WRITE) != 0) {
            perm += 'W';
        }
        return String.format("%1$20s  =>  %2$s", name, perm);
    }
}
