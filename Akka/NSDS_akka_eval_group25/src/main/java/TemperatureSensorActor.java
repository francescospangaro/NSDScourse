import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.util.concurrent.ThreadLocalRandom;

public class TemperatureSensorActor extends AbstractActor {

    private ActorRef dispatcher;
    private final static int MIN_TEMP = 0;
    private final static int MAX_TEMP = 50;

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(DispatcherReferenceMsg.class, this::onDispatcherRefMsg)
                .match(GenerateMsg.class, this::onGenerate)
                .build();
    }

    private void onGenerate(GenerateMsg msg) {
        System.out.println("TEMPERATURE SENSOR: Sensing temperature!");
        int temp = ThreadLocalRandom.current().nextInt(MIN_TEMP, MAX_TEMP + 1);
        dispatcher.tell(new TemperatureMsg(temp, self()), self());
    }

    private void onDispatcherRefMsg(DispatcherReferenceMsg msg) {
        dispatcher = msg.server();
    }

    static Props props() {
        return Props.create(TemperatureSensorActor.class);
    }

}
