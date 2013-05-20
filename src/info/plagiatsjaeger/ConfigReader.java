package info.plagiatsjaeger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;


public class ConfigReader
{
	private static final Logger	_logger		= Logger.getLogger(Control.class.getName());
	private static final String	PROP_FILE	= "config.properties";

	/**
	 * Schreibt einen Parameter in das ConfigFile.
	 * 
	 * @param name
	 * @param value
	 */
	public void setProperty(String name, String value)
	{
		File f = new File("dummy");
		_logger.info(f.getAbsolutePath());
		Properties prop = new Properties();

		try
		{
			prop.load(new FileInputStream(PROP_FILE));
			// set the properties value
			prop.setProperty(name, value);
			// save properties to project root folder
			prop.store(new FileOutputStream(PROP_FILE), null);
		}
		catch (IOException e)
		{
			_logger.fatal(e.getMessage(), e);
		}
	}

	/**
	 * Liest einen Parameter aus dem ConfigFile aus.
	 * 
	 * @param name
	 * @return Wert als String
	 */
	public static String getProperty(String name)
	{
		Properties prop = new Properties();

		try
		{
			// load a properties file
			prop.load(new FileInputStream(PROP_FILE));

			// get the property value and print it out
			return prop.getProperty(name);

		}
		catch (IOException e)
		{
			_logger.fatal(e.getMessage(), e);
		}
		return "";
	}
}
