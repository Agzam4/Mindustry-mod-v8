package agzam4.debug;

import static arc.Core.app;

import java.lang.reflect.*;
import java.util.Arrays;

import agzam4.ui.MyScrollPane;
import arc.audio.Sound;
import arc.func.*;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.scene.event.Touchable;
import arc.scene.ui.*;
import arc.scene.ui.layout.Collapser;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Align;
import arc.util.Nullable;
import arc.util.Strings;
import mindustry.entities.Effect;
import mindustry.gen.*;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

public class ObjectInspector extends BaseDialog {
	
	// AgzamDebug.info(Blocks.copperWall)
	
	
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
		MyScrollPane pane = new MyScrollPane(mainTable);
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

        		boolean found = false;
        		for (var prov : fieldTables) {
					if(prov.type.isAssignableFrom(field.getType())) {
						var e = prov.prov.get();
						e.init(field, null);
						t.add(e).growX().row();
						found = true;
						break;
					}
				}
        		if(!found) {
//					t.add("not found: " + field.getType()).growX().row();
					var e = fieldTables.peek().prov.get();
					e.init(field, null);
					t.add(e).growX().row();
					found = true;
        		}
        		
//        		try {
//        			field.setAccessible(true);
//        			Object obj = field.get(null);
//        			if(obj == null) {
//        				t.labelWrap("[white]" + field.getName() + ": [magenta]null").wrapLabel(false).growX().row();
//        				continue;
//        			}
//        			String str = "";
//        			str = "[lightgray]" + obj;
//        			if(obj instanceof Boolean) str = ((Boolean) obj) ? "[lime]true" : "[red]false";
//        			if(obj instanceof TextureRegion) {
//        				TextureRegion reg = (TextureRegion) obj;
//        				t.button(" " + field.getName(), new TextureRegionDrawable(reg), Styles.grayt, 25f, () -> {
//        					new ObjectInspector(obj).show();
//        				}).align(Align.left).wrapLabel(false).growX().get().getLabel().setAlignment(Align.left);
//        				t.row();
//        				continue;
//        			}
//        			if(obj instanceof Sound) continue;
//        			if(obj instanceof Color) str = "[#" + obj.toString() + "]" + obj;
//        			t.button(field.getName() + ": " + str, Styles.grayt, () -> {
//    					new ObjectInspector(obj).show();
//        			}).align(Align.left).wrapLabel(false).growX().get().getLabel().setAlignment(Align.left);
//        			t.row();
//        		} catch (Error | Exception e) {
//        			continue;
//        		}
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
        		
        		boolean found = false;
        		for (var prov : fieldTables) {
					if(prov.type.isAssignableFrom(field.getType())) {
						var e = prov.prov.get();
						e.init(field, src);
						t.add(e).growX().row();
						found = true;
						break;
					}
				}
        		if(!found) {
//					t.add("not found: " + field.getType()).growX().row();
					var e = fieldTables.peek().prov.get();
					e.init(field, src);
					t.add(e).growX().row();
					found = true;
        		}
        		
//        		try {
//        			field.setAccessible(true);
//        			Object obj = Modifier.isStatic(field.getModifiers()) ? field.get(null) : field.get(src);
//        			if(obj == null) {
//        				t.labelWrap("[white]" + field.getName() + ": [magenta]null").wrapLabel(false).growX().row();
//        				continue;
//        			}
//        			String str = "";
//        			str = "[lightgray]" + obj;
//        			if(obj instanceof Boolean) str = ((Boolean) obj) ? "[lime]true" : "[red]false";
//        			if(obj instanceof TextureRegion) {
//        				TextureRegion reg = (TextureRegion) obj;
//        				t.button(" " + field.getName(), new TextureRegionDrawable(reg), Styles.grayt, 25f, () -> {
//        					new ObjectInspector(obj).show();
//        				}).align(Align.left).wrapLabel(false).growX().get().getLabel().setAlignment(Align.left);
//        				t.row();
//        				continue;
//        			}
//        			if(obj instanceof Sound) continue;
//        			if(obj instanceof Color) str = "[#" + obj.toString() + "]" + obj;
//        			t.button(field.getName() + ": " + str, Styles.grayt, () -> {
//    					new ObjectInspector(obj).show();
//        			}).align(Align.left).wrapLabel(false).growX().get().getLabel().setAlignment(Align.left);
//        			t.row();
//        		} catch (Error | Exception e) {
////    				t.labelWrap("[white]" + field.getName() + ": [red]" + e.getMessage()).wrapLabel(false).growX().row();
//        			continue;
//        		}
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
	
	private static Seq<FieldTableProv> fieldTables = Seq.with(
			new FieldTableProv(Iterable.class, IterTableBuilder::new),
			
			// Objects
			new FieldTableProv(Boolean.class, BooleanTableBuilder::new),
			new FieldTableProv(Boolean.TYPE, BooleanTableBuilder::new),

			new FieldTableProv(TextureRegion.class, TextureRegionTableBuilder::new),
			
			// Default
			new FieldTableProv(Object.class, FieldTableBuilder::new)
	);

	private static class FieldTableProv {
		
		public Class<?> type;
		public Prov<? extends FieldTableBuilder<?>> prov;
		
		public FieldTableProv(Class<?> type, Prov<? extends FieldTableBuilder<?>> prov) {
			this.type = type;
			this.prov = prov;
		}
		
	}
	

	private static class FieldTableBuilder<T> extends Table {
		
		public static final float size = 40f;
		
		public FieldTableBuilder() {}
		
		protected Field field = null;
		protected @Nullable Object src = null;
		protected @Nullable T obj = null;
		
		@SuppressWarnings("unchecked")
		public void init(Field field, @Nullable Object src) {
			this.field = field;
			field.setAccessible(true);
			this.src = src;
			try {
				obj = (T) field.get(src);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
			marginLeft(20);
			align(Align.left);
			
			build();
		}
		
		public void set(T value) {
			try {
				field.set(src, value);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		
		public void build() {
			buildZoom();
//			label(() -> field.getName());
			buildName();
			buildValue();
		}
		
		public void buildZoom() {
			var cell = button(Iconc.zoom + "", () -> {
				new ObjectInspector(obj).show();
			}).disabled(obj == null).size(size).padRight(10f);
			
			cell.get().setStyle(Styles.grayt);
		}
		
		public void buildName() {
			label(() -> field.getName() + ": ");
		}
		
		public void buildValue() {
			label(() -> toString(obj)).color(Color.lightGray);
		}
		
		public String toString(Object object) {
			if(object == null) return "[magenta]null[]";
			return object.toString();
		}
	}

	private static class IterTableBuilder extends FieldTableBuilder<Iterable<?>> {
	
		@Override
		public void build() {
			super.build();
		}
		
		@Override
		@SuppressWarnings("unused")
		public void buildValue() {
			if(obj == null) {
				super.buildValue();
				return;
			}
			int count = 0;
			for (var o : obj) count++;
			final int fcount = count;
			
			
//			row();
//			table(g -> {
			
				
	            Table fc = new Table();
	            fc.table(lt -> {
	            	Table s = new Table();
	            	s.touchable = Touchable.childrenOnly;
	            	MyScrollPane scroll = new MyScrollPane(s, Styles.defaultPane);
		            scroll.setOverscroll(false, false);
//		            scroll.touchable = Touchable.childrenOnly;
	            	int index = 0;
		            for (final var o : obj) {
		    			s.button(Iconc.zoom + "", () -> {
		    				new ObjectInspector(o).show();
		    			}).disabled(o == null).size(size).margin(0).pad(0).get().setStyle(Styles.grayt);
		    			s.add(" " + ++index + ". ").color(Pal.accent);
		            	s.add(toString(o)).color(Color.lightGray).align(Align.left).marginRight(15f);
		            	s.row();
					}
		            
		            scroll.fireClick();
//		            s.addListener(new InputListener() {
//		            	
//		            	@Override
//		            	public void enter(InputEvent event, float x, float y, int pointer, Element fromActor) {
//		            		scroll.requestScroll();
//		            	}
//		            	
//		            });
		            
		            lt.add(scroll).grow().fill().pad(0).margin(0).height(size*Math.min(10, fcount));
	            });
	            
	            fc.background(Styles.grayPanel);
	            
	            fc.align(Align.left);
	            fc.setHeight(size);
	            fc.margin(0);
	            
//	            Table ct = new Table();
//	            ct.add(scroll).grow().fill();
	            
	            Collapser coll = new Collapser(fc, true);
	            coll.setDuration(2f);
//	            fc.table(ft -> {
//	            	
//	            	for (var o : obj) {
//	        			ft.label(() -> toString(o)).color(Color.lightGray);
//	        			ft.row();
//					}
////	                ft.left().defaults().left();
//	//
////	                ft.add(Core.bundle.format("bullet.frags", type.fragBullets));
////	                ft.button(Icon.downOpen, Styles.emptyi, () -> coll.toggle(false)).update(i -> i.getStyle().imageUp = (!coll.isCollapsed() ? Icon.upOpen : Icon.downOpen)).size(8).padLeft(16f).expandX();
//	            });
//	            fc.row();
	            
	                     
	            table(ft -> {
//                    ft.left().defaults().left();
                    ft.background(Styles.grayPanel);

                    ft.table(t -> t.add(Strings.format("@ elements", fcount)).center()).grow().fill().center();
//                    ft.label(() -> Strings.format("@ elements", fcount)).grow().fill().center();
//	            	table(t -> {
//	    				t.add(Strings.format("@ elements", fcount)).color(Color.lightGray);
//	    				t.background(Styles.grayPanel);
//	    			}).fill();
                    
//                    ft.add(Core.bundle.format("bullet.frags", type.fragBullets));
                    ft.button(Icon.downOpen, Styles.emptyi, () -> coll.toggle(false))
                    .update(i -> {
                    	i.getStyle().imageUp = (!coll.isCollapsed() ? Icon.upOpen : Icon.downOpen);
                    })// 
                    .padLeft(5f).padRight(5f).size(size);
                }).fill().row();

	            add();add();
	            add(coll).margin(0).pad(0);
	            row();
	            
	            
//	            background(Styles.grayPanel);
//			});
		}
		
	}
	
	private static class BooleanTableBuilder extends FieldTableBuilder<Boolean> {
		
		@Override
		public void build() {
			buildZoom();
			if(obj == null) {
				super.build();
				return;
			}
			check(field.getName(), b -> {
				set(b);
			}).get().setChecked(obj);
		}
		
	}


	private static class TextureRegionTableBuilder extends FieldTableBuilder<TextureRegion> {
		
		@Override
		public void buildValue() {
			if(obj == null) {
				super.buildValue();
				return;
			}
			image(obj).height(size).width(size / obj.ratio());
//			label(() -> toString(obj)).color(Color.lightGray);
		}
		
	}
	
}
