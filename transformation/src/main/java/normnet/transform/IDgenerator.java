package normnet.transform;

public class IDgenerator {
	
	private static int ID = 1000000000;
	
	public static String getID() {
		
		return "ID"+String.valueOf(ID++);
	}
	

}
