package main;


import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.lang.PipedRDFIterator;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTFactory;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.triples.*;
import org.rdfhdt.hdtjena.NodeDictionary;
import parser.Parser;

import java.io.*;
import java.util.HashMap;

public class Main {
    @Parameter(names={"--owlFile", "-owl"})
    private String owlFile;

    @Parameter(names={"--datasetFile", "-d"})
    private String datasetFile;

    @Parameter(names={"--outputFile", "-o"})
    private String outputFile;

    public void run() throws Exception {
        try {
            int numberOfLinks=0;
            if (owlFile != null && datasetFile != null && outputFile != null)
            {
                HashMap<String,String> owlHashmap=new HashMap<>();
                PipedRDFIterator<Triple> iteratorOwl= Parser.parse(owlFile);
                while (iteratorOwl.hasNext())
                {
                    Triple triple=iteratorOwl.next();
                    Node subjectOwl=triple.getSubject();
                    Node objectOwl=triple.getObject();
                    owlHashmap.put(objectOwl.toString(),subjectOwl.toString());
                }

                System.out.println(owlHashmap);
                BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
                PipedRDFIterator<Triple> iteratorDataset = Parser.parse(datasetFile);
                while (iteratorDataset.hasNext()) {
                    Triple tripleDataset = iteratorDataset.next();
                    Node subjectDataset = tripleDataset.getSubject();
                    Node objectDataset = tripleDataset.getObject();
                    Node predicateDataset = tripleDataset.getPredicate();
                    if (owlHashmap.containsKey(subjectDataset.toString())) {
                        numberOfLinks++;
                        writer.write("<" + owlHashmap.get(subjectDataset.toString())+ "> <" + predicateDataset + "> <" + objectDataset + "> .\n");
                    }
                    else {
                        writer.write("<" + subjectDataset + "> <" + predicateDataset + "> <" + objectDataset + "> .\n");
                    }
                }
                System.out.println("Finished");
                System.out.println("Number of substituted links: "+numberOfLinks);

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
