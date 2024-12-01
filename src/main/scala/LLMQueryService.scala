import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.stream.Materializer
import LLMJsonConverters._
import akka.util.ByteString
import org.slf4j.LoggerFactory
import spray.json._
import protobuf.llmQuery.{LlmQueryRequest, LlmQueryResponse}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

object LLMQueryService {
  // Initializing the logger
  private val logger = LoggerFactory.getLogger(getClass)

  /**
   * Sends an HTTP request to query the LLM service using the provided protocol buffer request.
   * @param protoRequest The request object with input and configuration details.
   * @param system The actor system used to handle asynchronous tasks.
   * @return A Future that holds the response from the LLM service.
   */

  def queryLLM(protoRequest: LlmQueryRequest)(implicit system: ActorSystem): Future[LlmQueryResponse] = {
    implicit val ec: ExecutionContext = system.dispatcher
    implicit val materializer: Materializer = Materializer(system)

    // Fetching the API Gateway URL and maxWords from the configuration file
    val url = ConfigurationManager.getConfig("lambdaApiGateway")
    val maxWords: Int = if (protoRequest.maxWords != 0) {
      protoRequest.maxWords
    } else {
      ConfigurationManager.getConfig("maxWords").toInt
    }

    // Creating the HTTP request
    val httpRequest = HttpRequest(
      method = HttpMethods.GET,
      uri = Uri(url),
      entity = HttpEntity.Strict(
        ContentTypes.`application/grpc+proto`,
        ByteString(protoRequest.toProtoString.getBytes)
      )
    )

    // Logging the constructed HTTP request for debugging
    logger.info(s"Sending request to URL: $url with maxWords: $maxWords")

    // Sending the HTTP request and handle the response
    Http().singleRequest(httpRequest).flatMap { response =>
      response.status.intValue() match {
        case statusCode if statusCode >= 200 && statusCode < 300 =>
          // Logging the successful response status
          logger.info(s"Received successful response with status: ${response.status}")

          // Extracting and process the response body
          response.entity.toStrict(5.seconds).map { entity =>
            val responseBody = entity.data.utf8String
            val resp = responseBody.parseJson.convertTo[LlmQueryResponse]

            // If response output exceeds maxWords, truncating it
            if (resp.output.split(" ").length > maxWords) {
              val truncatedOutput = resp.output.split(" ").take(maxWords).mkString(" ")
              val processedResp = LlmQueryResponse(resp.input, truncatedOutput)
              logger.info(s"Processed response: $processedResp")
              processedResp
            } else {
              logger.info(s"Response within limit: $resp")
              resp
            }
          }

        case statusCode if statusCode >= 400 && statusCode < 600 =>
          // Logging the error response details
          val errorMsg = s"API call failed with status: ${response.status}, error: ${response.entity.toString}"
          logger.error(errorMsg)
          Future.failed(new RuntimeException(errorMsg))
      }
    }
  }
}
