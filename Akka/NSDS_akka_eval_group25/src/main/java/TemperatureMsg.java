import akka.actor.ActorRef;

public class TemperatureMsg {

	private int temperature;
	private ActorRef sender;

	public TemperatureMsg(int temp, ActorRef sender) {
		this.temperature = temp;
		this.sender = sender;
	}
	
	public int getTemperature() {
		return temperature;
	}

	public ActorRef getSender() {
		return sender;
	}
}
