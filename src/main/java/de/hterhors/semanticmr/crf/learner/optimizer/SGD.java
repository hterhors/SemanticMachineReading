package de.hterhors.semanticmr.crf.learner.optimizer;

import de.hterhors.semanticmr.crf.variables.DoubleVector;

/**
 * The SGD optimizer. TODO: link paper.
 * 
 *
 * 
 * <b> The current version does not support momentum functions! Only alpha
 * static alpha decay is supported! </b>
 * 
 * @author hterhors
 *
 */
public class SGD implements Optimizer {

	private float alpha = 0.001F;
	private float momentum = 0.0F;
	private float decay = 0.0F;
	private boolean nesterov = false;

	private DoubleVector mom = new DoubleVector();
	private DoubleVector vel = new DoubleVector();
	private int t = 0;

	public SGD(double alpha, double momentum, double decay, boolean nesterov) {
		this.alpha = (float) alpha;
		this.momentum = (float) momentum;
		this.decay = (float) decay;
		this.nesterov = nesterov;
		if (momentum != 0.0 || nesterov) {
			throw new IllegalStateException("Momentum is not efficiently implemented in SDG!");
		}
	}

	public SGD(double alpha, double decay) {
		this(alpha, 0, decay, false);
	}

	@Override
	public void applyUpdates(DoubleVector theta, DoubleVector gradient) {
		t++;
		float alpha_t = alpha * (1.0F / (1.0F + decay * t));

		if (momentum == 0 && decay == 0 && !nesterov) {
			// perform sparse updates
			theta.mulAndSub(gradient, alpha_t);
		} else {
			if (momentum == 0) {
				theta.mulAndSub(gradient, alpha_t);
			} else {
				throw new IllegalStateException("Momentum is not efficiently implemented!");
				/**
				 * TOOD: Implement nesterov or momentum efficiently without a lot of vector
				 * copying.
				 */

//				vel = mom.mul(momentum).sub(gradient.mul(alpha_t));
//
//				if (nesterov) {
//					theta.addToThis(vel.mul(momentum).sub(gradient.mul(alpha_t)));
//				} else {
//					theta.addToThis(vel);
//				}
//				mom = vel;
			}
		}
	}

	@Override
	public double getCurrentAlphaValue() {
		return alpha;
	}

	@Override
	public String toString() {
		return "SGD [alpha=" + alpha + ", momentum=" + momentum + ", decay=" + decay + ", nesterov=" + nesterov + ", m="
				+ mom + ", v=" + vel + ", t=" + t + "]";
	}

}
