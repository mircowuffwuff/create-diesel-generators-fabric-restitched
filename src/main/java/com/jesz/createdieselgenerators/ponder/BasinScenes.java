package com.jesz.createdieselgenerators.ponder;

import com.jesz.createdieselgenerators.fluids.FluidRegistry;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.createmod.ponder.foundation.element.InputWindowElement;
import net.createmod.catnip.math.Pointing;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import static com.jesz.createdieselgenerators.blocks.BasinLidBlock.ON_A_BASIN;

public class BasinScenes {
    public static void basin_lid(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("basin_fermenting_station", "Setting up a Basin Fermenting Station");
        scene.configureBasePlate(0, 0, 3);
        scene.showBasePlate();

        Selection basinSection = util.select().position(1, 1, 1);
        Selection basinLidSection = util.select().position(1, 2, 1);

        scene.idle(15);


        scene.world().showSection(basinSection, Direction.DOWN);
        scene.world().showSection(basinLidSection, Direction.DOWN);
        scene.overlay().showText(50)
                .attachKeyFrame()
                .text("Basin Lids allow you to create Ethanol.")
                .pointAt(util.vector().blockSurface(util.grid().at(1, 1, 1), Direction.NORTH))
                .placeNearTarget();
        scene.idle(60);
        scene.overlay().showText(30)
                .attachKeyFrame()
                .text("Give it some Sugar and Bone Meal...")
                .pointAt(util.vector().blockSurface(util.grid().at(1, 1, 1), Direction.NORTH))
                .placeNearTarget();
        scene.idle(30);
        scene.world().modifyBlock(util.grid().at(1, 2, 1), s -> s.setValue(ON_A_BASIN, false), false);
        scene.world().hideSection(basinLidSection, Direction.UP);
        scene.idle(10);
        scene.overlay().showControls(util.vector().topOf(1, 1, 1), Pointing.DOWN, 20).withItem(new ItemStack(Items.SUGAR));
        scene.idle(30);
        scene.overlay().showControls(util.vector().topOf(1, 1, 1), Pointing.DOWN, 20).withItem(new ItemStack(Items.BONE_MEAL));
        scene.idle(30);
        scene.world().showSection(basinLidSection, Direction.DOWN);
        scene.world().modifyBlock(util.grid().at(1, 2, 1), s -> s.setValue(ON_A_BASIN, false), false);
        scene.idle(20);
        scene.world().modifyBlock(util.grid().at(1, 2, 1), s -> s.setValue(ON_A_BASIN, true), false);
        scene.idle(50);

        scene.overlay().showText(30)
                .attachKeyFrame()
                .text("... Ethanol will be created.")
                .pointAt(util.vector().blockSurface(util.grid().at(1, 1, 1), Direction.NORTH))
                .placeNearTarget();
        scene.idle(40);
        scene.world().showSection(util.select().fromTo(3, 0, 1, 4, 1, 1), Direction.SOUTH);
        scene.world().showSection(util.select().position(2, 1, 1), Direction.SOUTH);
        scene.idle(20);
        scene.world().setKineticSpeed(util.select().position(3, 0, 0), 16f);
        scene.world().setKineticSpeed(util.select().position(3, 1, 1),-16f);
        scene.idle(10);
        FluidStack content = new FluidStack(FluidRegistry.ETHANOL.get()
                .getSource(), 50);
        //scene.world.modifyBlockEntity(tankPos, FluidTankBlockEntity.class, be -> TransferUtil.insertFluid(be.getTankInventory(), content));
        scene.world().modifyBlockEntity(util.grid().at(4, 0, 1), FluidTankBlockEntity.class, be ->
                TransferUtil.insertFluid(be.getTankInventory(), content));
        scene.idle(60);




    }
}
