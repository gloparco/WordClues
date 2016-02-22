package com.kindredgames.wordclues.logic;

import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;

import com.kindredgames.wordclues.CacheController;
import com.kindredgames.wordclues.CacheFile;
import com.kindredgames.wordclues.CacheFileCrypted;
import com.kindredgames.wordclues.Cashier;
import com.kindredgames.wordclues.CluesWebViewManager;
import com.kindredgames.wordclues.GameCenterManager;
import com.kindredgames.wordclues.GameController;
import com.kindredgames.wordclues.LoadUrlCallback;
import com.kindredgames.wordclues.MessageDispatcher;
import com.kindredgames.wordclues.ResultCallback;
import com.kindredgames.wordclues.SignalCallback;
import com.kindredgames.wordclues.StoreManager;
import com.kindredgames.wordclues.core.R;
import com.kindredgames.wordclues.util.KGLog;
import com.kindredgames.wordclues.util.Utils;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainViewController implements GameController, Cashier, MessageDispatcher {

    public static final String RESPONSE_OK = "{\"ok\":1}";
    public static final String RESPONSE_FAILURE = "{\"ok\":0}";
    public static final String RESPONSE_GAME_CENTER_CLOSED = "{\"topic\":\"gamecenter.closed\"}";

    public static final String DEFAULT_CACHE_USER = "default"; // so far we don't switch cache if user ever changes, as it's not supposed to
    public static final String DEFAULT_USER_DISPLAY_NAME = "Dude";

    public static final String FEEDBACK_EMAIL = "support@kindredgames.com";

    private GameGenerator gameEngine;
    private StoreManager storeManager;
    private GameCenterManager gameCenterManager;

    private LoadUrlCallback loadUrlCallback;
    //UIActivityIndicatorView activityIndicator;

    private CluesWebViewManager webViewManager;
    //SLComposeViewController socialController;

    private String currentUserId; // GameCenter PlayerID or default value
    private String currentUserDisplayName; // GameCenter display name or default name
    private String currentUserCache; // Name of cache to be used for the user. Precalculated for performance

    private CacheController cache;
    private CacheController fastCache;

    private String startPage;
    private Context context;

    public MainViewController(LoadUrlCallback loadUrlCallback, Context context, String startPage, GameCenterManager gameCenterManager) {
        super();
        this.context = context;
        this.startPage = startPage;
        this.gameCenterManager = gameCenterManager;
        this.loadUrlCallback = loadUrlCallback;
        initUserCache(context);
        gameEngine = new GameGenerator(fastCache, context);
        this.webViewManager = new CluesWebViewManager(this);
        // Looks like onResume gets called always, so this is where all data loading should be done
        //initUser(); // initializing user may also cause chain reaction, and all components must be ready. So the call must be the last one.
        initSounds();
    }

    public void setStoreManager(StoreManager storeManager) {
        this.storeManager = storeManager;
    }

    public CluesWebViewManager getWebViewManager() {
        return webViewManager;
    }

    public GameCenterManager getGameCenter() {
        return gameCenterManager;
    }

    public void onStart() {
        initUser();
        authenticateWithGameCenter();
    }

    public void onResume() {
        storeManager.init();
        getGameCenter().resume();
    }

    public void onPause() {
        gameCenterManager.pause();
    }

    public void onStop() {
        gameCenterManager.disconnect();
    }

    protected void initUser() {
        if (getGameCenter().isAuthenticated()) {
            updateCurrentGameCenterUser();
        } else {
            //[self resetDefaultUser];
            loadLastUser();
            authenticateWithGameCenter();
        }
    }

    protected void initUserCache(Context context) {
        //nonCloudKeys = @["game"];

        // File cache is a custom file-based cache solution. As each cached item is in a separate file, cache contents is easy to check or delete. Use it for debugging.
        // Also it should have better performance for appending to cached value
        cache = new CacheFileCrypted(context);
        fastCache = new CacheFile(context);
    }

    protected void loadLastUser() {
        String cachedPlayerId = cache.get("playerId", "");
        if (Utils.isEmptyString(cachedPlayerId)) {
            resetDefaultUser();
            // don't load UI first time as it will immediately result in reloading once GC responses
        } else {
            currentUserId = cachedPlayerId;
            currentUserCache = getUserCacheFolder(currentUserId);
            currentUserDisplayName = cache.get("playerName", "");
            KGLog.d("Loading cached user: %s", currentUserId);
            loadGamePage(false);
        }
    }

    public void viewDidLoad() {
//        super.viewDidLoad();
//        // Do any additional setup after loading the view.
//        zoomFitContent();
//        setNeedsStatusBarAppearanceUpdate();
//        initReachibility();
    }

    public void webViewDidLoad() {
        //activityIndicator.stopAnimating();
    }

    protected void loadGamePage(boolean saveNewUser) {
        KGLog.d("Loading URL: %s", startPage);

        loadUrlCallback.loadUrl(startPage);

        // The last loaded user is going to be the next candidate as long as JS doesn't support switching
        if (saveNewUser) {
            cache.set("playerId", currentUserId, "");
            cache.set("playerName", currentUserDisplayName, "");
        }
    }

    public String generateGames(int gamesCount) {
        String games = gameEngine.generateGamesJson(gamesCount);
        return filterJsonString(games);
    }

    public String generateGame() {
        String game = gameEngine.generateGameJson();
        return filterJsonString(game);
    }

    public String getUserCacheFolder(String userId) {
        return userId.replace(":", "");
        //return [KGCUtils getStringHash:userId];
    }

    public String getCacheUser() {
        return currentUserCache;
    }

    public String getUserCache(String cacheName) {
        String userId = getCacheUser();
        String cachedValue = cache.get(cacheName, userId);
        return filterJsonStringNullable(cachedValue);
    }

    /**
     * null data deletes the cache file
     */
    public String setUserCache(String cacheName, String data) {
        String userId = getCacheUser();
        if (cache.set(cacheName, data, userId)) {
//        if ([nonCloudKeys indexOfObject:cacheName] == NSNotFound) {
//            [cloud set:cacheName value:data forUser:userId];
//        }
            return RESPONSE_OK;
        } else {
            return RESPONSE_FAILURE;
        }
    }

    private String filterJsonString(String json) {
//    return (json == null) ? "{}" : [json stringByReplacingOccurrencesOfString:"\n" withString:""];
        return (json == null) ? "null" : json.replace("\n", "");
    }

    private String filterJsonStringNullable(String json) {
        return (json == null) ? null : json.replace("\n", "");
    }

    private void authenticateWithGameCenter() {
        gameCenterManager.connect(new SignalCallback() {
            @Override
            public void onResult() {
                updateCurrentGameCenterUser();
            }

            @Override
            public void onFault() {
                updateCurrentGameCenterUser(); // That can handle default user
            }
        });
    }

    private void resetDefaultUser() {
        currentUserId = DEFAULT_CACHE_USER;
        currentUserCache = getUserCacheFolder(currentUserId);
        currentUserDisplayName = DEFAULT_USER_DISPLAY_NAME;
    }

    private void updateCurrentGameCenterUser() {
        String newGameCenterUser = getGameCenter().getPlayerId();
        KGLog.d("GC user new=%s", newGameCenterUser);
        boolean changingGameCenterUser = (currentUserId == null || !currentUserId.equals(newGameCenterUser));
        if (newGameCenterUser == null) {
            resetDefaultUser();
        } else {
            currentUserId = newGameCenterUser;
            currentUserCache = getUserCacheFolder(currentUserId);
            currentUserDisplayName = getGameCenter().getPlayerAlias();
        }
        if (changingGameCenterUser) {
            KGLog.d("Changing GC user %s", currentUserId);
            loadGamePage(true);
        }

        if (!StringUtils.isBlank(currentUserId) && !DEFAULT_CACHE_USER.equals(currentUserId)) {
            // Leaderboards have to be refreshed even for the same user, as long as it is a valid GameCenter player
            loadLeaderboards();
            loadAchievements();
        }
    }

    private String postSocial(String service, String message) {
//        if (![SLComposeViewController isAvailableForServiceType:service]) {
//            // The device must be linked to a social service in device's settings for this feature to work
//            KGLog("Service %@ not available", service);
//            return (String )RESPONSE_FAILURE;
//        }
//        socialController = [[SLComposeViewController alloc] init]; //initiate the Social Controller
//        socialController = [SLComposeViewController composeViewControllerForServiceType:service];
//        [socialController setInitialText:message]; //the message you want to post
//        //[mySLComposerSheet addImage:yourimage]; //an image you could post
//        //for more instance methodes, go here:https://developer.apple.com/library/ios/#documentation/NetworkingInternet/Reference/SLComposeViewController_Class/Reference/Reference.html#//apple_ref/doc/uid/TP40012205
//        [self presentViewController:socialController animated:YES completion:null];
//
//        [socialController setCompletionHandler:^(SLComposeViewControllerResult result) {
//            String output;
//            switch (result) {
//                case SLComposeViewControllerResultCancelled:
//                    output = "Action Cancelled";
//                    break;
//                case SLComposeViewControllerResultDone:
//                    output = "Post Successfull";
//                    break;
//                default:
//                    break;
//            } //check if everythink worked properly. Give out a message on the state.
//            //UIAlertView *alert = [[UIAlertView alloc] initWithTitle:"Facebook" message:output delegate:null cancelButtonTitle:"Ok" otherButtonTitles:null];
//            //[alert show];
//            KGLog("Social Post result: %", output);
//        }];
        return RESPONSE_OK;
    }

    public String postFacebook(JSONObject message) {
        return postSocial("fb", extractFrom(message, "message"));
    }

    public String postTwitter(JSONObject message) {
        return postSocial("tw", extractFrom(message, "message"));
    }

    public void postEmail(JSONObject message) {
        String subject = extractFrom(message, "subject");
        if (subject == null) {
            subject = "Word Clues Feedback";
        }
        String body = extractFrom(message, "body");
        if (body == null) {
            body = "";
        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        //intent.setType("message/rfc822");
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{FEEDBACK_EMAIL});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, body);
        try {
            loadUrlCallback.startActivity(Intent.createChooser(intent, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            KGLog.e("There are no email clients installed.");
            //Toast.makeText(MyActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        } catch (Exception exc) {
            KGLog.e("Error opening email post: %s", exc.toString());
        }
    }

    private String extractFrom(JSONObject message, String field) {
        try {
            return message != null && message.has(field) ? message.getString(field) : null;
        } catch (JSONException exc) {
            KGLog.e("Error reading JSON field %s: %s", field, exc.toString());
            return null;
        }
    }

    public void linkStore() {
        openUrl(storeManager.getStoreUrl());
    }

    public void linkTwitter() {
        if (canOpenTwitterApp()) {
            openUrl("twitter://user?screen_name=kindredgames");
        } else {
            openUrl("https://twitter.com/kindredgames");
        }
    }

    public void linkFacebook() {
        if (canOpenFacebookApp()) {
            openUrl("fb://profile/kindredgames");
        } else {
            openUrl("https://www.facebook.com/");
        }
    }

    public boolean canOpenFacebookApp() {
        return checkApplicationInfo("com.facebook.katana");
    }

    public boolean canOpenTwitterApp() {
        return checkApplicationInfo("com.twitter.android");
    }

    private boolean checkApplicationInfo(String namespace) {
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(namespace, 0);
            return true;
        } catch( PackageManager.NameNotFoundException e ){
            return false;
        }
    }

    public void linkCompany() {
        openUrl("http://www.kindredgames.com");
    }

    public void openUrl(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(browserIntent);
    }

    public String canMakePayments() {
        return storeManager.isEnabled() ? "{\"storeEnabled\":true}" : "{\"storeEnabled\":false}";
    }

    public void price(JSONObject invoice) {
        if (storeManager.isEnabled()) {
            try {
                JSONArray productIds = invoice.getJSONArray("productIds");
                List<String> productNames = new ArrayList<String>();
                if (productIds != null) {
                    for (int i = productIds.length() - 1; i >= 0; i--) {
                        String productName = productIds.getString(i);
                        if (StringUtils.isNotEmpty(productName)) {
                            productNames.add(productName);
                        }
                    }
                } else {
                    String productName = extractFrom(invoice, "productId");
                    if (StringUtils.isNotEmpty(productName)) {
                        productNames.add(productName);
                    }
                }
                if (productNames.size() > 0) {
                    storeManager.prices(productNames);
                } else {
                    KGLog.e("Price request has no valid productID: %s", invoice);
                }
            } catch (JSONException exc) {
                KGLog.e("Error pricing products: %s", exc);
            }
        } else {
            KGLog.d("Payments are disabled");
        }
    }

    public void buy(JSONObject invoice) {
        if (storeManager.isEnabled()) {
            storeManager.buy(extractFrom(invoice, "productId"));
        } else {
            KGLog.d("Payments are disabled");
        }
    }

    public String owns(JSONObject productIdMessage) {
        String productId = extractFrom(productIdMessage, "productId");
        if (productId != null) {
            return cache.get(getProductCacheName(productId), "");
        }
        return null;
    }

    public void restorePurchases() {
        if (storeManager.isEnabled()) {
            storeManager.restorePurchases();
        }
    }

    @Override
    public void acceptPayment(String productId, String receipt) {
        cache.set(getProductCacheName(productId), receipt, "");
    }

    @Override
    public JSONObject getStoredReceipt(String productId) {
        return Utils.jsonData(cache.get(getProductCacheName(productId), ""));
    }

    public String getProductCacheName(String productId) {
        return String.format("product_%s", productId);
    }

    public String getAchievementValues() {
        try {
            JSONObject achievements = getGameCenter().getAchievementValues();
            if (achievements != null) {
                return achievements.toString(0);
            }
        } catch (JSONException exc) {
            KGLog.e("Error stringifying achievements: %s", exc.toString());
        }
        return null;
    }

    public void updateAchievements(JSONObject message) {
        gameCenterManager.updateAchievements(message);
    }

    public void loadLeaderboards() {
        getGameCenter().loadLeaderboards(new ResultCallback<JSONObject>() {
            @Override
            public void onResult(JSONObject result) {
                try {
                    JSONObject message = new JSONObject();
                    message.put("topic", "leaderboards");
                    message.put("scores", result);
                    dispatchMessage(message);
                } catch (JSONException exc) {
                    KGLog.e("Error building Leaderboards message: %s", exc.toString());
                }
            }

            @Override
            public void onFault(String reason) {
                KGLog.w("Error loading Leaderboards %s", reason);
            }
        });
    }

    public void loadAchievements() {
        getGameCenter().loadAchievements(new ResultCallback<JSONObject>() {
            @Override
            public void onResult(JSONObject result) {
                try {
                    JSONObject message = new JSONObject();
                    message.put("topic", "achievements");
                    message.put("achievements", result);
                    dispatchMessage(message);
                } catch (JSONException exc) {
                    KGLog.e("Error building Achievements message: %s", exc.toString());
                }
            }

            @Override
            public void onFault(String reason) {
                KGLog.w("Error loading Achievements: %s", reason);
            }
        });
    }

    public void dispatchMessage(JSONObject message) {
        webViewManager.sendMessageObject(loadUrlCallback, message);
    }

    private HashMap<String, Integer> soundMap;
    private MediaPlayer soundPlayer;
    private SoundPool soundPool;

    private void initSounds() {
        String[] sounds = context.getResources().getStringArray(R.array.sounds);
        soundMap = new HashMap<String, Integer>(sounds.length);
        try {
            //FileDescriptor soundFile = context.getResources().openRawResourceFd(R.raw.sounds).getFileDescriptor();
            soundPool = new SoundPool(sounds.length, AudioManager.STREAM_MUSIC, 0);
            soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                @Override
                public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                    KGLog.d("Sound %d loaded with status=%d", sampleId, status);
                }
            });

            AssetManager am = context.getAssets();

            //soundPlayer = MediaPlayer.create(context, R.raw.sounds);
            for (String soundName : sounds) {
//                String[] item = soundCoded.split("=");
//                String name = item[0].trim();
//                String[] clipCodes = item[1].split(",");
//                int offset = Integer.parseInt(clipCodes[0].trim());
//                int length = Integer.parseInt(clipCodes[1].trim());
                //soundMap.put(name, soundPool.load(soundFile, offset, length, 1));
                soundMap.put(soundName, soundPool.load(am.openFd("sounds/" + soundName + ".mp3"), 1));
            }
        } catch (Exception exc) {
            KGLog.error("Error creating sounds: %s", exc);
        }
    }

    @Override
    public void playSound(String name) {
        int soundId = soundMap.get(name);
        soundPool.play(soundId, 1.0f, 1.0f, 100, 0, 1.0f);
    }

}
