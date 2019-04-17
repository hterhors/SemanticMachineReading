package de.hterhors.semanticmr.eval;

public enum EEvaluationMode {

	/**
	 * Most detailed annotation, checks the full fields of a document linked
	 * annotation.
	 */
	DOCUMENT_LINKED,

	/**
	 * Second most detailed evaluation. Checks only the literal and entity type of
	 * each annotation (includes document linked annotation)
	 */
	LITERAL,

	/**
	 * Least detailed evaluation mode. Checks only for entity types. Ignores
	 * literals and document positions.
	 */
	ENTITY_TYPE;
}
