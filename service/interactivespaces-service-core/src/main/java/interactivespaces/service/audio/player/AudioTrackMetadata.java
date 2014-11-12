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

package interactivespaces.service.audio.player;

/**
 * The metadata for an audio track.
 *
 * @author Keith M. Hughes
 */
public interface AudioTrackMetadata {

  /**
   * Get the track ID.
   *
   * @return the track ID
   */
  String getId();

  /**
   * Set the track ID.
   *
   * @param id
   *          the id to set
   */
  void setId(String id);

  /**
   * Get the track title.
   *
   * @return the title, can be {@code null}
   */
  String getTitle();

  /**
   * Set the track title.
   *
   * @param title
   *          the title to set, can be {@code null}
   */
  void setTitle(String title);

  /**
   * Get the artist.
   *
   * @return the artist, can be {@code null}
   */
  String getArtist();

  /**
   * Set the artist.
   *
   * @param artist
   *          the artist to set, can be {@code null}
   */
  void setArtist(String artist);

  /**
   * Get the album.
   *
   * @return the album, can be {@code null}
   */
  String getAlbum();

  /**
   * Set the album.
   *
   * @param album
   *          the album to set, can be {@code null}
   */
  void setAlbum(String album);
}
