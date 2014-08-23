# NerService

The project contains the last version of SemiTags REST Service. Main codes are available in rest.js. The REST service 
uses indexes produced by indexing pipline implemented in NerIndexing Support project. 

Apart from that new SemiTags interface is available in index.html file. It connects to SemiTags REST service running at
http://nlp.vse.cz:8081/recognize

## Example of launching the REST service

```bash
nohup node --max-old-space-size=8000 --stack-size=65500 rest.js &
```