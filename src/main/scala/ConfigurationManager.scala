import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.LoggerFactory

object ConfigurationManager {
  // Initializing the logger
  private val logger = LoggerFactory.getLogger(getClass)

  // Loading the configuration file
  val config: Config = ConfigFactory.load()

  def loadConfig(): Config = {
    logger.info("Loading configuration...")
    config
  }

  /**
   * This function is retrieving a configuration value based on the provided key.
   * @param key The key to look up in the configuration file.
   * @return The configuration value as a String.
   */
  def getConfig(key: String): String = {
    try {
      // Retrieving the configuration value
      val value = config.getString(key)
      logger.info(s"Retrieved config for key: $key")
      value
    } catch {
      case e: Exception =>
        // Logging any errors in retrieving the config value
        logger.error(s"Error retrieving config for key: $key", e)
        throw e
    }
  }
}
