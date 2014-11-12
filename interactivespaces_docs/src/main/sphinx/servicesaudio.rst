The Interactive Spaces Core Audio Services
********

Audio Track Player Service
======================

The Audio Track Player Service allows you to easily play back audio from an Interactive Spaces activity.

The service currently only supports MP3 files.

To use the Audio Player Service, you must get an instance of a 
``AudioTrackPlayer``. These players use various system resources
that must be released when you are through with the player. One way of
handling this is to allocate a player in ``onActivitySetup()``
and add it as a Managed Resource for the Activity. Then Interactive Spaces
will automatically clean up any resources used by the player when your
activity stops running.

As an example, here is the first part of your Activity, showing the player
instance variable and the code to obtain a player. Notice the player is
added as a Managed Resource.

.. code-block:: java

  private AudioTrackPlayer audioTrackPlayer;

  @Override
  public void onActivitySetup() {
    AudioTrackPlayer audioTrackPlayerService =
        getSpaceEnvironment().getServiceRegistry().getRequiredService(
            AudioTrackPlayer.SERVICE_NAME);

    audioTrackPlayer = audioTrackPlayerService.newPlayer();

    addManagedResource(audioTrackPlayer);
  }

Playing an audio track is then quite easy. You create an instance of ``PlayableAudioTrack`` and hand it to the player.


.. code-block:: java

  PlayableAudioTrack track = new PlayableAudioTrack(getActivityFilesystem().getInstallFile("gong.mp3"));
  audioTrackPlayer.start(track);

The player can only play 1 track at a time.

If you want to stop playing the track that is current playing, use the ``stop()`` player method.

.. code-block:: java

  audioTrackPlayer.stop();

The audio track player lets you register listeners that will give you callbacks about when tracks start playing and when they stop.
As an example, the following will log when the player is playing.

.. code-block:: java

  audioTrackPlayer.addListener(new AudioTrackPlayerListener {

    @Override
    public void onAudioTrackStart(AudioTrackPlayer player, PlayableAudioTrack track) {
      getLog().info("Starting to play track " + track);
    }

    @Override
    public void onAudioTrackStop(AudioTrackPlayer player, PlayableAudioTrack track) {
      getLog().info("Track complete " + track);
    }
  });

Listeners can be used for tasks such as notifying a display when a song has started playing and when it has
stopped, or for controlling mood lighting.

The Audio Jukebox Service
=========================

The Audio Jukebox Service lets you easily create an audio jukebox. Jukeboxes can tell you all of the audio
tracks available in the jukebox, play any particular track, or do a shuffle play through all known tracks.

Speech Synthesis Service
===============

The Speech Synthesis Service allows your activities to speak. The service takes
a string of text which is then spoken by the computer which contains the
Space Controller running the service.

To use the Speech Synthesis Service, you must get an instance of a 
``SpeechSynthesisPlayer``. These players use various system resources
that must be released when you are through with the player. One way of
handling this is to allocate a player in ``onActivitySetup()``
and add it as a Managed Resource for the Activity. Then Interactive Spaces
will automatically clean up any resources used by the player when your
activity stops running.

As an example, here is the first part of your Activity, showing the player
instance variable and the code to obtain a player. Notice the player is
added as a Managed Resource.

.. code-block:: java

  private SpeechSynthesisPlayer speechPlayer;

  @Override
  public void onActivitySetup() {
    SpeechSynthesisService speechSynthesisService =
        getSpaceEnvironment().getServiceRegistry().getRequiredService(
            SpeechSynthesisService.SERVICE_NAME);

    speechPlayer = speechSynthesisService.newPlayer();

    addManagedResource(speechPlayer);
  }

Now making your activity speak is easy, you just use the ``speak`` method
on the player.

.. code-block:: java

  speechPlayer.speak("Hello, world.", true);

The second argument for the ``speak()`` method determines if the method will
block while the text is being spoken, or if it will return immediately
with the text spoken asynchronously. if the value is ``true`` the method
will block, if it is ``false`` the method will return immediately.

For more details about what you can do with the Speech Synthesis Service, see the
:javadoc:`interactivespaces.service.speech.synthesis.SpeechSynthesisService` 
Javadoc.

