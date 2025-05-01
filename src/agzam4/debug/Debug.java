package agzam4.debug;

import arc.scene.Group;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.world.blocks.storage.CoreBlock.CoreBuild;

public class Debug {

	
	public static void init() {
		Vars.mods.getScripts().context.evaluateString(Vars.mods.getScripts().scope,
				"var mod = Vars.mods.getMod(\"agzam4mod\");\n"
				+ "var get = (pkg) => mod.loader.loadClass(pkg).newInstance();\n"
				+ "const AgzamDebug = get(\"agzam4.debug.Debug\")\n"
				+ "const AgzamUI = get(\"agzam4.MobileUI\")", "main.js", 0);

//		Team.sharded.rules().unitBuildSpeedMultiplier = .2f;
//		Team.sharded.rules().unitCostMultiplier = 5f;
//		Team.sharded.rules().unitDamageMultiplier = 5f;
//		Team.sharded.rules().unitHealthMultiplier = 5f;
//		Team.sharded.rules().unitCrashDamageMultiplier = 5f;
//		Groups.unit.each(u -> {if(u.id%5 != 0)u.kill();});
		
		
//		Team.crux.rules().infiniteResources = true;
//		Team.crux.rules().unitCrashDamageMultiplier = .1f;
//		Team.crux.rules().unitHealthMultiplier = .25f;
		
		
//		Vars.state.rules.spawns.clear()

//		Call.setRules(Vars.state.rules);
//		Team.crux.cores().each(c -> c.items.add(Items.fissileMatter, 99999));
//		Vars.state.rules.bannedBlocks.add(Blocks.scorch, Blocks.lancer, Blocks.arc, Blocks.parallax, Blocks.fuse, Blocks.meltdown)
	}
	
}
