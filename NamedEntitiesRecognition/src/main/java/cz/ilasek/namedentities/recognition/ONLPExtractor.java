package cz.ilasek.namedentities.recognition;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import cz.ilasek.namedentities.models.Chunk;
import cz.ilasek.namedentities.models.SVOTriple;

public class ONLPExtractor {
    public static void main(String[] args) throws IOException {
        ONLPExtractor onlp = new ONLPExtractor();
        
        String[] sentences = onlp.detectSentences("Businesses and consumers are adapting to life under the sanctions. Some traders buy banned goods from neighboring Lebanon or other Arab countries. Others are looking for new markets for their products, especially in friendly nations. Iraq and Iran are buying more Syrian exports, Sukkar said. More than 300 Syrian companies participated in a trade fair in Iran last month after the two countries signed a free trade agreement, the official Syrian Arab News Agency reported.");
        for (String sentence : sentences) {
            List<Chunk> chunks = onlp.getChunks(sentence);
            List<SVOTriple> triples = onlp.getSVOTriples(chunks);
            for (SVOTriple triple : triples)
                System.out.println(triple);
        }

    }

    public String[] detectSentences(String text) throws IOException {
        InputStream modelIn = getClass().getResourceAsStream(
                "/opennlp/model/en-sent.bin");

        try {
            SentenceModel model = new SentenceModel(modelIn);
            SentenceDetectorME sentenceDetector = new SentenceDetectorME(model);

            return sentenceDetector.sentDetect(text);
        } finally {
            if (modelIn != null) {
                try {
                    modelIn.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public String[] tokenize(String sentence) throws IOException {
        InputStream modelIn = getClass().getResourceAsStream(
                "/opennlp/model/en-token.bin");

        try {
            TokenizerModel model = new TokenizerModel(modelIn);
            Tokenizer tokenizer = new TokenizerME(model);

            return tokenizer.tokenize(sentence);
        } finally {
            if (modelIn != null) {
                try {
                    modelIn.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public String[] pos(String[] tokens) throws IOException {
        InputStream modelIn = null;

        try {
            modelIn = getClass().getResourceAsStream(
                    "/opennlp/model/en-pos-maxent.bin");
            POSModel model = new POSModel(modelIn);
            POSTaggerME tagger = new POSTaggerME(model);

            return tagger.tag(tokens);
        } finally {
            if (modelIn != null) {
                try {
                    modelIn.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public String[] chunk(String[] tokenSentence, String[] pos) throws IOException {
        InputStream modelIn = null;
        ChunkerModel model = null;

        try {
            modelIn = ONLPExtractor.class.getClass().getResourceAsStream(
                    "/opennlp/model/en-chunker.bin");
            model = new ChunkerModel(modelIn);
            ChunkerME chunker = new ChunkerME(model);

            return chunker.chunk(tokenSentence, pos);
        } finally {
            if (modelIn != null) {
                try {
                    modelIn.close();
                } catch (IOException e) {
                }
            }
        }
    }
    
    public List<Chunk> getChunks(String sentence) throws IOException {
        List<Chunk> chunks = new LinkedList<Chunk>();
        
        
        String[] tokens = tokenize(sentence);
        String[] pos = pos(tokens);
        
        String[] chunkArray = chunk(tokens, pos);
        Chunk currentChunk = null;
        int i = 0;
        for (String chunk : chunkArray) {
            if (!chunk.substring(0, 1).equals("O")) {
                if (chunk.substring(0, 1).equals("B")) {
                    if (currentChunk != null)
                        chunks.add(currentChunk);
                    
                    currentChunk = new Chunk();
                    currentChunk.setChunk(tokens[i]);
                    currentChunk.setType(chunk.substring(2));
                }
                else {
                    if (currentChunk != null)
                        currentChunk.addChunkPart(tokens[i]);
                }
            }
            else {
                if (currentChunk != null)
                    chunks.add(currentChunk);
                
                currentChunk = new Chunk();
                currentChunk.setChunk(tokens[i]);
                currentChunk.setType("O");
            }
            i++;
        }
        
        chunks.add(currentChunk);
        
        return chunks;
    }
    
    public List<SVOTriple> getSVOTriples(List<Chunk> chunks) {
        List<SVOTriple> triples = new LinkedList<SVOTriple>();
        
        SVOTriple triple = new SVOTriple();
        Chunk lastChunk = null;
        for (Chunk chunk : chunks) {
            if (chunk.getType().equals("NP")) {
                if (triple.getSubject() == null)
                    triple.setSubject(chunk);
                else if ((triple.getObject() == null) && (triple.getVerb() != null)) {
                    triple.setObject(chunk);
                 }
            }
            if (chunk.getType().equals("VP")) {
                if (triple.getVerb() == null)
                    triple.setVerb(chunk);
            }
            if (chunk.getType().equals("O")) {
                triples.add(triple);
                triple = new SVOTriple();
            }
            
            lastChunk = chunk;
        }
        
        if ((lastChunk != null) && !lastChunk.getType().equals("O"))
            triples.add(triple);
        
        return triples;
    }

}
