<div class="report">
	{foreach from=$messages item=message}
	<div class="{$message.type}">
		{$message.text}
	</div>
	{/foreach}
	<ul>
		<li>
			<a href="report.php?rID={$smarty.get.rID}&amp;type=short">Schnellbericht</a>
		</li>
		<li>
			<a href="report.php?rID={$smarty.get.rID}&amp;type=grafic">Grafikbericht</a>
		</li>
		<li>
			<a href="report.php?rID={$smarty.get.rID}&amp;type=all">Resultatbericht</a>
		</li>
	</ul>

	<h1>{$title}</h1>
	<table>
		<tr>
			<th>Dokumentname:</th>
			<td>
				{$report.dOriginalName}
			</td>
			<th>Author:</td>
			<td>
				{$report.dAuthor}
			</th>

		</tr>
		<tr>
			<th>Datum:</th>
			<td>
				{$report.rDatetime|date_format:'%d.%m.%Y %H:%M'}
				<br />
				{$report.rEndTime|date_format:'%d.%m.%Y %H:%M'}
			</td>
			<th>Plagiatsverdacht zu:</th>
			<td>
				{$report.rSimilarity} %
			</td>
		</tr>
		<tr>
			<th>Schwellenwert:</th>
			<td>
				{$report.rThreshold} %
			</td>
			<th>Detailgrad:</th>
			<td>
				{$report.slTitle}
			</td>
		</tr>
		<tr>
			<th>Internetquellen prüfen?</th>
			<td>
				{if $report.rCheckWWW == '1'}ja{else}nein{/if}
			</td>
			<th>Status der Prüfung:</th>
			<td>
				{$report.eName} ({$report.eDescription})
			</td>
		</tr>

	</table>

	{$reportContent}
</div>
