public class DispatchLogicMsg {
	
	public static final int ROUND_ROBIN = 0;
	public static final int LOAD_BALANCER = 1;
	
	private int logic;
	
    public DispatchLogicMsg (int logic) {
    	this.logic = logic;
    }

	public int getLogic() {
		return logic;
	}
}
