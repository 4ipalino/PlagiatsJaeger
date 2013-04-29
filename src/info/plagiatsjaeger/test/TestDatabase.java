package info.plagiatsjaeger.test;


import info.plagiatsjaeger.MyComparer;
import info.plagiatsjaeger.MySqlDatabaseHelper;
import info.plagiatsjaeger.Settings;
import info.plagiatsjaeger.SourceLoader;
import info.plagiatsjaeger.interfaces.IComparer;
import info.plagiatsjaeger.interfaces.OnCompareFinishedListener;
import info.plagiatsjaeger.types.CompareResult;

import java.util.ArrayList;

public class TestDatabase
{
	
	public static void main(String[] args)
	{
		
		IComparer comparer = new MyComparer(1);
		final MySqlDatabaseHelper dbhelper = new MySqlDatabaseHelper();
		comparer.setOnCompareFinishedListener(new OnCompareFinishedListener()
		{
			@Override
			public void onLinkFound(ArrayList<CompareResult> compareResult, int docId)
			{
				dbhelper.insertCompareResults(compareResult, docId);
			}

			@Override
			public void onLinkFound(ArrayList<CompareResult> compareResult, String link)
			{
				dbhelper.insertCompareResults(compareResult, link);
			}
		});
		String sourceText ="Bei der Pekingente wird besonderer Wert auf die Haut gelegt. Deshalb werden die Tiere nach der Schlachtung einer besonderen Prozedur unterzogen, die sich mit handels�blichen Enten nicht nachvollziehen l�sst.Die Ente wird gerupft, aber nicht ausgenommen, Kopf und F��e werden zun�chst nicht entfernt. Durch einen kleinen Schnitt am Hals wird nun die Haut aufgeblasen wie ein Luftballon, damit sie sich vollst�ndig vom Fleisch trennt. Durch einen m�glichst kleinen Schnitt unterhalb des Fl�gels werden anschlie�end die Innereien entfernt. Die F��e werden abgeschnitten. Nun wird die Ente am Hals aufgeh�ngt, mit kochendem Wasser �berbr�ht, gew�rzt und rundherum mit in hei�em Wasser aufgel�stem Honig oder Malzzucker eingestrichen, um dann an einem gut bel�fteten Ort f�r einige Stunden zu trocknen.";		
		String link = "http://de.wikipedia.org/wiki/Pekingente_(Gericht)";
		comparer.compareText(sourceText, SourceLoader.loadURL(link), link);
		
		Settings setting = dbhelper.getSettings(1);
		
		

	}
	
	
	
}
