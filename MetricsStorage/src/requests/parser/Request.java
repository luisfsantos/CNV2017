package requests.parser;

import properties.PropertiesManager;
import requests.exception.NoModelFileException;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Created by lads on 12/05/2017.
 */
public class Request {
    private static final String ModelLocation = PropertiesManager.getInstance().getString("model.location");
    private static Logger logger = Logger.getLogger(Request.class.getName());
    String requestID;
    int sceneColumns;
    int sceneRows;
    int windowColumns;
    int windowRows;
    int columnOffset;
    int rowOffset;
    String sceneFileName;

    public Request () {

    }

    public Request(String requestID, int sceneColumns, int sceneRows, int windowColumns, int windowRows, int columnOffset, int rowOffset, String file) {
        this.requestID = requestID;
        this.sceneColumns = sceneColumns;
        this.sceneRows = sceneRows;
        this.windowColumns = windowColumns;
        this.windowRows = windowRows;
        this.columnOffset = columnOffset;
        this.rowOffset = rowOffset;
        this.sceneFileName = file;
    }

    public int getSceneColumns() {
        return sceneColumns;
    }

    public int getSceneRows() {
        return sceneRows;
    }

    public int getWindowColumns() {
        return windowColumns;
    }

    public int getWindowRows() {
        return windowRows;
    }

    public int getColumnOffset() {
        return columnOffset;
    }

    public int getRowOffset() {
        return rowOffset;
    }

    public String getSceneFileName() {
        return sceneFileName;
    }

    public String getRequestID() {
        return requestID;
    }

    public void setRequestID(String requestID) {
        this.requestID = requestID;
    }

    public File getSceneFile() throws NoModelFileException {
        File file = new File(ModelLocation + sceneFileName);
        logger.info("Looking for: " + ModelLocation + sceneFileName);
        logger.info("I am at:" + Paths.get(".").toAbsolutePath().normalize().toString());
        if (!file.exists()) {
            logger.warning("File not found!");
            throw new NoModelFileException(sceneFileName);
        }
        logger.info("Found the file!");
        return file;
    }

    public int getImageArea() {
        return windowColumns*windowRows;
    }

    public int getSceneArea() {
        return sceneColumns*sceneRows;
    }

    public String getRequestHash() {
        StringBuilder hash = new StringBuilder();
        hash.append("sc=").append(sceneColumns)
                .append("&sr=").append(sceneRows)
                .append("&wc=").append(windowColumns)
                .append("&wr=").append(windowRows)
                .append("&coff=").append(columnOffset)
                .append("&roff=").append(rowOffset)
                .append("&f=").append(sceneFileName)
                .append("&id=").append(requestID);
        return hash.toString();
    }

    @Override
    public String toString() {
        return String.format("Scene Columns: %d, " +
                "Scene Rows: %d, " +
                "Window Columns: %d, " +
                "Window Rows: %d, " +
                "Column Offset: %d, " +
                "Row Offset: %d, " +
                "Scene: %s", sceneColumns, sceneRows, windowColumns, windowRows, columnOffset, rowOffset, sceneFileName);
    }
}
