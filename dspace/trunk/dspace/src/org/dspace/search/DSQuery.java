/*
 * DSQuery.java
 *
 * Version: $Revision$
 *
 * Date: $Date$
 *
 * Copyright (c) 2001, Hewlett-Packard Company and Massachusetts
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


// java classes
import java.io.*;
import java.util.*;
import java.sql.*;

// lucene search engine classes
import org.apache.lucene.index.*;
import org.apache.lucene.document.*;
import org.apache.lucene.queryParser.*;
import org.apache.lucene.search.*;
import org.apache.lucene.analysis.*;

// dspace classes
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.core.ConfigurationManager;


// issues
//   need to filter query string for security
//   cmd line query needs to process args correctly (seems to split them up)

public class DSQuery
{
    /** Do a query, returning a List of DSpace Handles to objects matching the query.
     *  @param query string in Lucene query syntax
     */
    public static synchronized List doQuery(String querystring)
        throws ParseException, IOException
    {
        ArrayList handlelist = new ArrayList();

        try
        {
            IndexSearcher searcher = new IndexSearcher(
                ConfigurationManager.getProperty("search.dir"));

            QueryParser qp = new QueryParser("default", new DSAnalyzer());

            Query myquery = qp.parse(querystring);
            Hits hits = searcher.search(myquery);

            for (int i = 0; i < hits.length(); i++)
            {
                Document d = hits.doc(i);

                String handletext = d.get("handle");
//unused?                String locstring = d.get("location");

                //int itemid = Integer.parseInt(handlestring);

                handlelist.add(handletext); //new Integer(itemid));
            }
        }
        catch (NumberFormatException e)
        {
            // a bad parse means that there are no results
            // doing nothing with the exception gets you
            //   throw new SQLException( "Error parsing search results: " + e );
            // ?? quit?
        }

        return handlelist;
    }


    /** Do a query, restricted to a collection
     * @param query
     * @param collection
     */
    public static List doQuery(String querystring, Collection coll)
        throws IOException, ParseException
    {
        String location = "l" + (coll.getID());

        String newquery = new String("+(" + querystring + ") +location:\"" + location + "\"");

        return doQuery(newquery);
    }


    /** Do a query, restricted to a community
     * @param querystring
     * @param community
     */
    public static List doQuery(String querystring, Community comm)
        throws IOException, ParseException
    {
        String location = "m" + (comm.getID());

        String newquery = new String("+(" + querystring + ") +location:\"" + location + "\"");

        return doQuery(newquery);
    }


    /** Do a query, printing results to stdout
     */
    public static void doCMDLineQuery(String query)
    {
        System.out.println("Command line query: " + query);

        try
        {
            List results = doQuery(query);

            Iterator i = results.iterator();

            while (i.hasNext())
            {
                System.out.println(i.next());
            }
        }
        catch (Exception e)
        {
            System.out.println("Exception caught: " + e);
        }
    }


    public static void main(String[] args)
    {
        DSQuery q = new DSQuery();

        if (args.length > 0)
        {
            q.doCMDLineQuery(args[0]);
        }
    }
}
