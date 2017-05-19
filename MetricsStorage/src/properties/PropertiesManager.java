package properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by lads on 19/05/2017.
 */
public class PropertiesManager {
    String filename = "config.properties";
    Properties prop = new Properties();
    InputStream input = null;
    private static PropertiesManager instance;

    private PropertiesManager(){
        try {
            input = PropertiesManager.class.getClassLoader().getResourceAsStream(filename);
            if(input==null){
                throw new RuntimeException("No config file!");
            }
            prop.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            if(input!=null){
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static synchronized PropertiesManager getInstance(){
        if(instance == null){
            instance = new PropertiesManager();
        }
        return instance;
    }

    public String getString(String name) {
        if (input == null) {
            return null;
        } else {
            return prop.getProperty(name);
        }
    }

    public int getInteger(String name) {
        if (input == null) {
            return 0;
        } else {
            return Integer.parseInt(prop.getProperty(name));
        }
    }

}
