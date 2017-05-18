
public enum GenericState {
// BEGIN DUMMY_CODE	
	GOING_UP {
		@Override
		GenericState accept(Event e) {
			switch(e){
			}
			return GOING_UP;
		}

		@Override
		void leaving() {
			// TODO Auto-generated method stub
			
		}

		@Override
		void entering() {
			// TODO Auto-generated method stub
			
		}

	}, 
	;
	private long timeout;
	private GenericState(long timeout) {
		this.timeout=timeout;
	}
	private GenericState() {
	}
// END DUMMY_CODE	
	abstract GenericState accept(Event e);
	abstract void leaving();
	abstract void entering();
	boolean hasTimeout() { return timeout==0;};
	long getTimeout() {
		return timeout;
	}
}
