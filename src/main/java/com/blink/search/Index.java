package com.blink.search;

import com.blink.util.Pair;
import java.util.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.LinkedList;

public class Index {

	// Term id -> (position in index file, doc frequency) dictionary
	private static Map<Integer, Pair<Long, Integer>> postingDict 
		= new TreeMap<Integer, Pair<Long, Integer>>();
	// Doc name -> doc id dictionary
	private static Map<String, Integer> docDict//already populated
		= new TreeMap<String, Integer>();
	// Term -> term id dictionary
	private static Map<String, Integer> termDict //not populated
		= new TreeMap<String, Integer>();
	// Block queue
	private static LinkedList<File> blockQueue
		= new LinkedList<File>();

	// Total file counter
	private static int totalFileCount = 0;
	// Document counter
	private static int docIdCounter = 0;
	// Term counter
	private static int wordIdCounter = 0;
	// Index
	private static BaseIndex index = null;

	
	/* 
	 * Write a posting list to the given file 
	 * Record the file position of this posting list
	 * so that it can be read back during retrieval
	 * */
	private static void writePosting(FileChannel fc, PostingList posting)
			throws IOException {
        postingDict.put(posting.getTermId(), new Pair<Long,Integer>(fc.position(), posting.getList().size()));
        index.writePosting(fc, posting);

	}

	public static void main(String[] args) throws IOException {
		/* Parse command line */
		if (args.length != 3) {
			System.err
					.println("Usage: java Index data_dir output_dir");
			return;
		}

		/* Get index */
		String className = "com.blink.search.BasicIndex";
		try {
			Class<?> indexClass = Class.forName(className);
			index = (BaseIndex) indexClass.newInstance();
		} catch (Exception e) {
			System.err
					.println("Index method must be \"Basic\"");
			throw new RuntimeException(e);
		}

		/* Get root directory */
		String root = args[1];
		File rootdir = new File(root);
		if (!rootdir.exists() || !rootdir.isDirectory()) {
			System.err.println("Invalid data directory: " + root);
			return;
		}

		/* Get output directory */
		String output = args[2];
		File outdir = new File(output);
		if (outdir.exists() && !outdir.isDirectory()) {
			System.err.println("Invalid output directory: " + output);
			return;
		}

		if (!outdir.exists()) {
			if (!outdir.mkdirs()) {
				System.err.println("Create output directory failure");
				return;
			}
		}

		/* A filter to get rid of all files starting with .*/
		FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				String name = pathname.getName();
				return !name.startsWith(".");
			}
		};

		/* BSBI indexing algorithm */
		File[] dirlist = rootdir.listFiles(filter);
		Map<Integer, PostingList> mapOfPostList = new TreeMap<Integer, PostingList>();

		/* For each block */
		for (File block : dirlist) {
			File blockFile = new File(output, block.getName());
			blockQueue.add(blockFile);
			File blockDir = new File(root, block.getName());
			File[] filelist = blockDir.listFiles(filter);
			
			/* For each file */
			for (File file : filelist) {
				++totalFileCount;
				String fileName = block.getName() + "/" + file.getName();
				docDict.put(fileName, docIdCounter++);

				BufferedReader reader = new BufferedReader(new FileReader(file));
				String line;
				while ((line = reader.readLine()) != null) {
					String[] tokens = line.trim().split("\\s+");
					for (String token : tokens) {
						/*
						 *       For each term, build up a list of
						 *       documents in which the term occurs
						 */
						if (!termDict.containsKey(token)){
							termDict.put(token, wordIdCounter++);
						}

						if (!mapOfPostList.containsKey(termDict.get(token))){
							
							List<Integer> positions = new ArrayList<Integer>();
							positions.add(docDict.get(fileName));
							PostingList new_postingList = new PostingList(termDict.get(token), positions);
							mapOfPostList.put(termDict.get(token), new_postingList );

						} else {
							int listSize = mapOfPostList.get(termDict.get(token)).getList().size();
							if ((mapOfPostList.get(termDict.get(token)).getList().get(listSize-1)) != docDict.get(fileName)){
								mapOfPostList.get(termDict.get(token)).getList().add(docDict.get(fileName));
							}
						}
					}
				}
				reader.close();
				
			}


			/* Sort and output */
			if (!blockFile.createNewFile()) {
				System.err.println("Create new block failure.");
				return;
			}
			
			RandomAccessFile bfc = new RandomAccessFile(blockFile, "rw");

			/*       Write all posting lists for all terms to file (bfc) //utility function
			 *		 call the writing function from here, for every elements of the postalList
			 */
			FileChannel fc = bfc.getChannel();
         
            for (PostingList posting : mapOfPostList.values()) {
                index.writePosting(fc, posting);
            }
            
            mapOfPostList.clear();
			bfc.close();
			
			
		}
		// Printing for test purpose
		// for (Map.Entry<Integer, PostingList> entry : mapOfPostList.entrySet()) {
		// 		Integer key = entry.getKey();
		// 		PostingList value = entry.getValue();
		// 		System.out.println(key + " =: " + value.getTermId() + value.getList().toString());
		// }


		/* output total number of files. */
		System.out.println(totalFileCount);

		/* Merge blocks */
		while (true) {
			if (blockQueue.size() <= 1){
				break;
			}
			File b1 = blockQueue.removeFirst();
			File b2 = blockQueue.removeFirst();
			File combfile = new File(output, b1.getName() + "+" + b2.getName());
			if (!combfile.createNewFile()) {
				System.err.println("Create new block failure.");
				return;
			}

			RandomAccessFile bf1 = new RandomAccessFile(b1, "r");
			RandomAccessFile bf2 = new RandomAccessFile(b2, "r");
			RandomAccessFile mf = new RandomAccessFile(combfile, "rw");
			
			FileChannel fc1 = bf1.getChannel();
            FileChannel fc2 = bf2.getChannel();
            FileChannel fcm = mf.getChannel();

            PostingList p1 = index.readPosting(fc1);
            PostingList p2 = index.readPosting(fc2);
            
            while(p1 != null || p2 != null)
            {
                while (p1 != null && (p2 == null || p1.getTermId() < p2.getTermId()))
                {
                    writePosting(fcm, p1);
                    p1 = index.readPosting(fc1);
                }
                
                while (p2 != null && (p1 == null || p2.getTermId() < p1.getTermId()))
                {
                    writePosting(fcm, p2);
                    p2 = index.readPosting(fc2);
                }
                
                // when same termID is found
                if (p1 != null && p2 != null)
                {
                    PostingList pList;
                    
                    pList = new PostingList(p1.getTermId());
                    Iterator<Integer> i1 = p1.getList().iterator();
                    Integer doc1 = getNext(i1);
                    Iterator<Integer> i2 = p2.getList().iterator();
                    Integer doc2 = getNext(i2);
                    
                    while(doc1 != null && doc2 != null)
                    {
                        // if equal
                        if(doc1.equals(doc2))
                        {
                            pList.getList().add(doc1);
                            doc1 = getNext(i1);
                            doc2 = getNext(i2);
                        }
                        else if(doc1 > doc2)
                        {
                            pList.getList().add(doc2);
                            doc2 = getNext(i2);
                        }
                        else
                        {
                            pList.getList().add(doc1);
                            doc1 = getNext(i1);
                        }
                    }
                    while(doc1 != null)
                    {
                        pList.getList().add(doc1);
                        doc1 = getNext(i1);
                    }
                    while(doc2 != null)
                    {
                        pList.getList().add(doc2);
                        doc2 = getNext(i2);
                    }
                    writePosting(fcm, pList);
                }
                p1 = index.readPosting(fc1);
                p2 = index.readPosting(fc2);
            } 
			/*
			 *       Combine blocks bf1 and bf2 into our combined file, mf
			 *       The two blocks will merge based on term ID    
			 */
			bf1.close();
			bf2.close();
			mf.close();
			b1.delete();
			b2.delete();
			blockQueue.add(combfile);
		}

		/* Dump constructed index back into file system */
		File indexFile = blockQueue.removeFirst();
		indexFile.renameTo(new File(output, "corpus.index"));

		BufferedWriter termWriter = new BufferedWriter(new FileWriter(new File(
				output, "term.dict")));
		for (String term : termDict.keySet()) {
			termWriter.write(term + "\t" + termDict.get(term) + "\n");
		}
		termWriter.close();

		BufferedWriter docWriter = new BufferedWriter(new FileWriter(new File(
				output, "doc.dict")));
		for (String doc : docDict.keySet()) {
			docWriter.write(doc + "\t" + docDict.get(doc) + "\n");
		}
		docWriter.close();

		BufferedWriter postWriter = new BufferedWriter(new FileWriter(new File(
				output, "posting.dict")));
		for (Integer termId : postingDict.keySet()) {
			postWriter.write(termId + "\t" + postingDict.get(termId).getFirst()
					+ "\t" + postingDict.get(termId).getSecond() + "\n");
		}
		postWriter.close();
	}


	static <X> X getNext(Iterator<X> item)
    {
        if(item.hasNext())
        {
            return item.next();
        }
        else
        {
            return null;
        }
    }

}
