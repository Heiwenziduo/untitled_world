package com.github.nahnullscience.cypher_nexus.datagen.provider

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.ModBlocks
import net.minecraft.data.PackOutput
import net.minecraft.resources.ResourceLocation
import net.neoforged.neoforge.client.model.generators.BlockModelProvider
import net.neoforged.neoforge.client.model.generators.BlockStateProvider
import net.neoforged.neoforge.client.model.generators.ModelFile
import net.neoforged.neoforge.common.data.ExistingFileHelper


class ClientBlockStateProvider(output: PackOutput, exFileHelper: ExistingFileHelper) :
    BlockStateProvider(output, CypherNexus.MOD_ID, exFileHelper
) {
    override fun registerStatesAndModels() {
        val exampleModel: ModelFile = models().withExistingParent(
            "example_model",
            this.mcLoc("block/cobblestone")
        )
        val exampleTexture: ResourceLocation = modLoc("block/example_texture")

        /* @doc
         * BlockModelProvider
         * #withExistingParent  returns a new model builder with the given parent
         * #getExistingFile  performs a lookup in the model provider's ExistingFileHelper, return a ModelFile or throw
         * #cubeAll  a mix between singleTexture and cube
         * #cross  used by flowers, saplings and many other foliage blocks
         * */
        val model: BlockModelProvider = models()
        val test_cube = model.cubeAll("test_cube", modLoc("block/test_cube"))

        simpleBlockWithItem(ModBlocks.CYPHER_INDEX_BLOCK, test_cube)
    }
}