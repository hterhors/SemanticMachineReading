package de.hterhors.semanticmr.crf.exploration;

import java.util.Set;

import de.hterhors.semanticmr.structure.slotfiller.AbstractSlotFiller;
import de.hterhors.semanticmr.structure.slotfiller.EntityTemplate;

public class DetermineStructureDepth {

	public static int getDepth(EntityTemplate template) {

		int depth = 1;

		final Set<AbstractSlotFiller<?>> slotFillers = template.getAllSlotFillerValues();

		for (AbstractSlotFiller<?> abstractSlotFiller : slotFillers) {
			if (abstractSlotFiller instanceof EntityTemplate) {
				/*
				 * TODO: Check not for same type but for shared super entity.
				 * 
				 * Avoid recursive burn out.
				 */
				if (!template.getEntityType().equals(abstractSlotFiller.getEntityType())) {
					depth += getDepth((EntityTemplate) abstractSlotFiller);
				}
			}
		}

		return depth;

	}

}
