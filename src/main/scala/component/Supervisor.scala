package component

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

import core._

class Supervisor(val config: Config) extends Actor with ActorLogging {
  import context.dispatcher
  implicit val timeout = Timeout(3.seconds)

  val tickActor = config.router map {
    _ => context.actorOf(TickActor.props(config), TickActor.name)
  }
  tickActor.map(_ ! Tick)

  val service = context.actorOf(Service.props(config, tickActor.isDefined,
    List(),
    List()),
    Service.name)

  def receive = {
    case _ =>
  }
}

object Supervisor {
  val actorSystem = ActorSystem("james")
  def props(config: Config) = Props(new Supervisor(config))
  def name = "supervisor"

  def getChild(childName: String) = actorSystem.actorSelection(s"/user/$name/$childName")
}

