java -jar resources/closure/compiler.jar --js_output_file="clues-html/js/main.js" clues-html-dev/js/hammer.js clues-html-dev/js/howler.js clues-html-dev/js/random.js clues-html-dev/js/core.js clues-html-dev/js/utils.js clues-html-dev/js/wc.js clues-html-dev/js/wc-android.js clues-html-dev/js/wc-amazon.js clues-html-dev/js/sounds.js clues-html-dev/js/cacheDefaults.js clues-html-dev/js/bridge.js
java -jar resources/closure/closure-stylesheets.jar --allow-unrecognized-functions --allow-unrecognized-properties --output-file "clues-html/css/main.css" clues-html-dev/css/main.css clues-html-dev/css/android.css