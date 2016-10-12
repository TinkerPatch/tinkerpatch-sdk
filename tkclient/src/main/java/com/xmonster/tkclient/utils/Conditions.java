package com.xmonster.tkclient.utils;

import android.content.Context;
import android.text.TextUtils;

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
import java.util.regex.Pattern;

/**
 * Created by sun on 11/10/2016.
 */

public class Conditions {

    private static final String FILE_NAME = "CONDITIONS_MAP";


    private final Map<String, String> properties;

    public Conditions (Context context) {
        properties = read(context);
    }

    public Boolean check(String rules) {
        List<String> rpList = Helper.toReversePolish(rules);
        return Helper.calcReversePolish(rpList, properties);
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

    static class Helper {
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

        public static List<String> toReversePolish(String input) {
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
            return rpList;
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

        public static Boolean calcReversePolish(List<String> list, Map<String, String> props) {
            Stack<Object> stack = new Stack<>();
            for (String word : list) {
                if (!isToken(word)) {
                    stack.push(word);
                } else {
                    switch (word) {
                        case "|": {
                            Boolean left, right;
                            Object v1 = stack.pop();
                            Object v2 = stack.pop();
                            left = calcExpr((String) v1, props);
                            if (left) {
                                stack.push(Boolean.TRUE);
                                continue;
                            }
                            right = calcExpr((String) v2, props);
                            stack.push(right);
                            break;
                        }
                        case "&": {
                            Boolean left, right;
                            Object v1 = stack.pop();
                            Object v2 = stack.pop();
                            left = calcExpr(v1, props);
                            if (!left) {
                                stack.push(Boolean.FALSE);
                                continue;
                            }
                            right = calcExpr(v2, props);
                            stack.push(right);
                            break;
                        }
                        default:
                            throw new RuntimeException("Unsupported Operator:" + word);
                    }
                }
            }
            return (Boolean) stack.pop();
        }
        public static Boolean calcExpr(Object obj, Map<String, String>props) {
            if (obj instanceof String) {
                return calcExpr((String)obj, props);
            } else if (obj instanceof Boolean) {
                return (Boolean) obj;
            } else {
                throw new RuntimeException("illegal type pass to calcExpr");
            }
        }

        public static Boolean calcExpr(String expr, Map<String, String>props) {
            List<String> exprList = splitExpr(expr);
            String op = exprList.get(1);
            String left = exprList.get(0);
            String right = exprList.get(2);
            if (props.containsKey(left)) {
                left = props.get(left);
            }
            if (props.containsKey(right)) {
                right = props.get(right);
            }
            return calcExpr(left, right, op);
        }

        public static Boolean calcExpr(String left, String right, String op) {
            switch (op) {
                case "==":
                    return left.equals(right);
                case "!=":
                    return !left.equals(right);
                case ">=":
                    if (isInt(left)) {
                        return Integer.valueOf(left) >= Integer.valueOf(right);
                    } else {
                        return left.compareToIgnoreCase(right) >= 0;
                    }
                case ">":
                    if (isInt(left)) {
                        return Integer.valueOf(left) > Integer.valueOf(right);
                    } else {
                        return left.compareToIgnoreCase(right) > 0;
                    }
                case "<=":
                    if (isInt(left)) {
                        return Integer.valueOf(left) <= Integer.valueOf(right);
                    } else {
                        return left.compareToIgnoreCase(right) <= 0;
                    }
                case "<":
                    if (isInt(left)) {
                        return Integer.valueOf(left) < Integer.valueOf(right);
                    } else {
                        return left.compareToIgnoreCase(right) < 0;
                    }
                default:
                    throw new RuntimeException("Unsupported Operator");
            }
        }

        public static List<String> splitExpr(String expr) {
            String[] ops = new String[] {"==", "!=", ">=", "<=", ">", "<"};
            for (String op : ops) {
                if (expr.contains(op)) {
                    int pos = expr.indexOf(op);
                    String left = expr.substring(0, pos);
                    String right = expr.substring(pos+op.length(), expr.length());
                    return Arrays.asList(left, op, right);
                }
            }
            return new ArrayList<>();
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

        private static final Pattern INT_PATTERN = Pattern.compile("-?[0-9]+");
        private static Boolean isInt(String string) {
            return INT_PATTERN.matcher(string).matches();
        }
    }
}
