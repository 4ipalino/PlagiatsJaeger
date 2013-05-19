package info.plagiatsjaeger.onlinesearch;

import info.plagiatsjaeger.SourceLoader;
import info.plagiatsjaeger.WordProcessing;
import info.plagiatsjaeger.interfaces.IOnlineSearch;
import info.plagiatsjaeger.interfaces.OnLinkFoundListener;
import info.plagiatsjaeger.types.Settings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;


/**
 * Diese Abstrakte Klasse stellt den Klassen fuer die einzelnen Suchmaschinen
 * entsprechende Methoden zur Verfuegung
 * 
 * @author FischerC
 */
public abstract class OnlineSearch implements IOnlineSearch
{

	private ArrayList<String>		_allSearchResults	= new ArrayList<String>();
	protected static int			SEARCH_SENTENCELENGTH;
	private static int				SEARCH_JUMPLENGTH;
	private OnLinkFoundListener		_onLinkFoundListener;
	private static int				MAX_URLS			= 5;
	protected static final String	CHARSET				= "UTF-8";

	private static final Logger		_logger				= Logger.getLogger(BlekkoSearch.class.getName());

	protected OnlineSearch()
	{
		Settings settings = Settings.getInstance();
		SEARCH_JUMPLENGTH = settings.getSearchJumpLength();
		SEARCH_SENTENCELENGTH = settings.getCompareSentenceLength();
	}

	public ArrayList<String> search(String searchString, URL _URL)
	{
		ArrayList<String> result = null;
		try
		{
			InputStreamReader reader = new InputStreamReader(_URL.openStream(), CHARSET);

			BufferedReader bufferedReader = new BufferedReader(reader);

			StringBuilder stringBuilder = new StringBuilder();
			String line = bufferedReader.readLine();
			while (line != null)
			{
				stringBuilder.append(line);
				line = bufferedReader.readLine();
			}
			result = getUrlsFromJson(stringBuilder.toString());
		}

		catch (IOException e)
		{
			_logger.fatal(e.getMessage(), e);
		}

		return result;
	}

	@Override
	public void searchAsync(final String completeString)
	{
		WordProcessing wordProcessing = new WordProcessing();
		String[] words = wordProcessing.splitToWords(completeString);

		// i wird immer um die satzlaenge + sprungweie erhoeht
		for (int i = 0; i < words.length - SEARCH_SENTENCELENGTH; i += SEARCH_SENTENCELENGTH + SEARCH_JUMPLENGTH)
		{
			Thread thread = new Thread(new Runnable()
			{

				@Override
				public void run()
				{
					// Sicherstellen, dass nur 1 mal pro Sekunde eine
					// Suchabfrage gestartet wird.
					try
					{
						Thread.sleep(1000);
					}
					catch (InterruptedException e)
					{
						_logger.fatal(e.getMessage(), e);
					}
				}
			});
			thread.start();
			String searchString = "";
			for (int j = 0; j < SEARCH_SENTENCELENGTH; j++)
			{
				if (searchString.length() > 0)
				{
					searchString += " ";
				}
				searchString += words[i + j];
			}
			_logger.info(i + "/" + (words.length - SEARCH_SENTENCELENGTH) + " Suche nach: " + searchString);
			try
			{
				// Auf beendigung des Threads mit 1s warten
				thread.join();
			}
			catch (InterruptedException e)
			{
				_logger.fatal(e.getMessage(), e);
			}
			search(searchString, buildSearchString(searchString));
		}
	}

	protected abstract URL buildSearchString(String searchString);

	/**
	 * Extrahiert die Links aus dem eingegebenen String. Wenn ein
	 * {@link OnLinkFoundListener} registriert ist werden diesem die Links
	 * Uebermittelt.
	 * 
	 * @param searchResult
	 * @return result
	 */
	protected ArrayList<String> getUrlsFromJson(String searchResult)
	{
		ArrayList<String> alUrlList = new ArrayList<String>();
		// Matchpattern
		// Altes JSON
		Pattern patPattern = Pattern.compile("\"url\"\\s*?:\\s*?\"([^\"]+?)\"");
		// Neues JSON
		Pattern patPatternNew = Pattern.compile("\"displayUrl\"\\s*?:\\s*?\"([^\"]+?)\"");

		Matcher matMatcher;

		// Und schliesslich in der for schleife//
		matMatcher = patPattern.matcher(searchResult);

		if (matMatcher.find())
		{
			// Falls matcher nicht leer ist
			matMatcher.reset();

			int numURL = 0;
			while (numURL < MAX_URLS && matMatcher.find())
			{
				numURL++;
				String strLink = SourceLoader.cleanUrl(Jsoup.parse(matMatcher.group(1)).text());
				// Falls Link bereits in _serchResults vorhanden nicht nochmal
				// schicken
				if (!_allSearchResults.contains(strLink))
				{
					alUrlList.add(strLink);
					// System.out.println(strLink);
					if (_onLinkFoundListener != null) _onLinkFoundListener.onLinkFound(strLink);
				}
			}
		}
		else
		{
			matMatcher = patPatternNew.matcher(searchResult);
			matMatcher.reset();
			int numURL = 0;
			while (numURL < MAX_URLS && matMatcher.find())
			{
				numURL++;
				String strLink = SourceLoader.cleanUrl(Jsoup.parse(matMatcher.group(1)).text());
				// Falls Link bereits in _serchResults vorhanden nicht nochmal
				// schicken
				if (!_allSearchResults.contains(strLink))
				{
					alUrlList.add(strLink);
					// System.out.println(strLink);
				}
			}
		}
		_allSearchResults.addAll(alUrlList);
		return alUrlList;
	}

	@Override
	public ArrayList<String> search(String searchString)
	{
		return search(searchString, buildSearchString(searchString));
	}

	@Override
	public void setOnLinkFoundListener(OnLinkFoundListener listener)
	{
		_onLinkFoundListener = listener;
	}

}
