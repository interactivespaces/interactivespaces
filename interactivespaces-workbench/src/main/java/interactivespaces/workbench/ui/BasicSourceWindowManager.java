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

import interactivespaces.workbench.activity.project.ActivityProjectManager;
import interactivespaces.workbench.activity.project.Source;

import java.awt.Component;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;

/**
 * A basic implementation of a {@link SourceWindowManager}.
 * 
 * @author Keith M. Hughes
 * @since Sep 21, 2012
 */
public class BasicSourceWindowManager implements SourceWindowManager {

	/**
	 * The name to give an untitled window.
	 */
	public static final String FILENAME_UNTITLED = "Untitled";

	/**
	 * Mapping from full file paths to the code editors for them.
	 */
	private BiMap<String, SourceEditor> filenameToEditor = HashBiMap.create();

	/**
	 * Mapping from full file paths to the code editors for them.
	 */
	private BiMap<JComponent, SourceEditor> componentToEditor = HashBiMap
			.create();

	/**
	 * The desktop where the windows reside.
	 */
	private JTabbedPane sourcePane;

	/**
	 * Code editor listeners that should be placed on all code editors.
	 */
	private List<SourceEditorListener> editorListeners = Lists.newArrayList();

	/**
	 * The activity project manager used for file operations.
	 */
	private ActivityProjectManager activityProjectManager;

	/**
	 * The user interface factory for UI elements.
	 */
	private UserInterfaceFactory userInterfaceFactory;
	
	/**
	 * The workbench UI we are part of.
	 */
	private WorkbenchUi workbenchUi;

	public BasicSourceWindowManager(WorkbenchUi workbenchUi,
			JTabbedPane sourcePane,
			ActivityProjectManager activityProjectManager) {
		this.workbenchUi = workbenchUi;
		this.activityProjectManager = activityProjectManager;
		this.sourcePane = sourcePane;

		addSourceEditorListener(new SourceEditorListener() {
			@Override
			public void contentModified(SourceEditor editor) {
				editorContentModified(editor);
			}
		});

		sourcePane.addChangeListener(new ChangeListener() {
			// This method is called whenever the selected tab changes
			public void stateChanged(ChangeEvent evt) {
				JTabbedPane pane = (JTabbedPane) evt.getSource();

				// Get current tab
				JComponent component = (JComponent) pane.getSelectedComponent();
				onSourceEditorWindowSelect(component);
			}
		});
	}

	@Override
	public void addNewSourceWindow(Source source) {
		// TODO(keith): Either support unnamed files (save before running
		// program), or remove this.
		String filePath = FILENAME_UNTITLED;
		String fileName = FILENAME_UNTITLED;

		String sourceFilePath = source.getPath();
		if (sourceFilePath != null) {
			filePath = sourceFilePath;
			fileName = source.getName();
		}

		SourceEditor editor = userInterfaceFactory.newSourceEditor(source);
		source.setAdapter(editor);
		editor.setContentModified(false);

		JComponent component = editor.getComponent();
		JScrollPane scrollPane = new JScrollPane(component);
		changeEditorTitle(editor, false);
		component.setVisible(true);

		filenameToEditor.put(filePath, editor);
		componentToEditor.put(scrollPane, editor);
		sourcePane.addTab(fileName, null, scrollPane, filePath);
		sourcePane.setSelectedComponent(scrollPane);

		for (SourceEditorListener editorListener : editorListeners)
			editor.addSourceEditorListener(editorListener);

		// TODO(keith): Potential race condition here
		editor.removeAllEdits();
	}

	@Override
	public void closeAllWindows() {
		sourcePane.removeAll();
		filenameToEditor.clear();
	}

	@Override
	public boolean areWindowsVisible() {
		// TODO(keith): Make work.
		return false;
	}

	@Override
	public void setWindowsVisible(boolean visible) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean hasModifiedWindows() {
		boolean hasModified = false;

		for (SourceEditor editor : filenameToEditor.values())
			hasModified |= editor.isContentModified();

		return hasModified;
	}

	@Override
	public void saveAll() {
		for (SourceEditor editor : filenameToEditor.values())
			saveWindow(editor);
	}

	@Override
	public void saveCurrentWindow() {
		SourceEditor editor = getCurrentEditor();
		if (editor != null) {
			saveWindow(editor);
		}
	}

	@Override
	public String getCurrentWindowPath() {
		SourceEditor editor = getCurrentEditor();
		if (editor != null) {
			return editor.getSource().getPath();
		}

		return null;
	}

	@Override
	public void removeSourceWindow(String sourceFilePath) {
		SourceEditor editor = filenameToEditor.get(sourceFilePath);
		if (editor != null) {
			JComponent component = editor.getComponent();
			sourcePane.remove(component);
			componentToEditor.remove(component);
		}
	}

	@Override
	public void revertCurrentWindow() {
		SourceEditor editor = getCurrentEditor();
		if (editor != null) {
			editor.revert();

			changeEditorTitle(editor, false);
		}
	}

	@Override
	public void undoEditCurrentWindow() {
		SourceEditor editor = getCurrentEditor();
		if (editor != null) {
			editor.undoEdit();

			// changeEditorTitle(editor, false);
		}
	}

	@Override
	public void redoEditCurrentWindow() {
		SourceEditor editor = getCurrentEditor();
		if (editor != null) {
			editor.redoEdit();

			// TODO(keith): Check if any more pending undos so can properly set
			// whether or not window needs to be saved.
		}
	}

	@Override
	public void addSourceEditorListener(SourceEditorListener editorListener) {
		editorListeners.add(editorListener);

		// Also add into any existing editors
		for (SourceEditor editor : filenameToEditor.values())
			editor.addSourceEditorListener(editorListener);
	}

	/**
	 * Set the read-only status of all source editing panes.
	 * 
	 * @param readOnly
	 *            {@code true} if the editors should be read-only
	 */
	private void setAllWindowsReadOnly(boolean readOnly) {
		for (SourceEditor editor : filenameToEditor.values())
			editor.setReadOnly(readOnly);
	}

	/**
	 * A code editor has been selected from the tabbed pane.
	 * 
	 * @param component
	 *            the selected editor's component
	 */
	private void onSourceEditorWindowSelect(JComponent component) {
		SourceEditor selected = componentToEditor.get(component);
		workbenchUi.onSourceEditorSelect(component.getActionMap());
	}

	/**
	 * Get the currently selected code editor.
	 * 
	 * @return the currently selected code editor or {@code null} if none
	 */
	private SourceEditor getCurrentEditor() {
		JComponent component = (JComponent) sourcePane.getSelectedComponent();
		if (component != null) {
			return componentToEditor.get(component);
		}

		return null;
	}

	/**
	 * Save the contents of a window.
	 * 
	 * @param editor
	 */
	private void saveWindow(SourceEditor editor) {
		editor.synchronizeToSource();
		Source source = editor.getSource();
		activityProjectManager.saveSource(source);
		editor.setContentModified(false);

		changeEditorTitle(editor, false);
	}

	/**
	 * An editor's content has been modified.
	 * 
	 * @param editor
	 */
	private void editorContentModified(SourceEditor editor) {
		if (!editor.isMarkedModified()) {
			editor.setMarkedModified(true);

			changeEditorTitle(editor, true);
		}
	}

	/**
	 * Change the title of a source editor.
	 * 
	 * @param editor
	 *            the editor to be modified.
	 * @param markAsModified
	 *            {@code true} if the editor should be marked as modified
	 */
	private void changeEditorTitle(SourceEditor editor, boolean markAsModified) {
		String newTitle = editor.getSource().getName();
		if (markAsModified)
			newTitle = "*" + newTitle;

		JComponent component = editor.getComponent();
		int index = 0;
		for (Component c : sourcePane.getComponents()) {
			if (c.equals(component)) {
				sourcePane.setTitleAt(index, newTitle);
				break;
			}

			index++;
		}
	}

	@Override
	public void setUserInterfaceFactory(
			UserInterfaceFactory userInterfaceFactory) {
		this.userInterfaceFactory = userInterfaceFactory;
	}

}
