# Wikipedia benchmarks

Appart from source codes of scripts for creation of the benchmark, we provide here two types of Wikipedia benchmarks:

## soft_benchmark

Is composed of paragraphs that contain surface forms that have at least two candidates and both candidates are very close to each other in terms of popularity (how often the surface form is mentioned in the meaning of one of them).


## 3d_candidate benchmark

Is built only with paragraphs that contain surface forms used in the meaning of the third most popular candidate. Therefore this benchmark is very difficult for services that rely heavily on the overall entity popularity and tests the ability to work with the context of a disambiguated surface form.

## soft_res and 3d_candidate_res

Folders soft_res and 3d_candidate_res contain tab delimited golden standard records. The file golden_standard.tab contains correct links for all paragraphs in the benchmark, golden_spotlight.tab is limited only to the entities that were recognized by DBpedia Spotlight entity linking tool - Thus contains more cleaned results of real named entities that should be easier to recognize.
