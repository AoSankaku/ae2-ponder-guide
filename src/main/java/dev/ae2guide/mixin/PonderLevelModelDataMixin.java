package dev.ae2guide.mixin;

import net.createmod.ponder.api.level.PonderLevel;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Ponder's schematic level inherits an empty getModelData implementation.
 * AE2's cable and drive models need the data from their virtual block entities.
 */
@Mixin(value = PonderLevel.class, remap = false)
public abstract class PonderLevelModelDataMixin {
    public ModelData getModelData(BlockPos position) {
        var blockEntity = ((PonderLevel) (Object) this).getBlockEntity(position);
        return blockEntity == null ? ModelData.EMPTY : blockEntity.getModelData();
    }
}
