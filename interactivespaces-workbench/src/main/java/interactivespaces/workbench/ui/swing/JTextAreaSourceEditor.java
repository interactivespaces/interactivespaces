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

package interactivespaces.workbench.ui.swing;

import interactivespaces.workbench.activity.project.Source;
import interactivespaces.workbench.ui.SourceEditor;
import interactivespaces.workbench.ui.SourceEditorListener;

import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.google.common.collect.Lists;

/**
 * A {@link SourceEditor} using a {@link JTextArea}
 *
 * @author Keith M. Hughes
 * @since Sep 20, 2012
 */
public class JTextAreaSourceEditor implements SourceEditor {
	
	/**
	 * The text area to use.
	 */
	private JTextArea textArea;
	
	/**
	 * The source being edited.
	 */
	private Source source;
	
	/**
	 * Everyone who wants to know when this code was edited.
	 */
	private List<SourceEditorListener> editorListeners = Lists.newArrayList();

	public JTextAreaSourceEditor(Source source) {
		this.source = source;
		
		textArea = new JTextArea();
		
		textArea.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				contentChanged();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				contentChanged();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				contentChanged();
			} 
		});
	}

	@Override
	public Source getSource() {
		return source;
	}

	@Override
	public void setContent(String content) {
		textArea.setText(content);
	}

	@Override
	public String getContent() {
		return textArea.getText();
	}

	@Override
	public boolean isContentModified() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setContentModified(boolean modified) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public JComponent getComponent() {
		return textArea;
	}

	@Override
	public void selectLine(long line) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clearSelection() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void revert() {
		setContent(source.getContent());
		
		textArea.repaint();
	}

	@Override
	public void addSourceEditorListener(SourceEditorListener listener) {
		editorListeners.add(listener);
	}

	@Override
	public boolean isMarkedModified() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setMarkedModified(boolean markedModified) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		textArea.setEditable(readOnly);
	}

	@Override
	public boolean isReadOnly() {
		return textArea.isEditable();
	}

	@Override
	public void synchronizeToSource() {
		source.setContent(getContent());
	}

	/**
	 * Something about the content changed.
	 */
	private void contentChanged() {
		for (SourceEditorListener listener : editorListeners)
			listener.contentModified(this);
	}	
}
