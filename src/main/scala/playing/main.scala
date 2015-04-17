package playing
 
import akka.actor.Actor
import akka.actor.Props

object Greeter {
  case object Greet
  case object Done
  def simulatedWork[T](f: => T): T = {
    val r = f
    Thread.sleep(1000)
    r
  }
}

class HelloWorld extends Actor {
  import Greeter._
  override def preStart(): Unit = {
    val greeter = context.actorOf(Props[Greeter], "greeter")
    simulatedWork {
      println("about to send message")
    }
    greeter ! Greet
  }

  def receive = {
    case Done =>
      simulatedWork {
        println("received termination request")
      }
      context.stop(self)
  }
}

class Greeter extends Actor {
  import Greeter._
  def receive = {
    case Greet =>
      simulatedWork {
        println("received message")
      }
      println("HelloWorld")
      sender ! Done
  }
}

object Main {
  def main(args: Array[String]): Unit = {
    akka.Main.main(Array(classOf[HelloWorld].getName))
  }
}
