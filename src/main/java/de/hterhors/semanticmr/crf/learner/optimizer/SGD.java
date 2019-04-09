package de.hterhors.semanticmr.crf.learner.optimizer;

import de.hterhors.semanticmr.crf.variables.DoubleVector;

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

//	 public static void main(String[] args) {
//	 DoubleVector theta = new Vector();
//	 theta.set("x1", 1.0);
//	 theta.set("x2", 1.0);
//	
//	 SGD adam = new SGD(0.001, 0.9, 0, false);
//	 for (int t = 0; t < 200; t++) {
//	 double x1 = theta.getValueOfFeature("x1");
//	 double x2 = theta.getValueOfFeature("x2");
//	 double loss = 1.0 / 2.0 * Math.pow(x1 * 2 + x2 * 4, 2);
//	
//	 System.out.println();
//	 System.out.println("Time: " + t);
//	 System.out.println("Theta: " + theta);
//	 Vector g = new Vector();
//	 g.set("x1", 4 * (x1 + 2 * x2));
//	 g.set("x2", 8 * (x1 + 2 * x2));
//	 System.out.println("Loss: " + loss);
//	 System.out.println("Gradient: " + g);
//	 theta = adam.getUpdates(theta, g);
//	 System.out.println("Theta: " + theta);
//	 }
//	 }

}
