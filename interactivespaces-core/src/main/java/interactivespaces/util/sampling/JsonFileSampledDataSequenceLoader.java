/*
 * Copyright (C) 2014 Google Inc.
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

package interactivespaces.util.sampling;

import interactivespaces.util.data.json.JsonBuilder;
import interactivespaces.util.data.json.JsonMapper;
import interactivespaces.util.data.json.JsonNavigator;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;
import interactivespaces.util.sampling.SampledDataSequence.SampledDataFrame;

import java.io.File;
import java.util.List;

/**
 * A sampled data sequence loader which uses a JSON file for storage.
 *
 * @author Keith M. Hughes
 */
public class JsonFileSampledDataSequenceLoader implements SampledDataSequenceLoader {

  /**
   * The frame object array in the outer object.
   */
  public static final String PROPERTY_NAME_FRAMES = "frames";

  /**
   * The name of a frame source in a frame object.
   */
  public static final String PROPERTY_NAME_FRAME_SOURCE = "source";

  /**
   * The timestamp in a frame object.
   */
  public static final String PROPERTY_NAME_FRAME_TIMESTAMP = "timestamp";

  /**
   * The samples array in a frame object.
   */
  public static final String PROPERTY_NAME_FRAME_SAMPLES = "samples";

  /**
   * The JSON mapper to use for serializing and deserializing the data.
   */
  private static final JsonMapper JSON_MAPPER = new JsonMapper();

  /**
   * The file support for the object.
   */
  private final FileSupport fileSupport = FileSupportImpl.INSTANCE;

  @Override
  public void save(File dataFile, SampledDataSequence dataSequence) {
    JsonBuilder builder = new JsonBuilder();

    builder.newArray(PROPERTY_NAME_FRAMES);

    List<SampledDataFrame> frames = dataSequence.getFrames();
    for (int i = 0; i < frames.size(); i++) {
      SampledDataFrame frame = frames.get(i);
      builder.newObject();

      builder.put(PROPERTY_NAME_FRAME_SOURCE, frame.getSource());
      builder.put(PROPERTY_NAME_FRAME_TIMESTAMP, Long.toString(frame.getTimestamp()));

      builder.newArray(PROPERTY_NAME_FRAME_SAMPLES);

      for (int sample : frame.getSamples()) {
        builder.put(sample);
      }

      // From array of samples
      builder.up();

      // From object
      builder.up();
    }

    fileSupport.writeFile(dataFile, JSON_MAPPER.toString(builder.build()));
  }

  @Override
  public int load(File dataFile, SampledDataSequence dataSequence) {
    dataSequence.reset();

    JsonNavigator nav = new JsonNavigator(JSON_MAPPER.parseObject(fileSupport.readFile(dataFile)));

    nav.down(PROPERTY_NAME_FRAMES);
    int numberFrames = nav.getSize();
    for (int frame = 0; frame < numberFrames; frame++) {
      nav.down(frame);

      String source = nav.getString(PROPERTY_NAME_FRAME_SOURCE);
      long timestamp = Long.parseLong(nav.getString(PROPERTY_NAME_FRAME_TIMESTAMP));

      nav.down(PROPERTY_NAME_FRAME_SAMPLES);

      int numberSamples = nav.getSize();
      int[] samples = new int[numberSamples];
      for (int j = 0; j < numberSamples; j++) {
        samples[j] = nav.getInteger(j);
      }

      nav.up();

      dataSequence.addFrame(source, samples, timestamp);

      nav.up();
    }

    return numberFrames;
  }
}
