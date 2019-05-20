package de.hterhors.semanticmr.eval;

public enum EEvaluationDetail {

	/**
	 * Most detailed annotation, checks all attributes of a document linked
	 * annotation. This includes: surface form, textual position and entity type.
	 */
	DOCUMENT_LINKED,

	/**
	 * Second most detailed evaluation. Checks only the surface form and entity
	 * type. The offset position in the text is not checked.
	 */
	LITERAL,

	/**
	 * Least detailed evaluation. Checks only for entity types. Ignores surface
	 * forms and offset positions.
	 */
	ENTITY_TYPE;
}
