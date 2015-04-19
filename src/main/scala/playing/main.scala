package playing
 
import akka.actor.{
  Actor,
  ActorSystem,
  Props,
  PoisonPill,
  ActorRef
}
import scala.io.Source.fromFile

import akka.routing.RoundRobinPool
import akka.routing.Broadcast


object CharacterCounter {
  sealed trait Message
  case object GimmeWork extends Message
  case object CurrentlyBusy extends Message
  case object WorkAvailable extends Message
  case object Done extends Message
  case class RegisterWorker(worker: ActorRef) extends Message
  case class Terminated(worker: ActorRef) extends Message
  case class Result(lineLength: Int) extends Message
  case class Work[T](work: T) extends Message
  case class ProcessFile(path: String) extends Message
  case class ProcessLines(work: List[String], hasMore: Boolean) extends Message
  case class CountResult(counts: List[Int]) extends Message
  def simulatedWork[T](f: => T): T = {
    val r = f
    Thread.sleep(1000)
    r
  }
}

class Master extends Actor {
  import CharacterCounter._

  val workersCount = 10
  val workChunkSize = 2
  var pendingWork = 0
  //val workers = mutable.Set.empty[ActorRef]
  //val inputLines = fromFile("derps.txt").getLines()

  def receive = init

  def init: Receive = {

    case ProcessFile(path) =>
      val workersPool = context.actorOf(RoundRobinPool(workersCount).props(Props[FileReadWorker]), "readWorkers")
      val it = fromFile(path).getLines
      workersPool ! Broadcast(WorkAvailable)
      context.become(processing(it, workersPool))

  }

  def processing(it: Iterator[String], workers: ActorRef): Receive = {

    case GimmeWork if it.hasNext =>
      val lines = List.fill(workChunkSize){
        if (it.hasNext) Some(it.next)
          else None
      }.flatten

      println("master: sending lines!!!")
      pendingWork += 1
      sender ! ProcessLines(lines, it.hasNext)

    case GimmeWork if pendingWork > 0 =>
      sender ! PoisonPill

    case GimmeWork =>
      println("master: no more lines")
      //If no more lines, broadcast poison pill
      workers ! Broadcast(PoisonPill)
      context.system.shutdown()

    case CountResult(counts) =>
      pendingWork -= 1
      println("master: received counts")
      counts foreach {
        println
      }

  }

}

class FileReadWorker extends Actor{
  import CharacterCounter._

  def receive = {

    case WorkAvailable =>
      sender ! GimmeWork

    case ProcessLines(lines, hasNext) =>
      println("an worker: countingggg")
      Thread.sleep(1000)
      sender ! CountResult(lines.map { line => line.length })
      sender ! GimmeWork

  }
}


object Main {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("doingthings")
 
    // create the master
    val master = system.actorOf(Props(new Master()), name = "master")
 
    // start the calculation
    master ! CharacterCounter.ProcessFile("derp.txt")
  }
}
