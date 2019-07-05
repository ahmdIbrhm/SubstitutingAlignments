package main;


import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.lang.PipedRDFIterator;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.triples.*;
import org.rdfhdt.hdtjena.NodeDictionary;
import parser.Parser;

import java.io.*;

public class Main {
    @Parameter(names={"--owlFile", "-owl"})
    private String owlFile;

    @Parameter(names={"--datasetFile", "-d"})
    private String datasetFile;

    @Parameter(names={"--outputFile", "-o"})
    private String outputFile;

    public void run() throws Exception {
        try {
            if (owlFile != null && datasetFile != null && outputFile != null)
            {
                BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
                PipedRDFIterator<Triple> iteratorDataset = Parser.parse(datasetFile);
                while (iteratorDataset.hasNext()) {
                    Triple tripleDataset = iteratorDataset.next();
                    Node subjectDataset = tripleDataset.getSubject();
                    Node objectDataset = tripleDataset.getObject();
                    Node predicateDataset = tripleDataset.getPredicate();
                    boolean found = false;

                    HDT hdt= HDTManager.mapIndexedHDT("owl.hdt",null);
                    System.out.println("ToString: "+subjectDataset.toString());
                    IteratorTripleString iteratorOwl= hdt.search("","",subjectDataset.toString());
                    while (iteratorOwl.hasNext()) {
                        TripleString tripleOwl = iteratorOwl.next();
                        String subjectOwl = tripleOwl.getSubject().toString();
                        String  objectOwl = tripleOwl.getObject().toString();
                        if (subjectDataset.equals(subjectOwl)) {
                            writer.write("<" + objectOwl + "> <" + predicateDataset + "> <" + objectDataset + "> .\n");
                            found = true;
                            break;
                        } else if (subjectDataset.equals(objectOwl)) {
                            writer.write("<" + subjectOwl + "> <" + predicateDataset + "> <" + objectDataset + "> .\n");
                            found = true;
                            break;
                        }
                    }
                    if (found == false) {
                        writer.write("<" + subjectDataset + "> <" + predicateDataset + "> <" + objectDataset + "> .\n");
                    }
                }
                System.out.println("Finished");
                writer.close();
                System.exit(0);
            }
            else {
                System.out.println("Wrong parameters");
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    public static void main(String[] argv) throws Exception {
        Main main = new Main();
        JCommander.newBuilder()
                .addObject(main)
                .build()
                .parse(argv);
        main.run();
    }
}
