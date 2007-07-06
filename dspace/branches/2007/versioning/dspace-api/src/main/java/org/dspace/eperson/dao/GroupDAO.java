/*
 * GroupDAO.java
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

import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.eperson.Group;
import org.dspace.content.uri.ObjectIdentifier;

/**
 * @author James Rutherford
 */
public abstract class GroupDAO
{
    protected Logger log = Logger.getLogger(GroupDAO.class);

    protected Context context;

    public abstract Group create() throws AuthorizeException;

    // FIXME: This should be called something else, but I can't think of
    // anything suitable. The reason this can't go in create() is because we
    // need access to the object that was created, but we can't reach into the
    // subclass to get it (storing it as a protected member variable would be
    // even more filthy).
    public Group create(int id, UUID uuid) throws AuthorizeException
    {
//        Group group = new Group(context, id);
//
//        group.setIdentifier(new ObjectIdentifier(uuid));
//
//        update(group);
//
//        return group;
        return null;
    }

    public Group retrieve(int id)
    {
        return (Group) context.fromCache(Group.class, id);
    }

    public Group retrieve(UUID uuid)
    {
        return null;
    }

    public void update(Group group) throws AuthorizeException
    {
    }

    public void delete(int id) throws AuthorizeException
    {
    }
}