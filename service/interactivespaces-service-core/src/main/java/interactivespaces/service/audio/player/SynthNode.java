package interactivespaces.service.audio.player;

import javax.sound.midi.Instrument;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;

public class SynthNode {
  private static boolean DEBUG = true;

  public static void main(String[] args) {
    /** The MIDI channel to use for playing the note. */
    int nChannelNumber = 0;
    int nNoteNumber = 0; // MIDI key number
    int nVelocity = 0;

    /*
     * Time between note on and note off event in milliseconds. Note that on
     * most systems, the best resolution you can expect are 10 ms.
     */
    int nDuration = 0;
    int nNoteNumberArgIndex = 0;
    switch (args.length) {
      case 4:
        nChannelNumber = Integer.parseInt(args[0]) - 1;
        nChannelNumber = Math.min(15, Math.max(0, nChannelNumber));
        nNoteNumberArgIndex = 1;
        // FALL THROUGH

      case 3:
        nNoteNumber = Integer.parseInt(args[nNoteNumberArgIndex]);
        nNoteNumber = Math.min(127, Math.max(0, nNoteNumber));
        nVelocity = Integer.parseInt(args[nNoteNumberArgIndex + 1]);
        nVelocity = Math.min(127, Math.max(0, nVelocity));
        nDuration = Integer.parseInt(args[nNoteNumberArgIndex + 2]);
        nDuration = Math.max(0, nDuration);
        break;

      default:
        printUsageAndExit();
    }

    /*
     * We need a synthesizer to play the note on. Here, we simply request the
     * default synthesizer.
     */
    Synthesizer synth = null;
    try {
      synth = MidiSystem.getSynthesizer();
      for (Instrument i : synth.getAvailableInstruments()) {
        System.out.println(i);
      }
    } catch (MidiUnavailableException e) {
      e.printStackTrace();
      System.exit(1);
    }
    if (DEBUG)
      out("Synthesizer: " + synth);

    /*
     * Of course, we have to open the synthesizer to produce any sound for us.
     */
    try {
      synth.open();
    } catch (MidiUnavailableException e) {
      e.printStackTrace();
      System.exit(1);
    }

    /*
     * Turn the note on on MIDI channel 1. (Index zero means MIDI channel 1)
     */
    MidiChannel[] channels = synth.getChannels();
    MidiChannel channel = channels[nChannelNumber];
    if (DEBUG)
      out("MidiChannel: " + channel);
    channel.noteOn(nNoteNumber, nVelocity);

    /*
     * Wait for the specified amount of time (the duration of the note).
     */
    try {
      Thread.sleep(nDuration);
    } catch (InterruptedException e) {
    }

    /*
     * Turn the note off.
     */
    channel.noteOff(nNoteNumber);

    /*
     * Close the synthesizer.
     */
    synth.close();
  }

  private static void printUsageAndExit() {
    out("SynthNote: usage:");
    out("java SynthNote [<channel>] <note_number> <velocity> <duration>");
    System.exit(1);
  }

  private static void out(String strMessage) {
    System.out.println(strMessage);
  }
}