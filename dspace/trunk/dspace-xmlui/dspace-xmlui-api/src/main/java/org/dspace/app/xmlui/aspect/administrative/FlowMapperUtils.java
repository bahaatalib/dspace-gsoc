/*
 * FlowMapperUtils.java
 *
 * Version: $Revision: 1.3 $
 *
 * Date: $Date: 2006/07/13 23:20:54 $
 *
 * Copyright (c) 2002, Hewlett-Packard Company and Massachusetts
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
 */package org.dspace.app.xmlui.aspect.administrative;

import java.io.IOException;
import java.sql.SQLException;

import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.content.dao.CollectionDAO;
import org.dspace.content.dao.CollectionDAOFactory;
import org.dspace.content.dao.ItemDAO;
import org.dspace.content.dao.ItemDAOFactory;
import org.dspace.core.ArchiveManager;
import org.dspace.core.Context;

/**
 * 
 * @author Scott Phillips
 */
public class FlowMapperUtils 
{

	/** Language Strings */
	private static final Message T_map_items = new Message("default","xmlui.administrative.FlowMapperUtils.map_items");
	private static final Message T_unmaped_items = new Message("default","xmlui.administrative.FlowMapperUtils.unmaped_items");	
	

	/**
	 * Map the given items into this collection
	 * 
	 * @param context The current DSpace content
	 * @param collectionID The collection to map items into.
	 * @param itemIDs The items to map.
	 * @return Flow result
	 */
	public static FlowResult processMapItems(Context context, int collectionID, String[] itemIDs) throws SQLException, AuthorizeException, UIException, IOException
	{
        CollectionDAO collectionDAO = CollectionDAOFactory.getInstance(context);
        ItemDAO itemDAO = ItemDAOFactory.getInstance(context);

        FlowResult result = new FlowResult();
		result.setContinue(false);

		Collection toCollection = collectionDAO.retrieve(collectionID);
		
		for (String itemID : itemIDs)
        {
            Item item = itemDAO.retrieve(Integer.valueOf(itemID));

            // FIXME: Why did this check for READ permissions??
            // if (AuthorizeManager.authorizeActionBoolean(context, item, Constants.READ))
            ArchiveManager.move(context, item, null, toCollection);
        }
		
		context.commit();
		
		result.setContinue(true);
		result.setOutcome(true);
		result.setMessage(T_map_items);
		
		return result;
	}
	
	/**
	 * Unmap the given items from this collection
	 * 
	 * @param context The DSpace context
	 * @param collectionID The collection to unmap these items from.
	 * @param itemIDs The items to be unmapped.
	 * @return A flow result
	 */
	public static FlowResult processUnmapItems(Context context, int collectionID, String[] itemIDs) throws SQLException, AuthorizeException, UIException, IOException
	{
        CollectionDAO collectionDAO = CollectionDAOFactory.getInstance(context);
        ItemDAO itemDAO = ItemDAOFactory.getInstance(context);

        FlowResult result = new FlowResult();
		result.setContinue(false);

		Collection fromCollection = collectionDAO.retrieve(collectionID);
		
		for (String itemID : itemIDs)
        {
            Item item = itemDAO.retrieve(Integer.valueOf(itemID));

            // FIXME: Why did this check for READ permissions??
            //if (AuthorizeManager.authorizeActionBoolean(context, item, Constants.READ))
            ArchiveManager.move(context, item, fromCollection, null);
        }
		
		context.commit();
		
		result.setContinue(true);
		result.setOutcome(true);
		result.setMessage(T_unmaped_items);
		
		return result;
	}
}
