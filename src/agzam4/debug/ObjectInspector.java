package agzam4.debug;

import static arc.Core.app;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import arc.audio.Sound;
import arc.func.Cons2;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Table;
import arc.util.Align;
import mindustry.entities.Effect;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

public class ObjectInspector extends BaseDialog {
	
	private Object object;
    private String searchText = "";
    
    protected Table mainTable;

	public ObjectInspector(Object object) {
		super("");
		this.object = object;
		title.setColor(Color.white);
		titleTable.remove();
		closeOnBack();
		
		mainTable = new Table();
        ScrollPane pane = new ScrollPane(mainTable);
        pane.setFadeScrollBars(false);

        cont.table(table -> {
            table.left();
            table.image(Icon.zoom);
            TextField field = table.field(searchText, res -> {
                searchText = res;
                build();
            }).growX().get();

            shown(() -> {
                field.setText(searchText = "");
                build();
                app.post(field::requestKeyboard);
            });
        }).fillX().padBottom(4).top();

        cont.row();
        cont.add(pane).grow();
        
		build();
	}
	
	private void build() {
		Table t = mainTable;
		
        t.clear();
        t.add().height(10);
        t.row();

        t.add("Object").color(Pal.accent).colspan(4).pad(10).padBottom(4).get().setAlignment(Align.left);
        t.row();
        t.image().color(Pal.accent).fillX().height(3).pad(6).colspan(4).padTop(0).padBottom(10).row();
        t.labelWrap("Object: " + object).growX().wrapLabel(false).row();

        if(object == null) return;

        t.labelWrap("Class: " + object.getClass()).growX().wrapLabel(false).row();


        if(object instanceof Class<?> cls) {
            t.add("Static").color(Pal.accent).colspan(4).pad(10).padBottom(4).get().setAlignment(Align.left);
            t.row();
            t.image().color(Pal.accent).fillX().height(3).pad(6).colspan(4).padTop(0).padBottom(10).row();
            
            for (var field : cls.getFields()) {
        		if(!Modifier.isStatic(field.getModifiers())) continue;
            	if(field.getType() == Sound.class) continue;
        		if(field.getType() == Effect.class) continue;
        		if(!field.getName().contains(searchText)) continue;
        		try {
        			field.setAccessible(true);
        			Object obj = field.get(null);
        			if(obj == null) {
        				t.labelWrap("[white]" + field.getName() + ": [magenta]null").wrapLabel(false).growX().row();
        				continue;
        			}
        			String str = "";
        			str = "[lightgray]" + obj;
        			if(obj instanceof Boolean) str = ((Boolean) obj) ? "[lime]true" : "[red]false";
        			if(obj instanceof TextureRegion) {
        				TextureRegion reg = (TextureRegion) obj;
        				t.button(" " + field.getName(), new TextureRegionDrawable(reg), Styles.grayt, 25f, () -> {
        					new ObjectInspector(obj).show();
        				}).align(Align.left).wrapLabel(false).growX().get().getLabel().setAlignment(Align.left);
        				t.row();
        				continue;
        			}
        			if(obj instanceof Sound) continue;
        			if(obj instanceof Color) str = "[#" + obj.toString() + "]" + obj;
        			t.button(field.getName() + ": " + str, Styles.grayt, () -> {
    					new ObjectInspector(obj).show();
        			}).align(Align.left).wrapLabel(false).growX().get().getLabel().setAlignment(Align.left);
        			t.row();
        		} catch (Error | Exception e) {
//    				t.labelWrap("[white]" + field.getName() + ": [red]" + e.getMessage()).wrapLabel(false).growX().row();
        			continue;
        		}
			}
            
            
        }
        

        t.add("Declared Fields").color(Pal.accent).colspan(4).pad(10).padBottom(4).get().setAlignment(Align.left);
        t.row();
        t.image().color(Pal.accent).fillX().height(3).pad(6).colspan(4).padTop(0).padBottom(10).row();

        Cons2<Field[], Object> displayFields = (fields, src) -> {
        	Arrays.sort(fields, (f1,f2) -> f1.getName().compareTo(f2.getName()));
        	for (Field field : fields) {
        		//							if(field.getType() == TextureRegion.class) continue;
        		if(field.getType() == Sound.class) continue;
        		if(field.getType() == Effect.class) continue;
        		if(!field.getName().contains(searchText)) continue;
        		try {
        			field.setAccessible(true);
        			Object obj = Modifier.isStatic(field.getModifiers()) ? field.get(null) : field.get(src);
        			if(obj == null) {
        				t.labelWrap("[white]" + field.getName() + ": [magenta]null").wrapLabel(false).growX().row();
        				continue;
        			}
        			String str = "";
        			str = "[lightgray]" + obj;
        			if(obj instanceof Boolean) str = ((Boolean) obj) ? "[lime]true" : "[red]false";
        			if(obj instanceof TextureRegion) {
        				TextureRegion reg = (TextureRegion) obj;
        				t.button(" " + field.getName(), new TextureRegionDrawable(reg), Styles.grayt, 25f, () -> {
        					new ObjectInspector(obj).show();
        				}).align(Align.left).wrapLabel(false).growX().get().getLabel().setAlignment(Align.left);
        				t.row();
        				continue;
        			}
        			if(obj instanceof Sound) continue;
        			if(obj instanceof Color) str = "[#" + obj.toString() + "]" + obj;
        			t.button(field.getName() + ": " + str, Styles.grayt, () -> {
    					new ObjectInspector(obj).show();
        			}).align(Align.left).wrapLabel(false).growX().get().getLabel().setAlignment(Align.left);
        			t.row();
        		} catch (Error | Exception e) {
//    				t.labelWrap("[white]" + field.getName() + ": [red]" + e.getMessage()).wrapLabel(false).growX().row();
        			continue;
        		}
        	}
        };
        
        displayFields.get(object.getClass().getDeclaredFields(), object);

        t.add("Fields").color(Pal.accent).colspan(4).pad(10).padBottom(4).get().setAlignment(Align.left);
        t.row();
        t.image().color(Pal.accent).fillX().height(3).pad(6).colspan(4).padTop(0).padBottom(10).row();

        displayFields.get(object.getClass().getFields(), object);

        t.add("Methods").color(Pal.accent).colspan(4).pad(10).padBottom(4).get().setAlignment(Align.left);
        t.row();
        t.image().color(Pal.accent).fillX().height(3).pad(6).colspan(4).padTop(0).padBottom(10).row();

        Cons2<Method[], Object> displayMethods = (methods, src) -> {
        	Arrays.sort(methods, (f1,f2) -> f1.getName().compareTo(f2.getName()));
        	for (Method method : methods) {
        		if(!method.getName().contains(searchText)) continue;
        		try {
        			method.setAccessible(true);
        			if(method.getParameters().length == 0) {
        				t.button(method.getName(), Styles.grayt, () -> {
        					try {
            					new ObjectInspector(method.invoke(object)).show();
        					} catch (Error | Exception e) {
        						e.printStackTrace();
        					}
        				}).align(Align.left).growX().wrapLabel(false).get().getLabel().setAlignment(Align.left);
        				t.row();
        				continue;
        			}
        			t.labelWrap(method.getName()).growX().wrapLabel(false);
        			t.row();
        		} catch (Error | Exception e) {
        			continue;
        		}
        	}
        };

        displayMethods.get(object.getClass().getMethods(), object);
	}
	
}
