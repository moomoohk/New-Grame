package com.moomoohk.Grame.Essentials;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import com.moomoohk.Grame.AI.PlayerMovementAI;
import com.moomoohk.Grame.AI.PlayerSimAI;
import com.moomoohk.Grame.AI.SimpleChaseAI;
import com.moomoohk.Grame.AI.SimpleStrollAI;
import com.moomoohk.Grame.Interfaces.GrameObject;
import com.moomoohk.Grame.commands.AddEntityAICommand;
import com.moomoohk.Grame.commands.AddGrameObjectCommand;
import com.moomoohk.Grame.commands.ClearEntityAI;
import com.moomoohk.Grame.commands.CreateEntityCommand;
import com.moomoohk.Grame.commands.DrawCoordinatesCommand;
import com.moomoohk.Grame.commands.HelpCommand;
import com.moomoohk.Grame.commands.MakePlayerCommand;
import com.moomoohk.Grame.commands.MoveGrameObjectCommand;
import com.moomoohk.Grame.commands.PrintEntityAICommand;
import com.moomoohk.Grame.commands.QuitCommand;
import com.moomoohk.Grame.commands.RenderBaseCommand;
import com.moomoohk.Grame.commands.SetEntityOverrideAICommand;
import com.moomoohk.Grame.commands.SetObjectSpeedCommand;
import com.moomoohk.Grame.commands.SetSpriteCommand;
import com.moomoohk.Grame.commands.isOccupiedCommand;
import com.moomoohk.Grame.commands.setVisibleCommand;
import com.moomoohk.MooCommands.Command;
import com.moomoohk.MooConsole.Console;

/**
 * The Grame Utils class provides useful utilities.
 * 
 * @author Meshulam Silk <moomoohk@ymail.com>
 * @version 1.0
 * @since 2013-04-05
 */
public class GrameUtils
{
	/**
	 * Folder where {@link GrameObject}s can be saved.
	 */
	public static File saveFolder = new File("GrameObjects");
	/**
	 * Folder where sound files can be saved.
	 */
	public static File soundsFolder = new File("Sounds");
	/**
	 * {@link Console} for debug prints and command execution.
	 */
	public static Console console;
	private static boolean isDialog = false;
	private static ArrayList<String> wordQueue = new ArrayList<String>();

	static
	{
		try
		{
			console = new Console();
		}
		catch (Error e)
		{
			System.out.println("You don't appear to have MooConsole installed!");
			System.out.println("Get it at: https://github.com/moomoohk/MooConsole/raw/master/Build/MooConsole.jar");
		}
	}

	/**
	 * Writes a {@link String} out to a {@link File}.
	 * 
	 * @param f
	 *            {@link File} to write to.
	 * @param s
	 *            {@link String} to write.
	 * @return True if the process was completed successfully, else false.
	 */
	public static boolean write(File f, String s)
	{
		try
		{
			if (!f.exists())
				f.createNewFile();
			FileWriter stream = new FileWriter(f);
			BufferedWriter out = new BufferedWriter(stream);
			out.write(s);
			out.flush();
			out.close();
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Reads a {@link File} and returns a {@link String} array where each place is a segment of text separated by a delimiter.
	 * 
	 * @param st
	 *            Path to file.
	 * @param delimiter
	 *            Delimiter to use.
	 * @return A {@link String} array.
	 */
	public static String[] readFile(String st, String delimiter)
	{
		try
		{
			File f = new File(st);
			Scanner scanner = new Scanner(new FileReader(f));
			int count = 0;
			while (scanner.hasNextLine())
			{
				scanner.nextLine();
				count++;
			}
			String[] contents = new String[count];
			scanner = new Scanner(new FileReader(f));
			scanner.useDelimiter(delimiter);
			for (int i = 0; i < count; i++)
			{
				String val = isolate(scanner.nextLine(), scanner.delimiter().toString());
				contents[i] = val;
			}
			return contents;
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}
		return null;
	}

	private static String isolate(String line, String delimiter)
	{
		Scanner scanner = new Scanner(line);
		scanner.useDelimiter(delimiter);
		if (scanner.hasNext())
		{
			scanner.next();
			String value = scanner.next();
			return value.trim();
		}

		System.out.println("Empty or invalid line. Unable to process.");

		return "oops";
	}

	/**
	 * Creates and returns a random color.
	 * 
	 * @return A random color.
	 */
	public static Color randomColor()
	{
		Random r = new Random();
		return new Color(r.nextFloat(), r.nextFloat(), r.nextFloat());
	}

	/**
	 * Will generate a random set of {@link Coordinates} which exist in the given {@link Base}.
	 * 
	 * @param b
	 *            The {@link Base} in which to get {@link Coordinates}.
	 * @return A random set of {@link Coordinates} which exist in the given {@link Base}.
	 */
	public static Coordinates randomCoordinates(Base b)
	{
		return new Coordinates(new Random().nextInt(b.getColumns()), new Random().nextInt(b.getRows()));
	}

	/**
	 * Deletes the Entity save folder.
	 */
	public static void deleteEntFolder()
	{
		System.err.println("Deleting entity folder...");
		File f = saveFolder;
		if (f.isDirectory())
		{
			boolean success = delete(f);
			if (success)
				System.err.println("Entity folder deleted successfully");
			else
				System.err.println("Error in deleting entity folder");
		}
		else
		{
			System.err.println("Entity folder not found");
		}
	}

	/**
	 * Deletes a {@link File} and all its children files.
	 * 
	 * @param f
	 *            {@link File} to delete.
	 * @return True if the operation was successful, else false.
	 */
	private static boolean delete(File f)
	{
		if (f.isDirectory())
		{
			String[] subs = f.list();
			for (int i = 0; i < subs.length; i++)
			{
				System.err.println("-Deleting " + subs[i]);
				boolean success = delete(new File(f, subs[i]));
				if (!success)
					return false;
			}
		}
		return f.delete();
	}

	/**
	 * Creates a dialog with buttons.
	 * 
	 * @param paneText
	 *            Text to show in the dialog.
	 * @param options
	 *            Names of buttons.
	 * @param title
	 *            Title of dialog.
	 * @param events
	 *            What happens when a button gets pressed.
	 */
	public static void makeDialog(String paneText, Object[] options, String title, Runnable[] events)
	{
		if (!isDialog)
		{
			isDialog = true;
			JOptionPane pane = new JOptionPane(paneText);
			pane.setOptions(options);
			JDialog dialog = pane.createDialog(new JFrame(), title);
			dialog.setSize(400, 150);
			Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
			dialog.setLocation((screensize.width - dialog.getWidth()) / 2, (screensize.height - dialog.getHeight()) / 2);
			dialog.setVisible(true);
			Object obj = pane.getValue();
			int result = -1;
			for (int k = 0; k < options.length; k++)
				if (options[k].equals(obj))
					result = k;
			dialog.dispose();
			isDialog = false;
			if (result == -1)
				System.exit(0);
			events[result].run();
		}
	}

	/**
	 * Plays a sound.
	 * 
	 * @param file
	 *            The path to the sound file.
	 */
	public static void playSound(String file)
	{
		File soundFile = null;
		AudioInputStream audioStream = null;

		SourceDataLine sourceLine = null;
		String strFilename = soundsFolder + "/" + file;
		try
		{
			soundFile = new File(strFilename);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		try
		{
			audioStream = AudioSystem.getAudioInputStream(soundFile);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		AudioFormat audioFormat = audioStream.getFormat();
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
		try
		{
			sourceLine = (SourceDataLine) AudioSystem.getLine(info);
			sourceLine.open(audioFormat);
		}
		catch (LineUnavailableException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}

		sourceLine.start();

		int nBytesRead = 0;
		byte[] abData = new byte[128000];
		while (nBytesRead != -1)
		{
			try
			{
				nBytesRead = audioStream.read(abData, 0, abData.length);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			if (nBytesRead < 0)
			{
				continue;
			}
		}

		sourceLine.drain();
		sourceLine.close();
	}

	/**
	 * Capitalizes a word.
	 * 
	 * @param word
	 *            Word to capitalize.
	 * @return The capitalized word.
	 */
	public static String capitalize(String word)
	{
		if (word.length() > 0)
			return word.substring(0, 1).toUpperCase() + word.substring(1, word.length()).toLowerCase();
		return "";
	}

	/**
	 * Prints messages out into the console.
	 * 
	 * @param st
	 *            The message to print.
	 * @param level
	 *            The {@link MessageLevel} of the message.
	 */
	public static synchronized void print(String st, MessageLevel level)
	{
		if (!GrameManager.isDisablePrints() && ((level == MessageLevel.DEBUG || level == MessageLevel.DEBUG_ERROR && GrameManager.isDebug()) || (level == MessageLevel.SPAM && GrameManager.isSpam()) || (level == MessageLevel.NORMAL) || (level == MessageLevel.ERROR)))
		{
			StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
			wordQueue.add("[" + caller.getFileName().subSequence(0, caller.getFileName().indexOf(".java")) + ":" + caller.getLineNumber() + "] " + st);
			if (level == MessageLevel.ERROR || level == MessageLevel.DEBUG_ERROR)
				console.setConsoleTextColor(Color.red);
			else
				if (level == MessageLevel.DEBUG)
					console.setConsoleTextColor(Color.gray.brighter());
				else
					console.setConsoleTextColor(Color.white);
			console.addText(wordQueue.get(0) + "\n");
			wordQueue.remove(0);
		}
	}

	/**
	 * Will create a clickable JLabel which opens a URL.
	 * 
	 * @param text
	 *            JLabel text.
	 * @param URL
	 *            The URL to open on click.
	 * @param toolTip
	 *            The tooltip of the JLabel.
	 * @return A clickable JLabel.
	 */
	public static JLabel linkify(final String text, String URL, String toolTip)
	{
		URI temp = null;
		try
		{
			temp = new URI(URL);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		final URI uri = temp;
		final JLabel link = new JLabel();
		link.setText("<HTML><FONT color=\"#000099\">" + text + "</FONT></HTML>");
		if (!toolTip.equals(""))
			link.setToolTipText(toolTip);
		link.setCursor(new Cursor(Cursor.HAND_CURSOR));
		link.addMouseListener(new MouseAdapter()
		{
			public void mouseExited(MouseEvent arg0)
			{
				link.setText("<HTML><FONT color=\"#000099\">" + text + "</FONT></HTML>");
			}

			public void mouseEntered(MouseEvent arg0)
			{
				link.setText("<HTML><FONT color=\"#000099\"><U>" + text + "</U></FONT></HTML>");
			}

			public void mouseClicked(MouseEvent arg0)
			{
				if (Desktop.isDesktopSupported())
				{
					try
					{
						Desktop.getDesktop().browse(uri);
					}
					catch (Exception e)
					{
						CrashManager.showException(e);
					}
				}
				else
				{
					JOptionPane pane = new JOptionPane("Could not open link.");
					JDialog dialog = pane.createDialog(new JFrame(), "");
					dialog.setVisible(true);
				}
			}
		});
		return link;
	}

	/**
	 * Loads some basic commands.
	 */
	public static void loadBasicCommands()
	{
		ArrayList<Command<?>> commands = new ArrayList<Command<?>>();
		commands.add(new setVisibleCommand(console, "setvisible", "Toggles the visibility of the main window. Usage: setvisible [true/false]", 0, 1));
		commands.add(new MoveGrameObjectCommand(console, "move", "Moves a Grame Object. Usage: move <go ID> <base ID> <dir>", 3, 4));
		commands.add(new HelpCommand(console, "help", "Will print the help of a command. Leave blank for all the commands. Usage: help [command name]", 0, 1));
		commands.add(new RenderBaseCommand(console, "render", "Will render a base using a render in the Render list. Usage: render <base ID> <render name>", 2, 2));
		commands.add(new QuitCommand(console, "quit", "Quits the program. Usage: quit", 0, 0));
		commands.add(new CreateEntityCommand(console, "createentity", "Creates a new Entity. Usage: createentity [n:name] [t:type] [l:level] [c:color]", 0, 4));
		commands.add(new AddGrameObjectCommand(console, "addobject", "Adds a Grame Object to a Base. Usage: addobject <go ID> <base ID> <x> <y>", 4, 4));
		commands.add(new AddEntityAICommand(console, "addentityai", "Adds a MovememntAI to an Entity's AI list. Usage: addentityai <entity ID> <base ID> <movementai name>", 3, 3));
		commands.add(new MakePlayerCommand(console, "makeplayer", "Turns an Entity into a controllable \"player\". Usage: makeplayer <entity ID> <player 1/2> <base ID> <true/false>", 4, 4));
		commands.add(new SetEntityOverrideAICommand(console, "setentityoverrideai", "Sets the override AI for a given Entity. Usage: setentityoverrideai <entity ID> <base ID> <override movementai name>", 3, 3));
		commands.add(new ClearEntityAI(console, "clearentityai", "Clears the AI list of an Entity. Usage: clearentityai <entity ID>", 1, 1));
		commands.add(new PrintEntityAICommand(console, "printentityai", "Prints the AI list for a given Entity. Usage: printentityai <entity ID>", 1, 1));
		commands.add(new isOccupiedCommand(console, "isoccupied", "Checks whether Coordinates in a Base are occupied by a Grame object. Usage: isoccupied <base ID> <coordinates x> <coordinates y>", 3, 3));
		commands.add(new DrawCoordinatesCommand(console, "drawcoordinates", "Draws the coordinates in each square. Usage: drawcoordinates <true/false>", 1, 1));
		commands.add(new SetObjectSpeedCommand(console, "setspeed", "Sets the speed of an object.", 2, 2));
		commands.add(new SetSpriteCommand(console, "setsprite", "Sets a certain sprite. Usage: setsprite <sprite name> <path to sprite>", 2, -1));
		console.loadCommands(commands);
		print("Loaded " + Command.commands.size() + " commands.", MessageLevel.DEBUG);
	}

	/**
	 * Loads some basic AIs.
	 */
	public static void loadBasicAIs()
	{
		GrameManager.addAI(new SimpleStrollAI());
		GrameManager.addAI(new PlayerMovementAI(1));
		GrameManager.addAI(new SimpleChaseAI());
		GrameManager.addAI(new PlayerSimAI());
	}

	/**
	 * Prints out the current stack trace.
	 */
	public static void dumpStackTrace()
	{
		for (StackTraceElement ste : Thread.currentThread().getStackTrace())
			System.out.println(ste);
	}

	/**
	 * Used to denote print types.
	 * 
	 * @author Meshulam Silk <moomoohk@ymail.com>
	 * @version 1.0
	 * @since 2013-04-05
	 */
	public enum MessageLevel
	{
		/**
		 * Error messages.
		 */
		ERROR,
		/**
		 * Normal messages.
		 */
		NORMAL,
		/**
		 * Normal debug messages.
		 */
		DEBUG,
		/**
		 * Debug error messages.
		 */
		DEBUG_ERROR,
		/**
		 * Spam messages.
		 * <p>
		 * These are messages that might get sent repeatedly thus spamming the console.
		 */
		SPAM
	}
}
