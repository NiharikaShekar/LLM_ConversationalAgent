import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.slf4j.LoggerFactory
import scala.util.{Success, Try}

class ConfigurationManagerTest extends AnyFlatSpec with Matchers {

  // Test to check if configuration loads successfully
  "ConfigurationManager" should "load configuration successfully" in {
    // Loading the config using the loadConfig method
    val config = ConfigurationManager.loadConfig()

    config shouldBe a[Config]
  }

  // Test to verify retrieving an existing config key
  it should "retrieve existing configuration keys" in {
    // Trying to retrieve the server host configuration key
    val serverHost = Try(ConfigurationManager.getConfig("server.host"))

    serverHost.isSuccess shouldBe true
  }

  // Test to ensure exception is thrown for a non-existent config key
  it should "throw an exception for non-existent configuration keys" in {
    // Trying to retrieve a non-existent configuration key
    val thrown = the[Exception] thrownBy {
      ConfigurationManager.getConfig("non.existent.key")
    }

    thrown shouldBe an[Exception]
  }

  // Test to verify if the logger is properly configured
  it should "have a properly configured logger" in {
    // Getting the logger for the ConfigurationManager class
    val logger = LoggerFactory.getLogger(ConfigurationManager.getClass)

    logger shouldNot be(null)
  }
}
