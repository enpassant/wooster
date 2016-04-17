package core

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Cancellable, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import java.util.UUID
import scala.concurrent.Future
import scala.concurrent.duration._
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, RequestEntity}
import akka.http.scaladsl.model.HttpMethods._
import akka.stream.ActorMaterializer

case class Tick()
case class Restart()

class TickActor(val config: Config) extends Actor with ActorLogging  with ServiceFormats {
  implicit val timeout: Timeout = Timeout(15.seconds)
  import scala.concurrent.ExecutionContext.Implicits.global

  type RegisterFn = () => Future[HttpResponse]

  val serviceUri = s"http://${config.router.get}/services"

  implicit val system = context.system
  implicit val materializer = ActorMaterializer()

  def register(
    serviceUri: String,
    microService: MicroService,
    entity: Future[RequestEntity]): RegisterFn = () =>
  {
    val response: Future[HttpResponse] = entity.flatMap { e =>
      Http().singleRequest(HttpRequest(method = PUT,
        uri = serviceUri + "/" + microService.uuid,
        entity = e))
      }
      response.map { r =>
        log.info(s"Service ${microService.uuid} has joined to ${config.router.get}")
      }
      response
  }

  def schedule(registerFn: RegisterFn) = {
    val c = context.system.scheduler.scheduleOnce(60 seconds, self, Tick)
    context.become(process(registerFn, c))
  }

  def receive = {
    case microService: MicroService =>
      val entity = Marshal(microService).to[RequestEntity]
      val registerFn = register(serviceUri, microService, entity)
      registerFn()
      schedule(registerFn)

    case _ =>
  }

  def process(registerFn: RegisterFn, cancellable: Cancellable): Receive = {
    case Tick =>
      registerFn()
      schedule(registerFn)

    case Restart =>
      cancellable.cancel
      schedule(registerFn)
  }
}

object TickActor {
  def props(config: Config) = Props(new TickActor(config))
  def name = "tick"
}

