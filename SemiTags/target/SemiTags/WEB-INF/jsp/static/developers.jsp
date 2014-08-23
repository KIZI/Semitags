<%@ include file="/WEB-INF/jsp/include/include.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<%@ include file="/WEB-INF/jsp/include/head.jsp" %>
<title>SemiTags - Named Entity Recognition and Disambiguation Web Service</title>
</head>
<body>
<%@ include file="/WEB-INF/jsp/include/pageheader.jsp" %>
<h1>SemiTags - Named Entity Recognition and Disambiguation Web Service</h1>

<p>SemiTags provides a web form, where you can paste any plain text and the service shows recognized named entities.</p>

<h2>RESTful Web Service</h2>
<p>Apart from the web form, there is also a RESTful web service on the address: <a href="<c:url value="/rest/v1/recognize" />">http://ner.vse.cz<c:url value="/rest/v1/recognize" /></a></p>

<p>Currently the web service returns only entities with some disambiguation links. Recognized entities without a URI are not returned by the web service.</p>

<p>You can send a POST request to this URL with two parameters:</p>
<ul>
    <li>language - Either de or nl (default language is German - de).</li>
    <li>text - A plain text, where you want the entities to be recognized.</li>
</ul>

<p>The response returns an XML representing recognized entities. Each entity has following properties:</p>
<ul>
    <li>name - Name of the entity.</li>
    <li>occurrence - Attributes start and end denote the offset of an entity in the original text (a response may contain multiple occurrences). The offset is counted from 0 and is inclusive - start and end are offesets of the first and last character respectively (e.g. in 'In Prague on Monday' for 'Prague' start = 3 and end = 8).</li>
    <li>confidence - Absolute number denoting how often the entity occurs with other entities from the same text. In the future, this number will be normalized.</li>
    <li>dbpediaUri, wikipediaUri - Identifiers of the entity.</li>
    <li>type - Type of named entity (see the <a href="<c:url value="/app/static/types" />">list of types</a>).</li>
</ul>

<h2>Example Call</h2>
<pre>
curl -d "language=de&text=Jetzt in Brandenburg Aktuell Flughafen Chefs legen Rechenschaft vor Aufsichtsrat ab Brandenburger Fans schockiert über Hertha Debakel und neue Erkenntnisse im Mordfall Scholl 
Guten Abend meine Damen und Herren willkommen zu Brandenburg Aktuell die Erwartungen sind groß heute sollen Fakten auf den Tisch wie es mit dem neuen Flughafen weitergeht so war es versprochen worden die Projektgruppe tagt und der Aufsichtsrat unser Flughafen Experte Ludger Smolker ist in Schönefeld ist denn schon etwas von den Beratungen nach außen gedrungen Ludger 
na die Gerüchteküche brodelt also es wird schon gesagt dass möglicherweise einer der beiden äh Geschäftsführer seinen Hut 
will muss möglicherweise heute haben das wurde gesagt dass es möglicherweise haben äh wirkender Tsd nutzten zweitausend dreizehn ein Neuner feste wehende dortige sagt das alles Gerüchte genauso weiß man nicht ich habe vor zwei Dreiminuten normal mit Rolf Funkel den Flughafensprecher angeredet 
sie hat mir sagt dass äh eben nicht nur die Aufsichtsratsmitglieder Mitte Geschäftsführung bewegen zumal das Plan eingeladen sind das Airlines verweisen 
aufhören dann angesprochen werden dass man sehr dicht erhielt nun durch das ganze Debakel durchführt durch gelten dass diese Sitzung keinen wirklich dessen nach Stunden wissen haben unterstellten haben Stunden dauern kann 
ein darüberhinaus äh kann man wirklich auf feststellen Mängelliste ist sehr langen werden so auch Postminister dem achten Mai erfahren hat seitdem der Eröffnungstermin verschoben wurde ähm sage sehr viel zu bereden was alles passiert ist das Angelegenheit zusammengestellt 
achter Mai Paukenschlag große Pressekonferenz nun diese Herren Linie blamierten sich gewaltig die Flughafen Eröffnung geplatzt 
bei Millau nicht das stocksauer bin kein guter Tag für die 
Bürgerinnen." http://ner.vse.cz/SemiTags/rest/v1/recognize
</pre>

<h2>Example Response</h2>
<pre>
&lt;?xml version="1.0" encoding="UTF-8" standalone="yes"?&gt;
&lt;namedEntities&gt;
    &lt;namedEntity&gt;
        &lt;occurrence start="9" end="19" /&gt;
        &lt;occurrence start="84" end="94" /&gt;
        &lt;occurrence start="225" end="235" /&gt;
        &lt;confidence&gt;1264&lt;/confidence&gt;
        &lt;dbpediaUri&gt;http://de.dbpedia.org/wiki/Brandenburg&lt;/dbpediaUri&gt;
        &lt;name&gt;Brandenburg&lt;/name&gt;
        &lt;type&gt;location&lt;/type&gt;
        &lt;wikipediaUri&gt;http://de.wikipedia.org/wiki/Brandenburg&lt;/wikipediaUri&gt;
    &lt;/namedEntity&gt;
&lt;/namedEntities&gt; 
</pre>

</body>
</html>