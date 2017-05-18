import java.util.HashMap;
import java.util.Map;

/**
 * Infinite state machine dumper.
 * A cure for the infinite state machine anti-pattern.
 * @author tim
 *
 */
public class Dumper {

	Map<State,Map<Event,State>>  table;
	private String name;
	//hideous magic!
	Map<String,StateListener> stateListeners;
	
	public Dumper() {
		name="Dummy";
		table = new HashMap<>();
		stateListeners=new HashMap<>();
		Map<Event, State> exitingDown = new HashMap<>();
		exitingDown.put(Event.SAW_MOVEMENT, State.GOING_UP);
		exitingDown.put(Event.ERRORS, State.OW);
		exitingDown.put(Event.TUNNEL_UP, State.UP); // surprise, MOFOS
		table.put(State.DOWN, exitingDown );
	
		Map<Event, State> exitingGoingUp = new HashMap<>();
		exitingGoingUp.put(Event.DIED, State.DOWN);
		exitingGoingUp.put(Event.ERRORS, State.DOWN);
		exitingGoingUp.put(Event.TUNNEL_UP, State.UP);
		exitingGoingUp.put(Event.GO_DOWN, State.GOING_DOWN);
		table.put(State.GOING_UP, exitingGoingUp );
		
		Map<Event, State> exitingUp=new HashMap<>();
		exitingUp.put(Event.DIED,State.DOWN);
		exitingUp.put(Event.ERRORS,State.DOWN);
		exitingUp.put(Event.SAW_MOVEMENT,State.UP);
		exitingUp.put(Event.TUNNEL_UP,State.UP);  // race conditions everywhere		
		exitingUp.put(Event.GO_DOWN,State.GOING_DOWN);  // race conditions everywhere		
		table.put(State.UP, exitingUp);

		Map<Event, State> exitingGoingDown = new HashMap<>();
		exitingDown.put(Event.DIED, State.DOWN);
		exitingDown.put(Event.ERRORS, State.DOWN);
		exitingDown.put(Event.GO_DOWN, State.GOING_DOWN);
		exitingDown.put(Event.SAW_MOVEMENT, State.GOING_UP);
		exitingDown.put(Event.TUNNEL_UP, State.UP);
		
		table.put(State.GOING_DOWN, exitingGoingDown );
		
		addListener(null, null, new StateListener() {			
			@Override
			public void callback(State state, Event event) {
				//dump all transitions to the logs
				System.out.println("LOGGING transtition"+state+"["+event+"]");
			}
		});

		addListener(State.GOING_UP, null, new StateListener() {
			
			@Override
			public void callback(State state, Event event) {
				// going up
				RandomClass.DoSomething();
			}
		});
addListener(null, null, new StateListener() {
			
			@Override
			public void callback(State state, Event event) {
				// not in going up, but saw movement.
				if( state!=State.GOING_UP){
					RandomClass.cancelTimeout();
				}
			}
		});
addListener(State.GOING_UP, null, new StateListener() {
	
	@Override
	public void callback(State state, Event event) {
		// arm timeout timer
		RandomClass.armTimoeut();
	}
});

	}
	
	private void addListener(State state,Event event,StateListener listener){
		stateListeners.put(encodeTransition(state,event), listener);
	}
	/**
	 * Make a infinite state machine dumper from a supplied table.
	 * @param name what the machine is for
	 * @param table a state transition table
	 * @param stateListeners evil state listeners.
	 */
	public Dumper(String name,Map<State,Map<Event,State>> table,Map<String,StateListener> stateListeners){
		this.name=name;
		this.table=table;
		this.stateListeners=stateListeners;
	}
	/**
	 * COPY OF LEGACY CODE.
	 * Pathological string-oriented programming approach to
	 * keying on two possibly null objects.
	 * @param state
	 * @param event
	 * @return a key
	 */
	String encodeTransition(State state,Event event){
		if( state==null && event== null) return "*-*";
		if( state==null && event!=null) return "*-"+event.name();
		if( state!=null && event== null) return state.name()+"-*";
		return state.name()+"-"+event.name();
	}
	
	public static void main(String[] args) {
		new Dumper().dump2Java(State.DOWN);
	}

	/**
	 * what the infinite state machine dumps like.
	 * COPY OF LEGACY CODE.
	 */
	private void dump() {
		System.out.println("State machine dump for "+name);
		for(State state : table.keySet()){
			Map<Event, State> thisStateEdges = table.get(state);
			for ( Event thisEvent: thisStateEdges.keySet()){
				log( state,thisEvent,thisStateEdges.get(thisEvent));
			}
		}
	}
    
	/**
	 * Write a new java file that implements the state machine directly.
	 * Depends on section names in GenericStateMachine.
	 * @param groundState
	 */
	private void dump2Java(State groundState){
		Rewriter r =new Rewriter("/home/tim/workspace/foo/src/GenericStateMachine.java");
		
		String US=name+"StateMachine";
		r.replace("", "GenericStateMachine", US);
		String zero="private "+name+"State currentState ="+State.values()[0]+";";
		r.substitute(Rewriter.SECTION_WHOLE_FILE, "REPLACE_STATE", zero);
		r.replace("", "GenericState", name+"State");
		System.out.println(r.getSection(Rewriter.SECTION_WHOLE_FILE));

	
		Rewriter rs =new Rewriter("/home/tim/workspace/foo/src/GenericState.java");
		StringBuilder states= new StringBuilder();
		for(State state : table.keySet()){
			dumpAState(states,state);
		}
		rs.substitute(Rewriter.SECTION_WHOLE_FILE, "DUMMY_CODE", states.toString());
		rs.replace("", "GenericState", name+"State");
		System.out.println(rs.getSection(Rewriter.SECTION_WHOLE_FILE));

	}
	
	/**
	 * Write the code for a single state value to the supplied buffer.
	 * @param out
	 * @param state
	 */
	private void dumpAState(StringBuilder out, State state) {
		Map<Event, State> stateTransitions = table.get(state);
		
		out.append("  "+state.name()+"{\n");
		out.append("    @Override\n");  
		out.append("    GenericState accept(Event e) {\n");
		out.append("      switch(e){\n");
		for( Event e : stateTransitions.keySet()){
			out.append("        case "+e.name()+":{\n");
			StateListener  transitioner = stateListeners.get(encodeTransition(state, e));
			dsl(out,transitioner,"          ");
			out.append("          return "+stateTransitions.get(e)+"\n");
			out.append("         }\n");
		}
		out.append("      }\n");
		out.append("    return "+state.name()+";\n");
		out.append("    }\n");

		out.append("    @Override\n");
		out.append("    void leaving() {\n");
		StateListener  leaver = stateListeners.get(encodeTransition(state, null));
		dsl(out,leaver,"    ");
		out.append("    // TODO Auto-generated method stub\n");
		out.append("\n");	
		out.append("    }\n");

		out.append("    @Override\n");
		out.append("    void entering() {\n");
		StateListener  enterer = stateListeners.get(encodeTransition(state, null));
		dsl(out,enterer,"      ");
		out.append("      // TODO Auto-generated method stub\n");
		out.append("\n");	
		out.append("    }\n");

		out.append("  },\n");
		
	}

	private void dsl(StringBuilder out, StateListener l, String string) {
		if( l==null) return;
		out.append(string+"// code in "+l.getClass());
	}

	private void log(State state, Event thisEvent, State state2) {
		System.out.print(state.name());
		System.out.print("-->");
		System.out.print(thisEvent.name());
		System.out.print("-->");
		System.out.print(state2.name());
		System.out.println();
	}

}
