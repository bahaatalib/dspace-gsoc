/*
 * ArchiveManager.java
 *
 * Version: $Revision: 1727 $
 *
 * Date: $Date: 2007-01-19 10:52:10 +0000 (Fri, 19 Jan 2007) $
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
package org.dspace.core;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.browse.Browse;
import org.dspace.content.Bitstream;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.DCDate;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.InstallItem;
import org.dspace.content.MetadataSchema;
import org.dspace.content.dao.BundleDAOFactory;
import org.dspace.content.dao.ItemDAO;
import org.dspace.content.dao.ItemDAOFactory;
import org.dspace.content.dao.CollectionDAO;
import org.dspace.content.dao.CollectionDAOFactory;
import org.dspace.content.dao.CommunityDAO;
import org.dspace.content.dao.CommunityDAOFactory;
import org.dspace.content.uri.ObjectIdentifier;
import org.dspace.content.uri.PersistentIdentifier;
import org.dspace.eperson.EPerson;
import org.dspace.history.HistoryManager;
import org.dspace.search.DSIndexer;

/**
 * This class could really do with a CLI...
 */
public class ArchiveManager
{
    private static Logger log = Logger.getLogger(ArchiveManager.class);

    public static DSpaceObject getObject(Context context,
            PersistentIdentifier identifier)
    {
        return getObject(context, identifier.getResourceID(),
                identifier.getResourceTypeID());
    }

    public static DSpaceObject getObject(Context context, int id, int type)
    {
        switch(type)
        {
            case (Constants.BITSTREAM):
                try
                {
                    return Bitstream.find(context, id);
                }
                catch (SQLException sqle)
                {
                    throw new RuntimeException(sqle);
                }
            case (Constants.BUNDLE):
                return BundleDAOFactory.getInstance(context).retrieve(id);
            case (Constants.ITEM):
                return ItemDAOFactory.getInstance(context).retrieve(id);
            case (Constants.COLLECTION):
                return CollectionDAOFactory.getInstance(context).retrieve(id);
            case (Constants.COMMUNITY):
                return CommunityDAOFactory.getInstance(context).retrieve(id);
            default:
                throw new RuntimeException("Not a valid DSpaceObject type");
        }
    }

    /**
     * Withdraw the item from the archive. It is kept in place, and the content
     * and metadata are not deleted, but it is not publicly accessible.
     */
    public static void withdrawItem(Context context, Item item)
        throws AuthorizeException, IOException
    {
        ItemDAO itemDAO = ItemDAOFactory.getInstance(context);

        String timestamp = DCDate.getCurrent().toString();

        // Build some provenance data while we're at it.
        String collectionProv = "";
        Collection[] colls = item.getCollections();

        for (int i = 0; i < colls.length; i++)
        {
            collectionProv = collectionProv + colls[i].getMetadata("name")
                    + " (ID: " + colls[i].getID() + ")\n";
        }

        // Check permission. User either has to have REMOVE on owning collection
        // or be COLLECTION_EDITOR of owning collection
        if (!AuthorizeManager.authorizeActionBoolean(context,
                item.getOwningCollection(), Constants.COLLECTION_ADMIN)
                && !AuthorizeManager.authorizeActionBoolean(context,
                        item.getOwningCollection(), Constants.REMOVE))
        {
            throw new AuthorizeException("To withdraw item must be " +
                    "COLLECTION_ADMIN or have REMOVE authorization on " +
                    "owning Collection.");
        }

        item.setWithdrawn(true);
        item.setArchived(false);

        EPerson e = context.getCurrentUser();
        try
        {
            // Add suitable provenance - includes user, date, collections +
            // bitstream checksums
            String prov = "Item withdrawn by " + e.getFullName() + " ("
                    + e.getEmail() + ") on " + timestamp + "\n"
                    + "Item was in collections:\n" + collectionProv
                    + InstallItem.getBitstreamProvenanceMessage(item);

            item.addMetadata(MetadataSchema.DC_SCHEMA, "description", "provenance",
                    "en", prov);

            // Update item in DB
            itemDAO.update(item);

            // Invoke History system
            HistoryManager.saveHistory(context, item, HistoryManager.MODIFY, e,
                    context.getExtraLogInfo());

            // Remove from indicies
            Browse.itemRemoved(context, item.getID());
            DSIndexer.unIndexContent(context, item);
        }
        catch (SQLException sqle)
        {
            throw new RuntimeException(sqle);
        }

        // and all of our authorization policies
        // FIXME: not very "multiple-inclusion" friendly
        AuthorizeManager.removeAllPolicies(context, item);

        log.info(LogManager.getHeader(context, "withdraw_item", "user="
                + e.getEmail() + ",item_id=" + item.getID()));
    }

    /**
     * Reinstate a withdrawn item.
     */
    public static void reinstateItem(Context context, Item item)
        throws AuthorizeException, IOException
    {
        ItemDAO itemDAO = ItemDAOFactory.getInstance(context);

        String timestamp = DCDate.getCurrent().toString();

        // Check permission. User must have ADD on all collections.
        // Build some provenance data while we're at it.
        String collectionProv = "";
        Collection[] colls = item.getCollections();

        for (int i = 0; i < colls.length; i++)
        {
            collectionProv = collectionProv + colls[i].getMetadata("name")
                    + " (ID: " + colls[i].getID() + ")\n";
            AuthorizeManager.authorizeAction(context, colls[i],
                    Constants.ADD);
        }

        item.setWithdrawn(false);
        item.setArchived(true);

        // Add suitable provenance - includes user, date, collections +
        // bitstream checksums
        EPerson e = context.getCurrentUser();
        try
        {
            String prov = "Item reinstated by " + e.getFullName() + " ("
                    + e.getEmail() + ") on " + timestamp + "\n"
                    + "Item was in collections:\n" + collectionProv
                    + InstallItem.getBitstreamProvenanceMessage(item);

            item.addMetadata(MetadataSchema.DC_SCHEMA, "description", "provenance",
                    "en", prov);

            // Update item in DB
            itemDAO.update(item);

            // Invoke History system
            HistoryManager.saveHistory(context, item, HistoryManager.MODIFY, e,
                    context.getExtraLogInfo());

            // Add to indicies
            // Remove - update() already performs this
            // Browse.itemAdded(context, this);
            DSIndexer.indexContent(context, item);

            // authorization policies
            if (colls.length > 0)
            {
                // FIXME: not multiple inclusion friendly - just apply access
                // policies from first collection
                // remove the item's policies and replace them with
                // the defaults from the collection
                item.inheritCollectionDefaultPolicies(colls[0]);
            }
        }
        catch (SQLException sqle)
        {
            throw new RuntimeException(sqle);
        }

        log.info(LogManager.getHeader(context, "reinstate_item", "user="
                + e.getEmail() + ",item_id=" + item.getID()));
    }

    /**
     * Call with a null source to add to the destination; call with a null
     * destination to remove from the source; used in this way, move() can act
     * as an alias for add() and delete().
     *
     * WARNING: Communities, Collection, and Items that are orphaned after
     * being removed from a container will be *deleted*. It may be better to
     * leave them in the system with a means for re-associating them with other
     * containers, but that doesn't really fit with the strict hierarchical
     * nature of DSpace containers. ie: if you delete a Community, you expect
     * everything beneath it to get deleted as well, not just to be marked as
     * being orphaned.
     *
     * WARNING 2: This needs to include some sanity checks to make sure we
     * don't end up with circular parent-child relationships.
     */
    public static void move(Context context,
            DSpaceObject dso, DSpaceObject source, DSpaceObject dest)
        throws AuthorizeException
    {
        assert((dso instanceof Item) ||
               (dso instanceof Collection) ||
               (dso instanceof Community));
        assert((source != null) || (dest != null));

        logMove(dso, source, dest);

        if (dso instanceof Item)
        {
            if (dest != null)
            {
                addItemToCollection(context,
                        (Item) dso, (Collection) dest);
            }
            else
            {
                removeItemFromCollection(context,
                        (Item) dso, (Collection) source);
            }
        }
        else if (dso instanceof Collection)
        {
            if (dest != null)
            {
                addCollectionToCommunity(context,
                        (Collection) dso, (Community) dest);
            }
            else
            {
                removeCollectionFromCommunity(context,
                        (Collection) dso, (Community) source);
            }
        }
        else if (dso instanceof Community)
        {
            if (dest != null)
            {
                addCommunityToCommunity(context,
                        (Community) dso, (Community) dest);
            }
            else
            {
                removeCommunityFromCommunity(context,
                        (Community) dso, (Community) source);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////
    // Utility methods
    ////////////////////////////////////////////////////////////////////

    /**
     * Add an item to the collection. This simply adds a relationship between
     * the item and the collection - it does nothing like set an issue date,
     * remove a personal workspace item etc. This has instant effect;
     * <code>update</code> need not be called.
     */
    private static void addItemToCollection(Context context,
            Item item, Collection collection)
        throws AuthorizeException
    {
        ItemDAO itemDAO = ItemDAOFactory.getInstance(context);
        CollectionDAO collectionDAO = CollectionDAOFactory.getInstance(context);

        collectionDAO.link(collection, item);
    }

    private static void removeItemFromCollection(Context context,
            Item item, Collection collection)
        throws AuthorizeException
    {
        ItemDAO itemDAO = ItemDAOFactory.getInstance(context);
        CollectionDAO collectionDAO = CollectionDAOFactory.getInstance(context);

        // Remove mapping
        collectionDAO.unlink(collection, item);

        if (collectionDAO.getParentCollections(item).size() == 0)
        {
            // make the right to remove the item explicit because the implicit
            // relation has been removed. This only has to concern the
            // currentUser because he started the removal process and he will
            // end it too. also add right to remove from the item to remove
            // it's bundles.
            try
            {
                AuthorizeManager.addPolicy(context, item, Constants.DELETE,
                        context.getCurrentUser());
                AuthorizeManager.addPolicy(context, item, Constants.REMOVE,
                        context.getCurrentUser());
            }
            catch (SQLException sqle)
            {
                throw new RuntimeException(sqle);
            }

            itemDAO.delete(item.getID());
        }
    }

    private static void addCollectionToCommunity(Context context,
            Collection child, Community parent)
        throws AuthorizeException
    {
        CommunityDAO communityDAO = CommunityDAOFactory.getInstance(context);

        communityDAO.link((DSpaceObject) parent, (DSpaceObject) child);
    }

    private static void removeCollectionFromCommunity(Context context,
            Collection child, Community parent)
        throws AuthorizeException
    {
        CommunityDAO communityDAO = CommunityDAOFactory.getInstance(context);
        CollectionDAO collectionDAO = CollectionDAOFactory.getInstance(context);

        communityDAO.unlink((DSpaceObject) parent, (DSpaceObject) child);

        if (communityDAO.getParentCommunities(child).size() == 0)
        {
            // make the right to remove the child explicit because the
            // implicit relation has been removed. This only has to concern the
            // currentUser because he started the removal process and he will
            // end it too. also add right to remove from the child to
            // remove it's items.
            try
            {
                AuthorizeManager.addPolicy(context, child, Constants.DELETE,
                        context.getCurrentUser());
                AuthorizeManager.addPolicy(context, child, Constants.REMOVE,
                        context.getCurrentUser());
            }
            catch (SQLException sqle)
            {
                throw new RuntimeException(sqle);
            }

            // Orphan; delete it
            collectionDAO.delete(child.getID());
        }
    }

    private static void addCommunityToCommunity(Context context,
            Community child, Community parent)
        throws AuthorizeException
    {
        CommunityDAO communityDAO = CommunityDAOFactory.getInstance(context);

        communityDAO.link((DSpaceObject) parent, (DSpaceObject) child);
    }

    private static void removeCommunityFromCommunity(Context context,
            Community child, Community parent)
        throws AuthorizeException
    {
        CommunityDAO communityDAO = CommunityDAOFactory.getInstance(context);

        communityDAO.unlink((DSpaceObject) parent, (DSpaceObject) child);

        if (communityDAO.getParentCommunities(child).size() == 0)
        {
            // make the right to remove the collection explicit because the
            // implicit relation has been removed. This only has to concern the
            // currentUser because he started the removal process and he will
            // end it too. also add right to remove from the collection to
            // remove it's items.
            try
            {
                AuthorizeManager.addPolicy(context, child, Constants.DELETE,
                        context.getCurrentUser());
                AuthorizeManager.addPolicy(context, child, Constants.REMOVE,
                        context.getCurrentUser());
            }
            catch (SQLException sqle)
            {
                throw new RuntimeException(sqle);
            }

            communityDAO.delete(child.getID());
        }
    }

    private static void logMove(DSpaceObject dso, DSpaceObject source,
            DSpaceObject dest)
    {
        String dsoStr = "";
        String sourceStr = "";
        String destStr = "";

        switch (dso.getType())
        {
            case Constants.ITEM:
                dsoStr = "Item ";
                break;
            case Constants.COLLECTION:
                dsoStr = "Collection ";
                break;
            case Constants.COMMUNITY:
                dsoStr = "Community ";
                break;
            default:
        }

        if (source != null)
        {
            switch (source.getType())
            {
                case Constants.ITEM:
                    sourceStr = "Item ";
                    break;
                case Constants.COLLECTION:
                    sourceStr = "Collection ";
                    break;
                case Constants.COMMUNITY:
                    sourceStr = "Community ";
                    break;
                default:
            }
        }

        if (dest != null)
        {
            switch (dest.getType())
            {
                case Constants.ITEM:
                    destStr = "Item ";
                    break;
                case Constants.COLLECTION:
                    destStr = "Collection ";
                    break;
                case Constants.COMMUNITY:
                    destStr = "Community ";
                    break;
                default:
            }
        }

        sourceStr = sourceStr + (source == null ? "null" : source.getID());
        destStr = destStr + (dest == null ? "null" : dest.getID());
        dsoStr = dsoStr + (dso == null ? "null" : dso.getID());

        log.warn("***************************************************");
        log.warn("Moving " + dsoStr + " from " + sourceStr + " to " + destStr);
        log.warn("***************************************************");
    }
}