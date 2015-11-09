
package normnet.transform;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import javax.naming.OperationNotSupportedException;
import javax.swing.text.Utilities;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import ingenias.editor.Log;
import ingenias.editor.entities.*;
import ingenias.exception.CannotLoad;
import ingenias.exception.DamagedFormat;
import ingenias.exception.NotFound;
import ingenias.exception.NotInitialised;
import ingenias.exception.NullEntity;
import ingenias.exception.UnknowFormat;
import ingenias.generator.browser.Browser;
import ingenias.generator.browser.BrowserImp;
import ingenias.generator.browser.GraphEntity;
import ingenias.generator.browser.GraphRelationship;
import ingenias.generator.browser.GraphRole;

import org.cpntools.accesscpn.model.Arc;
import org.cpntools.accesscpn.model.HLAnnotation;
import org.cpntools.accesscpn.model.HLDeclaration;
import org.cpntools.accesscpn.model.Instance;
import org.cpntools.accesscpn.model.ModelFactory;
import org.cpntools.accesscpn.model.Name;
import org.cpntools.accesscpn.model.Page;
import org.cpntools.accesscpn.model.PetriNet;
import org.cpntools.accesscpn.model.Place;
import org.cpntools.accesscpn.model.Sort;
import org.cpntools.accesscpn.model.Transition;
import org.cpntools.accesscpn.model.cpntypes.CPNEnum;
import org.cpntools.accesscpn.model.cpntypes.CPNList;
import org.cpntools.accesscpn.model.cpntypes.CPNUnion;
import org.cpntools.accesscpn.model.cpntypes.CpntypesFactory;
import org.cpntools.accesscpn.model.cpntypes.impl.CpntypesFactoryImpl;
import org.cpntools.accesscpn.model.declaration.DeclarationFactory;
import org.cpntools.accesscpn.model.declaration.TypeDeclaration;
import org.cpntools.accesscpn.model.declaration.VariableDeclaration;
import org.cpntools.accesscpn.model.declaration.impl.DeclarationFactoryImpl;
import org.cpntools.accesscpn.model.exporter.DOMGenerator;
import org.cpntools.accesscpn.model.impl.ModelFactoryImpl;

/** 
 * Copyright (C) 2015  Huib Aldeberew, Jie Jiang
 * 
 * Modified by Jorge J. Gómez Sanz
 * 
 * This file is part of the NormNets tool. NormNets is an open source norm editor
 * which produces norm definitions and converts them to CPN 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation version 3 of the License
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 **/
public class NormnetsToCPNTransformation {

	/*
	 * Create a ModelFactory object which can produce places, transitions, arcs
	 * from CPN
	 */
	private ModelFactory modelfactory = new ModelFactoryImpl();

	/*
	 * Create a CpntypesFactory object which can produce data types from CPN
	 */
	private CpntypesFactory typefactory = new CpntypesFactoryImpl();

	/*
	 * Create a DeclarationFactory object which can produce declarations from
	 * CPN
	 */
	private DeclarationFactory declarfactory = new DeclarationFactoryImpl();

	// List to store the color declarations
	private ArrayList<String> colorDeclarations = new ArrayList<String>();

	
	public static void main(String args[]) throws UnknowFormat, DamagedFormat, CannotLoad, NotInitialised, IOException, NullEntity, NotFound {

		Log.initInstance(new PrintWriter(System.out), new PrintWriter(System.err));
		if (args.length==0){
			System.err.println("Wrong number of parameters. The program is expecting a path to a normsnet specification file");
			System.err.println("Arguments should be:");
			System.err.println(">NormnetsToCPNTransformation A_NORMNETS_SPECIFICATION_FILEPATH");
			System.err.println("Which prints the resulting CPN to the output and:");
			System.err.println(">NormnetsToCPNTransformation A_NORMNETS_SPECIFICATION_FILEPATH  [AN_OUTPUTH_PATH_FOR_THE_PRODUCED_CPN]");
			System.err.println("Which saves the resulting CPN to the declared output file");
			return;
		}

		NormnetsToCPNTransformation nn=new NormnetsToCPNTransformation();

		BrowserImp bimp=(BrowserImp) BrowserImp.initialise(args[0]);

		PetriNet cpn;

		GraphEntity[] normnets = Utils.generateEntitiesOfType(RegulativeNormNet.class, bimp);

		cpn = nn.transform(normnets[0]);

		// transform to CPN
		if (cpn == null) {
			System.err.println("Error creating CPN: The NormNet appears to be wrong, please validate before running the transformation!");
			return;
		}

		// write cpn model at selected location:
		try {


			if (args.length==2){
				String outputfile=args[1];
				DOMGenerator.export(cpn, new FileOutputStream(outputfile));
				String textFile = outputfile.replace(".cpn",		
								".txt");// FIXME volatile! only works if user
										// selects/uses the default .cpn
										// extension!
						

				FileWriter writer = new FileWriter(textFile);
				for (String output : nn.colorDeclarations)
					writer.write(output + "\n");// Check if we need the
				// newline!
				writer.close();
			} else {
				DOMGenerator.export(cpn, System.out);
			}

		} catch (Exception e) {
			System.err.println("Error creating CPN: An error has occurred generating the CPN model. Please check the console output for details.");	
			e.printStackTrace();
		};

	}

	public PetriNet transform(GraphEntity normNet) throws IOException, NullEntity, NotFound {

		// simple validation:
		if (Utils.getRelatedElements(normNet, ingenias.editor.entities.Root.class, ingenias.editor.entities.RoottargetRole.class).length == 0)
			return null;


		// create an ArrayList for storing all the CPN patterns
		ArrayList<Pattern> patterns = new ArrayList<Pattern>();

		// Create a page for storing CPN elements
		Page page = modelfactory.createPage();

		// Assign a name and an id to the page
		page.setName(buildName(Utils.getRelatedElements(normNet, Root.class, RoottargetRole.class)[0].getID()));// nice to have:
		// page name = NN
		// name
		page.setId("ID" + Utils.getRelatedElements(normNet, Root.class, RoottargetRole.class)[0].getID());

		// create Petri Net and attach the page to it
		PetriNet petrinet = modelfactory.createPetriNet();
		page.setPetriNet(petrinet);

		// create a general complied and violated color
		createCompliedViolated(normNet, page);

		// create the pattern for the top-level NN
		Pattern result = createNormNet(Utils.getRelatedElements(normNet, Root.class, RoottargetRole.class)[0], page);
		if (result != null)
			patterns.add(result);

		// return the result so we can write it to file. Changed.
		return petrinet;
	}


	/**
	 * @throws NullEntity 
	 * @throws NotFound ********************************************************************************************/

	private Pattern createFormulaCPN(String normID, GraphEntity formula, Page page) throws NullEntity, NotFound {
		assert formula.getEntity().getClass().equals(Formula.class);

		// call different functions according to the type of the formula
		if (formula.getEntity().getClass().equals(RAPAnd.class))
			return createFormulaAndCPN(normID, formula, page);
		
		if (formula.getEntity().getClass().equals(RAPOr.class))
			return createFormulaOrCPN(normID, formula, page);
		if (formula.getEntity().getClass().equals(RAPBefore.class))
			return createFormulaBeforeCPN(normID, formula, page);
		if (formula.getEntity().getClass().equals(RAP.class))
			return createRapCPN(normID, formula, page);
		return null;
	}

	private Pattern createFormulaAndCPN(String normID, GraphEntity andrap,
			Page page) throws NullEntity, NotFound {
		assert andrap.getEntity().getClass().equals(RAPAnd.class);
		Pattern rapandpt = new Pattern();
		
		GraphEntity left = Utils.getRelatedElements(andrap, LeftFormula.class, LeftFormulatargetRole.class)[0];
		GraphEntity right = Utils.getRelatedElements(andrap, RightFormula.class,RightFormulatargetRole.class)[0];

		Pattern leftPattern = createFormulaCPN(normID, left, page);
		Pattern rightPattern = createFormulaCPN(normID, right, page);

		Transition combination = createTransition(
				"auto" + normID + andrap.getID(), page);

		// connect to output places of left and right
		Arc fromLeft = createArc(leftPattern.getLastPlaces().get(0),
				combination, page);
		// we assume RAPS will only have 1element in the list.

		Arc fromRight = createArc(rightPattern.getLastPlaces().get(0),
				combination, page);

		// create output place
		Place output;

		// first check whether the union color already exist,
		// if not create a color based on the roles of the left and right
		// formulas
		int i = 0;
		for (; i < Colors.getcolors().size(); i++) {
			if (("U" + leftPattern.getLastPlaces().get(0).getSort().getText()
					+ rightPattern.getLastPlaces().get(0).getSort().getText())
					.contentEquals(Colors.getcolors().get(i).getText())||
					("U" + rightPattern.getLastPlaces().get(0).getSort().getText()
							+ leftPattern.getLastPlaces().get(0).getSort().getText())
					.contentEquals(Colors.getcolors().get(i).getText()))
				break;
		}
		System.out.println("existing or not:" + i);
		if (i == Colors.getcolors().size()) {
			// create a new color based on the role of the rap
			Sort color = createColorUnion(leftPattern.getLastPlaces().get(0)
					.getSort(), rightPattern.getLastPlaces().get(0).getSort(),
					page);
			Sort listcolor = createColorList(color, page);
			// create one place with the color
			output = createPlace("o" + normID + andrap.getID(), listcolor,
					page);
		}

		else {
			// create two places with the existing color
			System.out.println("In AND, if exsit use the current color:" + Colors.getcolorbyName("L" + Colors.getcolors().get(i).getText()).getText());
			output = createPlace("o" + normID + andrap.getID(),
					Colors.getcolorbyName("L" + Colors.getcolors().get(i).getText()),
					page);
		}

		// Create arc to output place
		Arc toOutput = createArc(combination, output, page);

		// set arc variables (no need to create new variables but reuse the
		// variables from preceding arcs)
		fromLeft.setHlinscription(leftPattern.getLastPlaces().get(0)
				.getTargetArc().get(0).getHlinscription());
		fromRight.setHlinscription(rightPattern.getLastPlaces().get(0)
				.getTargetArc().get(0).getHlinscription());
		setArcInscriptionUnion(toOutput);

		// Pattern
		rapandpt.addFirstPlaces(leftPattern.getFirstPlaces());
		rapandpt.addFirstPlaces(rightPattern.getFirstPlaces());
		rapandpt.addLastPlaces(output);
		rapandpt.addFirstTransitions(leftPattern.getFirstTransitions());
		rapandpt.addFirstTransitions(rightPattern.getFirstTransitions());
		rapandpt.addLastTransitions(combination);

		return rapandpt;
	}

	private Pattern createFormulaOrCPN(String normID, GraphEntity orrap, Page page) throws NullEntity, NotFound {
		assert orrap.getEntity().getClass().equals(RAPOr.class);
		
		
		GraphEntity left = Utils.getRelatedElements(orrap, LeftFormula.class, LeftFormulatargetRole.class)[0];
		GraphEntity right = Utils.getRelatedElements(orrap, RightFormula.class,RightFormulatargetRole.class)[0];
		

		Pattern raporpt = new Pattern();
		
		Pattern leftPattern = createFormulaCPN(normID, left, page);
		Pattern rightPattern = createFormulaCPN(normID, right, page);

		// create elements and complete left and right part of "Or"
		Transition forLeftT = createTransition(
				"auto" + normID + orrap.getID() + "l", page);
		Transition forRightT = createTransition(
				"auto" + normID + orrap.getID() + "r", page);
		Arc forLeftA = createArc(leftPattern.getLastPlaces().get(0), forLeftT,
				page);
		Arc forRightA = createArc(rightPattern.getLastPlaces().get(0),
				forRightT, page);

		// Create combination place and output place
		Place combination;
		Place output;
		VariableDeclaration var;

		// first check whether the union color already exist,
		// if not create a color based on the roles of the left and right
		// formulas
		int i = 0;
		for (; i < Colors.getcolors().size(); i++) {
			if (("U" + leftPattern.getLastPlaces().get(0).getSort().getText()
					+ rightPattern.getLastPlaces().get(0).getSort().getText())
					.contentEquals(Colors.getcolors().get(i).getText())
					||("U" + rightPattern.getLastPlaces().get(0).getSort().getText()
							+ leftPattern.getLastPlaces().get(0).getSort().getText())
					.contentEquals(Colors.getcolors().get(i).getText()))
				break;
		}

		if (i == Colors.getcolors().size()) {
			// create a new color based on the role of the rap
			Sort color = createColorUnion(leftPattern.getLastPlaces().get(0)
					.getSort(), rightPattern.getLastPlaces().get(0).getSort(),
					page);
			Sort listcolor = createColorList(color, page);
			// create one place with the color
			combination = createPlace("m" + normID + orrap.getID(),
					listcolor, page);
			output = createPlace("o" + normID + orrap.getID(), listcolor,
					page);
			var = createArcVariable(normID+orrap.getID(), listcolor, page);
		}

		else {
			// create two places with the existing color
			System.out.println("In OR, if exsit use the current color:" + Colors.getcolorbyName("L" + Colors.getcolors().get(i).getText()).getText());
			combination = createPlace("m" + normID + orrap.getID(), 
					Colors.getcolorbyName("L" + Colors.getcolors().get(i).getText()), page);
			output = createPlace("o" + normID + orrap.getID(),
					combination.getSort(), page);
			var = createArcVariable(normID+orrap.getID(), 
					combination.getSort(), page);
		}

		// Create combination Transition
		Transition combinationT = createTransition(
				"auto" + normID + orrap.getID(), page);

		// connect to output places of left and right
		Arc fromLeft = createArc(forLeftT, combination, page);
		Arc fromRight = createArc(forRightT, combination, page);
		Arc output1 = createArc(combination, combinationT, page);
		Arc output2 = createArc(combinationT, output, page);

		// set arc variables (no need to create new variables but reuse the
		// variables from preceding arcs)
		forLeftA.setHlinscription(leftPattern.getLastPlaces().get(0)
				.getTargetArc().get(0).getHlinscription());
		forRightA.setHlinscription(rightPattern.getLastPlaces().get(0)
				.getTargetArc().get(0).getHlinscription());
		setArcInscriptionUnion(fromLeft);
		setArcInscriptionUnion(fromRight);
		setArcInscription(output1, var);
		setArcInscription(output2, var);

		// complete the structure of the new pattern
		raporpt.addFirstPlaces(leftPattern.getFirstPlaces());
		raporpt.addFirstPlaces(rightPattern.getFirstPlaces());
		raporpt.addLastPlaces(output);
		raporpt.addFirstTransitions(leftPattern.getFirstTransitions());
		raporpt.addFirstTransitions(rightPattern.getFirstTransitions());
		raporpt.addLastTransitions(combinationT);
		return raporpt;
	}

	private Pattern createFormulaBeforeCPN(String normID, GraphEntity beforerap,
			Page page) throws NullEntity, NotFound {
		assert beforerap.getEntity().getClass().equals(RAPBefore.class);
		
		
		GraphEntity left = Utils.getRelatedElements(beforerap, LeftFormula.class, LeftFormulatargetRole.class)[0];
		GraphEntity right = Utils.getRelatedElements(beforerap, RightFormula.class,RightFormulatargetRole.class)[0];
		
		Pattern rapbeforept = new Pattern();


		Pattern leftPattern = createFormulaCPN(normID, left, page);
		Pattern rightPattern = createFormulaCPN(normID, right, page);

		// connect the last place of the left pattern to all the first
		// transitions of the right pattern
		ArrayList<Arc> before = new ArrayList<Arc>();
		for (int i = 0; i < rightPattern.getFirstTransitions().size(); i++) {
			before.add(createArc(leftPattern.getLastPlaces().get(0),
					rightPattern.getFirstTransitions().get(i), page));
			before.get(i).setHlinscription(
					leftPattern.getLastPlaces().get(0).getTargetArc().get(0)
					.getHlinscription());
		}

		// complete the structure of the new pattern
		rapbeforept.addFirstPlaces(leftPattern.getFirstPlaces());
		rapbeforept.addLastPlaces(rightPattern.getLastPlaces());
		rapbeforept.addFirstTransitions(leftPattern.getFirstTransitions());
		rapbeforept.addLastTransitions(rightPattern.getLastTransitions());
		return rapbeforept;
	}

	private Pattern createRapCPN(String normID, GraphEntity rap, Page page) throws NullEntity, NotFound {
	assert rap.getEntity().getClass().equals(RAP.class);
		
		

		Pattern rappt = new Pattern();
		
		GraphEntity role=rap.getAttributeByName("Role").getEntityValue();
		GraphEntity action=rap.getAttributeByName("Action").getEntityValue();
		
		// create two places
		Place inputplace = createPlace("i" + normID + rap.getID(), Colors
				.getcolorbyName(role.getID()), page);
		Place outputplace = createPlace("o" + normID + rap.getID(),Colors
				.getcolorbyName(role.getID()), page);

		// create one transition based on the name of the action in the rap
		Transition transititon = createTransition(action.getID()
				+ normID +rap.getID(), page);

		// create two arcs and an arc inscription
		Arc PTarc = createArc(inputplace, transititon, page);
		Arc TParc = createArc(transititon, outputplace, page);
		VariableDeclaration var = createArcVariable(normID + rap.getID(), 
				inputplace.getSort(), page);

		// set arc inscriptions based on the created variable
		setArcInscription(PTarc, var);
		setArcInscription(TParc, var);

		// add the featured places and transitions to the CPN Pattern
		rappt.addFirstPlaces(inputplace);
		rappt.addLastPlaces(outputplace);
		rappt.addFirstTransitions(transititon);
		rappt.addLastTransitions(transititon);

		return rappt;
	}

	private Pattern createNormCPN(GraphEntity cnorm, Page page) throws NotFound, NullEntity {
		assert cnorm.getEntity().getClass().equals(Norm.class);
		// create three formulas for precondition, deadline and target
		Pattern normPt = new Pattern();
		
		GraphEntity targ = Utils.getRelatedElements(cnorm, Directive.class,DirectivetargetRole.class)[0];
		Pattern target = createFormulaCPN("T" + cnorm.getID(), targ, page);

		GraphEntity deadl = Utils.getRelatedElements(cnorm, Deadline.class,DeadlinetargetRole.class)[0];
		Pattern deadline = createFormulaCPN("D" + cnorm.getID(), deadl, page);

		if(Utils.getRelatedElements(cnorm, Precondition.class,PreconditiontargetRole.class).length>0) {
			GraphEntity prec = Utils.getRelatedElements(cnorm, Precondition.class,PreconditiontargetRole.class)[0];
			Pattern precondition = createFormulaCPN("P" + cnorm.getID(), prec, page);
			Arc toTarget = createArc(precondition.getLastPlaces().get(0), target
					.getFirstTransitions().get(0), page);
			toTarget.setHlinscription(precondition.getLastPlaces().get(0)
					.getTargetArc().get(0).getHlinscription());
			Arc toDeadline = createArc(precondition.getLastPlaces().get(0),
					deadline.getLastTransitions().get(0), page);
			toDeadline.setHlinscription(precondition.getLastPlaces().get(0)
					.getTargetArc().get(0).getHlinscription());
			normPt.addFirstTransitions(precondition.getFirstTransitions());
			normPt.addFirstPlaces(precondition.getFirstPlaces());
		}


		// connect them to form a general Norm and add arc inscriptions
		Arc tToDeadline = createArc(target.getFirstPlaces().get(0), deadline
				.getLastTransitions().get(0), page);
		tToDeadline.setHlinscription(target.getFirstPlaces().get(0)
				.getSourceArc().get(0).getHlinscription());

		// Treat Deontic type
		
		String deontictype=cnorm.getAttributeByName("DeonticType").getSimpleValue();
		
		if (deontictype.equalsIgnoreCase("Obliged")) {
			target.getLastPlaces().get(0).setName(buildName("C"+cnorm.getID()));
			deadline.getLastPlaces().get(0).setName(buildName("V"+cnorm.getID()));
			// set arc inscription and color for Complied place and Violated place
			Place complied = target.getLastPlaces().get(0);
			String targetarc = complied.getTargetArc().get(0).getHlinscription().getText();
			complied.getTargetArc().get(0).getHlinscription().
			setText("[C"+complied.getSort().getText()
					+"("+targetarc+")]");
			Place violated = deadline.getLastPlaces().get(0);
			violated.getTargetArc().get(0).getHlinscription().
			setText("[V"+complied.getSort().getText()
					+"("+targetarc+")]");
			complied.setSort(Colors.getcolorbyName("Complied"));
			violated.setSort(Colors.getcolorbyName("Violated"));

			// give place C the Norm.lastplaces.arrary(0),place V the
			// Norm.lastplaces.array(1)
			// I am not sure whether now Lastplaces is a array with C in the
			// first and V in the second!!!!check.
			normPt.addLastPlaces(target.getLastPlaces().get(0)); // C is the
			// first
			// element
			// in the
			// array
			normPt.addLastPlaces(deadline.getLastPlaces().get(0));// V is the
			// second
			// element
			// in the
			// array
			// normPt.addLastTransitions(target.getLastTransitions().get(0));
			// normPt.addLastTransitions(deadline.getLastTransitions().get(0));
		} else if (deontictype.equalsIgnoreCase("Forbidden")) {
			target.getLastPlaces()
			.get(0)
			.setName(
					buildName(target.getLastPlaces().get(0).getName()
							.getText()
							+ "V"));
			deadline.getLastPlaces()
			.get(0)
			.setName(
					buildName(deadline.getLastPlaces().get(0).getName()
							.getText()
							+ "C"));
			// set arc inscription and color for Complied place and Violated place
			Place violated = target.getLastPlaces().get(0);
			String targetarc = violated.getTargetArc().get(0).getHlinscription().getText();
			violated.getTargetArc().get(0).getHlinscription().
			setText("[V"+violated.getSort().getText()
					+"("+targetarc+")]");
			Place complied = deadline.getLastPlaces().get(0);
			complied.getTargetArc().get(0).getHlinscription().
			setText("[C"+violated.getSort().getText()
					+"("+targetarc+")]");
			complied.setSort(Colors.getcolorbyName("Complied"));
			violated.setSort(Colors.getcolorbyName("Violated"));

			normPt.addLastPlaces(deadline.getLastPlaces().get(0)); // C is the
			// first
			// element
			// in the
			// array
			normPt.addLastPlaces(target.getLastPlaces().get(0));// V is the
			// second
			// element in
			// the array
			// normPt.addLastTransitions(deadline.getLastTransitions().get(0));
			// normPt.addLastTransitions(target.getLastTransitions().get(0));
		} else
			return null;


		normPt.addFirstTransitions(target.getFirstTransitions());
		normPt.addFirstTransitions(deadline.getFirstTransitions());
		normPt.addLastTransitions(target.getLastTransitions());
		normPt.addLastTransitions(deadline.getLastTransitions());
		normPt.addFirstPlaces(target.getFirstPlaces());
		normPt.addFirstPlaces(deadline.getFirstPlaces());

		return normPt;
	}

	private Pattern createNormNet(GraphEntity normNet, Page page) throws NullEntity, NotFound {
		assert normNet.getEntity().getClass().equals(NormNet.class);

		// like formulas:
		if (normNet.getEntity().getClass().equals(NormAnd.class))
			return createANDCPN(normNet, page);
		if (normNet.getEntity().getClass().equals(NormOr.class))
			return createORCPN(normNet, page);
		if (normNet.getEntity().getClass().equals(NormOE.class))	
			return createOECPN(normNet, page);
		if (normNet.getEntity().getClass().equals(Norm.class))			
			return createNormCPN(normNet, page);
		return null;
	}

	private Pattern createANDCPN(GraphEntity normAND, Page page) throws NullEntity, NotFound {
		assert normAND.getEntity().getClass().equals(NormAnd.class);

		Pattern normANDpt = new Pattern();

		GraphEntity left = Utils.getRelatedElements(normAND, LeftNormNet.class, LeftNormNettargetRole.class)[0];
		GraphEntity right = Utils.getRelatedElements(normAND, RightNormNet.class, RightNormNettargetRole.class)[0];

		Pattern leftPattern = createNormNet(left, page);
		Pattern rightPattern = createNormNet(right, page);
		// create three transitions
		Transition combination1 = createTransition("autoN" + normAND.getID()
		+ "1", page);
		Transition combination2 = createTransition("autoN" + normAND.getID()
		+ "2", page);
		Transition combination3 = createTransition("autoN" + normAND.getID()
		+ "3", page);
		// add the connections for AND between Norms
		Arc fromC1 = createArc(leftPattern.getLastPlaces().get(0),
				combination1, page);
		Arc fromV1 = createArc(leftPattern.getLastPlaces().get(1),
				combination2, page);
		Arc fromC2 = createArc(rightPattern.getLastPlaces().get(0),
				combination1, page);
		Arc fromV2 = createArc(rightPattern.getLastPlaces().get(1),
				combination3, page);

		// Create outputC place
		Place outputC = createPlace("C" + normAND.getID(),
				Colors.getcolorbyName("Complied"), page);
		// Create outputV place
		Place outputV = createPlace("V" + normAND.getID(), 
				Colors.getcolorbyName("Violated"), page);

		// connect to the output places
		Arc a1 = createArc(combination1, outputC, page);
		Arc a2 = createArc(combination2, outputV, page);
		Arc a3 = createArc(combination3, outputV, page);

		// create new variables with the color of Complied and Violated
		VariableDeclaration leftC = createArcVariable("varLC"+normAND.getID(), 
				Colors.getcolorbyName("Complied"), page);
		VariableDeclaration rightC = createArcVariable("varRC"+normAND.getID(), 
				Colors.getcolorbyName("Complied"), page);
		VariableDeclaration leftV = createArcVariable("varLV"+normAND.getID(), 
				Colors.getcolorbyName("Violated"), page);
		VariableDeclaration rightV = createArcVariable("varRV"+normAND.getID(), 
				Colors.getcolorbyName("Violated"), page);
		setArcInscription(fromC1, leftC);
		setArcInscription(fromC2, rightC);
		setArcInscription(fromV1, leftV);
		setArcInscription(fromV2, rightV);
		a2.setHlinscription(fromV1.getHlinscription());
		a3.setHlinscription(fromV2.getHlinscription());
		setArcInscriptionCompliedViolated(a1, leftC, rightC);

		// Complete the pattern of AND
		normANDpt.addFirstPlaces(leftPattern.getFirstPlaces());
		normANDpt.addFirstPlaces(rightPattern.getFirstPlaces());
		normANDpt.addLastPlaces(outputC);
		normANDpt.addLastPlaces(outputV);
		normANDpt.addFirstTransitions(leftPattern.getFirstTransitions());
		normANDpt.addFirstTransitions(rightPattern.getFirstTransitions());
		normANDpt.addLastTransitions(combination1);
		normANDpt.addLastTransitions(combination2);
		normANDpt.addLastTransitions(combination3);
		return normANDpt;
	}

	private Pattern createORCPN(GraphEntity normOR, Page page) throws NullEntity, NotFound {
		assert normOR.getEntity().getClass().equals(NormOr.class);

		Pattern normORpt = new Pattern();

		GraphEntity left = Utils.getRelatedElements(normOR, LeftNormNet.class, LeftNormNettargetRole.class)[0];
		GraphEntity right = Utils.getRelatedElements(normOR, RightNormNet.class, RightNormNettargetRole.class)[0];

		Pattern leftPattern = createNormNet(left, page);
		Pattern rightPattern = createNormNet(right, page);

		// create three transitions
		Transition combination1 = createTransition("autoN" + normOR.getID()
		+ "1", page);
		Transition combination2 = createTransition("autoN" + normOR.getID()
		+ "2", page);
		Transition combination3 = createTransition("autoN" + normOR.getID()
		+ "3", page);
		// add the connections for OR between Norms
		Arc fromC1 = createArc(leftPattern.getLastPlaces().get(0),
				combination1, page);
		Arc fromV1 = createArc(leftPattern.getLastPlaces().get(1),
				combination3, page);
		Arc fromC2 = createArc(rightPattern.getLastPlaces().get(0),
				combination1, page);
		Arc fromV2 = createArc(rightPattern.getLastPlaces().get(1),
				combination3, page);

		// Create outputC place
		Place outputC = createPlace("C" + normOR.getID(),
				Colors.getcolorbyName("Complied"), page);
		// Create outputV place
		Place outputV = createPlace("V" + normOR.getID(), 
				Colors.getcolorbyName("Violated"), page);

		// connect to the output places
		Arc a1 = createArc(combination1, outputC, page);
		Arc a2 = createArc(combination2, outputC, page);
		Arc a3 = createArc(combination3, outputV, page);

		// create new variables with the color of Complied and Violated
		VariableDeclaration leftC = createArcVariable("varLC"+normOR.getID(), 
				Colors.getcolorbyName("Complied"), page);
		VariableDeclaration rightC = createArcVariable("varRC"+normOR.getID(), 
				Colors.getcolorbyName("Complied"), page);
		VariableDeclaration leftV = createArcVariable("varLV"+normOR.getID(), 
				Colors.getcolorbyName("Violated"), page);
		VariableDeclaration rightV = createArcVariable("varRV"+normOR.getID(), 
				Colors.getcolorbyName("Violated"), page);
		setArcInscription(fromC1, leftC);
		setArcInscription(fromC2, rightC);
		setArcInscription(fromV1, leftV);
		setArcInscription(fromV2, rightV);
		a1.setHlinscription(fromC1.getHlinscription());
		a2.setHlinscription(fromC2.getHlinscription());
		setArcInscriptionCompliedViolated(a3, leftV, rightV);

		// complete the pattern of OR
		normORpt.addFirstPlaces(leftPattern.getFirstPlaces());
		normORpt.addFirstPlaces(rightPattern.getFirstPlaces());
		normORpt.addLastPlaces(outputC);
		normORpt.addLastPlaces(outputV);
		normORpt.addFirstTransitions(leftPattern.getFirstTransitions());
		normORpt.addFirstTransitions(rightPattern.getFirstTransitions());
		normORpt.addLastTransitions(combination1);
		normORpt.addLastTransitions(combination2);
		normORpt.addLastTransitions(combination3);
		return normORpt;
	}

	private Pattern createOECPN(GraphEntity normOE, Page page) throws NullEntity, NotFound {
		assert normOE.getEntity().getClass().equals(NormOr.class);

		Pattern normOEpt = new Pattern();

		GraphEntity left = Utils.getRelatedElements(normOE, LeftNormNet.class, LeftNormNettargetRole.class)[0];
		GraphEntity right = Utils.getRelatedElements(normOE, RightNormNet.class, RightNormNettargetRole.class)[0];

		Pattern leftPattern = createNormNet(left, page);
		Pattern rightPattern = createNormNet(right, page);

		// create three transitions
		Transition C1 = createTransition("autoN" + normOE.getID()
		+ "1", page);
		Transition V1 = createTransition("autoN" + normOE.getID()
		+ "2", page);
		Transition C2 = createTransition("autoN" + normOE.getID()
		+ "3", page);
		Transition V2 = createTransition("autoN" + normOE.getID()
		+ "4", page);
		// add the connections for OE between Norms
		Arc fromC11 = createArc(leftPattern.getLastPlaces().get(0),
				C1, page);
		Arc fromV11 = createArc(leftPattern.getLastPlaces().get(1),
				V1, page);
		Arc fromC21 = createArc(rightPattern.getLastPlaces().get(0),
				C2, page);
		Arc fromV21 = createArc(rightPattern.getLastPlaces().get(1),
				V2, page);

		//-----
		// Create outputC place
		Place outputC = createPlace("C" + normOE.getID(),
				Colors.getcolorbyName("Complied"), page);
		// Create outputV place
		Place outputV = createPlace("V" + normOE.getID(), 
				Colors.getcolorbyName("Violated"), page);

		// connect to the output places
		Arc a1 = createArc(C1, outputC, page);
		Arc a2 = createArc(C2, outputC, page);
		Arc a3 = createArc(V2, outputV, page);

		// create new variables with the color of Complied and Violated
		VariableDeclaration leftC = createArcVariable("varLC"+normOE.getID(), 
				Colors.getcolorbyName("Complied"), page);
		VariableDeclaration rightC = createArcVariable("varRC"+normOE.getID(), 
				Colors.getcolorbyName("Complied"), page);
		VariableDeclaration leftV = createArcVariable("varLV"+normOE.getID(), 
				Colors.getcolorbyName("Violated"), page);
		VariableDeclaration rightV = createArcVariable("varRV"+normOE.getID(), 
				Colors.getcolorbyName("Violated"), page);
		setArcInscription(fromC11, leftC);
		setArcInscription(fromC21, rightC);
		setArcInscription(fromV11, leftV);
		setArcInscription(fromV21, rightV);
		a1.setHlinscription(fromC11.getHlinscription());
		a2.setHlinscription(fromC21.getHlinscription());
		setArcInscription(a3, rightV);

		// Create vr place with the same color as norm1V
		Place vr = createPlace("vr" + normOE.getID(), 
				Colors.getcolorbyName("Violated"),page);
		Arc a4 = createArc(V1, vr, page);
		setArcInscription(a4, leftV);

		//make connection between vr and norm2
		for (int i = 0; i < rightPattern.getFirstTransitions().size(); i++) {
			Arc toN2 = createArc(vr, rightPattern.getFirstTransitions().get(i), page);
			Arc toN1 = createArc(rightPattern.getFirstTransitions().get(i), vr, page);
			setArcInscription(toN2, leftV);
			toN1.setHlinscription(toN2.getHlinscription());			
		}		

		// complete the pattern of OE
		normOEpt.addFirstPlaces(leftPattern.getFirstPlaces());
		normOEpt.addLastPlaces(outputC);
		normOEpt.addLastPlaces(outputV);
		normOEpt.addFirstTransitions(leftPattern.getFirstTransitions());
		normOEpt.addLastTransitions(C1);
		normOEpt.addLastTransitions(C2);
		normOEpt.addLastTransitions(V1);
		return normOEpt;
	}

	/*************************************** NAME(label) ********************************************/
	/*
	 * build a name object for a place or a transition based on the
	 * corresponding name from NN, the name attribute of a places or a
	 * transition is its label that will appear in the CPN
	 */
	private Name buildName(String text) {
		Name n = modelfactory.createName();
		n.setText(text);
		return n;
	}

	/*************************************** TRANSITION ********************************************/
	// create a transition based on the name of an action from NN
	private Transition createTransition(String actionname, Page page) {
		Transition transition = modelfactory.createTransition();
		transition.setId(IDgenerator.getID());
		transition.setName(buildName(actionname));
		transition.setPage(page);
		return transition;
	}

	/*************************************** COLOR ************************************************/
	/*
	 * create a single color based on the name of a role from NN, only for
	 * single role-action pairs, corresponding expression in CPN tools: colset
	 * typename = with rolename.
	 */
	private Sort createColor(String rolename, Page page) {

		/*
		 * create a CPN type based on the role name, the type is of the form
		 * "with rolename"
		 */
		CPNEnum type = typefactory.createCPNEnum();
		type.addValue(rolename);

		// create a type declaration based on the type
		TypeDeclaration tydeclaration = declarfactory.createTypeDeclaration();
		tydeclaration.setTypeName(rolename);
		tydeclaration.setSort(type);

		/*
		 * create a representation of the type declaration and add it to the
		 * petri net
		 */
		HLDeclaration hldeclaration = modelfactory.createHLDeclaration();
		hldeclaration.setStructure(tydeclaration);
		hldeclaration.setId(IDgenerator.getID());
		System.out.println(hldeclaration.getText());
		hldeclaration.setParent(page.getPetriNet());

		// create a sort based on the type so that it can be set to places
		Sort color = modelfactory.createSort();
		color.setText(tydeclaration.getTypeName());
		System.out.println("colortext:" + color.getText());
		Colors.getcolors().add(color);
		return color;
	}

	/*
	 * create a color based on the union of two colors, used to create color for
	 * places which are connections in AND, OR, OE relation, corresponding
	 * expression in CPN tools: colset typename = union xx:rolename1 +
	 * xx:rolename2, xx is the abbreviation of the name of a single color, e.g.,
	 * r1:rolename1.
	 */
	private Sort createColorUnion(Sort color1, Sort color2, Page page) {

		/*
		 * create a type: combining the two places, the type is of the form
		 * "union xx:color1 + xx:color2". xx is the abbreviation of the name of a
		 * single color and here we use the upper case of the name of the color
		 */
		CPNUnion type = typefactory.createCPNUnion();
		type.addValue(color1.getText().toUpperCase(), color1.getText());
		System.out.println(color1.getText());
		type.addValue(color2.getText().toUpperCase(), color2.getText());
		System.out.println(color2.getText());

		// create a type declaration based on the type
		TypeDeclaration tydeclaration = declarfactory.createTypeDeclaration();
		tydeclaration.setTypeName("U" + color1.getText() + color2.getText()); // U
		// -->union
		tydeclaration.setSort(type);

		/*
		 * create a representation of the type declaration and add it to the
		 * petri net
		 */
		HLDeclaration hldeclaration = modelfactory.createHLDeclaration();
		hldeclaration.setStructure(tydeclaration);
		hldeclaration.setId(IDgenerator.getID());

		/*
		 * Union declarations can not be parsed by the DOMGenerator.export, we
		 * output all the union declarations to a txt file and paste in the CPN
		 * tools
		 */
		System.out.println(hldeclaration.getText());
		colorDeclarations.add(hldeclaration.getText());

		Sort color = modelfactory.createSort();
		color.setText(tydeclaration.getTypeName());
		System.out.println(color.getText());
		Colors.getcolors().add(color);
		return color;
	}

	/*
	 * create a color of the type of List, used to create color for places which
	 * are connections in AND, OR, OE relation, enable the form of ^^ between
	 * two tokens, corresponding expression in CPN tools: colset typename = list
	 * rolename.
	 */
	private Sort createColorList(Sort color, Page page) {

		/*
		 * create a type: List, the type is of the form "list color"
		 */
		CPNList type = typefactory.createCPNList();
		type.setSort(color.getText());

		// create a type declaration based on the type
		TypeDeclaration tydeclaration = declarfactory.createTypeDeclaration();
		tydeclaration.setTypeName("L" + color.getText()); // L -->list
		tydeclaration.setSort(type);

		/*
		 * create a representation of the type declaration and add it to the
		 * petri net
		 */
		HLDeclaration hldeclaration = modelfactory.createHLDeclaration();
		hldeclaration.setStructure(tydeclaration);
		hldeclaration.setId(IDgenerator.getID());
		System.out.println(hldeclaration.getText());
		hldeclaration.setParent(page.getPetriNet());

		// create a sort based on the type so that it can be set to places
		Sort colorlist = modelfactory.createSort();
		colorlist.setText(tydeclaration.getTypeName());
		System.out.println(colorlist.getText());
		Colors.getcolors().add(colorlist);
		return colorlist;
	}

	/*create a general complied color and a general violated color
	 *based on all the roles in the norm net
	 */

	private void createCompliedViolated(GraphEntity normNet, Page page) {
		assert normNet.getEntity().getClass().equals(RegulativeNormNet.class);

		CPNUnion complied = typefactory.createCPNUnion();
		CPNUnion violated = typefactory.createCPNUnion();

		HashSet<GraphEntity> roleEntities=findLinkedElements(normNet, RAP.class);

		HashSet<GraphEntity> roles=new HashSet<GraphEntity>();
		for (GraphEntity role:roleEntities){
			try {
System.err.println(role.getAttributeByName("Role").getEntityValue());
				roles.add(role.getAttributeByName("Role").getEntityValue());
			} catch (NullEntity | NotFound e) {
				e.printStackTrace();
			}
		}

		for(GraphEntity role:roles){
			Colors.getcolors().add((createColor(role.getID(), page)));
			complied.addValue("C"+role.getID(), role.getID());
			violated.addValue("V"+role.getID(), role.getID());			
		}

		// create a type declaration based on the type
		TypeDeclaration complieddeclaration = declarfactory.createTypeDeclaration();
		complieddeclaration.setTypeName("complied");
		complieddeclaration.setSort(complied);
		TypeDeclaration violateddeclaration = declarfactory.createTypeDeclaration();
		violateddeclaration.setTypeName("violated");
		violateddeclaration.setSort(violated);		

		/*
		 * create a representation of the type declaration and add it to the
		 * petri net
		 */
		HLDeclaration comhldeclaration = modelfactory.createHLDeclaration();
		comhldeclaration.setStructure(complieddeclaration);
		comhldeclaration.setId(IDgenerator.getID());
		System.out.println(comhldeclaration.getText());
		HLDeclaration viohldeclaration = modelfactory.createHLDeclaration();
		viohldeclaration.setStructure(violateddeclaration);
		viohldeclaration.setId(IDgenerator.getID());
		System.out.println(viohldeclaration.getText());

		/*
		 * Union declarations can not be parsed by the DOMGenerator.export, we
		 * output all the union declarations to a txt file and paste in the CPN
		 * tools
		 */
		colorDeclarations.add(comhldeclaration.getText());
		colorDeclarations.add(viohldeclaration.getText());	

		// create a sort based on the type so that it can be set to places
		Sort colorCom = modelfactory.createSort();
		colorCom.setText(complieddeclaration.getTypeName());
		System.out.println(colorCom.getText());
		Colors.getcolors().add(colorCom);
		Sort colorVio = modelfactory.createSort();
		colorVio.setText(violateddeclaration.getTypeName());
		System.out.println(colorVio.getText());
		Colors.getcolors().add(colorVio);

		//-------------------------------------------------------------------------------------------------

		// create a List type of the union type
		CPNList Lcomplied = typefactory.createCPNList();
		Lcomplied.setSort("complied");
		CPNList Lviolated = typefactory.createCPNList();
		Lviolated.setSort("violated");

		// create a type declaration based on the type
		TypeDeclaration Lcomdeclaration = declarfactory.createTypeDeclaration();
		Lcomdeclaration.setTypeName("Complied"); // L -->list
		Lcomdeclaration.setSort(Lcomplied);
		TypeDeclaration Lviodeclaration = declarfactory.createTypeDeclaration();
		Lviodeclaration.setTypeName("Violated"); // L -->list
		Lviodeclaration.setSort(Lviolated);

		/*
		 * create a representation of the type declaration and add it to the
		 * petri net
		 */
		HLDeclaration Lcomhldeclaration = modelfactory.createHLDeclaration();
		Lcomhldeclaration.setStructure(Lcomdeclaration);
		Lcomhldeclaration.setId(IDgenerator.getID());
		System.out.println(Lcomhldeclaration.getText());
		Lcomhldeclaration.setParent(page.getPetriNet());
		HLDeclaration Lviohldeclaration = modelfactory.createHLDeclaration();
		Lviohldeclaration.setStructure(Lviodeclaration);
		Lviohldeclaration.setId(IDgenerator.getID());
		System.out.println(Lviohldeclaration.getText());
		Lviohldeclaration.setParent(page.getPetriNet());

		// create a sort based on the type so that it can be set to places
		Sort LcolorCom = modelfactory.createSort();
		LcolorCom.setText(Lcomdeclaration.getTypeName());
		System.out.println(LcolorCom.getText());
		Colors.getcolors().add(LcolorCom);
		Sort LcolorVio = modelfactory.createSort();
		LcolorVio.setText(Lviodeclaration.getTypeName());
		System.out.println(LcolorVio.getText());
		Colors.getcolors().add(LcolorVio);
	}

	private HashSet<GraphEntity> findLinkedElements(GraphEntity normNet, Class<RAP> class1, HashSet<GraphEntity> visited) {
		HashSet<GraphEntity> result=new HashSet<GraphEntity>();
		System.out.println(visited);
		for (GraphRelationship gr:normNet.getAllRelationships()){
			for (GraphRole role:gr.getRoles()){
				try {
					if (!visited.contains(role.getPlayer())){
						if (role.getRoleEntity().getType().toLowerCase().endsWith("target") || role.getRoleEntity().getType().toLowerCase().endsWith("targetrole")){
							if (role.getPlayer().getEntity().getClass().equals(class1)){	
								System.err.println(role.getPlayer());
								result.add(role.getPlayer());
							}
							visited.add(role.getPlayer());
							result.addAll(findLinkedElements(role.getPlayer(),class1,visited));
						}
						else
							visited.add(role.getPlayer());
					}
				} catch (NullEntity e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}
		}
		return result;
	}


	private HashSet<GraphEntity> findLinkedElements(GraphEntity normNet, Class<RAP> class1) {
		return findLinkedElements(normNet,class1,new HashSet<GraphEntity>());
	}

	/*************************************** PLACE ************************************************/
	/*
	 * create a place and add a color to it !!!the label should be made up from
	 * a naming system
	 */
	private Place createPlace(String label, Sort color, Page page) {
		Place place = modelfactory.createPlace();
		place.setId(IDgenerator.getID());
		place.setName(buildName(label));
		place.setSort(color);
		place.setPage(page); // add the color to the place
		return place;
	}

	/*************************************** ARC *************************************************/
	// create an arc that connects a place to a transition
	private Arc createArc(Place place, Transition transition, Page page) {
		Arc arc = modelfactory.createArc();
		arc.setId(IDgenerator.getID());
		arc.setSource(place);
		arc.setTarget(transition);
		arc.setPage(page);
		return arc;
	}

	// create an arc that connects a transition to a place
	private Arc createArc(Transition transition, Place place, Page page) {
		Arc arc = modelfactory.createArc();
		arc.setId(IDgenerator.getID());
		arc.setSource(transition);
		arc.setTarget(place);
		arc.setPage(page);
		return arc;
	}

	/************************************* ARC_Variable ******************************************/
	/*
	 * create a variable for an arc based on the color of the place connected by
	 * the arc
	 */
	private VariableDeclaration createArcVariable(String varname, Sort color,
			Page page) {

		/*
		 * create a variable declaration based on the color of the place
		 * connecting by the arc
		 */
		VariableDeclaration vardeclaration = declarfactory
				.createVariableDeclaration();
		vardeclaration.setTypeName(color.getText());
		vardeclaration.addVariable(varname);

		/*
		 * create a representation of the variable declaration and add it to the
		 * petri net of the page (on the left panel)
		 */
		HLDeclaration hldeclaration = modelfactory.createHLDeclaration();
		hldeclaration.setStructure(vardeclaration);
		hldeclaration.setId(IDgenerator.getID()); // V -->variable
		System.out.println(hldeclaration.getText());
		hldeclaration.setParent(page.getPetriNet());
		return vardeclaration;
	}

	/*********************************** ARC_INSCRIPTION ****************************************/
	/*
	 * create an arc inscription based on the variable declaration
	 */
	private void setArcInscription(Arc arc, VariableDeclaration vardeclaration) {

		HLAnnotation arcinscription = modelfactory.createHLAnnotation();
		arcinscription.setText(vardeclaration.getVariables().get(0));
		arc.setHlinscription(arcinscription);
	}

	/*
	 * create an arc inscription based on the union of several arc inscriptions
	 * the inscription is of the form "arcinscription1 ^^ arcinscription2"
	 */
	private void setArcInscriptionUnion(Arc arc) {

		HLAnnotation arcinscription = modelfactory.createHLAnnotation();
		List<Arc> arcsin = arc.getTransition().getTargetArc();
		String Ainscription = "";

		for (Arc eacharc : arcsin) {
			if (Ainscription != "") {
				if (eacharc.getHlinscription().getText().startsWith("[")) {
					Ainscription = Ainscription + "^^"
							+ eacharc.getHlinscription().getText();
				} else {
					Ainscription = Ainscription + "^^" + "["
							+ eacharc.getPlaceNode().getSort().getText().toUpperCase() + "("
							+ eacharc.getHlinscription().getText() + ")]";
				}
			} else {
				if (eacharc.getHlinscription().getText().startsWith("[")) {
					Ainscription = eacharc.getHlinscription().getText();
				} else
					Ainscription = "["
							+ eacharc.getPlaceNode().getSort().getText().toUpperCase() + "("
							+ eacharc.getHlinscription().getText() + ")]";
			}

		}

		arcinscription.setText(Ainscription);
		arc.setHlinscription(arcinscription);
	}

	/*
	 * create an arc inscription connecting two arc inscriptions with the type of Complied or Violated
	 * the inscription is of the form "arcinscription1 ^^ arcinscription2"
	 */	
	private void setArcInscriptionCompliedViolated(Arc arc, VariableDeclaration left, VariableDeclaration right) {

		HLAnnotation arcinscription = modelfactory.createHLAnnotation();
		String Ainscription = left.getVariables().get(0)+"^^"+right.getVariables().get(0);
		arcinscription.setText(Ainscription);
		arc.setHlinscription(arcinscription);
	}

}
