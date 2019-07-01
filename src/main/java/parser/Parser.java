package parser;

import org.apache.jena.graph.Triple;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.lang.PipedRDFIterator;
import org.apache.jena.riot.lang.PipedRDFStream;
import org.apache.jena.riot.lang.PipedTriplesStream;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Parser
{
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public PipedRDFIterator<Triple> parse(String dump)
    {
        System.out.println("Dump to parse " + dump);
        PipedRDFIterator<Triple> iter = new PipedRDFIterator<Triple>();
        final String d = dump;
        final PipedRDFStream<Triple> inputStream = new PipedTriplesStream(iter);
        // PipedRDFStream and PipedRDFIterator need to be on different threads

        // Create a runnable for our parser thread
        Runnable parser = new Runnable() {
            @Override
            public void run() {
                // Call the parsing process.
                RDFDataMgr.parse(inputStream, d);
            }
        };

        // Start the parser on another thread
        executor.submit(parser);
        System.out.println(iter.next().toString());
        return iter;
    }
}
