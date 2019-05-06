package de.hterhors.semanticmr.projects.olp2.corpus.qa;

public class QuestionAnswerPair {

	public final String question;
	public final String answer;

	public QuestionAnswerPair(String question, String answer) {
		super();
		this.question = question;
		this.answer = answer;
	}

	@Override
	public String toString() {
		return "QuestionAnswerPair [question=" + question + ", answer=" + answer + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((answer == null) ? 0 : answer.hashCode());
		result = prime * result + ((question == null) ? 0 : question.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QuestionAnswerPair other = (QuestionAnswerPair) obj;
		if (answer == null) {
			if (other.answer != null)
				return false;
		} else if (!answer.equals(other.answer))
			return false;
		if (question == null) {
			if (other.question != null)
				return false;
		} else if (!question.equals(other.question))
			return false;
		return true;
	}

}
