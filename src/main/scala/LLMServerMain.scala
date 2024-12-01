import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object LLMServerMain extends App {

  // Initializing the ActorSystem, Materializer, and ExecutionContext
  implicit val system: ActorSystem = ActorSystem("LLMServerSystem")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  // Fetching server host and port from configuration
  val serverHost = ConfigurationManager.getConfig("server.host")
  val serverPort = ConfigurationManager.getConfig("server.port").toInt

  // Binding the server to the routes defined in LLMServiceEndpoints
  val bindingFuture = Http().newServerAt(serverHost, serverPort).bind(LLMServiceEndpoints.routes)

  // Logging the server startup and waiting for input to stop
  println(s"Server running at http://$serverHost:$serverPort/")
//  StdIn.readLine()
//
//  // Unbinding the server and terminating the system when done
//  bindingFuture
//    .flatMap(_.unbind())
//    .onComplete(_ => {
//      system.terminate()
//      println("Server stopped.")
//    })
}
