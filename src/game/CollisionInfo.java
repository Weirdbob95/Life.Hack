package game;

import util.Vec3;

public class CollisionInfo {

    public final Vec3 prevPos, pos, dir;
    public boolean hitX, hitY, hitZ;

    public CollisionInfo(Vec3 prevPos, Vec3 pos) {
        this.prevPos = prevPos;
        this.pos = pos;
        dir = pos.subtract(prevPos);
    }

    public Vec3 normal() {
        return new Vec3(hitX ? Math.signum(dir.x) : 0, hitY ? Math.signum(dir.y) : 0, hitZ ? Math.signum(dir.z) : 0).reverse();
    }
}
