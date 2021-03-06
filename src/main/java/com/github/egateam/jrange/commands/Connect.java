/**
 * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED
 * REPRESENTATIONS AND WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY DISCLAIMED.
 */

package com.github.egateam.jrange.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.github.egateam.commons.Utils;
import com.github.egateam.jrange.util.StaticUtils;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings({"CanBeFinal"})
@Parameters(commandDescription = "(Unfinished) Connect range links in paralog graph")
public class Connect {

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @Parameter(description = "<infiles>", required = true)
    private List<String> files;

    @Parameter(names = {"--outfile", "-o"}, description = "Output filename. [stdout] for screen.")
    private String outfile;

    private void validateArgs() throws Exception {
        if ( files.size() < 1 ) {
            throw new ParameterException("This command need one or more input files.");
        }

        for ( String inFile : files ) {
            if ( inFile.toLowerCase().equals("stdin") ) {
                continue;
            }
            if ( !new File(inFile).isFile() ) {
                throw new IOException(String.format("The input file [%s] doesn't exist.", inFile));
            }
        }

        if ( outfile == null ) {
            outfile = files.get(0) + ".replace.txt";
        }
    }

    public void execute() throws Exception {
        validateArgs();

        //----------------------------
        // Loading
        //----------------------------

        // store entire graph
        // edge weight represent strands:
        //      1 for  "+"
        //      -1 for "-"
        SimpleWeightedGraph<String, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

        for ( String inFile : files ) {
            List<String> lines = Utils.readLines(inFile);

            for ( String line : lines ) {
                String parts[] = line.split("\\t");
                int    count   = parts.length;
                if ( !(count == 2 || count == 3) ) {
                    continue;
                }

                String hitStrand;
                if ( count == 2 ) {
                    hitStrand = "+";
                } else {
                    hitStrand = parts[2];
                }

                // skip self links
                if ( Objects.equals(parts[0], parts[1]) ) {
                    continue;
                }

                // add vertexes
                for ( int i : new int[]{0, 1} ) {
                    graph.addVertex(parts[i]);
                }

                // add edge and set weight
                if ( !graph.containsEdge(parts[0], parts[1]) ) {
                    double weight = Objects.equals(hitStrand, "+") ? 1 : -1;
                    DefaultWeightedEdge edge = graph.addEdge(parts[0], parts[1]);
                    System.err.println(edge);
                    graph.setEdgeWeight(edge, weight);
                }

            }
        }

        // cc
        System.out.println(graph);

        //----------------------------
        // Output
        //----------------------------
//        Utils.writeLines(outfile, lines);
    }
}
