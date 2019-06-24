package de.hterhors.semanticmr.crf.templates.et;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hterhors.semanticmr.crf.model.AbstractFactorScope;
import de.hterhors.semanticmr.crf.model.Factor;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.crf.structure.slots.SlotType;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.templates.et.KnowledgeBaseTemplate.KnowledgeBaseScope;
import de.hterhors.semanticmr.crf.templates.helper.RDFObject;
import de.hterhors.semanticmr.crf.templates.helper.TripleStoreDatabase;
import de.hterhors.semanticmr.crf.variables.DoubleVector;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.crf.variables.State;

public class KnowledgeBaseTemplate extends AbstractFeatureTemplate<KnowledgeBaseScope> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Logger log = LogManager.getFormatterLogger(KnowledgeBaseTemplate.class.getName());

	public static boolean useQuery1 = true, useQuery2 = true, useQuery3 = true, useQuery4 = true, useQuery5 = true;

	final private static String subjectName1 = "s1";
	final private static String propertyName1 = "p1";
	final private static String propertyName2 = "p2";

	final private static String QUERY1 = "select distinct ?" + propertyName1 + " where { { <%s> ?" + propertyName1
			+ " <%s>} UNION {<%s> ?" + propertyName1 + " <%s>} }";

	final private static String QUERY3 = "select distinct ?" + subjectName1 + " where {?" + subjectName1
			+ " <%s> <%s> .} LIMIT 1";

//	final private static String QUERY6_1 = "ask {?" + subjectName1 + " <%s> <%s>}";

	final private static String QUERY3_domain = "select distinct ?" + subjectName1 + " where {?" + subjectName1
			+ "<%s> <%s> . ?" + subjectName1 + " a <%s> }";

	final private static String QUERY2 = "select distinct ?" + propertyName1 + " ?" + propertyName2 + " where { {<%s> ?"
			+ propertyName1 + " ?o. ?o ?" + propertyName2 + " <%s>} UNION { <%s> ?" + propertyName1 + " ?o. ?o ?"
			+ propertyName2 + " <%s>} } ";

	final private static String QUERY4 = "select distinct ?" + propertyName1 + " ?" + propertyName2 + " where {<%s> ?"
			+ propertyName1 + " ?o. <%s> ?" + propertyName2 + " ?o  } ";

	private TripleStoreDatabase db;

	public KnowledgeBaseTemplate(List<Instance> instances) {

		readExternal(instances);
//		readTrainingData(instances);

	}

//	private void readTrainingData(List<Instance> instances) {
//		db = new TripleStoreDatabase();
//
//		try {
//
//			for (OBIEInstance obieInstance : runner.corpusProvider.getTrainingCorpus().getInternalInstances()) {
//
//				for (IETmplateAnnotation ta : obieInstance.getGoldAnnotation().getAnnotations()) {
//
//					IOBIEThing thing = ta.getThing();
//
//					for (Field slot : ReflectionUtils.getFields(thing.getClass(), thing.getInvestigationRestriction())) {
//
//						List<IOBIEThing> fillers;
//						if (ReflectionUtils.isAnnotationPresent(slot, RelationTypeCollection.class)) {
//							fillers = (List<IOBIEThing>) slot.get(thing);
//						} else {
//							fillers = new ArrayList<>(1);
//							IOBIEThing filler = (IOBIEThing) slot.get(thing);
//							if (filler != null) {
//								fillers.add(filler);
//							}
//						}
//
//						for (IOBIEThing filler : fillers) {
//							db.add(thing, slot, filler);
//						}
//
//					}
//
//				}
//
//			}
//		} catch (IllegalArgumentException | IllegalAccessException e) {
//			e.printStackTrace();
//			System.exit(1);
//		}
//	}

	private void readExternal(List<Instance> instances) {
		log.info("Read external knowledgebase into triple store...");
		db = new de.hterhors.semanticmr.crf.templates.helper.TripleStoreDatabase(
				new File("/home/hterhors/git/OBIECore/ontology_properties_en.ttl"));
//		db = new TripleStoreDatabase(new File("/home/hterhors/git/OBIECore/knowledgebase_complete.ttl"));

		for (Instance instance : instances) {

			for (AbstractAnnotation ta : instance.getGoldAnnotations().getAnnotations()) {

				EntityTemplate thing = ta.asInstanceOfEntityTemplate();

				List<Map<String, RDFObject>> invertQ2Result1 = db
						.select("SELECT DISTINCT * WHERE { <http://dbpedia.org/resource/"
								+ thing.getEntityType().entityName + "> ?p ?o }").queryData;
				invertQ2Result1.forEach(System.out::println);

				Map<SlotType, Set<AbstractAnnotation>> annotations = thing.filter().rootAnnotation()
						.docLinkedAnnoation().literalAnnoation().entityTypeAnnoation().merge().nonEmpty().singleSlots()
						.multiSlots().build().getMergedAnnotations();

				for (Entry<SlotType, Set<AbstractAnnotation>> annotation : annotations.entrySet()) {

					for (AbstractAnnotation aa : annotation.getValue()) {

						db.delete(thing, annotation.getKey(), aa);
					}

				}

				List<Map<String, RDFObject>> invertQ2Result2 = db
						.select("SELECT DISTINCT * WHERE { <http://dbpedia.org/resource/"
								+ thing.getEntityType().entityName + "> ?p ?o }").queryData;
				invertQ2Result2.forEach(System.out::println);

			}

		}
	}

	public static class KnowledgeBaseScope extends AbstractFactorScope {

		/**
		 * The parent class type of the obie-template in a parent-child relation.
		 * Otherwise the first child of the pair.
		 */
		final EntityType parentOrFirst;

		/**
		 * The class type of the investigated child-property in a parent-child relation.
		 * Otherwise the second child of the pair.
		 */
		final EntityType childOrSecond;

		/**
		 * Whether the values are from the same slot.
		 */
		final boolean interSlotComparison;
		/**
		 * The first slot name true.
		 */
		final SlotType slot1;

		final SlotType slot2;

		public KnowledgeBaseScope(AbstractFeatureTemplate<KnowledgeBaseScope> template, EntityType value1,
				SlotType slot1, SlotType slot2, EntityType value2, boolean interSlotComparison) {
			super(template);
			this.parentOrFirst = value1;
			this.childOrSecond = value2;
			this.slot1 = slot1;
			this.slot2 = slot2;
			this.interSlotComparison = interSlotComparison;
		}

		@Override
		public int implementHashCode() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public boolean implementEquals(Object obj) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((childOrSecond == null) ? 0 : childOrSecond.hashCode());
			result = prime * result + (interSlotComparison ? 1231 : 1237);
			result = prime * result + ((parentOrFirst == null) ? 0 : parentOrFirst.hashCode());
			result = prime * result + ((slot1 == null) ? 0 : slot1.hashCode());
			result = prime * result + ((slot2 == null) ? 0 : slot2.hashCode());
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
			KnowledgeBaseScope other = (KnowledgeBaseScope) obj;
			if (childOrSecond == null) {
				if (other.childOrSecond != null)
					return false;
			} else if (!childOrSecond.equals(other.childOrSecond))
				return false;
			if (interSlotComparison != other.interSlotComparison)
				return false;
			if (parentOrFirst == null) {
				if (other.parentOrFirst != null)
					return false;
			} else if (!parentOrFirst.equals(other.parentOrFirst))
				return false;
			if (slot1 == null) {
				if (other.slot1 != null)
					return false;
			} else if (!slot1.equals(other.slot1))
				return false;
			if (slot2 == null) {
				if (other.slot2 != null)
					return false;
			} else if (!slot2.equals(other.slot2))
				return false;
			return true;
		}

	}

//	class DatatypePropertyScope extends FactorScope {
//
//		/**
//		 * The parent class type of the obie-template in a parent-child relation.
//		 * Otherwise the first child of the pair.
//		 */
//		final String owlClassName;
//
//		/**
//		 * The class type of the investigated child-property in a parent-child relation.
//		 * Otherwise the second child of the pair.
//		 */
//		final String slotValue;
//
//		/**
//		 * The slot name or slot name combination if {@link #interSlotComparison} is
//		 * true.
//		 */
//		final String slotName;
//
//		public DatatypePropertyScope(Class<? extends IOBIEThing> entityRootClassType, String owlClassName,
//				String slotName, String slotValue) {
//			super(KnowledgeBaseTemplate.this, entityRootClassType, owlClassName, slotValue, slotName);
//			this.owlClassName = owlClassName;
//			this.slotValue = slotValue;
//			this.slotName = slotName;
//		}
//
//	}

	@Override
	public List<KnowledgeBaseScope> generateFactorScopes(State state) {
		List<KnowledgeBaseScope> factors = new ArrayList<>();
		for (AbstractAnnotation entity : state.getCurrentPredictions().getAnnotations()) {

			Map<SlotType, Set<AbstractAnnotation>> annotations = entity.asInstanceOfEntityTemplate().filter()
					.docLinkedAnnoation().literalAnnoation().entityTypeAnnoation().merge().nonEmpty().multiSlots()
					.singleSlots().build().getMergedAnnotations();

			List<SlotType> slots = new ArrayList<>(annotations.keySet());

			{
				final EntityType parent = entity.getEntityType();

				for (int i = 0; i < slots.size(); i++) {

					final SlotType slot = slots.get(i);

					final List<AbstractAnnotation> slot1Values = new ArrayList<>(annotations.get(slot));

					for (int k = 0; k < slot1Values.size(); k++) {

						final AbstractAnnotation slotValue1 = slot1Values.get(k);

						if (slotValue1.getEntityType().isLiteral) {
//						final String value = ((IDatatype) slotValue1).getInterpretedValue();
//
//						factors.add(new DatatypePropertyScope(rootClassType, parentThing.getONTOLOGY_NAME(),
//								slot.getAnnotation(OntologyModelContent.class).ontologyName(), value));
						} else {
							factors.add(new KnowledgeBaseScope(this, parent, slot, null, slotValue1.getEntityType(),
									false));
						}
					}
				}
			}
			{
				for (int i = 0; i < slots.size(); i++) {

					final SlotType slot1 = slots.get(i);

					final List<AbstractAnnotation> slot1Values = new ArrayList<>(annotations.get(slot1));

					for (int j = i + 1; j < slots.size(); j++) {

						final SlotType slot2 = slots.get(j);

						final List<AbstractAnnotation> slot2Values = new ArrayList<>(annotations.get(slot2));

						for (int k = 0; k < slot1Values.size(); k++) {
							final AbstractAnnotation slotValue1 = slot1Values.get(k);
							if (slotValue1.getEntityType().isLiteral) {

//							if (ReflectionUtils.isAnnotationPresent(slot1, DatatypeProperty.class)) {
//								final String value1 = ((IDatatype) slotValue1).getInterpretedValue();
//
//								for (int l = 0; l < slot2Values.size(); l++) {
//									IOBIEThing slotValue2 = slot2Values.get(l);
//
//									if (slotValue2 == null)
//										continue;
//
//									if (ReflectionUtils.isAnnotationPresent(slot2, DatatypeProperty.class)) {
//
//										/**
//										 * Do not compare two datatype properties.
//										 */
//									} else {
//	//
////										final AbstractIndividual value2 = slotValue2.getIndividual();
//	//
////										factors.add(new DatatypePropertyScope(rootClassType, value2,
////												"(" + slot1.getName() + ")<->(" + slot2.getName() + ")", value1, true));
//									}
//								}
//
							} else {

								final EntityType value1 = slotValue1.getEntityType();

								for (int l = 0; l < slot2Values.size(); l++) {

									AbstractAnnotation slotValue2 = slot2Values.get(l);

									if (slotValue2.getEntityType().isLiteral) {
//										final String value2 = ((IDatatype) slotValue2).getInterpretedValue();
										//
//										factors.add(new DatatypePropertyScope(rootClassType, value1,
//												"(" + slot1.getName() + ")<->(" + slot2.getName() + ")", value2, true));
									} else {

										final EntityType value2 = slotValue2.getEntityType();

										factors.add(new KnowledgeBaseScope(this, value1, slot1, slot2, value2, true));
									}
								}
							}

						}
					}
				}
			}

			/*
			 * TODO: kinda inefficient cause getFillers() is called a lot.
			 * 
			 * intra-slot relation.
			 * 
			 * For every distinct slot pair do: for every distinct slotValue pair do: add
			 * factor
			 */
			{
				/**
				 * No datatype-pair queries.
				 */
				for (int i = 0; i < slots.size(); i++) {

					final SlotType slot = slots.get(i);

					final List<AbstractAnnotation> slotValues = new ArrayList<>(annotations.get(slot));

					if (slotValues.size() < 2)
						continue;

					for (int k = 0; k < slotValues.size() - 1; k++) {
						final AbstractAnnotation slotValue1 = slotValues.get(k);

						final EntityType value1 = slotValue1.getEntityType();

						for (int l = k + 1; l < slotValues.size(); l++) {
							final AbstractAnnotation slotValue2 = slotValues.get(l);

							final EntityType value2 = slotValue2.getEntityType();

							factors.add(new KnowledgeBaseScope(this, value1, slot, slot, value2, false));
						}
					}
				}
			}

		}

		return factors;
	}

	final private static String MEAN_STD_DEVIATION_TEMPLATE = "%s of %s is within %s x std deviation(%s) of mean(%s)";
	final private static String NOT_MEAN_STD_DEVIATION_TEMPLATE = "%s of %s is NOT within %s x std deviation (%s) of mean(%s)";

	@Override
	public void generateFeatureVector(Factor<KnowledgeBaseScope> factor) {

		if (factor.getFactorScope() instanceof KnowledgeBaseScope) {

			KnowledgeBaseScope objectScope = (KnowledgeBaseScope) factor.getFactorScope();

			final String parentOrFirst = "http://dbpedia.org/resource/" + objectScope.parentOrFirst.entityName;
			final String childOrSecond = "http://dbpedia.org/resource/" + objectScope.childOrSecond.entityName;

			final String slot1Name = objectScope.slot1 == null ? "null" : objectScope.slot1.slotName;
			final String slot2Name = objectScope.slot2 == null ? "null" : objectScope.slot2.slotName;

			if (useQuery3) {

				String query3;

				query3 = String.format(QUERY3, "http://dbpedia.org/ontology/" + slot1Name, childOrSecond);

				if (!db.select(query3).queryData.isEmpty()) {
					factor.getFeatureVector().set("QUERY3: " + slot1Name + " not empty", true);
					factor.getFeatureVector().set("QUERY3: " + slot1Name + " " + childOrSecond + " not empty", true);

				}
			}
			if (useQuery2) {

				String query2 = String.format(QUERY2, parentOrFirst, childOrSecond, childOrSecond, parentOrFirst);

				List<Map<String, RDFObject>> result2 = db.select(query2).queryData;

				if (!result2.isEmpty()) {
					factor.getFeatureVector().set("QUERY2: not empty", true);
					factor.getFeatureVector().set("QUERY2: " + slot1Name + " & " + slot2Name + " not empty", true);
				}

				for (Map<String, RDFObject> map : result2) {
					RDFObject property1 = map.get(propertyName1);
					RDFObject property2 = map.get(propertyName2);
					if (property1 != null && property2 != null) {
						factor.getFeatureVector().set("QUERY2: " + slot1Name + " & " + slot2Name + " * "
								+ property1.value + " " + property2.value + " * not empty", true);
					}
				}
			}
			if (useQuery1) {

				String query1 = String.format(QUERY1, parentOrFirst, childOrSecond, childOrSecond, parentOrFirst);

				List<Map<String, RDFObject>> result1 = db.select(query1).queryData;

				if (!result1.isEmpty()) {
					factor.getFeatureVector().set("QUERY1: not empty", true);
					factor.getFeatureVector().set("QUERY1: dbo:" + slot1Name + " & dbo:" + slot2Name + " not empty",
							true);
				}

				for (Map<String, RDFObject> map : result1) {
					RDFObject property = map.get(propertyName1);
					if (property != null) {
						factor.getFeatureVector().set("QUERY1: dbo:" + slot1Name + " & dbo:" + slot2Name + " * "
								+ property.value + " * not empty", true);
					}
				}
			}
			if (useQuery4) {
				String query4 = String.format(QUERY4, parentOrFirst, childOrSecond);

				List<Map<String, RDFObject>> result4 = db.select(query4).queryData;

				if (!result4.isEmpty()) {
					factor.getFeatureVector().set("QUERY4: not empty", true);
					factor.getFeatureVector().set("QUERY4: " + slot1Name + " & " + slot2Name + " not empty", true);
				}

				for (Map<String, RDFObject> map : result4) {
					RDFObject property1 = map.get(propertyName1);
					RDFObject property2 = map.get(propertyName2);
					if (property1 != null && property2 != null) {
						factor.getFeatureVector().set("QUERY4: " + slot1Name + " & " + slot2Name + " " + property1.value
								+ " " + property2.value + " not empty", true);
					}
				}
			}

		}
//		else if (factor.getFactorScope() instanceof DatatypePropertyScope) {
//
////			if (true)
////				return;
//
//			if (useQuery5) {
//
//				DatatypePropertyScope datatypeScope = (DatatypePropertyScope) factor.getFactorScope();
////			
//
//				String query = String.format(
//						"select distinct ?val where {?individual <%s> ?value . ?individual a <%s> . BIND(STR(?value) AS ?val). } LIMIT 1000",
//						datatypeScope.slotName, datatypeScope.owlClassName);
//
//				List<Map<String, RDFObject>> q1Result = db.select(query).queryData;
//				MeanDevPair meanDevPair;
//				try {
//					meanDevPair = getMeanDevPair(datatypeScope.slotName, q1Result);
//				} catch (IndexOutOfBoundsException e) {
//					return;
//				}
//
//				final double distToMean = Math.abs(meanDevPair.mean - Double.parseDouble(datatypeScope.slotValue));
//
//				log.info("meanDevPair: " + meanDevPair);
//				log.info("distToMean: " + distToMean);
//
//				for (int i = 0; i < 100; i++) {
//
//					if (distToMean >= i * 10 && distToMean < ((i + 1) * 10))
//						factor.getFeatureVector().set(
//								"Distance to mean " + datatypeScope.slotName + "->" + String.valueOf(i * 10), true);
//				}
//
////			if (v == Double.valueOf(datatypeScope.slotValue))
////				factor.getFeatureVector().set("Common value for " + datatypeScope.slotName, true);
//
//				for (int i = 10; i >= 1; i--) {
//
//					boolean within = Math.abs(Double.parseDouble(datatypeScope.slotValue) - meanDevPair.mean) <= i
//							* 0.001 * meanDevPair.dev;
//
//					/**
//					 * Add only the feature which is the farthest away from mean.
//					 */
//					if (!within) {
//						factor.getFeatureVector()
//								.set(String.format(NOT_MEAN_STD_DEVIATION_TEMPLATE, datatypeScope.slotName,
//										datatypeScope.owlClassName, i, meanDevPair.dev, meanDevPair.mean), !within);
//						break;
//					}
//
//				}
//				for (int i = 1; i <= 10; i++) {
//
//					boolean within = Math.abs(Double.parseDouble(datatypeScope.slotValue) - meanDevPair.mean) <= i
//							* 0.001 * meanDevPair.dev;
//
//					/**
//					 * Add only the feature which is the nearest to the mean.
//					 */
//					if (within) {
//						factor.getFeatureVector().set(String.format(MEAN_STD_DEVIATION_TEMPLATE, datatypeScope.slotName,
//								datatypeScope.owlClassName, i, meanDevPair.dev, meanDevPair.mean), within);
//						break;
//					}
//				}
//			}
	}

	private Map<String, MeanDevPair> cache = new HashMap<>();

	class MeanDevPair {

		public final double mean;
		public final double dev;

		public MeanDevPair(double mean, double dev) {
			this.mean = mean;
			this.dev = dev;
		}

	}

	private MeanDevPair getMeanDevPair(final String slotContex, List<Map<String, RDFObject>> qr) {
		MeanDevPair mdp = null;
		if ((mdp = cache.get(slotContex)) != null) {
			return mdp;
		}

		List<Double> values = new ArrayList<>();

		double meanOrMedian = 0;
		for (Map<String, RDFObject> r : qr) {

			RDFObject v;
			if ((v = r.get("val")) == null)
				continue;

			final double dv = Double.parseDouble(v.value);

			values.add(dv);
			meanOrMedian += dv;

		}

//		mean /= values.size();

		Collections.sort(values);
		meanOrMedian = values.get((int) (values.size() / 2));

		double variance = 0;
		for (Double value : values) {
			variance += Math.pow(meanOrMedian - value, 2);
		}

		final double stdDev = Math.sqrt(variance);
		mdp = new MeanDevPair(meanOrMedian, stdDev);
		cache.put(slotContex, mdp);

		return mdp;
	}

	private void doubleProp(DoubleVector vector, KnowledgeBaseScope factor, String query, String context,
			String value1Filler, String value2Filler, final boolean interSlotComparison) {
		List<Map<String, RDFObject>> q2Result = db.select(String.format(query, value1Filler, value2Filler)).queryData;
		for (Map<String, RDFObject> r : q2Result) {
			vector.set(context + ": " + factor.slot1.slotName + "->" + r.get(propertyName1).value + "->"
					+ r.get(propertyName2).value + "-interSlot =" + interSlotComparison, true);
		}

		List<Map<String, RDFObject>> invertQ2Result = db
				.select(String.format(query, value2Filler, value1Filler)).queryData;
		for (Map<String, RDFObject> r : invertQ2Result) {
			vector.set("Invert-" + context + ": " + factor.slot1.slotName + "->" + r.get(propertyName1).value + "->"
					+ r.get(propertyName2).value + (interSlotComparison ? " interSlot" : " intraSlot"), true);
		}
	}

	private void singleProp(DoubleVector vector, KnowledgeBaseScope factor, String query, String context,
			final String value1Filler, final String value2Filler, final boolean interSlotComparison) {
		List<Map<String, RDFObject>> q1Result = db.select(String.format(query, value1Filler, value2Filler)).queryData;
		for (Map<String, RDFObject> r : q1Result) {
			vector.set(context + ": " + factor.slot1.slotName + "->" + r.get(propertyName1).value + "-interSlot ="
					+ interSlotComparison, true);
		}

		List<Map<String, RDFObject>> invertQ1Result = db
				.select(String.format(query, value2Filler, value1Filler)).queryData;
		for (Map<String, RDFObject> r : invertQ1Result) {
			vector.set("Invert-" + context + ": " + factor.slot1.slotName + "->" + r.get(propertyName1).value
					+ (interSlotComparison ? " interSlot" : " intraSlot"), true);
		}
	}

	private void singleSingleFiller(DoubleVector vector, KnowledgeBaseScope factor, String query, String context,
			final String value1Filler, final String value2Filler, final boolean interSlotComparison) {
		List<Map<String, RDFObject>> q1Result = db.select(String.format(query, value1Filler)).queryData;
		for (Map<String, RDFObject> r : q1Result) {
			vector.set(context + ": " + factor.slot1.slotName + "->" + r.get(propertyName1).value + "-interSlot ="
					+ interSlotComparison, true);
		}

		List<Map<String, RDFObject>> invertQ1Result = db.select(String.format(query, value2Filler)).queryData;
		for (Map<String, RDFObject> r : invertQ1Result) {
			vector.set("Invert-" + context + ": " + factor.slot1.slotName + "->" + r.get(propertyName1).value
					+ (interSlotComparison ? " interSlot" : " intraSlot"), true);
		}
	}

}
