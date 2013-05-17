package info.plagiatsjaeger;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;


/**
 * Klasse zum Laden von Daten.
 * 
 * @author Andreas
 */
public class SourceLoader
{
	public static final Logger				_logger				= Logger.getLogger(SourceLoader.class.getName());
	
	/**
	 * Laed den Text einer Webseite.
	 * 
	 * @param strUrl
	 * @return
	 */
	public static String loadURL(String strUrl)
	{
		StringBuilder result = new StringBuilder();

		try
		{
			URL url = new URL(strUrl);
			InputStreamReader reader = new InputStreamReader(url.openStream());

			BufferedReader bufferedReader = new BufferedReader(reader);
			String line = "";
			while ((line = bufferedReader.readLine()) != null)
			{
				result.append(line).append("\n");
			}
		}
		catch (MalformedURLException e)
		{
			_logger.fatal(e.getMessage());
			e.printStackTrace();
			return "FAIL MalformedURLException";
		}
		catch (UnsupportedEncodingException e)
		{
			_logger.fatal(e.getMessage());
			e.printStackTrace();
			return "FAIL UnsupportedEncodingException";
		}
		catch (IOException e)
		{
			_logger.fatal(e.getMessage());
			e.printStackTrace();
			return "FAIL IOException";
		}

		return Jsoup.parse(result.toString()).text();
	}

	/**
	 * Laed eine Datei.
	 * 
	 * @param filePath
	 * @return
	 */
	public static String loadFile(String filePath)
	{
		String result = "";
		FileInputStream fileInputstream = null;
		DataInputStream dataInputStream = null;

		StringBuilder stringBuilder = new StringBuilder();

		try
		{
			String line = "";
			fileInputstream = new FileInputStream(filePath);
			dataInputStream = new DataInputStream(fileInputstream);
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(dataInputStream));

			while ((line = bufferedReader.readLine()) != null)
			{
				stringBuilder.append(line).append("\n");
			}
			// Close the input stream
			dataInputStream.close();
		}
		catch (FileNotFoundException e)
		{
			_logger.fatal(e.getMessage());
			e.printStackTrace();
			return "FAIL FileNotFoundException";
		}
		catch (IOException e)
		{
			_logger.fatal(e.getMessage());
			e.printStackTrace();
			return "FAIL IOException";
		}
		finally
		{
			result = stringBuilder.toString();
		}
		return result;
	}
}