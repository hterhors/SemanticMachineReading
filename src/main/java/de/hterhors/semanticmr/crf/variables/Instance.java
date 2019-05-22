package de.hterhors.semanticmr.crf.variables;

import de.hterhors.semanticmr.corpus.EInstanceContext;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.exce.MismatchingDocumentsException;

/**
 * The Instance object couples a document and the gold annotation of that
 * document. This class is used during training as training instance, during
 * testing as testing instance and during prediction as prediction instance.
 * 
 * @author hterhors
 *
 */
public class Instance implements Comparable<Instance> {

	/**
	 * The instance document.
	 */
	private final Document document;

	/**
	 * The corresponding gold annotation.
	 */
	private final Annotations goldAnnotations;

	/**
	 * The context of this instance, whether it belongs to train dev or test set.
	 */
	private final EInstanceContext originalContext;
	private EInstanceContext redistributedContext = EInstanceContext.UNSPECIFIED;

	public Instance(EInstanceContext context, Document document, Annotations goldAnnotations) {
		this.originalContext = context == null ? EInstanceContext.UNSPECIFIED : context;
		this.goldAnnotations = goldAnnotations;
		this.document = document;
		this.goldAnnotations.unmodifiable();

		for (AbstractAnnotation a : goldAnnotations.getAnnotations()) {
			if (a instanceof DocumentLinkedAnnotation) {
				if (!a.asInstanceOfDocumentLinkedAnnotation().document.equals(document))
					throw new MismatchingDocumentsException("Not all " + DocumentLinkedAnnotation.class.getSimpleName()
							+ "s are defined on the instance's document.");
			}
		}
	}

	public String getName() {
		return getDocument().documentID;
	}

	public EInstanceContext getOriginalContext() {
		return originalContext;
	}

	public Document getDocument() {
		return document;
	}

	public Annotations getGoldAnnotations() {
		return goldAnnotations;
	}

	@Override
	public String toString() {
		return "Instance [originalContext=" + originalContext
				+ (redistributedContext != EInstanceContext.UNSPECIFIED
						? ", redistributedContext=" + redistributedContext
						: "")
				+ ", document=" + document + ", goldAnnotations=" + goldAnnotations + "]";
	}

	@Override
	public int compareTo(Instance o) {
		return document.documentID.compareTo(o.document.documentID);
	}

	public void setRedistributedContext(EInstanceContext redistributedContext) {
		this.redistributedContext = redistributedContext;
	}

}
