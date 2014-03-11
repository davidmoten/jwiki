package fbot.ft;

import static fbot.lib.commons.Commons.*;

import java.util.ArrayList;
import java.util.Scanner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

import fbot.lib.commons.CStrings;

import fbot.lib.mbot.MBot;
import fbot.lib.util.FCLI;

/**
 * Assist with cleanup on Commons.
 * 
 * @author Fastily
 * 
 */
public class CCleaner
{
	/**
	 * Main driver.
	 * 
	 * @param args Prog args
	 */
	public static void main(String[] args)
	{
		// Constants.debug = true;
		CommandLine l = parseArgs(args);
		if (l.hasOption('p'))
			nukeLinksOnPage(l.getOptionValue('p'), l.getOptionValue('r'), "File");
		else if (l.hasOption("dr"))
			drDel(l.getOptionValue("dr"));
		else if (l.hasOption('u'))
			nukeUploads(l.getOptionValue('u'), l.getOptionValue('r'));
		else if (l.hasOption('c'))
			categoryNuke(l.getOptionValue('c'), l.getOptionValue('r'), false);
		else if (l.hasOption('t'))
			talkPageClear();
		else if (l.hasOption('o'))
			clearOSD(l.getOptionValue('r'));
		else
		{
			categoryNuke(CStrings.cv, CStrings.copyvio, false, "File");
			emptyCatDel(fastily.getCategoryMembers(CStrings.osd, "Category"));
			emptyCatDel(fastily.getCategoryMembers("Non-media deletion requests", "Category"));
			nukeEmptyFiles(fastily.getCategoryMembers(CStrings.osd, "File"));
			
			if (l.hasOption('d'))
				unknownClear();
			
			if (l.hasOption('a'))
				DRArchive.main(new String[0]);
			else if (l.hasOption("ac"))
				DRArchive.main(new String[] { "-c" });
		}
	}
	
	/**
	 * Parses prog arguments.
	 * 
	 * @param args The arguments recieved by main
	 * @return A CommandLine object with parsed args.
	 */
	private static CommandLine parseArgs(String[] args)
	{
		Options ol = new Options();
		
		OptionGroup og = new OptionGroup();
		og.addOption(FCLI.makeArgOption("dr", "Delete all files linked in a DR", "DR"));
		og.addOption(FCLI.makeArgOption("p", "Set mode to delete all files linked on a page", "title"));
		og.addOption(FCLI.makeArgOption("u", "Set mode to delete all uploads by a user", "username"));
		og.addOption(FCLI.makeArgOption("c", "Set mode to delete all category members", "category"));
		og.addOption(new Option("o", false, "Delete all members of a Other Speedy Deletions"));
		og.addOption(new Option("t", false, "Clears orphaned talk pages from DBR"));
		og.addOption(new Option("a", false, "Archive DRs ready for archiving"));
		og.addOption(new Option("ac", false, "Close all Singleton DRs"));
		ol.addOptionGroup(og);
		
		ol.addOption(FCLI.makeArgOption("r", "Reason param, for use with options that require a reason", "reason"));
		ol.addOption("help", false, "Print this help message and exit");
		ol.addOption("d", false, "Deletes everything we can in Category:Unknown");
		
		return FCLI.gnuParse(ol, args, "CCleaner [-dr|-t|[-p <title>|-u <user>|-c <cat>] -r <reason>]] [-d] [-a|-ac]");
	}
	
	/**
	 * Deletes all pages on "Commons:Database reports/Orphaned talk pages".
	 * 
	 * @return A list of pages we failed to process
	 */
	private static String[] talkPageClear()
	{
		ArrayList<String> l = new ArrayList<String>();
		Scanner m = new Scanner(fastily.getPageText("Commons:Database reports/Orphaned talk pages"));
		
		String ln;
		while (m.hasNextLine())
			if ((ln = m.nextLine()).contains("{{plnr"))
				l.add(ln.substring(ln.indexOf("=") + 1, ln.indexOf("}}")));
		m.close();
		
		return nuke("Orphaned talk page", l.toArray(new String[0]));
	}
	
	/**
	 * Clears daily categories in Category:Unknown. List is grabbed from <a
	 * href="https://commons.wikimedia.org/wiki/User:FSV/UC">User:FSV/UC</a>
	 * 
	 * @return A list of pages we failed to process
	 */
	private static String[] unknownClear()
	{
		fsv.nullEdit("User:FSV/UC");
		
		ArrayList<String> catlist = new ArrayList<String>();
		ArrayList<MBot.DeleteItem> l = new ArrayList<MBot.DeleteItem>();
		
		for (String c : fastily.getValidLinksOnPage("User:FSV/UC"))
		{
			catlist.add(c);
			String r;
			if (c.contains("permission"))
				r = String.format("[[COM:OTRS|No permission]] since %s", c.substring(c.indexOf("as of") + 6));
			else if (c.contains("license"))
				r = String.format("No license since %s", c.substring(c.indexOf("as of") + 6));
			else
				r = String.format("No source since %s", c.substring(c.indexOf("as of") + 6));
			
			for (String s : fastily.getCategoryMembers(c, "File"))
				l.add(new MBot.DeleteItem(s, r));
		}
		doAction("Fastily", l.toArray(new MBot.DeleteItem[0]));
		return emptyCatDel(catlist.toArray(new String[0]));
	}
	
}