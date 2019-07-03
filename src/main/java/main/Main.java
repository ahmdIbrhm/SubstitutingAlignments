package main;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
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
            if(owlFile!=null && datasetFile!=null && outputFile!=null ) {
                BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
                BufferedReader readerDataset = new BufferedReader(new FileReader(datasetFile));
                String lineDataset;
                while ((lineDataset = readerDataset.readLine()) != null) {
                    String[] tripleDataset = lineDataset.split(" ");
                    String subjectDataset = tripleDataset[0];
                    String predicateDataset = tripleDataset[1];
                    String objectDataset = tripleDataset[2];
                    boolean found = false;
                    BufferedReader readerOwl = new BufferedReader(new FileReader(owlFile));
                    String lineOwl;
                    while ((lineOwl = readerOwl.readLine()) != null) {
                        String[] tripleOwl = lineOwl.split(" ");
                        String subjectOwl = tripleOwl[0];
                        String objectOwl = tripleOwl[2];

                        if (subjectDataset.equals(subjectOwl)) {
                            writer.write(objectOwl + " " + predicateDataset + " " + objectDataset + " .\n");
                            found = true;
                            break;
                        } else if (subjectDataset.equals(objectOwl)) {
                            writer.write(subjectOwl + " " + predicateDataset + " " + objectDataset + " .\n");
                            found = true;
                            break;
                        }
                    }
                    readerOwl.close();
                    if (found == false) {
                        writer.write(subjectDataset + " " + predicateDataset + " " + objectDataset + " .\n");
                    }
                }
                readerDataset.close();
                writer.close();
            }
            else
            {
                System.out.println("Error in parameters");
            }

//            if(owlFile!=null && datasetFile!=null && outputFile!=null) {
//                BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
//                PipedRDFIterator<Triple> iteratorDataset = Parser.parse("/home/ahmad/IdeaProjects/SubstitutingAlignments/dataset.nt");
//                System.out.println(iteratorDataset.hasNext());
//                while (iteratorDataset.hasNext()) {
//                    Triple tripleDataset = iteratorDataset.next();
//                    Node subjectDataset = tripleDataset.getSubject();
//                    Node objectDataset = tripleDataset.getObject();
//                    Node predicateDataset = tripleDataset.getPredicate();
//                    boolean found = false;
//                    PipedRDFIterator<Triple> iteratorOwl = Parser.parse(owlFile);
//                    while (iteratorOwl.hasNext()) {
//                        Triple tripleOwl = iteratorOwl.next();
//                        Node subjectOwl = tripleOwl.getSubject();
//                        Node objectOwl = tripleOwl.getObject();
//                        if (subjectDataset.equals(subjectOwl)) {
//                            writer.write("<" + objectOwl + "> <" + predicateDataset + "> <" + objectDataset + "> .\n");
//                            found = true;
//                            break;
//                        } else if (subjectDataset.equals(objectOwl)) {
//                            writer.write("<" + subjectOwl + "> <" + predicateDataset + "> <" + objectDataset + "> .\n");
//                            found = true;
//                            break;
//                        }
//                    }
//                    if (found == false) {
//                        writer.write("<" + subjectDataset + "> <" + predicateDataset + "> <" + objectDataset + "> .\n");
//                    }
//                }
//                System.out.println("Finished");
//                writer.close();

//                System.exit(0);
//            }
//            else {
//                System.out.println("Wrong parameters");
//            }
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
