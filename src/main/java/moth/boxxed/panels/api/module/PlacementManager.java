package moth.boxxed.panels.api.module;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.ryanhcode.sable.companion.ClientSubLevelAccess;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import moth.boxxed.panels.api.module.config.gui.ModuleConfigScreen;
import moth.boxxed.panels.api.panel.AbstractPanelBlock;
import moth.boxxed.panels.api.panel.AbstractPanelBlockEntity;
import moth.boxxed.panels.index.PanelKeybinds;
import moth.boxxed.panels.network.packet.PlaceModulePacket;
import moth.boxxed.panels.util.FlatAABB;
import moth.boxxed.panels.util.RectUtil;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.*;

import java.lang.Math;
import java.util.Objects;

public class PlacementManager {
    //Normal placing
    private static Module.Rotation rotation = Module.Rotation.ZERO;
    private static ModuleType<?> selectedType;
    private static int selectedTypeIndex = 0;
    private static ResourceLocation selectedItem;

    //Moving stuff
    private static Module movingModule = null;
    private static Vector2i movingOldPos = null;
    private static ModuleConfigScreen oldScreen = null;
    private static AbstractPanelBlockEntity oldParent = null;

    public static void startMovingModule(ModuleConfigScreen screen, Module module) {
        movingModule = module;
        movingOldPos = module.getPos();
        oldScreen = screen;
        oldParent = module.parentBlockEntity;
        Minecraft.getInstance().setScreen(null);
    }

    public static boolean isMovingModule(Module module) {
        return movingModule == module;
    }

    public static boolean isMovingModule() {
        return movingModule != null;
    }

    public static void stopMoving() {
        if (!tryPlaceMovingModule()) {
            movingModule.setPos(movingOldPos);
            movingModule.setParentBE(oldParent);
        }

        if (movingModule.parentBlockEntity != oldParent) {
            oldParent.removeModule(movingModule.name);
            movingModule.parentBlockEntity.addModule(movingModule.name, movingModule);
        }

        Minecraft.getInstance().setScreen(oldScreen);

        movingModule = null;
        movingOldPos = null;
        oldScreen = null;
        oldParent = null;
    }

    public static void renderMovingModule(Vec3 cameraPos, PoseStack poseStack) {
        if (!isMovingModule())
            return;

        HitResult hitResult = Minecraft.getInstance().hitResult;
        Level level = Minecraft.getInstance().level;
        Player player = Minecraft.getInstance().player;
        if (level == null)
            return;
        if (player == null)
            return;

        if (!(hitResult instanceof BlockHitResult blockHitResult))
            return;

        BlockPos pos = blockHitResult.getBlockPos();
        BlockState blockState = level.getBlockState(pos);
        if (!(blockState.getBlock() instanceof AbstractPanelBlock &&
                level.getBlockEntity(pos) instanceof AbstractPanelBlockEntity pbe)) {
            return;
        }

        Direction blockDirection = blockState.getValue(AbstractPanelBlock.FACING);
        Quaternionf blockRotation = Axis.YP.rotationDegrees(blockDirection.toYRot());
        Vec3 localSpace = blockHitResult.getLocation().subtract(pos.getBottomCenter());
        localSpace = new Vec3(blockRotation.transform(localSpace.toVector3f()));

        if (pbe.canPlaceModuleOnSurface(localSpace, blockHitResult.getDirection())) {
            Vector2i position = pbe.getPosForModule(localSpace);
            position.sub(
                    (int) (movingModule.getSize().x<=1 ? movingModule.getSize().x : movingModule.getSize().x/2),
                    (int) (movingModule.getSize().y<=1 ? movingModule.getSize().y : movingModule.getSize().y/2)
            );

            Vector2i contentAreaSize = pbe.getContentArea();

            Vector2d doublePos = RectUtil.clampAABBPosToAABB(
                    new FlatAABB(0, 0, contentAreaSize.x(), contentAreaSize.y()),
                    movingModule.getShape().move(position.x, position.y).getBounds()
            );

            position = new Vector2i(
                    (int) Math.floor(doublePos.x),
                    (int) Math.floor(doublePos.y)
            );
            movingModule.setPos(position);

            poseStack.pushPose();

            ClientSubLevelAccess subLevel = SableCompanion.INSTANCE.getContainingClient(pos);
            transformPoseToPlot(subLevel, poseStack, cameraPos);
            Vec3 translation = sublevelTranslation(Vec3.atLowerCornerOf(pos));
            poseStack.translate(pos.getX()-cameraPos.x()-translation.x, pos.getY()-cameraPos.y()-translation.y, pos.getZ()-cameraPos.z()-translation.z);

            poseStack.pushPose();
            poseStack.rotateAround(Axis.YP.rotationDegrees(blockDirection.toYRot() + (blockDirection.getAxis()== Direction.Axis.Z ? 0 : 180)), 0.5f, 0, 0.5f);
            poseStack.pushPose();
            pbe.renderTransform(poseStack);
            poseStack.pushPose();
            pbe.getIndividualModuleTransform().accept(movingModule, poseStack);

            double d0 = Util.getMillis()/300f;
            float pulse = (float) Mth.map(Math.sin(d0), -1, 1, 0.5, 1);

            MultiBufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
            VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());

            boolean hitsOtherModule = pbe.collidesWithOther(movingModule);
            float otherColors = hitsOtherModule ? 0 : 1;

            LevelRenderer.renderShape(poseStack, consumer, movingModule.getVoxelShape(), 0, 0, 0, 1, otherColors, otherColors, pulse);

            poseStack.popPose();
            poseStack.popPose();
            poseStack.popPose();
            poseStack.popPose();
        }
    }

    public static boolean tryPlaceMovingModule() {
        HitResult hitResult = Minecraft.getInstance().hitResult;
        Level level = Minecraft.getInstance().level;
        Player player = Minecraft.getInstance().player;
        if (level == null)
            return false;
        if (player == null)
            return false;

        if (!(hitResult instanceof BlockHitResult blockHitResult))
            return false;

        BlockPos pos = blockHitResult.getBlockPos();
        BlockState blockState = level.getBlockState(pos);
        if (!(blockState.getBlock() instanceof AbstractPanelBlock &&
                level.getBlockEntity(pos) instanceof AbstractPanelBlockEntity pbe)) {
            return false;
        }

        Direction blockDirection = blockState.getValue(AbstractPanelBlock.FACING);
        Quaternionf blockRotation = Axis.YP.rotationDegrees(blockDirection.toYRot());
        Vec3 localSpace = blockHitResult.getLocation().subtract(pos.getBottomCenter());
        localSpace = new Vec3(blockRotation.transform(localSpace.toVector3f()));

        if (pbe.canPlaceModuleOnSurface(localSpace, blockHitResult.getDirection())) {
            Vector2i position = pbe.getPosForModule(localSpace);
            position.sub(
                    (int) (movingModule.getSize().x<=1 ? movingModule.getSize().x : movingModule.getSize().x/2),
                    (int) (movingModule.getSize().y<=1 ? movingModule.getSize().y : movingModule.getSize().y/2)
            );

            Vector2i contentAreaSize = pbe.getContentArea();

            Vector2d doublePos = RectUtil.clampAABBPosToAABB(
                    new FlatAABB(0, 0, contentAreaSize.x(), contentAreaSize.y()),
                    movingModule.getShape().move(position.x, position.y).getBounds()
            );

            position = new Vector2i(
                    (int) Math.floor(doublePos.x),
                    (int) Math.floor(doublePos.y)
            );
            movingModule.setPos(position);
            movingModule.setParentBE(pbe);

            return !pbe.collidesWithOther(movingModule);
        }

        return false;
    }

    public static boolean attemptRotation(int keyCode, int action) {
        if (!PanelKeybinds.ROTATE_MODULE.matches(keyCode, action)) {
            return false;
        }

        if (isMovingModule() || (selectedType != null && selectedItem != null)) {
            rotation = rotation.next();
            return true;
        }

        return false;
    }

    public static void render(Vec3 cameraPos, PoseStack poseStack) {
        HitResult hitResult = Minecraft.getInstance().hitResult;
        Level level = Minecraft.getInstance().level;
        Player player = Minecraft.getInstance().player;
        if (level == null || player == null) {
            selectedType = null;
            selectedTypeIndex = -1;
            selectedItem = null;
            return;
        }

        ItemStack inHandItem = player.getMainHandItem();
        if (!ModuleType.isRegisteredModule(inHandItem.getItem()) || !(hitResult instanceof BlockHitResult blockHitResult)) {
            selectedType = null;
            selectedTypeIndex = -1;
            selectedItem = null;
            return;
        }

        BlockPos pos = blockHitResult.getBlockPos();
        BlockState blockState = level.getBlockState(pos);
        if (!(blockState.getBlock() instanceof AbstractPanelBlock &&
                level.getBlockEntity(pos) instanceof AbstractPanelBlockEntity pbe)) {
            selectedType = null;
            selectedTypeIndex = -1;
            selectedItem = null;
            return;
        }

        Direction blockDirection = blockState.getValue(AbstractPanelBlock.FACING);
        Quaternionf blockRotation = Axis.YP.rotationDegrees(blockDirection.toYRot());
        Vec3 localSpace = blockHitResult.getLocation().subtract(pos.getBottomCenter());
        localSpace = new Vec3(blockRotation.transform(localSpace.toVector3f()));

        if (pbe.canPlaceModuleOnSurface(localSpace, blockHitResult.getDirection())) {
            Vector2i position = pbe.getPosForModule(localSpace);
            //TODO: Make a selection thing similar to clutter no more

            ResourceLocation itemLoc = BuiltInRegistries.ITEM.getKey(inHandItem.getItem());
            if (!Objects.equals(selectedItem, itemLoc)) {
                selectedItem = itemLoc;
                selectedTypeIndex = 0;
            }
            selectedType = ModuleType.getTypeFromItem(inHandItem.getItem()).get(selectedTypeIndex);

            if (selectedType == null) {
                return;
            }

            Module module = selectedType.create(0, 0);
            module.setRotation(rotation);

            position.sub(
                    (int) (module.getSize().x<=1 ? module.getSize().x : module.getSize().x/2),
                    (int) (module.getSize().y<=1 ? module.getSize().y : module.getSize().y/2)
            );

            Vector2i contentAreaSize = pbe.getContentArea();

            Vector2d doublePos = RectUtil.clampAABBPosToAABB(
                    new FlatAABB(0, 0, contentAreaSize.x(), contentAreaSize.y()),
                    module.getShape().move(position.x, position.y).rotate(module.getRotation().getAngle()).getBounds()
            );

            position = new Vector2i(
                    (int) Math.floor(doublePos.x),
                    (int) Math.floor(doublePos.y)
            );
            module.setPos(position);

            poseStack.pushPose();

            ClientSubLevelAccess subLevel = SableCompanion.INSTANCE.getContainingClient(pos);
            transformPoseToPlot(subLevel, poseStack, cameraPos);
            Vec3 translation = sublevelTranslation(Vec3.atLowerCornerOf(pos));
            poseStack.translate(pos.getX()-cameraPos.x()-translation.x, pos.getY()-cameraPos.y()-translation.y, pos.getZ()-cameraPos.z()-translation.z);

            poseStack.pushPose();
            poseStack.rotateAround(Axis.YP.rotationDegrees(blockDirection.toYRot() + (blockDirection.getAxis()== Direction.Axis.Z ? 0 : 180)), 0.5f, 0, 0.5f);
            poseStack.pushPose();
            pbe.renderTransform(poseStack);
            poseStack.pushPose();
            pbe.getIndividualModuleTransform().accept(module, poseStack);

            double d0 = Util.getMillis()/300f;
            float pulse = (float) Mth.map(Math.sin(d0), -1, 1, 0.5, 1);

            MultiBufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
            VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());

            boolean hitsOtherModule = pbe.collidesWithOther(module);
            float otherColors = hitsOtherModule ? 0 : 1;

            Vector2f center = new Vector2f(
                    ((int) module.getSize().x)/32f,
                    ((int) module.getSize().y)/32f
            );
            poseStack.mulPose(Axis.YP.rotationDegrees(module.getRotation().getAngle()));
//            poseStack.rotateAround(Axis.YP.rotationDegrees(180), center.x, 0, center.y);
            LevelRenderer.renderShape(poseStack, consumer, module.getVoxelShape(), 0, 0, 0, 1, otherColors, otherColors, pulse);

            poseStack.popPose();
            poseStack.popPose();
            poseStack.popPose();
            poseStack.popPose();
        }
    }

    public static boolean tryPlaceModule() {
        if (selectedType == null) {
            return false;
        }

        HitResult hitResult = Minecraft.getInstance().hitResult;
        Level level = Minecraft.getInstance().level;
        Player player = Minecraft.getInstance().player;
        if (level == null)
            return false;
        if (player == null)
            return false;

        ItemStack inHandItem = player.getMainHandItem();
        if (!ModuleType.isRegisteredModule(inHandItem.getItem()))
            return false;
        if (!(hitResult instanceof BlockHitResult blockHitResult))
            return false;

        BlockPos pos = blockHitResult.getBlockPos();
        BlockState blockState = level.getBlockState(pos);
        if (!(blockState.getBlock() instanceof AbstractPanelBlock &&
                level.getBlockEntity(pos) instanceof AbstractPanelBlockEntity pbe)) {
            return false;
        }

        Direction blockDirection = blockState.getValue(AbstractPanelBlock.FACING);
        Quaternionf blockRotation = Axis.YP.rotationDegrees(blockDirection.toYRot());
        Vec3 localSpace = blockHitResult.getLocation().subtract(pos.getBottomCenter());
        localSpace = new Vec3(blockRotation.transform(localSpace.toVector3f()));

        if (pbe.canPlaceModuleOnSurface(localSpace, blockHitResult.getDirection())) {
            Vector2i position = pbe.getPosForModule(localSpace);
            //TODO: Make a selection thing similar to clutter no more
            Module module = selectedType.create(0, 0);
            module.setRotation(rotation);
            position.sub(
                    (int) (module.getSize().x<=1 ? module.getSize().x : module.getSize().x/2),
                    (int) (module.getSize().y<=1 ? module.getSize().y : module.getSize().y/2)
            );

            Vector2i contentAreaSize = pbe.getContentArea();

            Vector2d doublePos = RectUtil.clampAABBPosToAABB(
                    new FlatAABB(0, 0, contentAreaSize.x(), contentAreaSize.y()),
                    module.getShape().move(position.x, position.y).rotate(module.getRotation().getAngle()).getBounds()
            );

            position = new Vector2i(
                    (int) Math.floor(doublePos.x),
                    (int) Math.floor(doublePos.y)
            );
            module.setPos(position);

            boolean hitsOtherModule = pbe.collidesWithOther(module);

            if (!hitsOtherModule) {
                PacketDistributor.sendToServer(new PlaceModulePacket(pos, Module.ModuleInfo.fromModule(module, level.registryAccess())));
                return true;
            }
        }

        return false;
    }

    private static void transformPoseToPlot(ClientSubLevelAccess subLevel, PoseStack stack, Vec3 cameraPos) {
        if (subLevel != null) {
            Pose3dc pose = subLevel.renderPose();

            Vector3dc sublevelPos = pose.position();
            Vector3dc scale = pose.scale();
            Quaterniondc orientation = pose.orientation();

            stack.translate(sublevelPos.x() - cameraPos.x, sublevelPos.y() - cameraPos.y, sublevelPos.z() - cameraPos.z);
            stack.mulPose(new Quaternionf(orientation));
            stack.translate(cameraPos.x, cameraPos.y, cameraPos.z);
            stack.scale((float) scale.x(), (float) scale.y(), (float) scale.z());
        }
    }

    private static Vec3 sublevelTranslation(Vec3 center) {
        ClientSubLevelAccess subLevel = SableCompanion.INSTANCE.getContainingClient(center);
        if (subLevel != null) {
            Pose3dc pose = subLevel.renderPose();
            return JOMLConversion.toMojang(pose.rotationPoint()).scale(1);
        }
        return Vec3.ZERO;
    }
}
