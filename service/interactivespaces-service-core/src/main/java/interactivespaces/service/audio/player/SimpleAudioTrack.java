/**
 *
 */
package interactivespaces.service.audio.player;

/**
 * Simple implementation of an {@link AudioTrackMetadata}.
 *
 * @author Keith M. Hughes
 */
public class SimpleAudioTrack implements AudioTrackMetadata {

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
   * Can be {@code null}.
   */
  private String artist;

  /**
   * Album the song is on.
   *
   * <p>
   * Can be {@code null}.
   */
  private String album;

  /**
   * Construct a track with all {@code null} fields.
   */
  public SimpleAudioTrack() {
  }

  /**
   * Construct a new audio track.
   *
   * @param id
   *          ID for the track
   * @param title
   *          the title, can be {@code null}
   * @param artist
   *          the artist, can be {@code null}
   * @param album
   *          the album, can be {@code null}
   */
  public SimpleAudioTrack(String id, String title, String artist, String album) {
    this.id = id;
    this.title = title;
    this.artist = artist;
    this.album = album;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public void setTitle(String title) {
    this.title = title;
  }

  @Override
  public String getArtist() {
    return artist;
  }

  @Override
  public void setArtist(String artist) {
    this.artist = artist;
  }

  @Override
  public String getAlbum() {
    return album;
  }

  @Override
  public void setAlbum(String album) {
    this.album = album;
  }

  @Override
  public String toString() {
    return "SimpleAudioTrack [id=" + id + ", title=" + title + ", artist=" + artist + ", album=" + album + "]";
  }
}
