/*
 * GroupEditServlet.java
 */

package org.dspace.app.webui.servlet.admin;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;

import org.dspace.app.webui.servlet.DSpaceServlet;
import org.dspace.app.webui.util.JSPManager;
import org.dspace.app.webui.util.UIUtil;

import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.authorize.ResourcePolicy;
import org.dspace.content.Collection;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;

/**
 * Servlet for editing groups
 * @author dstuve
 * @version $Revision$
 */
public class GroupEditServlet extends DSpaceServlet
{
    protected void doDSGet(Context c,
                    HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException, SQLException, AuthorizeException
    {
        // show the main page (select groups)
        showMainPage(c, request, response);
    }
    
    protected void doDSPost(Context c,
                    HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException, SQLException, AuthorizeException
    {
        String button = UIUtil.getSubmitButton(request, "submit");
        
        if( button.equals("submit_edit") || button.equals("submit_add_eperson_cancel") )
        {
            // just chosen a group to edit - get group and pass it to group_edit.jsp
            Group group = Group.find(c, UIUtil.getIntParameter(request, "group_id"));
            
            request.setAttribute("group", group);
            request.setAttribute("members", group.getMembers());
            
            JSPManager.showJSP(request, response, "/admin/group_edit.jsp" );
        }
        else if( button.equals( "submit_add" ) )
        {
            // want to add a group - create a blank one, and pass to group_edit.jsp
            Group group = Group.create(c);
            
            group.setName("new group");
            group.update();
            
            request.setAttribute("group", group);
            request.setAttribute("members", group.getMembers());
            JSPManager.showJSP(request, response, "/admin/group_edit.jsp" );
        }
        else if( button.equals("submit_change_name") )
        {
            // alter group's name and return to group_edit.jsp
            Group group = Group.find(c, UIUtil.getIntParameter(request, "group_id"));
            
            group.setName( request.getParameter("group_name") );
            group.update();
            
            request.setAttribute("group", group);
            request.setAttribute("members", group.getMembers());
            JSPManager.showJSP(request, response, "/admin/group_edit.jsp" );
        }
        else if( button.equals("submit_add_eperson_add") )
        {
            // add a user to the group, return to group_edit.jsp
            Group group = Group.find(c, UIUtil.getIntParameter(request, "group_id"));
            EPerson e   = EPerson.find(c,UIUtil.getIntParameter(request,"eperson_id") );            

            group.addMember( e );
            group.update();
            
            request.setAttribute("group", group);
            request.setAttribute("members", group.getMembers());
            JSPManager.showJSP(request, response, "/admin/group_edit.jsp" );
        }
        else if( button.equals( "submit_add_eperson") )
        {
            // go to group_eperson_select.jsp
            Group group = Group.find(c, UIUtil.getIntParameter(request, "group_id"));
            EPerson [] epeople = EPerson.findAll(c, EPerson.EMAIL);
            
            request.setAttribute("group",   group  );
            request.setAttribute("epeople", epeople);
            JSPManager.showJSP(request, response, "/admin/group_eperson_select.jsp" );
        }
        else if( button.equals( "submit_remove_eperson" ) )
        {
            // remove the eperson, show group_edit.jsp
            Group group = Group.find(c, UIUtil.getIntParameter(request, "group_id"));
            EPerson e   = EPerson.find(c,UIUtil.getIntParameter(request,"eperson_id") );            
            
            group.removeMember( e );
            group.update();
            
            request.setAttribute("group", group);
            request.setAttribute("members", group.getMembers());
            JSPManager.showJSP(request, response, "/admin/group_edit.jsp" );
        }
        else if( button.equals( "submit_group_delete" ) )
        {
            // delete group, return to group_list.jsp
            Group group = Group.find(c,
                UIUtil.getIntParameter(request, "group_id"));
            
            group.delete();
            
            showMainPage(c, request, response);
        }
        else
        {
            // all other input, show main page
            showMainPage(c, request, response);
        }
        c.complete();

    }
    
    private void showMainPage(Context c,
                    HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException, SQLException, AuthorizeException
    {
        Group [] groups = Group.findAll(c, Group.NAME);
        
//        if( groups == null ) { System.out.println("groups are null"); }
//        else System.out.println("# of groups: " + groups.length);

        request.setAttribute("groups", groups);
        
        JSPManager.showJSP(request, response, "/admin/group_list.jsp" );
    }   
}

