package parser;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.lang.PipedRDFIterator;
import org.apache.jena.riot.lang.PipedRDFStream;
import org.apache.jena.riot.lang.PipedTriplesStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Parser
{
    private static ExecutorService executor = Executors.newSingleThreadExecutor();

    public static void parse (final String dump)
    {
        PipedRDFIterator<Triple> iter = new PipedRDFIterator<Triple>();

        System.out.println(dump);
        final PipedRDFStream<Triple> inputStream = new PipedTriplesStream(iter);

        Runnable parser = new Runnable()
        {
            @Override
            public void run()
            {
                System.out.println(iter.next());
//                RDFDataMgr.parse(inputStream,dump);
            }
        };
        executor.submit(parser);
//        System.out.println(iter.hasNext());
//        return iter;
    }
}
