/*
	This file is part of DuDe, the Duplication Detector.
	Copyright (C) 2013 José Orlando Pereira.

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
 */

package pt.haslab.dude;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.rabinfingerprint.polynomial.Polynomial;

public class Tool {
	public static void main(String[] args) {
		Options options = new Options();
		options.addOption("s", "minshare", true, "threshold for similarity (% of file sizes, default=10)");
		options.addOption("c", "minchunk", true, "minimum chunk considered (# of bytes, default=10)");
		options.addOption("g", "graph", false, "output in GraphViz (.dot) format (>2 files)");
		options.addOption("d", "detail", false, "show common text found");
		
		try {	
			int minchunk = 10, minshare = 10;
			boolean graph = false, detail = false;

			CommandLineParser parser = new BasicParser();
			CommandLine cmd = parser.parse(options, args);
			
			if (cmd.hasOption('s')) minshare = Integer.parseInt(cmd.getOptionValue('s'));
			if (cmd.hasOption('c')) minchunk = Integer.parseInt(cmd.getOptionValue('c'));
			if (cmd.hasOption('g')) graph = true;
			if (cmd.hasOption('d')) detail = true;
			
			String[] files = cmd.getArgs();

			if (files.length<2) throw new ParseException("I need at least two files");			

			if (files.length == 2 && graph) throw new ParseException("need >2 files to output graph");			

			if (files.length > 2 && detail) throw new ParseException("detail shown only for 2 files");			

			Polynomial polynomial = Polynomial.createIrreducible(53);
			
			List<FileInfo> bigger = new ArrayList<FileInfo>();
			for(String s: files)
				bigger.add(new FileInfo(s, bigger, polynomial, detail, minchunk, minshare));
						
			for(FileInfo fi: bigger) {
				fi.compute();
				if (!detail && !graph)
					fi.dumpSummary(System.out);
			}

			if (graph) {
				System.out.println("graph dupls {");
				for(FileInfo fi: bigger)
					fi.dumpEdges(System.out);
				System.out.println("}");
			}
			
			return;
		} catch (ParseException e) {
			System.err.println("invalid options: "+e.getMessage());
		} catch (FileNotFoundException e) {
			System.err.println(e.getMessage());
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}

		HelpFormatter formatter = new HelpFormatter();
		formatter.setWidth(78);
		formatter.printHelp("java -jar dude.jar [options] file1 file2 [ ... fileN ]",
				"DuDe is a duplication detector for text files.", options,
				"For more information: http://github.com/jopereira/dude");
	}

}
