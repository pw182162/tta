package org.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AppleScriptLib {

	public static String getXmlFileLocatoin()
	{
		String appleScript = "do shell script \"defaults read com.apple.iApps iTunesRecentDatabases | "
				+ "sed -En 's:^ *\\\"(.*)\\\"$:\\\\1:p' | "
				+ "/usr/bin/perl -MURI -e 'print URI->new(<>)->file;'\"\n";

		return executeAppleScript(appleScript);
	}

	public static String getSelectedTracks()
	{
		String appleScript = "tell application \"iTunes\"\n"
				+ 	"set myList to {}\n"
				+ 	"if selection is not {} then\n"
				+ 		"set mySelection to selection\n"
				+ 		"repeat with aTrack in mySelection\n"
				+ 			"set end of myList to (get database ID of aTrack)\n"
				+ 		"end repeat\n"
				+ 	"end if\n"
				+ 	"myList\n"
				+ "end tell\n";

		return executeAppleScript(appleScript);
	}

	public static String getLyric(Integer trackID)
	{
		String appleScript = "tell application \"iTunes\"\n"
				+ 	"set matchtrack to get first track in playlist 1 whose database ID is " + trackID + "\n"
				+ 	"get lyrics of the matchtrack\n"
				+ "end tell\n";

		return executeAppleScript(appleScript);
	}

	public static void playPauseTrack(Integer trackID)
	{
		String appleScript = "tell application \"iTunes\"\n"
				+ "if player state is playing then\n"
				+ 	"pause\n"
				+ "else\n"
				+ 	"set matchtrack to get first track in playlist 1 whose database id is " + trackID + "\n"
				+ 	"play matchtrack\n"
				+ "end if\n"
				+ "end tell\n";

		executeAppleScript(appleScript);
	}

	public static String executeAppleScript(String appleScript)
	{
		Runtime runtime = Runtime.getRuntime();
		String[] args = { "osascript", "-e", appleScript };
		String result = null;

		try
		{
			Process process = runtime.exec(args);
			InputStream inputStream = process.getInputStream();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream), 1);
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				if (result == null) {
					result = line;
				} else {
					result += "\n"+line;
				}
//				System.out.println(result);				
			}
			inputStream.close();
			bufferedReader.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return result;
	}
}
