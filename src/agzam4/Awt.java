package agzam4;

import mindustry.Vars;

public class Awt {

	public static boolean avalible = false;
	
	public static boolean avalible() {
		return Package.getPackage("java.awt") != null;
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
