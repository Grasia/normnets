package normnet.transform;

import org.eclipse.emf.ecore.resource.Resource;

public class Util {
	
	public static String getResourceName(Resource resource)
	{	String uri = resource.getURI().toString();
		
		String resourceName = "NoName";
		// TODO: Handle error cases, and throw exception if there is no name
		if (uri.contains("/")) {
			int hashIndex = uri.lastIndexOf("/");
			
			if (uri.contains(".")) {
				int dotIndex = uri.lastIndexOf(".");
				resourceName = uri.substring(hashIndex+1, dotIndex);
			}
			else {
				resourceName = uri.substring(hashIndex+1);		
			}
		}
		return resourceName;
	}
	
	public static String getName(String URI) {
		if (!URI.contains("#")) {
			return null;
		} else {
			int hashIndex = URI.lastIndexOf("#");
			return URI.substring(hashIndex+1);
		}	
	}
}
