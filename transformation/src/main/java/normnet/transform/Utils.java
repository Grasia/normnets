package normnet.transform;

import ingenias.editor.ModelJGraph;
import ingenias.editor.cell.NAryEdge;
import ingenias.editor.entities.NAryEdgeEntity;
import ingenias.editor.entities.RoleEntity;
/*
 Copyright (C) 2005 Jorge Gomez Sanz

 This file is part of INGENIAS Agent Framework, an agent infrastructure linked
 to the INGENIAS Development Kit, and availabe at http://grasia.fdi.ucm.es/ingenias or
 http://ingenias.sourceforge.net. 

 INGENIAS Agent Framework is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 INGENIAS Agent Framework is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with INGENIAS Agent Framework; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 */
import ingenias.exception.NotInitialised;
import ingenias.exception.NullEntity;
import ingenias.generator.browser.Browser;
import ingenias.generator.browser.Graph;
import ingenias.generator.browser.GraphAttribute;
import ingenias.generator.browser.GraphEntity;
import ingenias.generator.browser.GraphRelationship;
import ingenias.generator.browser.GraphRole;
import ingenias.generator.datatemplate.Sequences;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.maven.artifact.resolver.ArtifactResolutionException;

/**
 * This class encapsulates methods to traverse the specification. In general, it
 * allows to obtain, given an entity, query the specification to obtain
 * connected entities. It also provides transformation methods to arrays of
 * GraphEntity and GraphRelationship.
 *
 * @author jj
 *
 */
public class Utils {

    /**
     * It replaces incorrect chars that may cause conflicts in the final
     * instances
     *
     * @param string The string being converted, like white spaces
     * @return A string without improper characters
     */
    public static String replaceBadChars(String string) {
        return string.replace(' ', '_').replace(',', '_').replace('.', '_')
                .replace('-', '_').trim().replace("\n", "");
    }

   
   

    /**
     * It obtains the elements in the specification linked with "element" that
     * have an association of type "relationshipname" and they occupy the
     * extreme labelled with the same string as "role"
     *
     * @param element The element to be studied
     * @param relationshipname The name of the relationship which will be
     * studied
     * @param role The name of the extreme of the relationship that has to be
     * studied
     * @return a list of elements placed in the association extreme
     * @throws NullEntity
     */
    public static GraphEntity[] getRelatedElements(GraphEntity element,
            Class<? extends NAryEdgeEntity> relationshipclass, Class<? extends RoleEntity> role) throws NullEntity {
        Vector rels = element.getAllRelationships();
        Enumeration enumeration = rels.elements();
        Vector related = new Vector();
        while (enumeration.hasMoreElements()) {
            GraphRelationship gr = (GraphRelationship) enumeration
                    .nextElement();
            if (gr.getNAryEdge().getClass().equals(relationshipclass)) {
                GraphRole[] roles = gr.getRoles();
                for (int k = 0; k < roles.length; k++) {
                    // System.err.println(roles[k].getName());
                    if (roles[k].getRoleEntity().getClass().equals(role)) {
                        // System.err.println("added"+roles[k].getName());
                        related.add(roles[k].getPlayer());
                    }
                }
            }
        }
        return toGEArray(new HashSet(related).toArray());
    }

    /**
     * It obtains the elements in the specification linked with "element" that
     * have an association of type "relationshipname" and they occupy the
     * extreme labelled with the same string as "role". Elements returned cannot
     * be equal to "element".
     *
     * @param element The element to be studied
     * @param relationshipname The name of the relationship which will be
     * studied
     * @param role The name of the extreme of the relationship that has to be
     * studied
     * @return a list of elements placed in the association extreme
     * @throws NullEntity
     */
    public static GraphEntity[] getRelatedElementsAux(GraphEntity element,
    		Class<? extends NAryEdgeEntity> relationshipclass, Class<? extends RoleEntity> role) throws NullEntity {
        Vector rels = element.getAllRelationships();
        Enumeration enumeration = rels.elements();
        Vector related = new Vector();
        while (enumeration.hasMoreElements()) {
            GraphRelationship gr = (GraphRelationship) enumeration
                    .nextElement();
            if (gr.getNAryEdge().getClass().equals(relationshipclass)) {
                GraphRole[] roles = gr.getRoles();
                for (int k = 0; k < roles.length; k++) {
                    if (roles[k].getRoleEntity().getClass().equals(role)
                            && !roles[k].getPlayer().equals(element)) {
                        related.add(roles[k].getPlayer());
                    }
                }
            }
        }
        return Utils.toGEArray(new HashSet(related).toArray());
    }

    /**
     * Same as getRelatedElementsAux but returning the result as vectors
     *
     * @param element The element to be studied
     * @param relationshipname The name of the relationship which will be
     * studied
     * @param role The name of the extreme of the relationship that has to be
     * studied
     * @return a list of elements placed in the association extreme
     * @throws NullEntity
     */
    public static Vector getRelatedElementsVectorAux(GraphEntity element,
    		Class<? extends NAryEdgeEntity> relationshipclass, Class<? extends RoleEntity> role) throws NullEntity {
        Vector rels = element.getAllRelationships();
        Enumeration enumeration = rels.elements();
        Vector related = new Vector();
        while (enumeration.hasMoreElements()) {
            GraphRelationship gr = (GraphRelationship) enumeration
                    .nextElement();
            if (gr.getNAryEdge().getClass().equals(relationshipclass)) {
                GraphRole[] roles = gr.getRoles();
                for (int k = 0; k < roles.length; k++) {
                	if (roles[k].getRoleEntity().getClass().equals(role)
                            && !roles[k].getPlayer().equals(element)) {
                        related.add(roles[k].getPlayer());
                    }
                }
            }
        }
        return new Vector(new HashSet(related));
    }

    /**
     * Same as getRelatedElements but returning the result as a vector
     *
     * @param element The element to be studied
     * @param relationshipname The name of the relationship which will be
     * studied
     * @param role The name of the extreme of the relationship that has to be
     * studied
     * @return a list of elements placed in the association extreme
     */
    public static Vector<GraphEntity> getRelatedElementsVector(
            GraphEntity agent, Class<? extends NAryEdgeEntity> relationshipclass, Class<? extends RoleEntity> role)
            throws NullEntity {
        Vector rels = agent.getAllRelationships();
        Enumeration enumeration = rels.elements();
        Vector related = new Vector();
        while (enumeration.hasMoreElements()) {
            GraphRelationship gr = (GraphRelationship) enumeration
                    .nextElement();
            if (gr.getNAryEdge().getClass().equals(relationshipclass)) {
                GraphRole[] roles = gr.getRoles();
                for (int k = 0; k < roles.length; k++) {
                    if (roles[k].getRoleEntity().getClass().equals(role)) {
                        related.add(roles[k].getPlayer());
                    }
                }
            }
        }
        return new Vector(new HashSet(related));
    }
    
    public static Vector<GraphEntity> getRelatedElementsVectorInSameDiagram(
            GraphEntity agent, Class<? extends NAryEdgeEntity> relationshipclass, Class<? extends RoleEntity> role)
            throws NullEntity {
        GraphRelationship[] rels = agent.getRelationships();        
        Vector related = new Vector();
        for (GraphRelationship gr:rels){        
            if (gr.getNAryEdge().getClass().equals(relationshipclass)) {
                GraphRole[] roles = gr.getRoles();
                for (int k = 0; k < roles.length; k++) {
                    if (roles[k].getRoleEntity().getClass().equals(role)) {
                        related.add(roles[k].getPlayer());
                    }
                }
            }
        }
        return new Vector(new HashSet(related));
    }

   


    public static Graph getGraphByName(String name, Browser browser) {
        for (Graph g : browser.getGraphs()) {
            if (g.getName().equals(name)) {
                return g;
            }
        }
        return null;
    }

    public static Collection<Graph> getGraphsByType(Class<? extends ModelJGraph> type, Browser browser) {
        Collection<Graph> result = new Vector<>();
        for (Graph g : browser.getGraphs()) {
            if (g.getGraph().getClass().equals(type)) {
                result.add(g);
            }
        }
        return result;
    }
    
   
    public static GraphRole getTargetRole(GraphRole[] roles) {
        for (GraphRole gRole : roles) {
            if (gRole.getName().endsWith("target")) {
                return gRole;
            }
        }
        return null;
    }

    public static List<GraphRole> getTargetsRole(GraphRole[] roles) {
        List<GraphRole> result = new ArrayList<>();
        for (GraphRole gRole : roles) {
            if (gRole.getName().endsWith("target")) {
                result.add(gRole);
            }
        }
        return result;
    }

    public static GraphRole getSourceRole(GraphRole[] roles) {
        for (GraphRole gRole : roles) {
            if (gRole.getName().endsWith("source")) {
                return gRole;
            }
        }
        return null;
    }

    public static Collection<GraphEntity> getTargetsEntity(GraphEntity ge, Class<? extends NAryEdge> relationType) {
        List<GraphEntity> result = new ArrayList<>();
        try {
            for (GraphRelationship gr : ge.getRelationships()) {
                if (gr.getNAryEdge().getClass().equals(relationType)) {
                    for (GraphRole gRole: getTargetsRole(gr.getRoles()) ) {
                        if (gRole != null && gRole.getPlayer().getID() != ge.getID()) {
                            result.add(gRole.getPlayer());
                        }
                    }
                }
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public static Collection<GraphEntity> getSourcesEntity(GraphEntity ge, Class<? extends NAryEdge> relationType) {
        List<GraphEntity> result = new ArrayList<GraphEntity>();
        try {
            for (GraphRelationship gr : ge.getRelationships()) {
                if (gr.getNAryEdge().getClass().equals(relationType)) {
                    GraphRole gRole = getSourceRole(gr.getRoles());
                    if (gRole != null && gRole.getPlayer().getID() != ge.getID()) {
                        result.add(gRole.getPlayer());
                    }
                }
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public static GraphEntity getSourceEntity(GraphEntity ge, GraphRelationship gr)  {
    	GraphRole gRole = getSourceRole( gr.getRoles());
        try {
			if (gRole != null && gRole.getPlayer().getID() != ge.getID()) {
			    return gRole.getPlayer();
			}
		} catch (NullEntity e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return null;
    }
    
    public static GraphEntity getSourceEntity(GraphEntity ge,  Class<? extends NAryEdge> relationType) {
        try {
            for (GraphRelationship gr : ge.getRelationships()) {
                if (gr.getNAryEdge().getClass().equals(relationType)) {
                	 return getSourceEntity(ge, gr);
                }
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return null;
    }



	public static GraphEntity getTargetEntity(GraphEntity ge, GraphRelationship gr)
			throws NullEntity {
		GraphRole gRole = getTargetRole(gr.getRoles());
		if (gRole != null && !gRole.getPlayer().getID().equals(ge.getID())) {
		    return gRole.getPlayer();
		}
		return null;
	}

    public static boolean isTargetOfAnyRelationship(GraphEntity ge)
            throws NullEntity {
        for (GraphRelationship gr : ge.getRelationships()) {
            for (GraphRole gRole : gr.getRoles()) {
                if (gRole.getPlayer().getID().equals(ge.getID())
                        && gRole.getName().endsWith("target")) {
                    return true;
                }
            }
        }
        return false;
    }

    public static GraphEntity getFirstEntity(Graph graph) {
        GraphEntity result = null;
        try {
            for (GraphEntity ge : graph.getEntities()) {
                if (!isTargetOfAnyRelationship(ge)) {
                    return ge;
                }
            }
        } catch (NullEntity e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }
    
    public static List<GraphEntity> getFirstEntities(Graph graph) {
        List<GraphEntity> result = new ArrayList<>();;
        try {
            for (GraphEntity ge : graph.getEntities()) {
                if (!isTargetOfAnyRelationship(ge)) {
                    result.add(ge);
                }
            }
        } catch (NullEntity e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    /**
     * It casts an array of objets to an array of GraphEntity
     *
     * @param o the array of objects
     * @return
     */
    public static GraphEntity[] toGEArray(Object[] o) {
        GraphEntity[] result = new GraphEntity[o.length];
        System.arraycopy(o, 0, result, 0, o.length);
        return result;
    }

    /**
     * It casts an array of objets to an array of GraphRelationship
     *
     * @param o the array of objects
     * @return
     */
    public static GraphRelationship[] toGRArray(Object[] o) {
        GraphRelationship[] result = new GraphRelationship[o.length];
        System.arraycopy(o, 0, result, 0, o.length);
        return result;
    }

    /**
     * It casts an array of objets to an array of GraphRole
     *
     * @param o the array of objects
     * @return
     */
    public static GraphRole[] toGRoArray(Object[] o) {
        GraphRole[] result = new GraphRole[o.length];
        System.arraycopy(o, 0, result, 0, o.length);
        return result;
    }

 
    /**
     * It obtains all entities in the specification whose type represented as
     * string is the same as the string passed as parameter
     *
     * @param type The type the application is looking for
     * @return
     * @throws NotInitialised
     */
    public static GraphEntity[] generateEntitiesOfType(Class type,
            Browser browser) throws NotInitialised {
        Graph[] gs = browser.getGraphs();
        Sequences p = new Sequences();
        GraphEntity[] ges = browser.getAllEntities();
        HashSet actors = new HashSet();
        for (int k = 0; k < ges.length; k++) {
            if (ges[k].getEntity().getClass().equals(type)) {
                actors.add(ges[k]);
            }
        }
        return toGEArray(actors.toArray());
    }

    /**
     * It obtains the extremes of the association of type "relationshipname",
     * where one of their roles is "role", and originated in the "element"
     *
     * @param element The element to be studied
     * @param relationshipname The name of the relationship which will be
     * studied
     * @param role The name of the extreme of the relationship that has to be
     * studied
     * @return An array of roles
     */
    public static GraphRole[] getRelatedElementsRoles(GraphEntity element,
            String relationshipname, String role) {
        Vector rels = element.getAllRelationships();
        Enumeration enumeration = rels.elements();
        Vector related = new Vector();
        while (enumeration.hasMoreElements()) {
            GraphRelationship gr = (GraphRelationship) enumeration
                    .nextElement();
            if (gr.getType().toLowerCase()
                    .equals(relationshipname.toLowerCase())) {
                GraphRole[] roles = gr.getRoles();
                for (int k = 0; k < roles.length; k++) {
                    if (roles[k].getName().toLowerCase()
                            .equals(role.toLowerCase())) {
                        related.add(roles[k]);
                    }
                }
            }
        }
        return toGRoArray(related.toArray());
    }

    /**
     * It obtains the extremes of the association of type "relationshipname",
     * where one of their roles is "role", and originated in the "element"
     *
     * @param element The element to be studied
     * @param relationshipname The name of the relationship which will be
     * studied
     * @param role The name of the extreme of the relationship that has to be
     * studied
     * @return A vector of roles
     */
    public static Vector<GraphRole> getRelatedElementsRolesVector(
            GraphEntity element, String relationshipname, String role) {
        Vector rels = element.getAllRelationships();
        Enumeration enumeration = rels.elements();
        Vector<GraphRole> related = new Vector<GraphRole>();
        while (enumeration.hasMoreElements()) {
            GraphRelationship gr = (GraphRelationship) enumeration
                    .nextElement();
            if (gr.getType().toLowerCase()
                    .equals(relationshipname.toLowerCase())) {
                GraphRole[] roles = gr.getRoles();
                for (int k = 0; k < roles.length; k++) {
                    if (roles[k].getName().toLowerCase()
                            .equals(role.toLowerCase())) {
                        related.add(roles[k]);
                    }
                }
            }
        }
        return related;
    }

    /**
     * It returns an array of the relationships whose name is "relationshipname"
     * and that are linked to "element" and there is an element occupiying the
     * extreme labelled with "role"
     *
     * @param element The element to be studied
     * @param relationshipname The name of the relationship which will be
     * studied
     * @param role The name of the extreme of the relationship that has to be
     * studied
     * @return an array of relationships
     */
    public static GraphRelationship[] getRelatedElementsRels(
            GraphEntity element, String relationshipname, String role) {
        Vector rels = element.getAllRelationships();
        Enumeration enumeration = rels.elements();
        Vector related = new Vector();
        while (enumeration.hasMoreElements()) {
            GraphRelationship gr = (GraphRelationship) enumeration
                    .nextElement();
            if (gr.getType().toLowerCase()
                    .equals(relationshipname.toLowerCase())) {
                GraphRole[] roles = gr.getRoles();
                for (int k = 0; k < roles.length; k++) {
                    if (roles[k].getName().toLowerCase()
                            .equals(role.toLowerCase())) {
                        related.add(gr);
                    }
                }
            }
        }
        return toGRArray(related.toArray());
    }

    public static GraphRelationship[] getRelatedElementsRels(
            GraphEntity element, String relationshipname) {
        Vector rels = element.getAllRelationships();
        Enumeration enumeration = rels.elements();
        Vector related = new Vector();
        while (enumeration.hasMoreElements()) {
            GraphRelationship gr = (GraphRelationship) enumeration
                    .nextElement();
            if (gr.getType().toLowerCase()
                    .equals(relationshipname.toLowerCase())) {
                related.add(gr);
            }
        }
        return toGRArray(related.toArray());
    }

    /**
     * It obtains the entities in the graph "g" whose type is the same as
     * "typeName".
     *
     * @param g The graph considered
     * @param typeName The type being searched
     * @return The list of entities
     * @throws NullEntity
     */
    public static List<GraphEntity> getEntities(Graph g, String typeName)
            throws NullEntity {
        GraphEntity[] ge = g.getEntities();
        List<GraphEntity> result = new ArrayList<>();
        for (int k = 0; k < ge.length; k++) {
            if (ge[k].getType().equals(typeName)) {
                result.add(ge[k]);
            }
        }
        return result;
    }

    public static Vector<GraphRole> getRolesFromRelationship(
            GraphRelationship rel, String role) {
        Vector<GraphRole> related = new Vector<GraphRole>();
        GraphRole[] roles = rel.getRoles();
        for (int k = 0; k < roles.length; k++) {
            if (roles[k].getName().toLowerCase().equals(role.toLowerCase())) {
                related.add(roles[k]);
            }
        }
        return related;
    }

	public static boolean contains(Graph graph, GraphEntity actor) {
		try {
			for (GraphEntity ge:graph.getEntities())
				if (ge.equals(actor))
					return true;
		} catch (NullEntity e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
        
        public static String getAttributeByName(GraphEntity ge, String attributeName) {
            for(GraphAttribute ga: ge.getAllAttrs()) {
                if(ga.getName().equals(attributeName)) {
                    return ga.getSimpleValue();
                }
            }
            return "";
        }
        
        public static String yesNoToTrueFalse(String yesno) {
            if(yesno.equalsIgnoreCase("Yes")) {
                return "true";
            } else {
                return "false";
            }
        }
}