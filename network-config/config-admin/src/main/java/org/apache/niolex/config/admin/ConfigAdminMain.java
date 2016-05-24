/**
 * ConfigAdminMain.java
 *
 * Copyright 2012 Niolex, Inc.
 *
 * Niolex licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.apache.niolex.config.admin;

import java.util.Scanner;

import jline.console.ConsoleReader;
import jline.console.completer.StringsCompleter;

import org.apache.niolex.commons.file.FileUtil;
import org.apache.niolex.network.Config;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @since 2012-7-9
 */
public class ConfigAdminMain {

	private static final String USAGE = FileUtil.getCharacterFileContentFromClassPath("usage.txt", ConfigAdminMain.class,
	        Config.SERVER_ENCODING);
	/**
	 * The main function.
	 * 
	 * @param args command line arguments
	 * @throws Exception if necessary
	 */
	public static void main(String[] args) throws Exception {
		if (args.length != 3) {
			System.out.println("Usage: cadmin <server> <user> <password>");
			return;
		}
		final Updater updater = new UpdaterClient(args[0]);
		String res = updater.subscribeAuthInfo(args[1], args[2]);
		if (!res.startsWith("SUCC")) {
		    System.out.println("Authentication failure, config admin will stop now.");
		    return;
		}
		final ConsoleReader reader = new ConsoleReader();
		boolean isJline = reader.clearScreen();
		if (isJline) {
			reader.addCompleter(new StringsCompleter("help", "exit", "last", "add group",
					"refresh group", "add item", "update item", "get item", "add user",
					"update user", "change password", "add auth", "remove auth"));
			System.out.println("Welcome to Config management Console.");
			System.out.println("JLine enabled, please use <tab> key to help you speed up.");
		} else {
			System.out.println("Welcome to Config management Console.");
			System.out.println("JLine disabled, fall back to native console");
		}
		System.out.println("\nType help for usage.");
		final Scanner scan = new Scanner(System.in);
		String[] lastCmds = new String[0];
		while (true) {
			String line = null;
			if (isJline) {
				line = reader.readLine("#> ");
			} else {
				System.out.print("#> ");
				line = scan.nextLine();
			}
			String[] cmds = line.split(" +", 5);
			if (line.equalsIgnoreCase("last")) {
				cmds = lastCmds;
			} else {
				lastCmds = cmds;
			}
			if (cmds.length < 3) {
				if (line.equalsIgnoreCase("exit")) {
					System.out.println("\nGood Bye.");
					System.exit(0);
				}
				printHelp();
				continue;
			}
			String cmd = cmds[0];
			if (cmd.equalsIgnoreCase("add")) {
				if (cmds[1].equalsIgnoreCase("group")) {
					// Add group
					System.out.println(updater.addGroup(cmds[2]));
				} else if (cmds[1].equalsIgnoreCase("item")) {
					// Add item
					if (cmds.length != 5) {
						System.out.println("Invalid add item format.");
						continue;
					}
					System.out.println(updater.addItem(cmds[2], cmds[3], cmds[4]));
				} else if (cmds[1].equalsIgnoreCase("user")) {
					// Add user.
					if (cmds.length != 5) {
						System.out.println("Invalid add user format.");
						continue;
					}
					System.out.println(updater.addUser(cmds[2], cmds[3], cmds[4]));
				} else if (cmds[1].equalsIgnoreCase("auth")) {
					// Add user.
					if (cmds.length != 4) {
						System.out.println("Invalid add auth format.");
						continue;
					}
					System.out.println(updater.addAuth(cmds[2], cmds[3]));
				} else {
					System.out.println("Invalid command.");
					continue;
				}
			} else if (cmd.equalsIgnoreCase("update")) {
				if (cmds[1].equalsIgnoreCase("item")) {
					// Update item
					if (cmds.length != 5) {
						System.out.println("Invalid update item format.");
						continue;
					}
					System.out.println(updater.updateItem(cmds[2], cmds[3], cmds[4]));
				} else if (cmds[1].equalsIgnoreCase("user")) {
					// Update user.
					if (cmds.length != 5) {
						System.out.println("Invalid update user format.");
						continue;
					}
					System.out.println(updater.updateUser(cmds[2], cmds[3], cmds[4]));
				} else {
					System.out.println("Invalid command.");
					continue;
				}
			} else if (cmd.equalsIgnoreCase("get")) {
				if (cmds[1].equalsIgnoreCase("item")) {
					// Get item
					if (cmds.length != 4) {
						System.out.println("Invalid get item format.");
						continue;
					}
					System.out.println(updater.getItem(cmds[2], cmds[3]));
				} else {
					System.out.println("Invalid command.");
					continue;
				}
			} else if (cmd.equalsIgnoreCase("refresh")) {
				if (cmds[1].equalsIgnoreCase("group")) {
					// Refresh group
					if (cmds.length != 3) {
						System.out.println("Invalid refresh group format.");
						continue;
					}
					System.out.println(updater.refreshGroup(cmds[2]));
				} else {
					System.out.println("Invalid command.");
					continue;
				}
			} else if (cmd.equalsIgnoreCase("change")) {
				if (cmds[1].equalsIgnoreCase("password")) {
					// Change password
					if (cmds.length != 4) {
						System.out.println("Invalid change password format.");
						continue;
					}
					System.out.println(updater.changePassword(cmds[2], cmds[3]));
				} else {
					System.out.println("Invalid command.");
					continue;
				}
			} else if (cmd.equalsIgnoreCase("remove")) {
				if (cmds[1].equalsIgnoreCase("auth")) {
					// Remove auth
					if (cmds.length != 4) {
						System.out.println("Invalid remove auth format.");
						continue;
					}
					System.out.println(updater.removeAuth(cmds[2], cmds[3]));
				} else {
					System.out.println("Invalid command.");
					continue;
				}
			}
		}
	}

	/**
	 *
	 */
	private static void printHelp() {
		System.out.println("Welcome to Config management Console.");
		System.out.println("Usage:");
		System.out.println(USAGE);
	}

}
