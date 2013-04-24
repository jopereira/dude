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

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import org.rabinfingerprint.fingerprint.RabinFingerprintLongWindowed;
import org.rabinfingerprint.polynomial.Polynomial;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;
import com.google.common.io.ByteStreams;

public class FileInfo {
	private BloomFilter<String> bloom;
	private String name;
	private int seq;
	private AliasInfo[] stats;
	private Polynomial polynomial;
	private int size;
	private boolean detail;
	private int minshare, minchunk;
	
	private class AliasInfo {
		private FileInfo target;
		private boolean last;
		private int pos;
		private String found;
		private int fline, iline;
		
		public AliasInfo(FileInfo fileInfo) {
			this.target = fileInfo;
			pos = 0;
			found = "";
		}

		public void test(String s, int line) {
		    boolean r = target.bloom.mightContain(s);
		    if (r) {
		    	found += s;
		    	fline = line;
		    	if (!last) {
		    		iline = line;
		    	}
		    } else if (last) {
		    	pos+=found.length();
		    	if (detail && found.length()>minchunk) {
		    		System.out.println("---------------------");
		    		System.out.println("["+iline+"-"+fline+"] " +found);
		    	}
		    	found = "";
		    }
		    last = r;	
		}
		
		public int match() {
			return pos;
		}
	};
	
	public FileInfo(String name, List<FileInfo> bigger, Polynomial polynomial, boolean detail, int minchunk, int minshare) {
		this.name = name;
		this.polynomial = polynomial;
		this.detail = detail;
		this.minchunk = minchunk;
		this.minshare = minshare;
		this.seq = bigger.size()+1;
			
		stats = new AliasInfo[bigger.size()];
		for(int i = 0; i < stats.length; i++)
			stats[i] = new AliasInfo(bigger.get(i));
	
		bloom = BloomFilter.create(new Funnel<String>() {
			public void funnel(String arg0, PrimitiveSink arg1) {
				arg1.putString(arg0);
			}
		}, 1000000);
	}

	public void compute() throws FileNotFoundException, IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		RabinFingerprintLongWindowed window = new RabinFingerprintLongWindowed(polynomial, 16);
		int line = 1;
		for (byte b : ByteStreams.toByteArray(new FileInputStream(name))) {
			if (b == 10) { b = 32; line++; }
		    window.pushByte(b);
		    long l = window.getFingerprintLong();
		    baos.write(b);
		    if (l % 0xff < 10) {
		    	String s = new String(baos.toByteArray());
		    	size += s.length();
		    	bloom.put(s);
		    	for(AliasInfo st: stats)
		    		st.test(s, line);
		    	baos = new ByteArrayOutputStream();
		    }
		}
	}
	
	private static String disp(String fn) {
		return fn.substring(0,  fn.length()-4);
	}

	public void dumpEdges(PrintStream out) {
		out.println("  f"+seq+" [label=\""+disp(name)+" ("+size+")\"];");
		for(AliasInfo st: stats) {
			int m = st.match();
			if (m*100>size*minshare || m*100>st.target.size*minshare)
				out.println("  f"+seq+"--f"+st.target.seq+" [label=\""+m+"\"];");
		}
	}
	
	public void dumpSummary(PrintStream out) {
		for(AliasInfo st: stats) {
			int m = st.match();
			if (m*100>size*minshare || m*100>st.target.size*minshare)
				out.println(st.pos+" "+name+" "+st.target.name);		
		}
	}
}
