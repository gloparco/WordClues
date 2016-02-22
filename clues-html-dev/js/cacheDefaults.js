/*global GAME */
GAME.DEFAULT = {};

GAME.DEFAULT.settingsCache =
	{
	soundOn: true,
	bg: { current: 1, max: 11, random: false },
	darkOn: false,
	font: { current: 0, classes: ['', 'fontOpenSansLight', 'fontAudiowide', 'fontKaushanScript', 'fontSalsa', 'fontFantasy'] },
	current:
		{
		score: 0,
		questionNo: 0,
		clueNo: 0,
		tripleScoresLeft: 1,
		doubleScoresLeft: 4,
		revealAllLeft: 2,
		multiplier: 1,
		firstLettersRevealed: 0,
		vowelRevealed: '',
		guessWorth: 100,
		allCluesRevealed: false,
		lifeSaverPlayed: false,
		bonusLettersSelected: 0,
		pendingBonus: [],
		bonusLettersRevealed: []
		},
	firstTimePlaying: true,
	firstTimeVersion21: true,
	tripleScoresBanked: 1,
	lifeSaversBanked: 1
	};

GAME.DEFAULT.achievementsCache =
	{
	WordClues500PointGame: { value: 0 },
	WordClues1000PointGame: { value: 0 },
	WordClues1500PointGame: { value: 0 },
	WordClues2000PointGame: { value: 0 },
	WordCluesPerfectGame: { value: 0 },
	WordClues10GamesPlayed: { value: 0 },
	WordClues50GamesPlayed: { value: 0 },
	WordClues100GamesPlayed: { value: 0 },
	WordClues150GamesPlayed: { value: 0 },
	WordClues200GamesPlayed: { value: 0 },
	WordClues10000TotalPoints: { value: 0 },
	WordClues25000TotalPoints: { value: 0 },
	WordClues50000TotalPoints: { value: 0 },
	WordClues75000TotalPoints: { value: 0 },
	WordClues100000TotalPoints: { value: 0 },
	WordClues250000TotalPoints: { value: 0 },
	WordCluesStreak10: { value: 0 },
	WordCluesStreak25: { value: 0 },
	WordCluesStreak50: { value: 0 },
	WordCluesStreak100: { value: 0 },
	WordCluesStreak200: { value: 0 }
	};

GAME.DEFAULT.statsCache =
	{
	playerID: '',
    gamesPlayed: 0,
    totalPoints: 0,
    avgPoints: 0,
    highScore: 0,
    longestStreak: 0,
    currentStreak: 0
	};

//GAME.DEFAULT.gameCache =
//	[
//		[
//    {"answer":"show","clues":["puppet%","peep%","%room","%up","side%","%case","%place","dog%","%man"]},
//    {"answer":"thought","clues":["%experiment","%leader","%up","%provoking","fore%","after%","%about","%process","%wave"]},
//    {"answer":"hat","clues":["%stand","straw%","%box","%maker","%trick","%size","%rack","hard%","top%"]},
//    {"answer":"glass","clues":["%cutter","hour%","%ceiling","%house","wine%","%works","parting%","%oven","%eye"]},
//    {"answer":"on","clues":["%coming","hold%","%set","%look","%sale","hang%","%shore","head%","%going"]},
//    {"answer":"moon","clues":["%light","%shine","%beam","%struck","%walk","harvest%","honey%","full%","half%"]},
//    {"answer":"watch","clues":["anchor%","over%","%maker","%spring","%house","%tower","stop%","%man","%dog"]},
//    {"answer":"note","clues":["%worthy","%paper","%book","%taker","bank%","%pad","whole%","quarter%","foot%"]},
//    {"answer":"bull","clues":["pit%","%nose","%frog","%fight","%head","%whip","%doze","%dog","%finch"]},
//    {"answer":"nail","clues":["%bed","toe%","%salon","hang%","%file","%head","%brush","%art","%gun"]}
//		],
//		[
//{"answer":"bear","clues":["honey%","sugar%","polar%","water%","bug%","%skin"]},
//{"answer":"week","clues":["%ahead","work%","%end","%long","%day"]},
//{"answer":"post","clues":["guide%","goal%","starting%","%haste","%modern","%dated","%man","%office","fence%","lamp%"]},
//{"answer":"pressure","clues":["%cooker","%washer","air%","%treated","%sensor","low%","vapor%","water%","%tank"]},
//{"answer":"carpet","clues":["%bombing","%snake","%rod","%beater","%bagger"]},
//{"answer":"book","clues":["account%","%seller","%case","sketch%","telephone%","picture%","school%","hand%","check%","pass%"]},
//{"answer":"book","clues":["spelling%","law%","%keeper","pocket%","stamp%","%shelf","%fair","%house","story%","guide%"]},
//{"answer":"book","clues":["play%","prayer%","%shop","note%","cook%","text%","%worm","%mark","phrase%","bank%"]},
//{"answer":"station","clues":["weather%","way%","space%","%agent","pump%","%master"]},
//{"answer":"tax","clues":["%free","%payer","property%","%dodger","%man","%bracket"]}
//		],
//		[
//{"answer":"tea","clues":["%service","%set","%garden","%room","%house","%party","%spoon","%leaf"]},
//{"answer":"proof","clues":["sound%","%read","water%","%sheet","%positive","rust%","bomb%","bullet%","%text","burglar%"]},
//{"answer":"man","clues":["bonds%","music%","straw%","watch%","straight%","mad%","space%","iron%","old%","%child"]},
//{"answer":"man","clues":["weather%","post%","lay%","bar%","news%","middle%","cave%","%power","hang%","%made"]},
//{"answer":"man","clues":["%handle","quarry%","fore%","hench%","noble%","show%","handy%","tax%","%hole","sand%"]},
//{"answer":"man","clues":["%kind","chair%","bell%","work%","tin%","snow%","mail%","minute%","patrol%"]},
//{"answer":"bean","clues":["vanilla%","%curd","jelly%","%pole","string%","navy%","%mill","%meal","%stalk","carob%"]},
//{"answer":"hole","clues":["worm%","touch%","rat%","black%","peep%","spout%","water%","button%","mouse%","pot%"]},
//{"answer":"hole","clues":["key%","blow%","man%","sink%","pin%","swimming%","pigeon%","loop%"]},
//{"answer":"fire","clues":["%ant","%door","gun%","cross%","%extinguisher","%engine","%box","%proof","set%","wild%"]}
//		],
//		[
//{"answer":"hold","clues":["up%","anchor%","%back","foot%","%fast","hand%","%on","strong%","%down","%hands"]},
//{"answer":"air","clues":["%blast","%port","%pressure","%gun","%balloon","hot%","%trap","%drum","%brake","%drill"]},
//{"answer":"air","clues":["%compressor","%filter","%tight","open%","%bag","%brush","%bed","%dried"]},
//{"answer":"mad","clues":["%man","%hatter","%world","dog%","%house","%cap"]},
//{"answer":"cut","clues":["%throat","under%","clean%","%off","clear%","hair%","rough%","%out"]},
//{"answer":"surface","clues":["%temperature","%wave","%area","%tension","%water"]},
//{"answer":"knock","clues":["%knees","%down","%around","%out","%off"]},
//{"answer":"mate","clues":["room%","school%","house%","class%","check%","cabin%","ship%","first%","play%"]},
//{"answer":"cup","clues":["coffee%","eye%","butter%","tin%","suction%"]},
//{"answer":"house","clues":["coffee%","%music","straw%","gate%","%mate","watch%","mad%","state%","summer%","glass%"]}
//		],
//		[
//{"answer":"house","clues":["%boat","jail%","toll%","%hold","pump%","out%","tree%","%arrest","school%","%hunter"]},
//{"answer":"house","clues":["meeting%","%wife","book%","poor%","church%","farm%","smoke%","town%","ware%","guard%"]},
//{"answer":"house","clues":["dog%","field%","tea%","bath%","light%","round%"]},
//{"answer":"grass","clues":["%bar","%hopper","%land","%seed","crab%","blue%","lemon%","%skirt"]},
//{"answer":"fish","clues":["%hook","king%","%tail","puff%","jelly%","%food","cat%","%net","shell%","star%"]},
//{"answer":"fish","clues":["sun%","sucker%","%bait","surgeon%","%story","sword%","%line","razor%","archer%"]},
//{"answer":"trap","clues":["animal%","sand%","air%","fox%","spring%","mouse%","sewer%","booby%","%door"]},
//{"answer":"stand","clues":["%pipe","%up","under%","news%","display%","music%","witness%","hat%","%strong","%down"]},
//{"answer":"straw","clues":["%bed","%man","%ride","%poll","%hat","%house"]},
//{"answer":"home","clues":["%coming","smart%","%land","%run","%made","%brew","%alone","%sick","%stead","log%"]}
//		],
//		[
//{"answer":"sure","clues":["%hands","%thing","%fire","%enough","%footed"]},
//{"answer":"ink","clues":["%blot","%bottle","%pad","%jet","%well"]},
//{"answer":"ring","clues":["anchor%","%finger","nose%","oil%","toe%","thumb%","key%","%worm","slip%","wedding%"]},
//{"answer":"space","clues":["%station","%suit","%out","%ship","%man","outer%"]},
//{"answer":"class","clues":["%mate","upper%","first%","%room","middle%","second%"]},
//{"answer":"stock","clues":["anchor%","%market","over%","%car","%exchange","laughing%","%broker","%room","%yard"]},
//{"answer":"field","clues":["%hockey","battle%","%marshal","corn%","%house","%mouse","rice%","ball%","left%","right%"]},
//{"answer":"jail","clues":["%time","%break","%bait","%bird","%house"]},
//{"answer":"lock","clues":["%pick","%up","door%","%smith","%jaw","pad%","%box","combination%","vapor%","safety%"]},
//{"answer":"lock","clues":["%step","%down","leg%","%out","safe%","dead%"]}
//		],
//		[
//{"answer":"back","clues":["%stroke","%wash","%lash","hump%","step%","bare%","%log","camel%","green%","spring%"]},
//{"answer":"back","clues":["%end","%splash","%stairs","hold%","touch%","sling%","%stop","%rent","out%","hunch%"]},
//{"answer":"back","clues":["quarter%","strike%","%door","%bone","seat%","switch%","%hand","paper%","chair%","%room"]},
//{"answer":"back","clues":["%track","%ground","%side","setting%","pull%"]},
//{"answer":"fruit","clues":["%stand","%cake","%fly","grape%","passion%","%tree","%store"]},
//{"answer":"ill","clues":["%defined","%deed","%will","%wishes","%advised"]},
//{"answer":"river","clues":["%bed","%side","%bank","%boat","%bottom"]},
//{"answer":"buck","clues":["%wheat","%saw","%eye","%shot","%tooth","%skin"]},
//{"answer":"warning","clues":["%label","%light","%shot","%bells","%sign"]},
//{"answer":"ankle","clues":["%reflex","%deep","%boot","%jack","%jerk","%bone"]}
//		],
//		[
//{"answer":"vice","clues":["%roy","%versa","%chairman","%president","%principal"]},
//{"answer":"round","clues":["%up","%down","%off","%robin","%house","quarter%","%about","%out"]},
//{"answer":"play","clues":["horse%","%bill","%book","%mate","%time","match%","trick%","%thing","%room","%actor"]},
//{"answer":"play","clues":["%maker","out%","%wright","end%","%ground","%house","sword%"]},
//{"answer":"hay","clues":["%fever","%market","%ride","%wire","%stack"]},
//{"answer":"roller","clues":["%shades","%coaster","%derby","%blade","paint%","%skate"]},
//{"answer":"paint","clues":["%roller","finger%","%remover","%can","%thinner","%brush","%ball","war%"]},
//{"answer":"hat","clues":["%stand","%box","%maker","%trick","%size","straw%","%rack","hard%","top%"]},
//{"answer":"dry","clues":["%dock","%rot","%goods","%socket","%cleaners"]},
//{"answer":"waist","clues":["%size","%deep","%high","%band","%line"]}
//		],
//		[
//{"answer":"jelly","clues":["%fish","%bag","%roll","%bean","%belly"]},
//{"answer":"road","clues":["%runner","cross%","%work","high%","%house","rail%","%way","%side"]},
//{"answer":"head","clues":["%fast","barrel%","%band","%hunter","%way","hogs%","%case","mast%","nail%","pin%"]},
//{"answer":"head","clues":["bulk%","mop%","red%","hammer%","hot%","letter%","spear%","white%","%long","%ache"]},
//{"answer":"head","clues":["fore%","%master","bull%","%dress","%quarters","%board","arrow%","%on","%start","%stone"]},
//{"answer":"show","clues":["puppet%","peep%","%room","%up","side%","%case","%place","dog%","%man"]},
//{"answer":"glass","clues":["%cutter","hour%","%ceiling","%house","wine%","%works","parting%","%oven","%eye"]},
//{"answer":"dress","clues":["%code","cross%","over%","wedding%","head%"]},
//{"answer":"shot","clues":["up%","anchor%","slap%","gun%","cannon%","trick%","hot%","blood%","pot%","sling%"]},
//{"answer":"yard","clues":["school%","court%","farm%","%stick","bone%","rail%","stock%","vine%","barn%","ship%"]}
//		],
//		[
//{"answer":"eye","clues":["%doctor","%flap","%cup","buck%","%drop","hawk%","cross%","%lash","%ball","lazy%"]},
//{"answer":"eye","clues":["%brow","eagle%","red%","%liner","%lid","bug%","blind%","bird%","%piece","%witness"]},
//{"answer":"stage","clues":["%left","%fright","%coach","%door","%hand"]},
//{"answer":"sweet","clues":["%smells","%bread","%pea","bitter%","%dreams","%heart","%victory","%nothings"]},
//{"answer":"shop","clues":["coffee%","repair%","%keeper","smoke%","book%","sweat%","work%","%lift","pawn%","china%"]},
//{"answer":"place","clues":["burial%","common%","market%","%holder","birth%","resting%","show%","meeting%"]},
//{"answer":"gold","clues":["%digger","%bar","%foil","%bullion","%dust","%mine","%leaf"]},
//{"answer":"stop","clues":["%motion","%over","%sign","%order","short%","back%","%watch","rest%","%gap","door%"]},
//{"answer":"rock","clues":["bed%","%lobster","%crab","%climbing","hard%","%salt","%candy"]},
//{"answer":"toll","clues":["%collector","%bar","%gate","%booth","%house"]}
//		],
//		[
//{"answer":"iron","clues":["angle%","scrap%","waffle%","%age","cast%","%clad","%fist","%lung","wrought%","branding%"]},
//{"answer":"first","clues":["%mate","%rate","%bloom","%class","%hand"]},
//{"answer":"rest","clues":["arm%","steady%","leg%","%stop","quarter%","foot%","wrist%"]},
//{"answer":"second","clues":["%hand","%rate","%class","%fiddle","%chance"]},
//{"answer":"morning","clues":["%glory","%star","good%","%sickness","%gown"]},
//{"answer":"anchor","clues":["%ice","%bolt","%lining","%ring","%shot","%well","%lift","%rocket","%stock","%plate"]},
//{"answer":"anchor","clues":["%gate","%ball","%watch","%shackle","%drag","%hold"]},
//{"answer":"rough","clues":["%draft","%stuff","%edges","%cut","%riders"]},
//{"answer":"point","clues":["flash%","mid%","boiling%","starting%","sticking%","match%","landing%","melting%","%blank","strong%"]},
//{"answer":"shoe","clues":["brake%","%string","%polish","snow%","%pad","%horn","%lace","horse%"]}
//		],
//		[
//{"answer":"treasure","clues":["%hunt","sunken%","buried%","%chest","%trove","hidden%"]},
//{"answer":"sun","clues":["%stroke","%ray","%fish","%dial","%burn","%light","%shine","%flower","setting%","%bathe"]},
//{"answer":"sun","clues":["%rise","%set","%down","%burst","%spot"]},
//{"answer":"tree","clues":["fruit%","%frog","pine%","citrus%","%house","%hugger","%top","olive%","%nut","%trunk"]},
//{"answer":"tree","clues":["peach%","palm%","%wax","cherry%","apple%","boot%","%line","crab%","pear%"]},
//{"answer":"skin","clues":["buck%","pig%","potato%","%graft","wolf%","%tag","bear%","%deep","%flint","%tight"]},
//{"answer":"nerve","clues":["%damage","%gas","%wracking","%ending","%center"]},
//{"answer":"juice","clues":["carrot%","grape%","orange%","apple%","lemon%"]},
//{"answer":"jet","clues":["%pack","%black","%plane","%lag","%ski","ink%"]},
//{"answer":"well","clues":["anchor%","%being","%wisher","oil%","%acquainted","wishing%","%found","%done","gas%","ink%"]}
//		]
//	];
