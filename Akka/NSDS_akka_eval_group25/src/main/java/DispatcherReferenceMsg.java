import akka.actor.ActorRef;

public record DispatcherReferenceMsg(ActorRef server) {
}
