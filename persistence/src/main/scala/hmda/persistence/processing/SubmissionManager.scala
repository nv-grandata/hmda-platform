package hmda.persistence.processing

import akka.actor.{ ActorRef, Props }
import hmda.model.fi.SubmissionId
import hmda.persistence.CommonMessages.Command
import hmda.persistence.HmdaActor
import hmda.persistence.processing.HmdaRawFile.{ AddLine, CompleteUpload, StartUpload, UploadCompleted }
import hmda.persistence.processing.SubmissionManager.GetActorRef

object SubmissionManager {

  val name = "SubmissionManager"

  case class GetActorRef(name: String) extends Command

  def props(id: SubmissionId): Props = Props(new SubmissionManager(id))
}

class SubmissionManager(id: SubmissionId) extends HmdaActor {

  val submissionFSM: ActorRef = context.actorOf(SubmissionFSM.props(id))
  val submissionUpload: ActorRef = context.actorOf(HmdaRawFile.props(id))
  val submissionParser: ActorRef = context.actorOf(HmdaFileParser.props(id))
  val submissionValidator: ActorRef = context.actorOf(HmdaFileValidator.props(id))

  var uploaded: Int = 0

  override def receive: Receive = {

    case StartUpload =>
      submissionFSM ! StartUpload

    case m @ AddLine(timestamp, data) =>
      submissionUpload ! m

    case CompleteUpload =>
      submissionUpload ! CompleteUpload
      submissionFSM ! CompleteUpload

    case UploadCompleted(size, submissionId) =>
      uploaded = size

    case GetActorRef(name) => name match {
      case SubmissionFSM.name => sender() ! submissionFSM
      case HmdaRawFile.name => sender() ! submissionUpload
      case HmdaFileParser.name => sender() ! submissionParser
      case HmdaFileValidator.name => sender() ! submissionValidator
    }

  }

}
