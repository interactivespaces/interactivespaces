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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.configuration.Configuration;
import interactivespaces.service.audio.player.AudioRepository;
import interactivespaces.service.audio.player.AudioTrack;
import interactivespaces.service.audio.player.PlayableAudioTrack;

import org.farng.mp3.MP3File;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * An {@link AudioRepository} which just picks up all tracks from scanning
 * directories.
 *
 * @author Keith M. Hughes
 */
public class ScanningFileAudioRepository implements AudioRepository {

  /**
   * Configuration property giving the base directories of the music repository.
   */
  public static final String PROPERTY_MUSIC_REPOSITORY_BASE = "space.music.repository.base";

  /**
   * File extension for an MP3 file.
   */
  private static final String MUSIC_FILE_EXTENSION_MP3 = ".mp3";

  /**
   * The configuration for the repository.
   */
  private Configuration configuration;

  /**
   * Map of track ID to playable tracks.
   */
  private Map<String, PlayableAudioTrack> tracks;

  private Random random;

  @Override
  public void startup() {
    tracks = Maps.newHashMap();
    random = new Random(System.currentTimeMillis());

    scanRepositories();
  }

  @Override
  public void shutdown() {
    // Nothing to do.
  }

  @Override
  public void setConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }

  @Override
  public AudioTrack getTrackData(String id) {
    PlayableAudioTrack ptrack = getPlayableTrack(id);
    if (ptrack != null) {
      return ptrack.getTrack();
    } else {
      return null;
    }
  }

  @Override
  public PlayableAudioTrack getPlayableTrack(String id) {
    return tracks.get(id);
  }

  @Override
  public PlayableAudioTrack getRandomTrack(Collection<PlayableAudioTrack> tracksPlayed) {
    List<PlayableAudioTrack> tracksToChoose = getListOfAllTracks();
    tracksToChoose.removeAll(tracksPlayed);
    if (tracksToChoose.isEmpty()) {
      tracksPlayed.clear();
      tracksToChoose = getListOfAllTracks();
    }

    int trackNumber = random.nextInt(tracksToChoose.size());
    return tracksToChoose.get(trackNumber);
  }

  /**
   * Get a list of all tracks
   *
   * @return a list of all tracks in no particular order
   */
  private List<PlayableAudioTrack> getListOfAllTracks() {
    return Lists.newArrayList(tracks.values());
  }

  /**
   * v Scan all base repositories.
   */
  private void scanRepositories() {
    String baseRepositories =
        configuration.getRequiredPropertyString(PROPERTY_MUSIC_REPOSITORY_BASE);

    for (String baseRepository : baseRepositories.split("::")) {
      if (baseRepository.trim().isEmpty()) {
        continue;
      }

      File baseRepositoryFile = new File(baseRepository);
      if (baseRepositoryFile.exists()) {
        scanRepositoryDir(baseRepositoryFile);
      }
    }
  }

  /**
   * Scan the contents of the directory and all subdirectories for tracks.
   *
   * @param repositoryDir
   *          the reposiory directory to scan
   */
  private void scanRepositoryDir(File repositoryDir) {
    for (File file : repositoryDir.listFiles()) {
      if (file.isDirectory()) {
        scanRepositoryDir(file);
      } else if (isMusicFile(file)) {
        addMusicFile(file);
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
      throw new InteractiveSpacesException(String.format("Cannot read music file %s",
          file.getAbsolutePath()));
    }

    AudioTrack track = new AudioTrack();
    track.setId(generateTrackId(file));
    getMusicMetadata(file, track);

    PlayableAudioTrack playableTrack = new PlayableAudioTrack(track, file);
    tracks.put(track.getId(), playableTrack);
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
   *
   * @return the metadata for the music, if possible to get, or null if not.
   */
  private void getMusicMetadata(File file, AudioTrack track) {
    try {
      MP3File metadata = new MP3File(file);

      track.setTitle(metadata.getID3v2Tag().getSongTitle());
      track.setArtist(metadata.getID3v2Tag().getLeadArtist());
      track.setAlbum(metadata.getID3v2Tag().getAlbumTitle());
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
