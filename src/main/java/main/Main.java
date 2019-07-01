package main;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.apache.jena.rdf.model.*;
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
            if (owlFile == null || datasetFile==null || outputFile==null){
                System.out.println("Specify all the arguments -owl, -d, -o");
                return;
            }
                Model modelDataset = ModelFactory.createDefaultModel();
                modelDataset.read(datasetFile, "N-TRIPLE");
                StmtIterator iteratorDataset;

                Model modelOwl = ModelFactory.createDefaultModel();
                modelOwl.read(owlFile, "N-TRIPLE");
                StmtIterator iteratorOwl = modelOwl.listStatements();
                while (iteratorOwl.hasNext()) {
                    Statement statementOwl = iteratorOwl.nextStatement();
                    Resource subjectOwl = statementOwl.getSubject(); //wikidata link
                    RDFNode objectOwl = statementOwl.getObject(); //osm or musicbrainz link
                    iteratorDataset = modelDataset.listStatements();
                    while (iteratorDataset.hasNext()) {
                        Statement statementDataset = iteratorDataset.nextStatement();
                        if (statementDataset.getSubject().equals(objectOwl.asResource())) {
                            Property property = statementDataset.getPredicate();
                            RDFNode object = statementDataset.getObject();
                            iteratorDataset.remove();
                            System.out.println(subjectOwl + " " + property + "     " + object);
                            Statement st = ResourceFactory.createStatement(subjectOwl, property, object);
                            modelDataset.add(st);
                        }
                    }
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
        Main main = new Main();
        JCommander.newBuilder()
                .addObject(main)
                .build()
                .parse(argv);
        main.run();
    }
}
