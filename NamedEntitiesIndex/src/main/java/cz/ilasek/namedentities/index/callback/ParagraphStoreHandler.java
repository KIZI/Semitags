package cz.ilasek.namedentities.index.callback;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowCallbackHandler;

import eu.linkedtv.utils.string.StringUtils;

public class ParagraphStoreHandler implements RowCallbackHandler {
    
    private final FileOutputStream os;
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 
     * @param entityId Entity id that we concatenate paragraphs for.
     * @param targetDir Target directory, where concatenated paragraphs should 
     *      be stored (without the slash / at the end!).
     * @throws IOException By problems with storing data in target directory.
     */
    public ParagraphStoreHandler(Long entityId, String targetDir) throws IOException {
        if (!(new File(targetDir)).exists())
            throw new FileNotFoundException("Target directory " + targetDir + " not found");
        
        String directoryName = StringUtils.reverseSubstring(entityId.toString(), 3);
        File directory = new File(targetDir + "/" + directoryName);
        if (!directory.exists()) {
            if (!directory.mkdir())
                throw new IOException("Directory " + directory.getAbsolutePath() + 
                        " does not exists and can not be created.");
        }
        
        File targetFile = new File(directory.getAbsolutePath() + "/" + entityId + ".txt");
        os = new FileOutputStream(targetFile);
    }
    
    
    @Override
    public void processRow(ResultSet rs) throws SQLException {
        String paragraph = rs.getString("paragraph_text");
        try {
            os.write((paragraph + "\n\n").getBytes());
        } catch (IOException e) {
            logger.error("Problems writing paragraph into file", e);
        }
    }

    /**
     * It is necessary to call this method after the finish of row processing
     * in order to clean resources and close the output file.
     * 
     * @throws IOException 
     */
    public void closeOutput() throws IOException {
        if (os != null)
            os.close();
    }
    
}
