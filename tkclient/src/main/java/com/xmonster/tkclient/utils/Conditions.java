package com.xmonster.tkclient.utils;

import android.content.Context;
import android.text.TextUtils;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sun on 11/10/2016.
 */

public class Conditions {

    private static final String FILE_NAME = "CONDITIONS_MAP";
    private static final Pattern INT_PATTERN = Pattern.compile("-?[0-9]+");

    private final Map<String, String> properties;

    public Conditions (Context context) {
        properties = read(context);
    }

    public Boolean check(String rules) {
        return true;
    }

    public Conditions set(String key, String value) {
        properties.put(key, value);
        return this;
    }

    public Conditions clean() {
        properties.clear();
        return this;
    }

    public void save(Context context) throws IOException {
        File file = new File(context.getFilesDir(), FILE_NAME);
        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
        outputStream.writeObject(properties);
        outputStream.flush();
        outputStream.close();
    }

    private HashMap<String, String> read(Context context) {
        try {
            File file = new File(context.getFilesDir(), FILE_NAME);
            if (file.exists()) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                return (HashMap<String, String>) ois.readObject();
            }
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }
        return new HashMap<>();
    }

    private static Boolean isInt(String string) {
        return INT_PATTERN.matcher(string).matches();
    }

    static class Parser {
        private static final String WITH_DELIMITER = "((?<=[%1$s])|(?=[%1$s]))";
        private static final List<String> TOKENS = new ArrayList<>(4);
        private static final HashMap<String, Integer> TOKEN_PRIORITY = new HashMap<>();
        static {
            TOKENS.add("&");
            TOKENS.add("|");
            TOKENS.add("(");
            TOKENS.add(")");

            TOKEN_PRIORITY.put("&", 2);
            TOKEN_PRIORITY.put("|", 1);
            TOKEN_PRIORITY.put("(", 3);
            TOKEN_PRIORITY.put(")", 3);
        }

        public static boolean calc(String input) {
            Stack<String> opStack = new Stack<>();
            List<String> rpList = new LinkedList<>();
            for (String word : tokenize(input)) {
                if (isToken(word)) {
                    pushOp(opStack, rpList, word);
                } else {
                    rpList.add(word);
                }
            }
            while (!opStack.isEmpty()) {
                rpList.add(opStack.pop());
            }
            System.out.println(rpList);
            return true;
        }

        private static void pushOp(Stack<String> stack, List<String> rpList, String op) {
            if (stack.isEmpty() || op.equals("(")) {
                stack.push(op);
                return;
            }

            if (stack.peek().equals("(")) {
                stack.push(op);
                return;
            }

            if (op.equals(")")) {
                String tmp;
                while(!"(".equals(tmp=stack.pop())){
                    rpList.add(tmp);
                }
                return;
            }

            if (TOKEN_PRIORITY.get(op) > TOKEN_PRIORITY.get(stack.peek())) {
                stack.push(op);
            } else {
                rpList.add(stack.pop());
                pushOp(stack, rpList, op);
            }
        }

        private static Boolean isToken(String word) {
            return TOKENS.contains(word);
        }

        private static List<String> tokenize(String input) {
            input = input.replaceAll("\\s+","").replaceAll("&&","&").replaceAll("\\|\\|","|");
            List<String> tokens = new ArrayList<>(TOKENS.size());
            for (String token : TOKENS) {
                tokens.add(Pattern.quote(token));
            }
            String splits = TextUtils.join("|", tokens);
            return Arrays.asList(input.split(String.format(WITH_DELIMITER, splits)));
        }
    }
}
