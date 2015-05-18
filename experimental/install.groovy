package interactivespaces.service.comm.twitter.internal.twitter4j;

import interactivespaces.service.comm.twitter.TwitterConnection;
import interactivespaces.service.comm.twitter.TwitterConnectionListener;
import interactivespaces.system.StandaloneInteractiveSpacesEnvironment;
import interactivespaces.util.InteractiveSpacesUtilities;

public class Twitter4jTest {

  public static void main(String[] args) {
    StandaloneInteractiveSpacesEnvironment spaceEnvironment =
        StandaloneInteractiveSpacesEnvironment.newStandaloneInteractiveSpacesEnvironment();
    spaceEnvironment.getLog().error("Running");
    Twitter4jTwitterConnectionService service = new Twitter4jTwitterConnectionService();
    service.setSpaceEnvironment(spaceEnvironment);
    service.startup();

    String apiKey = "PNxjk53iymRKaARcMCJmw";
    String apiKeySecret = "zHzgyuWXJ6o3YPOGHiGFOTE50cPtUzboYmntqCSTak";
    String userAccessToken = null; // "467669332-pgRCqpHn1mRqcdgKHptAvnYsxAEkRAqIw5STIvvN";
    String userAccessTokenSecret = null ; //"xgKTesb6EhvErwPFoMqRS0n4knfdbFMHRIuJXfm1stU";
    TwitterConnection connection =
        service.newTwitterConnection(apiKey, apiKeySecret, userAccessToken, userAccessTokenSecret,
            spaceEnvironment.getLog());
    connection.addListener(new TwitterConnectionListener() {

      @Override
      public void onMessage(TwitterConnection connection, String query, String from, String message) {
        System.out.format("%s %s %s\n", query, from, message);
      }
    });

    connection.startup();
    connection.addUserSearch("alkalineskier", "2011-01-01");

    InteractiveSpacesUtilities.delay(1000L * 60 * 5);
    connection.shutdown();

    service.shutdown();
    spaceEnvironment.shutdown();

    System.out.println("Done");
  }

}
