package de.jobrain.villager_pathfinding_fix.mixin;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1282;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1309;
import net.minecraft.class_1439;
import net.minecraft.class_1646;
import net.minecraft.class_1922;
import net.minecraft.class_1937;
import net.minecraft.class_2323;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_2769;
import net.minecraft.class_3988;
import net.minecraft.class_4095;
import net.minecraft.class_4140;
import net.minecraft.class_4142;
import net.minecraft.class_4208;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_1646.class})
public abstract class VillagerEntityMixin
extends class_3988 {
    @Unique
    private int stuckTicks = 0;
    @Unique
    private double lastX;
    @Unique
    private double lastY;
    @Unique
    private double lastZ;
    @Unique
    private int yieldTimer = 0;
    @Unique
    private int lastHurtTimeCheck = 0;
    @Unique
    private int panicTimer = 0;
    @Unique
    private class_1309 panicAttacker = null;

    protected VillagerEntityMixin(class_1299<? extends class_3988> entityType, class_1937 world) {
        super(entityType, world);
    }

    @Inject(method={"method_5773"}, at={@At(value="HEAD")})
    private void onTick(CallbackInfo ci) {
        class_1646 villager = (class_1646)this;
        if (!villager.method_73183().method_8608() && this.method_5805()) {
            Optional brainAttacker;
            if (villager.field_6235 > this.lastHurtTimeCheck) {
                class_1309 attacker = villager.method_6065();
                class_1282 source = villager.method_6081();
                if (attacker != null) {
                    this.triggerPanic(attacker);
                    this.spreadPanic(attacker, source);
                }
            }
            this.lastHurtTimeCheck = villager.field_6235;
            if (this.panicTimer == 0 && (brainAttacker = villager.method_18868().method_18904(class_4140.field_18452)).isPresent()) {
                this.triggerPanic((class_1309)brainAttacker.get());
            }
            if (this.panicTimer > 0) {
                --this.panicTimer;
                this.handlePanicLogic();
            }
            if (this.yieldTimer > 0) {
                --this.yieldTimer;
                if (this.isInDoor(villager)) {
                    this.yieldTimer = 0;
                } else {
                    if (villager.method_5942().method_23966()) {
                        villager.method_5942().method_6340();
                    }
                    return;
                }
            }
            this.checkStuck();
            if (villager.method_18868().method_18896(class_4140.field_18445)) {
                this.handleDoorLogic();
                this.checkVillagerCollision();
            }
        }
    }

    @Unique
    private void triggerPanic(class_1309 attacker) {
        this.panicTimer = 1200;
        this.panicAttacker = attacker;
    }

    @Unique
    private void spreadPanic(class_1309 attacker, class_1282 source) {
        class_1646 me = (class_1646)this;
        class_1937 world = me.method_73183();
        class_238 alarmRadius = me.method_5829().method_1014(15.0);
        List neighbors = world.method_8390(class_1646.class, alarmRadius, class_1297::method_5805);
        for (class_1646 neighbor : neighbors) {
            if (neighbor == me) continue;
            class_4095 brain = neighbor.method_18868();
            brain.method_18878(class_4140.field_18452, (Object)attacker);
            brain.method_18878(class_4140.field_18451, (Object)source);
        }
    }

    @Unique
    private void handlePanicLogic() {
        class_1646 me = (class_1646)this;
        class_4095 brain = me.method_18868();
        class_1937 world = me.method_73183();
        if (this.panicAttacker == null || !this.panicAttacker.method_5805()) {
            this.panicTimer = 0;
            return;
        }
        if (me.field_6012 % 10 != 0) {
            return;
        }
        class_238 searchBox = me.method_5829().method_1009(50.0, 20.0, 50.0);
        List golems = world.method_8390(class_1439.class, searchBox, class_1297::method_5805);
        if (!golems.isEmpty()) {
            golems.sort(Comparator.comparingDouble(g -> g.method_5858((class_1297)me)));
            class_1439 protector = (class_1439)golems.get(0);
            if (this.panicAttacker.method_5805() && !(this.panicAttacker instanceof class_1439)) {
                protector.method_5980(this.panicAttacker);
            }
            brain.method_18878(class_4140.field_18445, (Object)new class_4142((class_1297)protector, 0.8f, 2));
            return;
        }
        Optional homePos = brain.method_18904(class_4140.field_18438);
        if (homePos.isPresent() && ((class_4208)homePos.get()).comp_2207() == world.method_27983()) {
            return;
        }
        class_243 attackerPos = new class_243(this.panicAttacker.method_23317(), this.panicAttacker.method_23318(), this.panicAttacker.method_23321());
        class_243 myPos = new class_243(me.method_23317(), me.method_23318(), me.method_23321());
        class_243 fleeDir = myPos.method_1020(attackerPos).method_1029();
        class_243 targetVec = myPos.method_1019(fleeDir.method_1021(15.0));
        class_2338 targetPos = new class_2338((int)targetVec.field_1352, (int)targetVec.field_1351, (int)targetVec.field_1350);
        brain.method_18878(class_4140.field_18445, (Object)new class_4142(targetPos, 0.9f, 2));
    }

    @Unique
    private boolean isInDoor(class_1646 villager) {
        class_2338 pos = villager.method_24515();
        return villager.method_73183().method_8320(pos).method_26204() instanceof class_2323;
    }

    @Unique
    private void handleDoorLogic() {
        class_1646 me = (class_1646)this;
        class_1937 world = me.method_73183();
        class_2350 facing = this.method_5735();
        class_2338 posInFront = this.method_24515().method_10093(facing);
        class_2680 state = world.method_8320(posInFront);
        if (!(state.method_26204() instanceof class_2323) && !((state = world.method_8320(posInFront = this.method_24515())).method_26204() instanceof class_2323)) {
            return;
        }
        class_238 doorBox = new class_238(posInFront).method_1014(2.0);
        List villagersAtDoor = world.method_8390(class_1646.class, doorBox, class_1297::method_5805);
        class_2338 finalDoorPos = posInFront;
        villagersAtDoor.sort(Comparator.comparingDouble(v -> v.method_5707(finalDoorPos.method_46558())));
        boolean amIClosest = !villagersAtDoor.isEmpty() && villagersAtDoor.get(0) == me;
        int villagersCount = villagersAtDoor.size();
        boolean isBlocking = this.isDoorBlocking(world, posInFront, state, facing);
        if (isBlocking) {
            if (amIClosest) {
                world.method_8501(posInFront, (class_2680)state.method_28493((class_2769)class_2323.field_10945));
            }
        } else if (villagersCount > 1) {
            boolean currentOpenState = (Boolean)state.method_11654((class_2769)class_2323.field_10945);
            class_2680 currentStateInWorld = world.method_8320(posInFront);
            if (currentStateInWorld.method_26204() instanceof class_2323 && (Boolean)currentStateInWorld.method_11654((class_2769)class_2323.field_10945) != currentOpenState) {
                world.method_8501(posInFront, state);
            }
        }
    }

    @Unique
    private boolean isDoorBlocking(class_1937 world, class_2338 pos, class_2680 state, class_2350 facing) {
        class_265 shape = state.method_26220((class_1922)world, pos);
        if (shape.method_1110()) {
            return false;
        }
        double xSize = shape.method_1107().method_17939();
        double zSize = shape.method_1107().method_17941();
        if (facing.method_10166() == class_2350.class_2351.field_11051) {
            return xSize > 0.8;
        }
        if (facing.method_10166() == class_2350.class_2351.field_11048) {
            return zSize > 0.8;
        }
        return false;
    }

    @Unique
    private void checkVillagerCollision() {
        class_1646 me = (class_1646)this;
        if (this.isInDoor(me)) {
            return;
        }
        class_238 box = me.method_5829().method_1009(0.2, 0.0, 0.2);
        List neighbors = me.method_73183().method_8390(class_1646.class, box, other -> other != me && other.method_5805());
        for (class_1646 other2 : neighbors) {
            if (!other2.method_5942().method_23966() && !(other2.method_18798().method_1027() > 0.01) || me.method_5628() <= other2.method_5628()) continue;
            this.yieldTo(other2);
            break;
        }
    }

    @Unique
    private void yieldTo(class_1646 other) {
        class_1646 me = (class_1646)this;
        me.method_5942().method_6340();
        this.yieldTimer = 20;
        double dx = me.method_23317() - other.method_23317();
        double dy = me.method_23318() - other.method_23318();
        double dz = me.method_23321() - other.method_23321();
        class_243 direction = new class_243(dx, dy, dz).method_1029();
        me.method_5762(direction.field_1352 * 0.3, 0.0, direction.field_1350 * 0.3);
        me.field_64356 = true;
    }

    @Unique
    private void checkStuck() {
        class_1646 villager = (class_1646)this;
        class_4095 brain = villager.method_18868();
        if (brain.method_18896(class_4140.field_18445)) {
            double distSq = this.method_5649(this.lastX, this.lastY, this.lastZ);
            this.stuckTicks = this.method_5942().method_23966() ? (distSq < 1.0E-4 ? ++this.stuckTicks : 0) : 0;
            if (this.stuckTicks > 60) {
                brain.method_18875(class_4140.field_18445);
                brain.method_18875(class_4140.field_18449);
                this.stuckTicks = 0;
            }
        } else {
            this.stuckTicks = 0;
        }
        this.lastX = this.method_23317();
        this.lastY = this.method_23318();
        this.lastZ = this.method_23321();
    }
}
