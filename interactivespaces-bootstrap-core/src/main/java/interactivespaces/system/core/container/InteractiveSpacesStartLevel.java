package interactivespaces.system.core.container;

/**
 */
public enum InteractiveSpacesStartLevel {

  /**
   * The default OSGi startup level for bundles.
   */
  STARTUP_LEVEL_DEFAULT(1),

  /**
   * The OSGi startup level for bundles which should start after most bundles
   * but not the ones which really require everything running.
   */
  STARTUP_LEVEL_PENULTIMATE(4),

  /**
   * The OSGi startup level for bundles which should start after everything else
   * is started.
   */
  STARTUP_LEVEL_LAST(5);

  /**
   * Integer value for this start level.
   */
  private int startLevel;

  /**
   * Create a new start level enum with the given level.
   *
   * @param startLevel start level for entry
   */
  InteractiveSpacesStartLevel(int startLevel) {
    this.startLevel = startLevel;
  }

  /**
   * Get the integral start level value.
   *
   * @return start level value
   */
  public int getStartLevel() {
    return startLevel;
  }
}
