package com.xmonster.tkclient.utils;

import android.text.TextUtils;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TextUtils.class)
public class HelperTest {
    private Map<String, String> props;
    @Test
    public void toReversePolish1() throws Exception {
        TestCase.assertTrue(
            Conditions.Helper.toReversePolish("test>1 && (test<2 || uid=100)").equals(
                Arrays.asList("test>1", "test<2", "uid=100", "|", "&"))
        );
    }

    @Test
    public void calcReversePolish() throws Exception {
        List<String> list = Arrays.asList("test>1", "city==shanghai", "&");
        TestCase.assertTrue(Conditions.Helper.calcReversePolish(list, props));

        List<String> list1 = Arrays.asList("test>2", "city==shanghai", "&");
        TestCase.assertFalse(Conditions.Helper.calcReversePolish(list1, props));

        List<String> list2 = Arrays.asList("test==2", "city!=shangha", "&");
        TestCase.assertTrue(Conditions.Helper.calcReversePolish(list2, props));
    }

    @Test
    public void calc() throws Exception {
        List<String> list = Conditions.Helper.toReversePolish("test>1 && (test<2 || uid==100)");
        TestCase.assertFalse(Conditions.Helper.calcReversePolish(list, props));

        List<String> list1 = Conditions.Helper.toReversePolish("test>1 && (test<2 || uid==101)");
        TestCase.assertTrue(Conditions.Helper.calcReversePolish(list1, props));
    }

    @Test
    public void calcExpr() throws Exception {
        TestCase.assertTrue(Conditions.Helper.calcExpr("test>=1", props));
        TestCase.assertFalse(Conditions.Helper.calcExpr("test>2", props));
        TestCase.assertTrue(Conditions.Helper.calcExpr("test<=3", props));
    }

    @Test
    public void calcExpr1() throws Exception {
        TestCase.assertFalse(Conditions.Helper.calcExpr("city>shanghai", props));
        TestCase.assertFalse(Conditions.Helper.calcExpr("city>=shanghai", props));
        TestCase.assertTrue(Conditions.Helper.calcExpr("city==shanghai", props));
        TestCase.assertTrue(Conditions.Helper.calcExpr("city!=guangdong", props));
    }

    @Test
    public void splitExpr() throws Exception {
        TestCase.assertTrue(
            Conditions.Helper.splitExpr("test>=1").equals(Arrays.asList("test", ">=", "1"))
        );
    }

    @Before
    public void setup() {
        PowerMockito.mockStatic(TextUtils.class);
        PowerMockito.when(TextUtils.join(any(CharSequence.class), any(Iterable.class))).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                StringBuilder sb = new StringBuilder();
                CharSequence delimiter = (CharSequence) invocation.getArguments()[0];
                Iterator<?> it = ((Iterable) invocation.getArguments()[1]).iterator();
                if (it.hasNext()) {
                    sb.append(it.next());
                    while (it.hasNext()) {
                        sb.append(delimiter);
                        sb.append(it.next());
                    }
                }
                return sb.toString();
            }
        });

        props = new HashMap<>();
        props.put("test", "2");
        props.put("city", "shanghai");
        props.put("uid", "101");
    }
}