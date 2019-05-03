package de.hterhors.semanticmr.crf.structure;

public interface IDeepCopyable {

	public <A extends IDeepCopyable> A deepCopy();

}
