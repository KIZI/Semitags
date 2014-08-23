# NamedEntitesIndex

Tool for Wikipedia dumps parsing and  indexing. Implements an older version of Wikipedia indeixng pipeline. New version 
is provided in NerIndexingSupport project.

## Example indexing pipeline assembled in bash

```bash
#!/bin/bash
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
LOG=$SCRIPT_DIR"/indexing.log"
PERCENT_TESTING_ARTICLES=10

function runIndexing {
    lang=$1
    echo "=======================================================================================" >> $LOG
    echo "=======================================================================================" >> $LOG
    date >> $LOG
    echo "Indexing Wikipedia - lang: "$lang >> $LOG
    java -Xmx5240m -jar $SCRIPT_DIR/indexInCSV.jar $SCRIPT_DIR"/wikidumps/"$lang"wiki/dump-pagearticles" "http://"$lang".wikipedia.org/wiki/" $SCRIPT_DIR"/csv/"$lang"csv/" >> $LOG 2>&1
    date >> $LOG
    echo "Removing duplicates" >> $LOG 
    java -Xmx5240m -jar $SCRIPT_DIR/removeDuplicates.jar $SCRIPT_DIR"/csv/"$lang"csv" $SCRIPT_DIR"/csv/"$lang"nodups/" "http://"$lang".wikipedia.org/wiki/" >> $LOG 2>&1
    date >> $LOG
    echo "Copying remaining files from " $SCRIPT_DIR"/csv/"$lang"csv/ to "$SCRIPT_DIR"/csv/"$lang"nodups/" >> $LOG
    cp  $SCRIPT_DIR"/csv/"$lang"csv/article.csv" $SCRIPT_DIR"/csv/"$lang"nodups/article.csv"
    cp  $SCRIPT_DIR"/csv/"$lang"csv/paragraph.csv" $SCRIPT_DIR"/csv/"$lang"nodups/paragraph.csv"
    cp  $SCRIPT_DIR"/csv/"$lang"csv/surface_forms.csv" $SCRIPT_DIR"/csv/"$lang"nodups/surface_forms.csv"
    date >> $LOG
    echo "Building training and testing set" >> $LOG
    echo "Cleaning testing directory" >> $LOG
    rm  $SCRIPT_DIR"/csv/"$lang"testing/concat_articles/*" >> $LOG
    echo "Running command "$SCRIPT_DIR/buildTrainingTestingSet.jar $PERCENT_TESTING_ARTICLES $SCRIPT_DIR"/csv/"$lang"nodups" $SCRIPT_DIR"/csv/"$lang"training/" $SCRIPT_DIR"/csv/"$lang"testing/" >> $LOG
    java -Xmx5240m -jar $SCRIPT_DIR/buildTrainingTestingSet.jar $PERCENT_TESTING_ARTICLES $SCRIPT_DIR"/csv/"$lang"nodups" $SCRIPT_DIR"/csv/"$lang"training/" $SCRIPT_DIR"/csv/"$lang"testing/" >> $LOG 2>&1
    echo "Copying remaining files from " $SCRIPT_DIR"/csv/"$lang"nodups/ to "$SCRIPT_DIR"/csv/"$lang"training/" >> $LOG
    cp  $SCRIPT_DIR"/csv/"$lang"nodups/surface_forms.csv" $SCRIPT_DIR"/csv/"$lang"training/surface_forms.csv"
    cp  $SCRIPT_DIR"/csv/"$lang"nodups/entities.csv" $SCRIPT_DIR"/csv/"$lang"training/entities.csv"
    date >> $LOG
    echo "Counting cooccurrences" >> $LOG
    java -Xmx2048m -jar $SCRIPT_DIR/countCoOccurrences.jar $SCRIPT_DIR"/csv/"$lang"nodups" $lang >> $LOG 2>&1
    date >> $LOG
    echo "DONE." >> $LOG
}

if [ $# -lt 1 ]
then
    echo "Usage : $0 language[en|nl|de]"
    exit
fi

runIndexing $1;
```