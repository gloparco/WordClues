/*global $, document, window, setTimeout, console, Hammer, alert, setInterval, clearInterval */
var GAME = {};

GAME.game = (function (w)
	{
	// CONSTANTS
	var HELP_SCREENS = 3,
		CLUES = 5,
		MAX_SCORE = 100,
		BONUS_SCORE = { LOW: 20, MEDIUM: 40, HIGH: 80 },
		BONUS_LETTERS = { LOW: 3, MEDIUM: 2, HIGH: 1 },
		BONUS_QUESTION = [4, 9],
		DOUBLE_SCORES_PER_GAME = 4,
		TRIPLE_SCORES_PER_GAME = 1,
		CLUE_REDUCTION = 10,
		LETTER_REDUCTION = 25,
		REVEAL_ALL_PER_GAME = 2,
		FREE_GAME_LIMIT = 5,
		QUESTIONS_PER_GAME = 10,
		PERFECT_SCORE = (MAX_SCORE * TRIPLE_SCORES_PER_GAME * 2) + (MAX_SCORE * DOUBLE_SCORES_PER_GAME)
						+ (BONUS_SCORE.HIGH * BONUS_QUESTION.length * CLUES)
						+ (MAX_SCORE * (QUESTIONS_PER_GAME - BONUS_QUESTION.length)),
		WAIT_FOR_ANIMATION_TO_END = 320,
		WAIT_FOR_GUESS_BUTTON_ANIMATION_TO_END = 520,
		WAIT_FOR_MESSAGE = 1520,
		WAIT_FOR_CLUEBOARD = 1000;

	var gameNo = 0,
		$home,
		$stats_gp,
		$stats_tp,
		$stats_ap,
		$stats_hs,
		$stats_ls,
		$stats_cs,
		$iapPopup,
		$iapTripleWordScorePopup,
		$iapLifeSaverPopup,
		$restorePopup,
		$infoPopup,
		$game,
		$clueBoard,
		$guessButtons,
		$guessButtonHigh,
		$guessButtonMedium,
		$guessButtonLow,
		$buttonDescriptionHigh,
		$buttonDescriptionMedium,
		$buttonDescriptionLow,
		$letters,
		$powerButton,
		$okButton,
		$clear,
		$del,
		$powerOptions,
		$doubleScore,
		$tripleScore,
		$lifeSaver,
		$revealLetter,
		$revealVowel,
		$revealAllClues,
		$vowelSelect,
		$question,
		$worth,
		$answerLength,
		$score,
		$guess,
		$dsLeft,
		$tsLeft,
		$tsBanked,
		$lsBanked,
		$raLeft,
		$multiplier,
		$clueCover,
		$cluesLeft,
		$clue = [],
		$bonus = [],
		$sound,
		$help,
		$helpScreen = [],
		$settings,
		$settingsButton,
		$message,
		$message2,
		$popup,
		$popupCover,
		$price,
		$tripleWordPrice,
		$lifeSaverPrice,
		$currentIapPopup,
		guessedWord,
		settingsOpened = false,
		settingsData,
		statsData = {}, // may be access early on bridge initialization
		achievementsData = {},
		gameData,
		currentQuestion,
		popupIsShown,
		infoPopupShown,
		iapPopupShown,
		powerOptionsShown,
		vowelPopupShown,
		powerButtonDisabled,
		gameOver,
		currentHelpScreen,
		unlimitedOverride,
		passButtonActive,
		currentQuestionIsBonus,
		touchDisabled,
		allowDelete,
		thankYouMsg,
		bonusClue = [],
		achievementMsg = [],
		gamesLeft = FREE_GAME_LIMIT,
		gameTitleCount = 0,
        currentPopupNext;


	function onLoaded()
		{
		$(w).unbind('load', onLoaded);
		initErrorHandler();
		initApp();

		loadSettings(completeInit);
		}

    function completeInit()
        {
        applyAllSettings();
        initBridge();
        checkGameCenter();
        loadNonConsumablePurchases();

		// remove 300ms delay
		Hammer(document.body).on('tap', function(e)
			{
			onTouchEvent(e);
			});
		}

        // Handle all touch events
	function onTouchEvent(e)
        {
        if (!touchDisabled)
            {
            routeTouchEvent(e);
            }
        }

    function routeTouchEvent(e)
        {
        var message,
            targetID = getTargetID(e.target);
		switch (targetID)
			{
			// Info Button on Home Screen
			case 'infoButton':
				GAME.sound.play('click');
				infoPopupShown = true;
				$infoPopup.addClass('show');
				$popupCover.addClass('show dim');
				break;

			// Info Popup on Home Screen
			case 'infoPopup':
				GAME.sound.play('lowClick');
				hideInfoPopup();
				break;
			case 'rateApp':
				GAME.sound.play('lowClick');
				GAME.bridge.request('link.store');
				hideInfoPopup();
				break;
			case 'feedback':
				GAME.sound.play('lowClick');
				GAME.bridge.request('link.email');
				hideInfoPopup();
				break;
			case 'kgTwitter':
				GAME.sound.play('lowClick');
				GAME.bridge.request('link.twitter');
				hideInfoPopup();
				break;
			case 'kgFacebook':
				GAME.sound.play('lowClick');
				GAME.bridge.request('link.facebook');
				hideInfoPopup();
				break;

			// Facebook Button on Home Screen
			case 'facebook':
				GAME.sound.play('click');
				message = {message:'I just scored ' + settingsData.current.score + ' playing Word Clues, a fun, new word game from @KindredGames! You can play too: ' + this.messages.linkStore + ' #wordclues'};
				GAME.bridge.request('post.facebook', message,
					function (err, status)
						{
						if (err !== null || (status && !status.ok))
							{
							alert('You are not currently signed in to Facebook.  To do so, go to ' + this.messages.settingsFacebook);
							}
						});
				break;

			// Twitter Button on Home Screen
			case 'twitter':
				GAME.sound.play('click');
				message = {message:'I just scored ' + settingsData.current.score + ' playing Word Clues, a fun, new word game from @KindredGames! You can play too: ' + this.messages.linkStore + ' #wordclues'};
				GAME.bridge.request('post.twitter', message,
					function (err, status)
						{
						if (err !== null || (status && !status.ok))
							{
							alert('You are not currently signed in to Twitter.  To do so, go to ' + this.messages.settingsTwitter);
							}
						});
				break;

			// Leaderboard Button on Home Screen
			case 'leaderboardButton':
				GAME.sound.play('click');
				GAME.bridge.request('display.gamecenter');
				break;

			// In-App Purchase: Non-comsumable, unlimited plays
			case 'buy':
				GAME.sound.play('click');
				GAME.bridge.request('store.buy', { productId: 'WordCluesUnlimitedGames' });
				hideIapPopup();
				break;

			// Show Restore Purchase Popup Confirmation
			case 'restoreLink':
				GAME.sound.play('click');
				hideIapPopup();
				showRestorePopup();
				break;

			// Send Restore Purchase Message
			case 'restoreButton':
				GAME.sound.play('click');
				GAME.bridge.request('store.restore', { productId: 'WordCluesUnlimitedGames' });
				hideRestorePopup();
				break;

			// In-App Purchase: Comsumable, 10 Triple Word Scores
			case 'tripleWordBuyButton':
				GAME.sound.play('click');
				GAME.bridge.request('store.buy', { productId: 'WordCluesTripleWordScore' });
				hideIapPopup();
				break;

			// In-App Purchase: Comsumable, 10 Life Savers
			case 'lifeSaverBuyButton':
				GAME.sound.play('click');
				GAME.bridge.request('store.buy', { productId: 'WordCluesLifeSaver' });
				hideIapPopup();
				break;

			// Close IAP Popup
			case 'iapPopup':
			case 'iapTripleWordScorePopup':
			case 'iapLifeSaverPopup':
				GAME.sound.play('lowClick');
				hideIapPopup();
				break;

			// Restore Popup
			case 'restorePopup':
				GAME.sound.play('lowClick');
				hideRestorePopup();
				break;

			// Play Button on Home Screen
			case 'playButton':
				GAME.sound.play('click');
				gameTitleCount = 0;
				if (unlimitedOverride || gamesLeft > 0)
					{
					showGameView();
					}
				else
					{
					showIapPopup($iapPopup);
					}
				break;

			// OK Button on Game Screen
			case 'okButton':
				GAME.sound.play('click');
				$okButton.removeClass('show');
				if (currentQuestionIsBonus)
					{
					checkBonusAnswer();
					}
				else
					{
					checkAnswer();
					}
				break;

			// Clear Button on Game Screen
			case 'clearPass':
				GAME.sound.play('click');
				if (passButtonActive)
					{
					if (currentQuestionIsBonus)
						{
						checkBonusAnswer();
						}
					else
						{
						checkAnswer();
						}
					}
				else
					{
					resetGuess();
					}
				break;

			// One Letter Guess Button for Bonus Question on Game Screen
			case 'guessButtonHigh':
				GAME.sound.play('click');
				bonusGuess(BONUS_SCORE.HIGH, BONUS_LETTERS.HIGH);
				break;

			// Two Letters Guess Button for Bonus Question on Game Screen
			case 'guessButtonMedium':
				GAME.sound.play('click');
				bonusGuess(BONUS_SCORE.MEDIUM, BONUS_LETTERS.MEDIUM);
				break;

			// Three Letters Guess Button for Bonus Question on Game Screen
			case 'guessButtonLow':
				GAME.sound.play('click');
				bonusGuess(BONUS_SCORE.LOW, BONUS_LETTERS.LOW);
				break;

			// Keyboard Letter Button on Game Screen
			case 'a': case 'b': case 'c': case 'd': case 'e':
			case 'f': case 'g': case 'h': case 'i': case 'j':
			case 'k': case 'l': case 'm': case 'n': case 'o':
			case 'p': case 'q': case 'r': case 's': case 't':
			case 'u': case 'v': case 'w': case 'x': case 'y': case 'z':
				updateGuess(targetID);
				allowDelete = true;
				break;

			// Keyboard Delete Button on Game Screen
			case 'del':
				if (!settingsData.current.lifeSaverPlayed
					|| (settingsData.current.lifeSaverPlayed
						&& allowDelete))
					{
					removeFromGuess();
					allowDelete = false;
					}
				break;

			// Power Button on Game Screen
			case 'powerButton':
				if (powerButtonDisabled) return;
				GAME.sound.play('click');
				showPowerOptions();
				break;

			// Power Options on Game Screen
			case 'powerOptions':
				hidePowerOptions();
				break;
			case 'revealVowel':
				GAME.sound.play('click');
				if (settingsData.current.vowelRevealed.length > 0 || settingsData.current.lifeSaverPlayed)
					{
					return;
					}
				hidePowerOptions(showVowelPopup);
				break;
			case 'revealLetter':
				if (settingsData.current.firstLettersRevealed > 0 || settingsData.current.lifeSaverPlayed)
					{
					GAME.sound.play('click');
					return;
					}
				GAME.sound.play('power');
				adjustWorth(-LETTER_REDUCTION);
				settingsData.current.firstLettersRevealed++;
				saveSettings();
				hidePowerOptions(resetGuess);
				break;
			case 'revealAllClues':
				if (settingsData.current.revealAllLeft === 0 || settingsData.current.allCluesRevealed
					|| settingsData.current.lifeSaverPlayed)
					{
					GAME.sound.play('click');
					return;
					}
				GAME.sound.play('power');
				settingsData.current.revealAllLeft--;
				$raLeft.text(settingsData.current.revealAllLeft);
				hidePowerOptions(showAllClues);
				break;
			case 'tripleScore':
				if (settingsData.tripleScoresBanked === 0
					|| settingsData.current.tripleScoresLeft === 0
					|| settingsData.current.multiplier !== 1)
					{
					GAME.sound.play('click');
					hidePowerOptions(showIapPopup, $iapTripleWordScorePopup);
					return;
					}
				GAME.sound.play('power');
				settingsData.current.tripleScoresLeft--;
				settingsData.tripleScoresBanked--;
				settingsData.current.multiplier = 3;
				$tsLeft.text(settingsData.current.tripleScoresLeft);
				$tsBanked.text(settingsData.tripleScoresBanked);
				$multiplier.text(settingsData.current.multiplier);
				$multiplier.removeClass('hide').addClass('triple');
				saveSettings();
				hidePowerOptions();
				break;
			case 'doubleScore':
				if (settingsData.current.doubleScoresLeft === 0 || settingsData.current.multiplier !== 1)
					{
					GAME.sound.play('click');
					return;
					}
				GAME.sound.play('power');
				settingsData.current.doubleScoresLeft--;
				settingsData.current.multiplier = 2;
				$dsLeft.text(settingsData.current.doubleScoresLeft);
				$multiplier.text(settingsData.current.multiplier);
				$multiplier.removeClass('hide');
				saveSettings();
				hidePowerOptions();
				break;
			case 'lifeSaver':
				// if player has no LifeSavers left, and tries to play one, prompt them to buy more
				if (settingsData.lifeSaversBanked === 0)
					{
					GAME.sound.play('click');
					hidePowerOptions(showIapPopup, $iapLifeSaverPopup);
					return;
					}
				// check if LifeSaver has already been played for current question
				if (settingsData.current.lifeSaverPlayed
					|| settingsData.current.clueNo + 1 < CLUES)
					{
					GAME.sound.play('click');
					return;
					}
				GAME.sound.play('power');
				hidePowerOptions(playLifeSaver);
				break;
			case 'buyTripleScores':
				GAME.sound.play('click');
				hidePowerOptions(showIapPopup, $iapTripleWordScorePopup);
				break;
			case 'buyLifeSavers':
				GAME.sound.play('click');
				hidePowerOptions(showIapPopup, $iapLifeSaverPopup);
				break;

			// Vowel Select on Game Screen
			case 'vowelSelect':
				hideVowelPopup();
				break;
			case 'vowel_a': case 'vowel_e': case 'vowel_i': case 'vowel_o': case 'vowel_u':
				GAME.sound.play('power');
				adjustWorth(-LETTER_REDUCTION);
				var selectedLetter = targetID.split('_')[1];
				settingsData.current.vowelRevealed = selectedLetter;
				resetGuess();
				$vowelSelect.removeClass('show');
				$popupCover.removeClass('show').removeClass('dim');
				$('#' + settingsData.current.vowelRevealed).addClass('disable');
				popupIsShown = false;
				saveSettings();
				break;

			// Settings Button on Game Screen
			case 'settingsButton':
				GAME.sound.play('clickSwoosh');
				toggleSettingsMenu();
				break;

			// Individual Settings Buttons within the Settings Menu on Game Screen
			case 'set_bg':
			case 'set_dark':
			case 'set_font':
			case 'set_sound':
			case 'set_help':
			// Also, dark/light button on Home Screen
			case 'darkButton':
				GAME.sound.play('click');
				toggleSettings(targetID);
				break;

			// Help Overlay on Game Screen
			case 'help':
			case 'helpScreen1':
			case 'helpScreen2':
			case 'helpScreen3':
				GAME.sound.play('keyBack');
				showHelp();
				break;

			// Back Button (to close game) on Game Screen
			case 'backButton':
				GAME.sound.play('click');
				showHomeView();
				break;

			// OK Button on Popup
			case 'popup':
				GAME.sound.play('click');
				if (popupIsShown) { hidePopup(); }
				break;

			// Popup Cover - i.e., player touches anywhere other than popup to dismiss it
			case 'popupCover':
				if (powerOptionsShown) { hidePowerOptions(); }
				if (vowelPopupShown) { hideVowelPopup(); }
				if (infoPopupShown) { hideInfoPopup(); }
				if (iapPopupShown) { hideIapPopup(); }
				break;

			// Easter egg
			case 'gameTitle':
				gameTitleCount++;
				if (gameTitleCount === 10)
					{
					settingsData.tripleScoresBanked += 5;
					settingsData.lifeSaversBanked += 5;
					showPopup('<h1>Tsk-Tsk!</h1>David, do you really need this?  You have been given 5 Life Savers and 5 Triple Word Scores... use them wisely Buddy!<div class="popupButton">OK</div>');
					}
				break;
			}
		}

		function initErrorHandler()
			{
			window.onerror = function(errorMsg, url, lineNumber)
				{
				'use strict';
				var error = 'Exception [' + errorMsg + '] at ' + url + ' on line ' + lineNumber;
				try
					{
					GAME.bridge.logError(error);
					return;
					}
				catch (err) {}
				console.log(error);
				};
			}

	// Initialization of the App
	function initApp()
		{
		$home = $('#home');
		$stats_gp = $('#stats_gp');
		$stats_tp = $('#stats_tp');
		$stats_ap = $('#stats_ap');
		$stats_hs = $('#stats_hs');
		$stats_ls = $('#stats_ls');
		$stats_cs = $('#stats_cs');
		$infoPopup = $('#infoPopup');
		$iapPopup = $('#iapPopup');
		$iapTripleWordScorePopup = $('#iapTripleWordScorePopup');
		$iapLifeSaverPopup = $('#iapLifeSaverPopup');
		$restorePopup = $('#restorePopup');
		$game = $('#game');
		$clueBoard = $('#clueBoard');
		$clueCover = $('#clueCover');
		$cluesLeft = $('#cluesLeft');
		$guessButtons = $('#guessButtons');
		$guessButtonHigh = $('#guessButtonHigh');
		$guessButtonMedium = $('#guessButtonMedium');
		$guessButtonLow = $('#guessButtonLow');
		$buttonDescriptionHigh = $('#buttonDescriptionHigh');
		$buttonDescriptionMedium = $('#buttonDescriptionMedium');
		$buttonDescriptionLow = $('#buttonDescriptionLow');
		$letters = $('#letters');
		$powerButton = $('#powerButton');
		$okButton = $('#okButton');
		$clear = $('#clearPass');
		$del = $('#del');
		$powerOptions = $('#powerOptions');
		$doubleScore = $('#doubleScore');
		$tripleScore = $('#tripleScore');
		$lifeSaver = $('#lifeSaver');
		$revealLetter = $('#revealLetter');
		$revealVowel = $('#revealVowel');
		$revealAllClues = $('#revealAllClues');
		$vowelSelect = $('#vowelSelect');
		$question = $('#question');
		$worth = $('#worth');
		$answerLength = $('#answerLength');
		$score = $('#score');
		$guess = $('#guess');
		$dsLeft = $('#dsLeft');
		$tsLeft = $('#tsLeft');
		$tsBanked = $('#tsBanked');
		$lsBanked = $('#lsBanked');
		$raLeft = $('#raLeft');
		$multiplier = $('#multiplier');
		$sound = $('#set_sound');
		$help = $('#help');
		$settings = $('#settings');
		$settingsButton = $('#settingsButton');
		$message = $('#message');
		$message2 = $('#message2');
		$popup = $('#popup');
		$popupCover = $('#popupCover');
		$price = $('#price');
		$tripleWordPrice = $('#tripleWordPrice');
		$lifeSaverPrice = $('#lifeSaverPrice');
		$currentIapPopup = $iapPopup;
		popupIsShown = false;
		powerOptionsShown = false;
		vowelPopupShown = false;
		powerButtonDisabled = false;
		gameOver = false;
		currentHelpScreen = 0;
		unlimitedOverride = false;
		passButtonActive = true;
		currentQuestionIsBonus = false;
		touchDisabled = false;
        allowDelete = true;
        thankYouMsg = false;

		var i;

		for (i = 0; i < HELP_SCREENS; i++)
			{
			$helpScreen[i] = $('#helpScreen' + (i + 1));
			}

		for (i = 0; i < CLUES; i++)
			{
			var coverNo = i + 1;
			$clue[i] = $('#clue' + coverNo);
			$bonus[i] = $('#bonus' + coverNo);
			}
		}

	function initBridge()
		{
		GAME.bridge.init(handleResponse);
		}

	// Reset the currently loaded game
	function resetGame()
		{
		settingsData.current.score = 0;
		settingsData.current.questionNo = 0;
		settingsData.current.clueNo = 0;
		settingsData.current.doubleScoresLeft = DOUBLE_SCORES_PER_GAME;
		settingsData.current.tripleScoresLeft = TRIPLE_SCORES_PER_GAME;
		settingsData.current.revealAllLeft = REVEAL_ALL_PER_GAME;
		settingsData.current.multiplier = 1;
		settingsData.current.firstLettersRevealed = 0;
		settingsData.current.vowelRevealed = '';
		settingsData.current.guessWorth = MAX_SCORE;
		settingsData.current.pendingBonus = [];
		settingsData.current.bonusLettersSelected = 0;
		$dsLeft.text(settingsData.current.doubleScoresLeft);
		$tsLeft.text(settingsData.current.tripleScoresLeft);
		$tsBanked.text(settingsData.tripleScoresBanked);
		$lsBanked.text(settingsData.lifeSaversBanked);
		$raLeft.text(settingsData.current.revealAllLeft);
		saveSettings();

		gameData = null;
		saveGameData();
		}

	// Clear the $guess div
	function resetGuess()
		{
		var htmlCode = '';

		htmlCode = (currentQuestionIsBonus)
					? currentQuestion.clues[settingsData.current.clueNo].replace('%', '')
					: currentQuestion.answer;
		$guess.html(showBlanks(htmlCode));
		resetToNormalQuestion();
		resetClues();
		}

	// Reset back to initial state
	function resetToNormalQuestion()
		{
		$okButton.removeClass('show');
		$letters.removeClass('dim');
		$clear.addClass('off');
		$clear.text('PASS');
		passButtonActive = true;
		$del.addClass('off');
		}

	// Reset clues to match the guess area
	function resetClues()
		{
		// if it's a bonus question, only reset the current clue
		if (currentQuestionIsBonus)
			{
			populateClue(settingsData.current.clueNo, true);
			}
		// normal (non-bonus) questions reset all clues
		else
			{
			// iterate over clues
			for (var i = 0; i < $clue.length; i++)
				{
				populateClue(i, false);
				}
			}
		}

	// Show blank letters (or revealed vowels) for answer
	function showBlanks(word, ignoreBonusLetters)
		{
		var htmlCode = '';

		// reset guessedWord
		guessedWord = '';

		for (var i = 0; i < word.length; i++)
			{
			if (!currentQuestionIsBonus
					&& (i < settingsData.current.firstLettersRevealed || currentQuestion.answer[i] === settingsData.current.vowelRevealed))
				{
				htmlCode += '<div class="space revealed">' + word[i] + '</div>';
				guessedWord += word[i];
				}
			else if (currentQuestionIsBonus
						&& ignoreBonusLetters === undefined
						&& settingsData.current.bonusLettersRevealed.indexOf(i) !== -1)
				{
				htmlCode += '<div class="space revealed">' + word[i] + '</div>';
				guessedWord += word[i];
				}
			else
				{
				htmlCode += '<div class="space">&nbsp;</div>';
				guessedWord += ' ';
				}
			}

		return htmlCode;
		}

	// Letter Selected
	function updateGuess(letter)
		{
		var i, j, pos,
			letterIndex = guessedWord.indexOf(' ');

		if (letterIndex === -1) return;
		if (letter === settingsData.current.vowelRevealed) return;

		GAME.sound.play('keyClick');
		$clear.removeClass('off');
		$clear.text('clear');
		passButtonActive = false;
		$del.removeClass('off');

		$guess[0].children[letterIndex].innerText = letter;

		// if it's a bonus question, only write the letter to the current clue
		if (currentQuestionIsBonus)
			{
			for (j = 0; j < $clue[settingsData.current.clueNo][0].children.length; j++)
				{
				// if it's the div containing the word to be guessed
				if ($clue[settingsData.current.clueNo][0].children[j].className === 'beginSpace'
					|| $clue[settingsData.current.clueNo][0].children[j].className === 'endSpace')
					{
					for (pos = 0; pos < $clue[settingsData.current.clueNo][0].children[j].children.length; pos++)
						{
						if ($clue[settingsData.current.clueNo][0].children[j].children[pos].className.indexOf('revealed') === -1
								&& $clue[settingsData.current.clueNo][0].children[j].children[pos].innerHTML === '&nbsp;')
							{
							$clue[settingsData.current.clueNo][0].children[j].children[pos].innerText = letter;
							break;
							}
						}
					}
				}
			}
		// normal (non-bonus) questions write the letter to all clues
		else
			{
			for (i = 0; i < $clue.length; i++)
				{
				for (j = 0; j < $clue[i][0].children.length; j++)
					{
					// if it's the div containing the word to be guessed
					if ($clue[i][0].children[j].className === 'beginSpace'
						|| $clue[i][0].children[j].className === 'endSpace')
						{
						for (pos = 0; pos < $clue[i][0].children[j].children.length; pos++)
							{
							if ($clue[i][0].children[j].children[pos].className.indexOf('revealed') === -1
									&& $clue[i][0].children[j].children[pos].innerHTML === '&nbsp;')
								{
								$clue[i][0].children[j].children[pos].innerText = letter;
								break;
								}
							}
						}
					}
				}
			}
		guessedWord = guessedWord.substr(0, letterIndex) + letter + guessedWord.substr(letterIndex + 1);
		if (guessedWord.indexOf(' ') === -1)
			{
			$okButton.addClass('show');
			$letters.removeClass('show').addClass('dim');
			}
		}

	// Remove last letter entered
	function removeFromGuess()
		{
		var i, j,
			removed = false,
			another = false;

		$okButton.removeClass('show');
		$letters.removeClass('dim');

		for (i = guessedWord.length; i--;)
			{
			if (guessedWord[i] !== ' '
					&& (!currentQuestionIsBonus && (guessedWord[i] !== settingsData.current.vowelRevealed && i !== settingsData.current.firstLettersRevealed - 1)
						|| (currentQuestionIsBonus && settingsData.current.bonusLettersRevealed.indexOf(i) === -1)))
				{
				if (removed)
					{
					another = true;
					break;
					}
				GAME.sound.play('keyBack');
				guessedWord = guessedWord.substr(0, i) + ' ' + guessedWord.substr(i + 1);
				$guess[0].children[i].innerHTML = '&nbsp;';

				// if it's a bonus question, only remove the letter from the current clue
				if (currentQuestionIsBonus)
					{
					for (j = 0; j < $clue[settingsData.current.clueNo][0].children.length; j++)
						{
						// if it's the div containing the word to be guessed
						if ($clue[settingsData.current.clueNo][0].children[j].className.indexOf('shownWord') === -1)
							{
							$clue[settingsData.current.clueNo][0].children[j].children[i].innerHTML = '&nbsp;';
							}
						}
					}
				// normal (non-bonus) questions remove the letter from all clues
				else
					{
					for (var clueNum = 0; clueNum < $clue.length; clueNum++)
						{
						for (j = 0; j < $clue[clueNum][0].children.length; j++)
							{
							// if it's the div containing the word to be guessed
							if ($clue[clueNum][0].children[j].className.indexOf('shownWord') === -1)
								{
								$clue[clueNum][0].children[j].children[i].innerHTML = '&nbsp;';
								}
							}
						}
					}

				removed = true;
				}
			}

		// if there are no other user played letters, then turn off the Clear Button
		if (!another)
			{
			$clear.addClass('off');
			$clear.text('PASS');
			passButtonActive = true;
			$del.addClass('off');
			}
		}

	// Show In-App Purchase Popup
	function showIapPopup($el)
		{
		iapPopupShown = true;
		$currentIapPopup = $el;
		$el.addClass('show');
		$popupCover.addClass('show dim');
		}

	// Hide In-App Purchase Popup
	function hideIapPopup()
		{
		iapPopupShown = false;
		$currentIapPopup.removeClass('show');
		$popupCover.removeClass('show').removeClass('dim');
		}

	// Show Restore Purchase Popup
	function showRestorePopup()
		{
		iapPopupShown = true;
		$restorePopup.addClass('show');
		$popupCover.addClass('show dim');
		}

	// Hide Restore Purchase Popup
	function hideRestorePopup()
		{
		iapPopupShown = false;
		$restorePopup.removeClass('show');
		$popupCover.removeClass('show').removeClass('dim');
		}

	// Asynchronous Callback for bridge calls
	function handleResponse(message)
		{
		if (message.topic === 'store.paid')
			{
			thankYouMsg = true;
			switch(message.productId)
				{
				case 'WordCluesUnlimitedGames':
					unlimitedOverride = true;
					showPopup('<h1>Thank you!</h1>You have unlocked Unlimited Games. Thank you for your support!<div class="popupButton">OK</div>',
                        function()
                            {
                            thankYouMsg = false;
                            });
					break;

				case 'WordCluesTripleWordScore':
					settingsData.tripleScoresBanked += 10;
					$tsBanked.text(settingsData.tripleScoresBanked);
					saveSettings();
					showPopup('<h1>Thank you!</h1>You have banked 10 additional Triple Word Scores. Thank you for your support!<div class="popupButton">OK</div>');
					break;

				case 'WordCluesLifeSaver':
					settingsData.lifeSaversBanked += 10;
					$lsBanked.text(settingsData.lifeSaversBanked);
					saveSettings();
					showPopup('<h1>Thank you!</h1>You have banked 10 additional Life Savers. Thank you for your support!<div class="popupButton">OK</div>');
					break;
				}
			}
		else if (message.topic === 'store.priced')
			{
			switch(message.productId)
				{
				case 'WordCluesUnlimitedGames':
					$price.text(message.price);
					break;

				case 'WordCluesTripleWordScore':
					$tripleWordPrice.text(message.price);
					break;

				case 'WordCluesLifeSaver':
					$lifeSaverPrice.text(message.price);
					break;
				}
			}
		else if (message.topic === 'network')
			{
			if (message.type === 'inet')
				{
				if (message.on)
					{
					//TODO: Network is ON do something if needed
					}
				else
					{
					//TODO: Network is OFF do something if needed
					}
				}
			}
        else if (message.topic === 'leaderboards')
            {
            if (!isEmptyObj(message.scores))
                {
				loadStats(message.scores);
                }
            }
        else if (message.topic === 'achievements')
            {
            if (!isEmptyObj(message.achievements))
                {
                syncAchievements(message.achievements);
                }
            }
		}

	// Hide Info Menu Popup
	function hideInfoPopup()
		{
		$infoPopup.removeClass('show');
		$popupCover.removeClass('show').removeClass('dim');
		infoPopupShown = false;
		}

	// Load In-App Purchase info
	function loadNonConsumablePurchases()
		{
		GAME.bridge.request('store.owns', { productId: 'WordCluesUnlimitedGames' },
			function(err, message)
				{
				if (err)
					{
					console.log(err);
					}
				else
                    {
                    var pricedProductIds = ['WordCluesTripleWordScore', 'WordCluesLifeSaver'];
                    if (isValidUnlimitedReceipt(message))
                        {
                        unlimitedOverride = true;
                        }
                    else
                        {
                        unlimitedOverride = false;
                        pricedProductIds.push('WordCluesUnlimitedGames');
                        }
                    GAME.bridge.request('store.price', { productIds: pricedProductIds });
                    }
				checkForPurchase();
				}
			);
		}

    function isValidUnlimitedReceipt(receipt)
        {
        return (typeof receipt === 'object' && !isEmptyObj(receipt) && receipt.productId === 'WordCluesUnlimitedGames' && receipt.quantity > 0 && receipt.transactionId);
        }

    // Determine if player has unlocked all games
	function checkForPurchase()
		{
		// Player has not paid for unlimited games
		if (!unlimitedOverride)
			{
			gamesLeft = FREE_GAME_LIMIT - intOrZero(statsData.gamesPlayed);
			if (gamesLeft < 0) gamesLeft = 0;
			}
		}

	// Determine if player is logged in to Game Center
	function checkGameCenter()
		{
		GAME.bridge.request('check.gamecenter', '',
			function(err)
				{
				if (err)
					{
					console.log(err);
					return;
					}

				showStats();
				loadExternalAchievements();
				}
			);
		}

	// Show stats on the Home Screen
	function loadExternalAchievements()
		{
		GAME.bridge.request('achievements.get', '',
			function(err, message)
				{
				if (err)
					{
					console.log(err);
					return;
					}
				loadAchievements(message);
				}
			);
		}

	// Show stats on the Home Screen
	function showStats()
		{
		GAME.bridge.request('leaderboards.get', ['gamesPlayed', 'totalPoints', 'avgPoints', 'highScore', 'longestStreak', 'currentStreak'],
			function(err, message)
				{
				if (err)
					{
					console.log(err);
					return;
					}
				loadStats(message);
				}
			);
		}

	// Update the Stats Board
	function displayStats()
		{
		$stats_gp.text(formatNumber(statsData.gamesPlayed));
		$stats_tp.text(formatNumber(statsData.totalPoints));
		$stats_ap.text(formatNumber(statsData.avgPoints));
		$stats_hs.text(formatNumber(statsData.highScore));
		$stats_ls.text(formatNumber(statsData.longestStreak));
		$stats_cs.text(formatNumber(statsData.currentStreak));

		// Show new version message
		if (settingsData.firstTimeVersion21)
			{
			newVersionMsg();
			}
		}

	function formatNumber(value)
		{
		return (value === null || value === undefined || isNaN(value)) ? 0 : value;
		}

	// Save stats to cache and to Game Center
	function saveStats()
		{
		// calculate avgPoints
		var avgPts = (statsData.gamesPlayed > 0) ? Math.round(statsData.totalPoints / statsData.gamesPlayed) : 0;

		// Save to local cache
		GAME.bridge.request('set.stats',
			{
			gamesPlayed: statsData.gamesPlayed,
			totalPoints: statsData.totalPoints,
			avgPoints: avgPts,
			highScore: statsData.highScore,
			longestStreak: statsData.longestStreak,
			currentStreak: statsData.currentStreak
			});
		// Save to Game Center (system of record)
		GAME.bridge.request('leaderboards.set',
			{
			gamesPlayed: statsData.gamesPlayed,
			totalPoints: statsData.totalPoints,
			avgPoints: avgPts,
			highScore: statsData.highScore,
			longestStreak: statsData.longestStreak,
			currentStreak: statsData.currentStreak
			});
		}

	// Check to see if there are new achievements; if so, save to Game Center
	function checkAchievements()
		{
		achievementMsg.length = 0;

		// Figure out if there are any new achievements
		if (notAchieved(achievementsData.WordClues500PointGame)
			&& statsData.highScore >= 500)
			{
			achievementsData.WordClues500PointGame = achieved();
			settingsData.lifeSaversBanked++;
			achievementMsg.push('500 Point Game');
			}
		if (notAchieved(achievementsData.WordClues1000PointGame)
			&& statsData.highScore >= 1000)
			{
			achievementsData.WordClues1000PointGame = achieved();
			settingsData.lifeSaversBanked++;
			achievementMsg.push('1,000 Point Game');
			}
		if (notAchieved(achievementsData.WordClues1500PointGame)
			&& statsData.highScore >= 1500)
			{
			achievementsData.WordClues1500PointGame = achieved();
			settingsData.lifeSaversBanked++;
			achievementMsg.push('1,500 Point Game');
			}
		if (notAchieved(achievementsData.WordClues2000PointGame)
			&& statsData.highScore >= 2000)
			{
			achievementsData.WordClues2000PointGame = achieved();
			settingsData.lifeSaversBanked++;
			achievementMsg.push('2,000 Point Game');
			}
		if (notAchieved(achievementsData.WordCluesPerfectGame)
			&& statsData.highScore >= PERFECT_SCORE)
			{
			achievementsData.WordCluesPerfectGame = achieved();
			settingsData.lifeSaversBanked++;
			settingsData.tripleScoresBanked++;
			achievementMsg.push('Perfect Game');
			}
		if (notAchieved(achievementsData.WordClues10GamesPlayed)
			&& statsData.gamesPlayed >= 10)
			{
			achievementsData.WordClues10GamesPlayed = achieved();
			settingsData.tripleScoresBanked++;
			achievementMsg.push('10 Games Played');
			}
		if (notAchieved(achievementsData.WordClues50GamesPlayed)
			&& statsData.gamesPlayed >= 50)
			{
			achievementsData.WordClues50GamesPlayed = achieved();
			settingsData.tripleScoresBanked++;
			achievementMsg.push('50 Games Played');
			}
		if (notAchieved(achievementsData.WordClues100GamesPlayed)
			&& statsData.gamesPlayed >= 100)
			{
			achievementsData.WordClues100GamesPlayed = achieved();
			settingsData.tripleScoresBanked++;
			achievementMsg.push('100 Games Played');
			}
		if (notAchieved(achievementsData.WordClues150GamesPlayed)
			&& statsData.gamesPlayed >= 150)
			{
			achievementsData.WordClues150GamesPlayed = achieved();
			settingsData.tripleScoresBanked++;
			achievementMsg.push('150 Games Played');
			}
		if (notAchieved(achievementsData.WordClues200GamesPlayed)
			&& statsData.gamesPlayed >= 200)
			{
			achievementsData.WordClues200GamesPlayed = achieved();
			settingsData.tripleScoresBanked++;
			achievementMsg.push('200 Games Played');
			}
		if (notAchieved(achievementsData.WordClues10000TotalPoints)
			&& statsData.totalPoints >= 10000)
			{
			achievementsData.WordClues10000TotalPoints = achieved();
			settingsData.lifeSaversBanked++;
			achievementMsg.push('10,000 Total Points');
			}
		if (notAchieved(achievementsData.WordClues25000TotalPoints)
			&& statsData.totalPoints >= 25000)
			{
			achievementsData.WordClues25000TotalPoints = achieved();
			settingsData.lifeSaversBanked++;
			achievementMsg.push('25,000 Total Points');
			}
		if (notAchieved(achievementsData.WordClues50000TotalPoints)
			&& statsData.totalPoints >= 50000)
			{
			achievementsData.WordClues50000TotalPoints = achieved();
			settingsData.lifeSaversBanked++;
			achievementMsg.push('50,000 Total Points');
			}
		if (notAchieved(achievementsData.WordClues75000TotalPoints)
			&& statsData.totalPoints >= 75000)
			{
			achievementsData.WordClues75000TotalPoints = achieved();
			settingsData.lifeSaversBanked++;
			achievementMsg.push('75,000 Total Points');
			}
		if (notAchieved(achievementsData.WordClues100000TotalPoints)
			&& statsData.totalPoints >= 100000)
			{
			achievementsData.WordClues100000TotalPoints = achieved();
			settingsData.lifeSaversBanked++;
			achievementMsg.push('100,000 Total Points');
			}
		if (notAchieved(achievementsData.WordClues250000TotalPoints)
			&& statsData.totalPoints >= 250000)
			{
			achievementsData.WordClues250000TotalPoints = achieved();
			settingsData.lifeSaversBanked++;
			settingsData.tripleScoresBanked++;
			achievementMsg.push('250,000 Total Points');
			}
		if (notAchieved(achievementsData.WordCluesStreak10)
			&& statsData.longestStreak >= 10)
			{
			achievementsData.WordCluesStreak10 = achieved();
			achievementMsg.push('10 in a Row');
			}
		if (notAchieved(achievementsData.WordCluesStreak25)
			&& statsData.longestStreak >= 25)
			{
			achievementsData.WordCluesStreak25 = achieved();
			achievementMsg.push('25 in a Row');
			}
		if (notAchieved(achievementsData.WordCluesStreak50)
			&& statsData.longestStreak >= 50)
			{
			achievementsData.WordCluesStreak50 = achieved();
			settingsData.tripleScoresBanked++;
			achievementMsg.push('50 in a Row');
			}
		if (notAchieved(achievementsData.WordCluesStreak100)
			&& statsData.longestStreak >= 100)
			{
			achievementsData.WordCluesStreak100 = achieved();
			settingsData.tripleScoresBanked++;
			achievementMsg.push('100 in a Row');
			}
		if (notAchieved(achievementsData.WordCluesStreak200)
			&& statsData.longestStreak >= 200)
			{
			achievementsData.WordCluesStreak200 = achieved();
			settingsData.lifeSaversBanked++;
			settingsData.tripleScoresBanked++;
			achievementMsg.push('200 in a Row');
			}

		// Save new achievements to Game Center & cache
		if (achievementMsg.length !== 0)
			{
			saveSettings();
			saveAchievements();
			GAME.bridge.request('achievements.set', achievementsData);
			}
		}

    function notAchieved(achievement)
        {
        return achievement === null || achievement === undefined || achievement.value < 100;
        }

    function achieved()
        {
        return { value: 100 };
        }

	// Check the user's guess
	function checkAnswer()
		{
		// Winner
		if (guessedWord === currentQuestion.answer)
			{
			GAME.sound.play('correct');
			statsData.currentStreak++;
			statsData.longestStreak = (statsData.currentStreak > statsData.longestStreak) ? statsData.currentStreak : statsData.longestStreak;
			saveStats();
			checkAchievements();
			showPopup('<h1>Terrific!</h1>You correctly guessed <u>' + guessedWord + '</u> and scored ' + settingsData.current.guessWorth * settingsData.current.multiplier + ' pts<div class="popupButton">OK</div>');
			settingsData.current.score = parseInt(settingsData.current.score, 10) + settingsData.current.guessWorth * settingsData.current.multiplier;
			settingsData.current.clueNo = CLUES;
			}
		// Loser
		else if (settingsData.current.clueNo + 1 === CLUES)
			{
			GAME.sound.play('incorrect');
			statsData.currentStreak = 0;
			saveStats();
			showPopup('<h1>Sorry!</h1>No points this time... the word was <u>' + currentQuestion.answer + '</u><div class="popupButton">OK</div>');
			settingsData.current.clueNo = CLUES;
			}
		// Pass
		else if (guessedWord.indexOf(' ') !== -1)
			{
			GAME.sound.play('incorrect');
			showPopup('<h1>Too Bad!</h1>Not even a guess? Next time...<div class="popupButton">OK</div>');
			}
		// Wrong Answer
		else
			{
			GAME.sound.play('incorrect');
			showPopup('<h1>Nope!</h1>Good try, but <u>' + guessedWord + '</u> is not the answer<div class="popupButton">OK</div>');
			}
		}

	// Check the user's guess on a Bonus Question
	function checkBonusAnswer()
		{
		// Correct Answer
		if (guessedWord === bonusClue[settingsData.current.clueNo])
			{
			GAME.sound.play('correct');
			$bonus[settingsData.current.clueNo].removeClass('unanswered');
			statsData.currentStreak++;
			statsData.longestStreak = (statsData.currentStreak > statsData.longestStreak) ? statsData.currentStreak : statsData.longestStreak;
			saveStats();
			checkAchievements();
			showPopup('<h1>Terrific!</h1>You correctly guessed <u>' + guessedWord + '</u> for ' + settingsData.current.pendingBonus[settingsData.current.clueNo] + ' bonus pts<div class="popupButton">OK</div>');
			return;
			}
		// Incorrect Answer
		else
			{
			GAME.sound.play('incorrect');
			$bonus[settingsData.current.clueNo].text('0');
			$bonus[settingsData.current.clueNo][0].className = 'bonusPts zero';
			// fill in the correct word
			for (var c = 0; c < $clue[settingsData.current.clueNo][0].children.length; c++)
				{
				var htmlCode = '';
				if ($clue[settingsData.current.clueNo][0].children[c].className === 'beginSpace'
					|| $clue[settingsData.current.clueNo][0].children[c].className === 'endSpace')
					{
					for (var i = 0; i < bonusClue[settingsData.current.clueNo].length; i++)
						{
						htmlCode += '<div class="space">' + bonusClue[settingsData.current.clueNo][i] + '</div>';
						}
					$clue[settingsData.current.clueNo][0].children[c].innerHTML = htmlCode;
					break;
					}
				}
			statsData.currentStreak = 0;
			saveStats();
			showPopup('<h1>Sorry!</h1>No points this time... the word was <u>' + bonusClue[settingsData.current.clueNo] + '</u><div class="popupButton">OK</div>');
			settingsData.current.pendingBonus[settingsData.current.clueNo] = 0;
			}
		}

	function countUp(endScore, next)
		{
		var startScore = parseInt($score.text(), 10);

		var scoreCounter = setInterval(function()
			{
			touchDisabled = true;
			if (startScore <= endScore)
				{
				$score.text(startScore++);
				}
			else
				{
				clearInterval(scoreCounter);
				touchDisabled = false;
				if (next) { next(); }
				}
			}, 10);
		}

	// Show the next clue
	function showNextClue()
		{
		settingsData.current.clueNo++;
		if (settingsData.current.clueNo >= CLUES)
			{
			settingsData.current.questionNo++;

			if (currentQuestionIsBonus)
				{
				var newScore = parseInt($score.text(), 10);

				// add up all bonus scores
				for (var i = 0; i < settingsData.current.pendingBonus.length; i++)
					{
					newScore += settingsData.current.pendingBonus[i];
					}

				// show message indicating points earned in bonus round
				$message.text('You have earned ' + (newScore - parseInt($score.text(), 10)) + ' bonus pts!');
				$message.addClass('fadeInOutSlow');

				setTimeout(function()
					{
					$message.removeClass('fadeInOutSlow');

					// reset pending bonuses
					settingsData.current.pendingBonus.length = 0;
					settingsData.current.score = newScore;
					countUp(newScore, showNextQuestion);
					}, WAIT_FOR_MESSAGE);

				return;
				}

			showNextQuestion();
			return;
			}

        clearBonusLetters();
		resetGuess();
		saveSettings();
		GAME.sound.play('ding');
		showClue();

		if (currentQuestionIsBonus)
			{
			showBonusButtons();
			}
		else
			{
			adjustWorth(-CLUE_REDUCTION);
			}
		}

	// Show the next mystery word
	function showNextQuestion()
		{
		touchDisabled = true;
		resetToNormalQuestion();
		$clueBoard.addClass('hide');
		settingsData.current.clueNo = 0;
		settingsData.current.firstLettersRevealed = 0;
		if (settingsData.current.vowelRevealed) $('#' + settingsData.current.vowelRevealed).removeClass('disable');
		settingsData.current.vowelRevealed = '';
		settingsData.current.multiplier = 1;
		settingsData.current.allCluesRevealed = false;
		settingsData.current.lifeSaverPlayed = false;
		allowDelete = true;
		settingsData.current.guessWorth = 0;
		adjustWorth(MAX_SCORE);
		$multiplier.addClass('hide').removeClass('triple');
		$doubleScore.removeClass('dim');
		$tripleScore.removeClass('dim');
		$lifeSaver.removeClass('dim');
		$revealAllClues.removeClass('dim');
		$revealLetter.removeClass('dim');
		$revealVowel.removeClass('dim');

		// bonus question related nodes
		if (currentQuestionIsBonus)
			{
			$powerButton.removeClass('hide');
			$clear.removeClass('hide');
			$del.removeClass('hide');
			$worth.removeClass('hide');
			for (var i = 0; i < $bonus.length; i++)
				{
				$bonus[i][0].className = 'bonusPts hide';
				}
			}

		currentQuestionIsBonus = (BONUS_QUESTION.indexOf(settingsData.current.questionNo) != -1);

		setTimeout(function()
			{
			touchDisabled = false;
			checkGameOver();
			}, WAIT_FOR_CLUEBOARD);
		}

	// Check if the game is over; if so, save the cache
	function checkGameOver()
		{
		if (settingsData.current.questionNo >= gameData[gameNo].length)
			{
			gameOver = true;
			GAME.sound.play('winner');
			statsData.gamesPlayed++;
			statsData.totalPoints += settingsData.current.score;
			statsData.highScore = (settingsData.current.score > statsData.highScore) ? settingsData.current.score : statsData.highScore;
			saveStats();
			checkAchievements();
			showPopup('<h1>Game Over</h1>Your final score is ' + settingsData.current.score + '<div class="popupButton">OK</div>');
			displayStats();
			resetGame();
			return;
			}

		// not the end of game; show next question or "bonus" question
        clearBonusLetters();
		saveSettings();
		if (currentQuestionIsBonus)
			{
			showBonusQuestion();
			}
		else
			{
			unhideClueBoard();
			}
		}

    function clearBonusLetters()
        {
        if (settingsData.current.bonusLettersRevealed !== undefined && settingsData.current.bonusLettersRevealed !== null)
            {
            settingsData.current.bonusLettersRevealed.length = 0;
            }
        }

	// Show the updated Clue Board
	function unhideClueBoard()
		{
		currentQuestion = gameData[gameNo][settingsData.current.questionNo];
		$clueCover[0].className = 'clueCover';

		// re-enable the Power Button
		powerButtonDisabled = false;

		// iterate over clues
		for (var i = 0; i < $clue.length; i++)
			{
			populateClue(i, false);
			}

		if (settingsData.current.multiplier > 1)
			{
			$multiplier.text(settingsData.current.multiplier);
			$multiplier.removeClass('hide');
			}
		if (settingsData.current.vowelRevealed)
			{
			$('#' + settingsData.current.vowelRevealed).addClass('disable');
			}
		$dsLeft.text(settingsData.current.doubleScoresLeft);
		$tsLeft.text(settingsData.current.tripleScoresLeft);
		$tsBanked.text(settingsData.tripleScoresBanked);
		$lsBanked.text(settingsData.lifeSaversBanked);
		$raLeft.text(settingsData.current.revealAllLeft);

		var qNo = parseInt(settingsData.current.questionNo, 10) + 1;
		$question.text(qNo + '/' + gameData[gameNo].length);

		$popupCover.addClass('show');
		$message.text('Round ' + qNo);
		$answerLength.text(currentQuestion.answer.length);
		resetGuess();
		$message.addClass('fadeInOut');

		setTimeout(function()
			{
			$message.removeClass('fadeInOut');
			$message2.text(currentQuestion.answer.length + ' letters');
			$message2.addClass('fadeInOut');
			}, WAIT_FOR_MESSAGE);

		setTimeout(function()
			{
			$message2.removeClass('fadeInOut');
			$clueBoard.removeClass('hide');
			}, WAIT_FOR_MESSAGE * 2);

		setTimeout(function()
			{
			showClue();
			GAME.sound.play('ding');
			for (i = 0; i <= settingsData.current.clueNo; i++)
				{
				$popupCover.removeClass('show');
				}
			touchDisabled = false;
			if (settingsData.firstTimePlaying)
				{
				settingsOpened = false;
				toggleSettingsMenu();
				showHelp();
				settingsData.firstTimePlaying = false;
				saveSettings();
				}
			}, WAIT_FOR_MESSAGE * 2 + WAIT_FOR_CLUEBOARD);
		}

	// Show the "bonus" question - player must guess the clues instead of the answer
	function showBonusQuestion()
		{
		var bonusPts,
			startingClue = parseInt(settingsData.current.clueNo, 10);

		// Clear guess area
		$guess.html('');

		// disable and hide the Power Button, as it does not apply to "bonus" questions
		powerButtonDisabled = true;
		$powerButton.addClass('hide');

		currentQuestion = gameData[gameNo][settingsData.current.questionNo];

		// iterate over clues
		for (var i = 0; i < $clue.length; i++)
			{
			populateClue(i, true);

			// show the correct word for clues that have already been played
			if (i < startingClue)
				{
				for (var c = 0; c < $clue[i][0].children.length; c++)
					{
					if ($clue[i][0].children[c].className === 'beginSpace'
						|| $clue[i][0].children[c].className === 'endSpace')
						{
						for (var j = 0; j < bonusClue[i].length; j++)
							{
							$clue[i][0].children[c].children[j].innerText = bonusClue[i][j];
							}
						break;
						}
					}
				bonusPts = parseInt(settingsData.current.pendingBonus[i], 10);
				populateBonus(i, bonusPts);
				}
			}

		// if the user has already selected a bonus value for the current clue
		if (settingsData.current.pendingBonus.length > settingsData.current.clueNo)
			{
			populateBonus(settingsData.current.clueNo, parseInt(settingsData.current.pendingBonus[settingsData.current.clueNo], 10));
			}

		var qNo = parseInt(settingsData.current.questionNo, 10) + 1;
		$question.text(qNo + ' / ' + gameData[gameNo].length);

		// reveal all clues
		$clueCover[0].className = 'clueCover clue' + CLUES;

		guessedWord = '';
		$popupCover.addClass('show');
		$message.text('BONUS ROUND!');
		showBonusClue(bonusClue[startingClue]);
		$message.addClass('fadeInOut');

		setTimeout(function()
			{
			$message.removeClass('fadeInOut');
			$message2.text('Guess all 5 clues');
			$message2.addClass('fadeInOut');
			}, WAIT_FOR_MESSAGE);

		setTimeout(function()
			{
			$message2.removeClass('fadeInOut');
			$clueBoard.removeClass('hide');
			}, WAIT_FOR_MESSAGE * 2);

		setTimeout(function()
			{
			// reveal the proper clue
			showClue();
			GAME.sound.play('ding');
			for (i = 0; i <= settingsData.current.clueNo; i++)
				{
				$popupCover.removeClass('show');
				}
			}, WAIT_FOR_MESSAGE * 2 + WAIT_FOR_CLUEBOARD);

		setTimeout(function()
			{
			for (var i = 0; i < settingsData.current.clueNo; i++)
				{
				$bonus[i].removeClass('hide');
				}
			touchDisabled = false;

			// if the user has already selected a bonus value for the current clue
			if (settingsData.current.pendingBonus.length > settingsData.current.clueNo)
				{
				$bonus[settingsData.current.clueNo].addClass('unanswered').removeClass('hide');
				return;
				}

			showBonusButtons();
			}, WAIT_FOR_MESSAGE * 2 + WAIT_FOR_CLUEBOARD * 2);
		}

	// For bonus questions - show the bonus buttons
	function showBonusButtons()
		{
        var clue = bonusClue[settingsData.current.clueNo];
		settingsData.current.bonusLettersSelected = Math.ceil((clue.length / 2) - BONUS_LETTERS.LOW);
		settingsData.current.bonusLettersSelected = (settingsData.current.bonusLettersSelected > 0) ? settingsData.current.bonusLettersSelected : 0;
		var lettersRevealed = BONUS_LETTERS.LOW + parseInt(settingsData.current.bonusLettersSelected, 10);
        var showLow = clue.length > lettersRevealed;
		var lettersText = (lettersRevealed > 1) ? 'Letters' : 'Letter';
		$buttonDescriptionLow.text('Reveal ' + lettersRevealed + ' ' + lettersText);

		lettersRevealed = BONUS_LETTERS.MEDIUM + parseInt(settingsData.current.bonusLettersSelected, 10);
        var showMedium = clue.length > lettersRevealed;
		lettersText = (lettersRevealed > 1) ? 'Letters' : 'Letter';
		$buttonDescriptionMedium.text('Reveal ' + lettersRevealed + ' ' + lettersText);

		lettersRevealed = BONUS_LETTERS.HIGH + parseInt(settingsData.current.bonusLettersSelected, 10);
		lettersText = (lettersRevealed > 1) ? 'Letters' : 'Letter';
		$buttonDescriptionHigh.text('Reveal ' + lettersRevealed + ' ' + lettersText);

		$letters.removeClass('show').addClass('hide');
		$powerButton.addClass('hide');
		$clear.addClass('hide');
		$del.addClass('hide');
		$worth.addClass('hide');

		$guessButtons.removeClass('hide');
        if (showMedium)
            {
            $guessButtonMedium.removeClass('hide');
            $buttonDescriptionMedium.removeClass('hide');
            }
        if (showLow)
            {
            $guessButtonLow.removeClass('hide');
            $buttonDescriptionLow.removeClass('hide');
            }
		$guessButtonHigh.removeClass('hide');
		$buttonDescriptionHigh.removeClass('hide');
		}

	// Helper function for populating clues
	function populateClue(clueToPopulate, bonusQuestion)
		{
		var htmlCode = '',
			clue = currentQuestion.clues[clueToPopulate].split('%');

		// iterate over the clue's split array
		for (var i = 0; i < clue.length; i++)
			{
			if (clue[i] === '')
				{
				if (bonusQuestion)
					{
					htmlCode += (i > 0) ? '<div class="shownWord right">' : '<div class="shownWord left">';
					htmlCode += currentQuestion.answer;
					htmlCode += '</div>';
					}
				else
					{
					htmlCode += (i > 0) ? '<div class="beginSpace">' : '<div class="endSpace">';
					htmlCode += showBlanks(currentQuestion.answer);
					htmlCode += '</div>';
					}
				}
			else
				{
				if (bonusQuestion)
					{
					bonusClue[clueToPopulate] = clue[i];
					htmlCode += (i > 0) ? '<div class="beginSpace">' : '<div class="endSpace">';
					if (clueToPopulate !== settingsData.current.clueNo)
						{
						htmlCode += showBlanks(clue[i], true);
						}
					else
						{
						htmlCode += showBlanks(clue[i]);
						}
					htmlCode += '</div>';
					}
				else
					{
					htmlCode += (i > 0) ? '<div class="shownWord right">' : '<div class="shownWord left">';
					htmlCode += clue[i];
					htmlCode += '</div>';
					}
				}
			}

		// show the clue
		$clue[clueToPopulate].html(htmlCode);
		}

	// Helper function for populating bonuses in the DOM
	function populateBonus(i, bonusPts)
		{
		var bonusClass;

		$bonus[i][0].className = 'bonusPts hide';

		switch (bonusPts)
			{
			case BONUS_SCORE.LOW:
				bonusClass = 'low';
				break;
			case BONUS_SCORE.MEDIUM:
				bonusClass = 'medium';
				break;
			case BONUS_SCORE.HIGH:
				bonusClass = 'high';
				break;
			default:
				bonusClass = 'zero';
			}

		$bonus[i].addClass(bonusClass).text(bonusPts);
		}

	// Show the current clue in the #guess area
	function showBonusClue(clue)
		{
		$answerLength.text(clue.length);
		removeAllChildren($guess[0]);
		guessedWord = '';
		$guess.html(showBlanks(clue));
		}

	// Show the number of letters in the clue according to the button selected
	function bonusGuess(bonusPts, bonusLetters)
		{
		var revealedWord,
			randomNumbers = [];

		if (touchDisabled)
			{
			return;
			}

		// disable futher touches
		touchDisabled = true;

		// reveal random letters between the first and last letter
		randomNumbers = $.random(1, bonusClue[settingsData.current.clueNo].length - 2, settingsData.current.bonusLettersSelected + 1);
		settingsData.current.bonusLettersRevealed = randomNumbers;

		// reveal the first letter
		if (bonusLetters === BONUS_LETTERS.LOW)
			{
			settingsData.current.bonusLettersRevealed.push(0);
			}

		// reveal the last letter
		if (bonusLetters === BONUS_LETTERS.LOW || bonusLetters === BONUS_LETTERS.MEDIUM)
			{
			settingsData.current.bonusLettersRevealed.push(bonusClue[settingsData.current.clueNo].length - 1);
			}

		// show on screen
		populateBonus(settingsData.current.clueNo, bonusPts);
		revealedWord = showBlanks(bonusClue[settingsData.current.clueNo]);
		$guess.html(revealedWord);

		// fill in the correct word
		for (var c = 0; c < $clue[settingsData.current.clueNo][0].children.length; c++)
			{
			if ($clue[settingsData.current.clueNo][0].children[c].className === 'beginSpace'
				|| $clue[settingsData.current.clueNo][0].children[c].className === 'endSpace')
				{
				$clue[settingsData.current.clueNo][0].children[c].innerHTML = revealedWord;
				break;
				}
			}

		settingsData.current.pendingBonus[settingsData.current.clueNo] = bonusPts;
		saveSettings();
		$bonus[settingsData.current.clueNo].removeClass('hide').addClass('unanswered');
		$letters.removeClass('hide').addClass('show');
		$clear.removeClass('hide');
		$del.removeClass('hide');
		$guessButtons.addClass('hide');
		$guessButtonHigh.addClass('hide');
		$guessButtonMedium.addClass('hide');
		$guessButtonLow.addClass('hide');
		$buttonDescriptionHigh.addClass('hide');
		$buttonDescriptionMedium.addClass('hide');
		$buttonDescriptionLow.addClass('hide');

		// re-enable touch handling after animations have ended
		setTimeout(function()
			{
			touchDisabled = false;
			}, WAIT_FOR_GUESS_BUTTON_ANIMATION_TO_END);
		}

	// Toggle the User Settings Menu
	function toggleSettingsMenu()
		{
		// if settings are opened, close them
		if (settingsOpened)
			{
			$settingsButton.removeClass('selected');
			$settings.addClass('hide');
			settingsOpened = false;
			return;
			}

		// if settings are closed, open them
		$settingsButton.addClass('selected');
		$settings.removeClass('hide');
		settingsOpened = true;
		}

	// Apply User Settings
	function applyAllSettings()
		{
		GAME.sound.init(settingsData.soundOn);
		$game[0].style['background-image'] = 'url(images/' + settingsData.bg.current + '.jpg)';
		if (settingsData.darkOn)
			{
			$home.addClass('dark');
			$game.addClass('dark');
			}
		document.body.className += ' ' + settingsData.font.classes[settingsData.font.current];
		}

	// Toggle through a setting as user selects it
	function toggleSettings(targetID)
		{
		var previousClasses = [],
			tempClasses;

		switch(targetID)
			{
			case 'set_bg':
				settingsData.bg.current++;
				if (settingsData.bg.current > settingsData.bg.max) settingsData.bg.current = 1;
				$game[0].style['background-image'] = 'url(images/' + settingsData.bg.current + '.jpg)';
				break;

			case 'set_dark':
			case 'darkButton':
				if (settingsData.darkOn)
					{
					$home.removeClass('dark');
					$game.removeClass('dark');
					settingsData.darkOn = false;
					}
				else
					{
					$home.addClass('dark');
					$game.addClass('dark');
					settingsData.darkOn = true;
					}
				break;

			case 'set_font':
				previousClasses = settingsData.font.classes[settingsData.font.current];
				settingsData.font.current++;
				if (settingsData.font.current === settingsData.font.classes.length) settingsData.font.current = 0;
				tempClasses = document.body.className;
				document.body.className = tempClasses.replace(previousClasses, settingsData.font.classes[settingsData.font.current]);
				break;

			case 'set_sound':
				if (settingsData.soundOn)
					{
					$sound.addClass('off');
					settingsData.soundOn = false;
					GAME.sound.mute(true);
					}
				else
					{
					$sound.removeClass('off');
					settingsData.soundOn = true;
					GAME.sound.mute(false);
					}
				break;

			case 'set_help':
				showHelp();
				break;
			}

		saveSettings();
		}

	function showGameView()
		{
        console.log("showGameView");
		touchDisabled = true;
		loadGameData(false, function()
            {
            $game.removeClass('hide').removeClass('outToRight').addClass('inFromRight');
            $home.removeClass('inFromLeft').addClass('outToLeft');
            $worth.text(settingsData.current.guessWorth);
            $score.text(settingsData.current.score);

            currentQuestionIsBonus = (BONUS_QUESTION.indexOf(settingsData.current.questionNo) != -1);

            setTimeout(function()
                {
                if (currentQuestionIsBonus)
                    {
                    showBonusQuestion();
                    }
                else
                    {
                    unhideClueBoard();
                    }
                }, WAIT_FOR_ANIMATION_TO_END);
            });
		}

	// Slide the Game Screen out of the Viewport
	function showHomeView()
		{
		checkForPurchase();
		checkGameCenter();

		// Hide OK Button & undim letters
		$okButton.removeClass('show');
		$letters.removeClass('dim');

		$game.removeClass('hide').removeClass('inFromRight').addClass('outToRight');
		$home.removeClass('outToLeft').addClass('inFromLeft');

		$clueBoard.addClass('hide');
		gameOver = false;
		}

	// Show the message popup
	function showPopup(msg, next)
		{
        if (currentPopupNext)
            {
            currentPopupNext();
            }
        currentPopupNext = next;
		$popup.html(msg);
		$popup.addClass('show');
		$popupCover.addClass('show dim');

		setTimeout(function()
			{
			popupIsShown = true;
			}, WAIT_FOR_ANIMATION_TO_END);
		}

	// Hide the message popup
	function hidePopup()
		{
		$popup.removeClass('show');
		$popupCover.removeClass('show').removeClass('dim');
		popupIsShown = false;

		// New version always shown first
		if (settingsData.firstTimeVersion21)
			{
			settingsData.firstTimeVersion21 = false;
			saveSettings();
			resetGame();
			return;
			}

		// Thank you message
		if (thankYouMsg)
			{
			thankYouMsg = false;
			return;
			}

		// Easter egg
		if (gameTitleCount === 10)
			{
			gameTitleCount = 0;
			saveSettings();
			return;
			}

		// Achievement message shown
		if (achievementMsg.length > 0)
			{
			var msg = '<h1>Congrats!</h1>';
			msg += (achievementMsg.length > 1) ? 'Achievements unlocked:' : 'Achievement unlocked:';
			for (var i = 0; i < achievementMsg.length; i++)
				{
				msg += '<div class="achievement">' + achievementMsg[i] + '</div>';
				}
			achievementMsg.length = 0;
			showPopup(msg + '<div class="popupButton">OK</div>');
			return;
			}

		// Incorrect guess just occurred
		if (settingsData.current.score === parseInt($score.text(), 10))
			{
			afterGuess();
			return;
			}

		// Correct guess just occurred
		setTimeout(function()
			{
			countUp(settingsData.current.score, afterGuess);
			}, WAIT_FOR_ANIMATION_TO_END);
		}

	function afterGuess()
		{
		if (gameOver)
			{
			showHomeView();
			return;
			}

		// not the end of game
		showNextClue();
		}

	// Show Help Screen
	function showHelp()
		{
		// Help overlay is shown, so hide it
		if (currentHelpScreen >= HELP_SCREENS)
			{
			$help.addClass('hide');
			currentHelpScreen = 0;
			toggleSettingsMenu();
			for (var i = 0; i < HELP_SCREENS; i++)
				{
				$helpScreen[i][0].className = 'helpScreen';
				}
			// Hack -- have to reset the background image for some reason
			$game[0].style['background-image'] = 'url(images/' + settingsData.bg.current + '.jpg)';
			return;
			}

		// Reveal help
		if (currentHelpScreen === 0)
			{
			$game[0].style['background-image'] = 'none';
			$help.removeClass('hide');
			}
		else
			{
			$helpScreen[currentHelpScreen - 1].removeClass('show').addClass('hide');
			}

		// Show the next help screen
		$helpScreen[currentHelpScreen].removeClass('hide').addClass('show');
		currentHelpScreen++;
		}

	// Message for new version
	function newVersionMsg()
		{
		showPopup('<h1>New Version!</h1>Welcome to the newly updated Word Clues 2!'
					+ '<ul class="newVersionList">'
					+ '<li>Life Saver: if you get stuck after seeing all clues, we will bail you out by revealing all but one letter in the answer, while giving you a score of at least 50 pts!</li>'
					+ '<li>Triple Word Score: 3x your score, once per game!</li>'
					+ '<li>Achievements: accessed by touching "Leaders"</li>'
					+ '<li>Bonus Round: we give you the answer, you give us the clues!</li>'
					+ '</ul>'
					+ '<div class="popupButton">OK</div>');
		}

	// Show Power Options Popup
	function showPowerOptions()
		{
		$powerOptions.addClass('show');
		$popupCover.addClass('show dim');

		setTimeout(function()
			{
			popupIsShown = true;
			powerOptionsShown = true;
			}, WAIT_FOR_ANIMATION_TO_END);

		if (settingsData.current.tripleScoresLeft === 0 || settingsData.current.multiplier !== 1)
			{
			$tripleScore.addClass('dim');
			}

		if (settingsData.current.doubleScoresLeft === 0 || settingsData.current.multiplier !== 1)
			{
			$doubleScore.addClass('dim');
			}

		if (settingsData.current.revealAllLeft === 0 || settingsData.current.allCluesRevealed
			|| settingsData.current.lifeSaverPlayed)
			{
			$revealAllClues.addClass('dim');
			}

		if (settingsData.current.lifeSaverPlayed || settingsData.current.clueNo + 1 < CLUES)
			{
			$lifeSaver.addClass('dim');
			}

		if (settingsData.current.firstLettersRevealed > 0 || settingsData.current.lifeSaverPlayed)
			{
			$revealLetter.addClass('dim');
			}

		if (settingsData.current.vowelRevealed.length > 0 || settingsData.current.lifeSaverPlayed)
			{
			$revealVowel.addClass('dim');
			}
		}

	// Hide Power Options Popup
	function hidePowerOptions(next, params)
		{
		GAME.sound.play('lowClick');
		$powerOptions.removeClass('show');
		$popupCover.removeClass('show').removeClass('dim');
		popupIsShown = false;
		powerOptionsShown = false;

		if (next === undefined)
			{
			return;
			}

		setTimeout(function()
			{
			next(params);
			}, WAIT_FOR_ANIMATION_TO_END);
		}

	// User chose 'Reveal Vowel' Power Option
	function showVowelPopup()
		{
		$vowelSelect.addClass('show');
		$popupCover.addClass('show dim');

		setTimeout(function()
			{
			popupIsShown = true;
			vowelPopupShown = true;
			}, WAIT_FOR_ANIMATION_TO_END);
		}

	// Hide Vowel Popup
	function hideVowelPopup()
		{
		$vowelSelect.removeClass('show');
		$popupCover.removeClass('show').removeClass('dim');
		popupIsShown = false;
		vowelPopupShown = false;
		}

	// Adjust the worth of a correct answer
	function adjustWorth(adjustment)
		{
		settingsData.current.guessWorth += (adjustment) ? adjustment : 0;
		$worth.text(settingsData.current.guessWorth);
		}

	// User selected 'Reveal All Clues' Power Option
	function showAllClues()
		{
		resetGuess();
		settingsData.current.clueNo = CLUES - 1;
		settingsData.current.allCluesRevealed = true;
		saveSettings();
		GAME.sound.play('ding');
		showClue();
		}

	// User selected 'Life Saver' Power Option
	function playLifeSaver()
		{
		settingsData.current.lifeSaverPlayed = true;
		allowDelete = true;

		// if the guess worth is less than 50 pts, reset to 50 pts
		if (settingsData.current.guessWorth < 50)
			{
			settingsData.current.guessWorth = 50;
			$worth.text(settingsData.current.guessWorth);
			}

		// reveal all but one letter
		for (var i = currentQuestion.answer.length; i--;)
			{
			if (guessedWord[i] === ' ' || guessedWord[i] === '')
				{
				settingsData.current.firstLettersRevealed = i;
				break;
				}
			}

		settingsData.lifeSaversBanked--;
		$lsBanked.text(settingsData.lifeSaversBanked);
		saveSettings();
		resetGuess();
		}

	// Reveal an individual clue
	function showClue()
		{
		$clueCover[0].className = 'clueCover clue' + (parseInt(settingsData.current.clueNo, 10) + 1);
		if (settingsData.current.clueNo === CLUES)
			{
			$cluesLeft.text('');
			}
		else
			{
			$cluesLeft.text(CLUES - settingsData.current.clueNo - 1);
			}

		// if the clue is the last clue
		if (settingsData.current.clueNo + 1 >= CLUES)
			{
			$lifeSaver.removeClass('dim');
			}
		}

	// Load the Settings Cache
	function loadSettings(next)
		{
		GAME.bridge.request('get.appSettings', '',
			function(err, message)
				{
				if (err)
					{
					console.log(err);
                    settingsData = GAME.DEFAULT.settingsCache;
					}

                else if (typeof message === 'object' && !isEmptyObj(message))
					{
					settingsData = message;

					// Validate the object, making sure it has all fields (in case cache structure has changed across releases)
					for (var prop in GAME.DEFAULT.settingsCache)
						{
						copyProperty(prop, GAME.DEFAULT.settingsCache, settingsData);
						}
					}
				else
					{
                    console.log("Applying DEFAULT SETTINGS");
					settingsData = GAME.DEFAULT.settingsCache;
					}
                next();
				}
			);
		}

	// Load the Achievements Cache
	function loadAchievements(gamecenterAchievements)
		{
		GAME.bridge.request('get.achievements', '',
			function(err, message)
				{
				if (err)
					{
					console.log(err);
					return;
					}

				if (typeof message === 'object' && !isEmptyObj(message))
					{
					achievementsData = message;

					// Validate the object, making sure it has all fields (in case cache structure has changed across releases)
					for (var prop in GAME.DEFAULT.achievementsCache)
						{
						copyProperty(prop, GAME.DEFAULT.achievementsCache, achievementsData);
						}
					}
				else
					{
					achievementsData = cloneStructure(GAME.DEFAULT.achievementsCache);
					}

				syncAchievements(gamecenterAchievements);
				}
			);
		}

	function loadStats(gamecenterStats)
		{
		GAME.bridge.request('get.stats', '',
			function(err, message)
				{
				if (err)
					{
					console.log(err);
					return;
					}

				if (typeof message === 'object' && !isEmptyObj(message))
					{
					statsData = message;
					}
				else
					{
					statsData = cloneStructure(GAME.DEFAULT.statsCache);
					}

				syncStats(gamecenterStats);
				}
			);
		}

	// Synchronize the achievements in cache with the achievements on GameCenter
	function syncAchievements(gamecenterAchievements)
		{
		// Use the union of cache and Game Center
		for (var gcAchievement in gamecenterAchievements)
			{
			if (gamecenterAchievements[gcAchievement].value == 100)
				{
				achievementsData[gcAchievement] = { value: 100 };
				}
			}

		saveAchievements();
		}

	// Synchronize the stats in cache with the stats on GameCenter
	function syncStats(gamecenterStats)
		{
		// Always use Game Center as system of record unless number of games played on device is greater
		if (gamecenterStats !== undefined
			&& gamecenterStats !== null
			&& gamecenterStats.gamesPlayed !== undefined
			&& (statsData.gamesPlayed === undefined
				|| (statsData.gamesPlayed !== undefined
					&& gamecenterStats.gamesPlayed >= statsData.gamesPlayed)))
			{
			// replace cache stats with Game Center stats
			statsData = gamecenterStats;
			}

		saveStats();
		displayStats();
		}

	// Load the Game that is in cache
	function loadGameData(generate, next)
		{
gameData =
 [
     [
   {"answer":"show","clues":["puppet%","peep%","%room","%up","side%","%case","%place","dog%","%man"]},
   {"answer":"thought","clues":["%experiment","%leader","%up","%provoking","fore%","after%","%about","%process","%wave"]},
   {"answer":"hat","clues":["%stand","straw%","%box","%maker","%trick","%size","%rack","hard%","top%"]},
   {"answer":"glass","clues":["%cutter","hour%","%ceiling","%house","wine%","%works","parting%","%oven","%eye"]},
   {"answer":"on","clues":["%coming","hold%","%set","%look","%sale","hang%","%shore","head%","%going"]},
   {"answer":"moon","clues":["%light","%shine","%beam","%struck","%walk","harvest%","honey%","full%","half%"]},
   {"answer":"watch","clues":["anchor%","over%","%maker","%spring","%house","%tower","stop%","%man","%dog"]},
   {"answer":"note","clues":["%worthy","%paper","%book","%taker","bank%","%pad","whole%","quarter%","foot%"]},
   {"answer":"bull","clues":["pit%","%nose","%frog","%fight","%head","%whip","%doze","%dog","%finch"]},
   {"answer":"nail","clues":["%bed","toe%","%salon","hang%","%file","%head","%brush","%art","%gun"]}
     ],
     [
{"answer":"bear","clues":["honey%","sugar%","polar%","water%","bug%","%skin"]},
{"answer":"week","clues":["%ahead","work%","%end","%long","%day"]},
{"answer":"post","clues":["guide%","goal%","starting%","%haste","%modern","%dated","%man","%office","fence%","lamp%"]},
{"answer":"pressure","clues":["%cooker","%washer","air%","%treated","%sensor","low%","vapor%","water%","%tank"]},
{"answer":"carpet","clues":["%bombing","%snake","%rod","%beater","%bagger"]},
{"answer":"book","clues":["account%","%seller","%case","sketch%","telephone%","picture%","school%","hand%","check%","pass%"]},
{"answer":"book","clues":["spelling%","law%","%keeper","pocket%","stamp%","%shelf","%fair","%house","story%","guide%"]},
{"answer":"book","clues":["play%","prayer%","%shop","note%","cook%","text%","%worm","%mark","phrase%","bank%"]},
{"answer":"station","clues":["weather%","way%","space%","%agent","pump%","%master"]},
{"answer":"tax","clues":["%free","%payer","property%","%dodger","%man","%bracket"]}
     ]
 ];
next();



/*
		var request = (generate) ? 'generate.game' : 'get.game';
		GAME.bridge.request(request, '',
			function(err, message)
				{
				if (err)
					{
					console.log(err);
					return;
					}

				if (typeof message === 'object' && !isEmptyObj(message))
					{
					gameData = message;
					if (generate)
						{
						saveGameData();
						}
                    if (next)
                        {
                        next();
                        }
					}
				else
					{
					if (!generate)
						{
						loadGameData(true, next);
						}
					else
						{
                        console.log('ERROR. How it got here?');
						// should not be hit
						gameData = GAME.DEFAULT.gameCache;
						}
					}
				}
			);
*/
		}

	// Save settings to cache
	function saveSettings()
		{
		GAME.bridge.request('set.appSettings', settingsData);
		}

	// Save achievements to cache
	function saveAchievements()
		{
		GAME.bridge.request('set.achievements', achievementsData);
		}

	// Save game data to cache
	function saveGameData()
		{
		GAME.bridge.request('set.game', gameData);
		}

    function getTargetID(target)
        {
        var currentNode = target;
        try
            {
            while (currentNode && !currentNode.getAttribute('id'))
                {
                currentNode = currentNode.parentNode;
                }
            return currentNode ? currentNode.id : null;
            }
        catch (err)
            {
            return null;
            }
        }

	$(w).bind('load', onLoaded);

    return {
        onTouchEvent: onTouchEvent,
        routeTouchEvent: routeTouchEvent,
        isTouchDisabled: function() { return touchDisabled; },
        messages: { }
    };

    })(this);