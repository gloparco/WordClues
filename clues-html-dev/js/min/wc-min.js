var GAME={};GAME.game=function(e){function r(){$(e).unbind("load",r),a(),n(),hr(s)}function s(){z(),l(),G(),S(),Hammer(document.body).on("tap",function(e){t(e)})}function t(e){pt||o(e)}function o(e){var r,s=Sr(e.target);switch(s){case"infoButton":GAME.sound.play("click"),tt=!0,Qr.addClass("show"),js.addClass("show dim");break;case"infoPopup":GAME.sound.play("lowClick"),b();break;case"rateApp":GAME.sound.play("lowClick"),GAME.bridge.request("link.store"),b();break;case"feedback":GAME.sound.play("lowClick"),GAME.bridge.request("link.email"),b();break;case"kgTwitter":GAME.sound.play("lowClick"),GAME.bridge.request("link.twitter"),b();break;case"kgFacebook":GAME.sound.play("lowClick"),GAME.bridge.request("link.facebook"),b();break;case"facebook":GAME.sound.play("click"),r={message:"I just scored "+Js.current.score+" playing Word Clues, a fun, new word game from @KindredGames! You can play too: "+this.messages.linkStore+" #wordclues"},GAME.bridge.request("post.facebook",r,function(e,r){(null!==e||r&&!r.ok)&&alert("You are not currently signed in to Facebook.  To do so, go to "+this.messages.settingsFacebook)});break;case"twitter":GAME.sound.play("click"),r={message:"I just scored "+Js.current.score+" playing Word Clues, a fun, new word game from @KindredGames! You can play too: "+this.messages.linkStore+" #wordclues"},GAME.bridge.request("post.twitter",r,function(e,r){(null!==e||r&&!r.ok)&&alert("You are not currently signed in to Twitter.  To do so, go to "+this.messages.settingsTwitter)});break;case"leaderboardButton":GAME.sound.play("click"),GAME.bridge.request("display.gamecenter");break;case"buy":GAME.sound.play("click"),GAME.bridge.request("store.buy",{productId:"WordCluesUnlimitedGames"}),m();break;case"restoreLink":GAME.sound.play("click"),m(),g();break;case"restoreButton":GAME.sound.play("click"),GAME.bridge.request("store.restore",{productId:"WordCluesUnlimitedGames"}),k();break;case"tripleWordBuyButton":GAME.sound.play("click"),GAME.bridge.request("store.buy",{productId:"WordCluesTripleWordScore"}),m();break;case"lifeSaverBuyButton":GAME.sound.play("click"),GAME.bridge.request("store.buy",{productId:"WordCluesLifeSaver"}),m();break;case"iapPopup":case"iapTripleWordScorePopup":case"iapLifeSaverPopup":GAME.sound.play("lowClick"),m();break;case"restorePopup":GAME.sound.play("lowClick"),k();break;case"playButton":GAME.sound.play("click"),Ct=0,it||kt>0?J():f(Vr);break;case"okButton":GAME.sound.play("click"),is.removeClass("show"),ht?x():T();break;case"clearPass":GAME.sound.play("click"),dt?ht?x():T():c();break;case"guessButtonHigh":GAME.sound.play("click"),Y(Ar.HIGH,Er.HIGH);break;case"guessButtonMedium":GAME.sound.play("click"),Y(Ar.MEDIUM,Er.MEDIUM);break;case"guessButtonLow":GAME.sound.play("click"),Y(Ar.LOW,Er.LOW);break;case"a":case"b":case"c":case"d":case"e":case"f":case"g":case"h":case"i":case"j":case"k":case"l":case"m":case"n":case"o":case"p":case"q":case"r":case"s":case"t":case"u":case"v":case"w":case"x":case"y":case"z":p(s),vt=!0;break;case"del":(!Js.current.lifeSaverPlayed||Js.current.lifeSaverPlayed&&vt)&&(v(),vt=!1);break;case"powerButton":if(lt)return;GAME.sound.play("click"),or();break;case"powerOptions":ar();break;case"revealVowel":if(GAME.sound.play("click"),Js.current.vowelRevealed.length>0||Js.current.lifeSaverPlayed)return;ar(nr);break;case"revealLetter":if(Js.current.firstLettersRevealed>0||Js.current.lifeSaverPlayed)return void GAME.sound.play("click");GAME.sound.play("power"),ur(-Wr),Js.current.firstLettersRevealed++,kr(),ar(c);break;case"revealAllClues":if(0===Js.current.revealAllLeft||Js.current.allCluesRevealed||Js.current.lifeSaverPlayed)return void GAME.sound.play("click");GAME.sound.play("power"),Js.current.revealAllLeft--,Ls.text(Js.current.revealAllLeft),ar(cr);break;case"tripleScore":if(0===Js.tripleScoresBanked||0===Js.current.tripleScoresLeft||1!==Js.current.multiplier)return GAME.sound.play("click"),void ar(f,zr);GAME.sound.play("power"),Js.current.tripleScoresLeft--,Js.tripleScoresBanked--,Js.current.multiplier=3,Ms.text(Js.current.tripleScoresLeft),Ps.text(Js.tripleScoresBanked),Ws.text(Js.current.multiplier),Ws.removeClass("hide").addClass("triple"),kr(),ar();break;case"doubleScore":if(0===Js.current.doubleScoresLeft||1!==Js.current.multiplier)return void GAME.sound.play("click");GAME.sound.play("power"),Js.current.doubleScoresLeft--,Js.current.multiplier=2,Es.text(Js.current.doubleScoresLeft),Ws.text(Js.current.multiplier),Ws.removeClass("hide"),kr(),ar();break;case"lifeSaver":if(0===Js.lifeSaversBanked)return GAME.sound.play("click"),void ar(f,Zr);if(Js.current.lifeSaverPlayed||Js.current.clueNo+1<yr)return void GAME.sound.play("click");GAME.sound.play("power"),ar(ir);break;case"buyTripleScores":GAME.sound.play("click"),ar(f,zr);break;case"buyLifeSavers":GAME.sound.play("click"),ar(f,Zr);break;case"vowelSelect":lr();break;case"vowel_a":case"vowel_e":case"vowel_i":case"vowel_o":case"vowel_u":GAME.sound.play("power"),ur(-Wr);var t=s.split("_")[1];Js.current.vowelRevealed=t,c(),bs.removeClass("show"),js.removeClass("show").removeClass("dim"),$("#"+Js.current.vowelRevealed).addClass("disable"),st=!1,kr();break;case"settingsButton":GAME.sound.play("clickSwoosh"),V();break;case"set_bg":case"set_dark":case"set_font":case"set_sound":case"set_help":case"darkButton":GAME.sound.play("click"),Z(s);break;case"help":case"helpScreen1":case"helpScreen2":case"helpScreen3":GAME.sound.play("keyBack"),sr();break;case"backButton":GAME.sound.play("click"),Q();break;case"popup":GAME.sound.play("click"),st&&er();break;case"popupCover":at&&ar(),nt&&lr(),tt&&b(),ot&&m();break;case"gameTitle":Ct++,10===Ct&&(Js.tripleScoresBanked+=5,Js.lifeSaversBanked+=5,X('<h1>Tsk-Tsk!</h1>David, do you really need this?  You have been given 5 Life Savers and 5 Triple Word Scores... use them wisely Buddy!<div class="popupButton">OK</div>'))}}function a(){window.onerror=function(e,r,s){"use strict";var t="Exception ["+e+"] at "+r+" on line "+s;try{return void GAME.bridge.logError(t)}catch(o){}console.log(t)}}function n(){Hr=$("#home"),_r=$("#stats_gp"),Dr=$("#stats_tp"),jr=$("#stats_ap"),Fr=$("#stats_hs"),Kr=$("#stats_ls"),Yr=$("#stats_cs"),Qr=$("#infoPopup"),Vr=$("#iapPopup"),zr=$("#iapTripleWordScorePopup"),Zr=$("#iapLifeSaverPopup"),Jr=$("#restorePopup"),Xr=$("#game"),es=$("#clueBoard"),Bs=$("#clueCover"),Ts=$("#cluesLeft"),rs=$("#guessButtons"),ss=$("#guessButtonHigh"),ts=$("#guessButtonMedium"),os=$("#guessButtonLow"),as=$("#buttonDescriptionHigh"),ns=$("#buttonDescriptionMedium"),ls=$("#buttonDescriptionLow"),us=$("#letters"),cs=$("#powerButton"),is=$("#okButton"),ds=$("#clearPass"),hs=$("#del"),ps=$("#powerOptions"),vs=$("#doubleScore"),fs=$("#tripleScore"),ms=$("#lifeSaver"),gs=$("#revealLetter"),ks=$("#revealVowel"),Cs=$("#revealAllClues"),bs=$("#vowelSelect"),Ss=$("#question"),ws=$("#worth"),ys=$("#answerLength"),Gs=$("#score"),As=$("#guess"),Es=$("#dsLeft"),Ms=$("#tsLeft"),Ps=$("#tsBanked"),Ns=$("#lsBanked"),Ls=$("#raLeft"),Ws=$("#multiplier"),Os=$("#set_sound"),Is=$("#help"),qs=$("#settings"),Us=$("#settingsButton"),Hs=$("#message"),_s=$("#message2"),Ds=$("#popup"),js=$("#popupCover"),Fs=$("#price"),Ks=$("#tripleWordPrice"),Ys=$("#lifeSaverPrice"),Vs=Vr,st=!1,at=!1,nt=!1,lt=!1,ut=!1,ct=0,it=!1,dt=!0,ht=!1,pt=!1,vt=!0,ft=!1;var e;for(e=0;wr>e;e++)Rs[e]=$("#helpScreen"+(e+1));for(e=0;yr>e;e++){var r=e+1;xs[e]=$("#clue"+r),$s[e]=$("#bonus"+r)}}function l(){GAME.bridge.init(C)}function u(){Js.current.score=0,Js.current.questionNo=0,Js.current.clueNo=0,Js.current.doubleScoresLeft=Pr,Js.current.tripleScoresLeft=Nr,Js.current.revealAllLeft=Br,Js.current.multiplier=1,Js.current.firstLettersRevealed=0,Js.current.vowelRevealed="",Js.current.guessWorth=Gr,Js.current.pendingBonus=[],Js.current.bonusLettersSelected=0,Es.text(Js.current.doubleScoresLeft),Ms.text(Js.current.tripleScoresLeft),Ps.text(Js.tripleScoresBanked),Ns.text(Js.lifeSaversBanked),Ls.text(Js.current.revealAllLeft),kr(),et=null,br()}function c(){var e="";e=ht?rt.clues[Js.current.clueNo].replace("%",""):rt.answer,As.html(h(e)),i(),d()}function i(){is.removeClass("show"),us.removeClass("dim"),ds.addClass("off"),ds.text("PASS"),dt=!0,hs.addClass("off")}function d(){if(ht)j(Js.current.clueNo,!0);else for(var e=0;e<xs.length;e++)j(e,!1)}function h(e,r){var s="";zs="";for(var t=0;t<e.length;t++)!ht&&(t<Js.current.firstLettersRevealed||rt.answer[t]===Js.current.vowelRevealed)?(s+='<div class="space revealed">'+e[t]+"</div>",zs+=e[t]):ht&&void 0===r&&-1!==Js.current.bonusLettersRevealed.indexOf(t)?(s+='<div class="space revealed">'+e[t]+"</div>",zs+=e[t]):(s+='<div class="space">&nbsp;</div>',zs+=" ");return s}function p(e){var r,s,t,o=zs.indexOf(" ");if(-1!==o&&e!==Js.current.vowelRevealed){if(GAME.sound.play("keyClick"),ds.removeClass("off"),ds.text("clear"),dt=!1,hs.removeClass("off"),As[0].children[o].innerText=e,ht){for(s=0;s<xs[Js.current.clueNo][0].children.length;s++)if("beginSpace"===xs[Js.current.clueNo][0].children[s].className||"endSpace"===xs[Js.current.clueNo][0].children[s].className)for(t=0;t<xs[Js.current.clueNo][0].children[s].children.length;t++)if(-1===xs[Js.current.clueNo][0].children[s].children[t].className.indexOf("revealed")&&"&nbsp;"===xs[Js.current.clueNo][0].children[s].children[t].innerHTML){xs[Js.current.clueNo][0].children[s].children[t].innerText=e;break}}else for(r=0;r<xs.length;r++)for(s=0;s<xs[r][0].children.length;s++)if("beginSpace"===xs[r][0].children[s].className||"endSpace"===xs[r][0].children[s].className)for(t=0;t<xs[r][0].children[s].children.length;t++)if(-1===xs[r][0].children[s].children[t].className.indexOf("revealed")&&"&nbsp;"===xs[r][0].children[s].children[t].innerHTML){xs[r][0].children[s].children[t].innerText=e;break}zs=zs.substr(0,o)+e+zs.substr(o+1),-1===zs.indexOf(" ")&&(is.addClass("show"),us.removeClass("show").addClass("dim"))}}function v(){var e,r,s=!1,t=!1;for(is.removeClass("show"),us.removeClass("dim"),e=zs.length;e--;)if(" "!==zs[e]&&(!ht&&zs[e]!==Js.current.vowelRevealed&&e!==Js.current.firstLettersRevealed-1||ht&&-1===Js.current.bonusLettersRevealed.indexOf(e))){if(s){t=!0;break}if(GAME.sound.play("keyBack"),zs=zs.substr(0,e)+" "+zs.substr(e+1),As[0].children[e].innerHTML="&nbsp;",ht)for(r=0;r<xs[Js.current.clueNo][0].children.length;r++)-1===xs[Js.current.clueNo][0].children[r].className.indexOf("shownWord")&&(xs[Js.current.clueNo][0].children[r].children[e].innerHTML="&nbsp;");else for(var o=0;o<xs.length;o++)for(r=0;r<xs[o][0].children.length;r++)-1===xs[o][0].children[r].className.indexOf("shownWord")&&(xs[o][0].children[r].children[e].innerHTML="&nbsp;");s=!0}t||(ds.addClass("off"),ds.text("PASS"),dt=!0,hs.addClass("off"))}function f(e){ot=!0,Vs=e,e.addClass("show"),js.addClass("show dim")}function m(){ot=!1,Vs.removeClass("show"),js.removeClass("show").removeClass("dim")}function g(){ot=!0,Jr.addClass("show"),js.addClass("show dim")}function k(){ot=!1,Jr.removeClass("show"),js.removeClass("show").removeClass("dim")}function C(e){if("store.paid"===e.topic)switch(ft=!0,e.productId){case"WordCluesUnlimitedGames":it=!0,X('<h1>Thank you!</h1>You have unlocked Unlimited Games. Thank you for your support!<div class="popupButton">OK</div>',function(){ft=!1});break;case"WordCluesTripleWordScore":Js.tripleScoresBanked+=10,Ps.text(Js.tripleScoresBanked),kr(),X('<h1>Thank you!</h1>You have banked 10 additional Triple Word Scores. Thank you for your support!<div class="popupButton">OK</div>');break;case"WordCluesLifeSaver":Js.lifeSaversBanked+=10,Ns.text(Js.lifeSaversBanked),kr(),X('<h1>Thank you!</h1>You have banked 10 additional Life Savers. Thank you for your support!<div class="popupButton">OK</div>')}else if("store.priced"===e.topic)switch(e.productId){case"WordCluesUnlimitedGames":Fs.text(e.price);break;case"WordCluesTripleWordScore":Ks.text(e.price);break;case"WordCluesLifeSaver":Ys.text(e.price)}else"network"===e.topic?"inet"===e.type&&e.on:"leaderboards"===e.topic?isEmptyObj(e.scores)||vr(e.scores):"achievements"===e.topic&&(isEmptyObj(e.achievements)||fr(e.achievements))}function b(){Qr.removeClass("show"),js.removeClass("show").removeClass("dim"),tt=!1}function S(){GAME.bridge.request("store.owns",{productId:"WordCluesUnlimitedGames"},function(e,r){if(e)console.log(e);else{var s=["WordCluesTripleWordScore","WordCluesLifeSaver"];w(r)?it=!0:(it=!1,s.push("WordCluesUnlimitedGames")),GAME.bridge.request("store.price",{productIds:s})}y()})}function w(e){return"object"==typeof e&&!isEmptyObj(e)&&"WordCluesUnlimitedGames"===e.productId&&e.quantity>0&&e.transactionId}function y(){it||(kt=Tr-intOrZero(Qs.gamesPlayed),0>kt&&(kt=0))}function G(){GAME.bridge.request("check.gamecenter","",function(e){return e?void console.log(e):(E(),void A())})}function A(){GAME.bridge.request("achievements.get","",function(e,r){return e?void console.log(e):void pr(r)})}function E(){GAME.bridge.request("leaderboards.get",["gamesPlayed","totalPoints","avgPoints","highScore","longestStreak","currentStreak"],function(e,r){return e?void console.log(e):void vr(r)})}function M(){_r.text(P(Qs.gamesPlayed)),Dr.text(P(Qs.totalPoints)),jr.text(P(Qs.avgPoints)),Fr.text(P(Qs.highScore)),Kr.text(P(Qs.longestStreak)),Yr.text(P(Qs.currentStreak)),Js.firstTimeVersion21&&tr()}function P(e){return null===e||void 0===e||isNaN(e)?0:e}function N(){var e=Qs.gamesPlayed>0?Math.round(Qs.totalPoints/Qs.gamesPlayed):0;GAME.bridge.request("set.stats",{gamesPlayed:Qs.gamesPlayed,totalPoints:Qs.totalPoints,avgPoints:e,highScore:Qs.highScore,longestStreak:Qs.longestStreak,currentStreak:Qs.currentStreak}),GAME.bridge.request("leaderboards.set",{gamesPlayed:Qs.gamesPlayed,totalPoints:Qs.totalPoints,avgPoints:e,highScore:Qs.highScore,longestStreak:Qs.longestStreak,currentStreak:Qs.currentStreak})}function L(){gt.length=0,W(Xs.WordClues500PointGame)&&Qs.highScore>=500&&(Xs.WordClues500PointGame=B(),Js.lifeSaversBanked++,gt.push("500 Point Game")),W(Xs.WordClues1000PointGame)&&Qs.highScore>=1e3&&(Xs.WordClues1000PointGame=B(),Js.lifeSaversBanked++,gt.push("1,000 Point Game")),W(Xs.WordClues1500PointGame)&&Qs.highScore>=1500&&(Xs.WordClues1500PointGame=B(),Js.lifeSaversBanked++,gt.push("1,500 Point Game")),W(Xs.WordClues2000PointGame)&&Qs.highScore>=2e3&&(Xs.WordClues2000PointGame=B(),Js.lifeSaversBanked++,gt.push("2,000 Point Game")),W(Xs.WordCluesPerfectGame)&&Qs.highScore>=$r&&(Xs.WordCluesPerfectGame=B(),Js.lifeSaversBanked++,Js.tripleScoresBanked++,gt.push("Perfect Game")),W(Xs.WordClues10GamesPlayed)&&Qs.gamesPlayed>=10&&(Xs.WordClues10GamesPlayed=B(),Js.tripleScoresBanked++,gt.push("10 Games Played")),W(Xs.WordClues50GamesPlayed)&&Qs.gamesPlayed>=50&&(Xs.WordClues50GamesPlayed=B(),Js.tripleScoresBanked++,gt.push("50 Games Played")),W(Xs.WordClues100GamesPlayed)&&Qs.gamesPlayed>=100&&(Xs.WordClues100GamesPlayed=B(),Js.tripleScoresBanked++,gt.push("100 Games Played")),W(Xs.WordClues150GamesPlayed)&&Qs.gamesPlayed>=150&&(Xs.WordClues150GamesPlayed=B(),Js.tripleScoresBanked++,gt.push("150 Games Played")),W(Xs.WordClues200GamesPlayed)&&Qs.gamesPlayed>=200&&(Xs.WordClues200GamesPlayed=B(),Js.tripleScoresBanked++,gt.push("200 Games Played")),W(Xs.WordClues10000TotalPoints)&&Qs.totalPoints>=1e4&&(Xs.WordClues10000TotalPoints=B(),Js.lifeSaversBanked++,gt.push("10,000 Total Points")),W(Xs.WordClues25000TotalPoints)&&Qs.totalPoints>=25e3&&(Xs.WordClues25000TotalPoints=B(),Js.lifeSaversBanked++,gt.push("25,000 Total Points")),W(Xs.WordClues50000TotalPoints)&&Qs.totalPoints>=5e4&&(Xs.WordClues50000TotalPoints=B(),Js.lifeSaversBanked++,gt.push("50,000 Total Points")),W(Xs.WordClues75000TotalPoints)&&Qs.totalPoints>=75e3&&(Xs.WordClues75000TotalPoints=B(),Js.lifeSaversBanked++,gt.push("75,000 Total Points")),W(Xs.WordClues100000TotalPoints)&&Qs.totalPoints>=1e5&&(Xs.WordClues100000TotalPoints=B(),Js.lifeSaversBanked++,gt.push("100,000 Total Points")),W(Xs.WordClues250000TotalPoints)&&Qs.totalPoints>=25e4&&(Xs.WordClues250000TotalPoints=B(),Js.lifeSaversBanked++,Js.tripleScoresBanked++,gt.push("250,000 Total Points")),W(Xs.WordCluesStreak10)&&Qs.longestStreak>=10&&(Xs.WordCluesStreak10=B(),gt.push("10 in a Row")),W(Xs.WordCluesStreak25)&&Qs.longestStreak>=25&&(Xs.WordCluesStreak25=B(),gt.push("25 in a Row")),W(Xs.WordCluesStreak50)&&Qs.longestStreak>=50&&(Xs.WordCluesStreak50=B(),Js.tripleScoresBanked++,gt.push("50 in a Row")),W(Xs.WordCluesStreak100)&&Qs.longestStreak>=100&&(Xs.WordCluesStreak100=B(),Js.tripleScoresBanked++,gt.push("100 in a Row")),W(Xs.WordCluesStreak200)&&Qs.longestStreak>=200&&(Xs.WordCluesStreak200=B(),Js.lifeSaversBanked++,Js.tripleScoresBanked++,gt.push("200 in a Row")),0!==gt.length&&(kr(),Cr(),GAME.bridge.request("achievements.set",Xs))}function W(e){return null===e||void 0===e||e.value<100}function B(){return{value:100}}function T(){zs===rt.answer?(GAME.sound.play("correct"),Qs.currentStreak++,Qs.longestStreak=Qs.currentStreak>Qs.longestStreak?Qs.currentStreak:Qs.longestStreak,N(),L(),X("<h1>Terrific!</h1>You correctly guessed <u>"+zs+"</u> and scored "+Js.current.guessWorth*Js.current.multiplier+' pts<div class="popupButton">OK</div>'),Js.current.score=parseInt(Js.current.score,10)+Js.current.guessWorth*Js.current.multiplier,Js.current.clueNo=yr):Js.current.clueNo+1===yr?(GAME.sound.play("incorrect"),Qs.currentStreak=0,N(),X("<h1>Sorry!</h1>No points this time... the word was <u>"+rt.answer+'</u><div class="popupButton">OK</div>'),Js.current.clueNo=yr):-1!==zs.indexOf(" ")?(GAME.sound.play("incorrect"),X('<h1>Too Bad!</h1>Not even a guess? Next time...<div class="popupButton">OK</div>')):(GAME.sound.play("incorrect"),X("<h1>Nope!</h1>Good try, but <u>"+zs+'</u> is not the answer<div class="popupButton">OK</div>'))}function x(){if(zs===mt[Js.current.clueNo])return GAME.sound.play("correct"),$s[Js.current.clueNo].removeClass("unanswered"),Qs.currentStreak++,Qs.longestStreak=Qs.currentStreak>Qs.longestStreak?Qs.currentStreak:Qs.longestStreak,N(),L(),void X("<h1>Terrific!</h1>You correctly guessed <u>"+zs+"</u> for "+Js.current.pendingBonus[Js.current.clueNo]+' bonus pts<div class="popupButton">OK</div>');GAME.sound.play("incorrect"),$s[Js.current.clueNo].text("0"),$s[Js.current.clueNo][0].className="bonusPts zero";for(var e=0;e<xs[Js.current.clueNo][0].children.length;e++){var r="";if("beginSpace"===xs[Js.current.clueNo][0].children[e].className||"endSpace"===xs[Js.current.clueNo][0].children[e].className){for(var s=0;s<mt[Js.current.clueNo].length;s++)r+='<div class="space">'+mt[Js.current.clueNo][s]+"</div>";xs[Js.current.clueNo][0].children[e].innerHTML=r;break}}Qs.currentStreak=0,N(),X("<h1>Sorry!</h1>No points this time... the word was <u>"+mt[Js.current.clueNo]+'</u><div class="popupButton">OK</div>'),Js.current.pendingBonus[Js.current.clueNo]=0}function O(e,r){var s=parseInt(Gs.text(),10),t=setInterval(function(){pt=!0,e>=s?Gs.text(s++):(clearInterval(t),pt=!1,r&&r())},10)}function I(){if(Js.current.clueNo++,Js.current.clueNo>=yr){if(Js.current.questionNo++,ht){for(var e=parseInt(Gs.text(),10),r=0;r<Js.current.pendingBonus.length;r++)e+=Js.current.pendingBonus[r];return Hs.text("You have earned "+(e-parseInt(Gs.text(),10))+" bonus pts!"),Hs.addClass("fadeInOutSlow"),void setTimeout(function(){Hs.removeClass("fadeInOutSlow"),Js.current.pendingBonus.length=0,Js.current.score=e,O(e,R)},Rr)}return void R()}U(),c(),kr(),GAME.sound.play("ding"),dr(),ht?D():ur(-Lr)}function R(){if(pt=!0,i(),es.addClass("hide"),Js.current.clueNo=0,Js.current.firstLettersRevealed=0,Js.current.vowelRevealed&&$("#"+Js.current.vowelRevealed).removeClass("disable"),Js.current.vowelRevealed="",Js.current.multiplier=1,Js.current.allCluesRevealed=!1,Js.current.lifeSaverPlayed=!1,vt=!0,Js.current.guessWorth=0,ur(Gr),Ws.addClass("hide").removeClass("triple"),vs.removeClass("dim"),fs.removeClass("dim"),ms.removeClass("dim"),Cs.removeClass("dim"),gs.removeClass("dim"),ks.removeClass("dim"),ht){cs.removeClass("hide"),ds.removeClass("hide"),hs.removeClass("hide"),ws.removeClass("hide");for(var e=0;e<$s.length;e++)$s[e][0].className="bonusPts hide"}ht=-1!=Mr.indexOf(Js.current.questionNo),setTimeout(function(){pt=!1,q()},qr)}function q(){return Js.current.questionNo>=et[Ur].length?(ut=!0,GAME.sound.play("winner"),Qs.gamesPlayed++,Qs.totalPoints+=Js.current.score,Qs.highScore=Js.current.score>Qs.highScore?Js.current.score:Qs.highScore,N(),L(),X("<h1>Game Over</h1>Your final score is "+Js.current.score+'<div class="popupButton">OK</div>'),M(),void u()):(U(),kr(),void(ht?_():H()))}function U(){void 0!==Js.current.bonusLettersRevealed&&null!==Js.current.bonusLettersRevealed&&(Js.current.bonusLettersRevealed.length=0)}function H(){rt=et[Ur][Js.current.questionNo],Bs[0].className="clueCover",lt=!1;for(var e=0;e<xs.length;e++)j(e,!1);Js.current.multiplier>1&&(Ws.text(Js.current.multiplier),Ws.removeClass("hide")),Js.current.vowelRevealed&&$("#"+Js.current.vowelRevealed).addClass("disable"),Es.text(Js.current.doubleScoresLeft),Ms.text(Js.current.tripleScoresLeft),Ps.text(Js.tripleScoresBanked),Ns.text(Js.lifeSaversBanked),Ls.text(Js.current.revealAllLeft);var r=parseInt(Js.current.questionNo,10)+1;Ss.text(r+"/"+et[Ur].length),js.addClass("show"),Hs.text("Round "+r),ys.text(rt.answer.length),c(),Hs.addClass("fadeInOut"),setTimeout(function(){Hs.removeClass("fadeInOut"),_s.text(rt.answer.length+" letters"),_s.addClass("fadeInOut")},Rr),setTimeout(function(){_s.removeClass("fadeInOut"),es.removeClass("hide")},2*Rr),setTimeout(function(){for(dr(),GAME.sound.play("ding"),e=0;e<=Js.current.clueNo;e++)js.removeClass("show");pt=!1,Js.firstTimePlaying&&(Zs=!1,V(),sr(),Js.firstTimePlaying=!1,kr())},2*Rr+qr)}function _(){var e,r=parseInt(Js.current.clueNo,10);As.html(""),lt=!0,cs.addClass("hide"),rt=et[Ur][Js.current.questionNo];for(var s=0;s<xs.length;s++)if(j(s,!0),r>s){for(var t=0;t<xs[s][0].children.length;t++)if("beginSpace"===xs[s][0].children[t].className||"endSpace"===xs[s][0].children[t].className){for(var o=0;o<mt[s].length;o++)xs[s][0].children[t].children[o].innerText=mt[s][o];break}e=parseInt(Js.current.pendingBonus[s],10),F(s,e)}Js.current.pendingBonus.length>Js.current.clueNo&&F(Js.current.clueNo,parseInt(Js.current.pendingBonus[Js.current.clueNo],10));var a=parseInt(Js.current.questionNo,10)+1;Ss.text(a+" / "+et[Ur].length),Bs[0].className="clueCover clue"+yr,zs="",js.addClass("show"),Hs.text("BONUS ROUND!"),K(mt[r]),Hs.addClass("fadeInOut"),setTimeout(function(){Hs.removeClass("fadeInOut"),_s.text("Guess all 5 clues"),_s.addClass("fadeInOut")},Rr),setTimeout(function(){_s.removeClass("fadeInOut"),es.removeClass("hide")},2*Rr),setTimeout(function(){for(dr(),GAME.sound.play("ding"),s=0;s<=Js.current.clueNo;s++)js.removeClass("show")},2*Rr+qr),setTimeout(function(){for(var e=0;e<Js.current.clueNo;e++)$s[e].removeClass("hide");return pt=!1,Js.current.pendingBonus.length>Js.current.clueNo?void $s[Js.current.clueNo].addClass("unanswered").removeClass("hide"):void D()},2*Rr+2*qr)}function D(){var e=mt[Js.current.clueNo];Js.current.bonusLettersSelected=Math.ceil(e.length/2-Er.LOW),Js.current.bonusLettersSelected=Js.current.bonusLettersSelected>0?Js.current.bonusLettersSelected:0;var r=Er.LOW+parseInt(Js.current.bonusLettersSelected,10),s=e.length>r,t=r>1?"Letters":"Letter";ls.text("Reveal "+r+" "+t),r=Er.MEDIUM+parseInt(Js.current.bonusLettersSelected,10);var o=e.length>r;t=r>1?"Letters":"Letter",ns.text("Reveal "+r+" "+t),r=Er.HIGH+parseInt(Js.current.bonusLettersSelected,10),t=r>1?"Letters":"Letter",as.text("Reveal "+r+" "+t),us.removeClass("show").addClass("hide"),cs.addClass("hide"),ds.addClass("hide"),hs.addClass("hide"),ws.addClass("hide"),rs.removeClass("hide"),o&&(ts.removeClass("hide"),ns.removeClass("hide")),s&&(os.removeClass("hide"),ls.removeClass("hide")),ss.removeClass("hide"),as.removeClass("hide")}function j(e,r){for(var s="",t=rt.clues[e].split("%"),o=0;o<t.length;o++)""===t[o]?r?(s+=o>0?'<div class="shownWord right">':'<div class="shownWord left">',s+=rt.answer,s+="</div>"):(s+=o>0?'<div class="beginSpace">':'<div class="endSpace">',s+=h(rt.answer),s+="</div>"):r?(mt[e]=t[o],s+=o>0?'<div class="beginSpace">':'<div class="endSpace">',s+=e!==Js.current.clueNo?h(t[o],!0):h(t[o]),s+="</div>"):(s+=o>0?'<div class="shownWord right">':'<div class="shownWord left">',s+=t[o],s+="</div>");xs[e].html(s)}function F(e,r){var s;switch($s[e][0].className="bonusPts hide",r){case Ar.LOW:s="low";break;case Ar.MEDIUM:s="medium";break;case Ar.HIGH:s="high";break;default:s="zero"}$s[e].addClass(s).text(r)}function K(e){ys.text(e.length),removeAllChildren(As[0]),zs="",As.html(h(e))}function Y(e,r){var s,t=[];if(!pt){pt=!0,t=$.random(1,mt[Js.current.clueNo].length-2,Js.current.bonusLettersSelected+1),Js.current.bonusLettersRevealed=t,r===Er.LOW&&Js.current.bonusLettersRevealed.push(0),(r===Er.LOW||r===Er.MEDIUM)&&Js.current.bonusLettersRevealed.push(mt[Js.current.clueNo].length-1),F(Js.current.clueNo,e),s=h(mt[Js.current.clueNo]),As.html(s);for(var o=0;o<xs[Js.current.clueNo][0].children.length;o++)if("beginSpace"===xs[Js.current.clueNo][0].children[o].className||"endSpace"===xs[Js.current.clueNo][0].children[o].className){xs[Js.current.clueNo][0].children[o].innerHTML=s;break}Js.current.pendingBonus[Js.current.clueNo]=e,kr(),$s[Js.current.clueNo].removeClass("hide").addClass("unanswered"),us.removeClass("hide").addClass("show"),ds.removeClass("hide"),hs.removeClass("hide"),rs.addClass("hide"),ss.addClass("hide"),ts.addClass("hide"),os.addClass("hide"),as.addClass("hide"),ns.addClass("hide"),ls.addClass("hide"),setTimeout(function(){pt=!1},Ir)}}function V(){return Zs?(Us.removeClass("selected"),qs.addClass("hide"),void(Zs=!1)):(Us.addClass("selected"),qs.removeClass("hide"),void(Zs=!0))}function z(){GAME.sound.init(Js.soundOn),Xr[0].style["background-image"]="url(images/"+Js.bg.current+".jpg)",Js.darkOn&&(Hr.addClass("dark"),Xr.addClass("dark")),document.body.className+=" "+Js.font.classes[Js.font.current]}function Z(e){var r=[],s;switch(e){case"set_bg":Js.bg.current++,Js.bg.current>Js.bg.max&&(Js.bg.current=1),Xr[0].style["background-image"]="url(images/"+Js.bg.current+".jpg)";break;case"set_dark":case"darkButton":Js.darkOn?(Hr.removeClass("dark"),Xr.removeClass("dark"),Js.darkOn=!1):(Hr.addClass("dark"),Xr.addClass("dark"),Js.darkOn=!0);break;case"set_font":r=Js.font.classes[Js.font.current],Js.font.current++,Js.font.current===Js.font.classes.length&&(Js.font.current=0),s=document.body.className,document.body.className=s.replace(r,Js.font.classes[Js.font.current]);break;case"set_sound":Js.soundOn?(Os.addClass("off"),Js.soundOn=!1,GAME.sound.mute(!0)):(Os.removeClass("off"),Js.soundOn=!0,GAME.sound.mute(!1));break;case"set_help":sr()}kr()}function J(){console.log("showGameView"),pt=!0,gr(!1,function(){Xr.removeClass("hide").removeClass("outToRight").addClass("inFromRight"),Hr.removeClass("inFromLeft").addClass("outToLeft"),ws.text(Js.current.guessWorth),Gs.text(Js.current.score),ht=-1!=Mr.indexOf(Js.current.questionNo),setTimeout(function(){ht?_():H()},Or)})}function Q(){y(),G(),is.removeClass("show"),us.removeClass("dim"),Xr.removeClass("hide").removeClass("inFromRight").addClass("outToRight"),Hr.removeClass("outToLeft").addClass("inFromLeft"),es.addClass("hide"),ut=!1}function X(e,r){bt&&bt(),bt=r,Ds.html(e),Ds.addClass("show"),js.addClass("show dim"),setTimeout(function(){st=!0},Or)}function er(){if(Ds.removeClass("show"),js.removeClass("show").removeClass("dim"),st=!1,Js.firstTimeVersion21)return Js.firstTimeVersion21=!1,kr(),void u();if(ft)return void(ft=!1);if(10===Ct)return Ct=0,void kr();if(gt.length>0){var e="<h1>Congrats!</h1>";e+=gt.length>1?"Achievements unlocked:":"Achievement unlocked:";for(var r=0;r<gt.length;r++)e+='<div class="achievement">'+gt[r]+"</div>";return gt.length=0,void X(e+'<div class="popupButton">OK</div>')}return Js.current.score===parseInt(Gs.text(),10)?void rr():void setTimeout(function(){O(Js.current.score,rr)},Or)}function rr(){return ut?void Q():void I()}function sr(){if(ct>=wr){Is.addClass("hide"),ct=0,V();for(var e=0;wr>e;e++)Rs[e][0].className="helpScreen";return void(Xr[0].style["background-image"]="url(images/"+Js.bg.current+".jpg)")}0===ct?(Xr[0].style["background-image"]="none",Is.removeClass("hide")):Rs[ct-1].removeClass("show").addClass("hide"),Rs[ct].removeClass("hide").addClass("show"),ct++}function tr(){X('<h1>New Version!</h1>Welcome to the newly updated Word Clues 2!<ul class="newVersionList"><li>Life Saver: if you get stuck after seeing all clues, we will bail you out by revealing all but one letter in the answer, while giving you a score of at least 50 pts!</li><li>Triple Word Score: 3x your score, once per game!</li><li>Achievements: accessed by touching "Leaders"</li><li>Bonus Round: we give you the answer, you give us the clues!</li></ul><div class="popupButton">OK</div>')}function or(){ps.addClass("show"),js.addClass("show dim"),setTimeout(function(){st=!0,at=!0},Or),(0===Js.current.tripleScoresLeft||1!==Js.current.multiplier)&&fs.addClass("dim"),(0===Js.current.doubleScoresLeft||1!==Js.current.multiplier)&&vs.addClass("dim"),(0===Js.current.revealAllLeft||Js.current.allCluesRevealed||Js.current.lifeSaverPlayed)&&Cs.addClass("dim"),(Js.current.lifeSaverPlayed||Js.current.clueNo+1<yr)&&ms.addClass("dim"),(Js.current.firstLettersRevealed>0||Js.current.lifeSaverPlayed)&&gs.addClass("dim"),(Js.current.vowelRevealed.length>0||Js.current.lifeSaverPlayed)&&ks.addClass("dim")}function ar(e,r){GAME.sound.play("lowClick"),ps.removeClass("show"),js.removeClass("show").removeClass("dim"),st=!1,at=!1,void 0!==e&&setTimeout(function(){e(r)},Or)}function nr(){bs.addClass("show"),js.addClass("show dim"),setTimeout(function(){st=!0,nt=!0},Or)}function lr(){bs.removeClass("show"),js.removeClass("show").removeClass("dim"),st=!1,nt=!1}function ur(e){Js.current.guessWorth+=e?e:0,ws.text(Js.current.guessWorth)}function cr(){c(),Js.current.clueNo=yr-1,Js.current.allCluesRevealed=!0,kr(),GAME.sound.play("ding"),dr()}function ir(){Js.current.lifeSaverPlayed=!0,vt=!0,Js.current.guessWorth<50&&(Js.current.guessWorth=50,ws.text(Js.current.guessWorth));for(var e=rt.answer.length;e--;)if(" "===zs[e]||""===zs[e]){Js.current.firstLettersRevealed=e;break}Js.lifeSaversBanked--,Ns.text(Js.lifeSaversBanked),kr(),c()}function dr(){Bs[0].className="clueCover clue"+(parseInt(Js.current.clueNo,10)+1),Ts.text(Js.current.clueNo===yr?"":yr-Js.current.clueNo-1),Js.current.clueNo+1>=yr&&ms.removeClass("dim")}function hr(e){GAME.bridge.request("get.appSettings","",function(r,s){if(r)console.log(r),Js=GAME.DEFAULT.settingsCache;else if("object"!=typeof s||isEmptyObj(s))console.log("Applying DEFAULT SETTINGS"),Js=GAME.DEFAULT.settingsCache;else{Js=s;for(var t in GAME.DEFAULT.settingsCache)copyProperty(t,GAME.DEFAULT.settingsCache,Js)}e()})}function pr(e){GAME.bridge.request("get.achievements","",function(r,s){if(r)return void console.log(r);if("object"!=typeof s||isEmptyObj(s))Xs=cloneStructure(GAME.DEFAULT.achievementsCache);else{Xs=s;for(var t in GAME.DEFAULT.achievementsCache)copyProperty(t,GAME.DEFAULT.achievementsCache,Xs)}fr(e)})}function vr(e){GAME.bridge.request("get.stats","",function(r,s){return r?void console.log(r):(Qs="object"!=typeof s||isEmptyObj(s)?cloneStructure(GAME.DEFAULT.statsCache):s,void mr(e))})}function fr(e){for(var r in e)100==e[r].value&&(Xs[r]={value:100});Cr()}function mr(e){void 0!==e&&null!==e&&void 0!==e.gamesPlayed&&(void 0===Qs.gamesPlayed||void 0!==Qs.gamesPlayed&&e.gamesPlayed>=Qs.gamesPlayed)&&(Qs=e),N(),M()}function gr(e,r){et=[[{answer:"show",clues:["puppet%","peep%","%room","%up","side%","%case","%place","dog%","%man"]},{answer:"thought",clues:["%experiment","%leader","%up","%provoking","fore%","after%","%about","%process","%wave"]},{answer:"hat",clues:["%stand","straw%","%box","%maker","%trick","%size","%rack","hard%","top%"]},{answer:"glass",clues:["%cutter","hour%","%ceiling","%house","wine%","%works","parting%","%oven","%eye"]},{answer:"on",clues:["%coming","hold%","%set","%look","%sale","hang%","%shore","head%","%going"]},{answer:"moon",clues:["%light","%shine","%beam","%struck","%walk","harvest%","honey%","full%","half%"]},{answer:"watch",clues:["anchor%","over%","%maker","%spring","%house","%tower","stop%","%man","%dog"]},{answer:"note",clues:["%worthy","%paper","%book","%taker","bank%","%pad","whole%","quarter%","foot%"]},{answer:"bull",clues:["pit%","%nose","%frog","%fight","%head","%whip","%doze","%dog","%finch"]},{answer:"nail",clues:["%bed","toe%","%salon","hang%","%file","%head","%brush","%art","%gun"]}],[{answer:"bear",clues:["honey%","sugar%","polar%","water%","bug%","%skin"]},{answer:"week",clues:["%ahead","work%","%end","%long","%day"]},{answer:"post",clues:["guide%","goal%","starting%","%haste","%modern","%dated","%man","%office","fence%","lamp%"]},{answer:"pressure",clues:["%cooker","%washer","air%","%treated","%sensor","low%","vapor%","water%","%tank"]},{answer:"carpet",clues:["%bombing","%snake","%rod","%beater","%bagger"]},{answer:"book",clues:["account%","%seller","%case","sketch%","telephone%","picture%","school%","hand%","check%","pass%"]},{answer:"book",clues:["spelling%","law%","%keeper","pocket%","stamp%","%shelf","%fair","%house","story%","guide%"]},{answer:"book",clues:["play%","prayer%","%shop","note%","cook%","text%","%worm","%mark","phrase%","bank%"]},{answer:"station",clues:["weather%","way%","space%","%agent","pump%","%master"]},{answer:"tax",clues:["%free","%payer","property%","%dodger","%man","%bracket"]}]],r()
}function kr(){GAME.bridge.request("set.appSettings",Js)}function Cr(){GAME.bridge.request("set.achievements",Xs)}function br(){GAME.bridge.request("set.game",et)}function Sr(e){var r=e;try{for(;r&&!r.getAttribute("id");)r=r.parentNode;return r?r.id:null}catch(s){return null}}var wr=3,yr=5,Gr=100,Ar={LOW:20,MEDIUM:40,HIGH:80},Er={LOW:3,MEDIUM:2,HIGH:1},Mr=[4,9],Pr=4,Nr=1,Lr=10,Wr=25,Br=2,Tr=5,xr=10,$r=Gr*Nr*2+Gr*Pr+Ar.HIGH*Mr.length*yr+Gr*(xr-Mr.length),Or=320,Ir=520,Rr=1520,qr=1e3,Ur=0,Hr,_r,Dr,jr,Fr,Kr,Yr,Vr,zr,Zr,Jr,Qr,Xr,es,rs,ss,ts,os,as,ns,ls,us,cs,is,ds,hs,ps,vs,fs,ms,gs,ks,Cs,bs,Ss,ws,ys,Gs,As,Es,Ms,Ps,Ns,Ls,Ws,Bs,Ts,xs=[],$s=[],Os,Is,Rs=[],qs,Us,Hs,_s,Ds,js,Fs,Ks,Ys,Vs,zs,Zs=!1,Js,Qs={},Xs={},et,rt,st,tt,ot,at,nt,lt,ut,ct,it,dt,ht,pt,vt,ft,mt=[],gt=[],kt=Tr,Ct=0,bt;return $(e).bind("load",r),{onTouchEvent:t,routeTouchEvent:o,isTouchDisabled:function(){return pt},messages:{}}}(this);