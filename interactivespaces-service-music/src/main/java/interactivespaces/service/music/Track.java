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

package interactivespaces.service.music;

/**
 * The metadata for a song.
 * 
 * @author Keith M. Hughes
 */
public class Track {
	/**
	 * The track ID.
	 */
	private String id;
	
	/**
	 * Title of the song.
	 */
	private String title;

	/**
	 * Artist of the song.
	 * 
	 * <p>
	 * Can be null.
	 */
	private String artist;

	/**
	 * Album the song is on.
	 * 
	 * <p>
	 * Can be null.
	 */
	private String album;

	public Track() {
	}

	/**
	 * 
	 * @param title
	 *            the title, can be {@code null}
	 * @param artist
	 *            the artist, can be {@code null}
	 * @param album
	 *            the album, can be {@code null}
	 */
	public Track(String id, String title, String artist, String album) {
		this.id = id;
		this.title = title;
		this.artist = artist;
		this.album = album;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the title, can be {@code null}
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title
	 *            the title to set, can be {@code null}
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the artist, can be {@code null}
	 */
	public String getArtist() {
		return artist;
	}

	/**
	 * @param artist
	 *            the artist to set, can be {@code null}
	 */
	public void setArtist(String artist) {
		this.artist = artist;
	}

	/**
	 * @return the album, can be {@code null}
	 */
	public String getAlbum() {
		return album;
	}

	/**
	 * @param album
	 *            the album to set, can be {@code null}
	 */
	public void setAlbum(String album) {
		this.album = album;
	}

	@Override
	public String toString() {
		return "Track [id=" + id + ", title=" + title + ", artist=" + artist
				+ ", album=" + album + "]";
	}
}
