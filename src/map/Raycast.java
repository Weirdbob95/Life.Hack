package map;

import game.Enemy;
import static game.Enemy.HITBOX_SIZE;
import static map.CubeMap.isSolid;
import util.RegisteredEntity;
import util.Vec3;

public class Raycast {

    public Enemy hitEnemy;
    public boolean hitWall;
    public Vec3 hitPos;
    public Vec3 normal;

    public Raycast(Vec3 start, Vec3 end) {
        Vec3 dir = end.subtract(start);
        hitPos = end;

        //Check against walls
        Vec3 cp = start;
        while (!isSolid(cp)) {
            Vec3 relPos = cp.perComponent(d -> d % 1);
            Vec3 time = relPos.perComponent(dir, (x, d) -> (d < 0) ? -x / d : (1 - x) / d);
            double minTime = .001 + Math.min(time.x, Math.min(time.y, time.z));
            normal = time.perComponent(dir, (t, dt) -> (t + .001 == minTime) ? -Math.signum(dt) : 0);
            cp = cp.add(dir.multiply(minTime));
        }
        if (cp.subtract(start).lengthSquared() < dir.lengthSquared()) {
            hitWall = true;
            hitPos = cp;
        }

        //Check against enemies
        RegisteredEntity.getAll(Enemy.class).forEach(e -> {
            Vec3 pos = e.get("position", Vec3.class).get();
            Vec3 minB = pos.subtract(new Vec3(HITBOX_SIZE));
            Vec3 maxB = pos.add(new Vec3(HITBOX_SIZE));
            double[] hit = rayAABB(minB.toArray(), maxB.toArray(), start.toArray(), dir.toArray());
            if (hit != null && new Vec3(hit).subtract(start).lengthSquared() < hitPos.subtract(start).lengthSquared()) {
                normal = hitPos.perComponent(minB, (h, m) -> h == m ? -1. : 0).add(hitPos.perComponent(maxB, (h, m) -> h == m ? 1. : 0));
                hitEnemy = e;
                hitPos = new Vec3(hit);
            }
        });

        if (!hitWall && hitEnemy == null) {
            hitPos = null;
            normal = null;
        }
    }

    private static double[] rayAABB(double[] minB, double[] maxB, double[] start, double[] dir) {
        boolean inside = true;
        int[] quadrant = new int[3];
        double[] candidatePlane = new double[3];

        for (int i = 0; i < 3; i++) {
            if (start[i] < minB[i]) {
                quadrant[i] = -1;
                candidatePlane[i] = minB[i];
                inside = false;
            } else if (start[i] > maxB[i]) {
                quadrant[i] = 1;
                candidatePlane[i] = maxB[i];
                inside = false;
            }
        }
        if (inside) {
            return start;
        }

        double[] maxT = new double[3];
        for (int i = 0; i < 3; i++) {
            if (quadrant[i] != 0 && dir[i] != 0) {
                maxT[i] = (candidatePlane[i] - start[i]) / dir[i];
            } else {
                maxT[i] = -1;
            }
        }

        int whichPlane = 0;
        for (int i = 1; i < 3; i++) {
            if (maxT[i] > maxT[whichPlane]) {
                whichPlane = i;
            }
        }
        if (maxT[whichPlane] < 0) {
            return null;
        }

        double[] coord = new double[3];
        for (int i = 0; i < 3; i++) {
            if (whichPlane != i) {
                coord[i] = start[i] + maxT[whichPlane] * dir[i];
                if (coord[i] < minB[i] || coord[i] > maxB[i]) {
                    return null;
                }
            } else {
                coord[i] = candidatePlane[i];
            }
        }
        return coord;
    }
}
