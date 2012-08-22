/*
 * Copyright (C) 2012 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package interactivespaces.workbench.ui;

import interactivespaces.workbench.InteractiveSpacesWorkbench;
import interactivespaces.workbench.activity.project.ActivityProject;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Event;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

/**
 * A Swing UI window for the Interactive Spaces workbench.
 * 
 * @author Keith M. Hughes
 */
public class WorkbenchUi extends JFrame implements ActionListener {

	/**
	 * Initial width of the UI window.
	 */
	public static final int APP_WIDTH_DEFAULT = 800;

	/**
	 * Initial height of the UI window.
	 */
	public static final int APP_HEIGHT_DEFAULT = 600;
	/**
	 * Menu bar associated with this desktop (if any)
	 */
	private JMenuBar menuBar;

	/**
	 * The File menu.
	 */
	private JMenu fileMenu;

	/**
	 * The Edit menu.
	 */
	private JMenu editMenu;
	private JMenu viewMenu;
	private JMenu runMenu;
	private JMenu advancedMenu;
	private JMenu windowMenu;
	private JMenu helpMenu;

	private JCheckBoxMenuItem viewProgrammer;
	private JCheckBoxMenuItem viewMessages;
	private JCheckBoxMenuItem stopActiveMenu;

	private JCheckBoxMenuItem runFullDebugMenuItem;

	private JMenuItem advancedServerStartMenuItem;

	private JMenuItem advancedServerStopMenuItem;

	/**
	 * Menu item for a New Project.
	 */
	private JMenuItem newProjectMenuItem;

	/**
	 * Menu item for a new source file.
	 */
	private JMenuItem newSourceMenuItem;

	private JMenuItem openMenuItem;

	private JMenuItem closeMenuItem;

	private JMenuItem saveMenuItem;

	private JMenuItem saveAsMenuItem;

	private JMenuItem saveAllMenuItem;

	private JMenuItem revertMenuItem;

	private JMenuItem importProjectMenuItem;

	private JMenuItem importWorldsMenuItem;

	private JMenuItem exportProjectMenuItem;

	private JMenuItem deleteMenuItem;

	private JMenuItem exitMenuItem;

	private JMenuItem showInitialWorldMenuItem;

	private JMenuItem runMenuItem;

	private JMenuItem stopMenuItem;

	private JMenuItem startMenuItem;

	private JMenuItem steptMenuItem;

	private JMenuItem changeRobotInfoMenuItem;

	private JMenu newMenu;

	/**
	 * The workbench being controlled.
	 */
	private InteractiveSpacesWorkbench workbench;

	public WorkbenchUi(InteractiveSpacesWorkbench workbench) {
		super("Interactive Spaces Workbench");

		this.workbench = workbench;

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// desktop.saveLayout();

				Window w = e.getWindow();
				w.setVisible(false);
				w.dispose();
				System.exit(0);
			}
		});
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		setJMenuBar(getJMenuBar());

		// Set our intial size to 600x400 pixels.
		Rectangle bounds = getBounds();
		bounds.height = APP_HEIGHT_DEFAULT;
		bounds.width = APP_WIDTH_DEFAULT;
		setBounds(bounds);

		// Show the entire app
		setVisible(true);
	}

	public JMenuBar getJMenuBar() {
		// Menu bar we're building for this app
		JMenuBar mb = new JMenuBar();

		// Get the hash table of actions from our editor
		// Map<String, Action> actions = programmerUi.getEditorActions();

		fileMenu = new JMenu("File");
		mb.add(fileMenu);

		newMenu = new JMenu("New");
		fileMenu.add(newMenu);

		newProjectMenuItem = new JMenuItem("New Java Project");
		newMenu.add(newProjectMenuItem);
		newProjectMenuItem.addActionListener(this);
		/*
		 * fileMenu.addSeparator();
		 * 
		 * JMenuItem openBuilderMenu = new JMenuItem("Open World Builder...");
		 * fileMenu.add(openBuilderMenu);
		 * openBuilderMenu.addActionListener(this);
		 * openBuilderMenu.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_O,
		 * Event.CTRL_MASK | Event.SHIFT_MASK, false));
		 * 
		 * JMenuItem createSimpleWorldMenu = new
		 * JMenuItem("Create Simple World");
		 * fileMenu.add(createSimpleWorldMenu);
		 * createSimpleWorldMenu.addActionListener(this);
		 * openBuilderMenu.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_W,
		 * Event.CTRL_MASK | Event.SHIFT_MASK, false));
		 */
		fileMenu.addSeparator();

		openMenuItem = new JMenuItem("Open...");
		fileMenu.add(openMenuItem);
		openMenuItem.addActionListener(this);
		openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				Event.CTRL_MASK, false));

		closeMenuItem = new JMenuItem("Close");
		fileMenu.add(closeMenuItem);
		closeMenuItem.addActionListener(this);
		closeMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
				Event.CTRL_MASK, false));

		saveMenuItem = new JMenuItem("Save");
		fileMenu.add(saveMenuItem);
		saveMenuItem.addActionListener(this);
		saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				Event.CTRL_MASK, false));
		saveMenuItem.setEnabled(true);

		// saveAsMenuItem = new JMenuItem("Save As...");
		// fileMenu.add(saveAsMenuItem);
		// saveAsMenuItem.addActionListener(this);
		// saveAsMenuItem.setEnabled(false);

		saveAllMenuItem = new JMenuItem("Save All");
		fileMenu.add(saveAllMenuItem);
		saveAllMenuItem.addActionListener(this);
		saveAllMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				Event.CTRL_MASK | Event.SHIFT_MASK, false));
		saveAllMenuItem.setEnabled(true);

		revertMenuItem = new JMenuItem("Revert");
		fileMenu.add(revertMenuItem);
		revertMenuItem.addActionListener(this);

		fileMenu.addSeparator();

		exitMenuItem = new JMenuItem("Exit");
		fileMenu.add(exitMenuItem);
		exitMenuItem.addActionListener(this);
		exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
				Event.CTRL_MASK, false));

		editMenu = new JMenu("Edit");
		mb.add(editMenu);

		// mi = new JMenuItem("Undo");
		// editMenu.add(mi);
		// mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
		// InputEvent.CTRL_MASK, false));
		// mi.setEnabled(false);
		//
		// editMenu.addSeparator();

		/*
		 * mi = editMenu.add(actions.get(DefaultEditorKit.cutAction));
		 * mi.setText("Cut"); if (accelerators)
		 * mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
		 * InputEvent.CTRL_MASK, false));
		 * 
		 * mi = editMenu.add(actions.get(DefaultEditorKit.copyAction));
		 * mi.setText("Copy"); if (accelerators)
		 * mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
		 * InputEvent.CTRL_MASK, false));
		 * 
		 * mi = editMenu.add(actions.get(DefaultEditorKit.pasteAction));
		 * mi.setText("Paste"); if (accelerators)
		 * mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
		 * InputEvent.CTRL_MASK, false));
		 * 
		 * editMenu.addSeparator();
		 * 
		 * mi = editMenu.add(actions.get(DefaultEditorKit.selectAllAction));
		 * mi.setText("Select All"); if (accelerators)
		 * mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
		 * InputEvent.CTRL_MASK, false));
		 */

		deleteMenuItem = new JMenuItem("Delete");
		editMenu.add(deleteMenuItem);
		deleteMenuItem.addActionListener(this);

		viewMenu = new JMenu("View");
		mb.add(viewMenu);

		changeRobotInfoMenuItem = new JMenuItem("Change Robot Info Panel...");
		changeRobotInfoMenuItem.addActionListener(this);
		viewMenu.add(changeRobotInfoMenuItem);

		viewMenu.addSeparator();

		showInitialWorldMenuItem = new JMenuItem("Show Initial World");
		showInitialWorldMenuItem.addActionListener(this);
		viewMenu.add(showInitialWorldMenuItem);
		showInitialWorldMenuItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_F1, Event.SHIFT_MASK, false));

		JMenuItem worldMenu = new JMenuItem("Worlds");
		worldMenu.addActionListener(this);
		viewMenu.add(worldMenu);

		runMenu = new JMenu("Run");
		mb.add(runMenu);

		helpMenu = new JMenu("Help");
		mb.add(helpMenu);

		JMenuItem aboutMenu = new JMenuItem("About RobotWorld...");
		helpMenu.add(aboutMenu);
		aboutMenu.addActionListener(this);

		menuBar = mb;
		return mb;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source.equals(openMenuItem)) {

			JFileChooser chooser = new JFileChooser();
			// Find out what file we get
			int state = chooser.showOpenDialog(this);
			File file = chooser.getSelectedFile();
			System.out.format("Got %s\n", file);

			ActivityProject project = workbench.readActivityProject(file
					.getParentFile());
			workbench.buildActivityProject(project);
		} else if (source.equals(exitMenuItem)) {
			// Get rid of the frame we live in. We want Exit to have the
			// same effect as closing the window.
			//
			// We want to just displatch a closing event because
			// otherwise the up mouse event from the menu item will no longer
			// have a window associated with it and we'll generate an error
			Toolkit.getDefaultToolkit()
					.getSystemEventQueue()
					.postEvent(
							new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
		}
	}
}
