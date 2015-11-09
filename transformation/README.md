
This program transforms from NormNets semi-formalism into a CPN-Tools specification that can be later used with CPN.

To retrieve help, type:

	**$  mvn -q exec:java -Dexec.mainClass=normnet.transform.NormnetsToCPNTransformation**

The coding includes a number of samples to test the transformation. Samples, can be edited with the NormNet editor

	**$ mvn -q exec:java -Dexec.mainClass=ingenias.editor.IDE -Dexec.args="src/test/resources/example1.xml"**

To run a transformation over a particular NormNet specification file, type

	**$ mvn -q exec:java -Dexec.mainClass=normnet.transform.NormnetsToCPNTransformation -Dexec.args="src/test/resources/example1.xml"**

To save it to file, for instance target/example1.cpn, try

	**$ mvn -q exec:java -Dexec.mainClass=normnet.transform.NormnetsToCPNTransformation -Dexec.args="src/test/resources/example1.xml target/example1.cpn"**

## License


This software is distributed under the GPLV3 license

CPN-TOOls is a software distributed under GPLV2 license. 

http://cpntools.org/license/start


## Acknowledgements

This adaptation was made by Jorge J. Gómez Sanz over an original work from Jie Jiang, and advised by Dr. H.M. Aldewereld (TU Delft)

It was funded by the SociAAL project (http://grasia.fdi.ucm.es/sociaal), grant TIN2011-28335-C02-01 (Ministry of Economy and Competiveness,Spain) 

Raw material was provided by TU Delft, on behalf Dr. H.M. Aldewereld Dr. H.M. Aldewereld (TU Delft)
