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

import interactivespaces.workbench.project.activity.ActivityProject;

import java.io.File;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;

/**
 * A tree UI element for exploring a file system.
 * 
 * @author Keith M. Hughes
 */
public class JFileSystemExplorerTree extends JTree implements
		TreeSelectionListener {

	/**
	 * The root node for the tree of sections.
	 */
	private FileTreeNode rootNode;
	private DefaultTreeModel treeModel;

	public JFileSystemExplorerTree() {
		// projectManager.addProjectManagerListener(new
		// ProjectManagerListenerAdapter() {
		// @Override
		// public void closingProject(ProjectManager projectManager) {
		// buildTree(null);
		// }
		//
		// @Override
		// public void changingProject(ProjectManager projectManager) {
		// buildTree(projectManager.getCurrentProject());
		// }
		// });

		treeModel = new DefaultTreeModel(rootNode);
		setModel(treeModel);
		getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		addTreeSelectionListener(this);
		setRootVisible(false);

		setVisible(true);
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		FileTreeNode node = (FileTreeNode) getLastSelectedPathComponent();

		if (node == null)
			// Nothing is selected.
			return;

		File file = node.getFile();
		System.out.format("Selected %s\n", file);
		// ProjectSection section = (ProjectSection) nodeInfo;
		// if (section != null) {
		// ProjectGroup group = section.getGroups().get(0);
		// if (group != null)
		// projectManager.setCurrentGroup(group);
		// }
	}

	/**
	 * Clear out the old tree and build a new one based on the current project.
	 */
	public void buildTree(ActivityProject project) {
		if (project != null) {

			rootNode = new FileTreeNode(project.getActivitySourceFolder(), true, null);
			treeModel.setRoot(rootNode);
		} else {
			treeModel.setRoot(null);
			rootNode = null;		
		}

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				invalidate();
			}
		});
	}

	/**
	 * TreeNode for a file.
	 *
	 * @author Keith M. Hughes
	 */
	private static class FileTreeNode implements TreeNode {

		/**
		 * Node file.
		 */
		private File file;

		/**
		 * Children of the node file.
		 */
		private File[] children;

		/**
		 * Parent node.
		 */
		private TreeNode parent;

		/**
		 * {@code true} if a root file.
		 */
		private boolean root;

		/**
		 * @param file
		 *            file for the node
		 * @param root
		 *            {@code true} if a root file
		 * @param parent
		 *            node for the parent, can be {@code null}
		 */
		public FileTreeNode(File file, boolean root, TreeNode parent) {
			this.file = file;
			this.root = root;
			this.parent = parent;
			this.children = this.file.listFiles();
			if (this.children == null)
				this.children = new File[0];
		}

		/**
		 * Creates a new file tree node.
		 * 
		 * @param children
		 *            children files.
		 */
		public FileTreeNode(File[] children) {
			this.file = null;
			this.parent = null;
			this.children = children;
		}

		@Override
		public Enumeration<?> children() {
			final int elementCount = children != null ? children.length : 0;
			return new Enumeration<File>() {
				int count = 0;

				@Override
				public boolean hasMoreElements() {
					return this.count < elementCount;
				}

				@Override
				public File nextElement() {
					if (this.count < elementCount) {
						return FileTreeNode.this.children[this.count++];
					}
					throw new NoSuchElementException("Vector Enumeration");
				}
			};

		}

		@Override
		public boolean getAllowsChildren() {
			return true;
		}

		@Override
		public TreeNode getChildAt(int childIndex) {
			return new FileTreeNode(children[childIndex],
					parent == null, this);
		}

		@Override
		public int getChildCount() {
			return children != null ? children.length : 0;
		}

		@Override
		public int getIndex(TreeNode n) {
			FileTreeNode node = (FileTreeNode) n;
			for (int i = 0; i < children.length; i++) {
				if (node.file.equals(children[i]))
					return i;
			}
			return -1;
		}

		@Override
		public TreeNode getParent() {
			return parent;
		}

		@Override
		public boolean isLeaf() {
			return !file.isDirectory();
		}

		public File getFile() {
			return file;
		}

		@Override
		public String toString() {
			return file.getName();
		}
	}
}
