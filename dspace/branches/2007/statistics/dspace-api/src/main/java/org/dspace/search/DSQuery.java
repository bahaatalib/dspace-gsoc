/*
 * DSQuery.java
 *
 * Version: $Revision$
 *
 * Date: $Date$
 *
 * Copyright (c) 2002-2005, Hewlett-Packard Company and Massachusetts
 * Institute of Technology.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * - Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Neither the name of the Hewlett-Packard Company nor the name of the
 * Massachusetts Institute of Technology nor the names of their
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */
package org.dspace.search;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.TokenMgrError;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.oro.text.perl.Perl5Util;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.LogManager;

// issues
// need to filter query string for security
// cmd line query needs to process args correctly (seems to split them up)
/**
 * DSIndexer contains various static methods for performing queries on indices,
 * for collections and communities.
 *
 */
public class DSQuery
{
    // Result types
    static final String ALL = "999";

    static final String ITEM = "" + Constants.ITEM;

    static final String COLLECTION = "" + Constants.COLLECTION;

    static final String COMMUNITY = "" + Constants.COMMUNITY;

    // cache a Lucene IndexSearcher for more efficient searches
    private static IndexSearcher searcher = null;

    private static String indexDir = null;
    
    private static String operator = null;
    
    private static long lastModified;
    
    /** log4j logger */
    private static Logger log = Logger.getLogger(DSQuery.class);

    
    static
    {
        String maxClauses = ConfigurationManager.getProperty("search.max-clauses");
        if (maxClauses != null)
        {
            BooleanQuery.setMaxClauseCount(Integer.parseInt(maxClauses));
        } 
        
        indexDir = ConfigurationManager.getProperty("search.dir");
        
        operator = ConfigurationManager.getProperty("search.operator");   
    }

    /**
     * Do a query, returning a QueryResults object
     *
     * @param c  context
     * @param args query arguments in QueryArgs object
     * 
     * @return query results QueryResults
     */
    public static QueryResults doQuery(Context c, QueryArgs args)
            throws IOException
    {
        String querystring = args.getQuery();
        QueryResults qr = new QueryResults();
        List hitHandles = new ArrayList();
        List hitTypes = new ArrayList();

        // set up the QueryResults object
        qr.setHitHandles(hitHandles);
        qr.setHitTypes(hitTypes);
        qr.setStart(args.getStart());
        qr.setPageSize(args.getPageSize());

        // massage the query string a bit
        querystring = checkEmptyQuery(querystring); // change nulls to an empty
        // string
        querystring = workAroundLuceneBug(querystring); // logicals changed to
        // && ||, etc.
        querystring = stripHandles(querystring); // remove handles from query
        // string
        querystring = stripAsterisk(querystring); // remove asterisk from
        // beginning of string

        try
        {
            // grab a searcher, and do the search
            Searcher searcher = getSearcher(c);

            QueryParser qp = new QueryParser("default", DSIndexer.getAnalyzer());
            log.info("Final query string: " + querystring);
            
            if (operator == null || operator.equals("OR"))
            {
            	qp.setDefaultOperator(QueryParser.OR_OPERATOR);
            }
            else
            {
            	qp.setDefaultOperator(QueryParser.AND_OPERATOR);
            }
            
            Query myquery = qp.parse(querystring);
            Hits hits = searcher.search(myquery);

            // set total number of hits
            qr.setHitCount(hits.length());

            // We now have a bunch of hits - snip out a 'window'
            // defined in start, count and return the handles
            // from that window
            // first, are there enough hits?
            if (args.getStart() < hits.length())
            {
                // get as many as we can, up to the window size
                // how many are available after snipping off at offset 'start'?
                int hitsRemaining = hits.length() - args.getStart();

                int hitsToProcess = (hitsRemaining < args.getPageSize()) ? hitsRemaining
                        : args.getPageSize();

                for (int i = args.getStart(); i < (args.getStart() + hitsToProcess); i++)
                {
                    Document d = hits.doc(i);

                    String handleText = d.get("handle");
                    String handletype = d.get("type");

                    hitHandles.add(handleText);

                    if (handletype.equals("" + Constants.ITEM))
                    {
                        hitTypes.add(new Integer(Constants.ITEM));
                    }
                    else if (handletype.equals("" + Constants.COLLECTION))
                    {
                        hitTypes.add(new Integer(Constants.COLLECTION));
                    }
                    else if (handletype.equals("" + Constants.COMMUNITY))
                    {
                        hitTypes.add(new Integer(Constants.COMMUNITY));
                    }
                    else
                    {
                        // error! unknown type!
                    }
                }
            }
        }
        catch (NumberFormatException e)
        {
            log
                    .warn(LogManager.getHeader(c, "Number format exception", ""
                            + e));

            qr.setErrorMsg("Number format exception");
        }
        catch (ParseException e)
        {
            // a parse exception - log and return null results
            log.warn(LogManager.getHeader(c, "Invalid search string", "" + e));

            qr.setErrorMsg("Invalid search string");
        }
        catch (TokenMgrError tme)
        {
            // Similar to parse exception
            log
                    .warn(LogManager.getHeader(c, "Invalid search string", ""
                            + tme));

            qr.setErrorMsg("Invalid search string");
        }
        catch(BooleanQuery.TooManyClauses e)
        {
            log.warn(LogManager.getHeader(c, "Query too broad", e.toString()));
            qr.setErrorMsg("Your query was too broad. Try a narrower query.");
        }

        return qr;
    }

    static String checkEmptyQuery(String myquery)
    {
        if (myquery.equals(""))
        {
            myquery = "empty_query_string";
        }

        return myquery;
    }

    static String workAroundLuceneBug(String myquery)
    {
        // Lucene currently has a bug which breaks wildcard
        // searching when you have uppercase characters.
        // Here we substitute the boolean operators -- which
        // have to be uppercase -- before tranforming the
        // query string to lowercase.
        Perl5Util util = new Perl5Util();

        myquery = util.substitute("s/ AND / && /g", myquery);
        myquery = util.substitute("s/ OR / || /g", myquery);
        myquery = util.substitute("s/ NOT / ! /g", myquery);

        myquery = myquery.toLowerCase();

        return myquery;
    }

    static String stripHandles(String myquery)
    {
        // Drop beginning pieces of full handle strings
        Perl5Util util = new Perl5Util();

        myquery = util.substitute("s|^(\\s+)?http://hdl\\.handle\\.net/||",
                myquery);
        myquery = util.substitute("s|^(\\s+)?hdl:||", myquery);

        return myquery;
    }

    static String stripAsterisk(String myquery)
    {
        // query strings (or words) begining with "*" cause a null pointer error
        Perl5Util util = new Perl5Util();

        myquery = util.substitute("s/^\\*//", myquery);
        myquery = util.substitute("s| \\*| |", myquery);
        myquery = util.substitute("s|\\(\\*|\\(|", myquery);
        myquery = util.substitute("s|:\\*|:|", myquery);

        return myquery;
    }

    /**
     * Do a query, restricted to a collection
     * 
     * @param c
     *            context
     * @param args
     *            query args
     * @param coll
     *            collection to restrict to
     * 
     * @return QueryResults same results as doQuery, restricted to a collection
     */
    public static QueryResults doQuery(Context c, QueryArgs args,
            Collection coll) throws IOException
    {
        String querystring = args.getQuery();

        querystring = checkEmptyQuery(querystring);

        String location = "l" + (coll.getID());

        String newquery = new String("+(" + querystring + ") +location:\""
                + location + "\"");

        args.setQuery(newquery);

        return doQuery(c, args);
    }

    /**
     * Do a query, restricted to a community
     * 
     * @param c
     *            context
     * @param args
     *            query args
     * @param comm
     *            community to restrict to
     * 
     * @return QueryResults same results as doQuery, restricted to a collection
     */
    public static QueryResults doQuery(Context c, QueryArgs args, Community comm)
            throws IOException
    {
        String querystring = args.getQuery();

        querystring = checkEmptyQuery(querystring);

        String location = "m" + (comm.getID());

        String newquery = new String("+(" + querystring + ") +location:\""
                + location + "\"");

        args.setQuery(newquery);

        return doQuery(c, args);
    }


    /**
     * Do a query, printing results to stdout largely for testing, but it is
     * useful
     */
    public static void doCMDLineQuery(String query)
    {
        System.out.println("Command line query: " + query);
        System.out.println("Only reporting default-sized results list");

        try
        {
            Context c = new Context();

            QueryArgs args = new QueryArgs();
            args.setQuery(query);

            QueryResults results = doQuery(c, args);

            Iterator i = results.getHitHandles().iterator();
            Iterator j = results.getHitTypes().iterator();

            while (i.hasNext())
            {
                String thisHandle = (String) i.next();
                Integer thisType = (Integer) j.next();
                String type = Constants.typeText[thisType.intValue()];

                // also look up type
                System.out.println(type + "\t" + thisHandle);
            }
        }
        catch (Exception e)
        {
            System.out.println("Exception caught: " + e);
        }
    }

    public static void main(String[] args)
    {
        if (args.length > 0)
        {
            DSQuery.doCMDLineQuery(args[0]);
        }
    }

    /*---------  protected methods ----------*/

    /**	
     * get an IndexReader.
     * @throws IOException 
     */
    protected static IndexReader getIndexReader() 
    	throws IOException
    {
    	return getSearcher(null).getIndexReader();
    }
    
    /**
     * get an IndexSearcher, hopefully a cached one (gives much better
     * performance.) checks to see if the index has been modified - if so, it
     * creates a new IndexSearcher
     */
    protected static synchronized IndexSearcher getSearcher(Context c)
            throws IOException
    {
       
        // If we have already opened a searcher, check to see if the index has been updated
        // If it has, we need to close the existing searcher - we will open a new one later
        if (searcher != null && lastModified != IndexReader.getCurrentVersion(indexDir))
        {
            try
            {
                // Close the cached IndexSearcher
                searcher.close();
            }
            catch (IOException ioe)
            {
                // Index is probably corrupt. Log the error, but continue to either:
                // 1) Return existing searcher (may yet throw exception, no worse than throwing here)
                log.warn("DSQuery: Unable to check for updated index", ioe);
            }
            finally
            {
            	searcher = null;
            }
        }

        // There is no existing searcher - either this is the first execution,
        // or the index has been updated and we closed the old index.
        if (searcher == null)
        {
            // So, open a new searcher
            lastModified = IndexReader.getCurrentVersion(indexDir);
            searcher = new IndexSearcher(indexDir){
            	/* 
            	 * TODO: Has Lucene fixed this bug yet?
            	 * Lucene doesn't release read locks in 
            	 * windows properly on finalize. Our hack
            	 * extend IndexSearcher to force close().
            	 */
                protected void finalize() throws Throwable {
            		this.close();
            		super.finalize();
            	}
            };
        }

        return searcher;
    }
}

// it's now up to the display page to do the right thing displaying
// items & communities & collections
