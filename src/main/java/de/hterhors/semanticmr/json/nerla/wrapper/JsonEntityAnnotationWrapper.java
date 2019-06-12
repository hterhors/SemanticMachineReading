package de.hterhors.semanticmr.json.nerla.wrapper;

import com.google.gson.annotations.SerializedName;

import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;

public class JsonEntityAnnotationWrapper {

	@SerializedName("docID")
	private String documentID;
	@SerializedName("et")
	private String entityType;
	@SerializedName("off")
	private int offset;
	@SerializedName("sff")
	private String surfaceForm;

	public JsonEntityAnnotationWrapper(String documentID, String entityType, int offset, String surfaceForm) {
		this.documentID = documentID;
		this.entityType = entityType;
		this.offset = offset;
		this.surfaceForm = surfaceForm;
	}

	public JsonEntityAnnotationWrapper(DocumentLinkedAnnotation d) {
		this.documentID = d.document.documentID;
		this.entityType = d.getEntityType().entityName;
		this.offset = d.getStartDocCharOffset();
		this.surfaceForm = d.getSurfaceForm();
	}

	public String getDocumentID() {
		return documentID;
	}

	public void setDocumentID(String documentID) {
		this.documentID = documentID;
	}

	public String getEntityType() {
		return entityType;
	}

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public String getSurfaceForm() {
		return surfaceForm;
	}

	public void setSurfaceForm(String surfaceForm) {
		this.surfaceForm = surfaceForm;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((documentID == null) ? 0 : documentID.hashCode());
		result = prime * result + ((entityType == null) ? 0 : entityType.hashCode());
		result = prime * result + offset;
		result = prime * result + ((surfaceForm == null) ? 0 : surfaceForm.hashCode());
		return result;
	}

}
