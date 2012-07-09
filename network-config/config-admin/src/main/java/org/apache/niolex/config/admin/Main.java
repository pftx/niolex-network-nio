/**
 * Main.java
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

import org.apache.niolex.commons.file.FileUtil;

/**
 * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
 * @version 1.0.0
 * @Date: 2012-7-9
 */
public class Main {

	private static final String USAGE = FileUtil.getCharacterFileContentFromClassPath("usage.txt", Main.class, "utf8");
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		if (args.length != 3) {
			System.out.println("Usage: server user password");
			return;
		}
		final Updater updater = new UpdaterClient(args[0]);
		updater.subscribeAuthInfo(args[1], args[2]);
		final Scanner scan = new Scanner(System.in);
		System.out.println("Welcome to Config management Console.");
		while (true) {
			System.out.print("#> ");
			String line = scan.nextLine();
			String[] cmds = line.split(" +", 5);
			if (cmds.length < 3) {
				if (line.equalsIgnoreCase("exit")) {
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
				} else {
					System.out.println("Invalid command.");
					continue;
				}
			} else if (cmd.equalsIgnoreCase("get")) {
				if (cmds[1].equalsIgnoreCase("item")) {
					// Get item
					if (cmds.length != 4) {
						System.out.println("Invalid update item format.");
						continue;
					}
					System.out.println(updater.getItem(cmds[2], cmds[3]));
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
