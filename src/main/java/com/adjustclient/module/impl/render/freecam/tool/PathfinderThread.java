package com.adjustclient.module.impl.render.freecam.tool;

import com.adjustclient.module.impl.exploit.pathfinder.BlockPathfinder;
import com.adjustclient.module.impl.exploit.pathfinder.PathBlockChecker;
import com.adjustclient.module.impl.exploit.pathfinder.PathResult;

public final class PathfinderThread extends Thread {
    public volatile PendingAction pending;
    public final FreecamController controller;
    public Object field112;

    public PathfinderThread(FreecamController controller) { this.controller = controller; }

    @Override public void run() {
        while (!isInterrupted()) {
            synchronized (controller.lock) {
                try {
                    controller.lock.wait();
                } catch (InterruptedException interrupted) {
                    return;
                }
                PendingAction action = pending;
                if (action == null || action.complete) continue;
                BlockPathfinder pathfinder = new BlockPathfinder(
                        ((Number) controller.module.pathfinderTimeout.getValue()).longValue());
                PathResult result = pathfinder.findPath(action.start, action.destination);
                if (result.reached && result.path != null) {
                    new PathBlockChecker().simplify(result.path);
                    action.path = result.path;
                }
                action.complete = true;
            }
        }
    }
}
