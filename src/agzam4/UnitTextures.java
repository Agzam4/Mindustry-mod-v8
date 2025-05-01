package agzam4;

import arc.graphics.g2d.TextureRegion;
import mindustry.type.UnitType;
import mindustry.type.Weapon;

public class UnitTextures {
	
	public static final TextureRegion none = AgzamMod.sprite("none");

    public TextureRegion baseRegion, legRegion, region, previewRegion, shadowRegion, cellRegion, itemCircleRegion,
        softShadowRegion, jointRegion, footRegion, legBaseRegion, baseJointRegion, outlineRegion, treadRegion;

    public TextureRegion[][] weaponsTextures;
    public float[] enginesRadius;

    public UnitType unit;
    
    public UnitTextures(UnitType unit) {
    	this.unit = unit;

    	baseRegion 			= unit.baseRegion;
    	legRegion 			= unit.legRegion;
    	region 				= unit.region;
    	previewRegion 		= unit.previewRegion;
    	shadowRegion 		= unit.shadowRegion;
    	cellRegion 			= unit.cellRegion;
    	itemCircleRegion 	= unit.itemCircleRegion;
    	softShadowRegion 	= unit.softShadowRegion;
    	jointRegion 		= unit.jointRegion;
    	footRegion 			= unit.footRegion;
    	legBaseRegion 		= unit.legBaseRegion;
    	baseJointRegion 	= unit.baseJointRegion;
    	treadRegion 		= unit.treadRegion;

    	weaponsTextures = new TextureRegion[unit.weapons.size][4];
    	
    	for (int i = 0; i < unit.weapons.size; i++) {
			Weapon weapon = unit.weapons.get(i);
			weaponsTextures[i][0] = weapon.region;
			weaponsTextures[i][1] = weapon.heatRegion;
			weaponsTextures[i][2] = weapon.cellRegion;
			weaponsTextures[i][3] = weapon.outlineRegion;
		}
    	
    	enginesRadius = new float[unit.engines.size];
    	for (int i = 0; i < enginesRadius.length; i++) {
    		enginesRadius[i] = unit.engines.get(i).radius;
		}
	}

    public void hideEngines() {
    	for (int i = 0; i < unit.engines.size; i++) {
    		unit.engines.get(i).radius = 0;
		}
    }
    
    public void returnEngines() {
    	for (int i = 0; i < unit.engines.size; i++) {
    		unit.engines.get(i).radius = enginesRadius[i];
		}
    }
    
    public void returnTextures() {
        unit.baseRegion      	= baseRegion;
        unit.legRegion      	= legRegion;
        unit.region          	= region;
        unit.previewRegion   	= previewRegion;
//        unit.shadowRegion    	= shadowRegion;
        unit.cellRegion      	= cellRegion;
        unit.itemCircleRegion	= itemCircleRegion;
//        unit.softShadowRegion	= softShadowRegion;
        unit.jointRegion     	= jointRegion;
        unit.footRegion      	= footRegion;
        unit.legBaseRegion   	= legBaseRegion;
        unit.baseJointRegion 	= baseJointRegion;
        unit.treadRegion     	= treadRegion;
        
    	for (int i = 0; i < unit.weapons.size; i++) {
			Weapon weapon = unit.weapons.get(i);
			weapon.region = 		weaponsTextures[i][0];
			weapon.heatRegion = 	weaponsTextures[i][1];
			weapon.cellRegion = 	weaponsTextures[i][2];
			weapon.outlineRegion = 	weaponsTextures[i][3];
		}
	}
    
    public void hideTextures() {
    	unit.baseRegion 		= none;
    	unit.legRegion 			= none;
    	unit.region 			= none;
    	unit.previewRegion 		= none;
//    	unit.shadowRegion	  	= none;
    	unit.cellRegion 		= none;
    	unit.itemCircleRegion 	= none;
//    	unit.softShadowRegion 	= none;
    	unit.jointRegion 		= none;
    	unit.footRegion 		= none;
    	unit.legBaseRegion 		= none;
    	unit.baseJointRegion 	= none;
    	unit.treadRegion 		= none;
    	
    	for (int i = 0; i < unit.weapons.size; i++) {
			Weapon weapon = unit.weapons.get(i);
			weapon.region = 		none;
			weapon.heatRegion = 	none;
			weapon.cellRegion = 	none;
			weapon.outlineRegion = 	none;
		}
	}
}
