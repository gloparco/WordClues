package com.kindredgames.wordclues.amazon;

import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;

import com.kindredgames.wordclues.CacheController;
import com.kindredgames.wordclues.CacheFile;
import com.kindredgames.wordclues.logic.GameGenerator;

import junit.framework.Assert;

public class WordCluesAmazonDebugActivityTest extends ActivityInstrumentationTestCase2<WordCluesAmazonDebugActivity> {

    public WordCluesAmazonDebugActivityTest() {
        super(WordCluesAmazonDebugActivity.class);
    }

    public void testActivityTestCaseSetUpProperly() {
        assertNotNull("activity should be launched successfully", getActivity());
    }

    public void testCluesLoading() {
        Context context = getActivity().getApplicationContext();
        CacheController cache = new CacheFile(context);
        GameGenerator gameEngine = new GameGenerator(cache, context);
        Assert.assertEquals("Sets count", 1, gameEngine.getSetsCount());
        Assert.assertEquals("Lines count", 417, gameEngine.getLinesCount(0));
        Assert.assertEquals("First answer", "head", gameEngine.getLineClue(0, 10, 0));
        Assert.assertEquals("First clue with %ba", "%band", gameEngine.getLineClue(0, 10, 3));

        Assert.assertEquals("First answer", "angle", gameEngine.getLineClue(0, 36, 0));
        Assert.assertEquals("First clue with %be", "%bead", gameEngine.getLineClue(0, 36, 9));
    }

}
