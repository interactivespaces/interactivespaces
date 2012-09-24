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

import javax.swing.JComponent;

import interactivespaces.workbench.activity.project.SourceAdapter;

/**
 * An editor for code.
 * 
 * @author Keith M. Hughes
 */
public interface SourceEditor extends SourceAdapter {

	/**
	 * Get the actual code editing UI component.
	 * 
	 * @return The actual code editing UI component.
	 */
	JComponent getComponent();

	/**
	 * Select the specified line in the editor.
	 * 
	 * @param line
	 */
	void selectLine(long line);

	/**
	 * Clear any selection in the editor.
	 */
	void clearSelection();

	/**
	 * Drop any edits and go back to what is stored in the program source.
	 */
	void revert();

	/**
	 * Add in a listener for code editor events.
	 * 
	 * @param listener
	 *            the listener to add.
	 */
	void addSourceEditorListener(SourceEditorListener listener);

	/**
	 * Is the editor marked as modified?
	 * 
	 * @return {@code true} if the editor is marked as modified
	 */
	boolean isMarkedModified();

	/**
	 * Set whether the editor is marked as modified.
	 * 
	 * @return {@code true} if the editor is marked as modified
	 */
	void setMarkedModified(boolean markedModified);

	/**
	 * Set whether the code editor is read only or not.
	 * 
	 * @param readOnly
	 *            {@code true} if the editor should be read only, false
	 *            otherwise.
	 */
	void setReadOnly(boolean readOnly);

	/**
	 * Is the code editor read only?
	 * 
	 * @return {@code true} if the editor is read only, false otherwise.
	 */
	boolean isReadOnly();
	
	/**
	 * If possible, undo the last edit.
	 */
	void undoEdit();
	
	/**
	 * If possible, redo the last edit.
	 */
	void redoEdit();
	
	/**
	 * Remove all edits from the editor. This will remove any pending
	 * undo and redo events.
	 */
	void removeAllEdits();
}
