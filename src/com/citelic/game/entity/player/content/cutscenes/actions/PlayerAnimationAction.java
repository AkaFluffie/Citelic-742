package com.citelic.game.entity.player.content.cutscenes.actions;

import com.citelic.game.entity.Animation;
import com.citelic.game.entity.player.Player;

public class PlayerAnimationAction extends CutsceneAction {

	private Animation anim;

	public PlayerAnimationAction(Animation anim, int actionDelay) {
		super(-1, actionDelay);
		this.anim = anim;
	}

	@Override
	public void process(Player player, Object[] cache) {
		player.setNextAnimation(anim);
	}

}
