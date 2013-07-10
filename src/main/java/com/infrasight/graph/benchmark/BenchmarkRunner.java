package com.infrasight.graph.benchmark;

import java.io.File;
import java.util.Random;

import org.junit.Ignore;
import org.junit.Test;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.batch.BatchGraph;

/**
 * Abstract class that contains the graph benchmark tests.<br/>
 * Each Blueprints graph implementation to test needs to extend this and implement
 * the two abstract graph open/delete methods.
 * 
 * @author Konrad Eriksson <konrad@infrasightlabs.com>
 *
 */
public abstract class BenchmarkRunner {
	// These should probably be configurable via properties file
	protected static final int totalInsertVertices = 1000000;
	protected static final int COMMIT_SIZE = 10000;
	
	public abstract Graph openGraph(File workingDir, boolean massiveInserts);
	public abstract void deleteGraph(Graph graph);

	@Ignore
	@Test
	public void testInsertEmptyVertices() {
		Graph graph = newGraph(true);
		graph = BatchGraph.wrap(graph, COMMIT_SIZE);
		
		ProgressPrinter progress = new ProgressPrinter(totalInsertVertices, "vertices");
		int i = 0;
		for(; i < totalInsertVertices; i++) {
			graph.addVertex(i);
			
			if(i % COMMIT_SIZE == 0) {
				progress.report(i);
			}
		}
		graph.shutdown();
		progress.done("Inserted empty vertices", getWorkingDirectorySize());
		deleteGraph(graph);
	}

	@Ignore
	@Test
	public void testInsertVerticesWithProperties() {
		Graph graph = newGraph(true);
		
		ProgressPrinter progress = new ProgressPrinter(totalInsertVertices, "vertices");
		int i = 0;
		for(; i < totalInsertVertices; i++) {
			Vertex v = graph.addVertex(i);
			v.setProperty("int", i);
			v.setProperty("long", 10340340350530355L + i);
			v.setProperty("name", "name"+i);
			v.setProperty("string", "Some longer string that we need to add as property to the vertex");
			v.setProperty("boolean", (i % 1));
			
			if(i % COMMIT_SIZE == 0) {
				progress.report(i);
			}
		}
		graph.shutdown();
		progress.done("Inserting vertices with properties", getWorkingDirectorySize());
		deleteGraph(graph);
	}

	@Ignore
	@Test
	public void testInsertVerticesWithPropertiesAndKeyIndices() {
		Graph graph = newGraph(true);
		
		KeyIndexableGraph kig = (KeyIndexableGraph)graph;
		kig.createKeyIndex("int", Vertex.class);
		kig.createKeyIndex("long", Vertex.class);
		kig.createKeyIndex("name", Vertex.class);
		kig.createKeyIndex("string", Vertex.class);
		kig.createKeyIndex("boolean", Vertex.class);
		
		ProgressPrinter progress = new ProgressPrinter(totalInsertVertices, "vertices");
		int i = 0;
		for(; i < totalInsertVertices; i++) {
			Vertex v = graph.addVertex(null);
			v.setProperty("int", i);
			v.setProperty("long", 10340340350530355L + i);
			v.setProperty("name", "name"+i);
			v.setProperty("string", "Some longer string that we need to add as property to the vertex");
			v.setProperty("boolean", (i % 1));
			
			if(i % COMMIT_SIZE == 0) {
				progress.report(i);
			}
		}
		graph.shutdown();
		progress.done("Inserting vertices with properties using indices", getWorkingDirectorySize());
		deleteGraph(graph);
	}
	
	@Test
	public void testInsertConnectedVertices() {
		Graph graph = newGraph(true);
		
		Vertex vs[] = new Vertex[totalInsertVertices];
		Random rand = new Random(System.currentTimeMillis());
		ProgressPrinter progress = new ProgressPrinter(totalInsertVertices, "vertices");
		ProgressPrinter progress2 = new ProgressPrinter(totalInsertVertices*3, "edges");
		int i = 0;
		int edges = 0;
		for(; i < totalInsertVertices; i++) {
			vs[i] = graph.addVertex(null);
			vs[i].setProperty("name", "Nisse"+i);
			vs[i].setProperty("age", i % 100);
			if(i > 1) {
				for(int j = 0; j < 3; j++) {
					final int pos = rand.nextInt(i-1);
					vs[i].addEdge("knows", vs[pos]);
					edges++;
				}
			}
			
			if(i % COMMIT_SIZE == 0) {
				if(graph instanceof TransactionalGraph)
					((TransactionalGraph)graph).commit();
				progress.report(i);
				progress2.report(edges);
			}
		}
		graph.shutdown();
		progress.done("Inserted connected vertices", getWorkingDirectorySize());
		progress2.done("Inserted edges", getWorkingDirectorySize());
		deleteGraph(graph);
	}
	
	/** Open a new empty graph in working directory */
	protected Graph newGraph(boolean massiveInserts) {
		cleanWorkingDirectory();
		Graph g = openGraph(getWorkingDirectory(), massiveInserts);
		System.out.println("Opened "+g+" at "+getWorkingDirectory()+" (massiveInserts:"+massiveInserts+")");
		return g;
	}

	protected void cleanWorkingDirectory() {
		File dir = getWorkingDirectory();
		for(File file : dir.listFiles()) {
			if(file.isDirectory())
				BenchmarkUtil.delDir(file);
			else if(!file.delete())
				throw new RuntimeException("Failed to delete file: "+file);
		}
	}
	
	private File getWorkingDirectory() {
        String directory = System.getProperty("db.path");
        if (directory == null) {
            if (System.getProperty("os.name").toUpperCase().contains("WINDOWS"))
                directory = "C:/temp/blueprints_benchmark";
            else
                directory = "/tmp/blueprints_benchmark";
        }
        File dir = new File(directory);
        if(!dir.isDirectory()) {
			if(!dir.mkdirs())
				throw new RuntimeException("Failed to create directory: "+dir);
        }
        return dir;
    }
	
	private long getWorkingDirectorySize() {
		File dir = getWorkingDirectory();
		return BenchmarkUtil.getDirSize(dir);
	}


}
