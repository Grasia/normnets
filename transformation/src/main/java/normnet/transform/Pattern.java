/**
 * 
 */
package normnet.transform;

import java.util.ArrayList;

import org.cpntools.accesscpn.model.Place;
import org.cpntools.accesscpn.model.Transition;
import org.cpntools.accesscpn.model.impl.PetriNetImpl;

/**
 * the intermediate representation between NNs and CPNs 
 * @author huib
 *
 */
public class Pattern extends PetriNetImpl {

	/**
	 *  the first, last places and transitions in a CPN pattern.
	 *  used to identify the interfacing places and transitions 
	 *  when building connections between formulas
	 */
	
	private ArrayList<Place> firstPlaces;
	private ArrayList<Place> lastPlaces;
	private ArrayList<Transition> firstTransitions;
	private ArrayList<Transition> lastTransitions;
	
	public Pattern() {
		firstPlaces = new ArrayList<Place>();
		lastPlaces = new ArrayList<Place>();
		firstTransitions = new ArrayList<Transition>();
		lastTransitions = new ArrayList<Transition>();
	}

	public ArrayList<Place> getFirstPlaces() {
		return firstPlaces;
	}

	public void addFirstPlaces(ArrayList<Place> firstPlace) {
		firstPlaces.addAll(firstPlace);
	}
	
	public void addFirstPlaces(Place firstPlace) {
		firstPlaces.add(firstPlace);
	}

	public ArrayList<Place> getLastPlaces() {
		return lastPlaces;
	}

	public void addLastPlaces(ArrayList<Place> lastPlace) {
		lastPlaces.addAll(lastPlace);
	}
	
	public void addLastPlaces(Place lastPlace) {
		lastPlaces.add(lastPlace);
	}

	public ArrayList<Transition> getFirstTransitions() {
		return firstTransitions;
	}

	public void addFirstTransitions(ArrayList<Transition> firstTransition) {
		firstTransitions.addAll(firstTransition);
	}
	
	public void addFirstTransitions(Transition firstTransition) {
		firstTransitions.add(firstTransition);
	}

	public ArrayList<Transition> getLastTransitions() {
		return lastTransitions;
	}

	public void addLastTransitions(ArrayList<Transition> lastTransition) {
		lastTransitions.addAll(lastTransition);
	}
	
	public void addLastTransitions(Transition lastTransition) {
		lastTransitions.add(lastTransition);
	}

}
