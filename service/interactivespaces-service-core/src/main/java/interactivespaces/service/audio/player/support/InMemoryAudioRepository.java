/**
 *
 */
package interactivespaces.service.audio.player.support;

import interactivespaces.service.audio.player.AudioRepository;
import interactivespaces.service.audio.player.AudioTrack;
import interactivespaces.service.audio.player.PlayableAudioTrack;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.Map;

/**
 * A base implementation of an in-memory repository for audio.
 *
 * <p>
 * The {@link #startup()} and {@link #shutdown()} do nothing.
 *
 * @author Keith M. Hughes
 */
public class InMemoryAudioRepository implements AudioRepository {

  /**
   * Map of track ID to playable tracks.
   */
  private final Map<String, PlayableAudioTrack> tracks = Maps.newHashMap();

  @Override
  public void startup() {
    onStartup();
  }

  /**
   * Handle any additional startup.
   *
   * <p>
   * This is meant to be overriden.
   */
  protected void onStartup() {
    // Default is do nothing.
  }

  @Override
  public void shutdown() {
    onShutdown();
  }

  /**
   * Handle any additional shutdown.
   *
   * <p>
   * This is meant to be overriden.
   */
  protected void onShutdown() {
    // Default is do nothing.
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
  public Collection<PlayableAudioTrack> getAllPlayableTracks() {
    return Lists.newArrayList(tracks.values());
  }

  /**
   * Add in a new track.
   *
   * @param track
   *        the track to add
   */
  @VisibleForTesting
  public void addTrack(PlayableAudioTrack track) {
    tracks.put(track.getTrack().getId(), track);
  }
}
