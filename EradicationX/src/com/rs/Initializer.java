package com.rs;

import java.io.IOException;

import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemsEquipIds;
import com.rs.content.utils.IPMute;
import com.rs.content.utils.LendingAuctionManager;
import com.rs.cores.CoresManager;
import com.rs.game.RegionBuilder;
import com.rs.game.World;
import com.rs.game.npc.combat.CombatScriptsHandler;
import com.rs.game.player.LendingManager;
import com.rs.game.player.Raffle;
import com.rs.game.player.content.FishingSpotsHandler;
import com.rs.game.player.content.FriendChatsManager;
import com.rs.game.player.content.WorldVote;
import com.rs.game.player.content.clans.ClansManager;
import com.rs.game.player.controlers.ControlerHandler;
import com.rs.game.player.cutscenes.CutscenesHandler;
import com.rs.game.player.dialogues.DialogueHandler;
import com.rs.utils.DTRank;
import com.rs.utils.DisplayNames;
import com.rs.utils.DisplayNamesManager;
import com.rs.utils.DummyRank;
import com.rs.utils.IPBanL;
import com.rs.utils.ItemBonuses;
import com.rs.utils.ItemExamines;
import com.rs.utils.MacBanL;
import com.rs.utils.MapArchiveKeys;
import com.rs.utils.MapAreas;
import com.rs.utils.MusicHints;
import com.rs.utils.NPCBonuses;
import com.rs.utils.NPCCombatDefinitionsL;
import com.rs.utils.NPCDrops;
import com.rs.utils.NPCSpawns;
import com.rs.utils.ObjectSpawns;
import com.rs.utils.PkRank;
import com.rs.utils.PlayersOnline;
import com.rs.utils.RaffleWinner;
import com.rs.utils.ShopsHandler;
import com.rs.utils.VotingBoard;
import com.rs.utils.huffman.Huffman;

public class Initializer {

	public static void loadFiles() throws IOException {
		Cache.init();
		ItemsEquipIds.init();
		Huffman.init();
		DisplayNames.init();
		IPBanL.init();
		IPMute.init();
		PkRank.init();
		Raffle.init();
		RaffleWinner.init();
		DisplayNamesManager.init();
		DummyRank.init();
		DTRank.init();
		LendingManager.init();
		LendingAuctionManager.init();
		VotingBoard.init();
		WorldVote.init();
		MapArchiveKeys.init();
		MapAreas.init();
		ObjectSpawns.init();
		NPCSpawns.init();
		NPCCombatDefinitionsL.init();
		NPCBonuses.init();
		NPCDrops.init();
		ItemExamines.init();
		ItemBonuses.init();
		MusicHints.init();
		ShopsHandler.init();
		NPCDrops.init();
		FishingSpotsHandler.init();
		CombatScriptsHandler.init();
		DialogueHandler.init();
		ControlerHandler.init();
		CutscenesHandler.init();
		MacBanL.init();
		FriendChatsManager.init();
		ClansManager.init();
		CoresManager.init();
		World.init();
		RegionBuilder.init();
		//PlayersOnline.online();
	}
}
