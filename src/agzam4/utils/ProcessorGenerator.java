package agzam4.utils;

import agzam4.AgzamMod;
import agzam4.ModWork;
import agzam4.MyDraw;
import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Image;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Nullable;
import arc.util.Time;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.EventType.BlockBuildEndEvent;
import mindustry.game.EventType.TapEvent;
import mindustry.gen.Building;
import mindustry.gen.Call;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Tile;
import mindustry.world.blocks.defense.turrets.ItemTurret.ItemTurretBuild;
import mindustry.world.blocks.logic.LogicBlock;
import mindustry.world.blocks.logic.LogicBlock.LogicBuild;
import mindustry.world.blocks.logic.LogicBlock.LogicLink;
import mindustry.world.blocks.storage.StorageBlock.StorageBuild;
import mindustry.world.blocks.storage.CoreBlock.CoreBuild;

public class ProcessorGenerator {
	
	// TODO: many rows/small checkboxes
	// TODO: remove all select by button
	// TODO: hide hidden items but show they by button

	private static BaseDialog dialog;
	
	private static int buttonsPerRow = 0;
	

	private static final int NONE = -1;
	private static final int MINING = 0;
	private static final int DELIVERY = 1;
	
	private static int selectedType = -1;
	
	private static boolean[] needOre = new boolean[Vars.content.items().size];
	
	static StringBuilder commentMessage = new StringBuilder();

	private static boolean generateComment = true;
	
	public static void draw() {
		if(selectedType == DELIVERY) {
			MyDraw.drawTooltip("[accent]" + ModWork.bungle("dialog.utils.processor-generator.delivery-tap"),
					Core.input.mouseWorldX(), Core.input.mouseWorldY() - MyDraw.textHeight/2f);
			
			Building hover = Vars.world.buildWorld(Core.input.mouseWorldX(), Core.input.mouseWorldY());

			Draw.color(Pal.logicOperations);
			Draw.z(Layer.effect);
			
			boolean isDeliveryBuild = isDeliveryBuild(hover);
			
			
			if(lastTap != null) {
				Tile taped = Vars.world.tile(lastTap);
				
				float size = taped.block().size*Vars.tilesize/2f;
				float size1 = size + Vars.tilesize*Mathf.cosDeg(Time.delta*2f)/2f;
				float size2 = size - Vars.tilesize*Mathf.cosDeg(Time.delta*3f)/2f;
				Lines.stroke(taped.block().size);
				MyDraw.rotatingArcs(taped.build, size1, 1f);
				MyDraw.rotatingArcs(taped.build, size2, -1f);
//				Lines.arc(taped.drawx()*Vars.tilesize, taped.drawy()*Vars.tilesize,
//						taped.block().size*Vars.tilesize/2 + Vars.tilesize/2, 1f);
//
//				Lines.rect(taped.drawx()*Vars.tilesize, taped.drawy()*Vars.tilesize,
//						taped.block().size*Vars.tilesize/2 + Vars.tilesize/2, 
//						taped.block().size*Vars.tilesize/2 + Vars.tilesize/2);

			} 
			if(hover != null) {
				if(isDeliveryBuild && (lastTap == null || hover.pos() != lastTap)) {
					//						Lines.rect(hover.tile.drawx(), hover.tile.drawy(),
					//								hover.block().size*Vars.tilesize/2 + Vars.tilesize/2, 
					//								hover.block().size*Vars.tilesize/2 + Vars.tilesize/2);
					float size = hover.block.size*Vars.tilesize/2f;
//					size += Vars.tilesize*Mathf.cosDeg(AgzamMod.updates)/2f;
					Lines.stroke(hover.block.size);
					MyDraw.rotatingArcs(hover, size, 1f);
//					MyDraw.rotatingArcs(hover, size+Vars.tilesize/2f, -1f);
				}
			}
// TODO
//			if(from != null) {
//				Draw.z(Layer.effect);
//				Lines.stroke(1);
//				
//				Draw.color(Pal.logicOperations);
//				MyDraw.rotatingArcs(from, from.block().size * Vars.tilesize, .1f);
//				
//				if(lastTap != null) {
//					Tile taped = Vars.world.tile(lastTap);
//					Lines.line(from.centerX()*Vars.tilesize, from.centerY()*Vars.tilesize,
//							taped.centerX()*Vars.tilesize, taped.centerY()*Vars.tilesize);
//				} else {
//					if(hover != null) {
//						if(isDeliveryBuild) {
//							float size = hover.block().size*Vars.tilesize/2f;
//							size += Vars.tilesize*Mathf.cos(Time.millis()/100)/2f;
//							MyDraw.rotatingArcs(hover, size, 10f);
//						}
//					}
//				}
//				
//				if(to != null) {
//					Lines.line(from.centerX()*Vars.tilesize, from.centerY()*Vars.tilesize,
//							to.centerX()*Vars.tilesize, to.centerY()*Vars.tilesize);
//				}
//			}
		}
	}
	
	public static void build() {
		dialog = new BaseDialog(ModWork.bungle("dialog.utils.processor-generator"));
		dialog.left();
		dialog.title.setColor(Color.white);
		dialog.closeOnBack();
//		dialog.titleTable.remove();
//		Blocks.payloadSource;
//		ItemSelection
		dialog.cont.pane(Styles.defaultPane,  _p -> {
			_p.defaults().left().pad(15);
			Table p = new Table();
			p.defaults().left().pad(15);
			
			Table t = new Table();
			t.defaults().pad(10);
			
			addCategory(t, "mining");
			buttonsPerRow = 0;
			Vars.content.units().each(u -> {
				if(u.hidden) return;
				if(u.mineTier < 0) return;
				
				addUnitButton(t, u, () -> {
					selectedType = MINING;
//					hide();
					BaseDialog oresDialog = new BaseDialog(ModWork.bungle("dialog.ores"));

					oresDialog.title.setColor(Color.white);
					oresDialog.closeOnBack();
					oresDialog.cont.pane(op -> {
						op.defaults().left().pad(5);
						
//						Table ot = new Table();
						
						Vars.content.items().each(i -> {
							if(!Vars.indexer.hasOre(i)) return;
							if(u.mineTier < i.hardness) return;
							op.check(i.emoji() + " " + i.localizedName, needOre[i.id], b -> {
								needOre[i.id] = b;
							}).row();
						});
						
						op.button("@ok", () -> {
							boolean selected = false;
							for (int i = 0; i < needOre.length; i++) {
								if(needOre[i]) selected = true;
							}
							if(selected) {
								commentMessage = new StringBuilder();
								addCode(createMineCode(u), commentMessage.toString(), new Seq<>());
								hide();
								oresDialog.hide();
							}
						});
					});
					
					oresDialog.show();
				}).wrapLabel(false);
				if(buttonsPerRow >= 3) {
					buttonsPerRow = 0;
					t.row();
				}
			});
			
			t.row();
			addCategory(t, "delivery");
			buttonsPerRow = 0;
//			UnitTypes
			Vars.content.units().copy()
			.removeAll(u -> (u.hidden || u.itemCapacity <= 0))
			.sort(u -> u.itemCapacity*u.speed*u.boostMultiplier)
			.each(u -> {
				if(!(u.canBoost || u.flying || u.allowLegStep)) return;
				addUnitButton(t, u, () -> {
					selectedType = DELIVERY;
					carrier = u;
					Log.info("Selecting delivery");
					to = null;
					hide();
				}).wrapLabel(false).tooltip("[accent]" + ModWork.round(u.itemCapacity*u.boostMultiplier*u.speed*.6f/Vars.tilesize) + "[white] items/sec on 100 tiles");
				//.row();
				
				if(buttonsPerRow >= 3) {
					buttonsPerRow = 0;
					t.row();
				}
			});
			t.row();
			p.add(t).row();

			// TODO
			
			Table st = new Table();
			st.defaults().pad(10).growX();
			addCategory(st, "settings");
			st.check(ModWork.bungle("dialog.utils.processor-generator.generate-comments"), true, b -> {
				generateComment = b;
			}).growX().row();
			p.add(st).row();
			
			_p.add(p).row();
			//			t.  .growX().pad(10).padBottom(4)
		}).padRight(10).scrollX(false);
		
		Events.on(BlockBuildEndEvent.class, e -> {
			if(e.tile.block() == Blocks.microProcessor) {
				Building building = e.tile.build;
				if(building == null) return;
				if(building.team != Vars.player.team()) return;
				if(building instanceof LogicBuild) {
					LogicBuild build = (LogicBuild) building;
					String lines[] = build.code.split("\n");
					Seq<LogicLink> links = new Seq<>();
					StringBuilder newCode = new StringBuilder();
					for (int i = 0; i < lines.length; i++) {
						String line = lines[i];
						if(line.startsWith("set agzamModLink")) {
							String[] data = line.replaceAll("\"", "").split(" ");
							if(data.length == 4) {
								try {
									int x = Integer.parseInt(data[2])-build.tileX();
									int y = Integer.parseInt(data[3])-build.tileY();
									links.add(new LogicLink(x, y, "agzamMod-autolink-" + (links.size+1), true));
								} catch (NumberFormatException nfe) {
									Log.err(nfe);
								}
							}
						} else {
							newCode.append(line);
							newCode.append('\n');
						}
					}
					if(links.size > 0) Call.tileConfig(Vars.player, building, LogicBlock.compress(newCode.toString(), links));
				}
			}
		});
		
		Events.on(TapEvent.class, e -> {
			if(e.player != Vars.player) return;
//			Log.info("TAP! @", selectedType);
			if(selectedType == DELIVERY) {
				if(!isDeliveryBuild(e.tile.build)) return;
				
				if(lastTap != null) {
					if(lastTap.intValue() == e.tile.pos()) {
						if(to != e.tile) {
							to = e.tile;
							selectedType = NONE;
//							if(from.build == null) return;
							if(to.build == null) return;
							if(carrier == null) return;
							link = new LogicLink(to.centerX(), to.centerY(),
									"agzamMod-delivery-autolink", false);
							Seq<ItemStack> items = ModWork.getMaximumAcceptedConsumers(to.block());
							if(items.size == 0) return;
							boolean[] selected = new boolean[Vars.content.items().size];
							for (int i = 0; i < selected.length; i++) {
								for (int j = 0; j < items.size; j++) {
									if(items.get(j).item.id == i) {
										selected[i] = true;
										break;
									}
								}
							}
							
							BaseDialog deliveryItems = new BaseDialog(ModWork.bungle("dialog.delivery-items"));

							deliveryItems.title.setColor(Color.white);
							deliveryItems.closeOnBack();
							deliveryItems.cont.pane(op -> {
								op.defaults().left().pad(5);
//								UnitTypes.mono.lightColor
								items.each(i -> {
									op.check(i.item.emoji() + " " + i.item.localizedName, true, b -> {
										selected[i.item.id] = b;
									}).row();
								});
								
								op.button("@ok", () -> {
									boolean hasSelected = false;
									for (int i = 0; i < selected.length; i++) {
										if(selected[i]) {
											hasSelected = true;
											break;
										}
									}
									if(hasSelected) {
										Seq<ItemStack> selectedItems = new Seq<>();
										for (int i = 0; i < items.size; i++) {
											if(selected[items.get(i).item.id]) {
												selectedItems.add(items.get(i));
											}
										}
										commentMessage = new StringBuilder();
										addCode(createDeliveryCode(carrier, to.build, selectedItems), 
												commentMessage.toString(), new Seq<>(new LogicLink[]{link}));
										hide();
										deliveryItems.hide();
									}
								});
							});
							deliveryItems.show();
						}
					}
				}
				lastTap = e.tile.pos();
				return;
			}
			lastTap = null;
			return;
		});
	}
	private static LogicLink link;
	
	private static boolean isDeliveryBuild(@Nullable Building build) {
		if(build == null) return false;
		if(build.team() != Vars.player.team()) return false;
		if(build.block.itemCapacity <= 0) return false;
		if(!build.block.acceptsItems) return false;
		if(build instanceof CoreBuild) return false;
		if(build instanceof StorageBuild) {
			if(((StorageBuild) build).linkedCore != null) return false;
		}
		return true;
	}

	private static String createDeliveryCode(UnitType carrier, Building to, Seq<ItemStack> deliveryItems) { // TODO
		
		Code code = new Code();
		code.set("#Attempts", "0");
		code.markLast("mark-bind");
		code.ubind(carrier);
		code.markLast("mark-bind-bind");

		code.sum("#Attempts", "#Attempts", "1");
		code.uFlag("#Flag");
		code.jump("equal #Flag 0 ", +3);
		code.jump("lessThan " + Vars.player.team().data().unitCap*2 + " #Attempts", +2);
		code.jump("notEqual #Flag " + to.pos(), "mark-bind-bind");
		
		code.uSetFlag(to.pos());	
//		Seq<Item> deliveryItems = new Seq<Item>();
//		if(!(to instanceof StorageBuild)) {
//			if(to.block.consumers != null) {
//				for (int c = 0; c < to.block.consumers.length; c++) {
//					ModWork.consumeItems(to.block.consumers[c], to, 0, (item, ips) -> {
//						if(!deliveryItems.contains(item)) deliveryItems.addUnique(item);
//					});
//				}
//			}
			code.ulocateCore();
			code.markLast("mark-start");
			
			code.jump("equal @unit null", "mark-bind");
			code.getLink("#Building", 0);
			code.uItemStack();
			if(carrier.canBoost) code.boost(true);
			
			for (int i = 0; i < deliveryItems.size; i++) {
				if(to instanceof ItemTurretBuild) {
//					code.sensorAmmo("#DeliveryItems", "#Building");
					code.uSensorItem("#UnitItem", deliveryItems.get(i).item);
					code.sensorItems("#CoreItems", "#Core", deliveryItems.get(i).item);
					code.sum("#CoreAndUnitItems", "#CoreItems", "#UnitItem");
					code.set("#DeliveryItem", deliveryItems.get(i).item);
					code.jump("lessThan 0 #CoreAndUnitItems", "mark-delivery");
				} else {
					code.sensorItems("#DeliveryItems", "#Building", deliveryItems.get(i).item);
					code.set("#DeliveryItem", deliveryItems.get(i).item);
					code.jump("lessThan #DeliveryItems " + deliveryItems.get(i).amount, "mark-delivery");
				}
				//  ModWork.getMaximumAccepted(to.block, deliveryItems.get(i))
			}
			code.jump("always 0 0", "mark-start");
			code.jump("lessThanEq #Items 0", "mark-takeItems");
			code.markLast("mark-delivery");
			code.uSensorItems("#UnitItem");
			code.jump("notEqual #UnitItem #DeliveryItem", "mark-dropWrong");
			
			code.approachTo(to.tileX(), to.tileY());
			code.dropItems("#Building");
			code.jump("always 0 0", "mark-start");

			code.approachToCore();
			code.markLast("mark-dropWrong");
			code.dropItems("#Core");
			code.jump("always 0 0", "mark-start");
			
			code.approachToCore();
			code.markLast("mark-takeItems");
			code.takeItems("#Core", "#DeliveryItem");
			code.jump("always 0 0", "mark-start");
//		}

			commentMessage.append("[gold]Auto generated delivery processor[]\n");
			commentMessage.append("[accent]Unit: []" + carrier.emoji() + " " + carrier.localizedName);
			commentMessage.append("\n[accent]Items: []");
			for (int i = 0; i < deliveryItems.size; i++) {
				if(i != 0) commentMessage.append(", ");
				commentMessage.append(deliveryItems.get(i).item.emoji());
			}
			commentMessage.append("\n[lightgray]Agzam's mod");
		
		code.set("#Link", "\"" + to.tileX() + " " + to.tileY() + "\"");
		return code.toString();
	}

	private static void addCode(String code, String comment, Seq<LogicLink> links) {
		Vars.control.input.useSchematic(Code.createBuildPlan(code, comment, links, generateComment));		
	}

	private static String createMineCode(UnitType type) {
		Code code = new Code();
		code.ubind(type);
		code.ulocateCore();
		code.uItemStack();
		if(type.canBoost) code.boost(true);
		code.jump("equal #Items #ItemsCapacity", "mark-toCore");
//		code.jump("greaterThan #Items 0", "mark-mine");
		
		Seq<Item> ores = new Seq<>();
		for (int i = 0; i < needOre.length; i++) {
			Item item = Vars.content.item(i);
			if(needOre[i] && type.mineTier >= item.hardness) ores.add(item);
		}
		if(ores.size <= 0) return "";

		code.set("#mineItem", ores.get(0));
		code.sensorItems("#minCount", ores.get(0));
		for (int i = 1; i < ores.size; i++) {
			code.sensorItems("#ItemsCount", ores.get(i));
			code.jump("greaterThan #ItemsCount #minCount", +3);
			code.set("#mineItem", ores.get(i));
			code.set("#minCount", "#ItemsCount");
		}
		code.uSensorItems("#unitItem");
		code.jump("equal #unitItem null", +2);
		code.jump("notEqual #unitItem #mineItem", "mark-toCore");
		code.ulocateOre("#mineItem");
		code.approachAndMine();
		code.end();

		code.markLast("mark-mine");
		code.ulocateOre("#mineItem");
		code.approachAndMine();
		code.end();
		
		if(type.flying) {
			code.approachToCore(); // TODO: pathfind
		} else {
			code.pathfindToCore();

		}
		code.markLast("mark-toCore");
		code.itemDrop("#Core");

		commentMessage.append("[gold]Auto generated mine processor[]\n");
		commentMessage.append("[accent]Unit: []" + type.emoji() + " " + type.localizedName);
		commentMessage.append("\n[accent]Items: []");
		for (int i = 0; i < ores.size; i++) {
			if(i != 0) commentMessage.append(", ");
			commentMessage.append(ores.get(i).emoji());
		}
		commentMessage.append("\n[lightgray]Agzam's mod");
		
		String _code = code.toString();
//        Core.app.setClipboardText(_code);
		return _code;
	}

	private static Tile to = null;
	private static UnitType carrier = null;
	
	private static Integer lastTap = null;
	
	private static void hide() {
		PlayerUtils.hide();
		dialog.hide();
	}

	public static void show() {
		dialog.show();
	}
	
	private static Cell<TextButton> addUnitButton(Table table, UnitType type, Runnable onClick) {
		buttonsPerRow++;
		TextureRegionDrawable image = new TextureRegionDrawable(type.uiIcon);
		TextButton button = new TextButton(type.localizedName);
		if(type.uiIcon.width > type.uiIcon.height) {
	        button.add(new Image(image)).size(20, 20 * type.uiIcon.height / type.uiIcon.width).pad(0, 0, 0, 7);
		} else {
	        button.add(new Image(image)).size(20 * type.uiIcon.width / type.uiIcon.height, 20).pad(0, 0, 0, 7);
		}
        button.getCells().reverse();
        button.getLabelCell().expand(0, 0).fill(false);
        button.clicked(onClick);
        return table.add(button).growX().pad(10).padBottom(4).fillX();
        
//		PlacementFragment
//		return table.button(, , 20, onClick)
//				.growX().pad(10).padBottom(4).fillX();
	}

	private static void addCategory(Table table, String category) {
        table.add(ModWork.bungle("dialog.category." + category)).color(Pal.accent).colspan(4).pad(10).padBottom(4).growX().row();
		table.image().color(Pal.accent).fillX().height(3).pad(6).colspan(4).padTop(0).padBottom(10).growX().row();		
	}

}
