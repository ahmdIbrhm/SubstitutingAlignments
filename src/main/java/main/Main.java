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
import utility.Utility;

import java.io.*;

public class Main {
    @Parameter(names={"--owlFile", "-owl"})
    private String owlFile;

    @Parameter(names={"--datasetFile", "-d"})
    private String datasetFile;

    @Parameter(names={"--outputFile", "-o"})
    private String outputFile;

    @Parameter(names={"--type", "-t"})
    private String typeOfDataset;

    public void run() throws Exception {
        try {
            int numberOfLinks = 0;
            if (owlFile != null && datasetFile != null && outputFile != null)
            {
                BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
                HDT owlHdt=HDTManager.mapIndexedHDT(owlFile);
                if (typeOfDataset.equals("ntriples"))
                {
                    PipedRDFIterator<Triple> iteratorDataset = Parser.parse(datasetFile);
                    while (iteratorDataset.hasNext())
                    {
                        Triple tripleDataset = iteratorDataset.next();
                        Node subjectDataset = tripleDataset.getSubject();
                        Node objectDataset = tripleDataset.getObject();
                        Node predicateDataset = tripleDataset.getPredicate();
                        IteratorTripleString iteratorOwl= owlHdt.search("", "", subjectDataset.toString());
                        if (iteratorOwl.hasNext())
                        {
                            numberOfLinks++;
                            inHash(writer,predicateDataset,objectDataset,iteratorOwl.next().getSubject().toString());
                        }
                        else {
                            notInHash(writer,subjectDataset,predicateDataset,objectDataset);
                        }
                    }
                }
                else if (typeOfDataset.equals("hdt"))
                {
                    System.out.println("HDT type");
                    HDT hdt = HDTManager.mapIndexedHDT(datasetFile, null);
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
                        System.out.println(subjectDataset.toString());
//                        IteratorTripleString iteratorOwl= owlHdt.search("", "", subjectDataset.toString());
//                        if (iteratorOwl.hasNext())
//                        {
//                            numberOfLinks++;
//                            TripleString triple = iteratorOwl.next();
//                            System.out.println(triple.toString());
//                            inHash(writer,predicateDataset,objectDataset,triple.getSubject().toString());
//                        }
//                        else
//                        {
//                            notInHash(writer,subjectDataset,predicateDataset,objectDataset);
//                        }
                    }
                }
                System.out.println("Finished");
                System.out.println("Number of substituted links: " + numberOfLinks);
                writer.close();
                System.exit(0);
            }

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

    }
    private static void inHash(BufferedWriter writer,Node predicateDataset,Node objectDataset,String newSubject)
    {
        try {

            if (objectDataset.isURI()) {
                writer.write("<" + newSubject + "> <" + predicateDataset + "> <" + objectDataset + "> .\n");
            } else if (objectDataset.isLiteral()) {
                String string = objectDataset.getLiteral().getValue().toString();
                Node nodeString = Utility.createLiteral(string);
                String language = objectDataset.getLiteralLanguage();
                String dataType = objectDataset.getLiteralDatatypeURI();

                if (!language.trim().equals("")) {
                    writer.write("<" + newSubject + "> <" + predicateDataset + "> " + nodeString + "@" + language + ".\n");
                } else if (!dataType.trim().equals("")) {
                    writer.write("<" + newSubject + "> <" + predicateDataset + "> " + nodeString + "^^<" + dataType + "> .\n");
                } else {
                    writer.write("<" + newSubject + "> <" + predicateDataset + "> " + nodeString + " .\n");
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    private static void notInHash(BufferedWriter writer,Node subjectDataset,Node predicateDataset,Node objectDataset)
    {
        try
        {
            if (objectDataset.isURI())
            {
                writer.write("<" + subjectDataset + "> <" + predicateDataset + "> <" + objectDataset + "> .\n");
            }
            else if (objectDataset.isLiteral()) {
                String string = objectDataset.getLiteral().toString();
                Node nodeString = Utility.createLiteral(string);
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
