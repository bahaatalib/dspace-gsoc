package org.dspace.metadata;

import org.dspace.content.DSpaceObject;

public interface Predicate extends Comparable<Predicate>, DSpaceObject
{

    public String getURI();

    public String getNameSpace();

    public String getLocalName();
    
}