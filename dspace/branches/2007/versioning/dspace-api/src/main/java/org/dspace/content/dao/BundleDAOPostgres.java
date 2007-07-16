/*
 * BundleDAOPostgres.java
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
package org.dspace.content.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.core.Context;
import org.dspace.core.Constants;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.Item;
import org.dspace.content.uri.ObjectIdentifier;
import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.storage.rdbms.TableRow;
import org.dspace.storage.rdbms.TableRowIterator;

/**
 * @author James Rutherford
 */
public class BundleDAOPostgres extends BundleDAO
{
    public BundleDAOPostgres(Context context)
    {
        this.context = context;
        this.bitstreamDAO = BitstreamDAOFactory.getInstance(context);
    }

    @Override
    public Bundle create() throws AuthorizeException
    {
        try
        {
            UUID uuid = UUID.randomUUID();

            TableRow row = DatabaseManager.create(context, "bundle");
            row.setColumn("uuid", uuid.toString());
            DatabaseManager.update(context, row);

            int id = row.getIntColumn("bundle_id");

            return super.create(id, uuid);
        }
        catch (SQLException sqle)
        {
            throw new RuntimeException(sqle);
        }
    }

    @Override
    public Bundle retrieve(int id)
    {
        Bundle bundle = super.retrieve(id);

        if (bundle != null)
        {
            return bundle;
        }

        try
        {
            TableRow row = DatabaseManager.find(context, "bundle", id);

            if (row == null)
            {
                log.warn("bundle " + id + " not found");
                return null;
            }
            else
            {
                return retrieve(row);
            }
        }
        catch (SQLException sqle)
        {
            throw new RuntimeException(sqle);
        }
    }

    @Override
    public Bundle retrieve(UUID uuid)
    {
        Bundle bundle = super.retrieve(uuid);

        if (bundle != null)
        {
            return bundle;
        }

        try
        {
            TableRow row = DatabaseManager.findByUnique(context, "bundle",
                    "uuid", uuid.toString());

            if (row == null)
            {
                log.warn("bundle " + uuid + " not found");
                return null;
            }
            else
            {
                return retrieve(row);
            }
        }
        catch (SQLException sqle)
        {
            throw new RuntimeException(sqle);
        }
    }

    private Bundle retrieve(TableRow row)
    {
        int id = row.getIntColumn("bundle_id");
        Bundle bundle = new Bundle(context, id);
        populateBundleFromTableRow(bundle, row);

        return bundle;
    }

    @Override
    public void update(Bundle bundle) throws AuthorizeException
    {
        super.update(bundle);

        try
        {
            TableRow row =
                DatabaseManager.find(context, "bundle", bundle.getID());

            if (row != null)
            {
                update(bundle, row);
            }
            else
            {
                throw new RuntimeException("Didn't find bundle " +
                        bundle.getID());
            }
        }
        catch (SQLException sqle)
        {
            throw new RuntimeException(sqle);
        }
    }

    private void update(Bundle bundle, TableRow bundleRow)
        throws AuthorizeException
    {
        try
        {
            bundleRow.setColumn("name", bundle.getName());

            if (bundle.getPrimaryBitstreamID() > 0)
            {
                bundleRow.setColumn("primary_bitstream_id",
                        bundle.getPrimaryBitstreamID());
            }
            else
            {
                bundleRow.setColumnNull("primary_bitstream_id");
            }

            DatabaseManager.update(context, bundleRow);
        }
        catch (SQLException sqle)
        {
            throw new RuntimeException(sqle);
        }
    }

    @Override
    public void delete(int id) throws AuthorizeException
    {
        super.delete(id);

        try
        {
            DatabaseManager.delete(context, "bundle", id);
        }
        catch (SQLException sqle)
        {
            throw new RuntimeException(":(");
        }
    }

    @Override
    public List<Bundle> getBundlesByItem(Item item)
    {
        try
        {
            TableRowIterator tri = DatabaseManager.query(context,
                    "SELECT bundle_id FROM item2bundle " +
                    "WHERE item_id = " + item.getID());

            List<Bundle> bundles = new ArrayList<Bundle>();

            for (TableRow row : tri.toList())
            {
                int id = row.getIntColumn("bundle_id");
                bundles.add(retrieve(id));
            }

            return bundles;
        }
        catch (SQLException sqle)
        {
            throw new RuntimeException(sqle);
        }
    }

    @Override
    public List<Bundle> getBundlesByBitstream(Bitstream bitstream)
    {
        try
        {
            TableRowIterator tri = DatabaseManager.query(context,
                    "SELECT bundle.* FROM bundle, bundle2bitstream " +
                    "WHERE bundle.bundle_id = bundle2bitstream.bundle_id " +
                    "AND bundle2bitstream.bitstream_id = " + bitstream.getID());

            List<Bundle> bundles = new ArrayList<Bundle>();

            for (TableRow row : tri.toList())
            {
                int id = row.getIntColumn("bundle_id");
                bundles.add(retrieve(id));
            }

            return bundles;
        }
        catch (SQLException sqle)
        {
            throw new RuntimeException(sqle);
        }
    }

    @Override
    public void link(Bundle bundle, Bitstream bitstream)
        throws AuthorizeException
    {
        if (!linked(bundle, bitstream))
        {
            super.link(bundle, bitstream);

            try
            {
                TableRow row = DatabaseManager.create(context,
                        "bundle2bitstream");
                row.setColumn("bundle_id", bundle.getID());
                row.setColumn("bitstream_id", bitstream.getID());
                DatabaseManager.update(context, row);
            }
            catch (SQLException sqle)
            {
                throw new RuntimeException(sqle);
            }
        }
    }

    @Override
    public void unlink(Bundle bundle, Bitstream bitstream)
        throws AuthorizeException
    {
        if (linked(bundle, bitstream))
        {
            super.unlink(bundle, bitstream);

            try
            {
                // Delete the mapping row
                DatabaseManager.updateQuery(context,
                        "DELETE FROM bundle2bitstream " +
                        "WHERE bundle_id = ? AND bitstream_id = ? ",
                        bundle.getID(), bitstream.getID());
            }
            catch (SQLException sqle)
            {
                throw new RuntimeException(sqle);
            }
        }
    }

    private boolean linked(Bundle bundle, Bitstream bitstream)
    {
        try
        {
            TableRowIterator tri = DatabaseManager.query(context,
                    "SELECT id FROM bundle2bitstream " +
                    "WHERE bundle_id = ? " + 
                    "AND bitstream_id = ? ",
                    bundle.getID(), bitstream.getID());

            return tri.hasNext();
        }
        catch (SQLException sqle)
        {
            throw new RuntimeException(sqle);
        }
    }

    ////////////////////////////////////////////////////////////////////
    // Utility methods
    ////////////////////////////////////////////////////////////////////

    private void populateBundleFromTableRow(Bundle bundle, TableRow row)
    {
        UUID uuid = UUID.fromString(row.getStringColumn("uuid"));
        List <Bitstream> bitstreams =
            bitstreamDAO.getBitstreamsByBundle(bundle);

        bundle.setIdentifier(new ObjectIdentifier(uuid));
        bundle.setName(row.getStringColumn("name"));
        bundle.setBitstreams(bitstreams);
    }
}

