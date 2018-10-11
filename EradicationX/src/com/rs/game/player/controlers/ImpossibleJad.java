package com.rs.game.player.controlers;

import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;



import com.rs.Settings;
import com.rs.cores.CoresManager;
import com.rs.game.Animation;
import com.rs.game.RegionBuilder;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.impossiblejad.ImpossibleJadNPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

public class ImpossibleJad extends Controler {

	public static final WorldTile OUTSIDE = new WorldTile(4610, 5130, 0);

	private static final int THHAAR_MEJ_JAL = 2617;

	private static final int[] MUSICS = { 1337, 1338, 1339 };

	public void playMusic() {
		player.getMusicsManager().playMusic(selectedMusic);
	}

	private final int[][] WAVES = { { 2745 }, { 2745, 2745 },
			{ 2745, 2745, 2745 }, { 2745, 2745, 2745, 2745 },
			{ 2745, 2745, 2745, 2745, 2745 } };

	private int[] boundChuncks;
	private Stages stage;
	private boolean logoutAtEnd;
	private boolean login;
	public boolean spawned;
	public int selectedMusic;

	public static void enterFightCaves(Player player) {
		player.getControlerManager()
				.startControler("ImpossibleJadControler", 1); // start at wave 1
	}

	private static enum Stages {
		LOADING, RUNNING, DESTROYING
	}

	@Override
	public void start() {
		loadCave(false);
	}

	@Override
	public boolean processButtonClick(int interfaceId, int componentId,
			int slotId, int packetId) {
		if (stage != Stages.RUNNING)
			return false;
		if (interfaceId == 182 && (componentId == 6 || componentId == 13)) {
			if (!logoutAtEnd) {
				logoutAtEnd = true;
			} else
				player.forceLogout();
			return false;
		}
		return true;
	}

	/**
	 * return process normaly
	 */
	@Override
	public boolean processObjectClick1(WorldObject object) {
		if (object.getId() == 9357) {
			if (stage != Stages.RUNNING)
				return false;
			exitCave(1);
			return false;
		}
		return true;
	}

	/*
	 * return false so wont remove script
	 */
	@Override
	public boolean login() {
		loadCave(true);
		return false;
	}

	public void loadCave(final boolean login) {
		this.login = login;
		stage = Stages.LOADING;
		player.lock(); // locks player
		CoresManager.slowExecutor.execute(new Runnable() {
			@Override
			public void run() {
				// finds empty map bounds
				boundChuncks = RegionBuilder.findEmptyChunkBound(8, 8);
				// copys real map into the empty map
				// 552 640
				RegionBuilder.copyAllPlanesMap(552, 640, boundChuncks[0],
						boundChuncks[1], 8);
				// selects a music
				selectedMusic = MUSICS[Utils.random(MUSICS.length)];
				player.setNextWorldTile(!login ? getWorldTile(37, 29)
						: getWorldTile(37, 29));
				// 1delay because player cant walk while teleing :p, + possible
				// issues avoid
				WorldTasksManager.schedule(new WorldTask() {
					@Override
					public void run() {
						if (!login) {
							WorldTile walkTo = getWorldTile(37, 29);
							player.addWalkSteps(walkTo.getX(), walkTo.getY());
						}
						player.getDialogueManager()
								.startDialogue("SimpleNPCMessage",
										THHAAR_MEJ_JAL,
										"You're on your own now, JalYt.<br>Prepare to fight for your life!");
						player.setForceMultiArea(true);
						playMusic();
						player.unlock(); // unlocks player
						stage = Stages.RUNNING;
					}

				}, 1);
				if (!login) {
					/*
					 * lets stress less the worldthread, also fastexecutor used
					 * for mini stuff
					 */
					CoresManager.fastExecutor.schedule(new TimerTask() {

						@Override
						public void run() {
							if (stage != Stages.RUNNING)
								return;
							try {
								startWave();
							} catch (Throwable t) {
								Logger.handle(t);
							}
						}
					}, 6000);
				}
			}
		});
	}

	public WorldTile getSpawnTile() {
		switch (Utils.random(5)) {
		case 0:
			return getWorldTile(37, 78);
		case 1:
			return getWorldTile(37, 73);
		case 4:
		default:
			return getWorldTile(37, 78);
		}
	}

	@Override
	public void moved() {
		if (stage != Stages.RUNNING || !login)
			return;
		login = false;
		setWaveEvent();
	}

	public void startWave() {
		int currentWave = getCurrentWave();
		if (currentWave > WAVES.length) {
			win();
			return;
		}
		player.getInterfaceManager()
				.sendTab(
						player.getInterfaceManager().hasRezizableScreen() ? 11
								: 0, 316);
		player.getPackets().sendConfig(639, currentWave);
		if (stage != Stages.RUNNING)
			return;
		for (int id : WAVES[currentWave - 1]) {
			new ImpossibleJadNPC(id, getSpawnTile());
		}
		spawned = true;
	}

	public void spawnHealers() {
		if (stage != Stages.RUNNING)
			return;
		for (int i = 0; i < 4; i++)
			new ImpossibleJadNPC(2746, getSpawnTile());
	}

	public void win() {
		if (stage != Stages.RUNNING)
			return;
		exitCave(4);
	}

	public void nextWave() {
		playMusic();
		setCurrentWave(getCurrentWave() + 1);
		if (logoutAtEnd) {
			player.forceLogout();
			return;
		}
		setWaveEvent();
	}

	public void setWaveEvent() {
		if (getCurrentWave() == 5)
			player.getDialogueManager().startDialogue("SimpleNPCMessage",
					THHAAR_MEJ_JAL,
					"Good luck! You wont complete this mohahaha!");
		CoresManager.fastExecutor.schedule(new TimerTask() {

			@Override
			public void run() {
				try {
					if (stage != Stages.RUNNING)
						return;
					startWave();
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}
		}, 600);
	}

	@Override
	public void process() {
		if (spawned) {
			List<Integer> npcs = World.getRegion(player.getRegionId())
					.getNPCsIndexes();
			if (npcs == null || npcs.isEmpty()) {
				spawned = false;
				nextWave();
			}
		}
	}

	@Override
	public boolean sendDeath() {
		player.lock(7);
		player.stopAll();
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0) {
					player.setNextAnimation(new Animation(836));
				} else if (loop == 1) {
					player.getPackets().sendGameMessage(
							"You have been defeated!");
				} else if (loop == 3) {
					player.reset();
					exitCave(1);
					player.setNextAnimation(new Animation(-1));
				} else if (loop == 4) {
					player.getPackets().sendMusicEffect(90);
					stop();
				}
				loop++;
			}
		}, 0, 1);
		return false;
	}

	@Override
	public void magicTeleported(int type) {
		exitCave(2);
	}

	/*
	 * logout or not. if didnt logout means lost, 0 logout, 1, normal, 2 tele
	 */
	public void exitCave(int type) {
		stage = Stages.DESTROYING;
		WorldTile outside = new WorldTile(OUTSIDE, 2); // radomizes alil
		if (type == 0 || type == 2)
			player.setLocation(outside);
		else {
			player.setForceMultiArea(false);
			player.getPackets().closeInterface(
					player.getInterfaceManager().hasRezizableScreen() ? 11 : 0);
			if (type == 1 || type == 4) {
				player.setNextWorldTile(outside);
				if (type == 4) {
					player.reset();
					player.getDialogueManager()
							.startDialogue(
									"SimpleNPCMessage",
									THHAAR_MEJ_JAL,
									"You even defeated Tz Tok-Jad, I am most impressed! Please accept this gift as a reward.");
					if (!player.getInventory().addItem(1055, 1)) {
						World.addGroundItem(new Item(1055, 1), new WorldTile(
								player), player, null, true, 180, true);
						World.addGroundItem(new Item(6529, 16064),
								new WorldTile(player), player, null, true, 180, true);
					} else if (!player.getInventory().addItem(6529, 16064))
						World.addGroundItem(new Item(6529, 16064),
								new WorldTile(player), player, null, true, 180, true);
				} else if (getCurrentWave() == 1)
					player.getDialogueManager().startDialogue(
							"SimpleNPCMessage", THHAAR_MEJ_JAL,
							"Kill the zombies dont let them kill you!");
				else {
					int tokkul = getCurrentWave() * 8032 / WAVES.length;
					tokkul *= Settings.DROP_RATE; // 10x more
					if (!player.getInventory().addItem(6529, tokkul))
						World.addGroundItem(new Item(6529, tokkul),
								new WorldTile(player), player, null, true, 180, true);
					player.getDialogueManager()
							.startDialogue("SimpleNPCMessage", THHAAR_MEJ_JAL,
									"Well done in the cave, here, take TokKul as reward.");
					// TODO tokens
				}
			}
			removeControler();
		}
		/*
		 * 1200 delay because of leaving
		 */
		CoresManager.slowExecutor.schedule(new Runnable() {
			@Override
			public void run() {
				RegionBuilder
						.destroyMap(boundChuncks[0], boundChuncks[1], 8, 8);
			}
		}, 1200, TimeUnit.MILLISECONDS);
	}

	/*
	 * gets worldtile inside the map
	 */
	public WorldTile getWorldTile(int mapX, int mapY) {
		return new WorldTile(boundChuncks[0] * 8 + mapX, boundChuncks[1] * 8
				+ mapY, 0);
	}

	/*
	 * return false so wont remove script
	 */
	@Override
	public boolean logout() {
		/*
		 * only can happen if dungeon is loading and system update happens
		 */
		if (stage != Stages.RUNNING)
			return false;
		exitCave(0);
		return false;

	}

	@Override
	public boolean processMagicTeleport(WorldTile toTile) {
		return false;
	}

	@Override
	public boolean processItemTeleport(WorldTile toTile) {
		return false;
	}

	@Override
	public boolean processObjectTeleport(WorldTile toTile) {
		return false;
	}

	public int getCurrentWave() {
		if (getArguments() == null || getArguments().length == 0)
			return 0;
		return (Integer) getArguments()[0];
	}

	public void setCurrentWave(int wave) {
		if (getArguments() == null || getArguments().length == 0)
			this.setArguments(new Object[1]);
		getArguments()[0] = wave;
	}

	@Override
	public void forceClose() {
		/*
		 * shouldnt happen
		 */
		if (stage != Stages.RUNNING)
			return;
		exitCave(2);
	}
}
