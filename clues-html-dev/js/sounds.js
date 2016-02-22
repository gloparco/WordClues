/*global GAME, Howl */
GAME.sound = (function()
	{
	var effect,
		muted = false;

	function init(soundOn)
		{
//		if (soundOn !== undefined) muted = !soundOn;
//		effect = new Howl
//			({
//			urls: ['data/sounds.mp3'],
//			sprite:
//				{
//				click: [0, 155],
//				swoosh: [26, 415],
//				clickSwoosh: [0, 441],
//				ding: [700, 2610],
//				correct: [3310, 3300],
//				winner: [6610, 5062],
//				secret: [11672, 2342],
//				incorrect: [14014, 3381],
//				power: [17395, 2500],
//				keyClick: [20026, 100],
//				keyBack: [20026, 156],
//				lowClick: [20190, 1500]
//				}
//			});
		}

	function play(selectedSound)
		{
		if (!muted)
            {
            //effect.play(selectedSound);
            GAME.bridge.request('sound.' + selectedSound, '');
            }
		}

	function mute(muteSet)
		{
		muted = muteSet;
		}

	return {
		init: init,
		play: play,
		mute: mute
		};
	})();