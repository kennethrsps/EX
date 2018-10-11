package com.rs.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import com.rs.Launcher;
import com.rs.Settings;
import com.rs.content.utils.IPMute;
import com.rs.cores.CoresManager;
import com.rs.net.decoders.GrabPacketsDecoder;
import com.rs.game.item.FloorItem;
import com.rs.game.item.Item;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.game.tasks.WorldTask;
import com.rs.game.minigames.DamageDummy;
import com.rs.game.minigames.GodWarsBosses;
import com.rs.game.minigames.ZarosGodwars;
import com.rs.game.minigames.clanwars.FfaZone;
import com.rs.game.minigames.clanwars.RequestController;
import com.rs.game.minigames.duel.DuelControler;
import com.rs.game.npc.Dummy;
import com.rs.game.npc.NPC;
import com.rs.game.npc.corp.CorporealBeast;
import com.rs.game.npc.dragons.KingBlackDragon;
import com.rs.game.npc.godwars.GodWarMinion;
import com.rs.game.npc.godwars.armadyl.GodwarsArmadylFaction;
import com.rs.game.npc.godwars.armadyl.KreeArra;
import com.rs.game.npc.godwars.bandos.GeneralGraardor;
import com.rs.game.npc.godwars.bandos.GodwarsBandosFaction;
import com.rs.game.npc.godwars.saradomin.CommanderZilyana;
import com.rs.game.npc.godwars.saradomin.GodwarsSaradominFaction;
import com.rs.game.npc.godwars.zammorak.GodwarsZammorakFaction;
import com.rs.game.npc.godwars.zammorak.KrilTstsaroth;
import com.rs.game.npc.godwars.zaros.Nex;
import com.rs.game.npc.godwars.zaros.NexMinion;
import com.rs.game.npc.hati.Hati;
import com.rs.game.npc.kalph.KalphiteQueen;
import com.rs.game.npc.nomad.FlameVortex;
import com.rs.game.npc.nomad.Nomad;
import com.rs.game.player.content.EXInvasion;
import com.rs.game.npc.others.Bork;
import com.rs.game.npc.others.DZBosses;
import com.rs.game.npc.others.ItemHunterNPC;
import com.rs.game.npc.others.LivingRock;
import com.rs.game.npc.others.Lucien;
import com.rs.game.npc.others.Thunder;
import com.rs.game.npc.others.Nub;
import com.rs.game.npc.others.Nub1;
import com.rs.game.npc.others.Nub2;
import com.rs.game.npc.others.Nub3;
import com.rs.game.npc.others.Nub4;
import com.rs.game.npc.others.Nub5;
import com.rs.game.npc.others.Nub6;
import com.rs.game.npc.others.Nub7;
import com.rs.game.npc.others.Nub8;
import com.rs.game.npc.others.Nub9;
import com.rs.game.npc.others.Nub10;
import com.rs.game.npc.others.Nub11;
import com.rs.game.npc.others.Nub12;
import com.rs.game.npc.others.Nub13;
import com.rs.game.npc.others.Nub15;
import com.rs.game.npc.others.NPCevent;
import com.rs.game.npc.others.Vampyre;
import com.rs.game.npc.others.MasterOfFear;
import com.rs.game.npc.others.MentiosManager;
import com.rs.game.npc.others.MercenaryMage;
import com.rs.game.npc.others.PestMonsters;
import com.rs.game.npc.others.Revenant;
import com.rs.game.npc.others.TormentedDemon;
import com.rs.game.npc.slayer.Strykewyrm;
import com.rs.game.npc.slayer.GanodermicBeast;
import com.rs.game.npc.sorgar.Elemental;
import com.rs.game.player.OwnedObjectManager;
import com.rs.game.player.Player;
import com.rs.game.player.Raffle;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.BoxAction.HunterNPC;
import com.rs.game.player.content.ItemConstants;
import com.rs.game.player.content.LivingRockCavern;
import com.rs.game.player.content.clans.ClansManager;
import com.rs.game.player.content.custom.BossHighlight;
import com.rs.game.player.content.custom.InvasionManager;
import com.rs.game.player.content.custom.PriceManager;
import com.rs.game.player.content.custom.TriviaBot;
import com.rs.game.player.controlers.Wilderness;
import com.rs.rss.RSSFeedParser;
import com.rs.utils.AntiFlood;
import com.rs.utils.DummyRank;
import com.rs.utils.IPBanL;
import com.rs.utils.Logger;
import com.rs.utils.PkRank;
import com.rs.utils.RaffleManager;
import com.rs.utils.ShopsHandler;
import com.rs.utils.Utils;

public final class World {

	public static int exiting_delay;
	public static long exiting_start;

	private static final EntityList<Player> players = new EntityList<Player>(
			Settings.PLAYERS_LIMIT);

	private static final EntityList<NPC> npcs = new EntityList<NPC>(
			Settings.NPCS_LIMIT);
	private static final Map<Integer, Region> regions = Collections
			.synchronizedMap(new HashMap<Integer, Region>());

	// private static final Object lock = new Object();

	public static final void init() {
		// addLogicPacketsTask();
		readForums();
		TriviaBot.getInstance().start();
		BossHighlight.getInstance().start();
		addRestoreRunEnergyTask();
		addDrainPrayerTask();
		addRestoreHitPointsTask();
		addRestoreSkillsTask();
		addRestoreSpecialAttackTask();
		addRestoreShopItemsTask();
		addSummoningEffectTask();
		addOwnedObjectsTask();
		LivingRockCavern.init();
		addListUpdateTask();	
		spawnInvasion();
		drawRaffle();
		addResetTempBannedIpsTask();
	}
	
	private static void addResetTempBannedIpsTask() {
		CoresManager.slowExecutor.scheduleWithFixedDelay(new Runnable() {
		
			@Override
			public void run() {
				GrabPacketsDecoder.TEMP_BLOCKED_IPS.clear();
			}
			
		}, 0, 10, TimeUnit.MINUTES);
	}
	
	private static final EntityList<Player> lobbyPlayers = new EntityList<Player>(Settings.PLAYERS_LIMIT);

	
	public static final Player getLobbyPlayerByDisplayName(String username) {
		String formatedUsername = Utils.formatPlayerNameForDisplay(username);
		for (Player player : getLobbyPlayers()) {
			if (player == null) {
				continue;
			}
			if (player.getUsername().equalsIgnoreCase(formatedUsername)
					|| player.getDisplayName().equalsIgnoreCase(formatedUsername)) {
				return player;
			}
		}
		return null;
	}

	public static final EntityList<Player> getLobbyPlayers() {
		return lobbyPlayers;
	}
		
	public static final void addPlayer(Player player) {
		players.add(player);
		if (World.containsLobbyPlayer(player.getUsername())) {
			World.removeLobbyPlayer(player);
			AntiFlood.remove(player.getSession().getIP());
		}
		AntiFlood.add(player.getSession().getIP());
	}

	public static final void addLobbyPlayer(Player player) {
		lobbyPlayers.add(player);
		AntiFlood.add(player.getSession().getIP());
	}

	public static final boolean containsLobbyPlayer(String username) {
		for (Player p2 : lobbyPlayers) {
			if (p2 == null) {
				continue;
			}
			if (p2.getUsername().equalsIgnoreCase(username)) {
				return true;
			}
		}
		return false;
	}

	public static void removeLobbyPlayer(Player player) {
		for (Player p : lobbyPlayers) {
			if (p.getUsername().equalsIgnoreCase(player.getUsername())) {
				if (player.getCurrentFriendChat() != null) {
					player.getCurrentFriendChat().leaveChat(player, true);
				}
				lobbyPlayers.remove(p);
			}
		}
		AntiFlood.remove(player.getSession().getIP());
	}

	public static void removePlayer(Player player) {
		for (Player p : players) {
			if (p.getUsername().equalsIgnoreCase(player.getUsername())) {
				players.remove(p);
			}
		}
		AntiFlood.remove(player.getSession().getIP());
	}			

	private static final void addListUpdateTask() {
		CoresManager.fastExecutor.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					for (Player player : getPlayers()) {
						if (player == null || player.isDead()
								|| !player.isRunning())
							continue;
						player.getPackets().sendIComponentText(751, 16, "    Players     <col=00ff00>" + getPlayers().size());
						player.sendDefaultPlayersOptions();
					}
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}
		}, 0, 110);
	}

	/*
	 * private static void addLogicPacketsTask() {
	 * CoresManager.fastExecutor.scheduleAtFixedRate(new TimerTask() {
	 * 
	 * @Override public void run() { for(Player player : World.getPlayers()) {
	 * if(!player.hasStarted() || player.hasFinished()) continue;
	 * player.processLogicPackets(); } }
	 * 
	 * }, 300, 300); }
	 */

	private static void addOwnedObjectsTask() {
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				try {
					OwnedObjectManager.processAll();
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}
		}, 0, 1);
	}

	private static void addRestoreShopItemsTask() {
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				try {
					ShopsHandler.restoreShops();
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}
		}, 0, 30);
	}

	private static final void addSummoningEffectTask() {
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				try {
					for (Player player : getPlayers()) {
						if (player == null || player.getFamiliar() == null
								|| player.isDead() || !player.hasFinished())
							continue;
						if (player.getFamiliar().getOriginalId() == 6814) {
							player.heal(20);
							player.setNextGraphics(new Graphics(1507));
						}
					}
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}
		}, 0, 15);
	}

	private static final void addRestoreSpecialAttackTask() {

		CoresManager.fastExecutor.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					for (Player player : getPlayers()) {
						if (player == null || player.isDead()
								|| !player.isRunning())
							continue;
						player.getCombatDefinitions().restoreSpecialAttack();
					}
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}
		}, 0, 30000);
	}

	private static boolean checkAgility;


	private static final void addRestoreRunEnergyTask() {
		CoresManager.fastExecutor.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					for (Player player : getPlayers()) {
						if (player == null
								|| player.isDead()
								|| !player.isRunning()
								|| (checkAgility && player.getSkills()
										.getLevel(Skills.AGILITY) < 70))
							continue;
						player.restoreRunEnergy();
					}
					checkAgility = !checkAgility;
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}
		}, 0, 1000);
	}
	
	/**
	 * Spawns the Zombie Invasion everyday.
	 */
	public static final void spawnInvasion() {

		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				if (InvasionManager.isInvasionTimeNight()) {
					EXInvasion.spawnDailyInvasion();
				}
				if (InvasionManager.isInvasionTimeMorning()) {
					EXInvasion.spawnDailyInvasion();
				}
				if (InvasionManager.isInvasionTimeNight2()) {
					EXInvasion.spawnDailyInvasion();
				}
				if (InvasionManager.isInvasionTimeMorning2()) {
					EXInvasion.spawnDailyInvasion();
				}
			}
		}, 0, 1);
	}

	public static final void drawRaffle() {

		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				if (RaffleManager.isDrawingTime()) {
					Raffle.drawRaffle();
				}
				for (Player player : getPlayers()) {
					if (player.antiNull >= 0)
						player.antiNull--;
				}
			}
		}, 0, 1);
	}	
	
	public static final void readForums() {
		CoresManager.slowExecutor.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				Settings.parser = new RSSFeedParser("http://eradicationx.com/forums/index.php?/forum/32-server-updates.xml");
				Settings.feed = Settings.parser.readFeed();
				System.out.println("Refreshed parsing unit for forums.");
			}
			
		}, 0, 10, TimeUnit.MINUTES);
	}

	
	private static final void addDrainPrayerTask() {
		CoresManager.fastExecutor.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					for (Player player : getPlayers()) {
						if (player == null || player.isDead()
								|| !player.isRunning())
							continue;
						player.getPrayer().processPrayerDrain();
					}
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}
		}, 0, 600);
	}

	private static final void addRestoreHitPointsTask() {
		CoresManager.fastExecutor.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					for (Player player : getPlayers()) {
						if (player == null || player.isDead()
								|| !player.isRunning())
							continue;
						player.restoreHitPoints();
					}
					for (NPC npc : npcs) {
						if (npc == null || npc.isDead() || npc.hasFinished())
							continue;
						npc.restoreHitPoints();
					}
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}
		}, 0, 6000);
	}

	private static final void addRestoreSkillsTask() {
		CoresManager.fastExecutor.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					for (Player player : getPlayers()) {
						if (player == null || !player.isRunning())
							continue;
						int ammountTimes = player.getPrayer().usingPrayer(0, 8) ? 2
								: 1;
						if (player.isResting())
							ammountTimes += 1;
						boolean berserker = player.getPrayer()
								.usingPrayer(1, 5);
						for (int skill = 0; skill < 25; skill++) {
							if (skill == Skills.SUMMONING)
								continue;
							for (int time = 0; time < ammountTimes; time++) {
								int currentLevel = player.getSkills().getLevel(
										skill);
								int normalLevel = player.getSkills()
										.getLevelForXp(skill);
								if (currentLevel > normalLevel) {
									if (skill == Skills.ATTACK
											|| skill == Skills.STRENGTH
											|| skill == Skills.DEFENCE
											|| skill == Skills.RANGE
											|| skill == Skills.MAGIC) {
										if (berserker
												&& Utils.getRandom(100) <= 15)
											continue;
									}
									player.getSkills().set(skill,
											currentLevel - 1);
								} else if (currentLevel < normalLevel)
									player.getSkills().set(skill,
											currentLevel + 1);
								else
									break;
							}
						}
					}
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}
		}, 0, 30000);

	}

	public static final Map<Integer, Region> getRegions() {
		// synchronized (lock) {
		return regions;
		// }
	}

	public static final Region getRegion(int id) {
		return getRegion(id, false);
	}

	public static final Region getRegion(int id, boolean load) {
		// synchronized (lock) {
		Region region = regions.get(id);
		if (region == null) {
			region = new Region(id);
			regions.put(id, region);
		}
		if (load)
			region.checkLoadMap();
		return region;
		// }
	}

	public static final void addNPC(NPC npc) {
		npcs.add(npc);
	}

	public static final void removeNPC(NPC npc) {
		npcs.remove(npc);
	}

	public static final NPC spawnNPC(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		NPC n = null;
		HunterNPC hunterNPCs = HunterNPC.forId(id);
		if (hunterNPCs != null) {
			if (id == hunterNPCs.getNpcId())
			n = new ItemHunterNPC(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		} else if (id == 6142 || id == 6144 || id == 6145 || id == 6143)
			n = new PestMonsters(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		else if (id >= 5533 && id <= 5558)
			n = new Elemental(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		else if (id == 7134)
			n = new Bork(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		else if (id == 9441)
            n = new FlameVortex(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
			else if (id >= 8832 && id <= 8834)
			n = new LivingRock(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		else if (id >= 13465 && id <= 13481)
			n = new Revenant(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		else if (id == 1158 || id == 1160)
			n = new KalphiteQueen(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		else if (id >= 8528 && id <= 8532)
			n = new Nomad(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		else if (id == 6215 || id == 6211 || id == 3406 || id == 6216
				|| id == 6214 || id == 6215 || id == 6212 || id == 6219
				|| id == 6221 || id == 6218)
			n = new GodwarsZammorakFaction(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		else if (id == 6254 && id == 6259)
			n = new GodwarsSaradominFaction(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		else if (id == 6246 || id == 6236 || id == 6232 || id == 6240
				|| id == 6241 || id == 6242 || id == 6235 || id == 6234
				|| id == 6243 || id == 6236 || id == 6244 || id == 6237
				|| id == 6246 || id == 6238 || id == 6239 || id == 6230)
			n = new GodwarsArmadylFaction(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		else if (id == 6281 || id == 6282 || id == 6275 || id == 6279
				|| id == 9184 || id == 6268 || id == 6270 || id == 6274
				|| id == 6277 || id == 6276 || id == 6278)
			n = new GodwarsBandosFaction(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		else if (id == 6261 || id == 6263 || id == 6265)
			n = GodWarsBosses.graardorMinions[(id - 6261) / 2] = new GodWarMinion(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		else if (id == 6260)
			n = new GeneralGraardor(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		else if (id == 6222)
			n = new KreeArra(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		else if (id == 6223 || id == 6225 || id == 6227)
			n = GodWarsBosses.armadylMinions[(id - 6223) / 2] = new GodWarMinion(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		else if (id == 6203)
			n = new KrilTstsaroth(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		else if (id == 6204 || id == 6206 || id == 6208)
			n = GodWarsBosses.zamorakMinions[(id - 6204) / 2] = new GodWarMinion(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		else if (id == 50 || id == 2642)
			n = new KingBlackDragon(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		else if (id == 13460)
			n = new Hati(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		else if (id == 10479)
			n = new MentiosManager(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		else if (id == 13860)
			n = new Hati(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        else if (id == 8596)
            n = new Hati(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		else if (id == 13768)
			n = new MentiosManager(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		else if (id >= 9462 && id <= 9467)
			n = new Strykewyrm(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
		else if (id == 6248 || id == 6250 || id == 6252)
			n = GodWarsBosses.commanderMinions[(id - 6248) / 2] = new GodWarMinion(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		else if (id == 6247)
			n = new CommanderZilyana(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		else if (id == 8133)
			n = new CorporealBeast(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		else if (id == 15102)
			n = DamageDummy.dummy = new Dummy(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		else if (id == 13447)
			n = ZarosGodwars.nex = new Nex(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		else if (id == 13451)
			n = ZarosGodwars.fumus = new NexMinion(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		else if (id == 13452)
			n = ZarosGodwars.umbra = new NexMinion(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		else if (id == 13453)
			n = ZarosGodwars.cruor = new NexMinion(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		else if (id == 13454)
			n = ZarosGodwars.glacies = new NexMinion(id, tile, mapAreaNameHash,canBeAttackFromOutOfArea, spawned);
		else if (id == 14256)
			n = new Lucien(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		else if (id == 11872)
			n = new Thunder(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		else if (id == 6247 || id == 6250 || id == 6252 || id == 6248 || id == 6203 || id == 6204 || id == 6206 || id == 6208 || id == 12878 || id == 3847)
			n = new DZBosses(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);				
		else if (id == 15222)
			n = new Nub2(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);	
		else if (id == 1895)
			n = new Nub12(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);	
		else if (id == 15208)
			n = new Nub13(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);	
		else if (id == 15172)
			n = new Nub3(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);	
		else if (id == 8335)
			n = new Nub8(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);	
		else if (id == 9622 || id == 15235)
			n = new Nub9(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		else if (id == 15006)
			n = new Nub10(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		else if (id == 15009)
			n = new Nub11(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);		
		else if (id == 15208)
			n = new Nub15(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);		
		else if (id == 6)
			n = new Nub15(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);	
		else if (id == 11)
			n = new Nub15(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);				
		else if (id == 92)
			n = new NPCevent(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);			
		else if (id == 1900)
			n = new Nub7(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		else if (id == 15002)
			n = new Nub5(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		else if (id == 15003)
			n = new Nub6(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		else if (id == 11751)
			n = new Nub1(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		else if (id == 3334)
			n = new Nub4(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		else if (id == 14072)
			n = new Vampyre(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);			
		else if (id == 14260)
			n = new Nub(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);				
		else if (id == 8335)
			n = new MercenaryMage(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		else if (id == 8349 || id == 8450 || id == 8451)
			n = new TormentedDemon(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		else if (id == 15149)
			n = new MasterOfFear(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		else if (id == 14696)
			n = new GanodermicBeast(id, tile, mapAreaNameHash,
					canBeAttackFromOutOfArea, spawned);
		else
			n = new NPC(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		return n;
	}

	public static final NPC spawnNPC(int id, WorldTile tile,
			int mapAreaNameHash, boolean canBeAttackFromOutOfArea) {
		return spawnNPC(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea,
				false);
	}

	/*
	 * check if the entity region changed because moved or teled then we update
	 * it
	 */
	public static final void updateEntityRegion(Entity entity) {
		if (entity.hasFinished()) {
			if (entity instanceof Player)
				getRegion(entity.getLastRegionId()).removePlayerIndex(
						entity.getIndex());
			else
				getRegion(entity.getLastRegionId()).removeNPCIndex(
						entity.getIndex());
			return;
		}
		int regionId = entity.getRegionId();
		if (entity.getLastRegionId() != regionId) { // map region entity at
			// changed
			if (entity instanceof Player) {
				if (entity.getLastRegionId() > 0)
					getRegion(entity.getLastRegionId()).removePlayerIndex(
							entity.getIndex());
				Region region = getRegion(regionId);
				region.addPlayerIndex(entity.getIndex());
				Player player = (Player) entity;
				int musicId = region.getMusicId();
				if (musicId != -1)
					player.getMusicsManager().checkMusic(musicId);
				player.getControlerManager().moved();
				if (player.hasStarted())
					checkControlersAtMove(player);
			} else {
				if (entity.getLastRegionId() > 0)
					getRegion(entity.getLastRegionId()).removeNPCIndex(
							entity.getIndex());
				getRegion(regionId).addNPCIndex(entity.getIndex());
			}
			entity.checkIronmanArea();
			entity.checkMultiArea();
			entity.checkDonatorArea();
			entity.checkGypsy();
			entity.checkGroundFloor();
			entity.checkExtremeArea();
			entity.checkSuperArea();
			entity.checkEradicatorArea();
			entity.setLastRegionId(regionId);
		} else {
			if (entity instanceof Player) {
				Player player = (Player) entity;
				player.getControlerManager().moved();
				if (player.hasStarted())
					checkControlersAtMove(player);
			}
			entity.checkMultiArea();
			entity.checkDonatorArea();
			entity.checkGroundFloor();
			entity.checkGypsy();
			entity.checkExtremeArea();
			entity.checkSuperArea();
			entity.checkEradicatorArea();
		}
	}

	private static void checkControlersAtMove(Player player) {
		if (!(player.getControlerManager().getControler() instanceof RequestController)
				&& RequestController.inWarRequest(player))
			player.getControlerManager().startControler("clan_wars_request");
		else if (DuelControler.isAtDuelArena(player))
			player.getControlerManager().startControler("DuelControler");
		else if (FfaZone.inArea(player))
			player.getControlerManager().startControler("clan_wars_ffa");
	}

	/*
	 * checks clip
	 */
	public static boolean canMoveNPC(int plane, int x, int y, int size) {
		for (int tileX = x; tileX < x + size; tileX++)
			for (int tileY = y; tileY < y + size; tileY++)
				if (getMask(plane, tileX, tileY) != 0)
					return false;
		return true;
	}

	/*
	 * checks clip
	 */
	public static boolean isNotCliped(int plane, int x, int y, int size) {
		for (int tileX = x; tileX < x + size; tileX++)
			for (int tileY = y; tileY < y + size; tileY++)
				if ((getMask(plane, tileX, tileY) & 2097152) != 0)
					return false;
		return true;
	}

	public static int getMask(int plane, int x, int y) {
		WorldTile tile = new WorldTile(x, y, plane);
		int regionId = tile.getRegionId();
		Region region = getRegion(regionId);
		if (region == null)
			return -1;
		int baseLocalX = x - ((regionId >> 8) * 64);
		int baseLocalY = y - ((regionId & 0xff) * 64);
		return region.getMask(tile.getPlane(), baseLocalX, baseLocalY);
	}

	public static void setMask(int plane, int x, int y, int mask) {
		WorldTile tile = new WorldTile(x, y, plane);
		int regionId = tile.getRegionId();
		Region region = getRegion(regionId);
		if (region == null)
			return;
		int baseLocalX = x - ((regionId >> 8) * 64);
		int baseLocalY = y - ((regionId & 0xff) * 64);
		region.setMask(tile.getPlane(), baseLocalX, baseLocalY, mask);
	}

	public static int getRotation(int plane, int x, int y) {
		WorldTile tile = new WorldTile(x, y, plane);
		int regionId = tile.getRegionId();
		Region region = getRegion(regionId);
		if (region == null)
			return 0;
		int baseLocalX = x - ((regionId >> 8) * 64);
		int baseLocalY = y - ((regionId & 0xff) * 64);
		return region.getRotation(tile.getPlane(), baseLocalX, baseLocalY);
	}

	private static int getClipedOnlyMask(int plane, int x, int y) {
		WorldTile tile = new WorldTile(x, y, plane);
		int regionId = tile.getRegionId();
		Region region = getRegion(regionId);
		if (region == null)
			return -1;
		int baseLocalX = x - ((regionId >> 8) * 64);
		int baseLocalY = y - ((regionId & 0xff) * 64);
		return region
				.getMaskClipedOnly(tile.getPlane(), baseLocalX, baseLocalY);
	}

	public static final boolean checkProjectileStep(int plane, int x, int y,
			int dir, int size) {
		int xOffset = Utils.DIRECTION_DELTA_X[dir];
		int yOffset = Utils.DIRECTION_DELTA_Y[dir];
		/*
		 * int rotation = getRotation(plane,x+xOffset,y+yOffset); if(rotation !=
		 * 0) { dir += rotation; if(dir >= Utils.DIRECTION_DELTA_X.length) dir =
		 * dir - (Utils.DIRECTION_DELTA_X.length-1); xOffset =
		 * Utils.DIRECTION_DELTA_X[dir]; yOffset = Utils.DIRECTION_DELTA_Y[dir];
		 * }
		 */
		if (size == 1) {
			int mask = getClipedOnlyMask(plane, x
					+ Utils.DIRECTION_DELTA_X[dir], y
					+ Utils.DIRECTION_DELTA_Y[dir]);
			if (xOffset == -1 && yOffset == 0)
				return (mask & 0x42240000) == 0;
			if (xOffset == 1 && yOffset == 0)
				return (mask & 0x60240000) == 0;
			if (xOffset == 0 && yOffset == -1)
				return (mask & 0x40a40000) == 0;
			if (xOffset == 0 && yOffset == 1)
				return (mask & 0x48240000) == 0;
			if (xOffset == -1 && yOffset == -1) {
				return (mask & 0x43a40000) == 0
						&& (getClipedOnlyMask(plane, x - 1, y) & 0x42240000) == 0
						&& (getClipedOnlyMask(plane, x, y - 1) & 0x40a40000) == 0;
			}
			if (xOffset == 1 && yOffset == -1) {
				return (mask & 0x60e40000) == 0
						&& (getClipedOnlyMask(plane, x + 1, y) & 0x60240000) == 0
						&& (getClipedOnlyMask(plane, x, y - 1) & 0x40a40000) == 0;
			}
			if (xOffset == -1 && yOffset == 1) {
				return (mask & 0x4e240000) == 0
						&& (getClipedOnlyMask(plane, x - 1, y) & 0x42240000) == 0
						&& (getClipedOnlyMask(plane, x, y + 1) & 0x48240000) == 0;
			}
			if (xOffset == 1 && yOffset == 1) {
				return (mask & 0x78240000) == 0
						&& (getClipedOnlyMask(plane, x + 1, y) & 0x60240000) == 0
						&& (getClipedOnlyMask(plane, x, y + 1) & 0x48240000) == 0;
			}
		} else if (size == 2) {
			if (xOffset == -1 && yOffset == 0)
				return (getClipedOnlyMask(plane, x - 1, y) & 0x43a40000) == 0
						&& (getClipedOnlyMask(plane, x - 1, y + 1) & 0x4e240000) == 0;
			if (xOffset == 1 && yOffset == 0)
				return (getClipedOnlyMask(plane, x + 2, y) & 0x60e40000) == 0
						&& (getClipedOnlyMask(plane, x + 2, y + 1) & 0x78240000) == 0;
			if (xOffset == 0 && yOffset == -1)
				return (getClipedOnlyMask(plane, x, y - 1) & 0x43a40000) == 0
						&& (getClipedOnlyMask(plane, x + 1, y - 1) & 0x60e40000) == 0;
			if (xOffset == 0 && yOffset == 1)
				return (getClipedOnlyMask(plane, x, y + 2) & 0x4e240000) == 0
						&& (getClipedOnlyMask(plane, x + 1, y + 2) & 0x78240000) == 0;
			if (xOffset == -1 && yOffset == -1)
				return (getClipedOnlyMask(plane, x - 1, y) & 0x4fa40000) == 0
						&& (getClipedOnlyMask(plane, x - 1, y - 1) & 0x43a40000) == 0
						&& (getClipedOnlyMask(plane, x, y - 1) & 0x63e40000) == 0;
			if (xOffset == 1 && yOffset == -1)
				return (getClipedOnlyMask(plane, x + 1, y - 1) & 0x63e40000) == 0
						&& (getClipedOnlyMask(plane, x + 2, y - 1) & 0x60e40000) == 0
						&& (getClipedOnlyMask(plane, x + 2, y) & 0x78e40000) == 0;
			if (xOffset == -1 && yOffset == 1)
				return (getClipedOnlyMask(plane, x - 1, y + 1) & 0x4fa40000) == 0
						&& (getClipedOnlyMask(plane, x - 1, y + 1) & 0x4e240000) == 0
						&& (getClipedOnlyMask(plane, x, y + 2) & 0x7e240000) == 0;
			if (xOffset == 1 && yOffset == 1)
				return (getClipedOnlyMask(plane, x + 1, y + 2) & 0x7e240000) == 0
						&& (getClipedOnlyMask(plane, x + 2, y + 2) & 0x78240000) == 0
						&& (getClipedOnlyMask(plane, x + 1, y + 1) & 0x78e40000) == 0;
		} else {
			if (xOffset == -1 && yOffset == 0) {
				if ((getClipedOnlyMask(plane, x - 1, y) & 0x43a40000) != 0
						|| (getClipedOnlyMask(plane, x - 1, -1 + (y + size)) & 0x4e240000) != 0)
					return false;
				for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++)
					if ((getClipedOnlyMask(plane, x - 1, y + sizeOffset) & 0x4fa40000) != 0)
						return false;
			} else if (xOffset == 1 && yOffset == 0) {
				if ((getClipedOnlyMask(plane, x + size, y) & 0x60e40000) != 0
						|| (getClipedOnlyMask(plane, x + size, y - (-size + 1)) & 0x78240000) != 0)
					return false;
				for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++)
					if ((getClipedOnlyMask(plane, x + size, y + sizeOffset) & 0x78e40000) != 0)
						return false;
			} else if (xOffset == 0 && yOffset == -1) {
				if ((getClipedOnlyMask(plane, x, y - 1) & 0x43a40000) != 0
						|| (getClipedOnlyMask(plane, x + size - 1, y - 1) & 0x60e40000) != 0)
					return false;
				for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++)
					if ((getClipedOnlyMask(plane, x + sizeOffset, y - 1) & 0x63e40000) != 0)
						return false;
			} else if (xOffset == 0 && yOffset == 1) {
				if ((getClipedOnlyMask(plane, x, y + size) & 0x4e240000) != 0
						|| (getClipedOnlyMask(plane, x + (size - 1), y + size) & 0x78240000) != 0)
					return false;
				for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++)
					if ((getClipedOnlyMask(plane, x + sizeOffset, y + size) & 0x7e240000) != 0)
						return false;
			} else if (xOffset == -1 && yOffset == -1) {
				if ((getClipedOnlyMask(plane, x - 1, y - 1) & 0x43a40000) != 0)
					return false;
				for (int sizeOffset = 1; sizeOffset < size; sizeOffset++)
					if ((getClipedOnlyMask(plane, x - 1, y + (-1 + sizeOffset)) & 0x4fa40000) != 0
							|| (getClipedOnlyMask(plane, sizeOffset - 1 + x,
									y - 1) & 0x63e40000) != 0)
						return false;
			} else if (xOffset == 1 && yOffset == -1) {
				if ((getClipedOnlyMask(plane, x + size, y - 1) & 0x60e40000) != 0)
					return false;
				for (int sizeOffset = 1; sizeOffset < size; sizeOffset++)
					if ((getClipedOnlyMask(plane, x + size, sizeOffset
							+ (-1 + y)) & 0x78e40000) != 0
							|| (getClipedOnlyMask(plane, x + sizeOffset, y - 1) & 0x63e40000) != 0)
						return false;
			} else if (xOffset == -1 && yOffset == 1) {
				if ((getClipedOnlyMask(plane, x - 1, y + size) & 0x4e240000) != 0)
					return false;
				for (int sizeOffset = 1; sizeOffset < size; sizeOffset++)
					if ((getClipedOnlyMask(plane, x - 1, y + sizeOffset) & 0x4fa40000) != 0
							|| (getClipedOnlyMask(plane, -1 + (x + sizeOffset),
									y + size) & 0x7e240000) != 0)
						return false;
			} else if (xOffset == 1 && yOffset == 1) {
				if ((getClipedOnlyMask(plane, x + size, y + size) & 0x78240000) != 0)
					return false;
				for (int sizeOffset = 1; sizeOffset < size; sizeOffset++)
					if ((getClipedOnlyMask(plane, x + sizeOffset, y + size) & 0x7e240000) != 0
							|| (getClipedOnlyMask(plane, x + size, y
									+ sizeOffset) & 0x78e40000) != 0)
						return false;
			}
		}
		return true;
	}

	public static final boolean checkWalkStep(int plane, int x, int y, int dir,
			int size) {
		int xOffset = Utils.DIRECTION_DELTA_X[dir];
		int yOffset = Utils.DIRECTION_DELTA_Y[dir];
		int rotation = getRotation(plane, x + xOffset, y + yOffset);
		if (rotation != 0) {
			for (int rotate = 0; rotate < (4 - rotation); rotate++) {
				int fakeChunckX = xOffset;
				int fakeChunckY = yOffset;
				xOffset = fakeChunckY;
				yOffset = 0 - fakeChunckX;
			}
		}

		if (size == 1) {
			int mask = getMask(plane, x + Utils.DIRECTION_DELTA_X[dir], y
					+ Utils.DIRECTION_DELTA_Y[dir]);
			if (xOffset == -1 && yOffset == 0)
				return (mask & 0x42240000) == 0;
			if (xOffset == 1 && yOffset == 0)
				return (mask & 0x60240000) == 0;
			if (xOffset == 0 && yOffset == -1)
				return (mask & 0x40a40000) == 0;
			if (xOffset == 0 && yOffset == 1)
				return (mask & 0x48240000) == 0;
			if (xOffset == -1 && yOffset == -1) {
				return (mask & 0x43a40000) == 0
						&& (getMask(plane, x - 1, y) & 0x42240000) == 0
						&& (getMask(plane, x, y - 1) & 0x40a40000) == 0;
			}
			if (xOffset == 1 && yOffset == -1) {
				return (mask & 0x60e40000) == 0
						&& (getMask(plane, x + 1, y) & 0x60240000) == 0
						&& (getMask(plane, x, y - 1) & 0x40a40000) == 0;
			}
			if (xOffset == -1 && yOffset == 1) {
				return (mask & 0x4e240000) == 0
						&& (getMask(plane, x - 1, y) & 0x42240000) == 0
						&& (getMask(plane, x, y + 1) & 0x48240000) == 0;
			}
			if (xOffset == 1 && yOffset == 1) {
				return (mask & 0x78240000) == 0
						&& (getMask(plane, x + 1, y) & 0x60240000) == 0
						&& (getMask(plane, x, y + 1) & 0x48240000) == 0;
			}
		} else if (size == 2) {
			if (xOffset == -1 && yOffset == 0)
				return (getMask(plane, x - 1, y) & 0x43a40000) == 0
						&& (getMask(plane, x - 1, y + 1) & 0x4e240000) == 0;
			if (xOffset == 1 && yOffset == 0)
				return (getMask(plane, x + 2, y) & 0x60e40000) == 0
						&& (getMask(plane, x + 2, y + 1) & 0x78240000) == 0;
			if (xOffset == 0 && yOffset == -1)
				return (getMask(plane, x, y - 1) & 0x43a40000) == 0
						&& (getMask(plane, x + 1, y - 1) & 0x60e40000) == 0;
			if (xOffset == 0 && yOffset == 1)
				return (getMask(plane, x, y + 2) & 0x4e240000) == 0
						&& (getMask(plane, x + 1, y + 2) & 0x78240000) == 0;
			if (xOffset == -1 && yOffset == -1)
				return (getMask(plane, x - 1, y) & 0x4fa40000) == 0
						&& (getMask(plane, x - 1, y - 1) & 0x43a40000) == 0
						&& (getMask(plane, x, y - 1) & 0x63e40000) == 0;
			if (xOffset == 1 && yOffset == -1)
				return (getMask(plane, x + 1, y - 1) & 0x63e40000) == 0
						&& (getMask(plane, x + 2, y - 1) & 0x60e40000) == 0
						&& (getMask(plane, x + 2, y) & 0x78e40000) == 0;
			if (xOffset == -1 && yOffset == 1)
				return (getMask(plane, x - 1, y + 1) & 0x4fa40000) == 0
						&& (getMask(plane, x - 1, y + 1) & 0x4e240000) == 0
						&& (getMask(plane, x, y + 2) & 0x7e240000) == 0;
			if (xOffset == 1 && yOffset == 1)
				return (getMask(plane, x + 1, y + 2) & 0x7e240000) == 0
						&& (getMask(plane, x + 2, y + 2) & 0x78240000) == 0
						&& (getMask(plane, x + 1, y + 1) & 0x78e40000) == 0;
		} else {
			if (xOffset == -1 && yOffset == 0) {
				if ((getMask(plane, x - 1, y) & 0x43a40000) != 0
						|| (getMask(plane, x - 1, -1 + (y + size)) & 0x4e240000) != 0)
					return false;
				for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++)
					if ((getMask(plane, x - 1, y + sizeOffset) & 0x4fa40000) != 0)
						return false;
			} else if (xOffset == 1 && yOffset == 0) {
				if ((getMask(plane, x + size, y) & 0x60e40000) != 0
						|| (getMask(plane, x + size, y - (-size + 1)) & 0x78240000) != 0)
					return false;
				for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++)
					if ((getMask(plane, x + size, y + sizeOffset) & 0x78e40000) != 0)
						return false;
			} else if (xOffset == 0 && yOffset == -1) {
				if ((getMask(plane, x, y - 1) & 0x43a40000) != 0
						|| (getMask(plane, x + size - 1, y - 1) & 0x60e40000) != 0)
					return false;
				for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++)
					if ((getMask(plane, x + sizeOffset, y - 1) & 0x63e40000) != 0)
						return false;
			} else if (xOffset == 0 && yOffset == 1) {
				if ((getMask(plane, x, y + size) & 0x4e240000) != 0
						|| (getMask(plane, x + (size - 1), y + size) & 0x78240000) != 0)
					return false;
				for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++)
					if ((getMask(plane, x + sizeOffset, y + size) & 0x7e240000) != 0)
						return false;
			} else if (xOffset == -1 && yOffset == -1) {
				if ((getMask(plane, x - 1, y - 1) & 0x43a40000) != 0)
					return false;
				for (int sizeOffset = 1; sizeOffset < size; sizeOffset++)
					if ((getMask(plane, x - 1, y + (-1 + sizeOffset)) & 0x4fa40000) != 0
							|| (getMask(plane, sizeOffset - 1 + x, y - 1) & 0x63e40000) != 0)
						return false;
			} else if (xOffset == 1 && yOffset == -1) {
				if ((getMask(plane, x + size, y - 1) & 0x60e40000) != 0)
					return false;
				for (int sizeOffset = 1; sizeOffset < size; sizeOffset++)
					if ((getMask(plane, x + size, sizeOffset + (-1 + y)) & 0x78e40000) != 0
							|| (getMask(plane, x + sizeOffset, y - 1) & 0x63e40000) != 0)
						return false;
			} else if (xOffset == -1 && yOffset == 1) {
				if ((getMask(plane, x - 1, y + size) & 0x4e240000) != 0)
					return false;
				for (int sizeOffset = 1; sizeOffset < size; sizeOffset++)
					if ((getMask(plane, x - 1, y + sizeOffset) & 0x4fa40000) != 0
							|| (getMask(plane, -1 + (x + sizeOffset), y + size) & 0x7e240000) != 0)
						return false;
			} else if (xOffset == 1 && yOffset == 1) {
				if ((getMask(plane, x + size, y + size) & 0x78240000) != 0)
					return false;
				for (int sizeOffset = 1; sizeOffset < size; sizeOffset++)
					if ((getMask(plane, x + sizeOffset, y + size) & 0x7e240000) != 0
							|| (getMask(plane, x + size, y + sizeOffset) & 0x78e40000) != 0)
						return false;
			}
		}
		return true;
	}

	public static final boolean containsPlayer(String username) {
		for (Player p2 : players) {
			if (p2 == null)
				continue;
			if (p2.getUsername().equals(username))
				return true;
		}
		return false;
	}

	public static Player getPlayer(String username) {
		for (Player player : getPlayers()) {
			if (player == null)
				continue;
			if (player.getUsername().equals(username))
				return player;
		}
		return null;
	}

	public static final Player getPlayerByDisplayName(String username) {
		String formatedUsername = Utils.formatPlayerNameForDisplay(username);
		for (Player player : getPlayers()) {
			if (player == null)
				continue;
			if (player.getUsername().equalsIgnoreCase(formatedUsername)
					|| player.getDisplayName().equalsIgnoreCase(
							formatedUsername))
				return player;
		}
		return null;
	}
	
	public static final Player getPlayerPMName(String username) {
		String formatedUsername = username.toLowerCase().replaceAll(" ", "_");
		for (Player player : getPlayers()) {
			if (player == null)
				continue;
			if (player.getUsername().equalsIgnoreCase(formatedUsername)
					|| player.getDisplayName().equalsIgnoreCase(
							formatedUsername))
				return player;
		}
		return null;
	}	
	
	public static NPC getNPC(int npcId) {
		for (NPC npc : getNPCs()) {
			if(npc.getId() == npcId) {
				return npc;
			}
		}
		return null;
	}
	
	public static NPC getNPCNear(int npcId, Player player) {
		for (NPC npc : getNPCs()) {
			if(npc.getId() == npcId) {
				if (npc.withinDistance(player, 100))
					return npc;
			}
		}
		return null;
	}
	
	public static ArrayList<NPC> getNPCsNear(Player player) {
		ArrayList<NPC> result = new ArrayList<NPC>();
		for (NPC npc : getNPCs()) {
			if (npc.withinDistance(player, 100))
				result.add(npc);
		}
		return result;
	}	

	public static final EntityList<Player> getPlayers() {
		return players;
	}

	public static final EntityList<NPC> getNPCs() {
		return npcs;
	}

	private World() {

	}

	public static final void safeShutdown(final boolean restart, int delay) {
		if (exiting_start != 0)
			return;
		exiting_start = Utils.currentTimeMillis();
		exiting_delay = delay;
		for (Player player : World.getPlayers()) {
			if (player == null || !player.hasStarted() || player.hasFinished())
				continue;
			player.getPackets().sendSystemUpdate(delay);
		}
		CoresManager.slowExecutor.schedule(new Runnable() {
			@Override
			public void run() {
				try {
					for (Player player : World.getPlayers()) {
						if (player != null) {
						if (player.getClanName() != null)
							ClansManager.createClan(player, player.getClanName());
						}
						if (player == null || !player.hasStarted())
							continue;
						player.realFinish();
					}
					IPBanL.save();
					PkRank.save();
					DummyRank.save();
					IPMute.save();
					Launcher.shutdown();
					if (restart)
						Launcher.restart();
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}
		}, delay, TimeUnit.SECONDS);
	}

	/*
	 * by default doesnt changeClipData
	 */
	public static final void spawnTemporaryObject(final WorldObject object,
			long time) {
		spawnTemporaryObject(object, time, false);
	}

	public static final void spawnTemporaryObject(final WorldObject object,
			long time, final boolean clip) {
		final int regionId = object.getRegionId();
		WorldObject realMapObject = getRegion(regionId).getRealObject(object);
		if (getRegion(regionId).getSpawnedObject(object) != null)
		    realMapObject = getRegion(regionId).getSpawnedObject(object);
		// remakes object, has to be done because on static region coords arent
		// same of real
		final WorldObject realObject = realMapObject == null ? null
				: new WorldObject(realMapObject.getId(),
						realMapObject.getType(), realMapObject.getRotation(),
						object.getX(), object.getY(), object.getPlane());
		spawnObject(object, clip);
		final int baseLocalX = object.getX() - ((regionId >> 8) * 64);
		final int baseLocalY = object.getY() - ((regionId & 0xff) * 64);
		if (realObject != null && clip)
			getRegion(regionId).removeMapObject(realObject, baseLocalX,
					baseLocalY);
		CoresManager.slowExecutor.schedule(new Runnable() {
			@Override
			public void run() {
				try {
					getRegion(regionId).removeObject(object);
					if (clip) {
						getRegion(regionId).removeMapObject(object, baseLocalX,
								baseLocalY);
						if (realObject != null) {
							int baseLocalX = object.getX()
									- ((regionId >> 8) * 64);
							int baseLocalY = object.getY()
									- ((regionId & 0xff) * 64);
							getRegion(regionId).addMapObject(realObject,
									baseLocalX, baseLocalY);
						}
					}
					for (Player p2 : players) {
						if (p2 == null || !p2.hasStarted() || p2.hasFinished()
								|| !p2.getMapRegionsIds().contains(regionId))
							continue;
						if (realObject != null)
							p2.getPackets().sendSpawnedObject(realObject);
						else
							p2.getPackets().sendDestroyObject(object);
					}
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}

		}, time, TimeUnit.MILLISECONDS);
	}

	public static final boolean isSpawnedObject(WorldObject object) {
		final int regionId = object.getRegionId();
		WorldObject spawnedObject = getRegion(regionId)
				.getSpawnedObject(object);
		if (spawnedObject != null && object.getId() == spawnedObject.getId())
			return true;
		return false;
	}

	public static final boolean removeTemporaryObject(final WorldObject object,
			long time, final boolean clip) {
		final int regionId = object.getRegionId();
		// remakes object, has to be done because on static region coords arent
		// same of real
		final WorldObject realObject = object == null ? null : new WorldObject(
				object.getId(), object.getType(), object.getRotation(),
				object.getX(), object.getY(), object.getPlane());
		removeObject(object, clip);
		CoresManager.slowExecutor.schedule(new Runnable() {
			@Override
			public void run() {
				try {
					getRegion(regionId).removeRemovedObject(object);
					if (clip) {
						int baseLocalX = object.getX() - ((regionId >> 8) * 64);
						int baseLocalY = object.getY()
								- ((regionId & 0xff) * 64);
						getRegion(regionId).addMapObject(realObject,
								baseLocalX, baseLocalY);
					}
					for (Player p2 : players) {
						if (p2 == null || !p2.hasStarted() || p2.hasFinished()
								|| !p2.getMapRegionsIds().contains(regionId))
							continue;
						p2.getPackets().sendSpawnedObject(realObject);
					}
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}

		}, time, TimeUnit.MILLISECONDS);

		return true;
	}

	public static final void removeObject(WorldObject object, boolean clip) {
		int regionId = object.getRegionId();
		getRegion(regionId).addRemovedObject(object);
		if (clip) {
			int baseLocalX = object.getX() - ((regionId >> 8) * 64);
			int baseLocalY = object.getY() - ((regionId & 0xff) * 64);
			getRegion(regionId).removeMapObject(object, baseLocalX, baseLocalY);
		}
		synchronized (players) {
			for (Player p2 : players) {
				if (p2 == null || !p2.hasStarted() || p2.hasFinished()
						|| !p2.getMapRegionsIds().contains(regionId))
					continue;
				p2.getPackets().sendDestroyObject(object);
			}
		}
	}

	public static final WorldObject getObject(WorldTile tile) {
		int regionId = tile.getRegionId();
		int baseLocalX = tile.getX() - ((regionId >> 8) * 64);
		int baseLocalY = tile.getY() - ((regionId & 0xff) * 64);
		return getRegion(regionId).getObject(tile.getPlane(), baseLocalX, baseLocalY);
	}

	public static final WorldObject getObject(WorldTile tile, int type) {
		int regionId = tile.getRegionId();
		int baseLocalX = tile.getX() - ((regionId >> 8) * 64);
		int baseLocalY = tile.getY() - ((regionId & 0xff) * 64);
		return getRegion(regionId).getObject(tile.getPlane(), baseLocalX,
				baseLocalY, type);
	}

	public static final void spawnObject(WorldObject object, boolean clip) {
		int regionId = object.getRegionId();
		getRegion(regionId).addObject(object);
		if (clip) {
			int baseLocalX = object.getX() - ((regionId >> 8) * 64);
			int baseLocalY = object.getY() - ((regionId & 0xff) * 64);
			getRegion(regionId).addMapObject(object, baseLocalX, baseLocalY);
		}
		synchronized (players) {
			for (Player p2 : players) {
				if (p2 == null || !p2.hasStarted() || p2.hasFinished()
						|| !p2.getMapRegionsIds().contains(regionId))
					continue;
				p2.getPackets().sendSpawnedObject(object);
			}
		}
	}

	public static final void addGroundItem(final Item item, final WorldTile tile) {
		final FloorItem floorItem = new FloorItem(item, tile, null, null, false,
				false);
		final Region region = getRegion(tile.getRegionId());
		region.forceGetFloorItems().add(floorItem);
		int regionId = tile.getRegionId();
		for (Player player : players) {
			if (player == null || !player.hasStarted() || player.hasFinished()
					|| player.getPlane() != tile.getPlane()
					|| !player.getMapRegionsIds().contains(regionId))
				continue;
			player.getPackets().sendGroundItem(floorItem);
		}
	}

	public static final void addGroundItem(final Item item,
			final WorldTile tile, final Player owner/* null for default */, final Player loser,
			final boolean underGrave, long hiddenTime/* default 3minutes */,
			boolean invisible) {
		addGroundItem(item, tile, owner, loser, underGrave, hiddenTime, invisible,
				false, 180);
	}

	public static final void addGroundItem(final Item item,
			final WorldTile tile, final Player owner, final Player loser,
			final boolean underGrave, long hiddenTime/* default 3minutes */,
			boolean invisible, boolean intoGold) {
		addGroundItem(item, tile, owner, loser, underGrave, hiddenTime, invisible,
				intoGold, 180);
	}

	public static final void addGroundItem(final Item item,
			final WorldTile tile, final Player owner/* null for default */, final Player loser,
			final boolean underGrave, long hiddenTime/* default 3minutes */,
			boolean invisible, boolean intoGold, final int publicTime) {
		if (intoGold) {
			if (!ItemConstants.isTradeable(item)) {
				int price = PriceManager.getBuyPrice(item);
				if (price <= 0)
					return;
				item.setId(995);
				item.setAmount(price);
			}
		}
		
		final FloorItem floorItem = new FloorItem(item, tile, owner, loser, 
				owner == null ? false : underGrave, invisible);
		floorItem.setLoser(loser);
		final Region region = getRegion(tile.getRegionId());
		region.forceGetFloorItems().add(floorItem);
		if (invisible && hiddenTime != -1) {
			if (owner != null)
				owner.getPackets().sendGroundItem(floorItem);
			CoresManager.slowExecutor.schedule(new Runnable() {
				@Override
				public void run() {
					try {
						if (!region.forceGetFloorItems().contains(floorItem))
							return;
						int regionId = tile.getRegionId();
						if (underGrave || !ItemConstants.isTradeable(floorItem)
								|| item.getName().contains("Dr nabanik")) {
							region.forceGetFloorItems().remove(floorItem);
							if (owner != null) {
								if (owner.getMapRegionsIds().contains(regionId)
										&& owner.getPlane() == tile.getPlane())
									owner.getPackets().sendRemoveGroundItem(
											floorItem);
							}
							return;
						}

						floorItem.setInvisible(false);
						for (Player player : players) {
							if (player == null
									|| player == owner
									|| !player.hasStarted()
									|| player.hasFinished()
									|| player.getPlane() != tile.getPlane()
									|| !player.getMapRegionsIds().contains(
											regionId))
								continue;
							player.getPackets().sendGroundItem(floorItem);
						}
						removeGroundItem(floorItem, publicTime);
					} catch (Throwable e) {
						Logger.handle(e);
					}
				}
			}, hiddenTime, TimeUnit.SECONDS);
			return;
		}
		int regionId = tile.getRegionId();
		for (Player player : players) {
			if (player == null || !player.hasStarted() || player.hasFinished()
					|| player.getPlane() != tile.getPlane()
					|| !player.getMapRegionsIds().contains(regionId))
				continue;
			player.getPackets().sendGroundItem(floorItem);
		}
		removeGroundItem(floorItem, publicTime);
	}

	public static final void updateGroundItem(Item item, final WorldTile tile,
			final Player owner, final Player loser) {
		final FloorItem floorItem = World.getRegion(tile.getRegionId())
				.getGroundItem(item.getId(), tile, owner);
		if (floorItem == null) {
			addGroundItem(item, tile, owner, loser, false, 360, true);
			return;
		}
		floorItem.setAmount(floorItem.getAmount() + item.getAmount());
		owner.getPackets().sendRemoveGroundItem(floorItem);
		owner.getPackets().sendGroundItem(floorItem);

	}

	private static final void removeGroundItem(final FloorItem floorItem,
			long publicTime) {
		if (publicTime < 0) {
			return;
		}
		CoresManager.slowExecutor.schedule(new Runnable() {
			@Override
			public void run() {
				try {
					int regionId = floorItem.getTile().getRegionId();
					Region region = getRegion(regionId);
					if (!region.forceGetFloorItems().contains(floorItem))
						return;
					region.forceGetFloorItems().remove(floorItem);
					for (Player player : World.getPlayers()) {
						if (player == null
								|| !player.hasStarted()
								|| player.hasFinished()
								|| player.getPlane() != floorItem.getTile()
										.getPlane()
								|| !player.getMapRegionsIds()
										.contains(regionId))
							continue;
						player.getPackets().sendRemoveGroundItem(floorItem);
					}
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}
		}, publicTime, TimeUnit.SECONDS);
	}

	public static final boolean removeGroundItem(Player player,
			FloorItem floorItem) {
		return removeGroundItem(player, floorItem, true);
	}

	public static final boolean removeGroundItem(Player player,
			FloorItem floorItem, boolean add) {
		int regionId = floorItem.getTile().getRegionId();
		Region region = getRegion(regionId);
		if (!region.forceGetFloorItems().contains(floorItem))
			return false;
		if (player.getInventory().getFreeSlots() == 0
				&& (floorItem.getId() != 995 && floorItem.getId() != 1464 &&
					floorItem.getId() != 12852 && floorItem.getId() != 19819 &&
					floorItem.getId() != 2996)) {
			player.sm("Not enough inventory space.");
			return false;
		}
		int amount = player.getInventory().getItems().getNumberOf(995);
		if (amount < 0) {
			player.getPackets()
					.sendGameMessage(
							"<col=F51625> [Alert] You have to much coins in your Inventory, please get more space.");
			return false;
		}	
		if (add) {
			switch(floorItem.getId()) {
			case 995:
				if (!player.addMoney(floorItem.getAmount())) {
					player.sm("Not enough space to add these coins, like anywhere. Legit, anywhere. I don't know where to put them. I have"
							+ " four billion coins on me. How? You want me to add more? Even more?");
					return false;
				}
				break;
			case 1464:
				player.getCurrencyPouch().setVoteTickets(player.getCurrencyPouch().getVoteTickets() + floorItem.getAmount());
				break;
			case 19819:
				player.getCurrencyPouch().setInvasionTokens(player.getCurrencyPouch().getInvasionTokens() + floorItem.getAmount());
				break;
			case 12852:
				player.getCurrencyPouch().setEradicatedSeals(player.getCurrencyPouch().getEradicatedSeals() + floorItem.getAmount());
				break;
			case 2996:
				player.getCurrencyPouch().set100MTicket(player.getCurrencyPouch().get100MTicket() + floorItem.getAmount());
				break;
			default:
				player.getInventory().addItem(floorItem.getId(),
						floorItem.getAmount());
				break;
			}
		}
		region.forceGetFloorItems().remove(floorItem);
		if (floorItem.isInvisible() || floorItem.isGrave()) {
			player.getPackets().sendRemoveGroundItem(floorItem);
			return true;
		} else {
			for (Player p2 : World.getPlayers()) {
				if (p2 == null || !p2.hasStarted() || p2.hasFinished()
						|| p2.getPlane() != floorItem.getTile().getPlane()
						|| !p2.getMapRegionsIds().contains(regionId))
					continue;
				p2.getPackets().sendRemoveGroundItem(floorItem);
			}
			return true;
		}
	}

	public static final void sendObjectAnimation(WorldObject object,
			Animation animation) {
		sendObjectAnimation(null, object, animation);
	}

	public static final void sendObjectAnimation(Entity creator,
			WorldObject object, Animation animation) {
		if (creator == null) {
			for (Player player : World.getPlayers()) {
				if (player == null || !player.hasStarted()
						|| player.hasFinished()
						|| !player.withinDistance(object))
					continue;
				player.getPackets().sendObjectAnimation(object, animation);
			}
		} else {
			for (int regionId : creator.getMapRegionsIds()) {
				List<Integer> playersIndexes = getRegion(regionId)
						.getPlayerIndexes();
				if (playersIndexes == null)
					continue;
				for (Integer playerIndex : playersIndexes) {
					Player player = players.get(playerIndex);
					if (player == null || !player.hasStarted()
							|| player.hasFinished()
							|| !player.withinDistance(object))
						continue;
					player.getPackets().sendObjectAnimation(object, animation);
				}
			}
		}
	}

	public static final void sendGraphics(Entity creator, Graphics graphics,
			WorldTile tile) {
		if (creator == null) {
			for (Player player : World.getPlayers()) {
				if (player == null || !player.hasStarted()
						|| player.hasFinished() || !player.withinDistance(tile))
					continue;
				player.getPackets().sendGraphics(graphics, tile);
			}
		} else {
			for (int regionId : creator.getMapRegionsIds()) {
				List<Integer> playersIndexes = getRegion(regionId)
						.getPlayerIndexes();
				if (playersIndexes == null)
					continue;
				for (Integer playerIndex : playersIndexes) {
					Player player = players.get(playerIndex);
					if (player == null || !player.hasStarted()
							|| player.hasFinished()
							|| !player.withinDistance(tile))
						continue;
					player.getPackets().sendGraphics(graphics, tile);
				}
			}
		}
	}

	public static final void sendProjectile(Entity shooter,
			WorldTile startTile, WorldTile receiver, int gfxId,
			int startHeight, int endHeight, int speed, int delay, int curve,
			int startDistanceOffset) {
		for (int regionId : shooter.getMapRegionsIds()) {
			List<Integer> playersIndexes = getRegion(regionId)
					.getPlayerIndexes();
			if (playersIndexes == null)
				continue;
			for (Integer playerIndex : playersIndexes) {
				Player player = players.get(playerIndex);
				if (player == null
						|| !player.hasStarted()
						|| player.hasFinished()
						|| (!player.withinDistance(shooter) && !player
								.withinDistance(receiver)))
					continue;
				player.getPackets().sendProjectile(null, startTile, receiver,
						gfxId, startHeight, endHeight, speed, delay, curve,
						startDistanceOffset, 1);
			}
		}
	}

	public static final void sendProjectile(WorldTile shooter, Entity receiver,
			int gfxId, int startHeight, int endHeight, int speed, int delay,
			int curve, int startDistanceOffset) {
		for (int regionId : receiver.getMapRegionsIds()) {
			List<Integer> playersIndexes = getRegion(regionId)
					.getPlayerIndexes();
			if (playersIndexes == null)
				continue;
			for (Integer playerIndex : playersIndexes) {
				Player player = players.get(playerIndex);
				if (player == null
						|| !player.hasStarted()
						|| player.hasFinished()
						|| (!player.withinDistance(shooter) && !player
								.withinDistance(receiver)))
					continue;
				player.getPackets().sendProjectile(null, shooter, receiver,
						gfxId, startHeight, endHeight, speed, delay, curve,
						startDistanceOffset, 1);
			}
		}
	}

	public static final void sendProjectile(Entity shooter, WorldTile receiver,
			int gfxId, int startHeight, int endHeight, int speed, int delay,
			int curve, int startDistanceOffset) {
		for (int regionId : shooter.getMapRegionsIds()) {
			List<Integer> playersIndexes = getRegion(regionId)
					.getPlayerIndexes();
			if (playersIndexes == null)
				continue;
			for (Integer playerIndex : playersIndexes) {
				Player player = players.get(playerIndex);
				if (player == null
						|| !player.hasStarted()
						|| player.hasFinished()
						|| (!player.withinDistance(shooter) && !player
								.withinDistance(receiver)))
					continue;
				player.getPackets().sendProjectile(null, shooter, receiver,
						gfxId, startHeight, endHeight, speed, delay, curve,
						startDistanceOffset, shooter.getSize());
			}
		}
	}

	public static final void sendProjectile(Entity shooter, Entity receiver,
			int gfxId, int startHeight, int endHeight, int speed, int delay,
			int curve, int startDistanceOffset) {
		for (int regionId : shooter.getMapRegionsIds()) {
			List<Integer> playersIndexes = getRegion(regionId)
					.getPlayerIndexes();
			if (playersIndexes == null)
				continue;
			for (Integer playerIndex : playersIndexes) {
				Player player = players.get(playerIndex);
				if (player == null
						|| !player.hasStarted()
						|| player.hasFinished()
						|| (!player.withinDistance(shooter) && !player
								.withinDistance(receiver)))
					continue;
				int size = shooter.getSize();
				player.getPackets().sendProjectile(
						receiver,
						new WorldTile(shooter.getCoordFaceX(size), shooter
								.getCoordFaceY(size), shooter.getPlane()),
						receiver, gfxId, startHeight, endHeight, speed, delay,
						curve, startDistanceOffset, size);
			}
		}
	}

	public static final boolean isIronmanArea(WorldTile tile) {
		int destX = tile.getX();
		int destY = tile.getY();
		return (destX >= 3021 && destX <= 3055 && destY >= 9866 && destY <= 9906);

	}	
	
	public static final boolean isNearGypsy(WorldTile tile) {
		int destX = tile.getX();
		int destY = tile.getY();
		return (destX >= 3199 && destX <= 3208 && destY >= 3421 && destY <= 3428);

	}		
	
	public static final boolean isGroundFloor(WorldTile tile) {
		int destX = tile.getX();
		int destY = tile.getY();
		int plane = tile.getPlane();
		return (destX >= 3952 && destX <= 3984 && destY >= 4803 && destY <= 4831 && plane == 0);

	}		
	
	public static final boolean isDonatorArea(WorldTile tile) {
		int destX = tile.getX();
		int destY = tile.getY();
		return (destX >= 2846 && destX <= 2864 && destY >= 5215 && destY <= 5231)
				||  (destX >= 1961 && destX <= 1983 && destY >= 5031 && destY <= 5056);

	}	
	
	public static final boolean isExtremeArea(WorldTile tile) {
		int destX = tile.getX();
		int destY = tile.getY();
		return (destX >= 1855 && destX <= 1926 && destY >= 4925 && destY <= 4968)
				||  (destX >= 2864 && destX <= 2875 && destY >= 5200 && destY <= 5214);

	}
	
	public static final boolean isSuperArea(WorldTile tile) {
		int destX = tile.getX();
		int destY = tile.getY();
		return (destX >= 2500 && destX <= 2527 && destY >= 3070 && destY <= 3097)
			   || (destX >= 3332 && destX <= 3373 && destY >= 3331 && destY <= 3351)
			   || (destX >= 2336 && destX <= 2351 && destY >= 5600 && destY <= 5615);
	}
	
	public static final boolean isEradicatorArea(WorldTile tile) {
		int destX = tile.getX();
		int destY = tile.getY();
		return (destX >= 3166 && destX <= 3200 && destY >= 5698 && destY <= 5725)
				||  (destX >= 2318 && destX <= 2335 && destY >= 5599 && destY <= 5616)
				||  (destX >= 2352 && destX <= 2372 && destY >= 5548 && destY <= 5568);

	}	
	
	
	public static final boolean isMultiArea(WorldTile tile) {
		int destX = tile.getX();
		int destY = tile.getY();
		return (destX >= 3462 && destX <= 3511 && destY >= 9481
				&& destY <= 9521 && tile.getPlane() == 0) // kalphite queen lair
				|| (destX >= 4540 && destX <= 4799 && destY >= 5052
						&& destY <= 5183 && tile.getPlane() == 0) // thzaar city
				|| (destX >= 1721 && destX <= 1791 && destY >= 5123 && destY <= 5249) // mole
				|| (destX >= 3029 && destX <= 3374 && destY >= 3759 && destY <= 3903)// wild
				|| (destX >= 2250 && destX <= 2280 && destY >= 4670 && destY <= 4720)
				|| (destX >= 3198 && destX <= 3380 && destY >= 3904 && destY <= 3970)
				|| (destX >= 3191 && destX <= 3326 && destY >= 3510 && destY <= 3759)
				|| (destX >= 2987 && destX <= 3006 && destY >= 3912 && destY <= 3937)
				|| (destX >= 3111 && destX <= 3094 && destY >= 3110 && destY <= 3115) //donor boss
				|| (destX >= 2245 && destX <= 2295 && destY >= 4675 && destY <= 4720)
				|| (destX >= 2450 && destX <= 3520 && destY >= 9450 && destY <= 9550)
				|| (destX >= 3006 && destX <= 3071 && destY >= 3602 && destY <= 3710)
				|| (destX >= 3134 && destX <= 3192 && destY >= 3519 && destY <= 3646)
				|| (destX >= 2882 && destX <= 2942 && destY >= 4413 && destY <= 4479) // Daggonoths
				|| (destX >= 2815 && destX <= 2966 && destY >= 5240 && destY <= 5375)// wild
				|| (destX >= 2840 && destX <= 2950 && destY >= 5190 && destY <= 5230) // godwars
				|| (destX >= 3547 && destX <= 3555 && destY >= 9690 && destY <= 9699) // zaros
				// godwars
				|| KingBlackDragon.atKBD(tile) // King Black Dragon lair
				|| TormentedDemon.atTD(tile) // Tormented demon's area
				|| Bork.atBork(tile) // Bork's area
				|| (destX >= 2970 && destX <= 3000 && destY >= 4365 && destY <= 4400)// corp
				|| (destX >= 3195 && destX <= 3327 && destY >= 3520
						&& destY <= 3970 || (destX >= 2376 && 5127 >= destY
						&& destX <= 2422 && 5168 <= destY))
				|| (destX >= 2374 && destY >= 5129 && destX <= 2424 && destY <= 5168) // pits
				|| (destX >= 2622 && destY >= 5696 && destX <= 2573 && destY <= 5752) // torms
				|| (destX >= 2368 && destY >= 3072 && destX <= 2431 && destY <= 3135) // castlewars
				// out
				|| (destX >= 2365 && destY >= 9470 && destX <= 2436 && destY <= 9532) // castlewars
				|| (destX >= 2948 && destY >= 5537 && destX <= 3071 && destY <= 5631) // Risk
				// ffa.
				|| (destX >= 2756 && destY >= 5537 && destX <= 2879 && destY <= 5631) // Safe
																						// ffa

				|| (tile.getX() >= 3011 && tile.getX() <= 3132
						&& tile.getY() >= 10052 && tile.getY() <= 10175 && (tile
						.getY() >= 10066 || tile.getX() >= 3094)) // fortihrny
																	// dungeon
		;
		// in

		// multi
	}

	public static final boolean isPvpArea(WorldTile tile) {
		return Wilderness.isAtWild(tile);
	}

	public static void destroySpawnedObject(WorldObject object, boolean clip) {
		int regionId = object.getRegionId();
		int baseLocalX = object.getX() - ((regionId >> 8) * 64);
		int baseLocalY = object.getY() - ((regionId & 0xff) * 64);
		WorldObject realMapObject = getRegion(regionId).getRealObject(object);

		World.getRegion(regionId).removeObject(object);
		if (clip)
			World.getRegion(regionId).removeMapObject(object, baseLocalX,
					baseLocalY);
		for (Player p2 : World.getPlayers()) {
			if (p2 == null || !p2.hasStarted() || p2.hasFinished()
					|| !p2.getMapRegionsIds().contains(regionId))
				continue;
			if (realMapObject != null)
				p2.getPackets().sendSpawnedObject(realMapObject);
			else
				p2.getPackets().sendDestroyObject(object);
		}
	}

	public static void destroySpawnedObject(WorldObject object) {
		int regionId = object.getRegionId();
		int baseLocalX = object.getX() - ((regionId >> 8) * 64);
		int baseLocalY = object.getY() - ((regionId & 0xff) * 64);
		World.getRegion(regionId).removeObject(object);
		World.getRegion(regionId).removeMapObject(object, baseLocalX,
				baseLocalY);
		for (Player p2 : World.getPlayers()) {
			if (p2 == null || !p2.hasStarted() || p2.hasFinished()
					|| !p2.getMapRegionsIds().contains(regionId))
				continue;
			p2.getPackets().sendDestroyObject(object);
		}
	}

	public static final void spawnTempGroundObject(final WorldObject object,
			final int replaceId, long time) {
		final int regionId = object.getRegionId();
		WorldObject realMapObject = getRegion(regionId).getRealObject(object);
		final WorldObject realObject = realMapObject == null ? null
				: new WorldObject(realMapObject.getId(),
						realMapObject.getType(), realMapObject.getRotation(),
						object.getX(), object.getY(), object.getPlane());
		spawnObject(object, false);
		CoresManager.slowExecutor.schedule(new Runnable() {
			@Override
			public void run() {
				try {
					getRegion(regionId).removeObject(object);
					addGroundItem(new Item(replaceId), object, null, null, false,
							180, false);
					for (Player p2 : players) {
						if (p2 == null || !p2.hasStarted() || p2.hasFinished()
								|| p2.getPlane() != object.getPlane()
								|| !p2.getMapRegionsIds().contains(regionId))
							continue;
						if (realObject != null)
							p2.getPackets().sendSpawnedObject(realObject);
						else
							p2.getPackets().sendDestroyObject(object);
					}
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}
		}, time, TimeUnit.MILLISECONDS);
	}

	public static void sendWorldMessage(String message, boolean forStaff) {
		for (Player p : World.getPlayers()) {
			if (p == null || !p.isRunning() || p.isYellOff() || (forStaff && !p.checkStaff()))
				continue;
			p.getPackets().sendGameMessage(message.replace("colorhere", p.fontColor()));
		}
	}

	public static final void sendProjectile(WorldObject object,
			WorldTile startTile, WorldTile endTile, int gfxId, int startHeight,
			int endHeight, int speed, int delay, int curve, int startOffset) {
		for (Player pl : players) {
			if (pl == null || !pl.withinDistance(object, 20))
				continue;
			pl.getPackets()
					.sendProjectile(null, startTile, endTile, gfxId,
							startHeight, endHeight, speed, delay, curve,
							startOffset, 1);
		}
	}

	public static int getIdFromName(String playerName) {
		for (Player p : players) {
			if (p == null) {
				continue;
			}
			if (p.getUsername().equalsIgnoreCase(
					Utils.formatPlayerNameForProtocol(playerName))) {
				return p.getIndex();
			}
		}
		return 0;
	}

}