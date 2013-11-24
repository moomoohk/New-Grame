package com.moomoohk.Grame.Essentials;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;

import com.moomoohk.Grame.Basics.Dir;
import com.moomoohk.Grame.Essentials.GrameUtils.MessageLevel;
import com.moomoohk.Grame.Graphics.GridRender;
import com.moomoohk.Grame.Graphics.RenderManager;
import com.moomoohk.Grame.Interfaces.GrameObject;
import com.moomoohk.Grame.Interfaces.MainGrameClass;
import com.moomoohk.Grame.Interfaces.MovementAI;
import com.moomoohk.Grame.Interfaces.Render;
import com.moomoohk.Grame.test.MenuConfiguration;
import com.moomoohk.Mootilities.FileUtils.FileUtils;
import com.moomoohk.Mootilities.FrameDragger.FrameDragger;
import com.moomoohk.Mootilities.OSUtils.OSUtils;
import com.moomoohk.Mootilities.OSUtils.OSUtils.OS;
import com.moomoohk.Mootilities.ObjectUtils.ObjectUtils;

/**
 * The Grame Manager takes care of all the internal Grame operations.
 * <p>
 * Indexes and ticks any {@link GrameObject}s and {@link Base}s that are created automatically.
 * 
 * @author Meshulam Silk <moomoohk@ymail.com>
 * @version 1.0
 * @since 2013-04-05
 */
public class GrameManager implements Runnable
{
	/**
	 * The Grame version number.
	 */
	public static final String VERSION_NUMBER = "4.0.2";
	/**
	 * The WASD keys parsed to a {@link Dir}.
	 */
	public static Dir dir1 = null;
	/**
	 * The arrow keys parsed to a {@link Dir}.
	 */
	public static Dir dir2 = null;
	/**
	 * Current game time.
	 */
	public static int time = 1;
	private static boolean initialized = false;
	private static EngineState engineState = null;
	private static HashMap<String, Render> renders = new HashMap<String, Render>();
	private static HashMap<String, MovementAI> ais = new HashMap<String, MovementAI>();
	private static String gameName;
	private static boolean running = false;
	public static boolean paused = false;
	private static boolean debug = false;
	private static boolean disablePrints = false;
	private static boolean spam = false;
	private static InputHandler input;
	private static Thread thread;
	private static int fps = 0;
	private static Render defaultRender = new GridRender();
	private static File savePath;
	private static MainMenu mainMenu;
	private static MenuConfiguration menuConfiguration;

	public static void initialize(MainGrameClass mainClass)
	{
		initialize(mainClass, new MenuConfiguration());
	}

	public static void initialize(MainGrameClass mainClass, MenuConfiguration menuConfig)
	{
		if (initialized)
			return;
		initialized = true;
		GrameManager.setGameName(mainClass.getGameName());

		input = new InputHandler();
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
		{
			public void uncaughtException(Thread t, Throwable e)
			{
				GrameUtils.console.addText("Unhandled exception!\n");
				e.printStackTrace();
			}
		});

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
		{
			public void run()
			{
				GrameUtils.print("Exiting...", MessageLevel.NORMAL);
				stop();
				disposeAll();
			}
		}));
		try
		{
			if (OSUtils.getCurrentOS() == OS.UNIX)
				savePath = new File(OSUtils.getDynamicStorageLocation() + ".grame/saves/" + mainClass.getGameName() + "/");
			else
				savePath = new File(OSUtils.getDynamicStorageLocation() + "Grame/Saves/" + mainClass.getGameName() + "/");
		}
		catch (Error e)
		{
			System.out.println("You don't appear to have Mootilities installed!");
			System.out.println("Get it at: https://github.com/moomoohk/Mootilities/raw/master/Build/Mootilities.jar");
			System.exit(0);
		}
		if (!savePath.exists())
			savePath.mkdirs();
		menuConfiguration = menuConfig;
		mainMenu = new MainMenu(mainClass);
		mainMenu.setVisible(true);
	}

	private static void start()
	{
		if (running)
			return;
		running = true;
		thread = new Thread(new GrameManager(), "Engine Thread");
		thread.start();
		GrameUtils.print("Grame " + VERSION_NUMBER + " is initialized.", MessageLevel.NORMAL);
	}

	public static synchronized void stop()
	{
		if (!running)
			return;
		paused = true;
		running = false;
	}

	/**
	 * Starts the Grame Manager clock.
	 * <p>
	 * This method should never be called by the user as doing that might create issues.
	 */
	public void run()
	{
		int frames = 0;
		double unprocessed = 0.0D;
		long prev = System.nanoTime();
		double secondsTick = 1 / 60.0;
		int tickCount = 0;
		boolean ticked = false;
		while (running)
		{
			long curr = System.nanoTime();
			long delta = curr - prev;
			prev = curr;
			unprocessed += delta / 1000000000.0;
			while (unprocessed > secondsTick)
			{
				unprocessed -= secondsTick;
				ticked = true;
				tickCount++;
				if (tickCount % 60 != 0)
				{
					fps = frames;
					prev += 1000L;
					frames = 0;
				}
				if (engineState != null)
				{
					if (!paused)
					{
						tick(input.key);
						tickGrameObjects();
						tickBases();
					}
					RenderManager.tick();
				}
			}

			if (ticked)
				frames++;
			frames++;
		}
		RenderManager.dispose();
		System.gc();
	}

	private void tick(boolean[] key)
	{
		time += 1;
		dir1 = null;
		dir2 = null;
		if (key[83])
			dir1 = Dir.DOWN;
		if (key[65])
			dir1 = Dir.LEFT;
		if (key[68])
			dir1 = Dir.RIGHT;
		if (key[87])
			dir1 = Dir.UP;
		if (key[83] && key[65])
			dir1 = new Dir(-1, 1);
		if (key[83] && key[68])
			dir1 = new Dir(1, 1);
		if (key[87] && key[65])
			dir1 = new Dir(-1, -1);
		if (key[87] && key[68])
			dir1 = new Dir(1, -1);

		if (key[40])
			dir2 = Dir.DOWN;
		if (key[37])
			dir2 = Dir.LEFT;
		if (key[39])
			dir2 = Dir.RIGHT;
		if (key[38])
			dir2 = Dir.UP;
		if (key[40] && key[37])
			dir2 = new Dir(-1, 1);
		if (key[40] && key[39])
			dir2 = new Dir(1, 1);
		if (key[38] && key[37])
			dir2 = new Dir(-1, -1);
		if (key[38] && key[39])
			dir2 = new Dir(1, -1);
		if (key[27])
		{
			key[27] = false;
			paused = true;
			mainMenu.setVisible(true);
		}
	}

	private static void tickGrameObjects()
	{
		try
		{
			for (int i = 0; i < engineState.getGrameObjects().size(); i++)
			{
				GrameObject go = engineState.getGrameObjects().get(i);
				if (go == null)
					continue;
				if (engineState.getBases() != null)
					for (int bID = 0; bID < engineState.getBases().size(); bID++)
						if (findBase(bID).containsGrameObject(go.ID) && go.getSpeed() != 0 && time % go.getSpeed() == 0 && !go.isPaused())
							go.tick(bID);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private static void tickBases()
	{
		if (engineState.getBases() == null)
			return;
		synchronized (engineState.getBases())
		{
			try
			{
				for (Base b : engineState.getBases())
				{
					if (b == null)
						continue;
					b.tick();
				}
			}
			catch (Exception e)
			{
				CrashManager.showException(e);
			}
		}
	}

	/**
	 * Disposes of all the {@link GrameObject}s and {@link Base}s.
	 */
	public static void disposeAll()
	{
		GrameUtils.print("Disposing of all the Bases in the Base list...", MessageLevel.NORMAL);
		if (engineState != null)
			for (int i = 0; i < engineState.getBases().size(); i++)
			{
				GrameUtils.print("Disposing of " + engineState.getBases().get(i).ID, MessageLevel.NORMAL);
				engineState.getBases().remove(i);
			}
		GrameUtils.print("Done disposing of Bases.", MessageLevel.NORMAL);
	}

	/**
	 * Adds a {@link GrameObject} to the Grame Object list.
	 * <p>
	 * The user should never have to call this method.
	 * 
	 * @param go
	 *            {@link GrameObject} to add.
	 * @return The ID number for the object.
	 */
	public static int add(GrameObject go)
	{
		if (!initialized)
		{
			System.out.println("FATAL: Grame Manager not initialized! All GrameObject instantiations should be made from in the newGame method.");
			System.exit(0);
		}
		if (engineState.getGrameObjects().contains(go))
		{
			GrameUtils.print(go.getName() + " already exists in the Grame Object list!", MessageLevel.ERROR);
			return -1;
		}
		engineState.getGrameObjects().add(go);
		GrameUtils.print("Added " + go.getName() + " to the Grame Objects list (ID:" + (engineState.getGrameObjects().size() - 1) + ")", MessageLevel.NORMAL);
		return engineState.getGrameObjects().size() - 1;
	}

	/**
	 * Adds a {@link Base} to the Base list.
	 * <p>
	 * The user should never have to call this method.
	 * 
	 * @param b
	 *            {@link Base} to add.
	 * @return The ID number for the Base.
	 */
	public static int add(Base b)
	{
		if (!initialized)
		{
			System.out.println("FATAL: Grame Manager not initialized! All Base instantiations should be made from in the newGame method.");
			System.exit(0);
		}
		if (engineState.getBases().contains(b))
		{
			GrameUtils.print(b.toString() + " already exists in the Grame Object list!", MessageLevel.ERROR);
			return -1;
		}
		engineState.getBases().add(b);
		GrameUtils.print("Added a Base to the Base list (ID:" + (engineState.getBases().size() - 1) + ")", MessageLevel.NORMAL);
		return engineState.getBases().size() - 1;
	}

	/**
	 * Finds and returns a {@link GrameObject} from the Grame Objects list.<br>
	 * If the object is not found, null will be returned.
	 * 
	 * @param id
	 *            The ID of the object to find.
	 * @return The {@link GrameObject} with that ID. If not in the list, null will be returned.
	 */
	public static GrameObject findGrameObject(int id)
	{
		try
		{
			return engineState.getGrameObjects().get(id);
		}
		catch (Exception e)
		{
			// GrameUtils.print("Entity with ID:" + id +
			// " not found! Returning null instead.", "Grame Manager", false);
		}
		return null;
	}

	/**
	 * Finds and returns a {@link Base} from the Base list.<br>
	 * If the {@link Base} is not found, null will be returned.
	 * 
	 * @param id
	 *            The ID of the {@link Base} to find.
	 * @return The {@link Base} with that ID. If not in the list, null will be returned.
	 */
	public static Base findBase(int id)
	{
		try
		{
			return engineState.getBases().get(id);
		}
		catch (Exception e)
		{
			GrameUtils.print("Base with ID:" + id + " not found! Returning null instead.", MessageLevel.ERROR);
		}
		return null;
	}

	/**
	 * Sets the default {@link Render}.
	 * 
	 * @param render
	 *            {@link Render} to set.
	 */
	public static void setDefaultRender(Render render)
	{
		defaultRender = render;
	}

	/**
	 * Gets the default {@link Render}.
	 * 
	 * @return The default {@link Render}.
	 */
	public static Render getDefaultRender()
	{
		return defaultRender;
	}

	/**
	 * Indexes a {@link Render}.
	 * 
	 * @param render
	 *            {@link Render} to index.
	 */
	public static void addRender(Render render)
	{
		if (render == null)
			return;
		String name = render.getName().toLowerCase().trim().replace(' ', '_');
		for (String temp : renders.keySet())
			if (renders.get(temp).getName().equals(render.getName()))
				return;
		renders.put(name, render);
		GrameUtils.print("Added " + name + " to the render list.", MessageLevel.DEBUG);
	}

	/**
	 * Indexes a {@link MovementAI}.
	 * 
	 * @param ai
	 *            {@link MovementAI} to index.
	 */
	public static void addAI(MovementAI ai)
	{
		if (ai == null)
			return;
		String name = ai.toString().toLowerCase().trim().replace(' ', '_');
		for (String temp : ais.keySet())
			if (ais.get(temp).toString().equals(ai.toString()))
				return;
		ais.put(name, ai);
		GrameUtils.print("Added " + name + " to the AI list.", MessageLevel.DEBUG);
	}

	/**
	 * Pauses or unpauses all the {@link GrameObject}s.
	 * 
	 * @param f
	 *            True to pause, false to unpause all the {@link GrameObject}.
	 */
	public static void pauseAllGrameObjects(boolean f)
	{
		for (GrameObject go : engineState.getGrameObjects())
			go.pause(f);
	}

	/**
	 * Sets whether debug prints will be visible or not.
	 * 
	 * @param debug
	 *            True to enable debug prints, else false.
	 */
	public static void setDebug(boolean debug)
	{
		GrameManager.debug = debug;
	}

	/**
	 * Returns whether or not debug prints are enabled.
	 * 
	 * @return True if debug prints are enabled, else false.
	 */
	public static boolean isDebug()
	{
		return debug;
	}

	public static void setGameName(String gameName)
	{
		GrameManager.gameName = gameName;
	}

	public static String getGameName()
	{
		return gameName;
	}

	/**
	 * Completely disable prints.
	 * 
	 * @param disablePrints
	 *            True to disable all prints, else false.
	 */
	public static void setDisablePrints(boolean disablePrints)
	{
		GrameManager.disablePrints = disablePrints;
	}

	/**
	 * Returns whether or not prints are completely disabled.
	 * 
	 * @return True if prints are completely disabled, else false.
	 */
	public static boolean isDisablePrints()
	{
		return disablePrints;
	}

	/**
	 * Sets whether spam prints will be visible or not.
	 * 
	 * @param spam
	 *            True to enable spam prints, else false.
	 */
	public static void setSpam(boolean spam)
	{
		GrameManager.spam = spam;
	}

	/**
	 * Returns whether or not spam prints are enabled.
	 * 
	 * @return True if spam prints are enabled, else false.
	 */
	public static boolean isSpam()
	{
		return spam;
	}

	/**
	 * Gets the {@link InputHandler} that is currently being used.
	 * 
	 * @return The {@link InputHandler} that is currently being used.
	 */
	public static InputHandler getInputHandler()
	{
		return input;
	}

	/**
	 * Gets all the loaded {@link MovementAI}s sorted in a HashMap<String, {@link MovementAI}> where the names (spaces replaced with '-') are the keys.
	 * 
	 * @return A HashMap<String, {@link MovementAI}> of all loaded {@link MovementAI}s.
	 */
	public static HashMap<String, MovementAI> getAIs()
	{
		return ais;
	}

	/**
	 * Gets all the loaded {@link Render}s sorted in a HashMap<String, {@link Render}> where the names (spaces replaced with '-') are the keys.
	 * 
	 * @return A HashMap<String, {@link Render}> of all loaded {@link Render}s.
	 */
	public static HashMap<String, Render> getRenders()
	{
		return renders;
	}

	/**
	 * Gets the size of the {@link GrameObject} list.
	 * 
	 * @return The size of the {@link GrameObject} list.
	 */
	public static int getObjectListLength()
	{
		return engineState.getGrameObjects().size();
	}

	public static int getBaseListLength()
	{
		return engineState.getBases().size();
	}

	/**
	 * Gets the current FPS (Frames Per Second) count.
	 * 
	 * @return The current FPS (Frames Per Second) count.
	 */
	public static int getFPS()
	{
		return fps;
	}

	public static void setMainBase(int bID)
	{
		engineState.setMainBase(bID);
	}

	public static int getMainBase()
	{
		return engineState.getMainBase();
	}

	public static void setMainRender(Render r)
	{
		engineState.setMainRender(r);
	}

	public static Render getMainRender()
	{
		return engineState.getMainRender();
	}

	public static boolean saveEngineState(String saveName, boolean overwrite, Component parent)
	{
		boolean isPaused = paused;
		paused = true;
		boolean save = overwrite;
		if (!overwrite)
			if (new File(savePath.toString() + "/" + saveName + ".GrameSave").exists())
				save = JOptionPane.showConfirmDialog(parent, "Would you like to overwrite the " + saveName + " save?", "Overwrite save", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.YES_OPTION;
			else
				save = true;
		if (save)
			try
			{
				engineState.setSaved(new GregorianCalendar().getTime());
				ObjectUtils.save(engineState, savePath.toString(), saveName, "GrameSave");
			}
			catch (IOException e)
			{
				System.out.println("There was a problem saving!");
				new File(savePath.toString() + "/" + saveName + ".GrameSave").delete();
				e.printStackTrace();
			}
		paused = isPaused;
		return save;
	}

	private static class MainMenu extends JFrame
	{
		private static final long serialVersionUID = -2989260620184596791L;
		private static JPanel contentPane;
		private static MenuButton btnResume, btnNewGame, btnSaveGame, btnLoadGame, btnSettings, btnEndGame;
		private static JLabel lblMadeWithGrame;
		public static JScrollPane sidePanel;
		public static MouseListener helpTextListener = (new MouseAdapter()
		{
			public void mouseEntered(MouseEvent me)
			{
				lblMadeWithGrame.setText(((MenuButton) me.getSource()).getHelpText());
			}

			public void mouseExited(MouseEvent me)
			{
				lblMadeWithGrame.setText("");
			}
		});
		private static final Rectangle BUTTON1 = new Rectangle(10, 50, 130, 40), BUTTON2 = new Rectangle(20, 100, 110, 30), BUTTON3 = new Rectangle(20, 140, 110, 30), BUTTON4 = new Rectangle(20, 180, 110, 30), BUTTON5 = new Rectangle(20, 220, 110, 30), BUTTON6 = new Rectangle(20, 260, 110, 30);
		private static LoadGamePanel loadGamePanel = new LoadGamePanel(savePath.toString());
		private static SaveGamePanel saveGamePanel = new SaveGamePanel(savePath.toString());

		public MainMenu(final MainGrameClass mainClass)
		{
			setUndecorated(true);
			setResizable(false);
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setSize(600, 600);
			setLocationRelativeTo(null);
			contentPane = new JPanel();
			contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
			setContentPane(contentPane);
			contentPane.setLayout(null);

			JLabel lblGameName = new JLabel(mainClass.getGameName());
			lblGameName.setFont(new Font("Lucida Grande", Font.BOLD, 22));
			lblGameName.setHorizontalAlignment(SwingConstants.CENTER);
			lblGameName.setBounds(20, 6, 560, 33);
			contentPane.add(lblGameName);

			btnResume = new MenuButton("Resume Game", menuConfiguration.menuButtonStartColor, menuConfiguration.menuButtonEndColor, menuConfiguration.menuButtonClickColor, "Unpause the game");
			btnResume.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent arg0)
				{
					dispose();
					paused = false;
				}
			});
			contentPane.add(btnResume);

			btnNewGame = new MenuButton("New Game", menuConfiguration.menuButtonStartColor, menuConfiguration.menuButtonEndColor, menuConfiguration.menuButtonClickColor, "Start a new game");
			btnNewGame.setBounds(BUTTON1);
			btnNewGame.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent arg0)
				{
					dispose();
					paused = true;
					GrameUtils.console.setVisible(true);
					RenderManager.dispose();
					RenderManager.initialize();
					RenderManager.clearAllText();
					engineState = new EngineState();
					mainClass.newGame();
					start();
					paused = false;
				}
			});
			contentPane.add(btnNewGame);

			btnSaveGame = new MenuButton("Save Game", menuConfiguration.menuButtonStartColor, menuConfiguration.menuButtonEndColor, menuConfiguration.menuButtonClickColor, "Save your game to file");
			btnSaveGame.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent arg0)
				{
					saveGamePanel.loadInfo();
					saveGamePanel.showPanel();
					sidePanel.setViewportView(saveGamePanel);
				}
			});
			contentPane.add(btnSaveGame);

			btnLoadGame = new MenuButton("Load Game", menuConfiguration.menuButtonStartColor, menuConfiguration.menuButtonEndColor, menuConfiguration.menuButtonClickColor, "Load a saved game");
			btnLoadGame.setBounds(BUTTON2);
			btnLoadGame.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent arg0)
				{
					if (sidePanel.getViewport().getView() == null || sidePanel.getViewport().getView() != null && !sidePanel.getViewport().getView().equals(loadGamePanel))
					{
						loadGamePanel.loadInfo();
						loadGamePanel.updateGUI();
						sidePanel.setViewportView(loadGamePanel);
					}
				}
			});
			contentPane.add(btnLoadGame);

			btnSettings = new MenuButton("Settings", menuConfiguration.menuButtonStartColor, menuConfiguration.menuButtonEndColor, menuConfiguration.menuButtonClickColor, "Show the settings (not yet implemented)");
			btnSettings.setBounds(BUTTON3);
			btnSettings.setEnabled(false);
			contentPane.add(btnSettings);

			btnEndGame = new MenuButton("End Game", menuConfiguration.menuButtonStartColor, menuConfiguration.menuButtonEndColor, menuConfiguration.menuButtonClickColor, "Abandon the current game");
			btnEndGame.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent arg0)
				{
					sidePanel.setViewportView(null);
					stop();
					paused = false;
					updateButtons();
				}
			});
			contentPane.add(btnEndGame);

			sidePanel = new JScrollPane();
			sidePanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
			sidePanel.setBounds(150, 50, 430, 490);
			contentPane.add(sidePanel);

			lblMadeWithGrame = new JLabel();
			lblMadeWithGrame.addMouseListener(new MouseAdapter()
			{
				public void mouseEntered(MouseEvent me)
				{
					lblMadeWithGrame.setText("Made with moomoohk's Grame (v" + GrameManager.VERSION_NUMBER + ")");
				}

				public void mouseExited(MouseEvent me)
				{
					lblMadeWithGrame.setText("");
				}
			});
			lblMadeWithGrame.setFont(new Font("Lucida Grande", Font.BOLD | Font.ITALIC, 11));
			lblMadeWithGrame.setForeground(Color.DARK_GRAY);
			lblMadeWithGrame.setBounds(20, 553, 560, 37);
			contentPane.add(lblMadeWithGrame);

			MenuButton btnQuit = new MenuButton("Quit", menuConfiguration.quitButtonStartColor, menuConfiguration.quitButtonEndColor, menuConfiguration.quitButtonClickColor, "Quit the game");
			btnQuit.setBounds(10, 500, 130, 40);
			btnQuit.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent arg0)
				{
					System.exit(0);
				}
			});
			contentPane.add(btnQuit);

			btnResume.addMouseListener(helpTextListener);
			btnNewGame.addMouseListener(helpTextListener);
			btnSaveGame.addMouseListener(helpTextListener);
			btnLoadGame.addMouseListener(helpTextListener);
			btnSettings.addMouseListener(helpTextListener);
			btnEndGame.addMouseListener(helpTextListener);
			btnQuit.addMouseListener(helpTextListener);

			new FrameDragger().applyTo(this);
		}

		public void dispose()
		{
			super.dispose();
			sidePanel.setViewportView(null);
			lblMadeWithGrame.setText("");
		}

		public void updateButtons()
		{
			if (paused)
			{
				btnResume.setBounds(BUTTON1);
				btnSaveGame.setBounds(BUTTON2);
				btnNewGame.setBounds(BUTTON3);
				btnLoadGame.setBounds(BUTTON4);
				btnSettings.setBounds(BUTTON5);
				btnEndGame.setBounds(BUTTON6);
			}
			else
			{
				btnResume.setBounds(0, 0, 0, 0);
				btnSaveGame.setBounds(0, 0, 0, 0);
				btnNewGame.setBounds(BUTTON1);
				btnLoadGame.setBounds(BUTTON2);
				btnSettings.setBounds(BUTTON3);
				btnEndGame.setBounds(0, 0, 0, 0);
			}
		}

		public void setVisible(boolean f)
		{
			updateButtons();
			super.setVisible(f);
		}

		public static class MenuButton extends JButton
		{
			private static final long serialVersionUID = -2192610213120657509L;

			public boolean mouseOn = false, mouseDown = false;
			private double animTime = 0;
			private Color startColor, endColor, clickColor, fill;
			private String helpText;
			private Timer t = new Timer(10, new ActionListener()
			{
				public void actionPerformed(ActionEvent arg0)
				{
					repaint();
					animTime += 0.03;
				}
			});

			public MenuButton()
			{
				this("Default", Color.black, Color.black, Color.black, "Default");
			}

			public MenuButton(String text, Color startColor, Color endColor, Color clickColor, String helpText)
			{
				super(text);
				this.startColor = startColor;
				this.endColor = endColor;
				this.clickColor = clickColor;
				this.fill = this.startColor;
				this.helpText = helpText;
				addMouseListener(new MouseAdapter()
				{
					public void mouseReleased(MouseEvent arg0)
					{
						mouseDown = false;
						repaint();
						t.stop();
					}

					@Override
					public void mousePressed(MouseEvent arg0)
					{
						mouseDown = true;
						repaint();
						animTime = 0;
						t.start();
					}

					@Override
					public void mouseExited(MouseEvent arg0)
					{
						mouseOn = false;
						repaint();
					}

					@Override
					public void mouseEntered(MouseEvent arg0)
					{
						mouseOn = true;
						repaint();
						animTime = 0;
						t.start();
					}
				});
			}

			public void setStartColor(Color c)
			{
				this.startColor = c;
			}

			public void setEndColor(Color c)
			{
				this.endColor = c;
			}

			public void setClickColor(Color c)
			{
				this.clickColor = c;
			}

			public void setHelpText(String helpText)
			{
				this.helpText = helpText;
			}

			public String getHelpText()
			{
				return this.helpText;
			}

			protected void paintComponent(Graphics g)
			{
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				if (isEnabled())
				{
					if (mouseDown && mouseOn)
						fill = clickColor;
					else
						if (mouseOn)
							fill = new Color((int) (endColor.getRed() * Math.abs(Math.sin(animTime)) + startColor.getRed() * (1 - Math.abs(Math.sin(animTime)))), (int) (endColor.getGreen() * Math.abs(Math.sin(animTime)) + startColor.getGreen() * (1 - Math.abs(Math.sin(animTime)))),
									(int) (endColor.getBlue() * Math.abs(Math.sin(animTime)) + startColor.getBlue() * (1 - Math.abs(Math.sin(animTime)))));
						else
						{
							fill = new Color((int) (fill.getRed() * Math.abs(Math.sin(animTime)) + startColor.getRed() * (1 - Math.abs(Math.sin(animTime)))), (int) (fill.getGreen() * Math.abs(Math.sin(animTime)) + startColor.getGreen() * (1 - Math.abs(Math.sin(animTime)))), (int) (fill.getBlue()
									* Math.abs(Math.sin(animTime)) + startColor.getBlue() * (1 - Math.abs(Math.sin(animTime)))));
							if (fill.equals(startColor))
							{
								t.stop();
								if (mouseDown)
									fill = clickColor;
								else
									fill = startColor;
							}
						}
					g2.setPaint(fill);
				}
				else
					g2.setPaint(menuConfiguration.disabledButtonColor);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
				g2.setPaint(Color.black);
				if (mouseOn && isEnabled())
					g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 6, 6);
				else
					g2.fillRoundRect(3, 3, getWidth() - 6, getHeight() - 6, 6, 6);
				g2.setPaint(Color.white);
				FontMetrics fm = g2.getFontMetrics();
				g2.drawString(getText(), (getWidth() / 2) - (fm.stringWidth(getText()) / 2), (getHeight() / 2) + 4);
				g2.dispose();
			}
		}

		public abstract static class MenuPanel extends JPanel
		{
			private static final long serialVersionUID = 8536992209102721367L;
			protected String savePath;
			protected HashMap<String, EngineState> saves;
			protected JScrollPane scrollPane;
			protected MenuButton btnConfirm, btnCancel;
			protected JLabel noSaves = new JLabel("No saves found");

			public MenuPanel(String savePath)
			{
				this.savePath = savePath;
				this.saves = new HashMap<String, EngineState>();
				this.noSaves.setHorizontalAlignment(SwingConstants.CENTER);
				this.noSaves.setFont(new Font("Lucida Grande", Font.BOLD | Font.ITALIC, 15));
				this.noSaves.setForeground(Color.lightGray);

				SpringLayout springLayout = new SpringLayout();
				setLayout(springLayout);

				JLabel lblSelectSaveFile = new JLabel();
				lblSelectSaveFile.setFont(new Font("Lucida Grande", Font.BOLD, 13));
				springLayout.putConstraint(SpringLayout.NORTH, lblSelectSaveFile, 20, SpringLayout.NORTH, this);
				springLayout.putConstraint(SpringLayout.WEST, lblSelectSaveFile, 20, SpringLayout.WEST, this);
				springLayout.putConstraint(SpringLayout.EAST, lblSelectSaveFile, -20, SpringLayout.EAST, this);
				add(lblSelectSaveFile);

				scrollPane = new JScrollPane();
				scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
				springLayout.putConstraint(SpringLayout.NORTH, scrollPane, 10, SpringLayout.SOUTH, lblSelectSaveFile);
				springLayout.putConstraint(SpringLayout.WEST, scrollPane, 20, SpringLayout.WEST, this);
				springLayout.putConstraint(SpringLayout.SOUTH, scrollPane, -100, SpringLayout.SOUTH, this);
				springLayout.putConstraint(SpringLayout.EAST, scrollPane, -20, SpringLayout.EAST, this);
				add(scrollPane);

				btnConfirm = new MenuButton();
				springLayout.putConstraint(SpringLayout.SOUTH, btnConfirm, -20, SpringLayout.SOUTH, this);
				springLayout.putConstraint(SpringLayout.EAST, btnConfirm, 0, SpringLayout.EAST, lblSelectSaveFile);
				springLayout.putConstraint(SpringLayout.SOUTH, scrollPane, -20, SpringLayout.NORTH, btnConfirm);
				btnConfirm.setPreferredSize(new Dimension(100, 30));
				btnConfirm.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent paramActionEvent)
					{
						confirm();
					}
				});
				add(btnConfirm);

				btnCancel = new MenuButton("Cancel", menuConfiguration.cancelButtonStartColor, menuConfiguration.cancelButtonEndColor, menuConfiguration.cancelButtonClickColor, "Close this panel");
				springLayout.putConstraint(SpringLayout.WEST, btnCancel, 0, SpringLayout.WEST, lblSelectSaveFile);
				springLayout.putConstraint(SpringLayout.SOUTH, btnCancel, 0, SpringLayout.SOUTH, btnConfirm);
				btnCancel.setPreferredSize(new Dimension(100, 30));
				btnCancel.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent arg0)
					{
						sidePanel.setViewportView(null);
						lblMadeWithGrame.setText("");
					}
				});
				add(btnCancel);

				btnConfirm.addMouseListener(helpTextListener);
				btnCancel.addMouseListener(helpTextListener);

				initGUI();
			}

			public void loadInfo()
			{
				File f = new File(savePath);
				if (!f.exists())
					return;
				saves = new HashMap<String, EngineState>();
				for (File child : f.listFiles(new FilenameFilter()
				{
					@Override
					public boolean accept(File paramFile, String paramString)
					{
						return paramString.endsWith(".GrameSave");
					}
				}))
				{
					String name = child.toString().substring(f.toString().length() + 1, child.toString().lastIndexOf("."));
					try
					{
						Object save = ObjectUtils.load(f.toString(), name, "GrameSave");
						if (save != null)
							this.saves.put(name, ((EngineState) save));
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}

			protected SpringLayout getSpringLayout()
			{
				return (SpringLayout) getLayout();
			}

			protected abstract void confirm();

			protected abstract void initGUI();

			protected abstract void updateGUI();
		}

		public static class LoadGamePanel extends MenuPanel
		{
			private static final long serialVersionUID = 5362729999369047215L;
			private MenuButton btnDeleteSave;
			private JPanel selectedPanel;
			private String selectedEngineStateName;

			public LoadGamePanel(String savePath)
			{
				super(savePath);
			}

			@Override
			protected void confirm()
			{
				mainMenu.dispose();
				paused = true;
				GrameUtils.console.setVisible(true);
				RenderManager.dispose();
				RenderManager.initialize();
				RenderManager.clearAllText();
				engineState = saves.get(selectedEngineStateName);
				RenderManager.render(engineState.getMainBase(), engineState.getMainRender());
				RenderManager.setVisible(true);
				start();
				paused = false;
			}

			protected void initGUI()
			{
				btnConfirm.setText("Load");
				btnConfirm.setStartColor(menuConfiguration.confirmButtonStartColor);
				btnConfirm.setEndColor(menuConfiguration.confirmButtonEndColor);
				btnConfirm.setClickColor(menuConfiguration.confirmButtonClickColor);
				btnConfirm.setHelpText("Load the selected save");

				JLabel lblSelectSaveFile = new JLabel("Select save file to load:");
				lblSelectSaveFile.setFont(new Font("Lucida Grande", Font.BOLD, 13));
				getSpringLayout().putConstraint(SpringLayout.NORTH, lblSelectSaveFile, 20, SpringLayout.NORTH, this);
				getSpringLayout().putConstraint(SpringLayout.WEST, lblSelectSaveFile, 20, SpringLayout.WEST, this);
				getSpringLayout().putConstraint(SpringLayout.EAST, lblSelectSaveFile, -20, SpringLayout.EAST, this);
				add(lblSelectSaveFile);

				getSpringLayout().putConstraint(SpringLayout.NORTH, scrollPane, 10, SpringLayout.SOUTH, lblSelectSaveFile);

				btnDeleteSave = new MenuButton("Delete", menuConfiguration.otherButtonStartColor, menuConfiguration.otherButtonEndColor, menuConfiguration.otherButtonClickColor, "Delete selected save");
				getSpringLayout().putConstraint(SpringLayout.SOUTH, btnDeleteSave, 0, SpringLayout.SOUTH, btnConfirm);
				getSpringLayout().putConstraint(SpringLayout.WEST, btnDeleteSave, 20, SpringLayout.EAST, btnCancel);
				getSpringLayout().putConstraint(SpringLayout.EAST, btnDeleteSave, -20, SpringLayout.WEST, btnConfirm);
				getSpringLayout().putConstraint(SpringLayout.NORTH, btnDeleteSave, 0, SpringLayout.NORTH, btnConfirm);
				btnDeleteSave.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent arg0)
					{
						if (JOptionPane.showConfirmDialog(mainMenu, "Delete this save?", "Are you sure?", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.YES_OPTION)
							try
							{
								FileUtils.delete(new File(GrameManager.savePath.toString() + "/" + selectedEngineStateName + ".GrameSave"));
								loadInfo();
								updateGUI();
							}
							catch (IOException e)
							{
								e.printStackTrace();
							}
					}
				});
				add(btnDeleteSave);
				btnDeleteSave.addMouseListener(helpTextListener);
			}

			public void updateGUI()
			{
				if (this.saves.size() == 0)
				{
					this.btnConfirm.setEnabled(false);
					this.btnDeleteSave.setEnabled(false);
					scrollPane.setViewportView(noSaves);
				}
				else
				{
					selectedPanel = new JPanel();
					JPanel savesListPanel = new JPanel();
					savesListPanel.setLayout(new GridBagLayout());
					for (final String key : saves.keySet())
						try
						{
							GridBagConstraints gbc = new GridBagConstraints();
							gbc.gridwidth = GridBagConstraints.REMAINDER;
							gbc.anchor = GridBagConstraints.NORTH;
							gbc.insets = new Insets(3, 0, 3, 0);
							final JPanel savePanel = new JPanel();
							savePanel.setLayout(null);
							savePanel.setPreferredSize(new Dimension(360, 85));
							savePanel.setBorder(BorderFactory.createLineBorder(Color.black));
							savePanel.addMouseListener(new MouseAdapter()
							{
								@Override
								public void mouseExited(MouseEvent paramMouseEvent)
								{
									if (!selectedPanel.equals(savePanel))
										savePanel.setBorder(BorderFactory.createLineBorder(Color.black));
									else
										savePanel.setBorder(BorderFactory.createMatteBorder(2, 5, 2, 2, Color.gray.darker().darker()));
								}

								@Override
								public void mouseEntered(MouseEvent paramMouseEvent)
								{
									savePanel.setBorder(BorderFactory.createMatteBorder(2, 5, 2, 2, Color.black));
								}

								public void mousePressed(MouseEvent arg0)
								{
									selectedPanel.setBorder(BorderFactory.createLineBorder(Color.black));
									selectedPanel = savePanel;
									selectedEngineStateName = key;
									savePanel.setBorder(BorderFactory.createMatteBorder(2, 5, 2, 2, Color.gray.darker().darker()));
									btnConfirm.setEnabled(true);
									btnDeleteSave.setEnabled(true);
								}
							});

							JLabel lblName = new JLabel(key);
							lblName.setFont(new Font(lblName.getFont().getName(), Font.BOLD, 14));
							lblName.setBounds(10, 0, 340, 30);
							savePanel.add(lblName);

							JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
							separator.setBounds(10, 25, 340, 30);
							savePanel.add(separator);

							JLabel lblSavedTitle = new JLabel("Last saved:");
							lblSavedTitle.setBounds(10, 25, 80, 30);
							savePanel.add(lblSavedTitle);

							JLabel lblSaved = new JLabel("" + saves.get(key).getSaved());
							lblSaved.setBounds(90, 25, 230, 30);
							savePanel.add(lblSaved);

							JLabel lblCreatedTitle = new JLabel("Created:");
							lblCreatedTitle.setBounds(10, 50, 80, 30);
							savePanel.add(lblCreatedTitle);

							JLabel lblCreated = new JLabel("" + saves.get(key).getDateCreated().getTime());
							lblCreated.setBounds(90, 50, 240, 30);
							savePanel.add(lblCreated);

							savesListPanel.add(savePanel, gbc);
						}
						catch (Exception e)
						{
							System.out.println("Conflict"); //TODO: Handle conflicts
						}
					btnConfirm.setEnabled(false);
					btnDeleteSave.setEnabled(false);
					scrollPane.setViewportView(savesListPanel);
				}
			}
		}

		public static class SaveGamePanel extends MenuPanel
		{
			private static final long serialVersionUID = 812980996331735043L;

			private JTextField saveField;
			private JLabel lblSelectSaveFile;

			public SaveGamePanel(String savePath)
			{
				super(savePath);
			}

			public void showPanel()
			{
				saveField.setText("");
				updateGUI();
			}

			@Override
			protected void confirm()
			{
				if (saveEngineState(saveField.getText(), false, mainMenu))
					saveField.setText("");
				loadInfo();
				updateGUI();
			}

			@Override
			protected void initGUI()
			{
				btnConfirm.setText("Save");
				btnConfirm.setStartColor(menuConfiguration.confirmButtonStartColor);
				btnConfirm.setEndColor(menuConfiguration.confirmButtonEndColor);
				btnConfirm.setClickColor(menuConfiguration.confirmButtonClickColor);
				btnConfirm.setHelpText("Save your game");

				lblSelectSaveFile = new JLabel("Name your save:");
				lblSelectSaveFile.setFont(new Font("Lucida Grande", Font.BOLD, 13));
				getSpringLayout().putConstraint(SpringLayout.NORTH, lblSelectSaveFile, 20, SpringLayout.NORTH, this);
				getSpringLayout().putConstraint(SpringLayout.WEST, lblSelectSaveFile, 20, SpringLayout.WEST, this);
				getSpringLayout().putConstraint(SpringLayout.EAST, lblSelectSaveFile, -20, SpringLayout.EAST, this);
				add(lblSelectSaveFile);

				saveField = new JTextField();
				getSpringLayout().putConstraint(SpringLayout.WEST, saveField, 0, SpringLayout.WEST, lblSelectSaveFile);
				getSpringLayout().putConstraint(SpringLayout.EAST, saveField, 0, SpringLayout.EAST, lblSelectSaveFile);
				getSpringLayout().putConstraint(SpringLayout.NORTH, saveField, 10, SpringLayout.SOUTH, lblSelectSaveFile);
				saveField.addKeyListener(new KeyAdapter()
				{
					@Override
					public void keyReleased(KeyEvent arg0)
					{
						if (saveField.getText().trim().length() == 0)
							btnConfirm.setEnabled(false);
						else
							btnConfirm.setEnabled(true);
					}

					@Override
					public void keyPressed(KeyEvent arg0)
					{
						if (arg0.getKeyCode() == 27)
							saveField.setText("");
						if (arg0.getKeyCode() == 10)
							btnConfirm.doClick();
					}
				});
				add(saveField);

				getSpringLayout().putConstraint(SpringLayout.NORTH, scrollPane, 10, SpringLayout.SOUTH, saveField);
				getSpringLayout().putConstraint(SpringLayout.WEST, scrollPane, 20, SpringLayout.WEST, this);
				getSpringLayout().putConstraint(SpringLayout.SOUTH, scrollPane, -10, SpringLayout.NORTH, btnConfirm);
				getSpringLayout().putConstraint(SpringLayout.EAST, scrollPane, -20, SpringLayout.EAST, this);
			}

			@Override
			protected void updateGUI()
			{
				if (this.saves.size() == 0)
				{
					scrollPane.setViewportView(noSaves);
				}
				else
				{
					JPanel savesListPanel = new JPanel();
					savesListPanel.setLayout(new GridBagLayout());
					for (final String key : saves.keySet())
						try
						{
							GridBagConstraints gbc = new GridBagConstraints();
							gbc.gridwidth = GridBagConstraints.REMAINDER;
							gbc.insets = new Insets(3, 0, 3, 0);
							final JPanel savePanel = new JPanel();
							savePanel.setLayout(null);
							savePanel.setPreferredSize(new Dimension(360, 85));
							savePanel.setBorder(BorderFactory.createLineBorder(Color.black));
							savePanel.addMouseListener(new MouseAdapter()
							{
								@Override
								public void mouseExited(MouseEvent paramMouseEvent)
								{
									savePanel.setBorder(BorderFactory.createLineBorder(Color.black));
								}

								@Override
								public void mouseEntered(MouseEvent paramMouseEvent)
								{
									savePanel.setBorder(BorderFactory.createMatteBorder(1, 5, 1, 1, Color.black));
								}

								public void mousePressed(MouseEvent arg0)
								{
									saveField.setText(key);
									btnConfirm.setEnabled(true);
								}
							});

							JLabel lblName = new JLabel(key);
							lblName.setFont(new Font(lblName.getFont().getName(), Font.BOLD, 14));
							lblName.setBounds(10, 0, 340, 30);
							savePanel.add(lblName);

							JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
							separator.setBounds(10, 25, 340, 30);
							savePanel.add(separator);

							JLabel lblSavedTitle = new JLabel("Last saved:");
							lblSavedTitle.setBounds(10, 25, 80, 30);
							savePanel.add(lblSavedTitle);

							JLabel lblSaved = new JLabel("" + saves.get(key).getSaved());
							lblSaved.setBounds(90, 25, 230, 30);
							savePanel.add(lblSaved);

							JLabel lblCreatedTitle = new JLabel("Created:");
							lblCreatedTitle.setBounds(10, 50, 80, 30);
							savePanel.add(lblCreatedTitle);

							JLabel lblCreated = new JLabel("" + saves.get(key).getDateCreated().getTime());
							lblCreated.setBounds(90, 50, 240, 30);
							savePanel.add(lblCreated);

							savesListPanel.add(savePanel, gbc);
						}
						catch (Exception e)
						{
							System.out.println("Conflict");
						}
					btnConfirm.setEnabled(false);
					scrollPane.setViewportView(savesListPanel);
				}
				if (saveField.getText().trim().length() == 0)
					btnConfirm.setEnabled(false);
			}
		}
	}
}