import akka.actor.AbstractActor;
import akka.actor.Props;

public class SensorProcessorActor extends AbstractActor {

    private double currentAverage;
    private int readings;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TemperatureMsg.class, this::gotData)
                .build();
    }

    private void gotData(TemperatureMsg msg) throws Exception {

        System.out.println("SENSOR PROCESSOR " + self() + ": Got data from " + msg.getSender() + " value: " + msg.getTemperature() + " count: " + readings);

        if (msg.getTemperature() >= 0) {
            currentAverage = (currentAverage * readings + msg.getTemperature()) / (readings + 1);
            readings++;
        } else throw new Exception("Negative temperature received");

        System.out.println("SENSOR PROCESSOR " + self() + ": Current avg is " + currentAverage);
    }

    static Props props() {
        return Props.create(SensorProcessorActor.class);
    }

    public SensorProcessorActor() {
    }
}
