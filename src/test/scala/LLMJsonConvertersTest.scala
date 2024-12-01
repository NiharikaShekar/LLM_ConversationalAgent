import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import spray.json._
import protobuf.llmQuery.{LlmQueryRequest, LlmQueryResponse}
import LLMJsonConverters._

class LLMJsonConvertersTest extends AnyFlatSpec with Matchers {

  // Test for converting LlmQueryRequest to JSON and back
  "LLMJsonConverters" should "convert LlmQueryRequest to JSON and back" in {
    // Creating a sample LlmQueryRequest
    val originalRequest = LlmQueryRequest("test input", 100)

    // Converting the request to JSON
    val json = originalRequest.toJson

    // Reconstructing the request from the JSON
    val reconstructedRequest = json.convertTo[LlmQueryRequest]

    // Checking that the input and maxWords are the same after reconstruction
    reconstructedRequest.input shouldBe originalRequest.input
    reconstructedRequest.maxWords shouldBe originalRequest.maxWords
  }

  // Test for converting LlmQueryResponse to JSON and back
  it should "convert LlmQueryResponse to JSON and back" in {
    // Creating a sample LlmQueryResponse
    val originalResponse = LlmQueryResponse("test input", "test output")

    // Converting the response to JSON
    val json = originalResponse.toJson

    // Reconstructing the response from the JSON
    val reconstructedResponse = json.convertTo[LlmQueryResponse]

    // Checking that the input and output are the same after reconstruction
    reconstructedResponse.input shouldBe originalResponse.input
    reconstructedResponse.output shouldBe originalResponse.output
  }

  // Test for handling JSON serialization of case classes
  it should "handle JSON serialization of case classes" in {
    // Creating a sample LlmQueryRequestCase
    val requestCase = LlmQueryRequestCase("test", 50)

    // Converting the case class to JSON
    val json = requestCase.toJson

    // Verifying that the JSON fields match the original case class values
    json.asJsObject.fields("input") shouldBe JsString("test")
    json.asJsObject.fields("maxWords") shouldBe JsNumber(50)
  }
}
