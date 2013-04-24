DuDe, the duplication detector for text files
=============================================

DuDe finds duplicate segments in text, even when it has been edited 
and reformatted, namely, by inserting or removing words, or by 
breaking lines.

Given a set o text files, it lists the total number of characters in 
shared segments for file pairs that exceed a minimum sharing 
threshold.

This can be presented as a graph, with one node for each file and one 
edge for each pair found. The shared portions for each pair can also 
be shown in detail.

This works only in text files. Other file types, such as .doc or .pdf 
need first to be converted to plain text.

Building and running
--------------------

To build use `mvn package`. To run, use `java -jar target/dude-
<version>-jar-with-dependencies.jar`. Running it without 
any arguments presents a brief help on command line syntax.

How does it work
----------------

DuDe is a quick hack. It uses Rabin fingerprints to chop input
files in variable sized chunks and Bloom filters for indexing.
The following excellent libraries do the heavy lifting:
 
 * https://github.com/themadcreator/rabinfingerprint
 * http://code.google.com/p/guava-libraries/

Source code
-----------

Latest source is available at http://github.com/jopereira/dude

License
-------

DuDe is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

DuDe is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with DuDe.  If not, see <http://www.gnu.org/licenses/>.

