<%-- news-edit.jsp
  -
  - Copyright (c) 2002, Hewlett-Packard Company and Massachusetts
  - Institute of Technology.  All rights reserved.
  -
  - Redistribution and use in source and binary forms, with or without
  - modification, are permitted provided that the following conditions are
  - met:
  -
  - - Redistributions of source code must retain the above copyright
  - notice, this list of conditions and the following disclaimer.
  -
  - - Redistributions in binary form must reproduce the above copyright
  - notice, this list of conditions and the following disclaimer in the
  - documentation and/or other materials provided with the distribution.
  -
  - - Neither the name of the Hewlett-Packard Company nor the name of the
  - Massachusetts Institute of Technology nor the names of their
  - contributors may be used to endorse or promote products derived from
  - this software without specific prior written permission.
  -
  - THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  - ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  - LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  - A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
  - HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  - INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  - BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
  - OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  - ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
  - TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
  - USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
  - DAMAGE.
  --%>

<%--
  - News Edit Form JSP
  -
  - Attributes:
   --%>

<%@ page contentType="text/html;charset=UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"
    prefix="fmt" %>

<%@ taglib uri="http://www.dspace.org/dspace-tags.tld" prefix="dspace" %>

<%@ page import="org.dspace.app.webui.servlet.admin.NewsEditServlet" %>
<%@ page import="org.dspace.core.Constants" %>

<%
    Integer position = (Integer)request.getAttribute("position");

    //get the existing news
    String news = (String)request.getAttribute("news");

    if (news == null)
    {
        news = "";
    }

%>

<dspace:layout titlekey="jsp.dspace-admin.news-edit.title"
               navbar="admin"
               locbar="link"
               parenttitlekey="jsp.administer"
               parentlink="/dspace-admin">

    <%-- <H1>News Editor</H1> --%>
    <H1><fmt:message key="jsp.dspace-admin.news-edit.heading"/></H1>

    <%-- <p>Add or edit text in the box below to have it appear
    in the <strong><%= positionStr%></strong> of the DSpace home page.</p> --%>
    <p><fmt:message key="jsp.dspace-admin.news-edit.text1"/><strong>
<% if (position.intValue() == Constants.NEWS_TOP)
   { %>
    <fmt:message key="jsp.dspace-admin.news-edit.positionStr.top"/>
<% }
   else
   { %>
    <fmt:message key="jsp.dspace-admin.news-edit.positionStr.side"/>
<% } %>
    </strong> <fmt:message key="jsp.dspace-admin.news-edit.text2"/></p>

    <%-- <p>You may format the text using HTML tags, but please note that the HTML will not be validated here.</p> --%>
    <p><fmt:message key="jsp.dspace-admin.news-edit.text3"/></p>

    <form action="<%= request.getContextPath() %>/dspace-admin/news-edit" method="POST">
        <center>
            <table>
                <tr>
                   <%--  <td class="submitFormLabel">News:</td> --%>
                    <td class="submitFormLabel"><fmt:message key="jsp.dspace-admin.news-edit.news"/></td>
                    <td><textarea name="news" rows="10" cols="50" wrap=soft><%= news %></textarea></td>
                </tr>
                <tr>
                    <td colspan="2" align="center">
                    <input type="HIDDEN" name="position" value='<%= position.intValue()%>'>
                    <%-- <input type="SUBMIT" name="submit_save" value="Save"> --%>
                    <input type="SUBMIT" name="submit_save" value="<fmt:message key="jsp.dspace-admin.news-edit.save"/>">
                    <%-- <input type="SUBMIT" name="cancel" value="Cancel"> --%>
                    <input type="SUBMIT" name="cancel" value="<fmt:message key="jsp.dspace-admin.news-edit.cancel"/>">
                    </td>
                </tr>
            </table>
        </center>
    </form>
</dspace:layout>
