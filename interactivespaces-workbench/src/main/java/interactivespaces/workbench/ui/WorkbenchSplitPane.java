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

import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.activity.ActivityProject;

import org.jdesktop.swingx.JXMultiSplitPane;
import org.jdesktop.swingx.MultiSplitLayout;
import org.jdesktop.swingx.MultiSplitLayout.Divider;
import org.jdesktop.swingx.MultiSplitLayout.Leaf;
import org.jdesktop.swingx.MultiSplitLayout.Split;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;

/**
 * A Split Pane UI for the Workbench UI.
 *
 * @author Keith M. Hughes
 */
public class WorkbenchSplitPane extends JXMultiSplitPane implements ActionListener {

  /**
   * The name of the file which will store the window layout.
   */
  public static final String LAYOUT_FILENAME = "layout.xml";

  /**
   * The workbench UI we are contained in.
   */
  private final WorkbenchUi workbenchUi;

  /**
   * Where the information windows go.
   */
  private JTabbedPane confPane;

  /**
   * Where the source windows go.
   */
  private JTabbedPane sourcePane;

  /**
   * Where the info windows go.
   */
  private JTabbedPane infoPane;

  /**
   * The source window manager to use for all source files.
   */
  private SourceWindowManager sourceWindowManager;

  /**
   * The project explorer.
   */
  private JFileSystemExplorerTree projectExplorerPane;

  /**
   * Construct a split pane window for the workbench UI.
   *
   * @param workbenchUi
   *          the UI to be embeded in the split pane
   */
  public WorkbenchSplitPane(WorkbenchUi workbenchUi) {
    this.workbenchUi = workbenchUi;

    init();
  }

  /**
   * initialize the split pane.
   */
  private void init() {
    setupLayout();

    confPane = new JTabbedPane();
    add(confPane, "conf");

    sourcePane = new JTabbedPane();
    add(sourcePane, "source");

    infoPane = new JTabbedPane();
    add(infoPane, "info");

    projectExplorerPane = new JFileSystemExplorerTree();
    JScrollPane projectExplorerScrollPane =
        new JScrollPane(projectExplorerPane, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    confPane.addTab("Project Explorer", projectExplorerScrollPane);

    sourceWindowManager =
        new BasicSourceWindowManager(workbenchUi, sourcePane, workbenchUi.getWorkbench().getProjectManager());
  }

  /**
   * Set up the layout of the split pane.
   */
  private void setupLayout() {
    Split col1 = new Split();
    col1.setRowLayout(false);
    col1.setWeight(0.75);

    Leaf source = new Leaf("source");
    source.setWeight(0.75);

    Leaf info = new Leaf("info");
    info.setWeight(0.25);

    col1.setChildren(source, new Divider(), info);

    Split row1 = new Split();
    row1.setRowLayout(true);
    Leaf conf = new Leaf("conf");
    conf.setWeight(0.25);

    row1.setChildren(conf, new Divider(), col1);

    MultiSplitLayout layout = new MultiSplitLayout(row1);
    setLayout(layout);

    // This means that the splits don't resize when content is put in them.
    layout.setFloatingDividers(false);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    // TODO Auto-generated method stub

  }

  /**
   * @return the sourceWindowManager
   */
  public SourceWindowManager getSourceWindowManager() {
    return sourceWindowManager;
  }

  /**
   * Set the new activity project.
   *
   * @param project
   *          the new project
   */
  public void setCurrentActivityProject(Project project) {
    // TODO(keith): make this work with all project types
    projectExplorerPane.buildTree((ActivityProject) project);
  }
}
