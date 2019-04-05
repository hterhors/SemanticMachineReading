package de.hterhors.semanticmr.structure.slotfiller;

import de.hterhors.semanticmr.evaluation.IEvaluatable;
import de.hterhors.semanticmr.structure.IDeepCopyable;
import de.hterhors.semanticmr.structure.slotfiller.container.DocumentPosition;
import de.hterhors.semanticmr.structure.slotfiller.container.TextualContent;

public abstract class AbstractSlotFiller<T> implements IEvaluatable<T>, IDeepCopyable<AbstractSlotFiller<T>> {

	final public String toPrettyString() {
		return toPrettyString(0);
	}

	public abstract String toPrettyString(final int depth);

	public abstract EntityType getEntityType();

	public static Literal toSlotFiller(final String entityTypeName, final String literal) {
		return new Literal(EntityType.get(entityTypeName), new TextualContent(literal));
	}

	public static EntityType toSlotFiller(final String entityTypeName) {
		return EntityType.get(entityTypeName);
	}

	public static DocumentLink toSlotFiller(final String entityTypeName, final String textualContent,
			final int offset) {
		return new DocumentLink(EntityType.get(entityTypeName), new TextualContent(textualContent),
				new DocumentPosition(offset));
	}

}