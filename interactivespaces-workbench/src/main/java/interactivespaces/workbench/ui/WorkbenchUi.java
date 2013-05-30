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
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.ui.wizard.JWizardDialog;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Event;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultEditorKit;

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
	 * The desktop that will be used.
	 */
	private WorkbenchSplitPane desktop;

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
	private JMenu helpMenu;

	/**
	 * Menu item for a New Project.
	 */
	private JMenuItem newProjectMenuItem;

	private JMenuItem openMenuItem;

	private JMenuItem closeMenuItem;

	private JMenuItem saveMenuItem;

	private JMenuItem saveAllMenuItem;

	private JMenuItem revertMenuItem;

	/**
	 * menu item for the Edit Undo operation.
	 */
	private JMenuItem undoMenuItem;

	/**
	 * menu item for the Edit Redo operation.
	 */
	private JMenuItem redoMenuItem;

	/**
	 * menu item for the Edit Copy operation.
	 */
	private JMenuItem copyMenuItem;

	/**
	 * menu item for the Edit Cut operation.
	 */
	private JMenuItem cutMenuItem;

	/**
	 * menu item for the Edit Paste operation.
	 */
	private JMenuItem pasteMenuItem;

	/**
	 * Menu item for the Edit Select All operation.
	 */
	private JMenuItem selectAllMenuItem;

	private JMenuItem exitMenuItem;

	private JMenu newMenu;

	/**
	 * Menu item for the Project menu's Open Activity Conf.
	 */
	private JMenuItem openActivityConfMenuItem;

	/**
	 * Menu item for the Project menu's Build activity.
	 */
	private JMenuItem buildActivityMenuItem;

	/**
	 * Action for starting an activity up.
	 */
	private Action activityStartupAction;

	/**
	 * Action for activating an activity.
	 */
	private Action activityActivateAction;

	/**
	 * Action for deactivating an activity.
	 */
	private Action activityDeactivateAction;

	/**
	 * Action for shutting an activity down.
	 */
	private Action activityShutdownAction;

	/**
	 * The workbench being controlled.
	 */
	private InteractiveSpacesWorkbench workbench;

	/**
	 * The project this UI is working with.
	 */
	private Project currentProject;

	private JToolBar toolbar;

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

		desktop = new WorkbenchSplitPane(this);
		desktop.getSourceWindowManager().setUserInterfaceFactory(
				workbench.getUserInterfaceFactory());

		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		toolbar = new JToolBar();
		toolbar.setFloatable(false);
		contentPane.add(toolbar, BorderLayout.NORTH);

		contentPane.add(desktop, BorderLayout.CENTER);

		setJMenuBar(getJMenuBar());

		// Set our initial size to 600x400 pixels.
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

		newProjectMenuItem = new JMenuItem("New Project");
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

		undoMenuItem = new JMenuItem("Undo");
		editMenu.add(undoMenuItem);
		undoMenuItem.addActionListener(this);
		undoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
				InputEvent.CTRL_MASK, false));
		undoMenuItem.setEnabled(true);

		redoMenuItem = new JMenuItem("Redo");
		editMenu.add(redoMenuItem);
		redoMenuItem.addActionListener(this);
		redoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y,
				InputEvent.CTRL_MASK, false));
		redoMenuItem.setEnabled(true);

		editMenu.addSeparator();

		ActionMap defaultActionMap = new ActionMap();
		DefaultEditorKit kit = new DefaultEditorKit();
		for (Action action : kit.getActions()) {
			defaultActionMap.put(action.getValue(Action.NAME), action);
		}

		cutMenuItem = new JMenuItem(
				defaultActionMap.get(DefaultEditorKit.cutAction));
		cutMenuItem.setText("Cut");
		editMenu.add(cutMenuItem);

		copyMenuItem = new JMenuItem(
				defaultActionMap.get(DefaultEditorKit.copyAction));
		cutMenuItem.setText("Copy");
		editMenu.add(copyMenuItem);

		pasteMenuItem = new JMenuItem(
				defaultActionMap.get(DefaultEditorKit.pasteAction));
		cutMenuItem.setText("Paste");
		editMenu.add(pasteMenuItem);

		editMenu.addSeparator();

		selectAllMenuItem = new JMenuItem(
				defaultActionMap.get(DefaultEditorKit.selectAllAction));
		cutMenuItem.setText("Select All");
		editMenu.add(selectAllMenuItem);

		viewMenu = new JMenu("View");
		mb.add(viewMenu);

		viewMenu.addSeparator();

		JMenu projectMenu = new JMenu("Project");
		mb.add(projectMenu);

		openActivityConfMenuItem = new JMenuItem("Open Activity Conf");
		openActivityConfMenuItem.addActionListener(this);
		projectMenu.add(openActivityConfMenuItem);

		buildActivityMenuItem = new JMenuItem("Build");
		buildActivityMenuItem.addActionListener(this);
		projectMenu.add(buildActivityMenuItem);

		JMenu runMenu = new JMenu("Run");
		mb.add(runMenu);

		activityStartupAction = new WorkbenchAction("Startup", null,
				"Startup the activity", new Integer(KeyEvent.VK_L)) {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Starting up activity");
			}
		};
		activityActivateAction = new WorkbenchAction("Activate", null,
				"Activate the activity", new Integer(KeyEvent.VK_L)) {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Activating activity");
			}
		};
		activityDeactivateAction = new WorkbenchAction("Deactivate", null,
				"Deactivate the activity", new Integer(KeyEvent.VK_L)) {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Deactivating activity");
			}
		};
		activityShutdownAction = new WorkbenchAction("Shutdown", null,
				"Shut the activity down", new Integer(KeyEvent.VK_L)) {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Shutting down activity");
			}
		};

		JMenuItem activityStartupMenuItem = new JMenuItem(activityStartupAction);
		runMenu.add(activityStartupMenuItem);
		toolbar.add(activityStartupAction).setText("St");

		JMenuItem activityActivateMenuItem = new JMenuItem(
				activityActivateAction);
		runMenu.add(activityActivateMenuItem);
		toolbar.add(activityActivateAction).setText("Ac");

		JMenuItem activityDeactivateMenuItem = new JMenuItem(
				activityDeactivateAction);
		runMenu.add(activityDeactivateMenuItem);
		toolbar.add(activityDeactivateAction).setText("De");

		JMenuItem activityShutdownMenuItem = new JMenuItem(
				activityShutdownAction);
		runMenu.add(activityShutdownMenuItem);
		toolbar.add(activityShutdownAction).setText("Sh");

		helpMenu = new JMenu("Help");
		mb.add(helpMenu);

		JMenuItem aboutMenu = new JMenuItem(
				"About Interactive Spaces Workbench...");
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
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int state = chooser.showOpenDialog(this);

			if (state == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();

				openActivityProject(file);
			}
		} else if (source.equals(newProjectMenuItem)) {
			JWizardDialog wizard = new JWizardDialog("New Project", this,
					new NewProjectWizard(this, workbench));
			wizard.setLocationRelativeTo(this);
			wizard.setVisible(true);
		} else if (source.equals(exitMenuItem)) {
			// Get rid of the frame we live in. We want Exit to have the
			// same effect as closing the window.
			//
			// We want to just dispatch a closing event because
			// otherwise the up mouse event from the menu item will no longer
			// have a window associated with it and we'll generate an error
			Toolkit.getDefaultToolkit()
					.getSystemEventQueue()
					.postEvent(
							new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
		} else if (source.equals(saveMenuItem)) {
			desktop.getSourceWindowManager().saveCurrentWindow();
		} else if (source.equals(saveAllMenuItem)) {
			desktop.getSourceWindowManager().saveAll();
		} else if (source.equals(revertMenuItem)) {
			desktop.getSourceWindowManager().revertCurrentWindow();
		} else if (source.equals(undoMenuItem)) {
			desktop.getSourceWindowManager().undoEditCurrentWindow();
		} else if (source.equals(redoMenuItem)) {
			desktop.getSourceWindowManager().redoEditCurrentWindow();
		} else if (source.equals(buildActivityMenuItem)) {
			workbench.buildProject(currentProject);
		} else if (source.equals(openActivityConfMenuItem)) {
			desktop.getSourceWindowManager().addNewSourceWindow(
					workbench.getProjectManager()
							.getActivityConfSource(currentProject));
		}
	}

	/**
	 * Open up an activity project, if there is one to open.
	 * 
	 * @param baseProjectDir
	 *            the base directory for the project
	 */
	private void openActivityProject(File baseProjectDir) {
		// TODO(keith): If project already open should open up new workbench UI.
		if (workbench.getProjectManager().isProjectFolder(
				baseProjectDir)) {
			setCurrentProject(workbench.getProjectManager()
					.readProject(baseProjectDir));
		} else {
			JOptionPane.showMessageDialog(this,
					"The folder is not an Activity project folder",
					"Not Activity Project Folder", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Set the current project.
	 * 
	 * @param project
	 *            the project to make the current project
	 */
	public void setCurrentProject(Project project) {
		currentProject = project;
		setTitle("Interactive Spaces - " + currentProject.getName());
		desktop.setCurrentActivityProject(project);
	}

	/**
	 * @return the workbench
	 */
	public InteractiveSpacesWorkbench getWorkbench() {
		return workbench;
	}

	/**
	 * A source editor has been selected. Get its action map and set up menu
	 * items.
	 * 
	 * @param map
	 *            the map of actions to attach to the menu items
	 */
	public void onSourceEditorSelect(ActionMap map) {
		copyMenuItem.setAction(map.get(DefaultEditorKit.copyAction));
		cutMenuItem.setAction(map.get(DefaultEditorKit.cutAction));
		pasteMenuItem.setAction(map.get(DefaultEditorKit.pasteAction));
		selectAllMenuItem.setAction(map.get(DefaultEditorKit.selectAllAction));
	}

	/**
	 * An {@link Action} for workbench operations.
	 * 
	 * @author Keith M. Hughes
	 */
	private abstract class WorkbenchAction extends AbstractAction {
		public WorkbenchAction(String text, ImageIcon icon, String desc,
				Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}
	}
}
