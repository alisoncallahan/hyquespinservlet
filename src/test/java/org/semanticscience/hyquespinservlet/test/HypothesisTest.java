package org.semanticscience.hyquespinservlet.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

import org.apache.jena.atlas.io.IndentedWriter;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticscience.hyquespinservlet.lib.Hypothesis;
import org.semanticscience.hyquespinservlet.lib.HypothesisParser;
import org.semanticscience.hyquespinservlet.server.HyQueSPINExecutor;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.arq.SPINARQFunction;
import org.topbraid.spin.arq.SPINARQPFunction;
import org.topbraid.spin.model.Ask;
import org.topbraid.spin.model.Element;
import org.topbraid.spin.model.Function;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.model.Select;
import org.topbraid.spin.system.SPINModuleRegistry;
import org.topbraid.spin.util.SystemTriples;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class HypothesisTest {

	private static String testJson = "{\"p1\":{\"type\":\"http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000002\",\"events\":{\"e1\":\"e1\",\"e2\":\"e2\",\"e3\":\"e3\"},\"identifier\":\"p1\",\"isa\":\"proposition\"},\"p2\":{\"type\":\"http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000002\",\"events\":{\"e4\":\"e4\",\"e5\":\"e5\",\"e6\":\"e6\"},\"identifier\":\"p2\",\"isa\":\"proposition\"},\"p3\":{\"type\":\"http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000003\",\"propositions\":{\"p1\":\"p1\",\"p2\":\"p2\"},\"identifier\":\"p3\",\"isa\":\"proposition\"},\"e1\":{\"label\":\"Gal4p induces expression of GAL1\",\"type\":\"http://bio2rdf.org/go:0010628\",\"negate\":\"false\",\"agent\":\"http://bio2rdf.org/sgd:Gal4p\",\"target\":\"http://bio2rdf.org/sgd:GAL1\",\"location\":\"\",\"context\":\"\",\"buttons\":{\"clear\":\"Reset\"},\"identifier\":\"e1\",\"isa\":\"event\"},\"e2\":{\"label\":\"Gal3p induces expression of GAL2\",\"type\":\"http://bio2rdf.org/go:0010628\",\"negate\":\"false\",\"agent\":\"http://bio2rdf.org/sgd:Gal3p\",\"target\":\"http://bio2rdf.org/sgd:GAL2\",\"location\":\"\",\"context\":\"\",\"buttons\":{\"clear\":\"Reset\"},\"identifier\":\"e2\",\"isa\":\"event\"},\"e3\":{\"label\":\"Gal4p induces expression of GAL7\",\"type\":\"http://bio2rdf.org/go:0010628\",\"negate\":\"false\",\"agent\":\"http://bio2rdf.org/sgd:Gal4p\",\"target\":\"http://bio2rdf.org/sgd:GAL7\",\"location\":\"\",\"context\":\"\",\"buttons\":{\"clear\":\"Reset\"},\"identifier\":\"e3\",\"isa\":\"event\"},\"e4\":{\"label\":\"Gal4p induces expression of GAL7\",\"type\":\"http://bio2rdf.org/go:0010628\",\"negate\":\"false\",\"agent\":\"http://bio2rdf.org/sgd:Gal4p\",\"target\":\"http://bio2rdf.org/sgd:GAL7\",\"location\":\"\",\"context\":\"\",\"buttons\":{\"clear\":\"Reset\"},\"identifier\":\"e4\",\"isa\":\"event\"},\"e5\":{\"label\":\"Gal80p induces expression of GAL7\",\"type\":\"http://bio2rdf.org/go:0010628\",\"negate\":\"false\",\"agent\":\"http://bio2rdf.org/sgd:Gal80p\",\"target\":\"http://bio2rdf.org/sgd:GAL7\",\"location\":\"\",\"context\":\"\",\"buttons\":{\"clear\":\"Reset\"},\"identifier\":\"e5\",\"isa\":\"event\"},\"e6\":{\"label\":\"Gal80p represses Gal4p\",\"type\":\"http://bio2rdf.org/go:0044092\",\"negate\":\"false\",\"agent\":\"http://bio2rdf.org/sgd:Gal80p\",\"target\":\"http://bio2rdf.org/sgd:Gal4p\",\"location\":\"\",\"context\":\"\",\"buttons\":{\"clear\":\"Reset\"},\"identifier\":\"e6\",\"isa\":\"event\"},\"h1\":{\"label\":\"GAL hypothesis 1\",\"description\":\"Six event hypothesis\",\"author\":\"Alison\",\"source\":\"\",\"propositions\":[\"p3\"],\"isa\":\"hypothesis\",\"identifier\":\"h1\"}}";
	private static String testJson2 = "{\"p1\":{\"type\":\"http://semanticscience.org/ontology/hyque.owl#HYPOTHESIS_0000014\",\"events\":{\"e1\":\"e1\"},\"identifier\":\"p1\",\"isa\":\"proposition\"},\"e1\":{\"label\":\"Gal4p induces expression of GAL1\",\"type\":\"http://bio2rdf.org/go:0010628\",\"negate\":\"false\",\"agent\":\"http://bio2rdf.org/sgd:Gal4p\",\"target\":\"http://bio2rdf.org/sgd:GAL1\",\"location\":\"\",\"context\":\"\",\"buttons\":{\"clear\":\"Reset\"},\"identifier\":\"e1\",\"isa\":\"event\"},\"h1\":{\"label\":\"\",\"description\":\"\",\"author\":\"\",\"source\":\"\",\"propositions\":[\"p1\"],\"isa\":\"hypothesis\",\"identifier\":\"h1\"}}";
	private static Hypothesis hyp = null;
	private static File f = null;
	private static FileInputStream fis = null;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		hyp = new Hypothesis(testJson2);
		//f = new File("/home/alison/Desktop/hypothesis_evaluation.n3");
		f = new File("/home/alison/Desktop/hypothesis1output.rdf");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		hyp = null;
	}

	/*@Test
	public void testMakeModel() {
		Model m = hyp.makeHypothesisModel();
		int count = 0;
		StmtIterator sItr = m.listStatements();
		while(sItr.hasNext()){
			Statement s = sItr.next();
			System.out.println(s.toString());
			count++;
		}
		boolean b = false;
		if(count > 0){
			b = true;
		}
		assertTrue(b);
	}*/
	
	/*
	@Test
	public void testEvaluation(){
		Model m = ModelFactory.createDefaultModel();
		InputStream rulesIS = HyQueSPINExecutor.class.getClassLoader().getResourceAsStream("hyque-rules.spin.rdf");
		OntModel spinOnlyModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		
		spinOnlyModel.read(rulesIS, null);
		m = hyp.makeHypothesisModel();
		
	}*/
	
	/*@Test
	public void testGenerateJSON(){
		Model m = ModelFactory.createDefaultModel();
		InputStream rulesIS = HyQueSPINExecutor.class.getClassLoader()
				.getResourceAsStream("hyque-rules.spin.rdf");
		OntModel spinOnlyModel = ModelFactory
				.createOntologyModel(OntModelSpec.OWL_MEM);
		spinOnlyModel.read(rulesIS, null);
		try {
			fis = new FileInputStream(f);
			m.read(fis, null, "RDF/XML");
			//m = hyp.makeHypothesisModel();
			//String out = HypothesisParser.getJSONString(m, spinOnlyModel);
			String json = HypothesisParser.getJstreeJSON(m, spinOnlyModel);
			System.out.println(json);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}	
	}*/
	
	@Test
	public void querySPINRules(){
		SPINModuleRegistry.get().init();
		OntModel hyqueSPINModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		hyqueSPINModel.add(SystemTriples.getVocabularyModel());
		String rules = "http://134.117.108.158/tmp/hyque_spin/cardiotoxicity.spin.rdf";
		hyqueSPINModel.read(rules);
        SPINModuleRegistry.get().registerAll(hyqueSPINModel, null);

        //spin ask example
        Resource rsrc3 = hyqueSPINModel.getResource("http://bio2rdf.org/hyque-rules/cardiotoxicity#IsRAF1TargetAndIsActionInhibit");
        Function q3 = SPINFactory.asFunction(rsrc3);
        SPINARQFunction f3 = new SPINARQFunction(q3);
        Query s3 = f3.getBodyQuery();
        String out = s3.serialize();
		System.out.println(out);

        //spin function example
        Resource rsrc2 = hyqueSPINModel.getResource("http://bio2rdf.org/hyque-rules/cardiotoxicity#Is_hERG_inhibitedScore");
        Function q2 = SPINFactory.asFunction(rsrc2);
		SPINARQFunction f2 = new SPINARQFunction(q2);
		Query s2 = f2.getBodyQuery();
		//System.out.println(s2);

		//magic property example
        Resource rsrc = hyqueSPINModel.getResource("http://bio2rdf.org/hyque-rules/cardiotoxicity#retrieveDrugTargetsFromLiterature");
		Function query = SPINFactory.asFunction(rsrc);
		SPINARQFunction function = new SPINARQFunction(query);
		Query select = function.getBodyQuery();
		//System.out.println(select);
		
	}
	
	/*@Test
	public void testSPARQLQueries(){
		
		
		Model model = ModelFactory.createDefaultModel();
		// get evidence details here
		// side effects
		String sideEffectsQueryString = "SELECT DISTINCT ?sideEffect ?label "
				+ "WHERE {"
				+ "SERVICE <http://s2.semanticscience.org:12076/sparql> {"
				+ "<http://bio2rdf.org/drugbank:DB01268> <http://bio2rdf.org/drugbank_vocabulary:xref> ?pcc ."
				+ "SERVICE <http://s3.semanticscience.org:12074/sparql> {"
				+ "?compound <http://bio2rdf.org/sider_vocabulary:pubchem-stereo-compound-id> ?pcc ."
				+ "?compound <http://bio2rdf.org/sider_vocabulary:side-effect> ?sideEffect ."
				+ "?sideEffect <http://www.w3.org/2000/01/rdf-schema#label> ?label ."
				+ "} ."
				+ "} ."
				+ "}";
		
		String drugEffectsString = "SELECT DISTINCT ?action ?target ?label "
						+ "WHERE {"
						+ "SERVICE <http://s2.semanticscience.org:12076/sparql> {"
						+ "?interaction <http://bio2rdf.org/drugbank_vocabulary:drug> <http://bio2rdf.org/drugbank:DB01268> ."
						+ "?interaction <http://bio2rdf.org/drugbank_vocabulary:action> ?action ."
						+ "?interaction a <http://bio2rdf.org/drugbank_vocabulary:Drug-Target-Interaction> ."
						+ "?interaction <http://bio2rdf.org/drugbank_vocabulary:target> ?target ."
						+ "?target <http://www.w3.org/2000/01/rdf-schema#label> ?label ."
						+ "} ."
						+ "}";
		
		Query sideEffectsQuery = QueryFactory
				.create(drugEffectsString);
		QueryExecution sideEffectQExec = QueryExecutionFactory.create(
				sideEffectsQuery, model);
		ResultSet sideEffectResults = sideEffectQExec.execSelect();

		Integer sideEffectsCount = 0;

		while (sideEffectResults.hasNext()) {
			sideEffectsCount++;

			QuerySolution qs = sideEffectResults.next();
			RDFNode target = qs.get("target");
			RDFNode action = qs.get("action");
			RDFNode label = qs.get("label");
			System.out.println(target.toString()+label.toString());
		}
		
		// tunel assay results
		String tunelQueryS = "SELECT DISTINCT ?value ?assay "
		+" WHERE { "
		+" SERVICE <http://s2.semanticscience.org:12076/sparql> { "
		+" <http://bio2rdf.org/drugbank:DB01268> <http://www.w3.org/2000/01/rdf-schema#seeAlso> ?dbid ."
		+" SERVICE <http://www.ebi.ac.uk/rdf/services/chembl/sparql> { "
		+" ?activity a <http://rdf.ebi.ac.uk/terms/chembl#Activity> . "
		+" ?activity <http://rdf.ebi.ac.uk/terms/chembl#hasMolecule> ?chemblmolecule . "
		+" ?chemblmolecule <http://rdf.ebi.ac.uk/terms/chembl#moleculeXref> ?drug . "
		+" ?activity <http://rdf.ebi.ac.uk/terms/chembl#hasAssay> ?assay . "
		+" ?assay <http://purl.org/dc/terms/description> ?description . "
		+" ?activity <http://rdf.ebi.ac.uk/terms/chembl#publishedValue> ?value . "
		+" ?activity <http://www.bioassayontology.org/bao#BAO_0000208> <http://www.bioassayontology.org/bao#BAO_0001103> . "
		+" FILTER CONTAINS(?description,\"TUNEL\") . "
		+" FILTER CONTAINS(?description, \"apoptosis\") . "
		+" } . "
		+" } . "
		+" } ";
		String tunelAssayQueryString = "SELECT * "
				+ "WHERE {"
				+ "SERVICE <http://s2.semanticscience.org:12076/sparql> { "
				+ "<http://bio2rdf.org/drugbank:DB01268> <http://www.w3.org/2000/01/rdf-schema#seeAlso> ?dbid ."
				+ "SERVICE <http://www.ebi.ac.uk/rdf/services/chembl/sparql> {"
				+ "?activity a <http://rdf.ebi.ac.uk/terms/chembl#Activity> ."
				+ "?activity <http://rdf.ebi.ac.uk/terms/chembl#hasMolecule> ?chemblmolecule ."
				+ "?chemblmolecule <http://rdf.ebi.ac.uk/terms/chembl#moleculeXref> ?dbid ."
				+ "?activity <http://rdf.ebi.ac.uk/terms/chembl#hasAssay> ?assay ."
				+ "?assay <http://purl.org/dc/terms/description> ?description ."
				+ "FILTER (CONTAINS(?description, \"TUNEL\") && regex(?description, \"apoptosis\", \"i\")) ."
				+ "?activity <http://rdf.ebi.ac.uk/terms/chembl#publishedValue> ?value ."
				+ "?activity <http://www.bioassayontology.org/bao#BAO_0000208> <http://www.bioassayontology.org/bao#BAO_0001103> ."
				+ "FILTER CONTAINS(?description, \"TUNEL\") . "
		        + "FILTER CONTAINS(?description, \"apoptosis\") ."
				+ "} ." 
				+ "} ." 
				+ "}";

		Integer tunelAssayResultCount = 0;
		Query tunelAssayQuery = QueryFactory
				.create(tunelQueryS);
		QueryExecution tunelAssayQExec = QueryExecutionFactory.create(
				tunelAssayQuery, model);
		ResultSet tunelAssayResultSet = tunelAssayQExec.execSelect();

		while (tunelAssayResultSet.hasNext()) {
			QuerySolution tunelAssay = tunelAssayResultSet.next();
			tunelAssayResultCount++;
			RDFNode dbid = tunelAssay.get("description");
			RDFNode assay = tunelAssay.get("assay");
			RDFNode value = tunelAssay.get("value");

			System.out.println(dbid + " " + assay + " " + value);
			JSONObject tunelAssayResult = new JSONObject();
			tunelAssayResult.put("value", value.toString());
			tunelAssayResult.put("assayurl", assay.toString());
			tunelAssayResults.put(tunelAssayResultCount.toString(),
					tunelAssayResult);
		}// while
		
		String modelQueryS = 
				"prefix fn: <http://www.w3.org/2005/xpath-functions#> "
				+"SELECT DISTINCT ?target_uri ?target ?allele_type ?phenotype_uri "
				+"WHERE { "
				+"SERVICE <http://s2.semanticscience.org:12076/sparql> {"
				+" <http://bio2rdf.org/drugbank:DB01268> a <http://bio2rdf.org/drugbank_vocabulary:Drug> ."
				+" <http://bio2rdf.org/drugbank:DB01268> <http://bio2rdf.org/drugbank_vocabulary:target> ?target_uri ."
				+" ?target_uri <http://bio2rdf.org/drugbank_vocabulary:gene-name> ?target ."        	
				
				+" } ."
				+"BIND (fn:lower-case(?target) AS ?targetstring) ."
				+"SERVICE <http://s2.semanticscience.org:12082/sparql> { "
				+" ?association <http://bio2rdf.org/mgi_vocabulary:mouse-marker> ?mouse_marker_uri ."
				+" ?mouse_marker_uri <http://bio2rdf.org/mgi_vocabulary:marker-symbol> ?targetstring ."
				+" ?model_uri <http://bio2rdf.org/mgi_vocabulary:genetic-marker> ?mouse_marker_uri ."
				+" ?model_uri <http://bio2rdf.org/mgi_vocabulary:allele-type> ?allele_type ."
				+" ?model_uri <http://bio2rdf.org/mgi_vocabulary:phenotype> ?phenotype_uri ."
				+" } ."
				+"}";
		
		Query modelQuery = QueryFactory
				.create(modelQueryS);
		QueryExecution modelQExec = QueryExecutionFactory.create(
				modelQuery, model);
		ResultSet modelQueryResults = modelQExec.execSelect();
		while(modelQueryResults.hasNext()){
			QuerySolution r = modelQueryResults.next();
			System.out.println(r.toString());
		}
	}*/
}
