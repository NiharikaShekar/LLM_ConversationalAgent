import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import java.io.File

class YAMLLoggerTest extends AnyFlatSpec with Matchers {

  // Test for creating an empty mutable result list
  "YAML_Helper" should "create a mutable result list" in {
    // Creating a mutable list to hold results
    val results = YAML_Helper.createMutableResult()

    // Checking that the list is initially empty
    results shouldBe empty
  }

  // Test for appending results to the list
  it should "append results to the list" in {
    // Creating an empty result list
    val results = YAML_Helper.createMutableResult()

    // Appending a result to the list
    YAML_Helper.appendResult(results, 0, "Test Question", "LLM Response", "Ollama Response")

    // Verifying that the list now contains one result
    results.size shouldBe 1
    results.head.iteration shouldBe "Itr-0"  // Checking iteration label
    results.head.question shouldBe "Test Question"  // Checking the question value
  }

  // Test for saving results to a YAML file
  it should "save results to a YAML file" in {
    // Creating a result list and adding a result
    val results = YAML_Helper.createMutableResult()
    YAML_Helper.appendResult(results, 0, "Test Question", "LLM Response", "Ollama Response")

    // Saving the results to a YAML file
    YAML_Helper.save(results)

    // Verifying that the directory where the file should be saved exists
    val file = new File("src/main/resources/Results_Conversation")
    file.exists() shouldBe true

    // Checking that at least one file has been saved in the directory
    file.listFiles().length should be > 0
  }

  // Test for creating a file with a timestamp in the filename
  it should "create a file with a timestamp in the filename" in {
    // Creating a result list and adding a result
    val results = YAML_Helper.createMutableResult()
    YAML_Helper.appendResult(results, 0, "Test Question", "LLM Response", "Ollama Response")

    // Saving the results, which should create a file with a timestamp
    YAML_Helper.save(results)

    // Verifying that a file with a timestamp in its name has been created
    val files = new File("src/main/resources/Results_Conversation").listFiles()
    files.exists(_.getName.contains("Results_Conversation-")) shouldBe true
  }
}
