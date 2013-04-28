package info.plagiatsjaeger;

import info.plagiatsjaeger.interfaces.IComparer;
import info.plagiatsjaeger.interfaces.IOnlineSearch;
import info.plagiatsjaeger.interfaces.OnCompareFinishedListener;
import info.plagiatsjaeger.interfaces.OnLinkFoundListener;
import info.plagiatsjaeger.onlinesearch.BlekkoSearch;

import java.util.ArrayList;


/**
 * Steuerung f�r den gesammten Ablauf.
 * 
 * @author Andreas
 */
public class Control
{
	private Settings			_settings;

	/**
	 * Dateipfad f�r die Dateien auf dem Server.
	 */
	private static final String	ROOT_FILES	= "/srv/www/uploads/";

	/**
	 * <b>Noch nicht implementiert!</b></br> Konvertiert eine Datei in das
	 * normalisierte txt-Format.
	 * 
	 * @param documentHash
	 */
	public void startParsing(int documentHash)
	{
	}

	/**
	 * Startet eine Plagiatssuche zu dem �bergebenen Report.
	 * 
	 * @param rId
	 */
	public void startPlagiatsSearch(int rId)
	{
		MySqlDatabaseHelper mySqlDatabaseHelper = new MySqlDatabaseHelper();
		int intDocumentId = mySqlDatabaseHelper.getDocumentID(rId);
		_settings = mySqlDatabaseHelper.getSettings(rId);
		startPlagiatsSearch(ROOT_FILES + intDocumentId + ".txt", rId);
	}

	/**
	 * F�hrt eine Plagiatssuche zu dem �bergebenen Dokument durch.
	 * 
	 * @param filePath
	 * @param rId
	 */
	public void startPlagiatsSearch(String filePath, final int rId)
	{
		final String strSourceText = SourceLoader.loadFile(filePath);
		if (_settings.getCheckWWW())
		{
			IOnlineSearch iOnlineSearch = new BlekkoSearch();
			iOnlineSearch.setOnLinkFoundListener(new OnLinkFoundListener()
			{
				@Override
				public void onLinkFound(final String link)
				{
					new Thread(new Runnable()
					{
						@Override
						public void run()
						{
							compare(rId, strSourceText, link, 0);
						}
					}).start();
				}
			});
			iOnlineSearch.searchAsync(strSourceText, 8);
		}
		for (int i : _settings.getLocalFolders())
		{
			// TODO: Compare local Files
		}
	}

	/**
	 * Vergleicht den sourceText entweder mit einer Internetseite oder einem
	 * lokalen Dokument.
	 * 
	 * @param rId
	 * @param checkText
	 * @param link
	 * @param docId
	 */
	private void compare(int rId, String checkText, String link, int docId)
	{
		IComparer comparer = new MyComparer(rId);
		comparer.setOnCompareFinishedListener(new OnCompareFinishedListener()
		{
			@Override
			public void onLinkFound(ArrayList<CompareResult> compareResult, int docId)
			{
				MySqlDatabaseHelper mySqlDatabaseHelper = new MySqlDatabaseHelper();
				mySqlDatabaseHelper.insertCompareResults(compareResult, docId);
			}

			@Override
			public void onLinkFound(ArrayList<CompareResult> compareResult, String link)
			{
				MySqlDatabaseHelper mySqlDatabaseHelper = new MySqlDatabaseHelper();
				mySqlDatabaseHelper.insertCompareResults(compareResult, link);
			}
		});
		if (docId <= 0)
		{
			comparer.compareText(checkText, SourceLoader.loadFile(ROOT_FILES + docId + ".txt"), docId);
		}
		else
		{
			comparer.compareText(checkText, SourceLoader.loadURL(link), link);
		}
	}
}