# NerIndexingSupport

This project contains NodeJS scripts that support data cleaning and statistics counting of indexed Wikipedia dumps.

## Important folders

### tac
Contains utility scripts to preprocess TAC 2013 query and knowledge base files.

### wiki_benchmark

Contains scripts building a pipeline for Wikipedia benchmark creation. 

### Example call of the rest service
```
curl --data "preprocess=norm&text=Despite the crisis, Palestinian President Mahmoud Abbas was in Qatar meeting Hamas political chief Khaled Mashaal to push him to return to a cease-fire, and to encourage Qatar to support Egyptian cease-fire efforts, a Palestinian official said." http://nlp.vse.cz:8081/recognize
```

### Possible values of preprocess parameter 

* norm - Using Normalized most frequent sense disambiguation
* sum - Sum co-occurerence disamabiguation.
* max - Maximum co-occurrence disambiguation.

### Optional parameters

* queryEntity - The entity surface form required to be disambiguated in the context of the submitted text.
