Query-playback Android mobile app Using Rdio API
===

1. This is a two-page app: Page 1 is about query; Page 2 is about music playback with Rdio;

2. On Page 1, there is a text box, a mic logo and a search button. Hold down the mic to begin speech query. The query should be the name of a song, for example "November Rain". Release the mic to stop listening and execute speech recognition. Use Google Now API for speech recognition;

3. The speech transcription will be shown in the text box. Then you can tap the text box to modify it via typing. You can also type the query into the empty text box in the beginning, instead of doing a speech query;

4. Press the search button to send the string to Rdio search API. You will be taken to Page 2;

5. On Page 2, if a match is found for the song entered from Rdio search, it will automatically play the first song with Rdio playback API; if not, you will see the error msg "cannot find a song from Rdio";
