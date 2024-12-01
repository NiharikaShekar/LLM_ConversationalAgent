import spray.json._
import DefaultJsonProtocol._
import protobuf.llmQuery.{LlmQueryRequest, LlmQueryResponse}

// Intermediate case classes for proto objects
case class LlmQueryRequestCase(input: String, maxWords: Int)
case class LlmQueryResponseCase(input: String, output: String)


object LLMJsonConverters {
  // Defining JSON formats for intermediate case classes
  implicit val llmQueryRequestCaseFormat: RootJsonFormat[LlmQueryRequestCase] = jsonFormat2(LlmQueryRequestCase)
  implicit val llmQueryResponseCaseFormat: RootJsonFormat[LlmQueryResponseCase] = jsonFormat2(LlmQueryResponseCase)

  // Custom formats for proto-generated classes
  // Converting LlmQueryRequest object to JSON and vice versa
  implicit val llmQueryRequestFormat: RootJsonFormat[LlmQueryRequest] = new RootJsonFormat[LlmQueryRequest] {
    // Converting LlmQueryRequest to its case class format and then to JSON
    override def write(obj: LlmQueryRequest): JsValue = {
      LlmQueryRequestCase(obj.input, obj.maxWords).toJson
    }

    // Reading JSON and converting it back to LlmQueryRequest object
    override def read(json: JsValue): LlmQueryRequest = {
      val caseClass = json.convertTo[LlmQueryRequestCase]
      LlmQueryRequest(caseClass.input, caseClass.maxWords)
    }
  }

  // Converting LlmQueryResponse object to JSON and vice versa
  implicit val llmQueryResponseFormat: RootJsonFormat[LlmQueryResponse] = new RootJsonFormat[LlmQueryResponse] {
    // Converting LlmQueryResponse to its case class format and then to JSON
    override def write(obj: LlmQueryResponse): JsValue = {
      LlmQueryResponseCase(obj.input, obj.output).toJson
    }

    // Reading JSON and converting it back to LlmQueryResponse object
    override def read(json: JsValue): LlmQueryResponse = {
      val caseClass = json.convertTo[LlmQueryResponseCase]
      LlmQueryResponse(caseClass.input, caseClass.output)
    }
  }
}
