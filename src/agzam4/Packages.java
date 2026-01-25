package agzam4;

public class Packages {

	public static boolean avalible(String name) {
		return Package.getPackage(name) != null;
	}
	
}
