package com.kindredgames.wordclues;

import com.kindredgames.wordclues.util.KGLog;
import com.kindredgames.wordclues.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class CluesWebViewManager //implements ValueCallback<String>
{

    private static final int TOPIC_COMMAND_INDEX = 0;
    private static final int TOPIC_FIRST_OPTION_INDEX = 1;
    private static final int TOPIC_SECOND_OPTION_INDEX = 2;
    private static final int TOPIC_THIRD_OPTION_INDEX = 3;

    private static final String RESPONSE_OK = "{\"ok\":1}";
    private static final String RESPONSE_FAILURE = "{\"ok\":0}";

    private GameController controller;
    private boolean loaded;
    private List<String> earlyMessages;

//    private ScalableWebView _webView;
//
//    public void setWebView(ScalableWebView webView) {
//        _webView = webView;
//    }

    public CluesWebViewManager(GameController controller) {
//    public CluesWebViewManager(Activity activity) {
        super();
        this.controller = controller;
        this.loaded = false;
        this.earlyMessages = new ArrayList<String>();
    }

    public boolean shouldOverrideUrlLoading(LoadUrlCallback loadUrlCallback, java.lang.String url) {
        if (url.startsWith("wmw:")) {
            String[] components = url.split(":");

            // Topic format: command.name/do|destination/some/other/parameters
            String topic = components.length > 1 ? components[1] : null;
            String[] topicParams = topic != null ? topic.split("\\.") : null;

            int callbackId = components.length > 2 ? Integer.parseInt(components[2]) : -1;
            String argsAsString = null;
            if (components.length > 3) {
                try {
                    argsAsString = URLDecoder.decode(components[3], "UTF-8").trim();
                } catch (UnsupportedEncodingException exc) {
                    KGLog.e("Error decoding arguments: %s", exc.toString());
                    return false;
                }
            }
            KGLog.d("Received JS topic=[%s] message[%d]: %s", topic, argsAsString != null ? argsAsString.length() : -1, argsAsString);

            handleCall(loadUrlCallback, topic, topicParams, callbackId, argsAsString);
            return false;
        }
        // Accept this location change
        return true;
    }

//    @JavascriptInterface
//    public void handleJavascript(String topic, int callbackId, Object args) {
//        String[] topicParams = topic != null ? topic.split("\\.") : null;
//        handleCall(_webView, topic, topicParams, callbackId, args != null ? args.toString() : null);
//    }

    public void handleCall(LoadUrlCallback loadUrlCallback, String topic, String[] topicParams, int callbackId, String messageJson) {
        handleWebViewLoaded(loadUrlCallback); // first incoming message triggers sending all accumulated early messages

        String response = null;
        String command = topicParams[TOPIC_COMMAND_INDEX];
        String firstOption = topicParams.length - 1 >= TOPIC_FIRST_OPTION_INDEX ? topicParams[TOPIC_FIRST_OPTION_INDEX] : null;
        String function = firstOption; // only for readability
        String secondOption = topicParams.length - 1 >= TOPIC_SECOND_OPTION_INDEX ? topicParams[TOPIC_SECOND_OPTION_INDEX] : null;
        String functionArg = secondOption; // only for readability
        //String thirdOption = topicParams.count - 1 >= TOPIC_THIRD_OPTION_INDEX ? topicParams[TOPIC_THIRD_OPTION_INDEX] : nil;
        //String functionSecondArg = thirdOption; // only for readability

        try {
            if ("sound".equals(command)) {
                controller.playSound(firstOption);
                return;
            } else if ("get".equals(command)) {
                response = controller.getUserCache(firstOption);
            } else if ("set".equals(command)) {
                response = controller.setUserCache(firstOption, messageJson);
            } else if ("generate".equals(command)) {
                if ("game".equals(function)) {
                    response = controller.generateGames(1);
                    controller.setUserCache("game", response);
                } else if ("games".equals(function)) {
                    response = controller.generateGames(Integer.parseInt(functionArg));
                    controller.setUserCache("game", response);
                }
            } else if ("post".equals(command)) {
                if ("facebook".equals(function)) {
                    response = controller.postFacebook(Utils.jsonData(messageJson));
                } else if ("twitter".equals(function)) {
                    response = controller.postTwitter(Utils.jsonData(messageJson));
                }
            } else if ("display".equals(command)) {
                if ("gamecenter".equals(function)) {
                    controller.getGameCenter().displayGameCenter();
                }
            } else if ("link".equals(command)) {
                if ("store".equals(function)) {
                    controller.linkStore();
                } else if ("twitter".equals(function)) {
                    controller.linkTwitter();
                } else if ("facebook".equals(function)) {
                    controller.linkFacebook();
                } else if ("company".equals(function)) {
                    controller.linkCompany();
                } else if ("email".equals(function)) {
                    controller.postEmail(Utils.jsonData(messageJson));
                }
            } else if ("store".equals(command)) {
                if ("enabled".equals(function)) {
                    response = controller.canMakePayments();
                } else if ("price".equals(function)) {
                    controller.price(Utils.jsonData(messageJson));
                } else if ("buy".equals(function)) {
                    controller.buy(Utils.jsonData(messageJson));
                } else if ("owns".equals(function)) {
                    response = controller.owns(Utils.jsonData(messageJson));
                } else if ("restore".equals(function)) {
                    controller.restorePurchases();
                }
            } else if ("leaderboard".equals(command)) {
                if ("get".equals(function)) {
                    response = String.format("{\"value\":\"%d\"}", controller.getGameCenter().getLeaderboardValue(functionArg));
                } else if ("set".equals(function)) {
                    controller.getGameCenter().updateLeaderboards(Utils.jsonData(messageJson));
                }
            } else if ("leaderboards".equals(command)) {
                if ("get".equals(function)) {
                    response = controller.getGameCenter().getLeaderboardValues(new JSONArray(messageJson)).toString();
                    //response = [self dataToJson:[controller getLeaderboardValues]];
                } else if ("set".equals(function)) {
                    controller.getGameCenter().updateLeaderboards(Utils.jsonData(messageJson));
                }
            } else if ("achievements".equals(command)) {
                if ("get".equals(function)) {
                    response = controller.getAchievementValues();
                } else if ("set".equals(function)) {
                    controller.updateAchievements(Utils.jsonData(messageJson));
                }
            } else if ("check".equals(command)) {
                if ("gamecenter".equals(function)) {
                    response = controller.getGameCenter().checkGameCenter();
                } else if ("facebook".equals(function)) {
                    response = controller.canOpenFacebookApp() ? RESPONSE_OK : RESPONSE_FAILURE;
                } else if ("twitter".equals(function)) {
                    response = controller.canOpenTwitterApp() ? RESPONSE_OK : RESPONSE_FAILURE;
                }
            } else if ("error".equals(command)) {
                JSONObject errorMessage = Utils.jsonData(messageJson);
                KGLog.e("JS Error: %s", errorMessage.getString("message"));
            } else {
                KGLog.e("Unsupported topic: '%s'", topic);
                return;
            }
        } catch (JSONException exc) {
            KGLog.e(String.format("Exception handling JSON: %s", exc.toString()));
        }

        if (callbackId > 0) { // Null is also a valid response
            //KGLog.d("Returning to JS response: '%s'", response);
            returnResult(loadUrlCallback, callbackId, topic, response);
        }
    }

    private void returnResult(LoadUrlCallback loadUrlCallback, int callbackId,  String topic, String json) {
        KGLog.d(String.format("Response to JS: topic=%s: %s", topic, json));
        respondWithJavaScript(loadUrlCallback, String.format("GAME.bridge.resultForCallback(%d,[%s,%s]);", callbackId, formatStringParameter(topic), formatStringParameter(json)));
    }

    private void respondWithJavaScript(final LoadUrlCallback loadUrlCallback, final String response) {
        //final ValueCallback<String> resultCallback = this;
        loadUrlCallback.runJavascript(response);
    }

//    public void onReceiveValue(String v) {
//    }

    private String formatStringParameter(String param) {
        try {
            return param == null ? "null" : String.format("'%s'", URLEncoder.encode(param, Utils.ENCODING_UTF8).replaceAll("'", "\\'"));
        } catch (UnsupportedEncodingException exc) {
            KGLog.e(exc.toString());
            return null;
        }
    }

    public void sendMessageObject(LoadUrlCallback loadUrlCallback, JSONObject response) {
        sendMessage(loadUrlCallback, response.toString());
    }

    private void sendMessage(LoadUrlCallback loadUrlCallback, String response) {
        if (loaded) {
            KGLog.d("Message to JS: %s", response);
            respondWithJavaScript(loadUrlCallback, String.format("GAME.bridge.response(%s,%s)", formatStringParameter(null), formatStringParameter(response)));
        } else {
            KGLog.d("Cached message to JS: %s", response);
            earlyMessages.add(response);
        }
    }

    private void handleWebViewLoaded(LoadUrlCallback loadUrlCallback) {
        if (!loaded) {
            loaded = true;
            for (String response : earlyMessages) {
                sendMessage(loadUrlCallback, response);
            }
            earlyMessages.clear();
        }
    }

}
