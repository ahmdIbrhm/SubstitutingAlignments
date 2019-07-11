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
import utility.Utility;

import java.io.*;
import java.util.HashMap;

public class Main {
    @Parameter(names={"--owlFiles", "-owl"})
    private String owlFiles;

    @Parameter(names={"--datasetFile", "-d"})
    private String datasetFile;

    @Parameter(names={"--outputFile", "-o"})
    private String outputFile;

    @Parameter(names={"--type", "-t"})
    private String typeOfDataset;

    public void run() throws Exception {
        try {
            int numberOfLinks = 0;
            if (owlFiles != null && datasetFile != null && outputFile != null)
            {
                String[] owls = owlFiles.split(",");
                HashMap<String, String> owlHashmap = new HashMap<>();
                for (int i = 0; i < owls.length; i++) {
                    PipedRDFIterator<Triple> iteratorOwl = Parser.parse(owls[i]);
                    while (iteratorOwl.hasNext()) {
                        Triple triple = iteratorOwl.next();
                        Node subjectOwl = triple.getSubject();
                        Node objectOwl = triple.getObject();
                        owlHashmap.put(objectOwl.toString(), subjectOwl.toString());
                    }
                }
                BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

                if (typeOfDataset.equals("ntriples"))
                {
                    PipedRDFIterator<Triple> iteratorDataset = Parser.parse(datasetFile);
                    while (iteratorDataset.hasNext()) {
                        Triple tripleDataset = iteratorDataset.next();
                        Node subjectDataset = tripleDataset.getSubject();
                        Node objectDataset = tripleDataset.getObject();
                        Node predicateDataset = tripleDataset.getPredicate();
//                    System.out.println(objectDataset);
                        if (owlHashmap.containsKey(subjectDataset.toString())) {
                            numberOfLinks++;
                            inHash(writer,subjectDataset,predicateDataset,objectDataset,owlHashmap);

                        }
                        else {
                            notInHash(writer,subjectDataset,predicateDataset,objectDataset,owlHashmap);
                        }
                    }
                    System.out.println("Finished");
                    System.out.println("Number of substituted links: " + numberOfLinks);

                    writer.close();
                    System.exit(0);
                }
                else if (typeOfDataset.equals("hdt")) {
                    System.out.println("HDT type");
                    HDT hdt = HDTManager.mapIndexedHDT(datasetFile, null);
                    int nObjects= (int) hdt.getDictionary().getNsubjects();
                    NodeDictionary nodeDictionary = new NodeDictionary(hdt.getDictionary());
                    IteratorTripleID iter = hdt.getTriples().search(new TripleID(0, 0, 0));
                    while (iter.hasNext())
                    {
                        TripleID tripleId=iter.next();
                        long subjectId=tripleId.getSubject();
                        long predicateId=tripleId.getPredicate();
                        long objectId=tripleId.getObject();

                        Node subjectDataset = nodeDictionary.getNode(subjectId, TripleComponentRole.SUBJECT);
                        Node predicateDataset = nodeDictionary.getNode(predicateId, TripleComponentRole.PREDICATE);
                        Node objectDataset = nodeDictionary.getNode(objectId, TripleComponentRole.OBJECT);
                        if (owlHashmap.containsKey(subjectDataset.toString()))
                        {
                            numberOfLinks++;
                            inHash(writer,subjectDataset,predicateDataset,objectDataset,owlHashmap);
                        }
                        else
                        {
                            notInHash(writer,subjectDataset,predicateDataset,objectDataset,owlHashmap);
                        }
                    }
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    public static void inHash(BufferedWriter writer,Node subjectDataset,Node predicateDataset,Node objectDataset,HashMap<String,String> owlHashmap)
    {
        try {

            if (objectDataset.isURI()) {
                writer.write("<" + owlHashmap.get(subjectDataset.toString()) + "> <" + predicateDataset + "> <" + objectDataset + "> .\n");
            } else if (objectDataset.isLiteral()) {
                Utility utility = new Utility();
                String string = objectDataset.getLiteral().getValue().toString();
                Node nodeString = utility.createLiteral(string);
                String language = objectDataset.getLiteralLanguage();
                String dataType = objectDataset.getLiteralDatatypeURI();

                if (!language.trim().equals("")) {
                    writer.write("<" + subjectDataset + "> <" + predicateDataset + "> " + nodeString + "@" + language + ".\n");
                } else if (!dataType.trim().equals("")) {
                    writer.write("<" + subjectDataset + "> <" + predicateDataset + "> " + nodeString + "^^<" + dataType + "> .\n");
                } else {
                    writer.write("<" + subjectDataset + "> <" + predicateDataset + "> " + nodeString + " .\n");
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    public static void notInHash(BufferedWriter writer,Node subjectDataset,Node predicateDataset,Node objectDataset,HashMap<String,String> owlHashmap)
    {
        try
        {
            if (objectDataset.isURI()) {
                writer.write("<" + subjectDataset + "> <" + predicateDataset + "> <" + objectDataset + "> .\n");
            } else if (objectDataset.isLiteral()) {
                Utility utility = new Utility();
                String string = objectDataset.getLiteral().toString();
                System.out.println(string);
                Node nodeString = utility.createLiteral(string);
//                            System.out.println(nodeString);
                String language = objectDataset.getLiteralLanguage();
                String dataType = objectDataset.getLiteralDatatypeURI();

                if (!language.trim().equals("")) {
                    writer.write("<" + subjectDataset + "> <" + predicateDataset + "> " + nodeString + "@" + language + ".\n");
                } else if (!dataType.trim().equals("")) {
                    writer.write("<" + subjectDataset + "> <" + predicateDataset + "> " + nodeString + "^^<" + dataType + "> .\n");
                } else {
                    writer.write("<" + subjectDataset + "> <" + predicateDataset + "> " + nodeString + " .\n");
                }

            }
        }
        catch (Exception e)
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
