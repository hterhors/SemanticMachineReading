package de.hterhors.semanticmr.crf.structure.annotations;

import java.util.Objects;

import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.container.DocumentPosition;
import de.hterhors.semanticmr.crf.structure.annotations.container.TextualContent;
import de.hterhors.semanticmr.crf.variables.Document;
import de.hterhors.semanticmr.exce.DocumentLinkedAnnotationMismatchException;

public class AnnotationBuilder {

	public static DocumentLinkedAnnotation toAnnotation(final Document document, final String entityTypeName,
			final String textualContent, final int offset) {
		try {

			Objects.requireNonNull(document);
			Objects.requireNonNull(entityTypeName);
			Objects.requireNonNull(textualContent);
			return new DocumentLinkedAnnotation(document, EntityType.get(entityTypeName),
					new TextualContent(textualContent), new DocumentPosition(offset));
		} catch (DocumentLinkedAnnotationMismatchException e) {
			throw new RuntimeException(e);
		}
	}

	public static DocumentLinkedAnnotation toAnnotation(final Document document, final EntityType entityType,
			final String textualContent, final int offset) {
		try {
			Objects.requireNonNull(document);
			Objects.requireNonNull(entityType);
			Objects.requireNonNull(textualContent);
			return new DocumentLinkedAnnotation(document, entityType, new TextualContent(textualContent),
					new DocumentPosition(offset));
		} catch (DocumentLinkedAnnotationMismatchException e) {
			throw new RuntimeException(e);
		}
	}

	public static LiteralAnnotation toAnnotation(final String entityTypeName, final String literal) {
		Objects.requireNonNull(entityTypeName);
		Objects.requireNonNull(literal);
		return new LiteralAnnotation(EntityType.get(entityTypeName), new TextualContent(literal));
	}

	public static EntityTypeAnnotation toAnnotation(EntityType entityType) {
		Objects.requireNonNull(entityType);
		return EntityTypeAnnotation.get(entityType);
	}

	public static EntityTypeAnnotation toAnnotation(final String entityTypeName) {
		Objects.requireNonNull(entityTypeName);
		return toAnnotation(EntityType.get(entityTypeName));
	}

}
