package com.kindredgames.wordclues.amazon;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public enum AmazonWordCluesSkus {

    UNLIMITED_GAMES("WordCluesUnlimitedGames", "WordCluesUnlimitedGames", 1),
    LIFE_SAVER("WordCluesLifeSaver", "WordCluesLifeSaver", 1),
    TRIPLE_WORDSCORE("WordCluesTripleWordScore", "WordCluesTripleWordScore", 1);

    private String id;
    private String name;
	private int quantity;

	private AmazonWordCluesSkus(String id, String name, int quantity) {
		this.id = id;
        this.name = name;
		this.quantity = quantity;
	}

    public static AmazonWordCluesSkus valueForId(String id) {
        Set<String> keys = SKUS.keySet();
        for (String key : keys) {
            AmazonWordCluesSkus sku = SKUS.get(key);
            if (sku.getId().equals(id)) {
                return sku;
            }
        }
        return null;
    }

    public static AmazonWordCluesSkus valueForName(String name) {
        return SKUS.get(name);
    }

    public String getId() {
		return id;
	}

    public String getName() {
        return name;
    }

    public int getQuantity() {
		return quantity;
	}

	private static Map<String, AmazonWordCluesSkus> SKUS = new HashMap<String, AmazonWordCluesSkus>();
	static {
        SKUS.put(UNLIMITED_GAMES.getName(), UNLIMITED_GAMES);
        SKUS.put(LIFE_SAVER.getName(), LIFE_SAVER);
        SKUS.put(TRIPLE_WORDSCORE.getName(), TRIPLE_WORDSCORE);
	}

	public static Set<String> getAll() {
		return SKUS.keySet();
	}

}
