/*
 * DSpaceObject.java
 *
 * Version: $Revision: 3241 $
 *
 * Date: $Date: 2008-07-09 15:47:10 +0100 (Wed, 09 Jul 2008) $
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
package org.dspace.content;

import java.sql.SQLException;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.log4j.Logger;
import org.dspace.core.Context;
import org.dspace.uri.ExternalIdentifier;
import org.dspace.uri.ObjectIdentifier;
import org.dspace.uri.SimpleIdentifier;
import org.dspace.uri.UnsupportedIdentifierException;

import java.util.ArrayList;
import java.util.List;
import org.dspace.dao.jena.GlobalDAOJena;
import org.dspace.metadata.URIResource;
import org.dspace.metadata.Value;

/**
 * Abstract base class for DSpace objects
 */
public abstract class DSpaceObjectCore implements DSpaceObject
{
    private static Logger log = Logger.getLogger(DSpaceObject.class);

    // accumulate information to add to "detail" element of content Event,
    // e.g. to document metadata fields touched, etc.
    private StringBuffer eventDetails = null;
    
    protected Context context;
    protected int id;
    protected ObjectIdentifier oid;
    protected List<ExternalIdentifier> identifiers;

    protected void clearDetails()
    {
        eventDetails = null;
    }
    
    protected void addDetails( String detail )
    {
        if ( eventDetails == null )
        {
            eventDetails = new StringBuffer( detail );
        } else
        {
            eventDetails.append( ", " ).append( detail );
        }
    }
    
    protected String getDetails()
    {
        return ( eventDetails == null ? null : eventDetails.toString() );
    }
    
    public abstract int getType();
    
    public int getID()
    {
        return id;
    }

    public SimpleIdentifier getSimpleIdentifier()
    {
        return oid;
    }

    public void setSimpleIdentifier( SimpleIdentifier sid )
            throws UnsupportedIdentifierException
    {
        if ( sid instanceof ObjectIdentifier )
        {
            this.setIdentifier( (ObjectIdentifier) sid );
        } else
        {
            throw new UnsupportedIdentifierException( "DSpaceObjects must use ObjectIdentifiers, not SimpleIdentifiers" );
        }
    }

    public ObjectIdentifier getIdentifier()
    {
        return oid;
    }

    public void setIdentifier( ObjectIdentifier oid )
    {
        // ensure that the identifier is configured for the item
        this.oid = oid;
    }
    
    @Deprecated
    public ExternalIdentifier getExternalIdentifier()
    {
        if ( ( identifiers != null ) && ( identifiers.size() > 0 ) )
        {
            return identifiers.get( 0 );
        } else
        {
            log.warn( "no external identifiers found. type=" + getType() +
                      ", id=" + getID() );
            return null;
        }
    }

    public List<ExternalIdentifier> getExternalIdentifiers()
    {
        if ( identifiers == null )
        {
            identifiers = new ArrayList<ExternalIdentifier>();
        }

        return identifiers;
    }

    public void addExternalIdentifier( ExternalIdentifier identifier )
            throws UnsupportedIdentifierException
    {
        identifier.setObjectIdentifier( this.getIdentifier() );
        this.identifiers.add( identifier );
    }

    public void setExternalIdentifiers( List<ExternalIdentifier> identifiers )
            throws UnsupportedIdentifierException
    {
        for ( ExternalIdentifier eid : identifiers )
        {
            eid.setObjectIdentifier( this.getIdentifier() );
        }
        this.identifiers = identifiers;
    }
    
    public abstract String getName();

    ////////////////////////////////////////////////////////////////////
    // URIResource methods
    ////////////////////////////////////////////////////////////////////
    private GlobalDAOJena daoj;
    public String getURI()
    {
        try
        {
            if ( daoj == null )
                daoj = context.getGlobalDAO() instanceof GlobalDAOJena
                        ? (GlobalDAOJena) context.getGlobalDAO()
                        : new GlobalDAOJena();
            return daoj.getResource( this ).getURI();
        } catch ( SQLException ex )
        {
            log.error( ex );
        }
        return "";
    }
    
    public String getNameSpace()
    {
        try
        {
            if ( daoj == null )
                daoj = context.getGlobalDAO() instanceof GlobalDAOJena
                        ? (GlobalDAOJena) context.getGlobalDAO()
                        : new GlobalDAOJena();
            return daoj.getResource( this ).getNameSpace();
        } catch ( SQLException ex )
        {
            log.error( ex );
        }
        return "";
    }
    
    public String getLocalName()
    {
        try
        {
            if ( daoj == null )
                daoj = context.getGlobalDAO() instanceof GlobalDAOJena
                        ? (GlobalDAOJena) context.getGlobalDAO()
                        : new GlobalDAOJena();
            return daoj.getResource( this ).getLocalName();
        } catch ( SQLException ex )
        {
            log.error( ex );
        }
        return "";
    }
    
    public boolean isLiteralValue()
    {
        return false;
    }
    
    public int compareTo( Value o )
    {
        return o instanceof URIResource ? 
                ( (URIResource) o ).getURI().compareTo( getURI() )
                : -1;
    }

    ////////////////////////////////////////////////////////////////////
    // Utility methods
    ////////////////////////////////////////////////////////////////////
    public String toString()
    {
        return ToStringBuilder.reflectionToString( this,
                                            ToStringStyle.MULTI_LINE_STYLE );
    }

    public boolean equals( DSpaceObject other )
    {
        if ( this.getType() == other.getType() )
        {
            if ( this.getID() == other.getID() )
            {
                return true;
            }
        }

        return false;
    }

    public boolean contains( List<? extends DSpaceObject> dsos,
                              DSpaceObject dso )
    {
        for ( DSpaceObject obj : dsos )
        {
            if ( obj.equals( dso ) )
            {
                return true;
            }
        }
        return false;
    }

}
