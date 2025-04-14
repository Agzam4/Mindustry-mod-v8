package agzam4;

import mindustry.Vars;

public class Awt {

	public static boolean avalible = false;// = avalible();
	
	@SuppressWarnings("deprecation")
	public static boolean avalible() { // FIXME
//		return false;
//		try {
//			return Package.getPackage("java.awt") != null;
//			String result = Vars.mods.getScripts().runConsole("java.lang.Package.getPackage(\"java.awt\") != null;");//Vars.mods.mainLoader().getDefinedPackage(\"java.awt\")");
//			if(result == null) return false;
//			return !result.equals("null");//Vars.mods.mainLoader().getDefinedPackage("java.awt") != null;
//			boolean pcl = ClassLoader.getPlatformClassLoader().getDefinedPackage("java.awt") != null;
//			boolean scl = ClassLoader.getSystemClassLoader().getDefinedPackage("java.awt") != null;
//			if(!pcl) {
//				Log.info("PlatformClassLoader not loaded java.awt");
//			}
//			if(!scl) {
//				Log.info("SystemClassLoader not loaded java.awt");
//			}
			return Package.getPackage("java.awt") != null;
//			return pcl && scl;
//		} catch (Error e) {
//			return false;
//		}
//		return MyTray.class.getClassLoader().getDefinedPackage("java.awt") != null;
	}

	public static boolean message(String string) {
		//*
		if(!avalible()) return false;
		try {
			java.awt.Toolkit.getDefaultToolkit().beep();
			java.awt.SystemTray tray = java.awt.SystemTray.getSystemTray();
			java.awt.Image image = new java.awt.image.BufferedImage(5, 5, java.awt.image.BufferedImage.TYPE_INT_ARGB);
			java.awt.TrayIcon trayIcon = new java.awt.TrayIcon(image, "Mindustry");
	        trayIcon.setImageAutoSize(true);
	        trayIcon.setToolTip("Mindustry");
	        try {
				tray.add(trayIcon);
			} catch (java.awt.AWTException e1) {
				e1.printStackTrace();
			}		
	        trayIcon.displayMessage(Vars.appName, string, java.awt.TrayIcon.MessageType.INFO);
			return true;
		} catch (Exception | Error e) {
			return false;
		}
		
        //*/
	}

	public static boolean beep() {
		if(!avalible()) return false;
		try {
			java.awt.Toolkit.getDefaultToolkit().beep();
			return true;
		} catch (Exception | Error e) {
			return false;
		}
	}

}
