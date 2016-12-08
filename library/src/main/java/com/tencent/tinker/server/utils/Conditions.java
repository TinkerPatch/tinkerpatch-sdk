/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Shengjie Sim Sun
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.tencent.tinker.server.utils;

import android.content.Context;
import android.text.TextUtils;

import com.tencent.tinker.lib.util.TinkerLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Pattern;

import static com.tencent.tinker.server.client.TinkerClientAPI.TAG;


public class Conditions {

    static final String  FILE_NAME   = "CONDITIONS_MAP";
    static final Pattern INT_PATTERN = Pattern.compile("-?[0-9]+");

    private final Map<String, String> properties;

    public Conditions() {
        properties = new HashMap<>();
    }

    public Boolean check(String rules) {
        if (TextUtils.isEmpty(rules)) {
            return true;
        }
        List<String> rpList = Helper.toReversePolish(rules);
        try {
            return Helper.calcReversePolish(rpList, properties);
        } catch (Exception ignore) {
            TinkerLog.e(TAG, "parse conditions error(have you written '==' as '='?): " + rules);
            TinkerLog.w(TAG, "exception:" + ignore);
            return false;
        }
    }

    /**
     * set the k,v to conditions map.
     * you should invoke {@link #saveToDisk(Context)} for saving the map to disk
     *
     * @param key   the key
     * @param value the value
     * @return {@link Conditions} this
     */
    public Conditions set(String key, String value) {
        properties.put(key, value);
        return this;
    }

    /**
     * Clean all properties. you should invoke {@link #saveToDisk(Context)} for saving to disk.
     *
     * @return {@link Conditions} this
     */
    public Conditions clean() {
        properties.clear();
        return this;
    }

    /**
     * saveToDisk
     *
     * @param context {@link Context}
     * @throws IOException
     */
    public void saveToDisk(Context context) throws IOException {
        File file = new File(context.getFilesDir(), FILE_NAME);
        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
        outputStream.writeObject(properties);
        outputStream.flush();
        outputStream.close();
    }


    static final class Helper {
        private static final String                   WITH_DELIMITER = "((?<=[%1$s])|(?=[%1$s]))";
        private static final List<String>             TOKENS         = new ArrayList<>(4);
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

        private Helper() {
            // A Util Class
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
            if (stack.isEmpty() || "(".equals(op)) {
                stack.push(op);
                return;
            }

            if (")".equals(op)) {
                String tmp;
                while (!"(".equals(tmp = stack.pop())) {
                    rpList.add(tmp);
                }
                return;
            }
            if ("(".equals(stack.peek())) {
                stack.push(op);
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
                    // lazy calcExpr at pop from stack, some expr needn't calculate.
                    // such 'true || expr'
                    stack.push(word);
                } else {
                    Boolean left, right;
                    Object v1, v2;
                    switch (word) {
                        case "|":
                            v1 = stack.pop();
                            v2 = stack.pop();
                            left = calcExpr(v1, props);
                            if (left) {
                                stack.push(Boolean.TRUE);
                                continue;
                            }
                            right = calcExpr(v2, props);
                            stack.push(right);
                            break;
                        case "&":
                            v1 = stack.pop();
                            v2 = stack.pop();
                            left = calcExpr(v1, props);
                            if (!left) {
                                stack.push(Boolean.FALSE);
                                continue;
                            }
                            right = calcExpr(v2, props);
                            stack.push(right);
                            break;
                        default:
                            throw new RuntimeException("Unsupported Operator: " + word);
                    }
                }
            }
            return calcExpr(stack.pop(), props);
        }

        public static Boolean calcExpr(Object obj, Map<String, String> props) {
            if (obj instanceof String) {
                return calcExpr((String) obj, props);
            } else if (obj instanceof Boolean) {
                return (Boolean) obj;
            } else {
                throw new RuntimeException("illegal type pass to calcExpr");
            }
        }

        public static Boolean calcExpr(String expr, Map<String, String> props) {
            boolean isInProps = false;
            List<String> exprList = splitExpr(expr);
            String op = exprList.get(1);
            String left = exprList.get(0);
            String right = exprList.get(2);
            if (props.containsKey(left)) {
                isInProps = true;
                left = props.get(left);
            }
            if (props.containsKey(right)) {
                isInProps = true;
                right = props.get(right);
            }
            return isInProps && calcExpr(left, right, op);
        }

        public static Boolean calcExpr(String left, String right, String op) {
            switch (op) {
                case "==":
                    return left.equals(right);
                case "!=":
                    return !left.equals(right);
                case ">=":
                    if (isInt(left)) {
                        return Integer.parseInt(left) >= Integer.parseInt(right);
                    } else {
                        return left.compareToIgnoreCase(right) >= 0;
                    }
                case ">":
                    if (isInt(left)) {
                        return Integer.parseInt(left) > Integer.parseInt(right);
                    } else {
                        return left.compareToIgnoreCase(right) > 0;
                    }
                case "<=":
                    if (isInt(left)) {
                        return Integer.parseInt(left) <= Integer.parseInt(right);
                    } else {
                        return left.compareToIgnoreCase(right) <= 0;
                    }
                case "<":
                    if (isInt(left)) {
                        return Integer.parseInt(left) < Integer.parseInt(right);
                    } else {
                        return left.compareToIgnoreCase(right) < 0;
                    }
                default:
                    throw new RuntimeException("Unsupported Operator");
            }
        }

        public static List<String> splitExpr(String expr) {
            String[] ops = new String[]{"==", "!=", ">=", "<=", ">", "<"};
            for (String op : ops) {
                if (expr.contains(op)) {
                    int pos = expr.indexOf(op);
                    String left = expr.substring(0, pos);
                    String right = expr.substring(pos + op.length(), expr.length());
                    return Arrays.asList(left, op, right);
                }
            }
            return new ArrayList<>();
        }

        private static Boolean isToken(String word) {
            return TOKENS.contains(word);
        }

        private static List<String> tokenize(String input) {
            input = input.replaceAll("\\s+", "")
                .replaceAll("&amp;", "&").replaceAll("&lt;", "<").replaceAll("&gt;", ">")
                .replaceAll("&&", "&").replaceAll("\\|\\|", "|");
            List<String> tokens = new ArrayList<>(TOKENS.size());
            for (String token : TOKENS) {
                tokens.add(Pattern.quote(token));
            }
            String splits = TextUtils.join("|", tokens);
            return Arrays.asList(input.split(String.format(WITH_DELIMITER, splits)));
        }

        private static Boolean isInt(String string) {
            return INT_PATTERN.matcher(string).matches();
        }
    }
}
