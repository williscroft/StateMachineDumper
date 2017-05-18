import java.io.IOException;

public class exec {
public static void main(String[] args) {
	try {
		Runtime.getRuntime().exec("echo");
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}
}
