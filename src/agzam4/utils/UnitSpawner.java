package agzam4.utils;

import agzam4.ModWork;
import agzam4.render.MyDraw;
import agzam4.render.Text;
import agzam4.ui.CrossImageButton;
import agzam4.ui.UIUtils;
import arc.Core;
import arc.func.Cons;
import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.event.*;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.scene.ui.ImageButton.ImageButtonStyle;
import arc.scene.ui.TextButton.TextButtonStyle;
import arc.struct.*;
import arc.util.*;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.core.World;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.Units;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.Layer;
import mindustry.type.UnitType;
import mindustry.ui.Fonts;
import mindustry.ui.Styles;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import mindustry.world.blocks.legacy.LegacyBlock;

public class UnitSpawner {

	// TODO: rotate
	
	public static Table mainTable, container;
	public static TextButtonStyle buttonsStyle = Styles.grayt,
			toggleButtonsStyle = new TextButtonStyle(){{
	            font = Fonts.outline;
	            fontColor = Color.white;
	            checked = UIUtils.gray; //
	            down = UIUtils.gray; //
	            up = Styles.black;
	            over = Styles.flatOver;
	            disabled = Styles.black;
	            disabledFontColor = Color.gray;
	        }};
	        
	        
	 public static ImageButtonStyle toggleImageButtonsStyle = new ImageButtonStyle(){{
	            checked = UIUtils.gray; //
	            down = UIUtils.gray; //
	            up = Styles.black;
	            over = Styles.flatOver;
	            disabled = Styles.black;
	        }};;
	
	private static @Nullable UnlockableContent selected = null;
	private static @Nullable Team team = null;
	private static boolean setPlayer;
	private static boolean delayedSpawn;
	private static ObjectMap<Tile, ObjectMap<UnlockableContent, SpawnTarget>> delayedUnits = new ObjectMap<>();
	private static ObjectMap<Tile, SpawnTarget> delayedBlocks = new ObjectMap<>();
	
	static TextButton delayedSpawnButton;
	
	static class SpawnTarget {
		
		UnlockableContent target;
		Position pos;
		int amount = 0;
		Seq<Team> teams = new Seq<>();
		Color blink = new Color(1, 1, 1);
		
		public SpawnTarget(UnlockableContent target, Position pos) {
			this.target = target;
			this.pos = pos;
		}

		public void add(Team team) {
			if(team == null) team = Vars.player.team();
			amount++;
			teams.add(team);
			float r = 0, g = 0, b = 0;
			for (int i = 0; i < teams.size; i++) {
				r += teams.get(i).color.r;
				g += teams.get(i).color.g;
				b += teams.get(i).color.b;
			}
			r /= teams.size;
			g /= teams.size;
			b /= teams.size;
			blink.r(r);
			blink.g(g);
			blink.b(b);
		}
		
		public void run() {
			for (int i = 0; i < amount; i++) {
				spawn(target, pos, teams.get(i));
			}
		}

		public Team team() {
			return teams.size <= 0 ? team : teams.get(0);
		}

		public void team(Team team) {
			if(teams.size <= 0) teams.add(team);
			else teams.set(0, team);
		}

	}
	

	private static Cell<TextButton>[] teamsCells;
	private static boolean eraser;
	private static boolean filterUnits = false, filterBlocks = false;
	
	@SuppressWarnings("unchecked")
	public static void build() {
		mainTable = new Table();
		mainTable.row();
		
		container = mainTable.table(Styles.grayPanel).touchable(Touchable.enabled)
				.width(300) // 300
				.height(500)
				.get();
		applyStyle(container.button(Iconc.cancel + "", buttonsStyle, UnitSpawner::hide));//.row();
		container.row();
		

		Table teamTable = container.table(UIUtils.background).touchable(Touchable.enabled)
				.width(300) // 300
				.height(50)
				.get();
		container.row();
		Table propTable = container.table().touchable(Touchable.enabled)
				.width(300) // 300
				.height(50)
				.get();
		container.row();


		delayedSpawnButton = applyStyle(propTable.button(Iconc.pause + "", toggleButtonsStyle, () -> {
			delayedSpawn = !delayedSpawn;
			if(!delayedSpawn) {
				delayedUnits.each((tile, target) -> {
					target.each((c, t) -> {
						t.run();
					});
				});
				delayedBlocks.each((tile, t) -> spawn(t.target, tile, t.team()));
				delayedUnits.clear();
				delayedBlocks.clear();
			}
			delayedSpawnButton.setText((delayedSpawn ? Iconc.play : Iconc.pause) + "");
		})).get();
		applyStyle(propTable.button(Iconc.players + "", toggleButtonsStyle, () -> {
			setPlayer = !setPlayer;
		}).tooltip(Bungle.dialog("utils.unit-spawn-in")));
		applyStyle(propTable.button(Iconc.eraser + "", toggleButtonsStyle, () -> {
			eraser = !eraser;
		}));

		applyStyle(disableButton(propTable, "units", () -> {
			filterUnits = !filterUnits;
			rebuild.run();
		}).checked(filterBlocks));
		applyStyle(disableButton(propTable, "terrain", () -> {
			filterBlocks = !filterBlocks;
			rebuild.run();
		}).checked(filterBlocks));

		propTable.table(t -> t.touchable = Touchable.disabled).width(50).height(50).margin(0).pad(0);
		
		teamsCells = new Cell[Team.baseTeams.length];
		for (int i = 0; i < Team.baseTeams.length; i++) {
			final int ii = i;
			Team t = Team.baseTeams[i];
			teamsCells[i] = applyStyle(teamTable.button(t.emoji.isEmpty() ? t.localized().substring(0, 1) : t.emoji, toggleButtonsStyle, () -> {
				if(team == t) {
					team = null;
					for (int j = 0; j < teamsCells.length; j++) teamsCells[j].checked(false);
				} else {
					team = t;
					for (int j = 0; j < teamsCells.length; j++) teamsCells[j].checked(j == ii);
				}
			}).color(t.color));
		}

		container.row();
		
		container.setPosition(Core.scene.getWidth(), Core.scene.getHeight());
		
		Core.scene.add(mainTable);
//
		new Dragg(mainTable, mainTable).setPosition(
				ModWork.settingFloat("mobile-payloadspawn-x", 0), 
				ModWork.settingFloat("mobile-payloadspawn-y", 0)
				);
		
		mainTable.visible(() -> visible && ModWork.acceptKeyNoFocus() && avaliable());
		mainTable.exited(() -> Core.scene.setScrollFocus(null));
		
		//ItemSelection.
		buildTable(container, 
			Vars.content.units().select(b -> true).<UnlockableContent>as().add(Vars.content.blocks().select(b -> true).as()),
			() -> selected, 
			b -> {
				selected = b;
			}, 7,7
		);
		
		
	}
	
	public static boolean avaliable() {
		return !(ModWork.isNetGame() && Vars.net.client());
	}
	
	private static Cell<CrossImageButton> disableButton(Table container, String name, Runnable listener) {
		CrossImageButton button = new CrossImageButton(ModWork.drawable(name));
		button.clicked(listener);
		return container.add(button);
		
	}

	private static TextField search;

    public static <T extends UnlockableContent> void buildTable(Table table, Seq<T> items, Prov<T> holder, Cons<T> consumer, int rows, int columns){
        buildTable(table, items, holder, consumer, true, rows, columns);
    }
    static Runnable rebuild;
    
	public static <T extends UnlockableContent> void buildTable(Table table, Seq<T> items, Prov<T> holder, Cons<T> consumer, boolean closeSelect, int rows, int columns){
		ButtonGroup<ImageButton> group = new ButtonGroup<>();
		group.setMinCheckCount(0);
		Table cont = new Table().top();
		cont.defaults().size(40);
		cont.setWidth(300);
		rebuild = () -> {
			group.clear();
			cont.clearChildren();

			String text = search != null ? search.getText() : "";
			int i = 0;

			Seq<T> list = items.select(u -> 
			(text.isEmpty() || u.localizedName.toLowerCase().contains(text.toLowerCase())));
			for(T item : list){
//				if(!item.unlockedNow()) continue;
//				if(item.isHidden()) continue;
				if(filterUnits && item instanceof UnitType) continue;
				if(filterBlocks && item instanceof Block) continue;
				if(item instanceof LegacyBlock) continue;
				if(item.name.equals("block")) continue;
				if(item.name.equals("turret-unit-build-tower")) continue;
				if(!item.generateIcons) continue;
//				if((item instanceof Item && (Vars.state.rules.hiddenBuildItems.contains((Item) item)))) continue; FIXME
				ImageButton button = cont.button(Tex.whiteui, Styles.clearNoneTogglei, Mathf.clamp(item.selectionSize, 0f, 40f), () -> {
					if(closeSelect) Vars.control.input.config.hideConfig();
				}).tooltip(item.localizedName).group(group).get();
				button.changed(() -> consumer.get(button.isChecked() ? item : null));
				button.getStyle().imageUp = new TextureRegionDrawable(item.uiIcon);
				button.update(() -> button.setChecked(holder.get() == item));

				if(i++ % columns == (columns - 1)){
					cont.row();
				}
			}
		};

		Table searchTable = table.table(UIUtils.background).touchable(Touchable.enabled)
				.width(300).height(50)
				.get().background(Styles.black6);
		searchTable.image(Icon.zoom).width(50).height(50).margin(0).pad(0);
		search = searchTable.field(null, text -> rebuild.run()).width(250).height(50).margin(0).pad(0).get();//.padBottom(4).left().growX().width(300).uniformX().get();
		search.setMessageText("@players.search");
		table.row();
		
		Table scrollTable = table.table(UIUtils.background).touchable(Touchable.enabled)
				.width(300).height(300)
				.get().background(Styles.black6);
		rebuild.run();
		
		ScrollPane pane = new ScrollPane(cont, Styles.smallPane);
		pane.setScrollingDisabled(true, false);
		pane.setOverscroll(false, false);
		scrollTable.add(pane).maxHeight(40 * rows).height(40 * rows);
	}

	private static <T extends Button> Cell<T> applyStyle(Cell<T> cell) {
		return cell.width(50).height(50).margin(0).pad(0);
	}
	
	static boolean visible = false;

	public static void show() {
		PlayerUtils.hide();
		visible = true;
	}

	public static void hide() {
		visible = false;
	}
	
	private static void actionAt(@Nullable Tile tile) {
		if(tile == null) return;
		if(eraser) {
			if(delayedSpawn) {
				delayedUnits.remove(tile);
				delayedBlocks.remove(tile);
			} else {
				Units.nearby(tile.worldx(), tile.worldy(), Vars.tilesize, Vars.tilesize, u -> {
					if(u.team != team && team != null) return;
					if(selected != null && selected != u.type) return;
					if(u.tileOn() == tile) u.remove();
				});
				if(tile.build == null) {
					if(selected == null || tile.block() == selected) tile.setNet(Blocks.air);
				} else {
					if(tile.block() == selected && (tile.team() == team || team == null)) tile.setNet(Blocks.air);
				}
				if(selected == null || tile.overlay() == selected) tile.setFloorNet(tile.floor(), Blocks.air);
			}
			return;
		}
		if(delayedSpawn) {
			if(selected instanceof UnitType) {
				ObjectMap<UnlockableContent, SpawnTarget> content = delayedUnits.get(tile);
				if(content == null) {
					content = new ObjectMap<>();
					delayedUnits.put(tile, content);
				}
				SpawnTarget target = content.get(selected);
				if(target == null) {
					target = new SpawnTarget(selected, tile);
					content.put(selected, target);
				}
				target.add(team);
			}
			if(selected instanceof Block) {
				SpawnTarget target = delayedBlocks.get(tile);
				if(target == null) {
					target = new SpawnTarget(selected, tile);
					target.add(team);
					delayedBlocks.put(tile, target);
				}
				target.target = selected;
				target.team(team);
			}
			return;
		}
		spawn(selected, tile, team);		
	}

	public static @Nullable Tile lastTile = null;
	public static void update() {
		if(selected == null && !eraser) return;
		if(Core.scene.hasMouse()) return;
		if(Core.input.keyDown(KeyCode.mouseLeft)) {
			Tile tile = Vars.world.tileWorld(Core.input.mouseWorldX(), Core.input.mouseWorldY());
			if(tile != lastTile) actionAt(tile);
			lastTile = tile;
		} else {
			lastTile = null;
		}

		if(Core.input.keyDown(KeyCode.mouseRight)) {
			selected = null;
		}
	}
	
	static int drawContentIndex = 0;
	static int drawAmount = 0;
	
	public static void draw() {
		Draw.z(Layer.flyingUnit);
		Text.font(Fonts.outline);
		Text.size(1f);
	        
		delayedBlocks.each((pos, t) -> {
			Draw.mixcol(t.blink, 0.4f + Mathf.absin(Time.globalTime, 6f, 0.28f));
			if(t.target.fullIcon.width > t.target.fullIcon.height) Draw.rect(t.target.fullIcon, pos.getX(), pos.getY(), Vars.tilesize, Vars.tilesize/t.target.fullIcon.ratio());	
			else Draw.rect(t.target.fullIcon, pos.getX(), pos.getY(), Vars.tilesize*t.target.fullIcon.ratio(), Vars.tilesize);	
		});
		delayedUnits.each((tile, content) -> {
			Draw.alpha(.75f);
			drawAmount = 0;
			if(content.size <= 1) {
				content.each((c,t) -> {
					drawAmount += t.amount;
					Draw.mixcol(t.blink, 0.4f + Mathf.absin(Time.globalTime, 6f, 0.28f));
					if(t.target.fullIcon.width > t.target.fullIcon.height) Draw.rect(t.target.fullIcon, t.pos.getX(), t.pos.getY(), Vars.tilesize, Vars.tilesize/t.target.fullIcon.ratio());	
					else Draw.rect(t.target.fullIcon, t.pos.getX(), t.pos.getY(), Vars.tilesize*t.target.fullIcon.ratio(), Vars.tilesize);	
				});
			} else {
				drawContentIndex = 0;
				content.each((c,t) -> {
					drawAmount += t.amount;
					Draw.mixcol(t.blink, 0.4f + Mathf.absin(Time.globalTime, 6f, 0.28f));
					if(t.target.fullIcon.width > t.target.fullIcon.height) Draw.rect(t.target.fullIcon, t.pos.getX(), t.pos.getY(), Vars.tilesize, Vars.tilesize/t.target.fullIcon.ratio());	
					else Draw.rect(t.target.fullIcon, t.pos.getX(), t.pos.getY(), Vars.tilesize*t.target.fullIcon.ratio(), Vars.tilesize);	
				drawContentIndex++;
				});
			}
			if(drawAmount > 1) {
				Draw.color();
				Text.at("x" + drawAmount, tile.worldx()+1, tile.worldy(), Align.right | Align.top);
				Text.size(1f);
//				MyDraw.text("x" + drawAmount, tile.getX()+1, tile.getY(), 1, Align.right, Vars.tilesize/2, false);
			}
		});
		Text.size();
        Draw.reset();
	}
	
	private static void spawn(UnlockableContent target, Position position, Team team) {
		if(setPlayer && lastTile != null) return;
		Team t = team == null ? Vars.player.team() : team;
		if(target instanceof UnitType) {
			UnitType ut = (UnitType) target;
			Unit u = ut.spawn(position, t);
			u.rotation(90);
			if(setPlayer) {
				u.spawnedByCore(true);
				Vars.player.unit(u);
			}
			return;
		}
		Tile tile = Vars.world.tile(World.toTile(position.getX()), World.toTile(position.getY()));
		if(target instanceof OverlayFloor) {
			tile.setFloorNet(tile.floor(), (Block) target);
			return;
		}
		if(target instanceof Floor) {
			tile.setFloorNet((Block) target);
			return;
		}
		if(target instanceof Block) {
			Block block = (Block) target;
			for (int r = 0; r < 4; r++) {
				if(!block.canPlaceOn(tile, t, r)) continue;
				if(tile != null) tile.setNet(block, t, r);
				break;
			}
			return;
		}
	}

	static class Dragg {
		
		Element dragger, parent;
		
		protected Vec2 draggStart = null;
		
		public Dragg(Element dragger, Element parent) {
			this.dragger = dragger;
			this.parent = parent;
			
			dragger.addListener(new InputListener() {

				@Override
				public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
					draggStart = new Vec2(x, y);
					return true;
				}

				@Override
				public void touchDragged(InputEvent e, float x, float y, int pointer) {
					if(e.targetActor != mainTable && e.targetActor != container) return;
					update(x, y);
				}

				@Override
				public void touchUp(InputEvent e, float x, float y, int pointer, KeyCode button) {
					if(e.targetActor != mainTable && e.targetActor != container) return;
					update(x, y);
					draggStart = null;

					ModWork.setting("mobile-payloadspawn-x", parent.x);
					ModWork.setting("mobile-payloadspawn-y", parent.y);
				}
			});
		}

		public void update(float x, float y) {
			setPosition(
					Mathf.clamp(parent.x-draggStart.x+x, 
							parent.getPrefWidth()/2f, 
							Core.scene.getWidth() - parent.getPrefWidth()/2f),
					
					Mathf.clamp(parent.y-draggStart.y+y,
							parent.getPrefHeight()/2f, 
							Core.scene.getHeight() - parent.getPrefHeight()/2f));
		}

		public void setPosition(float x, float y) {
			parent.setPosition(
					Mathf.clamp(x, parent.getPrefWidth()/2f, 
							Core.scene.getWidth() - parent.getPrefWidth()/2f),
					Mathf.clamp(y, parent.getPrefHeight()/2f, 
							Core.scene.getHeight() - parent.getPrefHeight()/2f));
		}
	}
}
