import static org.junit.Assert.*;

import org.junit.Test;

public class RewriterTest {

	@Test
	public void test() {
		Rewriter r =new Rewriter("/home/tim/workspace/foo/src/GenericStateMachine.java");
		assertEquals(2,r.getSectionCount());
		r.substitute(Rewriter.SECTION_WHOLE_FILE, "REPLACE_STATE", "\tprivate GenericState currentState =GenericState.WHOOP;");
		r.replace(Rewriter.SECTION_WHOLE_FILE, "GenericState", "SomeState");
		System.out.println(r.getSection(Rewriter.SECTION_WHOLE_FILE));
	}

}
