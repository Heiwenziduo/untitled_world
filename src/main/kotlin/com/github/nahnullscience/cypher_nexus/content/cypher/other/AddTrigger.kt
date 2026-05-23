package com.github.nahnullscience.cypher_nexus.content.cypher.other

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.TriggerType

object AddTrigger : AbstractAddTrigger(
    manaDrain = 10f
) {
    override val triggerType = TriggerType.COLLISION
    override val resource = CypherNexus.modResource("add_trigger")
}