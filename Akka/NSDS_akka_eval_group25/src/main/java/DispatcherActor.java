import akka.actor.*;
import akka.japi.pf.DeciderBuilder;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DispatcherActor extends AbstractActorWithStash {
    private final static int retryNumber = 1;
    private final static int timeLength = 1;

    private final static int NO_PROCESSORS = 2;

    private final List<ActorRef> processors = new ArrayList<>();
    private int index = 0;

    private final Map<ActorRef, ActorRef> loadBalanceMap = new HashMap<>();

    private static SupervisorStrategy strategy =
            new OneForOneStrategy(
                    retryNumber, // Max no of retries
                    Duration.ofMinutes(timeLength), // Within what time period
                    DeciderBuilder.match(Exception.class, e -> SupervisorStrategy.resume())
                            .build());


    public DispatcherActor() {
        for (int i = 0; i < NO_PROCESSORS; i++) {
            processors.add(getContext().actorOf(SensorProcessorActor.props()));
        }
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }


    @Override
    public AbstractActor.Receive createReceive() {
        return createReceiveLoadBalancer();
    }

    private Receive createReceiveLoadBalancer() {
        System.out.println("------------------------");
        System.out.println("Changing to load balancer");
        return receiveBuilder()
                .match(DispatchLogicMsg.class, this::onDispatchLogicMsg)
                .match(TemperatureMsg.class, this::dispatchDataLoadBalancer)
                .build();
    }

    private Receive createReceiveRoundRobin() {
        System.out.println("------------------------");
        System.out.println("Changing to round robin");
        return receiveBuilder()
                .match(DispatchLogicMsg.class, this::onDispatchLogicMsg)
                .match(TemperatureMsg.class, this::dispatchDataRoundRobin)
                .build();
    }

    private void onDispatchLogicMsg(DispatchLogicMsg p) {
        if (p.getLogic() == DispatchLogicMsg.LOAD_BALANCER)
            getContext().become(createReceiveLoadBalancer());
        else
            getContext().become(createReceiveRoundRobin());
    }

    private void dispatchDataLoadBalancer(TemperatureMsg msg) {
        if (!loadBalanceMap.containsKey(msg.getSender())) {
            Map<ActorRef, Long> result = loadBalanceMap
                    .values()
                    .stream()
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

            for (ActorRef p : processors)
                if (!result.containsKey(p))
                    result.put(p, 0L);

            Map.Entry<ActorRef, Long> entryWithMinValue = result.entrySet()
                    .stream()
                    .min(Comparator.comparing(Map.Entry::getValue))
                    .orElseThrow();

            loadBalanceMap.put(msg.getSender(), entryWithMinValue.getKey());

            System.out.println("\n[DISPATCHER] load balancing policy: ");
            for (ActorRef p : processors) {
                System.out.println("Processor: " + p + " {");
                for (Map.Entry<ActorRef, ActorRef> entry : loadBalanceMap.entrySet()) {
                    if (entry.getValue() == p)
                        System.out.println("\t" + entry.getKey());
                }
                System.out.println("}");
            }

        }
        loadBalanceMap.get(msg.getSender()).tell(msg, self());
        System.out.println("[DISPATCHER] msg from: " + msg.getSender() + ", forwarded to " + loadBalanceMap.get(msg.getSender()));
    }

    private void dispatchDataRoundRobin(TemperatureMsg msg) {
        processors.get(index).tell(msg, self());
        index++;
        index = index % NO_PROCESSORS;
        System.out.println("[DISPATCHER] msg from: " + msg.getSender() + ", forwarded to " + processors.get(index));
    }

    static Props props() {
        return Props.create(DispatcherActor.class);
    }
}
