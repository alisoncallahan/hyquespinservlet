package org.semanticscience.hyquespinservlet.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticscience.hyquespinservlet.lib.Hypothesis;
import org.semanticscience.hyquespinservlet.lib.HypothesisParser;
import org.semanticscience.hyquespinservlet.server.HyQueSPINExecutor;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
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
	
	@Test
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
	}
	

}
