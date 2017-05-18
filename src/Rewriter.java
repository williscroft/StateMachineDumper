import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.regex.Matcher;

/**
 * The worst of all possible file template non-systems.
 * Notably eats valid java.
 * @author tim
 *
 */
public class Rewriter {
	public static final String SECTION_WHOLE_FILE = "";
	private Map<String,List<String>> sections = new HashMap<>();

	public Rewriter(String filename) {
		Stack<Context> context= new Stack<>();
		{
			List<String> outer =new ArrayList<>();
			String inBlock =SECTION_WHOLE_FILE;
			Context outerMost=new Context(outer, inBlock);
			sections.put(inBlock, outer);
			context.push(outerMost);
		}
		
		try( FileReader f = new FileReader(filename);
			 BufferedReader b = new BufferedReader(f);){
			for( String line = b.readLine(); line!=null; line=b.readLine()){
				if( line.trim().startsWith("// BEGIN")){
					StringTokenizer s = new StringTokenizer(line);
					if( s.countTokens()!=3) {
						System.err.println("Syntax error not // BEGIN {label} ["+line+"]");
						System.exit(-1);
					}
					s.nextToken();s.nextToken();
					String newBlock = s.nextToken();
					List<String> newSection = new ArrayList<>();
					sections.put(newBlock, newSection);
					Context newContext = new Context(newSection, newBlock);
					context.push(newContext);
				} else if( line.trim().startsWith("// END")){
					StringTokenizer s = new StringTokenizer(line);
					if( s.countTokens()!=3) {
						System.err.println("Syntax error not // END {label} ["+line+"]");
						System.exit(-1);
					}
					s.nextToken();s.nextToken();
					String nextToken = s.nextToken();
					String expect=context.peek().name;
					if( !nextToken.equals(expect)){
						System.err.println("label nesting error expected ["+expect+"] but got ["+nextToken+"]");
						System.exit(-2);
					}
					
					Context current = context.pop();
					context.peek().lines.add("BLOCK "+expect);
				} else {
					context.peek().lines.add(line);
				}
			}
			// file over.
		} catch (FileNotFoundException e) {
			System.out.println("File not found ["+filename+"]");
		} catch (IOException e) {
			System.out.println("IO error on file ["+filename+"]");
		}
	}
	
	/**
	 * Replace a single sub-section with new text
	 * @param context outer
	 * @param block which block marker
	 * @param with instead of inner block
	 */
	public void substitute(String context,String block,String with){
		replace(context,"BLOCK "+block,with);
	}
	/**
	 * find and replace in a sub-section.
	 * uses string.replaceAll()
	 * @param context outer
	 * @param find regexp
	 * @param replace the replacement.
	 */
	public void replace(String context,String find,String replace){
		List<String> s= sections.get(context);
		if( s==null){
			System.err.println("Block not in file "+context);
			System.exit(-3);
		}
		for( int lineNumber=0; lineNumber < s.size(); lineNumber++){
			String line = s.get(lineNumber);
			String edited=line.replaceAll(find, Matcher.quoteReplacement(replace));
			s.set(lineNumber, edited);
		}
	}
	
	
	
	/**
	 * Inline a block into a parent scope.
	 * @param outer the outer
	 * @param block the inner
	 */
	public void inline(String outer,String block){
		//FIXME test
		List<String> s= sections.get(block);
		if( s==null){
			System.err.println("Block not in file "+block);
			System.exit(-3);
		}
		String mess= flatten(s);
		substitute(outer,block,mess);
	}

	public String getSection(String section) {
		return flatten(sections.get(section));
	}


	private String flatten(List<String> list) {
		StringBuilder s= new StringBuilder();
		for( String line: list){
			s.append(line);
			s.append('\n');
		}
		return s.toString();
	}

	public Object getSectionCount() {
		return sections.size();
	}

}
class Context{
	List<String> lines;
	String name;
	
	public Context(List<String> lines,String name) {
		this.lines=lines;
		this.name=name;
	}
}