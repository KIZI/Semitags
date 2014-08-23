![SemiTags](http://ner.vse.cz/SemiTags/images/SemiTags.png)

Semitags
========

Project focused on named entity recogniton. Contains several modules for named entity recognition and linking support.

## Contained projects

### NerService

Last version of the REST service for named entity recognition and linking. For named entity uses older SemiTags project.

### NerIndexingSupport

Last version of utility scripts and indexing pipeline to support Wikipedia indexing for later disambiguation.

### SemiTags

Older version of our named entity recognition and disambiguation tool. Still supports multiple languages (English, German and Dutch) and provides REST service for pure named entity recognition. For named entity recognition Stanford Named Entity Recognizer (http://nlp.stanford.edu/software/CRF-NER.shtml) is used.

The webservice is publicly available at: http://ner.vse.cz/SemiTags/

### NamedEntitiesRecognition

Java project with support classes for multilingual named entity recognition. Implements three named entity linking methods (TF-ICF back of words context represnetation, Sum co-occurrence disambiguation and subject-verb-object disambiguation).

### NamedEntitiesIndex

Java project that supports Wikipedia dump files parsing and basic indexing. Advanced data cleaning is then implemented in NerIndexingSupport project.
