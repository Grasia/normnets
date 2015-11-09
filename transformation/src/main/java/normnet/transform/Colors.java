package normnet.transform;

import java.util.ArrayList;

import org.cpntools.accesscpn.model.Sort;


public class Colors {
	
	private static ArrayList<Sort> allcolors = new ArrayList<Sort>();
	
	public static ArrayList<Sort> getcolors() {

		return allcolors;
	}
	
	public static Sort getcolorbyName(String name) {

		Sort color = null;
		for(int i=0;i<allcolors.size();i++){
			if(allcolors.get(i).getText().equals(name))
				color = allcolors.get(i);
		}
		return color;
	}

}
