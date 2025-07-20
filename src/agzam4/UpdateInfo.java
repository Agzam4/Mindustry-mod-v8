package agzam4;

import java.io.OutputStream;

import agzam4.struct.Boolc2;
import arc.Core;
import arc.files.Fi;
import arc.func.Floatc;
import arc.util.ArcRuntimeException;
import arc.util.Http;
import arc.util.Http.HttpResponse;
import arc.util.Log;
import arc.util.Nullable;
import arc.util.Strings;
import arc.util.io.Streams;
import arc.util.serialization.Jval;
import arc.util.serialization.Jval.JsonArray;
import mindustry.Vars;
import mindustry.mod.Mods.LoadedMod;

public class UpdateInfo {
	
	public static enum CheckUpdatesInterval {
		onServerConnect, onLoad;
		
		public static CheckUpdatesInterval of(@Nullable String name, CheckUpdatesInterval def) {
			if(name == null) return def;
			for (var v : values()) {
				if(v.name().equals(name)) return v;
			}
			return def;
		}
	}
	
	private static CheckUpdatesInterval checkUpdatesInterval = CheckUpdatesInterval.of(ModWork.settingDef("check-updates-interval", null), CheckUpdatesInterval.onServerConnect);
	
	public static boolean isCurrentSessionChecked = false;
	private static float modImportProgress;

	public static CheckUpdatesInterval checkUpdatesInterval() {
		return checkUpdatesInterval;
	}
	
	public static void checkUpdatesInterval(CheckUpdatesInterval cui) {
		checkUpdatesInterval = cui;
		ModWork.setting("check-updates-interval", cui.name());
	}
	
	
	public static void check(Boolc2 onCheck) {
		Log.info("Checking mod updates");
		Http.get(Vars.ghApi + "/repos/" + AgzamMod.getRepo() + "/releases", res -> {
			String version = Vars.mods.getMod("agzam4mod").meta.version;
			if(version == null) version = "1.0";
			
			Jval json = Jval.read(res.getResultAsString());
			JsonArray releases = json.asArray();
			if(releases.size > 0) {
				Jval latest = releases.get(0);
				String ver = latest.getString("tag_name");
				ver = ver.replaceFirst("v", "");
				Log.info("Version: @/@", version, ver);
				boolean old = needUpdate();
				ModWork.setting("needupdate", !version.equalsIgnoreCase(ver));
				onCheck.get(old, needUpdate());
			}
			UpdateInfo.isCurrentSessionChecked = true;
		}, t -> Core.app.post(() -> {}));
	}

	public static String currentName() {
		return AgzamMod.mod.meta.displayName;
	}

	public static String currentVersion() {
		return AgzamMod.mod.meta.version;
	}

	public static boolean needUpdate() {
		return ModWork.settingDef("needupdate", false);
	}

	public static void githubImportMod(String repo, boolean isJava){
        githubImportMod(repo, null);
    }

	public static void githubImportMod(String repo, @Nullable String release){
		modImportProgress = 0f;
		Vars.ui.loadfrag.show("@downloading");
		Vars.ui.loadfrag.setProgress(() -> modImportProgress);

		githubImportJavaMod(repo, release);
	}
	
	private static void githubImportJavaMod(String repo, @Nullable String release){
        //grab latest release
        Http.get(Vars.ghApi + "/repos/" + repo + "/releases/" + (release == null ? "latest" : release), res -> {
        	Jval json = Jval.read(res.getResultAsString());
        	JsonArray assets = json.get("assets").asArray();

            //prioritize dexed jar, as that's what Sonnicon's mod template outputs
            Jval dexedAsset = assets.find(j -> j.getString("name").startsWith("dexed") && j.getString("name").endsWith(".jar"));
            Jval asset = dexedAsset == null ? assets.find(j -> j.getString("name").endsWith(".jar")) : dexedAsset;

            if(asset != null){
                //grab actual file
                String url = asset.getString("browser_download_url");

                Http.get(url, result -> handleMod(repo, result), UpdateInfo::importFail);
            }else{
                throw new ArcRuntimeException("No JAR file found in releases. Make sure you have a valid jar file in the mod's latest Github Release.");
            }
        }, UpdateInfo::importFail);
    }
	
	private static void handleMod(String repo, HttpResponse result){
        try{
            Fi file = Vars.tmpDirectory.child(repo.replace("/", "") + ".zip");
            long len = result.getContentLength();
            Floatc cons = len <= 0 ? f -> {} : p -> modImportProgress = p;

            try(OutputStream stream = file.write(false)){
                Streams.copyProgress(result.getResultAsStream(), stream, len, 4096, cons);
            }

            LoadedMod mod = Vars.mods.importMod(file);
            mod.setRepo(repo);
            file.delete();
            Core.app.post(() -> {
                try{
                    Vars.ui.loadfrag.hide();
                }catch(Throwable e){
                	Vars.ui.showException(e);
                }
            });
        }catch(Throwable e){
            modError(e);
        }
    }
	
    private static void importFail(Throwable t){
        Core.app.post(() -> modError(t));
    }
    
    static void modError(Throwable error){
        Vars.ui.loadfrag.hide();

        if(error instanceof NoSuchMethodError || Strings.getCauses(error).contains(t -> t.getMessage() != null && (t.getMessage().contains("trust anchor") || t.getMessage().contains("SSL") || t.getMessage().contains("protocol")))){
        	Vars.ui.showErrorMessage("@feature.unsupported");
        }else{
        	Vars.ui.showException(error);
        }
    }
}
