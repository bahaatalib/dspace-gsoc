<%--
  - wizard-default-item.jsp
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
  - initial questions page for collection creation wizard
  -
  - attributes:
  -    collection - collection we're creating
  --%>

<%@ page import="org.dspace.content.MetadataField" %>
<%@ page import="org.dspace.app.webui.servlet.admin.CollectionWizardServlet" %>
<%@ page import="org.dspace.content.Collection" %>

<%@ page contentType="text/html;charset=UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"
    prefix="fmt" %>
    

<%@ taglib uri="http://www.dspace.org/dspace-tags.tld" prefix="dspace" %>

<%  Collection collection = (Collection) request.getAttribute("collection");
    MetadataField[] dcTypes = (MetadataField[]) request.getAttribute("dctypes"); %>

<dspace:layout locbar="off"
               navbar="off"
               titlekey="jsp.dspace-admin.wizard-default-item.title"
               nocache="true">
  

<table width="95%">
    <tr>
      <td>
	<%-- <h1>Enter Default Item Metadata</h1> --%>
	<h1><fmt:message key="jsp.dspace-admin.wizard-default-item.enter"/></h1>
      </td>
      <td class="standard" align="right">
        <dspace:popup page="/help/site-admin.html#wizard_default"><fmt:message key="jsp.help"/></dspace:popup>
      </td>
    </tr>
  </table>
	<%-- <p>Whenever a new submission is started in this collection, it will have the
	metadata you entered below already filled out.</p> --%>
	<p><fmt:message key="jsp.dspace-admin.wizard-default-item.text1"/></p>
	
	<%-- <p>You can leave as many fields blank as you like.</p> --%>
	<p><fmt:message key="jsp.dspace-admin.wizard-default-item.text2"/></p>
	
    <form method="post" action="<%= request.getContextPath() %>/tools/collection-wizard">
        <center><table class="miscTable" summary="Enter default metadata table">
            <tr>
                <%-- <th class="oddRowOddCol"><strong>Dublin Core Field</strong></th> --%>
                <th id="t1" class="oddRowOddCol"><strong><fmt:message key="jsp.dspace-admin.wizard-default-item.dcore"/></strong></th>
                <%-- <th class="oddRowEvenCol"><strong>Value</strong></th> --%>
                <th id="t2" class="oddRowEvenCol"><strong><fmt:message key="jsp.dspace-admin.wizard-default-item.value"/></strong></th>
                <%-- <th class="oddRowOddCol"><strong>Language</strong></th> --%>
                <th id="t3" class="oddRowOddCol"><strong><fmt:message key="jsp.dspace-admin.wizard-default-item.language"/></strong></th>
            </tr>
<%
    String row = "even";

    for (int i = 0; i < 10; i++)
	{
	 %>
			<tr>
			    <td headers="t1" class="<%= row %>RowOddCol"><select name="dctype_<%= i %>">
			    	<%-- <option value="-1">Select field...</option> --%>
			    	<option value="-1"><fmt:message key="jsp.dspace-admin.wizard-default-item.select"/></option>
<%
		for (int dc = 0; dc < dcTypes.length; dc++)
		{ %>
					<option value="<%= dcTypes[dc].getFieldID() %>"><%= dcTypes[dc].getQualifier() == null ?
			dcTypes[dc].getElement() : dcTypes[dc].getElement() + "." + dcTypes[dc].getQualifier() %></option>
<%      } %>
				</select></td>
				<td headers="t2" class="<%= row %>RowEvenCol">
					<input type="text" name="value_<%= i %>" size="40" />
				</td>
				<td headers="t3" class="<%= row %>RowEvenCol">
					<input type="text" name="lang_<%= i %>" size="5" maxlength="5" />
				</td>
			</tr>
<%	} %>
		</table>
	</center>
       <p>&nbsp;</p>

<%-- Hidden fields needed for servlet to know which collection and page to deal with --%>
        <input type="hidden" name="collection_id" value="<%= collection.getID() %>" />
        <input type="hidden" name="stage" value="<%= CollectionWizardServlet.DEFAULT_ITEM %>" />

        <center>
            <table border="0" width="80%">
                <tr>
                    <td width="100%">&nbsp;
                        
                    </td>
                    <td>
                        <%-- <input type="submit" name="submit_next" value="Next &gt;" /> --%>
                        <input type="submit" name="submit_next" value="<fmt:message key="jsp.dspace-admin.general.next.button"/>" />
                    </td>
                </tr>
            </table>
        </center>
    </form>

</dspace:layout>
