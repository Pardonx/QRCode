package cn.yescallop.qrcode.lang;

import cn.nukkit.Server;
import cn.nukkit.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Language {

    private static Map<String, String> lang;
    private static Map<String, String> fallbackLang;

    public static void load(String langName) {
        if (lang != null) {
            return;
        }
        try {
            lang = loadLang(Language.class.getClassLoader().getResourceAsStream("lang/" + langName + ".ini"));
        } catch (NullPointerException e) {
            lang = new HashMap<>();
        }
        fallbackLang = loadLang(Language.class.getClassLoader().getResourceAsStream("lang/eng.ini"));
    }

    public static String translate(String str, Object... params) {
        String baseText = get(str);
        baseText = parseTranslation(baseText != null ? baseText : str);
        for (int i = 0; i < params.length; i++) {
            baseText = baseText.replace("{%" + i + "}", parseTranslation(String.valueOf(params[i])));
        }

        return baseText;
    }

    public static String translate(String str, String... params) {
        String baseText = get(str);
        baseText = parseTranslation(baseText != null ? baseText : str);
        for (int i = 0; i < params.length; i++) {
            baseText = baseText.replace("{%" + i + "}", parseTranslation(params[i]));
        }

        return baseText;
    }

    private static Map<String, String> loadLang(InputStream stream) {
        try {
            String content = Utils.readFile(stream);
            Map<String, String> d = new HashMap<>();
            for (String line : content.split("\n")) {
                line = line.trim();
                if (line.equals("") || line.charAt(0) == '#') {
                    continue;
                }
                String[] t = line.split("=");
                if (t.length < 2) {
                    continue;
                }
                String key = t[0];
                String value = "";
                for (int i = 1; i < t.length - 1; i++) {
                    value += t[i] + "=";
                }
                value += t[t.length - 1];
                if (value.equals("")) {
                    continue;
                }
                d.put(key, value);
            }
            return d;
        } catch (IOException e) {
            Server.getInstance().getLogger().logException(e);
            return null;
        }
    }

    private static String internalGet(String id) {
        if (lang.containsKey(id)) {
            return lang.get(id);
        } else if (fallbackLang.containsKey(id)) {
            return fallbackLang.get(id);
        }
        return null;
    }

    private static String get(String id) {
        if (lang.containsKey(id)) {
            return lang.get(id);
        } else if (fallbackLang.containsKey(id)) {
            return fallbackLang.get(id);
        }
        return id;
    }

    private static String parseTranslation(String text) {
        String newString = "";
        text = String.valueOf(text);

        String replaceString = null;

        int len = text.length();

        for (int i = 0; i < len; ++i) {
            char c = text.charAt(i);
            if (replaceString != null) {
                int ord = c;
                if ((ord >= 0x30 && ord <= 0x39) // 0-9
                        || (ord >= 0x41 && ord <= 0x5a) // A-Z
                        || (ord >= 0x61 && ord <= 0x7a) || // a-z
                        c == '.' || c == '-') {
                    replaceString += String.valueOf(c);
                } else {
                    String t = internalGet(replaceString.substring(1));
                    if (t != null) {
                        newString += t;
                    } else {
                        newString += replaceString;
                    }
                    replaceString = null;
                    if (c == '%') {
                        replaceString = String.valueOf(c);
                    } else {
                        newString += String.valueOf(c);
                    }
                }
            } else if (c == '%') {
                replaceString = String.valueOf(c);
            } else {
                newString += String.valueOf(c);
            }
        }

        if (replaceString != null) {
            String t = internalGet(replaceString.substring(1));
            if (t != null) {
                newString += t;
            } else {
                newString += replaceString;
            }
        }
        return newString;
    }
}