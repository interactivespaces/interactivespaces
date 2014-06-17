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

package interactivespaces.service.audio.player.internal;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.service.audio.player.AudioRepository;
import interactivespaces.service.audio.player.AudioTrack;
import interactivespaces.service.audio.player.PlayableAudioTrack;
import interactivespaces.service.audio.player.SimpleAudioTrack;
import interactivespaces.service.audio.player.support.InMemoryAudioRepository;

import org.farng.mp3.MP3File;

import java.io.File;
import java.util.Collection;

/**
 * An {@link AudioRepository} which just picks up all tracks from scanning
 * directories.
 *
 * <p>
 * At the moment this only handles MP3 files.
 *
 * @author Keith M. Hughes
 */
public class ScanningFileAudioRepository extends InMemoryAudioRepository {

  /**
   * File extension for an MP3 file.
   */
  private static final String MUSIC_FILE_EXTENSION_MP3 = ".mp3";

  /**
   * The bases for the repositories of audio.
   */
  private Collection<File> repositoryBases;

  /**
   * Set all repository bases for this repository.
   *
   * @param repositoryBases
   *          all repository bases
   */
  public void setRepositoryBases(Collection<File> repositoryBases) {
    this.repositoryBases = repositoryBases;
  }

  @Override
  public void onStartup() {
    scanRepositories();
  }

  /**
   * Scan all base repositories.
   */
  private void scanRepositories() {
    for (File repositoryBase : repositoryBases) {
      if (repositoryBase.isDirectory()) {
        scanRepositoryDir(repositoryBase);
      }
    }
  }

  /**
   * Scan the contents of the directory and all subdirectories for tracks.
   *
   * @param repositoryDir
   *          the repository directory to scan
   */
  private void scanRepositoryDir(File repositoryDir) {
    File[] files = repositoryDir.listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.isDirectory()) {
          scanRepositoryDir(file);
        } else if (isMusicFile(file)) {
          addMusicFile(file);
        }
      }
    }
  }

  /**
   * Is the given file a music file?
   *
   * @param file
   *          the file to check
   *
   * @return {@code true} if the file is a music file.
   */
  private boolean isMusicFile(File file) {
    return file.getName().endsWith(MUSIC_FILE_EXTENSION_MP3);
  }

  /**
   * Add a music file to the repository.
   *
   * @param file
   *          the music file to add
   */
  private void addMusicFile(File file) {
    if (!file.canRead()) {
      throw new SimpleInteractiveSpacesException(String.format("Cannot read music file %s", file.getAbsolutePath()));
    }

    AudioTrack track = new SimpleAudioTrack();
    track.setId(generateTrackId(file));
    getMusicMetadata(file, track);

    addTrack(new PlayableAudioTrack(track, file));
  }

  /**
   * Generate a track ID from the track file.
   *
   * @param file
   *          the music file
   * @return the id for the track
   */
  private String generateTrackId(File file) {
    String fileName = file.getName();

    return fileName.substring(0, fileName.indexOf('.'));
  }

  /**
   * Get the metadata for the music file.
   *
   * @param file
   *          the music file to extract the data from
   * @param track
   *          the track object getting the metadata
   */
  private void getMusicMetadata(File file, AudioTrack track) {
    try {
      MP3File metadata = new MP3File(file);

      track.setTitle(metadata.getID3v2Tag().getSongTitle());
      track.setArtist(metadata.getID3v2Tag().getLeadArtist());
      track.setAlbum(metadata.getID3v2Tag().getAlbumTitle());
    } catch (Exception e) {
      throw new InteractiveSpacesException(String.format("Error while scanning audio file %s", file.getAbsolutePath()),
          e);
    }
  }
}
