<%--
  - no-theses.jsp
  -
  - Version: $Revision$
  -
  - Date: $Date$
  -
  - Copyright (c) 2001, Hewlett-Packard Company and Massachusetts
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
  - "No Theses" message
  -
  - This page displays a message informing the user that theses are not
  - presently accepted in DSpace, and that their submission has been removed.
  - FIX-ME: MIT-SPECIFIC
  --%>
<%@ taglib uri="http://www.dspace.org/dspace-tags.tld" prefix="dspace" %>

<dspace:layout title="Theses Not Accepted in DSpace">

    <H1>Theses Not Accepted in DSpace</H1>

    <P>DSpace does not currently accept individually-submitted
    theses, but you are encouraged to use the separate electronic thesis
    submission site supported by the Libraries and by MIT Information Systems to
    submit your thesis to the <a href="http://thesis.mit.edu">Digital Library of
    MIT Theses</a>.  To learn how to submit your thesis to that system, see <a
    href="http://web.mit.edu/etheses/www/etheses-home.html">Submitting
    an Electronic Thesis at MIT</a>.</P>

    <P>Because DSpace does not accept individually-submitted theses, your
    submission will not proceed; any files you have uploaded for the current item
    will not be stored.</P> 

    <P>Please note that printed copies of your thesis are still the official
    requirement for your degree.  Due to important legal and record-keeping
    reasons, it is likely that in the future DSpace will work directly with the
    electronic thesis system to load groups of theses which have been officially
    vetted and approved.  Thanks for understanding.</P> 

    <P>For more information please <strong>contact the DSpace site
    administrators</strong>:</P>

    <dspace:include page="/components/contact-info.jsp" />

    <P>Thank you for your interest in DSpace!</P>

</dspace:layout>
