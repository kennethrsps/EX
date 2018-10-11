package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class Something2Combat extends CombatScript {

	public static final String[] ATTACKS = new String[] {
			"Yes im owner... And i have to kill you with my bow ;)", "He-he God mod on >:D",
			"Wait you can hit trought god ?", "Lemme eat please you can kill me by accident!",
			"He-he i got like 300000 hp you can't kill me ;)" };

	@Override
	public Object[] getKeys() {
		return new Object[] { 15006 };
	}

	@Override
	public int attack(final NPC npc, Entity target) {
		int attackStyle = Utils.random(3);
		if (attackStyle == 0) {
			npc.setNextAnimation(new Animation(4230));
			final WorldTile center = new WorldTile(target);
			World.sendGraphics(npc, new Graphics(753), center);
			npc.setNextForceTalk(new ForceTalk("I'm a Death Lotus Ninja, I WILL KILL YOU!"));
			WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					for (Player player : World.getPlayers()) { // lets just loop
																// all players
																// for massive
																// moves
						if (player == null || player.isDead()
								|| player.hasFinished())
							continue;
						if (player.withinDistance(center, 1)) {
							if (!player.getMusicsManager().hasMusic(171))
								player.getMusicsManager().playMusic(171);
							delayHit(npc, 0, player,
									new Hit(npc, getRandomMaxHit(npc, 300,
											NPCCombatDefinitions.RANGE, player),
											HitLook.RANGE_DAMAGE));
						}
					}
				}

			}, 4);
		} else if (attackStyle == 1) {
			npc.setNextGraphics(new Graphics(457));
			npc.setNextAnimation(new Animation(4230));
			final WorldTile center = new WorldTile(target);
			World.sendGraphics(npc, new Graphics(1019), center);
			npc.setNextForceTalk(new ForceTalk("Yolo magic attack!"));
			WorldTasksManager.schedule(new WorldTask() {
				int count = 0;

				@Override
				public void run() {
					for (Player player : World.getPlayers()) { // lets just loop
																// all players
																// for massive
																// moves
						if (player == null || player.isDead()
								|| player.hasFinished())
							continue;
						if (player.withinDistance(center, 1)) {
							delayHit(npc, 0, player,
									new Hit(npc, getRandomMaxHit(npc, 140,
											NPCCombatDefinitions.RANGE, player),
											HitLook.MAGIC_DAMAGE));
						}
					}
					if (count++ == 2) {
						stop();
						return;
					}
				}
			}, 0, 0);
		} else if (attackStyle == 2) {
			npc.setNextAnimation(new Animation(829));
			npc.heal(350);
			npc.setNextForceTalk(new ForceTalk("Phew got some food left :D"));
		}
		return 4;
	}

}

