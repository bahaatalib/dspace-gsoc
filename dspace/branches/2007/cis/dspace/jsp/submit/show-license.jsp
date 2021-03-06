<%--
  - show-license.jsp
  -
  - Version: $Revision$
  -
  - Date: $Date$
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
  - Show the user a license which they may grant or reject
  -
  - Attributes to pass in:
  -    submission.info  - the SubmissionInfo object
  -    license          - the license text to display
  --%>

<%@ page contentType="text/html;charset=UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"
    prefix="fmt" %>
    

<%@ page import="org.dspace.app.webui.servlet.SubmitServlet" %>
<%@ page import="org.dspace.app.webui.util.SubmissionInfo" %>

<%@ taglib uri="http://www.dspace.org/dspace-tags.tld" prefix="dspace" %>

<%
    SubmissionInfo si =
        (SubmissionInfo) request.getAttribute("submission.info");

    String license = (String) request.getAttribute("license");
%>

<dspace:layout locbar="off"
               navbar="off"
               titlekey="jsp.submit.show-license.title"
               nocache="true">

    <form action="<%= request.getContextPath() %>/submit" method="post">

        <jsp:include page="/submit/progressbar.jsp">
            <jsp:param name="current_stage" value="<%= SubmitServlet.GRANT_LICENSE %>"/>
            <jsp:param name="stage_reached" value="<%= SubmitServlet.getStepReached(si) %>"/>
             <jsp:param name="md_pages" value="<%= si.numMetadataPages %>"/>
        </jsp:include>

	<div><fmt:message key="jsp.submit.show-license.info1"/>
        &nbsp;&nbsp;<dspace:popup page="/help/index.html#license"><fmt:message key="jsp.morehelp"/></dspace:popup></div>

        <%-- <p><strong>Not granting the license will not delete your submission.</strong>
        Your item will remain in your "My DSpace" page.  You can then either remove
        the submission from the system, or agree to the license later once any
        queries you might have are resolved.</p> --%>
		<p><fmt:message key="jsp.submit.show-license.info2"/></p>

        <table class="miscTable" align="center">
            <tr>
                <td class="oddRowEvenCol">
                    <pre><%= license %></pre>
                </td>
            </tr>
        </table>

        <%= SubmitServlet.getSubmissionParameters(si) %>
        <input type="hidden" name="step" value="<%= SubmitServlet.GRANT_LICENSE %>" />

        <center>
	    <p><input type="submit" name="submit_grant" value="<fmt:message key="jsp.submit.show-license.grant.button"/>" /></p>
            <p><input type="submit" name="submit_reject" value="<fmt:message key="jsp.submit.show-license.notgrant.button"/>" /></p>          
        </center>
    </form>
</dspace:layout>
