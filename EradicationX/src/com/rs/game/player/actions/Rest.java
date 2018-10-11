package com.rs.game.player.actions;

import com.rs.game.Animation;
import com.rs.game.player.Player;
import com.rs.utils.Utils;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

public class Rest extends Action {

	private static int[][] REST_DEFS = { { 5713, 1549, 5748 }, { 11786, 1550, 11788 }, { 5713, 1551, 2921 } };
	private int index;

	@Override
	public boolean start(Player player) {
		if (!process(player))
			return false;
		index = Utils.random(REST_DEFS.length);
		player.setResting(true);
		player.setNextAnimation(new Animation(REST_DEFS[index][0]));
		player.getAppearence().setRenderEmote(REST_DEFS[index][1]);
		startRestore(player);
		return true;
	}

	@Override
	public boolean process(Player player) {
		if (player.getPoison().isPoisoned()) {
			player.getPackets().sendGameMessage("You can't rest while you're poisoned.");
			return false;
		}
		if (player.getAttackedByDelay() + 10000 > Utils.currentTimeMillis()) {
			player.getPackets().sendGameMessage("You can't rest until 10 seconds after the end of combat.");
			return false;
		}
		return true;
	}

	@Override
	public int processWithDelay(Player player) {
		return 0;
	}

	@Override
	public void stop(Player player) {
		player.setResting(false);
		player.setNextAnimation(new Animation(REST_DEFS[index][2]));
		player.getEmotesManager().setNextEmoteEnd();
		player.getAppearence().setRenderEmote(-1);
	}

	public void startRestore(final Player player) {
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				if (!player.isResting()) {
					stop();
					return;
				}
				if (player.getRunEnergy() >= 100) {
					player.setRunEnergy(100);
					stop();
					return;
				}
				player.setRunEnergy(player.getRunEnergy() + 2);
			}
		}, 0, 1);
	}

}