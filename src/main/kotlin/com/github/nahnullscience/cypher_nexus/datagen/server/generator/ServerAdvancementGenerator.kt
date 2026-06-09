//package com.github.nahnullscience.cypher_nexus.datagen.server.generator
//
//import com.github.nahnullscience.cypher_nexus.CypherNexus
//import com.github.nahnullscience.cypher_nexus.init.ModBlocks
//import com.github.nahnullscience.cypher_nexus.init.ModItems
//import net.minecraft.advancements.*
//import net.minecraft.advancements.critereon.ImpossibleTrigger
//import net.minecraft.advancements.critereon.ItemUsedOnLocationTrigger
//import net.minecraft.core.HolderLookup
//import net.minecraft.data.advancements.AdvancementProvider
//import net.neoforged.neoforge.common.data.AdvancementProvider
//import net.neoforged.neoforge.common.data.ExistingFileHelper
//import java.util.function.Consumer
//
//class ServerAdvancementGenerator : AdvancementProvider.AdvancementGenerator {
//    override fun generate(
//        registries: HolderLookup.Provider,
//        saver: Consumer<AdvancementHolder>,
//        existingFileHelper: ExistingFileHelper
//    ) {
//        pageMagic(registries, saver, existingFileHelper)
//    }
//
//    /**
//     *
//     * */
//    fun pageMagic(
//        registries: HolderLookup.Provider,
//        saver: Consumer<AdvancementHolder>,
//        existingFileHelper: ExistingFileHelper,
//    ) {
//        val pageName = "literacy"
//        /**
//         * represent key: "advancements.$tmpName.$modid.$path"
//         * */
//        fun langKey(path: String) = CypherNexus.modTranslation("advancements.$pageName", path)
//        fun saveTo(path: String) = CypherNexus.modResource("$pageName/$path") // "/" represents folder, not "."
//
//        val root = Advancement.Builder.recipeAdvancement()
//            // Sets the parent of the advancement, or create a placeholder if no other advancement has generated
////            .parent(AdvancementSubProvider.createPlaceholder("${UntitledWorld.MOD_ID}:$tmpName/root")) // create a new page if empty
//            .display(
//                ModItems.CYPHER_INDEX_BLOCK_ITEM,
//                langKey("root.title"),
//                langKey("root.description"),
//                // The background texture. (Use null for non-root advancements)
//                // vanilla backgrounds locate at textures\gui\advancements\backgrounds
//                CypherNexus.modResource("textures/gui/advancements/lore.png"),
//                AdvancementType.TASK,
//                true,
//                true,
//                false
//            )
//            // can reward any of four types by default
//            .rewards(AdvancementRewards.Builder.experience(1))
//            // TriggerInstance: Trigger instances are responsible for holding the defined conditions,
//            // and returning whether the inputs match the condition.
//            // Trigger: while triggers aim at supplying a method to check trigger instances
//            // and run attached listeners on success, and specifying a codec to serialize the trigger instance
//            .addCriterion("place_cypher_index",
//                ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(ModBlocks.CYPHER_INDEX_BLOCK))
////            .addCriterion("interaction_cypher_index",
////                CriteriaTriggers.DEFAULT_BLOCK_USE.createCriterion(
////                    DefaultBlockInteractionTrigger.TriggerInstance(
////                        Optional.empty(),
////                        Optional.of(
////                            ContextAwarePredicate.create(
////                                LootItemBlockStatePropertyCondition.hasBlockStateProperties(
////                                    ModBlocks.CYPHER_INDEX_BLOCK
////                                ).build()
////                            )
////                        )
////                    ) // damn it
////                )
////            )
//            // optional, list elements are criterion's keys, cannot contain keys not declared before
////            .requirements(AdvancementRequirements.anyOf(listOf("place_cypher_index", "interaction_cypher_index")))
//
//            // Save the advancement to disk, using the given resource location. This returns an AdvancementHolder,
//            // which may be stored in a variable and used as a parent by other advancement builders.
//            .save(saver, saveTo("root"), existingFileHelper)
//
//        val first_wand = Advancement.Builder.recipeAdvancement()
//            .parent(root)
//            .display(
//                ModItems.TIERED_WAND,
//                langKey("first_wand.title"),
//                langKey("first_wand.description"),
//                null,
//                AdvancementType.TASK,
//                true,
//                true,
//                false
//            )
//            .addCriterion("obtain_wand",
//                CriteriaTriggers.IMPOSSIBLE.createCriterion(ImpossibleTrigger.TriggerInstance()))
//            .save(saver, saveTo("first_wand"), existingFileHelper)
//
//        // TODO: CriteriaTrigger subclasses check cyphers usage, wand properties, etc.
//    }
//}