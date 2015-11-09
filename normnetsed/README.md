This project contains a meta-model specification for the NormsNet formalism based on previous work from Jie Jiang (TU Delft) with the Eclipse Modeling Framework. The metamodel was refactored to be more user friendly while still keeping its expresiveness. The notation used to define the visual modeling language is inspired on GOPRR. 

Examples of use of the metamodel are within the folder **examples**. The metamodel itself is in the folder **editor**. Before using the examples, the editor has to be installed. If a **mvn clean install** was previously executed from the parent folder, this is already obtained. Otherwise, execute the **mvn clean install**

With the current metamodel, it is possible to model the examples included in the Jie Jiang's original PhD.

http://dx.doi.org/10.4233/uuid:d02f642d-f797-4eab-a8b1-126b7d110431

Further additions are possible if the meta-model is changed. To modify the meta-model, go to the **normnetsed/editor**, open the metamodel specification with **ant edit**, save it, and return to the command line to perform a **mvn clean install**. 

Be advised that changes in the meta-model may invalidate either the transformation procedure or preventing to load models created with previous versions. Restoring the meta-model or using an older version of the editor may provide access to the old information, though, to make them accesible through the new editor, a manual editing of the specification file may be needed. 

Other possibilities are listed below.

#Required software
This template requires JDK, Maven, and Ant installed. Instructions for installing them can be found in their corresponding sites:

- Java. http://java.sun.com
- Ant.  http://ant.apache.org
- Maven. http://maven.apache.org
- CPN-Tools. http://cpntools.org/documentation/install_cpn_tools

#Configuring environment variables
The most troublesome part for installing the software is properly configuring the environment variables, specially for maven.  

Relevant environment variables to have are:
* JAVA_HOME. It will point to the JDK install folder. JRE is not enough, the full JDK is needed.
* M2_HOME. The home folder for maven. In ubuntu, it is /usr/share/maven. In windows, it will depend where you installed it

Also, it is necessary to include JDK, Maven, and Ant binaries into the PATH environment variable.

#Installing/using the template

Copy or fork this template and go to the main folder. From there, run
```Shell
	mvn clean install
```

To modify the visual language, open the meta-model editor and proceed to create new entities and attach them to the diagram, as in the example

```Shell
	cd editor
	ant edit
```

Produce a self-contained editor, run the following, from the main folder

```Shell
	cd editor
	mvn clean install

```
In the editor/target folder you will find several jars:

- *editor-1.0.0-SNAPSHOT-installer.jar*. This is an installer to distribute among your colleagues. It creates a folder where the jar executable is copied. It also provides instructions and license warnings.  
- *editor-1.0.0-SNAPSHOT-selfcontained.jar*. It is a self-contained file that can be distributed and used directly. If JDK is properly installed, a double click on this file from a file explorer ought to open the resulting editor. If not, from command line, a *java editor-1.0.0-SNAPSHOT-selfcontained.jar*  will do the trick.

The previous instructions allow to start creating models followign the constraints of your visual modeling language. However, it is more disciplined to create those examples as modules of the development. Also, it helps to convey a development process where the modeling language has a very well defined role. The example reuses directly the installed editor, if the developer previosly did a *mvn install* at some point. Working with the example, is easy. To open a specification with the generated editor:

```Shell
	cd example
	ant edit
```


#Creating the site
Documenting your visual language is a basic task. The project is prepared to produce sites containing documentation of your project. Description fields in the specification are processed as markdown code. The result will be added to the resulting site.

Run the following from your main project folder
```Shell
	mvn clean site site:deploy
```
The generated site will be in your target/finalsite folder.

For Ubuntu 14.04, if you find this error:
```
	[ERROR] Number of foreign imports: 1
	[ERROR] import: Entry[import  from realm ClassRealm[maven.api, parent: null]]
	[ERROR] 
	[ERROR] -----------------------------------------------------:
 org.apache.commons.lang.StringUtils
```

Try the following workaround
```Shell
	cd /usr/share/maven/lib
	sudo ln -s ../../java/commons-lang.jar .
```
## License


This software is distributed under the GPLV3 license

CPN-TOOls is a software distributed under GPLV2 license. 

http://cpntools.org/license/start


#Acknowledgements

This adaptation was made by Jorge J. Gómez Sanz.

It was funded by the SociAAL project (http://grasia.fdi.ucm.es/sociaal), grant TIN2011-28335-C02-01 (Ministry of Economy and Competiveness,Spain) 


