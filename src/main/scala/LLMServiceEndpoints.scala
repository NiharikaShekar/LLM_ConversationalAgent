import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import LLMJsonConverters._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}
import protobuf.llmQuery._

import scala.util.{Failure, Success}

object LLMServiceEndpoints {
  private val logger = LoggerFactory.getLogger(getClass)
  private val OLLAMA_QUERIES_RANGE = "ollama.range"

  /**
   * Defining all HTTP routes for the LLM Service.
   * @param system The ActorSystem instance used for handling requests.
   * @return The Route object containing all HTTP endpoints.
   */
  def routes(implicit system: ActorSystem): Route = {
    // Initializing ExecutionContext to handle Futures asynchronously
    implicit val ec: ExecutionContext = system.dispatcher

    concat(
      path("query-llm") {
        get {
          // Checking if the request entity can be unmarshalled as LlmQueryRequest
          entity(as[LlmQueryRequest]) { request =>
            // Handling the asynchronous API call to the LLM service
            onSuccess(LLMQueryService.queryLLM(request)) { response =>
              // Returning the successful response
              complete(response)
            }
          }
        }
      },
      path("start-conversation-agent") {
        get {
          // Checking if the request entity can be unmarshalled as LlmQueryRequest
          entity(as[LlmQueryRequest]) { request =>
            // Starting the Automated Conversational Agent in a separate Future thread
            Future {
              logger.info("Starting Automated Conversational Agent...")
              OllamaBedrockQueryAgent.start(request)
              logger.info("Successfully completed the execution of the client...")
            }.onComplete {
              // Handling success or failure of the Future execution
              case Success(value) => logger.info(s"Successfully completed the execution of the client $value")
              case Failure(ex) => logger.error(s"An error occurred when executing the client: $ex")
            }

            // Immediately responding to the client with an accepted status and information about the file location
            complete(
              StatusCodes.Accepted,
              "Conversation started. Please check the file in location " +
                "src/main/resources/agent-resp/convestn-{timestamp}"
            )
          }
        }
      },
      path("health") {
        get {
          // Responding with a health status indicating the service is up and running
          complete(StatusCodes.OK, "LLM REST Service is up and running!")
        }
      }
    )
  }
}
