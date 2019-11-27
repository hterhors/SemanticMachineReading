package de.hterhors.semanticmr.nerla.annotation;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.AnnotationBuilder;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.variables.Document;
import de.hterhors.semanticmr.init.specifications.SystemScope;

public class RegularExpressionNerlAnnotator {

	private static Logger log = LogManager.getFormatterLogger(RegularExpressionNerlAnnotator.class);

	private static final int MIN_NER_LENGTH = 2;

	final Map<EntityType, Set<Pattern>> pattern = new HashMap<>();

	public RegularExpressionNerlAnnotator(SystemScope scope, BasicRegExPattern patternFactory) {

		/**
		 * Initialize for all existing entity types.
		 */
		EntityType.getEntityTypes().forEach(e -> pattern.put(e, new HashSet<>()));

		for (EntityType et : pattern.keySet()) {
			pattern.get(et).add(patternFactory.toPattern(et));
		}

		for (Entry<EntityType, Set<Pattern>> etPattern : patternFactory.getHandMadePattern().entrySet()) {
			pattern.get(etPattern.getKey()).addAll(etPattern.getValue());
		}
	}

	public Map<EntityType, Set<DocumentLinkedAnnotation>> annotate(Document document) {

		Map<EntityType, Set<DocumentLinkedAnnotation>> annotationMap = new HashMap<>();

		pattern.keySet().stream().forEach(k -> annotationMap.put(k, new HashSet<>()));

		pattern.keySet().stream().forEach(entityType -> {

			Set<DocumentLinkedAnnotation> annotations = annotationMap.get(entityType);

			for (Pattern pattern : pattern.get(entityType)) {

				if (pattern == null)
					continue;

				Matcher matcher = pattern.matcher(document.documentContent);

				while (matcher.find()) {

					for (int index = 0; index < matcher.groupCount(); index++) {

						final String text = matcher.group(index);
						final int offset = matcher.start(index);

						index++;

						if (text == null || !text.equals(text.trim()) || text.length() < MIN_NER_LENGTH)
							continue;

						try {
							DocumentLinkedAnnotation docLinkedAnnotation = AnnotationBuilder.toAnnotation(document,
									entityType.entityName, text, offset);
							annotations.add(docLinkedAnnotation);
						} catch (Exception e) {
							log.warn("WARN:" + entityType.entityName + "-" + text + "-" + offset + ":"
									+ e.getMessage().substring(e.getMessage().indexOf(':')));
						}

						if (entityType.isLiteral) {
//if entity type is literal just take the first group							
							break;
						}
					}
				}
			}
//			System.out.println(document.documentID + ": found " + annotations.size() + " annotations for: "
//					+ entityType.entityName);
		});

		log.info(
				"Found " + annotationMap.values().stream().flatMap(v -> v.stream()).count() + " annotations in total!");

		return Collections.unmodifiableMap(annotationMap);
	}

}