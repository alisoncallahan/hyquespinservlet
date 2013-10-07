package org.semanticscience.hyquespinservlet.lib;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class HypothesisParser {

	private Model model = null;
	private Model spinModel = null;

	private HypothesisParser(Model aModel, OntModel spinM) {
		model = aModel;
		spinModel = spinM;
	}

	public static String getJSONString(Model m, OntModel spinM) {
		HypothesisParser hp = new HypothesisParser(m, spinM);
		String json = hp.generateJSON2(m, spinM);
		return json;
	}

	public static String getJstreeJSON(Model m,
			OntModel spinM) {
		HypothesisParser hp = new HypothesisParser(m, spinM);
		JSONObject jstreeJSON = hp.generateJstreeJSON(m, spinM);
		return jstreeJSON.toString();
	}

	/**
	 * This method generates a JSON representation of only hypothesis object
	 * details and event object details returned by HyQue SPIN evaluation
	 * 
	 * @param m
	 *            contains the output of SPIN execution
	 * @param spinM
	 *            contains SPIN rules
	 * @return JSON representation of subset of m describing only hypothesis and
	 *         event(s)
	 */
	private String generateJSON(Model m, OntModel spinM) {
		JsonFactory f = new JsonFactory();
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		JsonGenerator g = null;
		try {
			g = f.createJsonGenerator(out);
			g.writeStartObject();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			g.writeObjectFieldStart("hypothesisEval");
		} catch (JsonGenerationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		String hDetailsQueryString = " PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "SELECT ?h ?hEvalScore ?hEvalRule WHERE { "
				+ "?h rdf:type <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000000> ."
				+ "?h <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000008> ?hEval ."
				+ "?hEval <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000013> ?hEvalScore ."
				+ "?hEval <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000030> ?hEvalRule ."
				+ "}";

		Query hDetailsQuery = QueryFactory.create(hDetailsQueryString);
		QueryExecution hDetailsQExec = QueryExecutionFactory.create(
				hDetailsQuery, m);
		try {
			ResultSet results = hDetailsQExec.execSelect();
			while (results.hasNext()) {
				QuerySolution soln = results.nextSolution();
				RDFNode h = soln.get("h");
				String hEvalScore = soln.getLiteral("hEvalScore").getValue()
						.toString();
				RDFNode hEvalRule = soln.get("hEvalRule");
				try {
					g.writeStringField("type", "hypothesis");
					g.writeStringField("hypothesisURI", h.toString());
					g.writeStringField("score", hEvalScore);
					g.writeStringField("rule", hEvalRule.toString());
				} catch (JsonGenerationException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} finally {
			hDetailsQExec.close();
		}

		try {
			g.writeEndObject();
		} catch (JsonGenerationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			g.writeObjectFieldStart("events");
		} catch (JsonGenerationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String eSupportingDetailsQueryString = " PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "SELECT ?e ?eEvalScore ?eEvalRule ?eType WHERE { "
				+ "?e rdf:type <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000004> ."
				+ "?e <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000008> ?eEval ."
				+ "?e rdf:type ?eType ."
				+ "?eEval <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000013> ?eEvalScore ."
				+ "?eEval <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000030> ?eEvalRule ."
				+ "FILTER(?eEvalScore > 0)."
				+ "FILTER(?eEvalRule != false)."
				+ "FILTER(?eType != <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000004>) ."
				+ "}";
		int eCounter = 0;
		Query eDetailsQuery = QueryFactory
				.create(eSupportingDetailsQueryString);
		QueryExecution eDetailsQExec = QueryExecutionFactory.create(
				eDetailsQuery, m);
		try {
			ResultSet results = eDetailsQExec.execSelect();
			while (results.hasNext()) {
				eCounter++;
				QuerySolution soln = results.nextSolution();
				RDFNode e = soln.get("e");
				RDFNode eType = soln.get("eType");
				String eEvalScore = soln.getLiteral("eEvalScore").getValue()
						.toString();
				RDFNode eEvalRule = soln.get("eEvalRule");

				// query for data that supports the event
				String supportingDataQueryString = " PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
						+ "SELECT ?data ?dType WHERE { "
						+ "<"
						+ e.toString()
						+ "> <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000011> ?data ."
						+ "}";

				// query for event subevaluations
				String eventSubEvaluationsQueryString = " PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
						+ "SELECT ?subEval ?subEvalScore ?subEvalRule WHERE { "
						+ "<"
						+ e.toString()
						+ "> <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000008> ?evaluation ."
						+ "?evaluation <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000007> ?subEval ."
						+ "?subEval <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000013> ?subEvalScore . "
						+ "?subEval <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000030> ?subEvalRule ."
						+ "}";
				try {
					g.writeObjectFieldStart("event" + eCounter);
					g.writeStringField("eventURI", e.toString());
					g.writeStringField("eventType", eType.toString());
					g.writeStringField("score", eEvalScore);
					g.writeStringField("rule", eEvalRule.toString());

					int dCounter = 0;
					Query supportingDataQuery = QueryFactory
							.create(supportingDataQueryString);
					QueryExecution supportingDataQExec = QueryExecutionFactory
							.create(supportingDataQuery, m);
					try {
						g.writeObjectFieldStart("eventSupportingData");
						ResultSet dataResults = supportingDataQExec
								.execSelect();
						while (dataResults.hasNext()) {
							dCounter++;
							QuerySolution dsoln = dataResults.nextSolution();
							RDFNode data = dsoln.get("data");
							g.writeObjectFieldStart("data" + dCounter);
							g.writeStringField("dataURI", data.toString());
							g.writeEndObject();
						}
						g.writeEndObject();
					} finally {
						supportingDataQExec.close();
					}

					int seCounter = 0;
					Query eventSubEvalQuery = QueryFactory
							.create(eventSubEvaluationsQueryString);
					QueryExecution eventSubEvalQExec = QueryExecutionFactory
							.create(eventSubEvalQuery, m);
					try {
						g.writeObjectFieldStart("eventSubEvaluations");
						ResultSet subEvalResults = eventSubEvalQExec
								.execSelect();
						while (subEvalResults.hasNext()) {
							seCounter++;
							QuerySolution sesoln = subEvalResults
									.nextSolution();
							RDFNode subEval = sesoln.get("subEval");
							RDFNode subEvalRule = sesoln.get("subEvalRule");
							String subEvalRuleString = null;
							if (subEvalRule.isLiteral()) {
								subEvalRuleString = sesoln
										.getLiteral("subEvalRule").getValue()
										.toString();
							} else {
								subEvalRuleString = subEvalRule.toString();
							}
							String subEvalScore = sesoln
									.getLiteral("subEvalScore").getValue()
									.toString();
							g.writeObjectFieldStart("subEval" + seCounter);
							g.writeStringField("subEvalURI", subEval.toString());
							g.writeStringField("subEvalRule", subEvalRuleString);
							g.writeStringField("subEvalScore", subEvalScore);
							g.writeEndObject();
						}
						g.writeEndObject();
					} finally {
						eventSubEvalQExec.close();
					}
				} catch (JsonGenerationException exception) {
					exception.printStackTrace();
				} catch (IOException exception) {
					exception.printStackTrace();
				}
			}
		} finally {
			eDetailsQExec.close();
		}

		try {
			g.writeEndObject();
			g.close();
		} catch (JsonGenerationException exception) {
			// TODO Auto-generated catch block
			exception.printStackTrace();
		} catch (IOException exception) {
			// TODO Auto-generated catch block
			exception.printStackTrace();
		}
		return out.toString();
	}

	private String generateJSON2(Model evaluationModel, OntModel spinM) {

		JsonFactory f = new JsonFactory();
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		JsonGenerator g = null;
		try {
			g = f.createJsonGenerator(out);
			g.writeStartObject();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// hypothesis
		try {
			g.writeObjectFieldStart("hypothesisEval");
		} catch (JsonGenerationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// get details about hypothesis evaluation - evalU
		String hDetailsQueryString = " PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "SELECT ?h ?hEval ?hEvalScore ?hEvalRule WHERE { "
				+ "?h rdf:type <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000000> ."
				+ "?h <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000008> ?hEval ."
				+ "?hEval <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000013> ?hEvalScore ."
				+ "?hEval <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000030> ?hEvalRule ."
				+ "}";

		Query hDetailsQuery = QueryFactory.create(hDetailsQueryString);
		QueryExecution hDetailsQExec = QueryExecutionFactory.create(
				hDetailsQuery, evaluationModel);
		try {
			ResultSet results = hDetailsQExec.execSelect();
			while (results.hasNext()) {
				QuerySolution soln = results.nextSolution();
				RDFNode h = soln.get("h");
				RDFNode hEval = soln.get("hEval");
				String hEvalScore = soln.getLiteral("hEvalScore").getValue()
						.toString();
				RDFNode hEvalRule = soln.get("hEvalRule");
				try {
					g.writeStringField("type", "hypothesis");
					g.writeStringField("hypothesisURI", h.toString());
					g.writeStringField("hypotheisEvalURI", hEval.toString());
					g.writeStringField("score", hEvalScore);
					g.writeStringField("rule", hEvalRule.toString());
				} catch (JsonGenerationException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} finally {
			hDetailsQExec.close();
		}

		String hPropositionsQueryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "SELECT ?p WHERE {"
				+ "?h rdf:type <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000000> ."
				+ "?h <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000010> ?p ."
				+ "}";
		Query hPropositionsQuery = QueryFactory
				.create(hPropositionsQueryString);
		QueryExecution hPropositionsQExec = QueryExecutionFactory.create(
				hPropositionsQuery, evaluationModel);

		try {
			g.writeObjectFieldStart("propositions");
			ResultSet results = hPropositionsQExec.execSelect();
			int pCounter = 0;
			while (results.hasNext()) {
				pCounter++;
				QuerySolution soln = results.nextSolution();
				RDFNode p = soln.get("p");
				g.writeObjectFieldStart("proposition" + pCounter);
				g.writeStringField("propositionURI", p.toString());
				g.writeEndObject();
			}
			g.writeEndObject();
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			hPropositionsQExec.close();
		}

		try {
			g.writeEndObject();
		} catch (JsonGenerationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// propositions
		try {
			g.writeObjectFieldStart("propositions");
		} catch (JsonGenerationException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		String propositionQueryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "SELECT ?p ?pEval ?pEvalScore ?pEvalRule ?pType WHERE {"
				+ "?somePorH <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000010> ?p ."
				+ "?p rdf:type ?pType ."
				+ "?p <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000008> ?pEval ."
				+ "?pEval <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000013> ?pEvalScore ."
				//proposition is not linked to rule for proposition type HYPOTHESIS_0000014 -- add to rule!!!
				+ "?pEval <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000030> ?pEvalRule ."
				+ "}";
		int pCounter = 0;
		Query pDetailsQuery = QueryFactory.create(propositionQueryString);
		QueryExecution pDetailsQExec = QueryExecutionFactory.create(
				pDetailsQuery, evaluationModel);
		try {
			ResultSet results = pDetailsQExec.execSelect();
			while (results.hasNext()) {
				pCounter++;

				QuerySolution soln = results.nextSolution();
				RDFNode p = soln.get("p");
				RDFNode pType = soln.get("pType");
				RDFNode pEval = soln.get("pEval");
				String pEvalScore = soln.getLiteral("pEvalScore").getValue()
						.toString();
				RDFNode pEvalRule = soln.get("pEvalRule");

				// query for sub-propositions
				String subPropositionQueryString = " PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
						+ "SELECT ?p WHERE { "
						+ "<"
						+ p.toString()
						+ "> <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000010> ?p ."
						+ "}";

				String subEventQueryString = " PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
						+ "SELECT ?e WHERE { "
						+ "<"
						+ p.toString()
						+ "> <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000012> ?e ."
						+ "}";

				try {
					// write proposition json
					g.writeObjectFieldStart("proposition" + pCounter);
					g.writeStringField("type", "proposition");
					g.writeStringField("propositionURI", p.toString());
					g.writeStringField("propositionType", pType.toString());
					g.writeStringField("propositionEvalURI", pEval.toString());
					g.writeStringField("score", pEvalScore);
					g.writeStringField("rule", pEvalRule.toString());

					Query subPropositionQuery = QueryFactory
							.create(subPropositionQueryString);
					QueryExecution subPropositionQExec = QueryExecutionFactory
							.create(subPropositionQuery, evaluationModel);
					try {
						g.writeObjectFieldStart("propositions");
						ResultSet subPResults = subPropositionQExec
								.execSelect();
						int subPCounter = 0;
						while (subPResults.hasNext()) {
							subPCounter++;
							QuerySolution subPSoln = subPResults.nextSolution();
							RDFNode subP = subPSoln.get("p");
							g.writeObjectFieldStart("proposition" + subPCounter);
							g.writeStringField("propositionURI",
									subP.toString());
							g.writeEndObject();
						}
						g.writeEndObject();

					} finally {
						subPropositionQExec.close();
					}

					Query subEventQuery = QueryFactory
							.create(subEventQueryString);
					QueryExecution subEventQExec = QueryExecutionFactory
							.create(subEventQuery, evaluationModel);
					try {
						g.writeObjectFieldStart("events");
						ResultSet subEResults = subEventQExec.execSelect();
						int subECounter = 0;
						while (subEResults.hasNext()) {
							subECounter++;
							QuerySolution subESoln = subEResults.nextSolution();
							RDFNode subE = subESoln.get("e");
							g.writeObjectFieldStart("event" + subECounter);
							g.writeStringField("eventURI", subE.toString());
							g.writeEndObject();
						}
						g.writeEndObject();
					} finally {
						subEventQExec.close();
					}

					g.writeEndObject();
				} catch (JsonGenerationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		} finally {
			pDetailsQExec.close();
		}

		try {
			g.writeEndObject();
		} catch (JsonGenerationException exception) {
			// TODO Auto-generated catch block
			exception.printStackTrace();
		} catch (IOException exception) {
			// TODO Auto-generated catch block
			exception.printStackTrace();
		}

		// events
		try {
			g.writeObjectFieldStart("events");
		} catch (JsonGenerationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String eSupportingDetailsQueryString = " PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "SELECT ?e ?eEval ?eEvalScore ?eEvalRule ?eType WHERE { "
				+ "?e rdf:type <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000004> ."
				+ "?e <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000008> ?eEval ."
				+ "?e rdf:type ?eType ."
				+ "?eEval <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000013> ?eEvalScore ."
				+ "?eEval <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000030> ?eEvalRule ."
				+ "FILTER(?eEvalScore > 0)."
				+ "FILTER(?eEvalRule != false)."
				+ "FILTER(?eType != <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000004>) ."
				+ "}";
		int eCounter = 0;
		Query eDetailsQuery = QueryFactory
				.create(eSupportingDetailsQueryString);
		QueryExecution eDetailsQExec = QueryExecutionFactory.create(
				eDetailsQuery, evaluationModel);
		try {
			ResultSet results = eDetailsQExec.execSelect();
			while (results.hasNext()) {
				eCounter++;
				QuerySolution soln = results.nextSolution();
				RDFNode e = soln.get("e");
				RDFNode eType = soln.get("eType");
				RDFNode eEval = soln.get("eEval");
				String eEvalScore = soln.getLiteral("eEvalScore").getValue()
						.toString();
				RDFNode eEvalRule = soln.get("eEvalRule");

				// query for data that supports the event
				String supportingDataQueryString = " PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
						+ "SELECT ?data ?dType WHERE { "
						+ "<"
						+ e.toString()
						+ "> <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000011> ?data ."
						+ "}";

				// query for event subevaluations
				String eventSubEvaluationsQueryString = " PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
						+ "SELECT ?subEval ?subEvalScore ?subEvalRule WHERE { "
						+ "<"
						+ e.toString()
						+ "> <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000008> ?evaluation ."
						+ "?evaluation <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000007> ?subEval ."
						+ "?subEval <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000013> ?subEvalScore . "
						+ "?subEval <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000030> ?subEvalRule ."
						+ "}";
				try {
					g.writeObjectFieldStart("event" + eCounter);
					g.writeStringField("type", "event");
					g.writeStringField("eventURI", e.toString());
					g.writeStringField("eventType", eType.toString());
					g.writeStringField("eventEvalURI", eEval.toString());
					g.writeStringField("score", eEvalScore);
					g.writeStringField("rule", eEvalRule.toString());

					int dCounter = 0;
					Query supportingDataQuery = QueryFactory
							.create(supportingDataQueryString);
					QueryExecution supportingDataQExec = QueryExecutionFactory
							.create(supportingDataQuery, evaluationModel);
					try {
						g.writeObjectFieldStart("eventSupportingData");
						ResultSet dataResults = supportingDataQExec
								.execSelect();
						while (dataResults.hasNext()) {
							dCounter++;
							QuerySolution dsoln = dataResults.nextSolution();
							RDFNode data = dsoln.get("data");
							g.writeObjectFieldStart("data" + dCounter);
							g.writeStringField("dataURI", data.toString());
							g.writeEndObject();
						}
						g.writeEndObject();
					} finally {
						supportingDataQExec.close();
					}

					int seCounter = 0;
					Query eventSubEvalQuery = QueryFactory
							.create(eventSubEvaluationsQueryString);
					QueryExecution eventSubEvalQExec = QueryExecutionFactory
							.create(eventSubEvalQuery, evaluationModel);
					try {
						g.writeObjectFieldStart("eventSubEvaluations");
						ResultSet subEvalResults = eventSubEvalQExec
								.execSelect();
						while (subEvalResults.hasNext()) {
							seCounter++;
							QuerySolution sesoln = subEvalResults
									.nextSolution();
							RDFNode subEval = sesoln.get("subEval");
							RDFNode subEvalRule = sesoln.get("subEvalRule");
							String subEvalRuleString = null;
							if (subEvalRule.isLiteral()) {
								subEvalRuleString = sesoln
										.getLiteral("subEvalRule").getValue()
										.toString();
							} else {
								subEvalRuleString = subEvalRule.toString();
							}
							String subEvalScore = sesoln
									.getLiteral("subEvalScore").getValue()
									.toString();
							g.writeObjectFieldStart("subEval" + seCounter);
							g.writeStringField("subEvalURI", subEval.toString());
							g.writeStringField("subEvalRule", subEvalRuleString);
							g.writeStringField("subEvalScore", subEvalScore);
							g.writeEndObject();
						}
						g.writeEndObject();
					} finally {
						eventSubEvalQExec.close();
					}
				} catch (JsonGenerationException exception) {
					exception.printStackTrace();
				} catch (IOException exception) {
					exception.printStackTrace();
				}
			}
		} finally {
			eDetailsQExec.close();
		}

		try {
			g.writeEndObject();
			g.close();
		} catch (JsonGenerationException exception) {
			// TODO Auto-generated catch block
			exception.printStackTrace();
		} catch (IOException exception) {
			// TODO Auto-generated catch block
			exception.printStackTrace();
		}
		return out.toString();
	}

	private JSONObject generateJstreeJSON(
			Model evaluationModel, OntModel spinM) {

		HashMap<String, JSONArray> eventMap = this.generateEventsJSON(
				evaluationModel, spinM);

		HashMap<String, JSONArray> propositionsMap = new HashMap<String, JSONArray>();

		// propositions
		String propositionQueryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "SELECT * WHERE {"
				+ "?somePorH <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000010> ?p ."
				+ "?p rdf:type ?pType ."
				+ "?p <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000008> ?pEval ."
				+ "?pEval <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000013> ?pEvalScore ."
				+ "OPTIONAL { "
				+ "?pEval <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000030> ?pEvalRule ."
				+ "} "
				+ "BIND(IF(?pType = <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000014>, <http://bio2rdf.org/hyque-rules#calculateAtomicPropositionScore>, ?pEvalRule) AS ?evalRule) "
				+ "}";
		Query pDetailsQuery = QueryFactory.create(propositionQueryString);
		QueryExecution pDetailsQExec = QueryExecutionFactory.create(
				pDetailsQuery, evaluationModel);
		try {
			ResultSet results = pDetailsQExec.execSelect();
			while (results.hasNext()) {

				QuerySolution soln = results.nextSolution();
				RDFNode p = soln.get("p");
				RDFNode pType = soln.get("pType");
				RDFNode pEval = soln.get("pEval");
				String pEvalScore = soln.getLiteral("pEvalScore").getValue()
						.toString();
				RDFNode pEvalRule = soln.get("evalRule");

				// query for sub-propositions
				String subPropositionQueryString = " PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
						+ "SELECT ?p WHERE { "
						+ "<"
						+ p.toString()
						+ "> <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000010> ?p ."
						+ "}";

				// query for events
				String subEventQueryString = " PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
						+ "SELECT ?e WHERE { "
						+ "<"
						+ p.toString()
						+ "> <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000012> ?e ."
						+ "}";

				JSONArray propArray = new JSONArray();
				JSONObject proposition = new JSONObject();
				JSONObject metadata = new JSONObject();

				try {
					proposition.put("data", p.toString());
					metadata.put("id", p.toString());
					metadata.put("isa", "proposition");
					metadata.put("propositionURI", p.toString());
					metadata.put("type", pType.toString());
					metadata.put("propositionEvalURI", pEval.toString());
					metadata.put("score", pEvalScore);
					metadata.put("rule", pEvalRule.toString());

					Query subEventQuery = QueryFactory
							.create(subEventQueryString);
					QueryExecution subEventQExec = QueryExecutionFactory
							.create(subEventQuery, evaluationModel);

					Query subPropositionQuery = QueryFactory
							.create(subPropositionQueryString);
					QueryExecution subPropositionQExec = QueryExecutionFactory
							.create(subPropositionQuery, evaluationModel);

					JSONArray children = new JSONArray();
					try {
						ResultSet subEResults = subEventQExec.execSelect();
						while (subEResults.hasNext()) {
							QuerySolution subESoln = subEResults.nextSolution();
							RDFNode subE = subESoln.get("e");
							children.put(eventMap.get(subE.toString()));
						}
					} finally {
						subEventQExec.close();
					}

					try {
						ResultSet subPResults = subPropositionQExec
								.execSelect();
						while (subPResults.hasNext()) {
							QuerySolution subPSoln = subPResults.nextSolution();
							RDFNode subP = subPSoln.get("p");
							children.put(propositionsMap.get(subP.toString()));
							propositionsMap.remove(subP.toString());
						}
					} finally {
						subPropositionQExec.close();
					}
					
					JSONObject attr = new JSONObject();
					attr.put("rel", "proposition");
					proposition.put("metadata", metadata);
					proposition.put("attr", attr);
					proposition.put("children", children);
					proposition.put("state", "open");
					propArray.put(proposition);
				
					propositionsMap.put(p.toString(), propArray);
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
			}
		} finally {
			pDetailsQExec.close();
		}

		// hypothesis
		String hDetailsQueryString = " PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "SELECT ?h ?hEval ?hEvalScore ?hEvalRule WHERE { "
				+ "?h rdf:type <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000000> ."
				+ "?h <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000008> ?hEval ."
				+ "?hEval <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000013> ?hEvalScore ."
				+ "?hEval <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000030> ?hEvalRule ."
				+ "}";

		Query hDetailsQuery = QueryFactory.create(hDetailsQueryString);
		QueryExecution hDetailsQExec = QueryExecutionFactory.create(
				hDetailsQuery, evaluationModel);
		
		JSONObject jstreeData = new JSONObject();
		JSONArray hArray = new JSONArray();
		JSONObject hypothesis = new JSONObject();
		JSONObject metadata = new JSONObject();
		JSONArray hChildren = new JSONArray();
		try {
			ResultSet results = hDetailsQExec.execSelect();
			while (results.hasNext()) {
				
				QuerySolution soln = results.nextSolution();
				RDFNode h = soln.get("h");
				RDFNode hEval = soln.get("hEval");
				String hEvalScore = soln.getLiteral("hEvalScore").getValue()
						.toString();
				RDFNode hEvalRule = soln.get("hEvalRule");
				
				try {
					hypothesis.put("data", h.toString());
					metadata.put("id", h.toString());
					metadata.put("isa", "hypothesis");
					metadata.put("hEvalURI", hEval.toString());
					metadata.put("hEvalScore", hEvalScore);
					metadata.put("heEvalRule", hEvalRule.toString());
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}// while
		} finally {
			hDetailsQExec.close();
		}

		String hPropositionsQueryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "SELECT ?p WHERE {"
				+ "?h rdf:type <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000000> ."
				+ "?h <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000010> ?p ."
				+ "}";
		Query hPropositionsQuery = QueryFactory
				.create(hPropositionsQueryString);
		QueryExecution hPropositionsQExec = QueryExecutionFactory
				.create(hPropositionsQuery, evaluationModel);

		try {
			ResultSet pResults = hPropositionsQExec.execSelect();
			while (pResults.hasNext()) {
				QuerySolution psoln = pResults.nextSolution();
				RDFNode p = psoln.get("p");
				hChildren.put(propositionsMap.get(p.toString()));
			}
			
		} finally {
			hPropositionsQExec.close();
		}
		
		try {
			JSONObject attr = new JSONObject();
			attr.put("rel", "hypothesis");
			hypothesis.put("metadata", metadata);
			hypothesis.put("attr", attr);
			hypothesis.put("children", hChildren);
			hypothesis.put("state", "open");
			hArray.put(hypothesis);
			jstreeData.put("data", hArray);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jstreeData;
	}

	private HashMap<String, JSONArray> generateEventsJSON(
			Model evaluationModel, OntModel spinM) {
		HashMap<String, JSONArray> eventMap = new HashMap<String, JSONArray>();

		// get events
		String eSupportingDetailsQueryString = " PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "SELECT ?e ?eEval ?eEvalScore ?eEvalRule ?eType WHERE { "
				+ "?e rdf:type <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000004> ."
				+ "?e <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000008> ?eEval ."
				+ "?e rdf:type ?eType ."
				+ "?eEval <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000013> ?eEvalScore ."
				+ "?eEval <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000030> ?eEvalRule ."
				+ "FILTER(?eEvalScore > 0)."
				+ "FILTER(?eEvalRule != false)."
				+ "FILTER(?eType != <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000004>) ."
				+ "}";

		Query eDetailsQuery = QueryFactory
				.create(eSupportingDetailsQueryString);
		QueryExecution eDetailsQExec = QueryExecutionFactory.create(
				eDetailsQuery, evaluationModel);
		try {
			ResultSet results = eDetailsQExec.execSelect();
			while (results.hasNext()) {
				QuerySolution soln = results.nextSolution();
				RDFNode e = soln.get("e");
				RDFNode eType = soln.get("eType");
				RDFNode eEval = soln.get("eEval");
				String eEvalScore = soln.getLiteral("eEvalScore").getValue()
						.toString();
				RDFNode eEvalRule = soln.get("eEvalRule");

				// query for data that supports the event
				String supportingDataQueryString = " PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
						+ "SELECT ?data ?dType WHERE { "
						+ "<"
						+ e.toString()
						+ "> <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000011> ?data ."
						+ "}";

				// query for event subevaluations
				String eventSubEvaluationsQueryString = " PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
						+ "SELECT DISTINCT ?subEval ?subEvalScore ?subEvalRule WHERE { "
						+ "<"
						+ e.toString()
						+ "> <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000008> ?evaluation ."
						+ "?evaluation <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000007> ?subEval ."
						+ "?subEval <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000013> ?subEvalScore . "
						+ "?subEval <http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000030> ?subEvalRule ."
						+ "}";

				// using jettison
				JSONArray eventArray = new JSONArray();
				JSONObject event = new JSONObject();
				JSONObject metadata = new JSONObject();
				
				try {
					event.put("data", e.toString());

					metadata.put("id", e.toString());
					metadata.put("isa", "event");
					metadata.put("eventURI", e.toString());
					metadata.put("type", eType.toString());
					metadata.put("eventEvalURI", eEval.toString());
					metadata.put("score", eEvalScore);
					metadata.put("rule", eEvalRule.toString());

				} catch (JSONException e1) {
					e1.printStackTrace();
				}

				int dCounter = 0;

				Query supportingDataQuery = QueryFactory
						.create(supportingDataQueryString);
				QueryExecution supportingDataQExec = QueryExecutionFactory
						.create(supportingDataQuery, evaluationModel);
				try {
					JSONObject eSupportingData = new JSONObject();
					ResultSet dataResults = supportingDataQExec.execSelect();
					while (dataResults.hasNext()) {
						dCounter++;
						QuerySolution dsoln = dataResults.nextSolution();
						RDFNode dataNode = dsoln.get("data");

						JSONObject supportingDataObj = new JSONObject();
						supportingDataObj.put("dataURI", dataNode.toString());
						eSupportingData.put("data" + dCounter,
								supportingDataObj);
					}// while
					metadata.put("eventSupportingData", eSupportingData);
				} catch (JSONException e1) {
					e1.printStackTrace();
				} finally {
					supportingDataQExec.close();
				}

				int seCounter = 0;

				Query eventSubEvalQuery = QueryFactory
						.create(eventSubEvaluationsQueryString);
				QueryExecution eventSubEvalQExec = QueryExecutionFactory
						.create(eventSubEvalQuery, evaluationModel);
				try {
					JSONObject eSubEvaluations = new JSONObject();
					ResultSet subEvalResults = eventSubEvalQExec.execSelect();
					while (subEvalResults.hasNext()) {
						seCounter++;
						QuerySolution sesoln = subEvalResults.nextSolution();
						RDFNode subEval = sesoln.get("subEval");
						RDFNode subEvalRule = sesoln.get("subEvalRule");

						String subEvalRuleString = null;
						if (subEvalRule.isLiteral()) {
							subEvalRuleString = sesoln
									.getLiteral("subEvalRule").getValue()
									.toString();
						} else {
							subEvalRuleString = subEvalRule.toString();
						}

						JSONObject subEvalObj = new JSONObject();
						subEvalObj.put("subEvalURI", subEval.toString());

						if (subEvalRuleString != null) {
							String subEvalScore = sesoln
									.getLiteral("subEvalScore").getValue()
									.toString();
							subEvalObj.put("subEvalRule", subEvalRuleString);
							subEvalObj.put("subEvalScore", subEvalScore);
						}
						eSubEvaluations.put("subEval" + seCounter, subEvalObj);
					}
					metadata.put("eventSubEvaluations", eSubEvaluations);
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} finally {
					eventSubEvalQExec.close();
				}
				JSONObject attr = new JSONObject();
				attr.put("rel", "event");
				
				event.put("metadata", metadata);
				event.put("attr", attr);
				
				eventArray.put(event);
				eventMap.put(e.toString(), eventArray);
			}// while
		} catch (JSONException e1) {
			e1.printStackTrace();
		} finally {
			eDetailsQExec.close();
		}
		return eventMap;
	}

	public Model getModel() {
		return model;
	}
}
