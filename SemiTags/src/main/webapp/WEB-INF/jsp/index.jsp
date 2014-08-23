<%@ include file="/WEB-INF/jsp/include/include.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<%@ include file="/WEB-INF/jsp/include/head.jsp" %>
<title>Named Entity Recognition</title>
</head>
<body>
<%@ include file="/WEB-INF/jsp/include/pageheader.jsp" %>
<h1>Named Entity Recognition</h1>
    <div id="form">
        <form action="<c:url value="/app/index/" />" method="post">
            <textarea name="recText"><c:out value="${recText}"></c:out></textarea>
            <div>
                <select name="lang">
                    <c:forEach var="language" items="${languages}">
                        <c:choose>
                            <c:when test="${language == selectedLanguage}">
                                <option selected="selected"><c:out value="${language}" /></option>
                            </c:when>
                            <c:otherwise>
                                <option><c:out value="${language}" /></option>
                            </c:otherwise>
                        </c:choose>
                    </c:forEach>
                </select>
                <input type="submit" value="Recognize" />
            </div>
        </form>
    </div>

    <div id="rightcol">
        <div id="identified">
            <h3>Identified Named Entities</h3>
            <ul>
            <c:forEach var="identifiedEntity" items="${identifiedEntities}">
                <li><c:out value="${identifiedEntity.name}" /> (<c:out value="${identifiedEntity.type}" />)</li>
            </c:forEach>
            </ul>
        </div>
        <div id="disambiguated">
            <h3>Disambiguated Named Entities</h3>
            <c:forEach var="disambiguatedEntity" items="${disambiguatedEntities}">
            <ul>
                <li><c:out value="${disambiguatedEntity.name}" /> ... <a href="<c:out value="${disambiguatedEntity.uri}"></c:out>"><c:out value="${disambiguatedEntity.uri}"></c:out></a></li>
            </ul>
            </c:forEach>
        </div>
    </div> 
    
    <div id="ack">
        <h3>Acknowledgement</h3>
        <p>SemiTags uses for preprocessing and identification of named entities in unstructured texts <a href="http://nlp.stanford.edu/software/CRF-NER.shtml">Stanford Named Entity Recognizer</a>. The models for German language are trained based on <a href="http://www.cnts.ua.ac.be/conll2003/ner/">CONLL 2003</a> datasets. Dutch models are trained on datasets provided for <a href="http://www.clips.ua.ac.be/conll2002/ner/">CONLL-2002 shared task</a>.
    </div>    
</body>
</html>