<%@ include file="/WEB-INF/jsp/include/include.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<%@ include file="/WEB-INF/jsp/include/head.jsp" %>
<title>SemiTags - Named Entity Recognition and Disambiguation Web Service</title>
</head>
<body>
<%@ include file="/WEB-INF/jsp/include/pageheader.jsp" %>
<h1>List of recognized types in SemiTags</h1>

<p>Please note that this is a preliminary version of the list of types. With the support of link discovery and disambiguation to DBpedia entities (that we plan to add in the near future), we will add more types corresponding to DBpedia ontology.</p>

<h2>Currently supported types</h2>
<ul>
    <li>person - Person name.</li>
    <li>location - Name of a location (e.g. city, country).</li>
    <li>organization - Name of an organization (e.g. company, political party).</li>
    <li>miscellaneous - Another type of an entity not falling in previous three categories.</li>
</ul>


</body>
</html>