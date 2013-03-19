package tests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.TimeUnit;


public class RabinKarpComparer
{
	/* Konfiguration */
	/*
	 * Basis-Wert: 10 f�r Zahlensuche verwenden (Anzahl Buchstaben des
	 * Alphabets)
	 */
	int					base				= 257;
	/* initialer Modulo-Wert f�r die Hash-Funktion */
	int					q					= 1024;
	/*
	 * ab wievielen false matches soll q neu gew�hlt werden? 0 = Zufallsmodus
	 * ausschalten
	 */
	int					fmThreshold			= 1000;
	/*
	 * Unter- und Obergrenze von q festlegen, z. b. 2^10 - 2^31 2^31 ist bereits
	 * das Maximum f�r einen int
	 */
	int					minQ				= 10;
	int					maxQ				= 31;

	/* Anzahl der Suchdurchl�ufe */
	int					runs				= 10;
	/* Modus bestimmen; true f�r Textsuche, false f�r Zahlensuche */
	boolean				textSearch			= true;
	/* Konfiguration-Ende */

	// systemeigenes Zeilenumbruchszeichen ermitteln
	String				sep					= System.getProperty("line.separator");
	boolean				locked				= false;
	String				loadPath			= "";
	StringBuilder		completeString		= new StringBuilder(0);
	String				searchString		= "";
	String				searchedString		= "";
	boolean				fileSelected		= false;
	int					shiftFactor			= 0;
	ArrayList<Integer>	searchPosition		= new ArrayList<Integer>();
	String				timeString			= "";
	/* fm = false matches, Kollisionen */
	int					fm					= 0;
	long				completeTimeDiff	= 0;
	int					minQResult			= 0;
	int					qDiff				= 0;
	/* generische Liste mit allen Zeiten f�r jeden einzelnen Durchlauf */
	LinkedList<Long>	times				= new LinkedList<Long>();
	/*
	 * wenn bitweise Modulo gerechnet werden soll muss q-1 nicht jedes Mal neu
	 * berechnet werden
	 */
	int					reducedQ			= q - 1;
	int					qChanges			= 0;

	public RabinKarpComparer()
	{
		// TODO Auto-generated constructor stub
	}

	/*-----------------------------------------------------------------------------
	 *  initiale Hash-Funktion
	 *-----------------------------------------------------------------------------*/

	int hashFirst(String searchText, int patternLength)
	{
		int result = 0;
		if (textSearch)
		{
			int reducedBase = 1;
			for (int i = (patternLength - 1); i >= 0; i--)
			{
				if (i != (patternLength - 1)) reducedBase = bitModulo(reducedBase * base);

				result += bitModulo(reducedBase * (int) searchText.charAt(i));
				result = bitModulo(result);
			}
			shiftFactor = reducedBase;
		}
		else
		{
			/* f�r den Zahlensuchmodus wird nat�rlich eine Basis von 10 ben�tigt */

			/* Verschiebe-Faktor berechnen */
			shiftFactor = 1;
			for (int i = 1; i < patternLength; i++)
				shiftFactor = bitModulo(shiftFactor * base);

			/* Zahl ausschneiden */
			result = Integer.parseInt(searchText.substring(0, patternLength));
		}
		return bitModulo(result);
	}

	/*-----------------------------------------------------------------------------
	 *  Diese Modulo-Variante arbeitet bitweise mit dem &-Operator und ben�tigt daher
	 *  als q eine Zweierpotenz
	 *-----------------------------------------------------------------------------*/
	int bitModulo(int x)
	{
		return (x & reducedQ);
	}

	/*-----------------------------------------------------------------------------
	 *  rollende Hash-Funktion
	 *-----------------------------------------------------------------------------*/
	int hash(int oldHashValue, int startPos, int patternLength)
	{
		/*
		 * wenn die gesamte Stringl�nge kleiner als die des Musters ist, kann
		 * das Muster nicht vorkommen
		 */
		if (completeString.length() < patternLength) return 0;

		int result = 0;

		int intValue;
		int intValue2;
		if (textSearch)
		{
			/* das erste Zeichen von links bestimmen, das wegf�llt */
			intValue = (int) completeString.charAt(startPos - 1);
			/* das hinzukommende Zeichen von rechts bestimmen */
			intValue2 = (int) completeString.charAt(startPos + patternLength - 1);
		}
		else
		{
			/* das erste Zeichen von links bestimmen, das wegf�llt */
			intValue = Integer.parseInt(completeString.substring(startPos - 1, startPos));
			/* das hinzukommende Zeichen von rechts bestimmen */
			intValue2 = Integer.parseInt(completeString.substring(startPos + patternLength - 1, startPos + patternLength));
		}

		// System.out.println(oldHashValue + "-" + intValue + "-" + shiftFactor
		// + "-" +
		// base + "-" + intValue2);
		result = ((oldHashValue - (intValue * shiftFactor)) * base) + intValue2;
		result = bitModulo(result);

		return result;
	}

	/*-----------------------------------------------------------------------------
	 *  allgemeine Vorarbeiten
	 *-----------------------------------------------------------------------------*/
	void praeProcessing()
	{
		// if (loadPath == "" || searchString == "") return;

		long completeTimeBefore = 0;
		long completeTimeAfter = 0;

		/* Datei laden */
		completeString.setLength(0);
		// completeString = loadFile(loadPath);

		searchString = "est112";
		for (int i = 0; i < 10000; i++)
		{
			completeString.append("HalloTest" + i);
		}
		/* nimm die Vorher-Zeit f�r Gesamtdurchlauf */
		completeTimeBefore = System.currentTimeMillis();

		/* Algorithmus starten */
		searchPosition = rabinKarp();

		/* nimm die Danach-Zeit f�r Gesamtdurchlauf und bestimme Differenz */
		completeTimeAfter = System.currentTimeMillis();
		completeTimeDiff = completeTimeAfter - completeTimeBefore;

		/* berechne den Median = Durchschnitt der errechneten Durchl�ufe */
		long median = calcMedian();

		/* Ausgabe f�r Gesamtdurchlauf formatieren */
		timeString = String.format("Gesamtzeit: %02d min, %02d sec, %03d milliseconds, Median �ber %d-Durchl�ufe: %02d min, %02d sec, %03d milliseconds \n", TimeUnit.MILLISECONDS.toMinutes(completeTimeDiff), TimeUnit.MILLISECONDS.toSeconds(completeTimeDiff) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(completeTimeDiff)), completeTimeDiff % 1000, runs, TimeUnit.MILLISECONDS.toMinutes(median), TimeUnit.MILLISECONDS.toSeconds(median) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(median)), median % 1000);

		/* letztes Suchfenster f�r Anzeige festlegen */
		for (int i : searchPosition)
		{
			// int i = searchPosition;
			boolean cutted = false;
			if (i < 0)
			{
				/* nicht gefunden */
				if (completeString.length() > 100)
				{
					searchedString = completeString.substring(0, 100);
					cutted = true;
				}
				else
				{
					searchedString = completeString.substring(0);
				}
			}
			else if ((completeString.length() - i + 1) > 100)
			{
				/* ab der gefundenen Position folgen noch mehr als 100 Zeichen */
				searchedString = completeString.substring(i, i + 100);
				cutted = true;
			}
			else
			{
				/*
				 * ab der gefundenen Position folgen weniger oder genau 100
				 * Zeichen
				 */
				searchedString = completeString.substring(i);
			}
			/* Zeilenumbr�che entfernen */
			searchedString = searchedString.replace("\r\n", " ");
			searchedString = searchedString.replace("\n", " ");
			if (cutted) searchedString = searchedString + " [..]";
			
			System.out.println(searchedString);
		}
		/* Konsolenausgaben */
		/*
		 * die sind deswegen hier drin weil sie in draw() immer wieder
		 * aufgerufen werden w�rden
		 */
		System.out.println("q-Wechsel: " + qChanges);
		System.out.println(fm + " false matches nach letztem q-Wechsel\n");
		System.out.println("\nAlle Zeiten:\n");
		for (long timeX : times)
		{
			System.out.println(String.format("%02d min, %02d sec, %03d milliseconds", TimeUnit.MILLISECONDS.toMinutes(timeX), TimeUnit.MILLISECONDS.toSeconds(timeX) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeX)), timeX % 1000));
		}

		/* bestimme maximale Zeitspanne zwischen Minimum und Maximum */
		/*
		 * mu� nach Median-Funktion aufgerufen werden, da sortierte Collection
		 * erforderlich
		 */
		long maxTimeDiff = (times.get(times.size() - 1) - times.get(0));
		System.out.println(String.format("Maximale Differenz: %02d min, %02d sec, %03d milliseconds", TimeUnit.MILLISECONDS.toMinutes(maxTimeDiff), TimeUnit.MILLISECONDS.toSeconds(maxTimeDiff) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(maxTimeDiff)), maxTimeDiff % 1000));

		/* aktuelles q ausgeben */
		System.out.println("aktuelles q: " + q);
	}

	/*-----------------------------------------------------------------------------
	 *  eigentlicher Suchalgorithmus
	 *  - zur�ckgegeben wird die Suchposition
	 *    oder -1 wenn nicht gefunden
	 *-----------------------------------------------------------------------------*/
	ArrayList<Integer> rabinKarp()
	{
		ArrayList<Integer> result = new ArrayList<Integer>();
		long timeBefore = 0;
		long timeAfter = 0;
		long timeDiff = 0;
		int randomNumber = 0;
		int hs = 0;
		int hsub = 0;
		int n = 0;
		int m = 0;
		int n_m = 0;
		qChanges = 0;
		// boolean found = false;
		times.clear();

		/* Algorithmus gem�� der Anzahl in der Variablen �runs� durchlaufen */
		for (int counter = 1; counter <= runs; counter++)
		{
			/* n=L�nge des gesamten Textes, m=L�nge des Musters */
			n = completeString.length();
			m = searchString.length();
			n_m = n - m;

			/* nimm Zeit vorher */
			timeBefore = System.currentTimeMillis();

			/* hs=hash-Wert der ersten Zeichen des gesamten Textes */
			hs = hashFirst(completeString.substring(0, m), m);
			/* hsub=hash-Wert des Musters */
			hsub = hashFirst(searchString, m);

			/*
			 * da die Zufallszahlenerzeugung f�r die rand. RK-Algorithmus
			 * essentiell ist, messen wir die Instanziierung des Random-Objekts
			 * nat�rlich jeweils mit
			 */
			Random randomNumbers = new Random();
			/* Variablen f�r erneute Durchl�ufe zur�cksetzen */
			// result = -1;
			result.clear();

			/*
			 * in fm werden die Anzahl "False Matches" gespeichert, also die
			 * Kollisionen
			 */
			fm = 0;
			// found = false;
			int i = 0;

			/* solange Text noch nicht komplett durchlaufen... */
			for (i = 0; i <= n_m; i++)
			{
				/*
				 * wenn Hashwert des Musters mit dem Hashwert des
				 * Textausschnittes �bereinstimmt...
				 */
				if (hs == hsub)
				{
					/* ... und die Strings an der Stelle auch �bereinstimmen ... */
					if (completeString.substring(i, i + m).equals(searchString))
					{
						/* �bereinstimmung gefunden */
						result.add(i);
						// found = true;
						break;
					}
					else
					{
						fm += 1;
						if (fmThreshold != 0)
						{
							if (fm == fmThreshold)
							{
								/*
								 * w�hle q neu - eine Zweierpotenz zwischen
								 * 2^minQ bis 2^maxQ
								 */
								randomNumber = randomNumbers.nextInt(qDiff) + minQ;
								/* Schiebeoperatoren sind schneller */
								q = minQResult << (randomNumber - minQ);
								reducedQ = q - 1;
								/* false matches zur�cksetzen */
								fm = 0;
								qChanges++;

								/*
								 * mit neuem q muss Hash f�r Muster und
								 * Gesamtstring auch neu berechnet werden
								 */
								hsub = hashFirst(searchString, m);
								hs = hashFirst(completeString.substring(i, i + m), m);
							}
						}
					}
				}

				/* Bereichs�berlaufsfehler abfangen */
				if ((i + m + 1) > n) break;
				/* n�chsten Hashwert bestimmen */
				hs = hash(hs, i + 1, m);
			}

			// if (found) result = i;

			/* nimm Zeit danach und bestimme Differenz */
			timeAfter = System.currentTimeMillis();
			timeDiff = timeAfter - timeBefore;

			/* Zeiten der Gesamttabelle hinzuf�gen */
			times.add(timeDiff);
		}

		return result;
	}

	/*-----------------------------------------------------------------------------
	 *  Berechnung des Medians, Erkl�rung siehe hier:
	 *  http://www.mathe-lexikon.at/statistik/lagemasse/median.html 
	 *-----------------------------------------------------------------------------*/
	long calcMedian()
	{
		long result = 0;

		/* um den Median zu berechnen mu� die generische Liste sortiert sein */
		Collections.sort(times);

		/* testen, ob Anzahl ungerade... */
		if (times.size() % 2 != 0)
		{
			/* (da /2 und nicht /2.0 ist es eine Integer-Division) */
			result = times.get((times.size() / 2));
		}
		else
		{
			/*
			 * f�r den Feld-Index wird nat�rlich eine gerade Zahl ben�tigt. Der
			 * errechnete Wert soll nat�rlich nicht abgeschnitten, sondern
			 * aufgerundet werden
			 */
			result = Math.round((times.get((times.size() / 2) - 1) + times.get(times.size() / 2)) / 2.0);
		}

		return result;
	}
}
