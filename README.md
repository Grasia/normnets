The NormNets formalism is a work developed by Jie Jian (TU Delft) for his PHD, advised by Prof. dr. Y.H. Tan (TU Delft), Dr. M.V. Dignum (TU Delft), Dr. H.M. Aldewereld (TU Delft).

http://dx.doi.org/10.4233/uuid:d02f642d-f797-4eab-a8b1-126b7d110431

 It has been useful too the Compliance Checking In Supply Chain Management by Shuzheng Wang, advised by Dr. Huib Aldewereld (TU Delft), Dr. Michel Oey (TU Delft), and Jie Jiang (TU Delft).

http://resolver.tudelft.nl/uuid:7640df1c-2313-478e-a096-cbdeea4fef16

The project aims to translate the formalism from an initial Eclipse based plugin into INGENME metaediting facilities (http://ingenme.sf.net). Transferred functionality includes modeling capabilities and transformation towards CPN-TOOLS cpn specifications. New functionality includes a more user friendly modeling primitives, a simplification and refactorization of the original NormNets meta-model plus a conversion of whole project to a Maven oriented one.

Relevant parts of the project are:

- Normnetsed. This folder contains the metamodel definition plus some modeling examples to test the resulting editor.
- Tranformation. This folder contains the software that transforms a specification into a CPN declaration. 

To run this software you will need a full setup of Maven, ANT, and JDK 1.7. The development is a multiplatform one, and includes a piece of CPN-Tools software. CPN-Tools is not distributed for linux and it is not uploaded to Maven Central. Prior to run the transformation, readers will have to follow setup guidelines as described in the Transformation folder.  


## Instructions

The project has to be installed first to satisfy all dependencies. The transformation part requires additional actions, in particular, installing cpn-tools following the instructions. So, first of all, go to the setup folder and run

	**$ sh install-all.sh**

In windows,

	**$ sh install-all.bat**

Then, perform

	**$ mvn clean install**


Afterwards, depending on the goal, different options a presented:

- if the goal is to modeling some norms, you can use the **normnetsed/example** folder as a template for producing specifications of normative systems. In this folder, specifications are stored in the **src/main/spec/specification.xml** file. 
- if the goal is to enrigh the norm metamodel to experiment with new constructs, go to the **normnetsed/editor**, open the metamodel specification with **ant edit**, save it, and return to the command line to perform a **mvn clean install**. Be advised that changes in the meta-model may invalidate either the transformation procedure or preventing to load models created with previous versions. Restoring the meta-model or using an older version of the editor may provide access to the old information, though, to make them accesible through the new editor, a manual editing of the specification file may be needed. 
- if the goal is to transform built models into CPN specification files compatible with CPN-tools, then move to the **transformation** folder and follow the instructions to either invoke an editor or to launch the transformation over a particular file. 

## License


This software is distributed under the GPLV3 license

CPN-TOOls is a software distributed under GPLV2 license. 

http://cpntools.org/license/start

## Requirements

- Java 1.7 (http://java.sun.com). set environment variable JAVA_HOME to the corresponding JAVA install folder. WARNING:  the JRE is not enough, you need the full JSE
- Maven 3.1.1+ installed (http://maven.apache.org/download.html). Then set environment variable M2_HOME to the Maven install foder.
- Ant (http://ant.apache.org). Then set environment variable ANT_HOME to the Ant install folder. 
- Add binaries to environment variable PATH (In linux, this can be done from command line with something like PATH=$PATH:$HOME/bin:$JAVA_HOME/bin:$M2_HOME/bin)

## Acknowledgements

This adaptation was made by Jorge J. Gómez Sanz.

It was funded by the SociAAL project (http://grasia.fdi.ucm.es/sociaal), grant TIN2011-28335-C02-01 (Ministry of Economy and Competiveness,Spain) 

Raw material was provided by TU Delft, on behalf Dr. H.M. Aldewereld


