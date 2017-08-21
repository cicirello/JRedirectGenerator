/*
 * JRedirectGenerator:
 *
 * Copyright (C) 2017 Vincent A. Cicirello.
 * http://www.cicirello.org/
 *
 * Command line utility for generating simple html files for redirecting
 * old urls to new.  The intended use of this utility is for those who
 * do not have sufficient access to otherwise generate 301 redirects.
 *
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.cicirello.redirect;

import java.util.*;
import java.io.*;

/**
 * JRedirectGenerator:
 *
 * Command line utility for generating simple html files for redirecting
 * old urls to new.  The intended use of this utility is for those who
 * do not have sufficient access to otherwise generate 301 redirects.
 *
 * If you use the provided default redirect page template, in the file redirect.template,
 * then each generated html file:
 *
 *    - uses a meta refresh directive to redirect to the new url, 
 *
 *    - has a robots noindex directive, which will hopefully have the effect of
 *      causing the old url to drop out of search results 
 *
 *    - includes a canonical link pointing to the new url
 *
 *    - body with a simple javascript redirect in case user's browser doesn't follow the meta refresh
 *
 *    - body also has a link to click in case javascript is also disabled
 * 
 * The template can be altered as desired, provided you place <<CANONICAL_TO>> wherever the target
 * address is needed. 
 * 
 * Configuration file, redirect.config specifies the local root of the local development copy of
 * the website on the first non-comment line.  Comments are indicated by #.  After the line containing the
 * complete path to the local root of the site, this file should contain one line for each desired 
 * redirect.  Each of these lines should include a pair separated by space or tabs.  First item of pair is
 * the location within the site (relative to root) to redirect from.  This can be a relative path to a file
 * or to a directory.  To indicate a directory, end with a trailing / otherwise a file will be assumed.
 * If a directory, the redirect file will be an index.html in that directory, otherwise it will be a file
 * with the exact name you provide including extension.  The second element of the pair is the full
 * canonical url of the new location, including either http or https as relevant.  See the provided redirect.config
 * for an example.
 *
 * @author Vincent A. Cicirello
 * @version 8.21.2017
 */
public class JRedirectGenerator {
	
	private static final ArrayList<String> fromLocs = new ArrayList<String>();
	private static final ArrayList<String> toLocs = new ArrayList<String>();
	
	public static void main(String[] args) {
		
		outputCopyrightNotice();
		if (args.length > 0 && args[0].equalsIgnoreCase("-help")) {
			outputUsage();
			System.exit(0);
		}		
		
		String config = "redirect.config";
		if (args.length > 0) config = args[0];
		processRedirects(config);
		
		String templateFile = "redirect.template";
		if (args.length > 1) templateFile = args[1];
		String template = readTemplate(templateFile);
		
		for (int i = 0; i < fromLocs.size(); i++) {
			String from = fromLocs.get(i);
			String page = generateRedirectPage(template, toLocs.get(i));
			writeRedirectFile(from, page);
		}
	}
	
	private static void writeRedirectFile(String filename, String contents) {
		
		String dirName = filename.substring(0,filename.lastIndexOf('/'));
		File dir = new File(dirName);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		
		PrintWriter red = null;
		try {
			red = new PrintWriter(filename);
		} catch (FileNotFoundException ex) {
			System.out.println("Output fle not found: " + ex);
			System.exit(0);
		}
		red.println(contents);
		red.flush();
		red.close();
	}
	
	private static String generateRedirectPage(String template, String canonical) {
		return template.replace("<<CANONICAL_TO>>", canonical);
	}
	
	
	private static String readTemplate(String templateFile) {
		File f = new File(templateFile);
		String template = null;
		if (f.exists()) {
			Scanner scan = null;
		
			try {
				scan = new Scanner(f);
			} catch (FileNotFoundException ex) {
				System.out.println("Redirect template file, " + templateFile + ", does not exist. Exiting.");
				System.exit(0);
			}
			template = "";
			while (scan.hasNextLine()) {
				template += scan.nextLine() + "\n";
			}
			scan.close();
		} else {
			System.out.println("Redirect template file, " + templateFile + ", does not exist. Exiting.");
			System.exit(0);
		}
		return template;
	}
	
	
	private static void processRedirects(String configFile) {
		File f = new File(configFile);
		if (f.exists()) {
			Scanner config = null;
		
			try {
				config = new Scanner(f);
			} catch (FileNotFoundException ex) {
				System.out.println("Redirect list file, " + configFile + ", does not exist. Exiting.");
				System.exit(0);
			}
			
			String localRoot; 
			do {
				localRoot = config.nextLine().trim();
				if (localRoot.contains("#")) {
					int pound = localRoot.indexOf('#');
					if (pound >= 0) localRoot = localRoot.substring(0,pound).trim();
				}
			} while (localRoot.length()==0);
			localRoot = localRoot.replace('\\','/');
			if (localRoot.charAt(localRoot.length()-1) != '/') localRoot += '/';
			
			while (config.hasNextLine()) {
				String line = config.nextLine().trim();
				int pound = line.indexOf('#');
				if (pound >= 0) line = line.substring(0,pound).trim();
				StringTokenizer tokens = new StringTokenizer(line);
				if (tokens.countTokens()==2) {
					String from = tokens.nextToken();
					from = from.replace('\\','/');
					if (from.charAt(from.length()-1) == '/') from += "index.html"; 
					fromLocs.add(localRoot + from);
					toLocs.add(tokens.nextToken());
				} else if (tokens.countTokens() > 2 || tokens.countTokens()==1) {
					System.out.println("Redirect list is not formatted properly. Error in: " + line);
				} 
			}
			
			config.close();
		} else {
			System.out.println("Redirect list file, " + configFile + ", does not exist. Exiting.");
			System.exit(0);
		}
	}
	
	private static void outputUsage() {
		System.out.println();
		System.out.println("java -jar JRedirectGenerator.jar -help");
		System.out.println("     Outputs the usage instructions (this output).");
		System.out.println("java -jar JRedirectGenerator.jar");
		System.out.println("     Generates redirect pages using configuration from redirect.config");
		System.out.println("     and the page template from redirect.template.");
		System.out.println("java -jar JRedirectGenerator.jar file.config");
		System.out.println("     Allows specifying the configuration file that contains the redirects.");
		System.out.println("java -jar JRedirectGenerator.jar file1.config file2.template");
		System.out.println("     Allows specifying both configuration and template files, in that order.");
	}
	
	private static void outputCopyrightNotice() {
		System.out.println("\nJRedirectGenerator, Copyright (C) 2017 Vincent A. Cicirello\n");
		System.out.println("The source code is available within the jar file or also from");
		System.out.println("http://www.cicirello.org/\n");
		System.out.println("This program comes with ABSOLUTELY NO WARRANTY; This is free software,");
		System.out.println("and you are welcome to redistribute it under certain conditions.");
		System.out.println("This program is distributed in the hope that it will be useful,");
		System.out.println("but WITHOUT ANY WARRANTY; without even the implied warranty of");
		System.out.println("MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the");
		System.out.println("GNU General Public License for more details.");
		System.out.println("https://www.gnu.org/licenses/gpl-3.0.html\n");
	}
}