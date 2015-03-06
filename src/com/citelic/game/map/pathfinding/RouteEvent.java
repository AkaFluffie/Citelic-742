package com.citelic.game.map.pathfinding;

import com.citelic.game.entity.Entity;
import com.citelic.game.entity.player.Player;
import com.citelic.game.entity.player.item.FloorItem;
import com.citelic.game.map.objects.GameObject;
import com.citelic.game.map.pathfinding.strategy.EntityStrategy;
import com.citelic.game.map.pathfinding.strategy.FixedTileStrategy;
import com.citelic.game.map.pathfinding.strategy.FloorItemStrategy;
import com.citelic.game.map.pathfinding.strategy.ObjectStrategy;
import com.citelic.game.map.tile.Tile;

public class RouteEvent {

	/**
	 * Object to which we are finding the route.
	 */
	private Object object;
	/**
	 * The event instance.
	 */
	private Runnable event;
	/**
	 * Whether we also run on alternative.
	 */
	private boolean alternative;
	/**
	 * Contains last route strategies.
	 */
	private RouteStrategy[] last;

	public RouteEvent(Object object, Runnable event) {
		this(object, event, false);
	}

	public RouteEvent(Object object, Runnable event, boolean alternative) {
		this.object = object;
		this.event = event;
		this.alternative = alternative;
	}

	public boolean processEvent(final Player player) {
		if (!simpleCheck(player)) {
			player.getPackets().sendGameMessage("You can't reach that.");
			player.getPackets().sendResetMinimapFlag();
			return true;
		}
		RouteStrategy[] strategies = generateStrategies();
		if (last != null && match(strategies, last) && player.hasWalkSteps())
			return false;
		else if (last != null && match(strategies, last)
				&& !player.hasWalkSteps()) {
			for (int i = 0; i < strategies.length; i++) {
				RouteStrategy strategy = strategies[i];
				int steps = RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER,
						player.getX(), player.getY(), player.getZ(),
						player.getSize(), strategy,
						i == (strategies.length - 1));
				if (steps == -1)
					continue;
				if ((!RouteFinder.lastIsAlternative() && steps <= 0)
						|| alternative) {
					if (alternative)
						player.getPackets().sendResetMinimapFlag();
					event.run();
					return true;
				}
			}
			player.getPackets().sendGameMessage("You can't reach that.");
			player.getPackets().sendResetMinimapFlag();
			return true;
		} else {
			last = strategies;
			for (int i = 0; i < strategies.length; i++) {
				RouteStrategy strategy = strategies[i];
				int steps = RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER,
						player.getX(), player.getY(), player.getZ(),
						player.getSize(), strategy,
						i == (strategies.length - 1));
				if (steps == -1)
					continue;
				if ((!RouteFinder.lastIsAlternative() && steps <= 0)) {
					if (alternative)
						player.getPackets().sendResetMinimapFlag();
					event.run();
					return true;
				}
				int[] bufferX = RouteFinder.getLastPathBufferX();
				int[] bufferY = RouteFinder.getLastPathBufferY();

				Tile last = new Tile(bufferX[0], bufferY[0], player.getZ());
				player.resetWalkSteps();
				player.getPackets().sendMinimapFlag(
						last.getLocalX(player.getLastLoadedMapRegionTile(),
								player.getMapSize()),
						last.getLocalY(player.getLastLoadedMapRegionTile(),
								player.getMapSize()));
				for (int step = steps - 1; step >= 0; step--) {
					if (!player.addWalkSteps(bufferX[step], bufferY[step], 25,
							true))
						break;
				}

				return false;
			}
			player.getPackets().sendGameMessage("You can't reach that.");
			player.getPackets().sendResetMinimapFlag();
			return true;
		}
	}

	private boolean simpleCheck(Player player) {
		if (object instanceof Entity) {
			return player.getZ() == ((Entity) object).getZ();
		} else if (object instanceof GameObject) {
			return player.getZ() == ((GameObject) object).getZ();
		} else if (object instanceof FloorItem) {
			return player.getZ() == ((FloorItem) object).getTile().getZ();
		} else {
			throw new RuntimeException(object
					+ " is not instanceof any reachable entity.");
		}
	}

	private RouteStrategy[] generateStrategies() {
		if (object instanceof Entity) {
			return new RouteStrategy[] { new EntityStrategy((Entity) object) };
		} else if (object instanceof GameObject) {
			return new RouteStrategy[] { new ObjectStrategy((GameObject) object) };
		} else if (object instanceof FloorItem) {
			FloorItem item = (FloorItem) object;
			return new RouteStrategy[] {
					new FixedTileStrategy(item.getTile().getX(), item.getTile()
							.getY()), new FloorItemStrategy(item) };
		} else {
			throw new RuntimeException(object
					+ " is not instanceof any reachable entity.");
		}
	}

	private boolean match(RouteStrategy[] a1, RouteStrategy[] a2) {
		if (a1.length != a2.length)
			return false;
		for (int i = 0; i < a1.length; i++)
			if (!a1[i].equals(a2[i]))
				return false;
		return true;
	}

}
