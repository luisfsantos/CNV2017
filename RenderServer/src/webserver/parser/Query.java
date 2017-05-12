package webserver.parser;

import webserver.exception.NoModelFileException;
import webserver.handlers.RenderRequestHandler;

import java.io.File;
import java.nio.file.Paths;
import java.util.logging.Logger;

/**
 * Created by lads on 12/05/2017.
 */
public class Query {
    private static final String ModelLocation = "lib/RenderModels/";
    private static Logger logger = Logger.getLogger(Query.class.getName());

    final int sceneColumns;
    final int sceneRows;
    final int windowColumns;
    final int windowRows;
    final int columnOffset;
    final int rowOffset;
    final String sceneFileName;


    public Query(int sceneColumns, int sceneRows, int windowColumns, int windowRows, int columnOffset, int rowOffset, String file) {
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
        hash.append("sc").append(sceneColumns)
                .append("sr").append(sceneRows)
                .append("wc").append(windowColumns)
                .append("wr").append(windowRows)
                .append("co").append(columnOffset)
                .append("ro").append(rowOffset)
                .append("f").append(sceneFileName);
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
