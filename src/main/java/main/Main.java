package main;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.lang.PipedRDFIterator;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.lang.PipedRDFIterator;
import org.apache.jena.riot.lang.PipedRDFStream;
import org.apache.jena.riot.lang.PipedTriplesStream;
import parser.Parser;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    @Parameter(names={"--owlFile", "-owl"})
    private String owlFile;

    @Parameter(names={"--datasetFile", "-d"})
    private String datasetFile;

    @Parameter(names={"--outputFile", "-o"})
    private String outputFile;

    public void run() throws Exception {
        try {
            if (owlFile == null || datasetFile==null || outputFile==null){
                System.out.println("Specify all the arguments -owl, -d, -o");
                return;
            }
                Model modelDataset = ModelFactory.createDefaultModel();
                modelDataset.read(new File(datasetFile).toURI().toString(), "N-TRIPLE");
                StmtIterator iteratorDataset;

                Model modelOwl = ModelFactory.createDefaultModel();
                modelOwl.read(new File(owlFile).toURI().toString(), "N-TRIPLE");
                StmtIterator iteratorOwl = modelOwl.listStatements();

                File file=new File(outputFile);
                int i=0;
                while (iteratorOwl.hasNext())
                {
                    i++;
                    Statement statementOwl = iteratorOwl.nextStatement();
                    Resource subjectOwl = statementOwl.getSubject(); //wikidata link
                    RDFNode objectOwl = statementOwl.getObject(); //osm or musicbrainz link
                    iteratorDataset = modelDataset.listStatements();
                    while (iteratorDataset.hasNext())
                    {
                        Statement statementDataset = iteratorDataset.nextStatement();
                        if (statementDataset.getSubject().equals(objectOwl.asResource()))
                        {
                            Property property = statementDataset.getPredicate();
                            RDFNode object = statementDataset.getObject();
                            iteratorDataset.remove();
                            System.out.println(subjectOwl + " " + property + "     " + object);
                            Statement st = ResourceFactory.createStatement(subjectOwl, property, object);
                            modelDataset.add(st);
                        }
                    }
                    System.out.println("iteration: "+i);
                }
                OutputStream outputStream = new FileOutputStream(outputFile);
                modelDataset.write(outputStream, "N-TRIPLE");
                outputStream.close();
            }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    public static void main(String[] argv) throws Exception{
//        Main main = new Main();
//        JCommander.newBuilder()
//                .addObject(main)
//                .build()
//                .parse(argv);
//        main.run();
        Parser parser=new Parser();
        PipedRDFIterator<Triple> iterator=parser.parse("dataset.nt");
    }
}
