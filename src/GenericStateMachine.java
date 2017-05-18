import java.util.ArrayList;
import java.util.List;
/**
 * Driver class for GenericState.
 * Separate class so you can have more than one instance of each state machine.
 * @author tim
 *
 */
public class GenericStateMachine {
	
	synchronized public GenericState getCurrentState(){
		return currentState;
	}
	/**
	 * Feed events in here
	 * @param e new event.
	 */
	synchronized public void consumeEvent(Event e){
		GenericState newState = currentState.accept(e);
		if( newState != currentState) {
			cancelTimers();
			currentState.leaving();
			currentState=newState;
			currentState.entering();
			if( currentState.hasTimeout()){
				startTimer(new Timer(currentState.getTimeout()));
			}
		}
	}

	synchronized private void startTimer(Timer start) {
		timers.add(start);
		start.start();
	}
	synchronized  private void cancelTimers() {
		for(Timer t: timers){
			t.interrupt();
		}
		timers.clear();
	}

	public void killMachine(){
		cancelTimers();
	}

	List<Timer> timers = new ArrayList<>();
	// BEGIN REPLACE_STATE
	private GenericState currentState =GenericState.GOING_UP;
	// END REPLACE_STATE

	class Timer extends Thread {
		private long delay;
		public Timer(long delay){
			this.delay=delay;
		}
		public void run(){
			try {
				Thread.sleep(delay);
				consumeEvent(Event.TIMEOUT);
			} catch (InterruptedException e) {
				//didn't timeout, or dying
			}
		}
	}
}