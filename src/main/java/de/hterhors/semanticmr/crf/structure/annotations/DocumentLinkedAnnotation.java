package de.hterhors.semanticmr.crf.structure.annotations;

import java.util.Comparator;
import java.util.List;

import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.IEvaluatable;
import de.hterhors.semanticmr.crf.structure.annotations.container.DocumentPosition;
import de.hterhors.semanticmr.crf.structure.annotations.container.TextualContent;
import de.hterhors.semanticmr.crf.variables.Document;
import de.hterhors.semanticmr.crf.variables.DocumentToken;
import de.hterhors.semanticmr.eval.EEvaluationDetail;
import de.hterhors.semanticmr.exce.DocumentLinkedAnnotationMismatchException;

/**
 * Annotation object for entity-type based slots that are linked to the
 * document.
 * 
 * @author hterhors
 *
 */
final public class DocumentLinkedAnnotation extends LiteralAnnotation {

	public static final Comparator<DocumentLinkedAnnotation> COMPARE_BY_SURFACEFORM = new Comparator<DocumentLinkedAnnotation>() {

		@Override
		public int compare(DocumentLinkedAnnotation o1, DocumentLinkedAnnotation o2) {
			return o1.getSurfaceForm().compareTo(o2.getSurfaceForm());
		}
	};

	/**
	 * Contains the document position of this annotation.
	 */
	public final DocumentPosition documentPosition;

	/**
	 * The document this annotation is related to.
	 */
	public final Document document;

	public final List<DocumentToken> relatedTokens;

	public int getSentenceIndex() {
		return relatedTokens.get(0).getSentenceIndex();
	}

	public List<DocumentToken> getTokenizedSentenceOfAnnotation() {
		return document.getSentenceByIndex(relatedTokens.get(0).getSentenceIndex());
	}

	public String getSentenceOfAnnotation() {
		StringBuffer sb = new StringBuffer();
		List<DocumentToken> tokens = getTokenizedSentenceOfAnnotation();
		for (int i = 0; i < tokens.size() - 1; i++) {
			sb.append(tokens.get(i).getText());

			for (int j = tokens.get(i).getDocCharOffset(); j < tokens.get(i + 1).getDocCharOffset()
					- tokens.get(i).getLength(); j++) {
				sb.append(" ");
			}

		}
		sb.append(tokens.get(tokens.size() - 1).getText());

		return sb.toString();
	}

	public DocumentLinkedAnnotation(Document document, EntityType entityType, TextualContent textualContent,
			DocumentPosition documentPosition) throws DocumentLinkedAnnotationMismatchException {
		super(entityType, textualContent);
		this.document = document;
		this.documentPosition = documentPosition;

		this.relatedTokens = this.document.tokenList.subList(
				this.document.getTokenByCharStartOffset(getStartDocCharOffset()).getDocTokenIndex(),
				this.document.getTokenByCharEndOffset(getEndDocCharOffset()).getDocTokenIndex() + 1);

	}

	public int getStartDocCharOffset() {
		return documentPosition.docCharOffset;
	}

	/**
	 * End offset of this annotation (start + length) excluding
	 * 
	 * @return
	 */
	public int getEndDocCharOffset() {
		return documentPosition.docCharOffset + getLength();
	}

	public int getLength() {
		return textualContent.surfaceForm.length();
	}

	@Override
	public String toString() {
		return "DocumentLinkedAnnotation [documentPosition=" + documentPosition + ", document=" + document
				+ ", relatedTokens=" + relatedTokens + ", toString()=" + super.toString() + "]";
	}

	@SuppressWarnings("unchecked")
	@Override
	public DocumentLinkedAnnotation deepCopy() {
		try {
			return new DocumentLinkedAnnotation(document, entityType, textualContent.deepCopy(),
					documentPosition.deepCopy());
		} catch (DocumentLinkedAnnotationMismatchException e) {
			throw new IllegalStateException("This can not happen!");
		}
	}

	@Override
	public String toPrettyString(int depth) {
		final StringBuilder sb = new StringBuilder();
		sb.append(super.toPrettyString(depth));
		sb.append("\t");
		sb.append(getSentenceIndex());
		sb.append("\t");
		sb.append(documentPosition.toPrettyString());
		return sb.toString().trim();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((document == null) ? 0 : document.hashCode());
		result = prime * result + ((documentPosition == null) ? 0 : documentPosition.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		DocumentLinkedAnnotation other = (DocumentLinkedAnnotation) obj;
		if (document == null) {
			if (other.document != null)
				return false;
		} else if (!document.equals(other.document))
			return false;
		if (documentPosition == null) {
			if (other.documentPosition != null)
				return false;
		} else if (!documentPosition.equals(other.documentPosition))
			return false;
		return true;
	}

	@Override
	public Score evaluate(EEvaluationDetail evaluationDetail, IEvaluatable otherVal) {
		if (otherVal == null)
			return Score.FN;

		if (evaluationDetail == EEvaluationDetail.DOCUMENT_LINKED) {
			if (equals(otherVal))
				return Score.TP;
			else
				return Score.FN_FP;
		} else if (entityType.isLiteral || evaluationDetail == EEvaluationDetail.LITERAL
				|| evaluationDetail == EEvaluationDetail.ENTITY_TYPE) {
			return super.evaluate(evaluationDetail, otherVal);
		}

		throw new IllegalStateException("Unkown or unhandled evaluation mode: " + evaluationDetail);
	}

	@Override
	public boolean evaluateEquals(EEvaluationDetail evaluationDetail, IEvaluatable otherVal) {
		if (otherVal == null)
			return false;

		if (evaluationDetail == EEvaluationDetail.DOCUMENT_LINKED) {
			return equals(otherVal);
		} else if (entityType.isLiteral || evaluationDetail == EEvaluationDetail.LITERAL
				|| evaluationDetail == EEvaluationDetail.ENTITY_TYPE) {
			return super.evaluateEquals(evaluationDetail, otherVal);
		}

		throw new IllegalStateException("Unkown or unhandled evaluation mode: " + evaluationDetail);
	}

	/**
	 * Returns a new LiteralAnnotation object with all properties of this
	 * DocumentLinkedAnnotation but the document link character offset.
	 * 
	 * @return a literalAnnotation version of this documentLinkedAnnotation.
	 */
	public LiteralAnnotation reduceToLiteralAnnotation() {
		return new LiteralAnnotation(getEntityType(), textualContent.deepCopy());
	}
}
