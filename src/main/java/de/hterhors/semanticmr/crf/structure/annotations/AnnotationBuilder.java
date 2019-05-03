package de.hterhors.semanticmr.crf.structure.annotations;

import java.util.Objects;

import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.container.DocumentPosition;
import de.hterhors.semanticmr.crf.structure.annotations.container.TextualContent;
import de.hterhors.semanticmr.crf.variables.Document;
import de.hterhors.semanticmr.exce.DocumentLinkedAnnotationMismatchException;

public class AnnotationBuilder {

	public static DocumentLinkedAnnotation toAnnotation(final Document document, final String entityTypeName,
			final String textualContent, final int offset) throws DocumentLinkedAnnotationMismatchException {
		return new DocumentLinkedAnnotation(document, EntityType.get(entityTypeName),
				new TextualContent(textualContent), new DocumentPosition(offset));
	}

	public static LiteralAnnotation<?> toAnnotation(final String entityTypeName, final String literal) {
		return new LiteralAnnotation<>(EntityType.get(entityTypeName), new TextualContent(literal));
	}

	public static EntityTypeAnnotation<?> toAnnotation(EntityType entityType) {
		return EntityTypeAnnotation.get(entityType);
	}

	public static EntityTypeAnnotation<?> toAnnotation(final String entityTypeName) {
		Objects.requireNonNull(entityTypeName);
		return toAnnotation(EntityType.get(entityTypeName));
	}

}
