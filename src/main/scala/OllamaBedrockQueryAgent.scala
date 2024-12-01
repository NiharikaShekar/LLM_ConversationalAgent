import akka.actor.ActorSystem
import protobuf.llmQuery.{LlmQueryRequest, LlmQueryResponse}
import io.github.ollama4j.OllamaAPI
import io.github.ollama4j.utils.Options
import org.slf4j.LoggerFactory

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.jdk.CollectionConverters._

object OllamaBedrockQueryAgent {
  // Initializing the logger
  private val logger = LoggerFactory.getLogger(getClass)

  // Configuration keys
  private val OLLAMA_HOST = "ollama.host"
  private val OLLAMA_REQUEST_TIMEOUT = "ollama.request-timeout-seconds"
  private val OLLAMA_MODEL = "ollama.model"
  private val OLLAMA_QUERIES_RANGE = "ollama.range"

  // Prefixing for query generation
  private val LLAMA_PREFIX = "how can you respond to the statement "
  private val LLAMA_TO_LAMBDA_PREFIX = "Do you have any comments on "

  def main(args: Array[String]): Unit = {
    // Checking if the input seed text is passed
    if (args.isEmpty) {
      logger.error("Input seed text not passed")
      sys.exit(-1)
    }

    // Initializing the seed text and proto request
    val seedText = args(0)
    val protoRequest: LlmQueryRequest = new LlmQueryRequest(seedText, 100)

    // Creating an ActorSystem to manage concurrency
    implicit val system: ActorSystem = ActorSystem("AutomatedConversationalAgentSystem")

    try {
      // Starting the process of querying LLM
      start(protoRequest)
    } finally {
      // Terminating the actor system after completion
      system.terminate()
    }
  }

  def start(protoRequest: LlmQueryRequest)(implicit system: ActorSystem): Unit = {
    // Initializing Ollama API with configuration values
    val ollamaHost = ConfigurationManager.getConfig(OLLAMA_HOST)
    val llamaAPI = new OllamaAPI(ollamaHost)
    llamaAPI.setRequestTimeoutSeconds(ConfigurationManager.getConfig(OLLAMA_REQUEST_TIMEOUT).toLong)

    // Loading model and query range from application.conf
    val llamaModel = ConfigurationManager.getConfig(OLLAMA_MODEL)
    val range = ConfigurationManager.getConfig(OLLAMA_QUERIES_RANGE).toInt

    // Creating a mutable result collection for logging responses
    val results = YAML_Helper.createMutableResult()
    var currentRequest = protoRequest

    // Looping to generate responses and interact with LLM
    (0 until range).foreach { itr =>
      try {
        // Synchronously querying LLM and awaiting the response
        this.synchronized {
          val response = Await.result(LLMQueryService.queryLLM(currentRequest), 10.seconds)
          val input = currentRequest.input + " "
          val output = response.output

          // Synchronously generating response from Llama model
          val llamaResult = llamaAPI.generate(
            llamaModel,
            LLAMA_PREFIX + input + output,
            false,
            new Options(Map.empty[String, Object].asJava)
          )
          val llamaResp = llamaResult.getResponse

          // Printing and logging the Llama response
          println(llamaResp)
          YAML_Helper.appendResult(results, itr, input, output, llamaResp)

          // Preparing the next request for the next iteration
          currentRequest = new LlmQueryRequest(LLAMA_TO_LAMBDA_PREFIX + llamaResp, 100)
        }
      } catch {
        case e: Exception =>
          // Logging the error and throwing an exception if the process fails
          logger.error(s"PROCESS FAILED at iteration $itr: ${e.getMessage}", e)
          throw e
      }
    }

    // Saving the results once all iterations are complete
    try {
      YAML_Helper.save(results)
    } catch {
      case e: Exception =>
        logger.error(s"Error in processing: ${e.getMessage}", e)
    }
  }
}
