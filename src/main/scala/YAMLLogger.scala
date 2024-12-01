import org.yaml.snakeyaml.{DumperOptions, Yaml}

import scala.collection.mutable.ListBuffer
import java.io.{BufferedWriter, File, FileWriter}
import java.time.Instant
import scala.jdk.CollectionConverters._

case class YAMLLogger(
                            iteration: String,
                            question: String,
                            llmResponse: String,
                            ollamaResponse: String
                          )

object YAML_Helper {
  // Setting options for YAML formatting
  private val options = new DumperOptions
  options.setPrettyFlow(true)
  options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK) // Use block formatting for better readability

  private val yaml = new Yaml(options)

  // Creating an empty list to hold iteration results
  def createMutableResult(): ListBuffer[YAMLLogger] = {
    ListBuffer.empty[YAMLLogger]
  }

  // Adding results to the list for each iteration
  def appendResult(
                    results: ListBuffer[YAMLLogger],
                    iteration: Int,
                    question: String,
                    llmResp: String,
                    ollamaResp: String
                  ): Unit = {
    results += YAMLLogger(s"Itr-$iteration", question, llmResp, ollamaResp)
  }

  // Saving the iteration results into a YAML file
  def save(results: ListBuffer[YAMLLogger]): Unit = {
    // Creating a new file with the current timestamp
    val file = new File("src/main/resources/Results_Conversation/Results_Conversation-" + Instant.now().toString + ".yaml")
    val writer = new BufferedWriter(new FileWriter(file))

    try {
      // Writing each iteration result to the YAML file
      results.foreach { result =>
        val entry = Map(
          result.iteration -> Map(
            "question" -> result.question,
            "LLM Response" -> result.llmResponse,
            "Ollama Response" -> result.ollamaResponse
          ).asJava
        ).asJava
        yaml.dump(entry, writer)
      }
      println(s"YAML file created at: ${file.getAbsolutePath}")
    } finally {
      // Ensuring the writer is closed after saving
      writer.close()
    }
  }

}
