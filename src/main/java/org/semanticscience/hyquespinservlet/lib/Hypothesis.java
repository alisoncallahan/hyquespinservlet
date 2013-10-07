package org.semanticscience.hyquespinservlet.lib;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class Hypothesis {

	private String inputJson = null;

	public Hypothesis(String aString) {
		inputJson = aString;
	}

	public Model makeHypothesisModel() {
		Model m = null;
		try {
			// model that will contain hypothesis RDF
			m = ModelFactory.createDefaultModel();

			// necessary resources and object properties for describing
			// hypothesis
			Resource eventType = m
					.createProperty("http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000004");
			Resource hypothesisType = m
					.createProperty("http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000000");
			Property rdftype = m
					.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
			Property rdfslabel = m
					.createProperty("http://www.w3.org/2000/01/rdf-schema#label");
			Property hasAgent = m
					.createProperty("http://semanticscience.org/ontology/hybrow.owl#HYBROW_0000000");
			Property hasTarget = m
					.createProperty("http://semanticscience.org/ontology/hybrow.owl#HYBROW_0000001");
			Property isNegated = m
					.createProperty("http://semanticscience.org/ontology/hybrow.owl#HYBROW_0000003");
			Property physicalContext = m
					.createProperty("http://semanticscience.org/ontology/hybrow.owl#HYBROW_0000004");
			Property hasComponentPart = m
					.createProperty("http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000010");
			Property specifies = m
					.createProperty("http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000012");

			// read json from input
			Map<String, Object> jsonMap = null;
				ObjectMapper mapper = new ObjectMapper();
				jsonMap = mapper.readValue(inputJson, Map.class);
			
			// create namespace for model
			DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
			Date date = new Date();
			String namespace = "http://bio2rdf.org/hyqueData:hypothesis_"
					+ dateFormat.format(date) + "_";

			// iterate over JSON map
			Iterator<String> itr = jsonMap.keySet().iterator();
			while (itr.hasNext()) {
				String key = itr.next();
				Map<String, Object> val = (Map<String, Object>) jsonMap
						.get(key);

				if (((Map<String, Object>) val).get("isa").equals("hypothesis")) {
					// write hypothesis details to model
					String hypothesisIdentifier = (String) val
							.get("identifier");
					Resource hypothesisResource = m.createResource(namespace
							+ hypothesisIdentifier);
					m.add(hypothesisResource, rdftype, hypothesisType);

					if (val.get("propositions") != null) {
						ArrayList<String> propositions = (ArrayList<String>) val
								.get("propositions");
						Iterator<String> pItr = propositions.iterator();
						while (pItr.hasNext()) {
							String proposition = pItr.next();
							Resource pResource = m.createResource(namespace
									+ proposition);
							m.add(hypothesisResource, hasComponentPart,
									pResource);
						}
					}
				} else if (((Map<String, Object>) val).get("isa").equals(
						"proposition")) {
					// write proposition details to model
					String propositionIdentifier = (String) val
							.get("identifier");
					String propositionType = (String) val.get("type");
					Resource propositionResource = m.createResource(namespace
							+ propositionIdentifier);
					Resource pType = m.createResource(propositionType);
					m.add(propositionResource, rdftype, pType);

					if (val.get("events") != null) {
						Map<String, Object> events = (Map<String, Object>) val
								.get("events");
						Iterator eItr = events.keySet().iterator();
						while (eItr.hasNext()) {
							String eKey = (String) eItr.next();
							String event = (String) events.get(eKey);
							Resource eResource = m.createResource(namespace
									+ event);
							m.add(propositionResource, specifies, eResource);
						}
					}

					if (val.get("propositions") != null) {
						Map<String, Object> propositions = (Map<String, Object>) val
								.get("propositions");
						Iterator pItr = propositions.keySet().iterator();
						while (pItr.hasNext()) {
							String pKey = (String) pItr.next();
							String proposition = (String) propositions
									.get(pKey);
							Resource pResource = m.createResource(namespace
									+ proposition);
							m.add(propositionResource, hasComponentPart,
									pResource);
						}
					}

				} else if (val.get("isa").equals("event")) {

					// get event details
					String identifier = (String) val.get("identifier");
					String label = (String) val.get("label");
					String type = (String) val.get("type");
					String agent = (String) val.get("agent");
					String target = (String) val.get("target");
					String negated = (String) val.get("negate");
					String location = null;
					if (val.get("location").toString().length() != 0) {
						location = (String) val.get("location");
					}
					String context = null;
					if (val.get("context").toString().length() != 0) {
						context = (String) val.get("context");
					}

					// write event details RDF to model
					Resource eventResource = m.createResource(namespace
							+ identifier);
					Resource typeResource = m.createResource(type);
					Resource agentResource = m.createResource(agent);
					Resource targetResource = m.createResource(target);

					Literal labelResource = m.createLiteral(label);
					Literal negatedResource = m.createTypedLiteral(negated,
							"http://www.w3.org/2001/XMLSchema#boolean");

					m.add(eventResource, rdftype, eventType);
					m.add(eventResource, rdftype, typeResource);
					m.add(eventResource, hasAgent, agentResource);
					m.add(eventResource, hasTarget, targetResource);
					m.add(eventResource, isNegated, negatedResource);
					m.add(eventResource, rdfslabel, labelResource);

					Resource locationResource = null;
					if (location != null) {
						locationResource = m.createResource(location);
						m.add(eventResource, physicalContext, locationResource);
					}
				} // elseif
			}// while

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return m;
	}
}
