/**
 *
 */
package interactivespaces.master.server.remote;

/**
 * @author Keith M. Hughes
 */
public class RemoteMasterServerMessages {

  /**
   * The URI prefix for the handler for space controller operations in the Remote Master server.
   */
  public static final String URI_PREFIX_MASTER_SPACECONTROLLER = "/master/spacecontroller";

  /**
   * The space controller method on the Remote Master server for registering space controllers.
   */
  public static final String MASTER_SPACE_CONTROLLER_METHOD_REGISTER = "/register";

  /**
   * The field in the space controller registration method that provides the data for the registration.
   */
  public static final String MASTER_METHOD_FIELD_CONTROLLER_REGISTRATION_DATA = "data";

  /**
   * The failure response for a master method.
   */
  public static final String MASTER_METHOD_RESPONSE_FAILURE = "FAILURE";

  /**
   * The description field of the space controller registration.
   */
  public static final String CONTROLLER_REGISTRATION_DESCRIPTION = "description";

  /**
   * The name field of the space controller registration.
   */
  public static final String CONTROLLER_REGISTRATION_NAME = "name";

  /**
   * The host ID field of the space controller registration.
   */
  public static final String CONTROLLER_REGISTRATION_HOST_ID = "hostId";

  /**
   * The UUID field of the space controller registration.
   */
  public static final String CONTROLLER_REGISTRATION_UUID = "uuid";

  /**
   * The content type for responses from the remote master.
   */
  public static final String REMOTE_MASTER_RESPONSE_CONTENT_TYPE = "text/plain";

  /**
   * The success response for a space controller registration.
   */
  public static final String CONTROLLER_REGISTRATION_SUCCESS = "SUCCESS";

  /**
   * The failure response for a space controller registration.
   */
  public static final String CONTROLLER_REGISTRATION_FAILURE = "FAILURE";

  /**
   * Configuration property for the network host of the master.
   *
   */
  public static final String CONFIGURATION_MASTER_HOST = "interactivespaces.master.host";

  /**
   * Configuration property for the network port for master communications.
   *
   */
  public static final String CONFIGURATION_MASTER_COMMUNICATION_PORT = "interactivespaces.master.communication.port";

  /**
   * Default value for configuration property for the network port for master communications.
   */
  public static final int CONFIGURATION_MASTER_COMMUNICATION_PORT_DEFAULT = 8090;
}
