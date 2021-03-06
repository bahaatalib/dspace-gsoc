/*
 * ObjectIdentifier.java
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
package org.dspace.uri;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.log4j.Logger;

import org.dspace.content.DSpaceObject;
import org.dspace.content.dao.BitstreamDAO;
import org.dspace.content.dao.BitstreamDAOFactory;
import org.dspace.content.dao.BundleDAO;
import org.dspace.content.dao.BundleDAOFactory;
import org.dspace.content.dao.CollectionDAO;
import org.dspace.content.dao.CollectionDAOFactory;
import org.dspace.content.dao.CommunityDAO;
import org.dspace.content.dao.CommunityDAOFactory;
import org.dspace.content.dao.ItemDAO;
import org.dspace.content.dao.ItemDAOFactory;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Constants;
import org.dspace.core.Context;

/**
 * @author James Rutherford
 */
public class ObjectIdentifier
{
    private static Logger log = Logger.getLogger(ObjectIdentifier.class);

    private int resourceID;
    private int resourceTypeID;
    private UUID uuid;

    private Type type;

    public ObjectIdentifier()
    {
    }

    public ObjectIdentifier(int resourceID, int resourceTypeID)
    {
        this.type = Type.INTS;
        this.resourceID = resourceID;
        this.resourceTypeID = resourceTypeID;
    }

    public ObjectIdentifier(UUID uuid)
    {
        this.type = Type.UUID;
        this.uuid = uuid;
    }

    public ObjectIdentifier(Type type, String value)
    {
        this.type = type;
        
        switch (type)
        {
            case UUID:
                // value will be a string representation of a UUID
                this.uuid = UUID.fromString(value);
                break;
            case INTS:
                // value will be (eg) "3/12"
                this.resourceID =
                    Integer.parseInt(value.substring(value.indexOf('/') + 1));
                this.resourceTypeID =
                    Integer.parseInt(value.substring(0, value.indexOf('/')));
                break;
            default:
                throw new RuntimeException(":(");
        }
    }

    public static ObjectIdentifier fromString(String canonicalForm)
    {
        for (Type t : Type.values())
        {
            String ns = t.getNamespace();
            if (canonicalForm.startsWith(ns))
            {
                String value = canonicalForm.substring(ns.length() + 1);
                if ((value == null) || value.equals(""))
                {
                    break;
                }
                else if (t.equals(Type.INTS) && value.indexOf('/') == -1)
                {
                    // String must be of the form x/y with x & y ints
                    break;
                }

                return new ObjectIdentifier(t, value);
            }
        }

        return null;
    }

    public DSpaceObject getObject(Context context)
    {
        CommunityDAO communityDAO = CommunityDAOFactory.getInstance(context);
        CollectionDAO collectionDAO =
            CollectionDAOFactory.getInstance(context);
        ItemDAO itemDAO = ItemDAOFactory.getInstance(context);
        BundleDAO bundleDAO = BundleDAOFactory.getInstance(context);
        BitstreamDAO bitstreamDAO = BitstreamDAOFactory.getInstance(context);

        switch (type)
        {
            case INTS:
                switch(resourceTypeID)
                {
                    case (Constants.BITSTREAM):
                        return bitstreamDAO.retrieve(resourceID);
                    case (Constants.BUNDLE):
                        return bundleDAO.retrieve(resourceID);
                    case (Constants.ITEM):
                        return itemDAO.retrieve(resourceID);
                    case (Constants.COLLECTION):
                        return collectionDAO.retrieve(resourceID);
                    case (Constants.COMMUNITY):
                        return communityDAO.retrieve(resourceID);
                    default:
                        throw new RuntimeException("Not a valid DSpaceObject type");
                }
            case UUID:
                // If we have a UUID, there is no indication of what type of
                // object it is attached to, so we just keep trying in sequence
                // until we get something. This isn't an ideal approach, and we
                // should probably re-order them to minimise lookups.
                DSpaceObject dso = bitstreamDAO.retrieve(uuid);

                if (dso == null)
                {
                    dso = bundleDAO.retrieve(uuid);
                }
                if (dso == null)
                {
                    dso = itemDAO.retrieve(uuid);
                }
                if (dso == null)
                {
                    dso = collectionDAO.retrieve(uuid);
                }
                if (dso == null)
                {
                    dso = communityDAO.retrieve(uuid);
                }

                if (dso == null)
                {
                    throw new RuntimeException("Couldn't find " + uuid);
                }
                else
                {
                    return dso;
                }
            default:
                throw new RuntimeException("Whoops!");
        }
    }

    public URL getURL()
    {
        // This is a bit of a hack to get an almost-URLEncoded form of the URL.
        // (See the FIXME below).
        String url = ConfigurationManager.getProperty("dspace.url") +
            "/resource/" + getCanonicalForm().replaceAll(":", "%3A");

        try
        {
            return new URL(url);

            // FIXME: The only reason I'm not doing this is because of the
            // issues Tomcat < version 6 has with encoded slashes in URLs (it
            // just refuses to parse them).
//          return new URL(base + "resource/" + URLEncoder.encode(value, "UTF-8"));
//        }
//        catch (UnsupportedEncodingException uee)
//        {
//            throw new RuntimeException(uee);
        }
        catch (MalformedURLException murle)
        {
            throw new RuntimeException(murle);
        }
    }

    public String getCanonicalForm()
    {
        String s = type.getNamespace() + ":";

        switch (type)
        {
            case INTS:
                s += resourceTypeID + "/" + resourceID;
                break;
            case UUID:
                s += uuid.toString();
                break;
            default:
                throw new RuntimeException("Whoops!");
        }

        return s;
    }

    public enum Type
    {
        INTS ("dsi"), // signifies a pair of integers (resource type + id)
        UUID ("uuid");

        private final String namespace;

        private Type(String namespace)
        {
            this.namespace = namespace;
        }

        public String getNamespace()
        {
            return namespace;
        }
    }

    ////////////////////////////////////////////////////////////////////
    // Utility methods
    ////////////////////////////////////////////////////////////////////

    public String toString()
    {
        return ToStringBuilder.reflectionToString(this,
                ToStringStyle.MULTI_LINE_STYLE);
    }

    public boolean equals(Object o)
    {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
