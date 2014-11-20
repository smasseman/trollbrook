public class Test {
	public static void main(String[] args) throws Exception {
		Test.class.getMethod("main", String[].class).getParameters();
	}
}
