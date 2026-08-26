package agzam4;

public class Packages {

	@SuppressWarnings("deprecation")
	public static boolean avalible(String name) {
		return Package.getPackage(name) != null;
	}
	
}
