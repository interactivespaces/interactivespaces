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

package interactivespaces.service.script;

import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;

import java.io.File;

/**
 * A file -based script source.
 *
 * <p>
 * This will always check to see if the contents of the script has changed. if not, it will not change.
 *
 * @author Keith M. Hughes
 */
public class FileScriptSource implements ScriptSource {

  /**
   * The source file for the script.
   */
  private File sourceFile;

  /**
   * Last time the script was modified.
   */
  private long lastModified = 0;

  /**
   * The content of the script.
   */
  private String content;

  /**
   * The file support to use.
   */
  private FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * Construct a new script source.
   *
   * @param sourceFile
   *          the source file
   */
  public FileScriptSource(File sourceFile) {
    this.sourceFile = sourceFile;
  }

  @Override
  public String getScriptContents() {
    if (isModified()) {
      lastModified = sourceFile.lastModified();

      content = fileSupport.readFile(sourceFile);
    }

    return content;
  }

  @Override
  public boolean isModified() {
    return sourceFile.lastModified() != lastModified;
  }
}
