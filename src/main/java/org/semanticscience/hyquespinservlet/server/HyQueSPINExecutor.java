package org.semanticscience.hyquespinservlet.server;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.semanticscience.hyquespinservlet.lib.Hypothesis;
import org.semanticscience.hyquespinservlet.lib.HypothesisParser;
import org.topbraid.spin.inference.SPINInferences;
import org.topbraid.spin.system.SPINModuleRegistry;
import org.topbraid.spin.util.SystemTriples;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class HyQueSPINExecutor extends HttpServlet {

	static String inputParameter = "inputHypothesis";
	static String inputFileParameter = "inputHypothesisFile";
	static String inputFormatParameter = "inputFormat";
	static String outputFormatParameter = "outputFormat";
	static String jsTreeParameter = "jstree";
	static String tkiParameter = "tki";
	static String rulesParameter = "spinFile";
	static String rulesFormatParameter = "spinFileFormat";
	static String evaluationOnlyParameter = "evaluationOnly";
	
	public enum Format {
		JSON, RDFXML
	}

	@Override
	public void init() {
		// Initialize system functions and templates
		SPINModuleRegistry.get().init();
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException {

		PrintWriter out = null;
		String input = null;
		String inputFormat = null;
		String[] ruleFiles = null;
		String ruleFormat = null;
		Model evaluationModel = null;
		
		// create OntModel
		OntModel hyqueSPINModel = null; 

		OntModel spinOnlyModel = null;

		boolean tki = false;
		
		boolean jstree = false;
		
		try {
			out = resp.getWriter();

			Format format = Format.RDFXML;
			resp.setContentType("text/xml");

			//String rules = req.getParameter(rulesParameter);
			if(req.getParameterValues(rulesParameter) != null){
				ruleFiles = req.getParameterValues(rulesParameter);
			} else {
				out.print("Provide the URL  of a file containing the SPIN rules RDF using the spinFile parameter");
			}
			
			if(req.getParameter(rulesFormatParameter) != null){
				if(req.getParameter(rulesFormatParameter).equals("rdfxml")){
					ruleFormat = "RDF/XML";
				} else if(req.getParameter(rulesFormatParameter).equals("n3")){
					ruleFormat = "N3";
				} else if(req.getParameter(rulesFormatParameter).equals("ntriple")){
					ruleFormat = "N-TRIPLE";
				} else if(req.getParameter(rulesFormatParameter).equals("ttl")){
					ruleFormat = "TTL";
				}
			} else {
				out.print("Specify the format of the input SPIN RDF using the spinFileFormat parameter - rdfxml, n3, ntriple, or ttl");
			}
			
			if(req.getParameter(inputFormatParameter) != null){
				if(req.getParameter(inputFormatParameter).equals("rdfxml")){
					inputFormat = "RDF/XML";
				} else if(req.getParameter(inputFormatParameter).equals("n3")){
					inputFormat = "N3";
				} else if(req.getParameter(inputFormatParameter).equals("ntriple")){
					inputFormat = "N-TRIPLE";
				} else if(req.getParameter(inputFormatParameter).equals("ttl")){
					inputFormat = "TTL";
				} else if(req.getParameter(inputFormatParameter).equals("json")){
					inputFormat = "JSON";
				}
			} else {
				out.print("Specify the format of the input hypothesis RDF using the inputFormat parameter - rdfxml, n3, ntriple, or ttl");
			}
			hyqueSPINModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
			spinOnlyModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
			
			// add system triples
			hyqueSPINModel.add(SystemTriples.getVocabularyModel());

			
			// read SPIN rules into hyqueSPINModel and spinOnlyModel
			if (req.getParameter(outputFormatParameter) != null
					&& req.getParameter(outputFormatParameter).toLowerCase()
							.equals("json")) {
				format = Format.JSON;
				resp.setContentType("application/json");
			}
			for (int i = 0; i < ruleFiles.length; i++) {
				String rules = ruleFiles[i];
				hyqueSPINModel.read(rules, null, ruleFormat);
				spinOnlyModel.read(rules, null, ruleFormat);
			}
			
			if (req.getParameter(inputFileParameter) != null
					&& req.getParameter(inputParameter) != null) {
				out.print("Provide EITHER the URL of a file containing the hypothesis RDF using the inputHypothesisFile parameter OR a string of the hypothesis RDF using the inputHypothesis parameter");
			} else if (req.getParameter(inputFileParameter) != null && req.getParameter(inputParameter) == null) {
				String fileURL = req.getParameter(inputFileParameter);
				URL inputURL = new URL(fileURL);
				URLConnection connection = inputURL.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(
						connection.getInputStream()));

				StringBuilder response = new StringBuilder();
				String inputLine;

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();

				input = response.toString();
			} else if (req.getParameter(inputParameter) != null && req.getParameter(inputFileParameter) == null) {
				input = req.getParameter(inputParameter);
			}

			if (req.getParameter(outputFormatParameter) != null
					&& req.getParameter(outputFormatParameter).toLowerCase()
							.equals("json")) {
				format = Format.JSON;
				resp.setContentType("application/json");
			}

			if (req.getParameter(jsTreeParameter) != null
					&& req.getParameter(jsTreeParameter).toLowerCase()
							.equals("true")) {
				jstree = true;
			}
			
			if (req.getParameter(tkiParameter) != null && req.getParameter(tkiParameter).toLowerCase().equals("true")){
				tki = true;
			}

			Model inputModel = null;	
			if(inputFormat == "JSON" && tki == true ){
				Hypothesis hyp = new Hypothesis(input);
				inputModel = hyp.makeTKIHypothesisModel();
			} else if (inputFormat == "JSON" && tki == false) {
				Hypothesis hyp = new Hypothesis(input);
				inputModel = hyp.makeHypothesisModel();
			} else {
				inputModel = ModelFactory.createDefaultModel();
				inputModel.read(input, null, inputFormat);
			} 
			// add input model to SPIN model
			hyqueSPINModel.addSubModel(inputModel);

			// create output model to contain inferred statements
			 evaluationModel = ModelFactory.createDefaultModel();

			// add output model to SPIN model
			hyqueSPINModel.addSubModel(evaluationModel);

			// register locally defined functions
			SPINModuleRegistry.get().registerAll(hyqueSPINModel, null);

			// run SPIN inferences
			SPINInferences.run(hyqueSPINModel, evaluationModel, null, null,
					false, null);

			// add input model and SPIN rules to evaluation model, so all data
			// (provided by user and generated by HyQue) is sent back to user
			if(req.getParameter(evaluationOnlyParameter) != null && req.getParameter(evaluationOnlyParameter) == "false" ){
				evaluationModel.add(inputModel);
				evaluationModel.add(hyqueSPINModel);
			}
			
			makeOutput(out, format, evaluationModel, spinOnlyModel, jstree, tki);

			// remove input and evaluation models from hyqueSPINModel
			hyqueSPINModel.removeSubModel(inputModel);
			hyqueSPINModel.removeSubModel(evaluationModel);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				hyqueSPINModel = null;
				spinOnlyModel = null;
				evaluationModel = null;
				out.close();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}
	}

	void makeOutput(PrintWriter out, Format format, Model evaluation, OntModel spinOnlyModel,
			boolean jstree, boolean tki) {
		if (format == Format.JSON && jstree == true && tki == false) {
			out.println(makeJsTreeOutput(evaluation, spinOnlyModel));
		} else if (format == Format.JSON && jstree == false && tki == false) {
			out.println(makeJSONOutput(evaluation, spinOnlyModel));
		} else if (format == Format.JSON  && jstree == false && tki == true){
			out.println(makeTKIOutput(evaluation, spinOnlyModel));
		}else {
			out.println(makeRDFOutput(out, evaluation));
		}
	}

	String makeJSONOutput(Model model, OntModel spinOnlyModel) {
		String output = HypothesisParser.getJSONString(model, spinOnlyModel);
		return output;
	}

	String makeJsTreeOutput(Model model, OntModel spinOnlyModel ) {
		String output = HypothesisParser.getJstreeJSON(model, spinOnlyModel);
		return output;
	}
	
	String makeTKIOutput(Model model, OntModel spinOnlyModel){
		String output = HypothesisParser.getTKIJSON(model, spinOnlyModel);
		return output;
	}

	String makeRDFOutput(PrintWriter out, Model model) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		model.write(output, "RDF/XML");
		String results = output.toString();
		try {
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return results;
	}
}
